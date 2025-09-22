package com.umc.edison.presentation.edison

import com.umc.edison.presentation.model.ContentBlockModel
import com.umc.edison.presentation.model.ContentType
import android.text.Html
import javax.inject.Inject

/**
 * 백스페이스 처리 결과
 */
data class BackspaceResult(
    val focusIndex: Int,
    val cursorPosition: Int
)

/**
 * Gap 탭 처리 결과
 */
data class GapTapResult(
    val focusIndex: Int,
    val cursorPosition: Int
)

/**
 * 콘텐츠 블록(텍스트, 이미지)의 생성, 수정, 삭제를 담당하는 매니저 클래스
 */
class ContentBlockManager @Inject constructor() {
    private lateinit var chain: EditorChain
    private lateinit var onPublish: () -> Unit
    private lateinit var onShowToast: (String) -> Unit

    fun initialize(chain: EditorChain, onPublish: () -> Unit, onShowToast: (String) -> Unit) {
        this.chain = chain
        this.onPublish = onPublish
        this.onShowToast = onShowToast
    }
    
    companion object {
        private const val DEFAULT_TEXT_CONTENT = ""
        private const val BR_TAG = "<br>"
    }


    /**
     * 빈 텍스트 블록에서 Backspace 처리
     */
    fun onBackspaceEmptyAt(textIndex: Int): BackspaceResult? {
        return onBackspaceAtStart(textIndex)
    }

    /**
     * 텍스트 블록 시작에서 Backspace 처리 (이전 블록과 병합)
     * @return Pair<포커스할 블록 인덱스, 커서 위치>
     */
    fun onBackspaceAtStart(textIndex: Int): BackspaceResult? {
        val ordered = chain.toLinear()
        val node = ordered.getOrNull(textIndex) ?: return null
        if (node.block.type != ContentType.TEXT) return null

        val prevIdx = (textIndex - 1 downTo 0).firstOrNull { 
            ordered[it].block.type == ContentType.TEXT 
        } ?: return null
        val prevNode = ordered[prevIdx]

        // 이전 블록의 현재 길이를 저장 (커서 위치로 사용)
        val cursorPosition = prevNode.block.content.parseHtml().length
        
        prevNode.block.content += node.block.content
        chain.remove(node.id)

        if (chain.headId() != null && chain.isImage(chain.headId())) {
            chain.insertBetween(null, chain.headId(), ContentBlockModel(ContentType.TEXT, DEFAULT_TEXT_CONTENT, 0))
        }
        ensureTrailingText()
        onPublish()

        val focusIndex = chain.toLinear().indexOfFirst { it.id == prevNode.id }
        return BackspaceResult(focusIndex, cursorPosition)
    }

    /**
     * 콘텐츠 블록 삭제
     */
    fun deleteContentBlock(contentBlock: ContentBlockModel): Boolean {
        if (contentBlock.type != ContentType.IMAGE) return false

        val targetId = chain.toLinear()
            .firstOrNull { 
                it.block.type == ContentType.IMAGE && 
                it.block.content == contentBlock.content && 
                it.block.position == contentBlock.position 
            }?.id ?: return false

        val orderedBefore = chain.toLinear()
        val idx = orderedBefore.indexOfFirst { it.id == targetId }
        val left = orderedBefore.getOrNull(idx - 1)?.id
        val right = orderedBefore.getOrNull(idx + 1)?.id

        chain.remove(targetId)

        // 인접한 텍스트 블록 병합
        if (left != null && right != null && chain.isText(left) && chain.isText(right)) {
            val leftNode = chain.node(left)!!
            val rightNode = chain.node(right)!!
            leftNode.block.content += rightNode.block.content
            chain.remove(rightNode.id)
        }

        if (chain.headId() != null && chain.isImage(chain.headId())) {
            chain.insertBetween(null, chain.headId(), ContentBlockModel(ContentType.TEXT, DEFAULT_TEXT_CONTENT, 0))
        }
        ensureTrailingText()
        onPublish()
        return true
    }

    /**
     * 간격 탭 처리 (새 텍스트 블록 생성)
     * @return GapTapResult(포커스할 블록 인덱스, 커서 위치)
     */
    fun onGapTapped(leftIndex: Int?, rightIndex: Int?): GapTapResult? {
        val ordered = chain.toLinear()
        val leftId = leftIndex?.let { ordered.getOrNull(it)?.id }
        val rightId = rightIndex?.let { ordered.getOrNull(it)?.id }
        val newTextId = chain.insertBetween(leftId, rightId, ContentBlockModel(ContentType.TEXT, DEFAULT_TEXT_CONTENT, 0))
        onPublish()
        val focusIndex = chain.toLinear().indexOfFirst { it.id == newTextId }
        return GapTapResult(focusIndex, 0) // 새 블록이므로 커서 위치는 0
    }

    /**
     * 저장 가능 여부 확인
     */
    fun checkCanSave(): Boolean {
        val textBlocks = chain.toLinear().filter { it.block.type == ContentType.TEXT }
        return textBlocks.any { block ->
            val content = block.block.content.parseHtml().trim()
            content.isNotBlank() && content != BR_TAG
        }
    }

    /**
     * 빈 블록 정리
     */
    fun trimBlankBlocks() {
        val ordered = chain.toLinear()
        val toRemove = mutableListOf<String>()
        ordered.forEach { node ->
            if (node.block.type == ContentType.TEXT) {
                val trimmed = node.block.content.split(BR_TAG)
                    .joinToString("") { it.trim() }
                    .parseHtml()
                    .trim()
                if (trimmed.isEmpty()) toRemove += node.id
            }
        }
        if (toRemove.size == ordered.size) toRemove.removeLastOrNull()
        toRemove.forEach { chain.remove(it) }
        ensureInitialText()
        onPublish()
    }

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
        return Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY).toString()
    }
}
