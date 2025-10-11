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
import java.util.LinkedList
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
        val success = contentBlockManager.deleteContentBlock(contentBlock)
        if (success) {
            val uri = contentBlock.content.toUri()
            if (_uiState.value.selectedImages.contains(uri)) {
                _uiState.update { it.copy(selectedImages = it.selectedImages - uri) }
            }
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
        if (_currentInsertionTarget is InsertionTarget.None) {
            lastFocusedTextIndex?.let { setInsertionTarget(InsertionTarget.AfterText(it)) }
                ?: run {
                    val ordered = chain.toLinear()
                    val lastTextIdx = ordered.indexOfLast { it.block.type == ContentType.TEXT }
                    setInsertionTarget(InsertionTarget.AfterText(if (lastTextIdx >= 0) lastTextIdx else 0))
                }
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


    fun selectMainImage(uri: String?) {
        val updatedBubble = bubbleDataManager.toggleMainImage(_uiState.value.bubble, uri)
        _uiState.update { it.copy(bubble = updatedBubble) }
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
}

fun String.parseHtml(): String {
    return Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY).toString()
}
