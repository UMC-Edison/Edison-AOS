package com.umc.edison.presentation.edison

import android.content.Context
import android.net.Uri
import com.umc.edison.presentation.model.ContentBlockModel
import com.umc.edison.presentation.model.ContentType
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

/**
 * 이미지 관련 처리를 담당하는 핸들러 클래스
 * 이미지 선택, 저장, 삭제, 갤러리 관리 등을 처리합니다.
 */
class ImageHandler @Inject constructor() {
    private lateinit var chain: EditorChain
    private lateinit var onPublish: () -> Unit
    private lateinit var onShowToast: (String) -> Unit

    fun initialize(chain: EditorChain, onPublish: () -> Unit, onShowToast: (String) -> Unit) {
        this.chain = chain
        this.onPublish = onPublish
        this.onShowToast = onShowToast
    }
    
    companion object {
        private const val MAX_IMAGES = 10
        private const val IMAGE_FILE_PREFIX = "image_"
        private const val IMAGE_FILE_EXTENSION = ".jpg"
        private const val DEFAULT_TEXT_CONTENT = ""
    }

    /**
     * 이미지들을 기본 위치에 추가
     */
    fun addImagesDefault(uris: List<Uri>) {
        val ordered = chain.toLinear()
        val lastTextIndex = ordered.indexOfLast { it.block.type == ContentType.TEXT }
            .takeIf { it >= 0 } ?: run {
            ensureInitialText()
            chain.toLinear().indexOfLast { it.block.type == ContentType.TEXT }
        }
        addImagesAfterText(lastTextIndex, uris)
    }

    /**
     * 텍스트 블록 다음에 이미지들 추가
     */
    fun addImagesAfterText(textIndex: Int, uris: List<Uri>) {
        val ordered = chain.toLinear()
        val textNode = ordered.getOrNull(textIndex) ?: return
        require(textNode.block.type == ContentType.TEXT)
        val isEmpty = textNode.block.content.parseHtml().isBlank()

        var anchorId = textNode.id
        uris.forEach { uri ->
            anchorId = chain.insertAfter(anchorId, ContentBlockModel(ContentType.IMAGE, uri.toString(), 0))
        }
        if (!isEmpty) {
            chain.insertAfter(anchorId, ContentBlockModel(ContentType.TEXT, DEFAULT_TEXT_CONTENT, 0))
        }
        ensureTrailingText()
        onPublish()
    }

    /**
     * 두 블록 사이에 이미지들 추가
     */
    fun addImagesAtGap(leftIndex: Int?, rightIndex: Int?, uris: List<Uri>) {
        val ordered = chain.toLinear()
        val leftId = leftIndex?.let { ordered.getOrNull(it)?.id }
        val rightId = rightIndex?.let { ordered.getOrNull(it)?.id }

        val leftIsImg = leftId?.let { chain.isImage(it) } ?: false
        val rightIsImg = rightId?.let { chain.isImage(it) } ?: false

        if (leftIsImg && rightIsImg) {
            val midTextId = chain.insertBetween(leftId, rightId, ContentBlockModel(ContentType.TEXT, DEFAULT_TEXT_CONTENT, 0))
            val midIndex = chain.toLinear().indexOfFirst { it.id == midTextId }
            addImagesAfterText(midIndex, uris)
            return
        }

        var prev = leftId
        uris.forEach { uri ->
            prev = chain.insertBetween(prev, rightId, ContentBlockModel(ContentType.IMAGE, uri.toString(), 0))
        }
        ensureTrailingText()
        onPublish()
    }

    /**
     * 카메라 이미지를 내부 저장소에 저장
     */
    fun saveCameraImage(context: Context, uri: Uri): Uri {
        val fileName = "${IMAGE_FILE_PREFIX}${System.currentTimeMillis()}$IMAGE_FILE_EXTENSION"
        val file = File(context.filesDir, fileName)
        
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return Uri.fromFile(file)
    }

    /**
     * 이미지 선택 토글
     */
    fun toggleImageSelection(
        imageUri: Uri, 
        selectedImages: List<Uri>, 
        currentImageCount: Int
    ): List<Uri> {
        return if (selectedImages.contains(imageUri)) {
            selectedImages - imageUri
        } else if (selectedImages.size < MAX_IMAGES - currentImageCount) {
            selectedImages + imageUri
        } else {
            onShowToast("이미지는 최대 ${MAX_IMAGES}개까지 첨부할 수 있습니다.")
            selectedImages
        }
    }

    /**
     * 이미지가 선택된 상태인지 확인
     */
    fun isImageSelected(imageUri: Uri, selectedImages: List<Uri>): Boolean {
        return selectedImages.contains(imageUri)
    }

    /**
     * 선택된 이미지에서 특정 URI 제거
     */
    fun removeImageFromSelection(imageUri: Uri, selectedImages: List<Uri>): List<Uri> {
        return selectedImages - imageUri
    }

    /**
     * 이미지 선택 가능 여부 확인
     */
    fun canSelectMoreImages(selectedImages: List<Uri>, currentImageCount: Int): Boolean {
        return selectedImages.size < MAX_IMAGES - currentImageCount
    }

    /**
     * 현재 콘텐츠 블록에서 이미지 개수 계산
     */
    fun getCurrentImageCount(contentBlocks: List<ContentBlockModel>): Int {
        return contentBlocks.count { it.type == ContentType.IMAGE }
    }

    // Private helper methods
    private fun ensureInitialText() {
        if (chain.headId() == null) {
            chain.insertAfter(null, ContentBlockModel(ContentType.TEXT, DEFAULT_TEXT_CONTENT, 0))
        }
    }

    private fun ensureTrailingText() {
        if (chain.tailId() != null && chain.isImage(chain.tailId())) {
            chain.insertAfter(chain.tailId(), ContentBlockModel(ContentType.TEXT, DEFAULT_TEXT_CONTENT, 0))
        }
    }

    private fun String.parseHtml(): String {
        return android.text.Html.fromHtml(this, android.text.Html.FROM_HTML_MODE_LEGACY).toString()
    }
}
