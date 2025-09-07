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
import com.umc.edison.ui.components.CaretWhere
import java.util.LinkedList
import android.net.Uri.fromFile as uriFromFile

// --- 삽입 타깃(필수는 아니지만 갤러리 삽입 편의에 사용) ---
private sealed class InsertionTarget {
    data object None : InsertionTarget()
    data class AfterText(val textIndex: Int) : InsertionTarget()
    data class Between(val leftIndex: Int?, val rightIndex: Int?) : InsertionTarget()
}

// --- 내부 체인 노드 ---
private data class Node(
    val id: String = java.util.UUID.randomUUID().toString(),
    val block: ContentBlockModel,
    var prev: String? = null,
    var next: String? = null
)

// --- 아주 얇은 링크드 리스트 래퍼 ---
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
    private var lastFocusedTextIndex: Int? = null

    private val _uiState = MutableStateFlow(
        BubbleInputState.DEFAULT.copy(
            bubble = BubbleModel.DEFAULT.copy(contentBlocks = LinkedList())
        )
    )
    val uiState = _uiState.asStateFlow()

    private var _currentInsertionTarget: InsertionTarget = InsertionTarget.None
    private fun setInsertionTarget(target: InsertionTarget) {
        _currentInsertionTarget = target
        // 필요하면 상태에 노출
        // _uiState.update { it.copy(insertionTarget = ...) }
    }

    private fun publish() {
        val ordered = chain.toLinear().map { it.block }
        _uiState.update { s -> s.copy(bubble = s.bubble.copy(contentBlocks = ordered)) }
        checkCanSave()
    }

    private fun ensureInitialText() {
        if (chain.headId() == null) {
            chain.insertAfter(null, ContentBlockModel(ContentType.TEXT, "", 0))
        }
    }

    private fun ensureTrailingText() {
        if (chain.tailId() != null && chain.isImage(chain.tailId())) {
            chain.insertAfter(chain.tailId(), ContentBlockModel(ContentType.TEXT, "", 0))
        }
    }

    init {
        val id: String? = savedStateHandle["bubbleId"]
        fetchBubble(id)
        fetchLabels()
        fetchBubbles()
    }

    @RequiresApi(Build.VERSION_CODES.N)
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
                        bubble = pres.copy(contentBlocks = orderedBlocks),
                        selectedLabels = pres.labels
                    )
                }
                checkCanSave()
            },
            onComplete = { ensureInitialText(); publish() }
        )
    }

    /** ──────────────── Notion 스타일: Enter 동작 ──────────────── **/
    fun onEnterWithCaret(
        textIndex: Int,
        where: CaretWhere,
        plainLeft: String?,   // IN_MIDDLE일 때 좌측(plain)
        plainRight: String?   // IN_MIDDLE일 때 우측(plain)
    ) {
        val ordered = chain.toLinear()
        val node = ordered.getOrNull(textIndex) ?: return
        if (node.block.type != ContentType.TEXT) return

        when (where) {
            CaretWhere.AT_END -> {
                val newId = chain.insertAfter(node.id, ContentBlockModel(ContentType.TEXT, "", 0))
                ensureTrailingText()
                publish()
                val focusIndex = chain.toLinear().indexOfFirst { it.id == newId }
                _uiState.update { it.copy(focusedTextIndex = focusIndex) }
                setInsertionTarget(InsertionTarget.AfterText(focusIndex))
                lastFocusedTextIndex = focusIndex
            }
            CaretWhere.AT_START -> {
                val newId = chain.insertBetween(node.prev, node.id, ContentBlockModel(ContentType.TEXT, "", 0))
                ensureTrailingText()
                publish()
                val focusIndex = chain.toLinear().indexOfFirst { it.id == newId }
                _uiState.update { it.copy(focusedTextIndex = focusIndex) }
                setInsertionTarget(InsertionTarget.AfterText(focusIndex))
                lastFocusedTextIndex = focusIndex
            }
            CaretWhere.IN_MIDDLE -> {
                val left = (plainLeft ?: "").replace("\n", "<br>")
                val right = (plainRight ?: "").replace("\n", "<br>")
                node.block.content = left
                val newId = chain.insertAfter(node.id, ContentBlockModel(ContentType.TEXT, right, 0))
                ensureTrailingText()
                publish()
                val focusIndex = chain.toLinear().indexOfFirst { it.id == newId }
                _uiState.update { it.copy(focusedTextIndex = focusIndex) }
                setInsertionTarget(InsertionTarget.AfterText(focusIndex))
                lastFocusedTextIndex = focusIndex
            }
        }
    }

    /** Shift+Enter: 같은 블록에서 줄바꿈만 추가 */
    fun onShiftEnter(
        textIndex: Int,
        plainLeft: String?,
        plainRight: String?
    ) {
        val ordered = chain.toLinear()
        val node = ordered.getOrNull(textIndex) ?: return
        if (node.block.type != ContentType.TEXT) return
        val left = plainLeft ?: ""
        val right = plainRight ?: ""
        node.block.content = (left + "<br>" + right)
        publish()
        // 포커스 유지: UI가 selection을 유지해 줌
    }

    /** 빈 단락에서 Backspace → 삭제 후 이전 텍스트로 포커스 */
    @RequiresApi(Build.VERSION_CODES.N)
    fun onBackspaceEmptyAt(textIndex: Int) {
        onBackspaceAt(textIndex)
    }

    /** 커서가 맨 앞에서 Backspace → 이전 단락과 병합 */
    @RequiresApi(Build.VERSION_CODES.N)
    fun onBackspaceAtStart(textIndex: Int) {
        val ordered = chain.toLinear()
        val node = ordered.getOrNull(textIndex) ?: return
        if (node.block.type != ContentType.TEXT) return

        val prevIdx = (textIndex - 1 downTo 0).firstOrNull { ordered[it].block.type == ContentType.TEXT } ?: return
        val prevNode = ordered[prevIdx]

        prevNode.block.content += node.block.content
        chain.remove(node.id)

        if (chain.headId() != null && chain.isImage(chain.headId())) {
            chain.insertBetween(null, chain.headId(), ContentBlockModel(ContentType.TEXT, "", 0))
        }
        ensureTrailingText()
        publish()

        val focusIndex = chain.toLinear().indexOfFirst { it.id == prevNode.id }
        _uiState.update { it.copy(focusedTextIndex = focusIndex) }
        setInsertionTarget(InsertionTarget.AfterText(focusIndex))
        lastFocusedTextIndex = focusIndex
    }

    /** 텍스트 블록 바로 ‘아래’를 탭 → 그 아래에 새 단락 생성(노션 스타일) */
    fun onClickBelowBlock(textIndex: Int) {
        val ordered = chain.toLinear()
        val node = ordered.getOrNull(textIndex) ?: return
        if (node.block.type != ContentType.TEXT) return
        val newId = chain.insertAfter(node.id, ContentBlockModel(ContentType.TEXT, "", 0))
        ensureTrailingText()
        publish()
        val focusIndex = chain.toLinear().indexOfFirst { it.id == newId }
        _uiState.update { it.copy(focusedTextIndex = focusIndex) }
        setInsertionTarget(InsertionTarget.AfterText(focusIndex))
        lastFocusedTextIndex = focusIndex
    }

    /** 페이지 맨 아래 빈 공간 탭 → 마지막에 새 단락 생성 후 포커스 */
    fun onTapPageBottom() {
        val ordered = chain.toLinear()
        val lastTextIdx = ordered.indexOfLast { it.block.type == ContentType.TEXT }
        if (lastTextIdx >= 0) {
            onClickBelowBlock(lastTextIdx)
        } else {
            ensureInitialText()
            publish()
            _uiState.update { it.copy(focusedTextIndex = 0) }
            setInsertionTarget(InsertionTarget.AfterText(0))
            lastFocusedTextIndex = 0
        }
    }

    fun onTextFocused(textIndex: Int) {
        lastFocusedTextIndex = textIndex
        setInsertionTarget(InsertionTarget.AfterText(textIndex))
    }

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
                _uiState.update { it.copy(labels = labels.toPresentation()) }
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
                bubble = it.bubble.copy(labels = labels),
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
                it.copy(selectedTextStyles = emptyList(), selectedListStyle = ListStyle.NONE)
            }
        }
        if (iconType == IconType.TAG) {
            fetchLabels()
            updateLabelEditMode(LabelEditMode.EDIT)
        }
        _uiState.update { it.copy(selectedIcon = iconType) }
    }

    fun updateTextStyle(textStyle: TextStyle) {
        val selected = _uiState.value.selectedTextStyles.toMutableList()
        if (selected.contains(textStyle)) selected.remove(textStyle) else selected.add(textStyle)
        _uiState.update { it.copy(selectedTextStyles = selected) }
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
        val blocks = _uiState.value.bubble.contentBlocks
        _uiState.update {
            it.copy(bubble = it.bubble.copy(contentBlocks =
            if (blocks.isEmpty()) listOf(newTextBlock) else blocks + newTextBlock
            ))
        }
    }

    private fun addTextBlockToFront() {
        val blocks = _uiState.value.bubble.contentBlocks
        if (blocks.isEmpty()) { addTextBlock(); return }
        if (blocks.first().type == ContentType.TEXT && blocks.last().type == ContentType.TEXT) return
        if (blocks.first().type == ContentType.TEXT) return

        val newTextBlock = ContentBlockModel(ContentType.TEXT, "", 0)
        val shifted = blocks.map { it.copy(position = it.position + 1) }
        _uiState.update {
            it.copy(bubble = it.bubble.copy(contentBlocks = LinkedList(listOf(newTextBlock) + shifted)))
        }
        if (_uiState.value.bubble.contentBlocks.last().type == ContentType.IMAGE) addTextBlock()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun addContentBlocks() {
        val imageUris = _uiState.value.selectedImages
        if (imageUris.isEmpty()) {
            _uiState.update { it.copy(isGalleryOpen = false, selectedIcon = IconType.NONE) }
            return
        }

        when (val target = _currentInsertionTarget) {
            is InsertionTarget.AfterText -> addImagesAfterText(target.textIndex, imageUris)
            is InsertionTarget.Between   -> addImagesAtGap(target.leftIndex, target.rightIndex, imageUris)
            else                         -> addImagesDefault(imageUris)
        }

        _uiState.update {
            it.copy(isGalleryOpen = false, selectedIcon = IconType.NONE, selectedImages = emptyList())
        }
    }

    private fun addImagesDefault(uris: List<Uri>) {
        val ordered = chain.toLinear()
        val lastTextIndex = ordered.indexOfLast { it.block.type == ContentType.TEXT }
            .takeIf { it >= 0 } ?: run {
            ensureInitialText()
            chain.toLinear().indexOfLast { it.block.type == ContentType.TEXT }
        }
        addImagesAfterText(lastTextIndex, uris)
    }

    @RequiresApi(Build.VERSION_CODES.N)
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
            val midTextId = chain.insertBetween(leftId, rightId, ContentBlockModel(ContentType.TEXT, "", 0))
            val midIndex = chain.toLinear().indexOfFirst { it.id == midTextId }
            addImagesAfterText(midIndex, uris); return
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

    @RequiresApi(Build.VERSION_CODES.N)
    fun updateBubbleContent(bubble: BubbleModel) {
        val ordered = chain.toLinear()
        bubble.contentBlocks.forEach { incoming ->
            if (incoming.type == ContentType.TEXT) {
                val node = ordered.getOrNull(incoming.position)
                if (node != null && node.block.type == ContentType.TEXT) {
                    node.block.content = incoming.content
                }
            }
        }
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
        val updated = currentBubble.backLinks.filterNot { it.id == targetBackLink.id }
        _uiState.update { it.copy(bubble = currentBubble.copy(backLinks = updated)) }
    }

    fun deleteLinkBubble(targetLinkBubble: BubbleModel) {
        val current = _uiState.value.bubble
        _uiState.update {
            it.copy(
                bubble = current.copy(
                    linkedBubble = if (current.linkedBubble?.id == targetLinkBubble.id) null else current.linkedBubble
                )
            )
        }
    }

    fun deleteContentBlock(contentBlock: ContentBlockModel) {
        if (contentBlock.type != ContentType.IMAGE) return

        val targetId = chain.toLinear()
            .firstOrNull { it.block.type == ContentType.IMAGE && it.block.content == contentBlock.content && it.block.position == contentBlock.position }
            ?.id ?: return

        val uri = contentBlock.content.toUri()
        if (_uiState.value.selectedImages.contains(uri)) {
            _uiState.update { it.copy(selectedImages = it.selectedImages - uri) }
        }

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

        if (chain.headId() != null && chain.isImage(chain.headId())) {
            chain.insertBetween(null, chain.headId(), ContentBlockModel(ContentType.TEXT, "", 0))
        }
        ensureTrailingText()
        publish()
    }

    /** (남아 있어도 무해) ‘갭’ 클릭 → 빈 TEXT 생성 + 포커스 */
    fun onGapTapped(leftIndex: Int?, rightIndex: Int?) {
        val ordered = chain.toLinear()
        val leftId = leftIndex?.let { ordered.getOrNull(it)?.id }
        val rightId = rightIndex?.let { ordered.getOrNull(it)?.id }
        val newTextId = chain.insertBetween(leftId, rightId, ContentBlockModel(ContentType.TEXT, "", 0))
        val newIndex = chain.toLinear().indexOfFirst { it.id == newTextId }
        publish()
        _uiState.update { it.copy(focusedTextIndex = newIndex) }
        setInsertionTarget(InsertionTarget.AfterText(newIndex))
        lastFocusedTextIndex = newIndex
    }

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
                    _uiState.update { it.copy(bubble = savedBubble.toPresentation()) }
                }
            },
        )
    }

    private fun findPrevTextIndexFrom(index: Int): Int? {
        val ordered = chain.toLinear()
        for (i in index - 1 downTo 0) if (ordered[i].block.type == ContentType.TEXT) return i
        return null
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun onBackspaceAt(textIndex: Int) {
        val ordered = chain.toLinear()
        val node = ordered.getOrNull(textIndex) ?: return
        if (node.block.type != ContentType.TEXT) return

        val empty = node.block.content.parseHtml().isBlank()
        if (!empty) return

        val onlyOneText = ordered.count { it.block.type == ContentType.TEXT } == 1
        val hasImage = ordered.any { it.block.type == ContentType.IMAGE }
        if (onlyOneText && !hasImage) return

        val leftFocusIndex = findPrevTextIndexFrom(textIndex)
        chain.remove(node.id)

        if (chain.headId() != null && chain.isImage(chain.headId())) {
            chain.insertBetween(null, chain.headId(), ContentBlockModel(ContentType.TEXT, "", 0))
        }
        ensureTrailingText()
        publish()

        val newOrdered = chain.toLinear()
        val focusIndex = when {
            leftFocusIndex != null && leftFocusIndex in newOrdered.indices -> leftFocusIndex
            else -> newOrdered.indexOfLast { it.block.type == ContentType.TEXT }.takeIf { it >= 0 } ?: 0
        }

        _uiState.update { it.copy(focusedTextIndex = focusIndex) }
        setInsertionTarget(InsertionTarget.AfterText(focusIndex))
        lastFocusedTextIndex = focusIndex
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun checkCanSave() {
        val texts = chain.toLinear().filter { it.block.type == ContentType.TEXT }
        val ok = when (texts.size) {
            0 -> false
            1 -> texts[0].block.content.parseHtml().isNotBlank() && texts[0].block.content != "<br>"
            else -> {
                val a = texts[0].block.content.parseHtml()
                val b = texts.getOrNull(1)?.block?.content?.parseHtml().orEmpty()
                (a.isNotBlank() && texts[0].block.content != "<br>") || (b.isNotBlank() && texts[1].block.content != "<br>")
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
        if (toRemove.size == ordered.size) toRemove.removeLastOrNull()
        toRemove.forEach { chain.remove(it) }
        ensureInitialText()
        publish()
    }

    fun openGallery() {
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
        _uiState.update { it.copy(cameraImagePath = uri) }
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
        if (_uiState.value.bubble.backLinks.map { it.id }.contains(bubble.id)) return
        _uiState.update {
            it.copy(bubble = it.bubble.copy(backLinks = it.bubble.backLinks + bubble))
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
        val current = _uiState.value.bubble
        _uiState.update {
            it.copy(bubble = current.copy(mainImage = if (current.mainImage == uri) null else uri))
        }
    }

    fun updateToastMessage(message: String) {
        if (message.isNotEmpty()) showToast(message)
    }

    fun toggleImageSelection(imageUri: Uri) {
        val currImageSize = _uiState.value.bubble.contentBlocks.count { it.type == ContentType.IMAGE }
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
