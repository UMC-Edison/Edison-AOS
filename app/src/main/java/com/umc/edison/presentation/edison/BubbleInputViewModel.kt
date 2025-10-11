package com.umc.edison.presentation.edison

import android.content.Context
import android.net.Uri
import android.text.Html
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
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

private sealed class InsertionTarget {
    data object None : InsertionTarget()
    data class AfterText(val textIndex: Int) : InsertionTarget()
    data class Between(val leftIndex: Int?, val rightIndex: Int?) : InsertionTarget()
}

@HiltViewModel
class BubbleInputViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    toastManager: ToastManager,
    private val bubbleDataManager: BubbleDataManager,
    private val contentBlockManager: ContentBlockManager,
    private val imageHandler: ImageHandler
) : BaseViewModel(toastManager) {

    private val chain = EditorChain()
    private var lastFocusedTextIndex: Int? = null

    private val _uiState = MutableStateFlow(
        BubbleInputState.DEFAULT
    )
    val uiState = _uiState.asStateFlow()

    private var _currentInsertionTarget: InsertionTarget = InsertionTarget.None

    init {
        contentBlockManager.initialize(chain, ::publish, ::showToast)
        imageHandler.initialize(chain, ::publish, ::showToast)

        val id: String? = savedStateHandle["bubbleId"]
        fetchBubble(id)
        fetchLabels()
        fetchBubbles()
    }

    private fun setInsertionTarget(target: InsertionTarget) {
        _currentInsertionTarget = target
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

    init {
        val id: String? = savedStateHandle["bubbleId"]
        fetchBubble(id)
        fetchLabels()
        fetchBubbles()
    }

    private fun fetchBubble(bubbleId: String?) {
        if (bubbleId.isNullOrEmpty()) {
            chain.fromLinear(emptyList())
            ensureInitialText()
            publish()
            return
        }

        collectDataResource(
            flow = bubbleDataManager.getBubble(bubbleId),
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


    /** 빈 단락에서 Backspace → 삭제 후 이전 텍스트로 포커스 */
    fun onBackspaceEmptyAt(textIndex: Int) {
        val result = contentBlockManager.onBackspaceEmptyAt(textIndex)
        result?.let {
            _uiState.update {
                it.copy(
                    focusedTextIndex = result.focusIndex,
                    cursorPosition = result.cursorPosition
                )
            }
            setInsertionTarget(InsertionTarget.AfterText(result.focusIndex))
            lastFocusedTextIndex = result.focusIndex
        }
    }

    /** 커서가 맨 앞에서 Backspace → 이전 단락과 병합 */
    fun onBackspaceAtStart(textIndex: Int) {
        val result = contentBlockManager.onBackspaceAtStart(textIndex)
        result?.let {
            _uiState.update {
                it.copy(
                    focusedTextIndex = result.focusIndex,
                    cursorPosition = result.cursorPosition
                )
            }
            setInsertionTarget(InsertionTarget.AfterText(result.focusIndex))
            lastFocusedTextIndex = result.focusIndex
        }
    }

    fun onTextFocused(textIndex: Int) {
        lastFocusedTextIndex = textIndex
        setInsertionTarget(InsertionTarget.AfterText(textIndex))
    }

    private fun fetchBubbles() {
        collectDataResource(
            flow = bubbleDataManager.getAllBubbles(),
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
            flow = bubbleDataManager.getAllLabels(),
            onSuccess = { labels ->
                _uiState.update { it.copy(labels = labels.toPresentation()) }
            },
        )
    }

    fun saveLabel(label: LabelModel) {
        collectDataResource(
            flow = bubbleDataManager.addLabel(label),
            onSuccess = {
                updateLabelEditMode(LabelEditMode.EDIT)
                fetchLabels()
            },
        )
    }

    fun updateSelectedLabels(labels: List<LabelModel>) {
        val updatedBubble = bubbleDataManager.updateSelectedLabels(_uiState.value.bubble, labels)
        _uiState.update {
            it.copy(
                bubble = updatedBubble,
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
        val tailId = chain.tailId()
        if (tailId == null) return
        val tailBlockType = chain.node(tailId)?.block?.type
        if (tailBlockType != ContentType.TEXT) {
            chain.insertAfter(tailId, ContentBlockModel(ContentType.TEXT, "", 0))
            publish()
        }
    }

    private fun addTextBlockToFront() {
        val headId = chain.headId()
        if (headId != null) {
            val headBlockType = chain.node(headId)?.block?.type
            if (headBlockType != ContentType.TEXT) {
                chain.insertBetween(null, headId, ContentBlockModel(ContentType.TEXT, "", 0))
                publish()
            }
        }
    }

    fun addContentBlocks() {
        val imageUris = _uiState.value.selectedImages
        if (imageUris.isEmpty()) {
            closeGallery()
            return
        }

        when (val target = _currentInsertionTarget) {
            is InsertionTarget.AfterText -> imageHandler.addImagesAfterText(
                target.textIndex,
                imageUris
            )

            is InsertionTarget.Between -> imageHandler.addImagesAtGap(
                target.leftIndex,
                target.rightIndex,
                imageUris
            )

            else -> imageHandler.addImagesDefault(imageUris)
        }

        _uiState.update {
            it.copy(
                isGalleryOpen = false,
                selectedIcon = IconType.NONE,
                selectedImages = emptyList()
            )
        }
    }

    fun addImagesToContentBlocks(uris: List<Uri>) {
        val newImageBlocks = mutableListOf<ContentBlockModel>()

        uris.forEachIndexed { idx, imagePath ->
            val newImageBlock = ContentBlockModel(
                type = ContentType.IMAGE,
                content = imagePath.toString(),
                position = _uiState.value.bubble.contentBlocks.size + idx
            )

            newImageBlocks.add(newImageBlock)
        }

        val newTextBlock = ContentBlockModel(
            type = ContentType.TEXT,
            content = "",
            position = _uiState.value.bubble.contentBlocks.size + uris.size
        )

        _uiState.update {
            it.copy(
                bubble = it.bubble.copy(
                    contentBlocks = it.bubble.contentBlocks + newImageBlocks + newTextBlock
                ),
                isGalleryOpen = false,
                selectedIcon = IconType.NONE
            )
        }
    }

    fun closeGallery() {
        _uiState.update { it.copy(isGalleryOpen = false, selectedIcon = IconType.NONE) }
    }

    fun updateBubbleContent(bubble: BubbleModel) {
        val ordered = chain.toLinear()
        bubble.contentBlocks.forEach { incoming ->
            if (incoming.type == ContentType.TEXT) {
                val node = ordered.find { it.id == incoming.id }
                if (node?.block?.type == ContentType.TEXT) {
                    node.block.content = incoming.content
                }
            }
        }
        val updatedBubble = bubbleDataManager.updateBubbleContent(_uiState.value.bubble, bubble)
        _uiState.update { it.copy(bubble = updatedBubble) }
        checkCanSave()
    }

    fun deleteBackLink(targetBackLink: BubbleModel) {
        val updatedBubble = bubbleDataManager.removeBackLink(_uiState.value.bubble, targetBackLink)
        _uiState.update { it.copy(bubble = updatedBubble) }
    }

    fun deleteLinkBubble(targetLinkBubble: BubbleModel) {
        val updatedBubble =
            bubbleDataManager.removeLinkBubble(_uiState.value.bubble, targetLinkBubble)
        _uiState.update { it.copy(bubble = updatedBubble) }
    }

    fun deleteContentBlock(contentBlock: ContentBlockModel) {
        if (contentBlock.type != ContentType.IMAGE) return

        val currentBubble = _uiState.value.bubble
        val contentBlocks = currentBubble.contentBlocks.sortedBy { it.position }.toMutableList()

        val targetIndex = contentBlocks.indexOfFirst {
            it.position == contentBlock.position
            it.content == contentBlock.content
        }

        if (targetIndex == -1) return

        // 이미지가 첫 번째 블록일 때
        if (targetIndex == 0) {
            contentBlocks.removeAt(targetIndex)
            _uiState.update { it.copy(bubble = it.bubble.copy(contentBlocks = contentBlocks)) }

            if (contentBlocks.isEmpty() || contentBlocks[0].type == ContentType.IMAGE) {
                addTextBlockToFront()
            }
            return
        }

        // 이미지가 마지막 블록일 때
        if (targetIndex == currentBubble.contentBlocks.lastIndex) {
            contentBlocks.removeAt(targetIndex)
            _uiState.update { it.copy(bubble = it.bubble.copy(contentBlocks = contentBlocks)) }

            // 마지막 블록이 이미지 블록이면 텍스트 블록 추가
            if (contentBlocks.last().type == ContentType.IMAGE) {
                addTextBlock()
            }
            return
        }

        // 이미지가 중간 블럭일 때 이전 블록과 다음 블록이 TEXT일 경우 연결
        if (contentBlocks[targetIndex - 1].type == ContentType.TEXT && contentBlocks[targetIndex + 1].type == ContentType.TEXT) {
            contentBlocks[targetIndex - 1] = contentBlocks[targetIndex - 1].copy(
                content = contentBlocks[targetIndex - 1].content + contentBlocks[targetIndex + 1].content
            )

            contentBlocks.removeAt(targetIndex) // 제거하려는 이미지 블록 삭제
            contentBlocks.removeAt(targetIndex) // 다음 TEXT 블록 삭제

            _uiState.update { it.copy(bubble = it.bubble.copy(contentBlocks = contentBlocks)) }
            return
        }

        contentBlocks.removeAt(targetIndex)
        _uiState.update {
            it.copy(
                bubble = it.bubble.copy(contentBlocks = contentBlocks)
            )
        }
    }

    fun onGapTapped(leftIndex: Int?, rightIndex: Int?) {
        val result = contentBlockManager.onGapTapped(leftIndex, rightIndex)
        result?.let {
            _uiState.update {
                it.copy(
                    focusedTextIndex = result.focusIndex,
                    cursorPosition = result.cursorPosition
                )
            }
            setInsertionTarget(InsertionTarget.AfterText(result.focusIndex))
            lastFocusedTextIndex = result.focusIndex
        }
    }

    fun clearFocusedIndex() {
        _uiState.update { it.copy(focusedTextIndex = null) }
    }

    fun updateBubbleWithLink() {
        saveBubble(true)
    }

    fun saveBubble(isLinked: Boolean = false) {
        trimBlankBlock()
        checkCanSave()
        if (!uiState.value.canSave) {
            showToast("내용을 입력해주세요.")
            addTextBlockToFront()
            return
        }

        val flow = if (uiState.value.bubble.id.isNullOrEmpty())
            bubbleDataManager.addBubble(uiState.value.bubble)
        else
            bubbleDataManager.updateBubble(uiState.value.bubble)

        collectDataResource(
            flow = flow,
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

    private fun checkCanSave() {
        val canSave = contentBlockManager.checkCanSave()
        _uiState.update { it.copy(canSave = canSave) }
    }

    private fun trimBlankBlock() {
        contentBlockManager.trimBlankBlocks()
    }

    fun openGallery() {
        if (!checkCanAddImage()) {
            return
        }

        _uiState.update { it.copy(isGalleryOpen = true) }
    }

    fun saveCameraImage(uri: Uri) {
        _uiState.update { it.copy(cameraImagePath = uri) }
    }

    fun saveCameraImage(context: Context) {
        val savedUri = imageHandler.saveCameraImage(context, _uiState.value.cameraImagePath!!)
        _uiState.update {
            it.copy(
                cameraImagePath = null,
                isCameraOpen = false,
            )
        }
        addImagesToContentBlocks(
            uris = listOf(savedUri)
        )
    }

    fun updateCameraOpen(isOpen: Boolean) {
        if (isOpen && !checkCanAddImage()) {
            return
        }

        _uiState.update { it.copy(isCameraOpen = isOpen) }
    }

    fun addBackLink(bubble: BubbleModel) {
        val updatedBubble = bubbleDataManager.addBackLink(_uiState.value.bubble, bubble)
        _uiState.update { it.copy(bubble = updatedBubble) }
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
        if (message.isNotEmpty()) showToast(message)
    }

    fun toggleImageSelection(imageUri: Uri) {
        val currentImageCount =
            imageHandler.getCurrentImageCount(_uiState.value.bubble.contentBlocks)
        val updatedImages = imageHandler.toggleImageSelection(
            imageUri,
            _uiState.value.selectedImages,
            currentImageCount
        )
        _uiState.update { it.copy(selectedImages = updatedImages) }
    }

    fun updateSelectedImages(uris: List<Uri>): List<Uri> {
        val currImageSize =
            _uiState.value.bubble.contentBlocks.filter { it.type == ContentType.IMAGE }.size

        val availableSize = MAX_TOTAL_IMAGES - currImageSize
        return if (uris.size > availableSize) {
            showToast(MAX_TOTAL_IMAGES_LIMIT_MESSAGE)
            uris.take(availableSize)
        } else {
            uris
        }
    }

    private fun checkCanAddImage(): Boolean {
        val currImageSize =
            _uiState.value.bubble.contentBlocks.filter { it.type == ContentType.IMAGE }.size
        if (currImageSize >= MAX_TOTAL_IMAGES) {
            showToast(MAX_TOTAL_IMAGES_LIMIT_MESSAGE)
            return false
        }
        return true
    }

    companion object {
        const val MAX_IMAGE_SELECTION = 10
        const val MAX_TOTAL_IMAGES = 30

        const val MAX_TOTAL_IMAGES_LIMIT_MESSAGE = "이미지는 최대 ${MAX_TOTAL_IMAGES}개까지 첨부할 수 있습니다."
    }
}

fun String.parseHtml(): String {
    return Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY).toString()
}
