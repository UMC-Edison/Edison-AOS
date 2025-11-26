package com.umc.edison.presentation.edison

import android.content.Context
import android.net.Uri
import com.umc.edison.presentation.model.ContentBlockModel
import com.umc.edison.presentation.model.ContentType
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import com.umc.edison.presentation.edison.util.parseHtml

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
        const val MAX_IMAGE_SELECTION = 10
        const val MAX_TOTAL_IMAGES = 30
        const val MAX_TOTAL_IMAGES_LIMIT_MESSAGE = "이미지는 최대 ${MAX_TOTAL_IMAGES}개까지 첨부할 수 있습니다."
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

        val textContent = textNode.block.content.parseHtml().trim()
        val isEmpty = textContent.isEmpty()

        if (isEmpty) {
            if (uris.isNotEmpty()) {
                // 기존 빈 텍스트 블록을 첫 번째 이미지로 교체
                val prevId = textNode.prev
                val nextId = textNode.next

                // 기존 텍스트 블록 제거
                chain.remove(textNode.id)

                // 첫 번째 이미지 블록 추가
                var anchorId = chain.insertBetween(prevId, nextId, ContentBlockModel(ContentType.IMAGE, uris.first().toString(), 0))

                // 나머지 이미지들 추가
                uris.drop(1).forEach { uri ->
                    anchorId = chain.insertAfter(anchorId, ContentBlockModel(ContentType.IMAGE, uri.toString(), 0))
                }

                // 마지막에 새 텍스트 블록 추가
                chain.insertAfter(anchorId, ContentBlockModel(ContentType.TEXT, DEFAULT_TEXT_CONTENT, 0))
            }
        } else {
            var anchorId = textNode.id
            uris.forEach { uri ->
                anchorId = chain.insertAfter(anchorId, ContentBlockModel(ContentType.IMAGE, uri.toString(), 0))
            }
            // 마지막에 새 텍스트 블록 추가
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
     * 현재 콘텐츠 블록에서 이미지 개수 계산
     */
    fun getCurrentImageCount(contentBlocks: List<ContentBlockModel>): Int {
        return contentBlocks.count { it.type == ContentType.IMAGE }
    }

    fun checkCanAddImage(currImageSize: Int): Boolean {
        return currImageSize < MAX_TOTAL_IMAGES
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
}
