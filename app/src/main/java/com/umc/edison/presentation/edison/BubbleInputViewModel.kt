package com.umc.edison.presentation.edison

import android.content.Context
import android.net.Uri
import android.os.Build
import android.text.Html
import androidx.annotation.RequiresApi
import androidx.lifecycle.SavedStateHandle
import com.umc.edison.domain.usecase.bubble.AddBubbleUseCase
import com.umc.edison.domain.usecase.bubble.GetAllBubblesUseCase
import com.umc.edison.domain.usecase.bubble.GetBubbleUseCase
import com.umc.edison.domain.usecase.bubble.UpdateBubbleUseCase
import com.umc.edison.domain.usecase.label.AddLabelUseCase
import com.umc.edison.domain.usecase.label.GetAllLabelsUseCase
import com.umc.edison.presentation.ToastManager
import com.umc.edison.presentation.base.BaseViewModel
import com.umc.edison.presentation.label.LabelEditMode
import com.umc.edison.presentation.model.BubbleModel
import com.umc.edison.presentation.model.ContentBlockModel
import com.umc.edison.presentation.model.ContentType
import com.umc.edison.presentation.model.LabelModel
import com.umc.edison.presentation.model.toPresentation
import com.umc.edison.ui.components.IconType
import com.umc.edison.ui.components.ListStyle
import com.umc.edison.ui.components.TextStyle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import androidx.core.net.toUri
import io.reactivex.internal.util.BackpressureHelper.add
import java.util.LinkedList

// 삽입 타깃: 텍스트 뒤 or (왼쪽,오른쪽) 갭
private sealed class InsertionTarget {
    data object None : InsertionTarget()
    data class AfterText(val textIndex: Int) : InsertionTarget() // position 기반(현 UI 호환)
    data class Between(val leftIndex: Int?, val rightIndex: Int?) : InsertionTarget()
}

// 체인 노드(뷰모델 내부 전용)
private data class Node(
    val id: String = java.util.UUID.randomUUID().toString(),
    val block: ContentBlockModel,
    var prev: String? = null,
    var next: String? = null
)

// 아주 얇은 링크드 리스트 래퍼
private class EditorChain {
    private val nodes = mutableMapOf<String, Node>()
    private var head: String? = null
    private var tail: String? = null

    fun clear() { nodes.clear(); head = null; tail = null }

    fun fromLinear(linear: List<ContentBlockModel>) {
        clear()
        var prevId: String? = null
        linear.forEach { b ->
            val id = b.id.ifBlank { java.util.UUID.randomUUID().toString() }
            val n = Node(id = id, block = b.copy(id = id, position = 0))
            nodes[n.id] = n
            link(prevId, n.id)
            prevId = n.id
        }
    }

    fun toLinear(): List<Node> {
        val out = mutableListOf<Node>()
        var cur = head
        while (cur != null) {
            val n = nodes[cur] ?: break
            out += n
            cur = n.next
        }
        out.forEachIndexed { idx, n -> n.block.position = idx }
        return out
    }

    private fun link(left: String?, right: String?) {
        if (left != null) nodes[left]?.next = right else head = right
        if (right != null) nodes[right]?.prev = left else tail = left
    }

    fun headId() = head
    fun tailId() = tail
    fun node(id: String) = nodes[id]
    fun isText(id: String?) = id != null && nodes[id]?.block?.type == ContentType.TEXT
    fun isImage(id: String?) = id != null && nodes[id]?.block?.type == ContentType.IMAGE

    fun insertAfter(anchorId: String?, block: ContentBlockModel): String {
        val newId = java.util.UUID.randomUUID().toString()
        val next = if (anchorId == null) head else nodes[anchorId]?.next
        val newBlock = block.copy(id = newId, position = 0)
        nodes[newId] = Node(id = newId, block = newBlock, prev = anchorId, next = next)
        link(anchorId, newId); link(newId, next)
        return newId
    }

    fun insertBetween(leftId: String?, rightId: String?, block: ContentBlockModel): String {
        val newId = java.util.UUID.randomUUID().toString()
        val newBlock = block.copy(id = newId, position = 0)
        nodes[newId] = Node(id = newId, block = newBlock, prev = leftId, next = rightId)
        link(leftId, newId); link(newId, rightId)
        return newId
    }

    fun remove(targetId: String) {
        val n = nodes[targetId] ?: return
        link(n.prev, n.next)
        nodes.remove(targetId)
    }
}




@HiltViewModel
class BubbleInputViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    toastManager: ToastManager,
    private val getAllLabelsUseCase: GetAllLabelsUseCase,
    private val getBubbleUseCase: GetBubbleUseCase,
    private val getAllBubblesUseCase: GetAllBubblesUseCase,
    private val addLabelUseCase: AddLabelUseCase,
    private val addBubbleUseCase: AddBubbleUseCase,
    private val updateBubbleUseCase: UpdateBubbleUseCase,
) : BaseViewModel(toastManager) {
    private val chain = EditorChain()
    private var lastFocusedTextIndex: Int? = null // 텍스트 포커스 기억(없으면 기본 규칙)

    private val _uiState = MutableStateFlow(BubbleInputState.DEFAULT.copy(
        bubble = BubbleModel.DEFAULT.copy(contentBlocks = LinkedList())
    ))
    val uiState = _uiState.asStateFlow()

    private fun setInsertionTarget(target: InsertionTarget) {
        _uiState.update { it.copy(/* state에 노출하고 싶으면 */ /* insertionTarget = target */) }
        // 내부 보관은 뷰모델 변수로:
        _currentInsertionTarget = target
    }
    private var _currentInsertionTarget: InsertionTarget = InsertionTarget.None

    private fun publish() {
        val ordered = chain.toLinear().map { it.block }
        _uiState.update { s -> s.copy(bubble = s.bubble.copy(contentBlocks = ordered)) }
        checkCanSave()
    }

    // 체인(문서)에 최소 1개의 TEXT 블록이 항상 존재하도록
    private fun ensureInitialText() {
        if (chain.headId() == null) {
            chain.insertAfter(null, ContentBlockModel(ContentType.TEXT, "", 0))
        }
    }

    // 체인의 마지막 블록은 항상 TEXT가 되도록
    private fun ensureTrailingText() {
        if (chain.tailId() != null) {
            val tailIsImage = chain.isImage(chain.tailId())
            if (tailIsImage) chain.insertAfter(chain.tailId(), ContentBlockModel(ContentType.TEXT, "", 0))
        }
    }




    init {
        val id: String? = savedStateHandle["bubbleId"]
        fetchBubble(id)
        fetchLabels()
        fetchBubbles()
    }

//    private fun fetchBubble(bubbleId: String?) {
//        if (bubbleId.isNullOrEmpty()) {
//            addTextBlockToFront()
//            return
//        }
//
//        collectDataResource(
//            flow = getBubbleUseCase(bubbleId),
//            onSuccess = { bubble ->
//                val sortedContentBlocks = bubble.toPresentation().contentBlocks.sortedBy { it.position }
//                _uiState.update {
//                    it.copy(
//                        bubble = bubble.toPresentation().copy(contentBlocks = LinkedList(sortedContentBlocks)),
//                        selectedLabels = bubble.labels.toPresentation()
//                    )
//                }
//            },
//            onComplete = {
//                addTextBlockToFront()
//            }
//        )
//    }

    private fun fetchBubble(bubbleId: String?) {
        if (bubbleId.isNullOrEmpty()) {
            chain.fromLinear(emptyList())
            ensureInitialText()
            publish()
            return
        }

        collectDataResource(
            flow = getBubbleUseCase(bubbleId),
            onSuccess = { bubble ->
                val pres = bubble.toPresentation()
                val sorted = pres.contentBlocks.sortedBy { it.position }

                chain.fromLinear(sorted)
                ensureInitialText()
                val orderedBlocks = chain.toLinear().map { it.block }

                _uiState.update {
                    it.copy(
                        bubble = pres.copy(
                            contentBlocks = orderedBlocks
                        ),
                        selectedLabels = pres.labels
                    )
                }
                checkCanSave()
            },
            onComplete = { ensureInitialText(); publish() }
        )
    }


    fun onEnterAt(textIndex: Int) {
        // position 기반으로 해당 위치 뒤에 새 텍스트 삽입
        val ordered = chain.toLinear()
        val anchorId = ordered.getOrNull(textIndex)?.id
        chain.insertAfter(anchorId, ContentBlockModel(ContentType.TEXT, "", 0))
        publish()
    }

    fun onTextFocused(textIndex: Int) { lastFocusedTextIndex = textIndex; setInsertionTarget(InsertionTarget.AfterText(textIndex)) }



    private fun fetchBubbles() {
        collectDataResource(
            flow = getAllBubblesUseCase(),
            onSuccess = { bubbles ->
                _uiState.update {
                    it.copy(
                        bubbles = bubbles.toPresentation().filter { bubble ->
                            bubble.id != _uiState.value.bubble.id
                        }
                    )
                }
            },
        )
    }

    private fun fetchLabels() {
        collectDataResource(
            flow = getAllLabelsUseCase(),
            onSuccess = { labels ->
                _uiState.update {
                    it.copy(
                        labels = labels.toPresentation(),
                    )
                }
            },
        )
    }

    fun saveLabel(label: LabelModel) {
        collectDataResource(
            flow = addLabelUseCase(label.toDomain()),
            onSuccess = {
                updateLabelEditMode(LabelEditMode.EDIT)
                fetchLabels()
            },
        )
    }

    fun updateSelectedLabels(labels: List<LabelModel>) {
        _uiState.update {
            it.copy(
                bubble = it.bubble.copy(
                    labels = labels
                ),
                selectedLabels = labels
            )
        }
    }

    fun updateIcon(iconType: IconType) {
        if (iconType == IconType.CAMERA && _uiState.value.selectedIcon == IconType.CAMERA) {
            _uiState.update { it.copy(selectedIcon = IconType.NONE) }
            return
        }

        if (iconType == IconType.LINK && _uiState.value.selectedIcon == IconType.LINK) {
            _uiState.update { it.copy(selectedIcon = IconType.NONE) }
            return
        }

        if (iconType == IconType.NONE) {
            _uiState.update {
                it.copy(
                    selectedTextStyles = emptyList(),
                    selectedListStyle = ListStyle.NONE
                )
            }
        }

        if (iconType == IconType.TAG) {
            fetchLabels()
        }

        if (iconType == IconType.TAG) {
            updateLabelEditMode(LabelEditMode.EDIT)
        }

        _uiState.update { it.copy(selectedIcon = iconType) }
    }

    fun updateTextStyle(textStyle: TextStyle) {
        val selectedTextStyles = _uiState.value.selectedTextStyles.toMutableList()

        if (selectedTextStyles.contains(textStyle)) {
            selectedTextStyles.remove(textStyle)
        } else {
            selectedTextStyles.add(textStyle)
        }

        _uiState.update { it.copy(selectedTextStyles = selectedTextStyles) }
    }

    fun updateListStyle(listStyle: ListStyle) {
        _uiState.update { it.copy(selectedListStyle = listStyle) }
    }

    fun updateLabelEditMode(labelEditMode: LabelEditMode) {
        _uiState.update { it.copy(labelEditMode = labelEditMode) }
    }

    private fun addTextBlock() {
        val newTextBlock = ContentBlockModel(
            type = ContentType.TEXT,
            content = "",
            position = _uiState.value.bubble.contentBlocks.size
        )

        if (_uiState.value.bubble.contentBlocks.isEmpty()) {
            _uiState.update {
                it.copy(
                    bubble = it.bubble.copy(
                        contentBlocks = listOf(newTextBlock)
                    )
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    bubble = it.bubble.copy(
                        contentBlocks = it.bubble.contentBlocks + newTextBlock
                    )
                )
            }
        }
    }

    private fun addTextBlockToFront() {
        if (_uiState.value.bubble.contentBlocks.isEmpty()) {
            addTextBlock()
            return
        }

        if (_uiState.value.bubble.contentBlocks[0].type == ContentType.TEXT && _uiState.value.bubble.contentBlocks.last().type == ContentType.TEXT) {
            return
        }

        if (_uiState.value.bubble.contentBlocks[0].type == ContentType.TEXT) {
            return
        }

        val newTextBlock = ContentBlockModel(
            type = ContentType.TEXT,
            content = "",
            position = 0
        )

        val currentContentBlocks = _uiState.value.bubble.contentBlocks.map {
            it.copy(position = it.position + 1)
        }

        _uiState.update {
            val updatedContentBlocks = LinkedList(listOf(newTextBlock) + currentContentBlocks)
            it.copy(
                bubble = it.bubble.copy(
                    contentBlocks = updatedContentBlocks
                )
            )
        }

        if (_uiState.value.bubble.contentBlocks.last().type == ContentType.IMAGE) {
            addTextBlock()
        }
    }

//    fun addContentBlocks() {
//        val imagePaths = _uiState.value.selectedImages.filter { imageUri ->
//            _uiState.value.bubble.contentBlocks.none { it.content == imageUri.toString() }
//        }
//
//        val newImageBlocks = mutableListOf<ContentBlockModel>()
//
//        imagePaths.forEachIndexed { idx, imagePath ->
//            val newImageBlock = ContentBlockModel(
//                type = ContentType.IMAGE,
//                content = imagePath.toString(),
//                position = _uiState.value.bubble.contentBlocks.size + idx
//            )
//
//            newImageBlocks.add(newImageBlock)
//        }
//
//        val newTextBlock = ContentBlockModel(
//            type = ContentType.TEXT,
//            content = "",
//            position = _uiState.value.bubble.contentBlocks.size + imagePaths.size
//        )
//
//        _uiState.update {
//            it.copy(
//                bubble = it.bubble.copy(
//                    contentBlocks = it.bubble.contentBlocks + newImageBlocks + newTextBlock
//                ),
//                isGalleryOpen = false,
//                selectedIcon = IconType.NONE
//            )
//        }
//    }

    fun addContentBlocks() {
        // 기존처럼 selectedImages를 사용 (UI 변경 최소화)
        val imageUris = _uiState.value.selectedImages
            .filter { uri ->
                // 중복 삽입 방지: 현재 체인에 동일 content가 있는지
                chain.toLinear().none { it.block.type == ContentType.IMAGE && it.block.content == uri.toString() }
            }

        if (imageUris.isEmpty()) {
            _uiState.update { it.copy(isGalleryOpen = false, selectedIcon = IconType.NONE) }
            return
        }

        when (val target = _currentInsertionTarget) {
            is InsertionTarget.AfterText -> addImagesAfterText(target.textIndex, imageUris)
            is InsertionTarget.Between    -> addImagesAtGap(target.leftIndex, target.rightIndex, imageUris)
            else -> addImagesDefault(imageUris) // 타깃이 없으면 적절한 기본 규칙
        }

        _uiState.update {
            it.copy(
                isGalleryOpen = false,
                selectedIcon = IconType.NONE,
                selectedImages = emptyList() // 소비 후 비움
            )
        }
    }

    private fun addImagesDefault(uris: List<Uri>) {
        // 기본: 마지막 텍스트 뒤에 삽입 (없으면 맨 뒤에 텍스트 하나 만들고 그 뒤)
        val ordered = chain.toLinear()
        val lastTextIndex = ordered.indexOfLast { it.block.type == ContentType.TEXT }
            .takeIf { it >= 0 } ?: run {
            ensureInitialText()
            chain.toLinear().indexOfLast { it.block.type == ContentType.TEXT }
        }
        addImagesAfterText(lastTextIndex, uris)
    }

    private fun addImagesAfterText(textIndex: Int, uris: List<Uri>) {
        val ordered = chain.toLinear()
        val textNode = ordered.getOrNull(textIndex) ?: return
        require(textNode.block.type == ContentType.TEXT)
        val isEmpty = textNode.block.content.parseHtml().isBlank()

        var anchorId = textNode.id
        uris.forEach { uri ->
            anchorId = chain.insertAfter(anchorId, ContentBlockModel(ContentType.IMAGE, uri.toString(), 0))
        }
        if (!isEmpty) {
            chain.insertAfter(anchorId, ContentBlockModel(ContentType.TEXT, "", 0))
        }
        ensureTrailingText()
        publish()
    }

    private fun addImagesAtGap(leftIndex: Int?, rightIndex: Int?, uris: List<Uri>) {
        val ordered = chain.toLinear()
        val leftId = leftIndex?.let { ordered.getOrNull(it)?.id }
        val rightId = rightIndex?.let { ordered.getOrNull(it)?.id }

        val leftIsImg = leftId?.let { chain.isImage(it) } ?: false
        val rightIsImg = rightId?.let { chain.isImage(it) } ?: false

        if (leftIsImg && rightIsImg) {
            // 규칙 4: 이미지-이미지 사이면 먼저 빈 텍스트 삽입 후 그 뒤에 이미지들
            val midTextId = chain.insertBetween(leftId, rightId, ContentBlockModel(ContentType.TEXT, "", 0))
            val midIndex = chain.toLinear().indexOfFirst { it.id == midTextId }
            addImagesAfterText(midIndex, uris)
            return
        }

        var prev = leftId
        uris.forEach { uri ->
            prev = chain.insertBetween(prev, rightId, ContentBlockModel(ContentType.IMAGE, uri.toString(), 0))
        }
        ensureTrailingText()
        publish()
    }

    fun closeGallery() {
        _uiState.update { it.copy(isGalleryOpen = false, selectedIcon = IconType.NONE) }
    }

    //@RequiresApi(Build.VERSION_CODES.N)
//    fun updateBubbleContent(bubble: BubbleModel) {
//        _uiState.update {
//            it.copy(
//                bubble = bubble
//            )
//        }
//
//        checkCanSave()
//    }
    @RequiresApi(Build.VERSION_CODES.N)
    fun updateBubbleContent(bubble: BubbleModel) {
        // 텍스트 블록 내용만 체인에 반영(순서/삽입/삭제는 체인 API로만)
        val ordered = chain.toLinear()
        bubble.contentBlocks.forEach { incoming ->
            if (incoming.type == ContentType.TEXT) {
                val node = ordered.getOrNull(incoming.position)
                if (node != null && node.block.type == ContentType.TEXT) {
                    node.block.content = incoming.content
                }
            }
        }
        // 기타 메타 필드만 교체
        _uiState.update {
            it.copy(
                bubble = it.bubble.copy(
                    title = bubble.title,
                    mainImage = bubble.mainImage,
                    labels = bubble.labels,
                    backLinks = bubble.backLinks,
                    linkedBubble = bubble.linkedBubble
                )
            )
        }
        checkCanSave()
    }


    fun deleteBackLink(targetBackLink: BubbleModel) {
        val currentBubble = _uiState.value.bubble
        val updatedBackLinks = currentBubble.backLinks
            .filterNot { it.id == targetBackLink.id }


        _uiState.update { currentState ->
            currentState.copy(
                bubble = currentBubble.copy(
                    backLinks = updatedBackLinks
                )
            )
        }
    }

    fun deleteLinkBubble(targetLinkBubble: BubbleModel) {
        val currentBubble = _uiState.value.bubble

        _uiState.update { currentState ->
            currentState.copy(
                bubble = currentBubble.copy(
                    linkedBubble = if (currentBubble.linkedBubble?.id == targetLinkBubble.id) null else currentBubble.linkedBubble
                )
            )
        }
    }

//    fun deleteContentBlock(contentBlock: ContentBlockModel) {
//        if (contentBlock.type != ContentType.IMAGE) return
//
//        val currentBubble = _uiState.value.bubble
//        val contentBlocks = currentBubble.contentBlocks.sortedBy { it.position }.toMutableList()
//
//        val targetIndex = contentBlocks.indexOfFirst {
//            it.position == contentBlock.position
//            it.content == contentBlock.content
//        }
//
//        if (targetIndex == -1) return
//
//        if (_uiState.value.selectedImages.contains(contentBlock.content.toUri())) {
//            _uiState.update { it.copy(selectedImages = it.selectedImages - contentBlock.content.toUri()) }
//        }
//
//        if (targetIndex == 0) {
//            contentBlocks.removeAt(targetIndex)
//            _uiState.update { it.copy(bubble = it.bubble.copy(contentBlocks = contentBlocks)) }
//
//            if (contentBlocks.isEmpty() || contentBlocks[0].type == ContentType.IMAGE) {
//                addTextBlockToFront()
//            }
//            return
//        }
//
//        if (targetIndex == currentBubble.contentBlocks.lastIndex) {
//            contentBlocks.removeAt(targetIndex)
//            _uiState.update { it.copy(bubble = it.bubble.copy(contentBlocks = contentBlocks)) }
//
//            if (contentBlocks.last().type == ContentType.IMAGE) {
//                addTextBlock()
//            }
//            return
//        }
//
//        if (contentBlocks[targetIndex - 1].type == ContentType.TEXT && contentBlocks[targetIndex + 1].type == ContentType.TEXT) {
//            contentBlocks[targetIndex - 1] = contentBlocks[targetIndex - 1].copy(
//                content = contentBlocks[targetIndex - 1].content + contentBlocks[targetIndex + 1].content
//            )
//
//            contentBlocks.removeAt(targetIndex)
//            contentBlocks.removeAt(targetIndex)
//
//            _uiState.update { it.copy(bubble = it.bubble.copy(contentBlocks = contentBlocks)) }
//            return
//        }
//
//        contentBlocks.removeAt(targetIndex)
//        _uiState.update {
//            it.copy(
//                bubble = it.bubble.copy(contentBlocks = contentBlocks)
//            )
//        }
//    }

    fun deleteContentBlock(contentBlock: ContentBlockModel) {
        if (contentBlock.type != ContentType.IMAGE) return

        val targetId = chain.toLinear()
            .firstOrNull { it.block.type == ContentType.IMAGE && it.block.content == contentBlock.content && it.block.position == contentBlock.position }
            ?.id ?: return

        // selectedImages에 남아있다면 제거
        val uri = contentBlock.content.toUri()
        if (_uiState.value.selectedImages.contains(uri)) {
            _uiState.update { it.copy(selectedImages = it.selectedImages - uri) }
        }

        // 제거 + 양옆 텍스트 머지
        val orderedBefore = chain.toLinear()
        val idx = orderedBefore.indexOfFirst { it.id == targetId }
        val left = orderedBefore.getOrNull(idx - 1)?.id
        val right = orderedBefore.getOrNull(idx + 1)?.id

        chain.remove(targetId)

        if (left != null && right != null && chain.isText(left) && chain.isText(right)) {
            val leftNode = chain.node(left)!!
            val rightNode = chain.node(right)!!
            leftNode.block.content += rightNode.block.content
            chain.remove(rightNode.id)
        }

        // 맨 앞이 이미지면 맨 앞에 텍스트 보정
        if (chain.headId() != null && chain.isImage(chain.headId())) {
            chain.insertBetween(null, chain.headId(), ContentBlockModel(ContentType.TEXT, "", 0))
        }

        ensureTrailingText()
        publish()
    }

    /** 갭(leftIndex, rightIndex)을 탭했을 때 호출: 빈 TEXT 생성 + 포커스 이동 + 삽입 타깃 지정 */
    fun onGapTapped(leftIndex: Int?, rightIndex: Int?) {
        val ordered = chain.toLinear()
        val leftId = leftIndex?.let { ordered.getOrNull(it)?.id }
        val rightId = rightIndex?.let { ordered.getOrNull(it)?.id }

        // 이미지-이미지 사이면 규칙상 먼저 TEXT를 만들어야 하고, 그게 우리가 원하는 동작과 동일
        val newTextId = chain.insertBetween(
            leftId,
            rightId,
            ContentBlockModel(ContentType.TEXT, "", 0)
        )

        // 새 텍스트의 position 계산
        val newIndex = chain.toLinear().indexOfFirst { it.id == newTextId }

        // UI에 포커스 타깃 알려주기 + 이후 이미지 추가시 이 위치를 기준으로
        _currentInsertionTarget = InsertionTarget.AfterText(newIndex) // 다음 이미지 삽입도 여기 기준
        publish()

        // 포커스 줄 position을 상태로 전달
        _uiState.update { it.copy(focusedTextIndex = newIndex) }
    }

    /** 포커스가 적용된 뒤 다시 null로 되돌릴 때 호출 (UI에서 한 번 적용 후 호출) */
    fun clearFocusedIndex() {
        _uiState.update { it.copy(focusedTextIndex = null) }
    }



    @RequiresApi(Build.VERSION_CODES.N)
    fun updateBubbleWithLink() {
        saveBubble(true)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun saveBubble(isLinked: Boolean = false) {
        trimBlankBlock()
        checkCanSave()
        if (!uiState.value.canSave) { showToast("내용을 입력해주세요."); addTextBlockToFront(); return }

        collectDataResource(
            flow = if (uiState.value.bubble.id.isNullOrEmpty())
                addBubbleUseCase(uiState.value.bubble.toDomain())
            else
                updateBubbleUseCase(uiState.value.bubble.toDomain()),
            onSuccess = { savedBubble ->
                showToast("저장되었습니다.")

                if (isLinked) {
                    _uiState.update {
                        BubbleInputState.DEFAULT.copy(
                            bubble = BubbleModel.DEFAULT.copy(linkedBubble = savedBubble.toPresentation()),
                            bubbles = it.bubbles + savedBubble.toPresentation()
                        )
                    }
                    addTextBlock()
                } else {
                    _uiState.update {
                        it.copy(
                            bubble = savedBubble.toPresentation()
                        )
                    }
                }
            },
        )
    }


//    @RequiresApi(Build.VERSION_CODES.N)
//    private fun checkCanSave() {
//        var canSave = true
//
//        val bubble = _uiState.value.bubble
//
//        if (bubble.title.isNullOrEmpty() && bubble.mainImage.isNullOrEmpty()) {
//            canSave = if (bubble.contentBlocks.isEmpty()) {
//                false
//            } else if (bubble.contentBlocks.size == 1) {
//                bubble.contentBlocks[0].content.parseHtml()
//                    .isNotEmpty() && bubble.contentBlocks[0].content != "<br>"
//            } else {
//                (bubble.contentBlocks[0].content.parseHtml()
//                    .isNotEmpty() && bubble.contentBlocks[0].content != "<br>")
//                        || (bubble.contentBlocks[1].content.parseHtml()
//                    .isNotEmpty() && bubble.contentBlocks[1].content != "<br>")
//            }
//        }
//
//        _uiState.update { it.copy(canSave = canSave) }
//    }
//
//    @RequiresApi(Build.VERSION_CODES.N)
//    private fun trimBlankBlock() {
//        val contentBlocks = _uiState.value.bubble.contentBlocks.toMutableList()
//        val updatedContentBlocks = mutableListOf<ContentBlockModel>()
//
//        contentBlocks.forEachIndexed { _, contentBlock ->
//            if (contentBlock.type == ContentType.TEXT) {
//                val contents = contentBlock.content.split("<br>")
//                val newContent = if (contents.size > 1) {
//                    contents.joinToString(separator = "") { it.trim() }
//                } else {
//                    contentBlock.content
//                }
//
//                if (newContent.parseHtml().trim().isNotEmpty()) {
//                    updatedContentBlocks.add(contentBlock)
//                }
//            } else {
//                updatedContentBlocks.add(contentBlock)
//            }
//        }
//
//        _uiState.update {
//            it.copy(
//                bubble = it.bubble.copy(contentBlocks = updatedContentBlocks)
//            )
//        }
//    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun checkCanSave() {
        val texts = chain.toLinear().filter { it.block.type == ContentType.TEXT }
        val ok = when (texts.size) {
            0 -> false
            1 -> texts[0].block.content.parseHtml().isNotBlank() && texts[0].block.content != "<br>"
            else -> {
                val a = texts[0].block.content.parseHtml()
                val b = texts.getOrNull(1)?.block?.content?.parseHtml().orEmpty()
                (a.isNotBlank() && texts[0].block.content != "<br>") ||
                        (b.isNotBlank() && texts[1].block.content != "<br>")
            }
        }
        _uiState.update { it.copy(canSave = ok) }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun trimBlankBlock() {
        val ordered = chain.toLinear()
        val toRemove = mutableListOf<String>()
        ordered.forEach { n ->
            if (n.block.type == ContentType.TEXT) {
                val trimmed = n.block.content.split("<br>").joinToString("") { it.trim() }.parseHtml().trim()
                if (trimmed.isEmpty()) toRemove += n.id
            }
        }
        // 전부 지우면 하나는 남기자
        if (toRemove.size == ordered.size) toRemove.removeLastOrNull()
        toRemove.forEach { chain.remove(it) }
        ensureInitialText()
        publish()
    }


//    fun openGallery() {
//        _uiState.update { it.copy(isGalleryOpen = true) }
//    }

    fun openGallery() {
        // 타깃이 없다면 마지막 포커스 텍스트 or 기본 규칙
        if (_currentInsertionTarget is InsertionTarget.None) {
            lastFocusedTextIndex?.let { setInsertionTarget(InsertionTarget.AfterText(it)) }
                ?: run { setInsertionTarget(InsertionTarget.AfterText(textIndex = findLastTextIndexOrZero())) }
        }
        _uiState.update { it.copy(isGalleryOpen = true) }
    }

    private fun findLastTextIndexOrZero(): Int {
        val ordered = chain.toLinear()
        val idx = ordered.indexOfLast { it.block.type == ContentType.TEXT }
        return if (idx >= 0) idx else 0
    }


    fun saveCameraImage(uri: Uri) {
        _uiState.update {
            it.copy(cameraImagePath = uri)
        }
    }

    fun saveCameraImage(context: Context) {
        val savedUri = saveImageToInternalStorage(context, _uiState.value.cameraImagePath!!)
        _uiState.update {
            it.copy(
                cameraImagePath = null,
                isCameraOpen = false,
                selectedImages = it.selectedImages + savedUri
            )
        }
        addContentBlocks()
    }

    fun updateCameraOpen(isOpen: Boolean) {
        _uiState.update { it.copy(isCameraOpen = isOpen) }
    }

    fun addBackLink(bubble: BubbleModel) {
        if (_uiState.value.bubble.backLinks.map { it.id }.contains(bubble.id)) {
            return
        }

        _uiState.update {
            it.copy(
                bubble = it.bubble.copy(
                    backLinks = it.bubble.backLinks + bubble
                )
            )
        }
    }

    private fun saveImageToInternalStorage(context: Context, uri: Uri): Uri {
        val inputStream = context.contentResolver.openInputStream(uri)

        val fileName = "image_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, fileName)

        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()

        return Uri.fromFile(file)
    }

    fun selectMainImage(uri: String?) {
        if (_uiState.value.bubble.mainImage == uri) {
            _uiState.update {
                it.copy(
                    bubble = it.bubble.copy(mainImage = null)
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    bubble = it.bubble.copy(mainImage = uri)
                )
            }
        }
    }

    fun updateToastMessage(message: String) {
        if (!message.isEmpty()) {
            showToast(message)
        }
    }

    fun toggleImageSelection(imageUri: Uri) {
        val currImageSize = _uiState.value.bubble.contentBlocks.filter {
            it.type == ContentType.IMAGE
        }.size

        if (_uiState.value.selectedImages.contains(imageUri)) {
            _uiState.update { it.copy(selectedImages = it.selectedImages - imageUri) }
        } else if (_uiState.value.selectedImages.size < 10 - currImageSize) {
            _uiState.update { it.copy(selectedImages = it.selectedImages + imageUri) }
        } else {
            showToast("이미지는 최대 10개까지 첨부할 수 있습니다.")
        }
    }

    fun setGapTarget(leftIndex: Int?, rightIndex: Int?) {
        setInsertionTarget(InsertionTarget.Between(leftIndex, rightIndex))
    }

}


@RequiresApi(Build.VERSION_CODES.N)
fun String.parseHtml(): String {
    return Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY).toString()
}
