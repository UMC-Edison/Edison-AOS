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
    private val _uiState = MutableStateFlow(BubbleInputState.DEFAULT.copy(
        bubble = BubbleModel.DEFAULT.copy(contentBlocks = LinkedList())
    ))
    val uiState = _uiState.asStateFlow()

    init {
        val id: String? = savedStateHandle["bubbleId"]
        fetchBubble(id)
        fetchLabels()
        fetchBubbles()
    }

    private fun fetchBubble(bubbleId: String?) {
        if (bubbleId.isNullOrEmpty()) {
            addTextBlockToFront()
            return
        }

        collectDataResource(
            flow = getBubbleUseCase(bubbleId),
            onSuccess = { bubble ->
                val sortedContentBlocks = bubble.toPresentation().contentBlocks.sortedBy { it.position }
                _uiState.update {
                    it.copy(
                        bubble = bubble.toPresentation().copy(contentBlocks = LinkedList(sortedContentBlocks)),
                        selectedLabels = bubble.labels.toPresentation()
                    )
                }
            },
            onComplete = {
                addTextBlockToFront()
            }
        )
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

    fun addContentBlocks() {
        val imagePaths = _uiState.value.selectedImages.filter { imageUri ->
            _uiState.value.bubble.contentBlocks.none { it.content == imageUri.toString() }
        }

        val newImageBlocks = mutableListOf<ContentBlockModel>()

        imagePaths.forEachIndexed { idx, imagePath ->
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
            position = _uiState.value.bubble.contentBlocks.size + imagePaths.size
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

    @RequiresApi(Build.VERSION_CODES.N)
    fun updateBubbleContent(bubble: BubbleModel) {
        _uiState.update {
            it.copy(
                bubble = bubble
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

    fun deleteContentBlock(contentBlock: ContentBlockModel) {
        if (contentBlock.type != ContentType.IMAGE) return

        val currentBubble = _uiState.value.bubble
        val contentBlocks = currentBubble.contentBlocks.sortedBy { it.position }.toMutableList()

        val targetIndex = contentBlocks.indexOfFirst {
            it.position == contentBlock.position
            it.content == contentBlock.content
        }

        if (targetIndex == -1) return

        if (_uiState.value.selectedImages.contains(contentBlock.content.toUri())) {
            _uiState.update { it.copy(selectedImages = it.selectedImages - contentBlock.content.toUri()) }
        }

        if (targetIndex == 0) {
            contentBlocks.removeAt(targetIndex)
            _uiState.update { it.copy(bubble = it.bubble.copy(contentBlocks = contentBlocks)) }

            if (contentBlocks.isEmpty() || contentBlocks[0].type == ContentType.IMAGE) {
                addTextBlockToFront()
            }
            return
        }

        if (targetIndex == currentBubble.contentBlocks.lastIndex) {
            contentBlocks.removeAt(targetIndex)
            _uiState.update { it.copy(bubble = it.bubble.copy(contentBlocks = contentBlocks)) }

            if (contentBlocks.last().type == ContentType.IMAGE) {
                addTextBlock()
            }
            return
        }

        if (contentBlocks[targetIndex - 1].type == ContentType.TEXT && contentBlocks[targetIndex + 1].type == ContentType.TEXT) {
            contentBlocks[targetIndex - 1] = contentBlocks[targetIndex - 1].copy(
                content = contentBlocks[targetIndex - 1].content + contentBlocks[targetIndex + 1].content
            )

            contentBlocks.removeAt(targetIndex)
            contentBlocks.removeAt(targetIndex)

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

    @RequiresApi(Build.VERSION_CODES.N)
    fun updateBubbleWithLink() {
        saveBubble(true)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun saveBubble(isLinked: Boolean = false) {
        trimBlankBlock()

        checkCanSave()

        if (!_uiState.value.canSave) {
            showToast("내용을 입력해주세요.")
            addTextBlockToFront()
            return
        }

        collectDataResource(
            flow = if (_uiState.value.bubble.id.isNullOrEmpty()) {
                addBubbleUseCase(_uiState.value.bubble.toDomain())
            } else {
                updateBubbleUseCase(_uiState.value.bubble.toDomain())
            },
            onSuccess = { savedBubble ->
                showToast("저장되었습니다.")

                if (isLinked) {
                    _uiState.update {
                        BubbleInputState.DEFAULT.copy(
                            bubble = BubbleModel.DEFAULT.copy(
                                linkedBubble = savedBubble.toPresentation()
                            ),
                            bubbles = it.bubbles + savedBubble.toPresentation()
                        )
                    }

                    addTextBlock()
                }
            },
        )
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun checkCanSave() {
        var canSave = true

        val bubble = _uiState.value.bubble

        if (bubble.title.isNullOrEmpty() && bubble.mainImage.isNullOrEmpty()) {
            canSave = if (bubble.contentBlocks.isEmpty()) {
                false
            } else if (bubble.contentBlocks.size == 1) {
                bubble.contentBlocks[0].content.parseHtml()
                    .isNotEmpty() && bubble.contentBlocks[0].content != "<br>"
            } else {
                (bubble.contentBlocks[0].content.parseHtml()
                    .isNotEmpty() && bubble.contentBlocks[0].content != "<br>")
                        || (bubble.contentBlocks[1].content.parseHtml()
                    .isNotEmpty() && bubble.contentBlocks[1].content != "<br>")
            }
        }

        _uiState.update { it.copy(canSave = canSave) }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun trimBlankBlock() {
        val contentBlocks = _uiState.value.bubble.contentBlocks.toMutableList()
        val updatedContentBlocks = mutableListOf<ContentBlockModel>()

        contentBlocks.forEachIndexed { _, contentBlock ->
            if (contentBlock.type == ContentType.TEXT) {
                val contents = contentBlock.content.split("<br>")
                val newContent = if (contents.size > 1) {
                    contents.joinToString(separator = "") { it.trim() }
                } else {
                    contentBlock.content
                }

                if (newContent.parseHtml().trim().isNotEmpty()) {
                    updatedContentBlocks.add(contentBlock)
                }
            } else {
                updatedContentBlocks.add(contentBlock)
            }
        }

        _uiState.update {
            it.copy(
                bubble = it.bubble.copy(contentBlocks = updatedContentBlocks)
            )
        }
    }

    fun openGallery() {
        _uiState.update { it.copy(isGalleryOpen = true) }
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

}


@RequiresApi(Build.VERSION_CODES.N)
fun String.parseHtml(): String {
    return Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY).toString()
}
