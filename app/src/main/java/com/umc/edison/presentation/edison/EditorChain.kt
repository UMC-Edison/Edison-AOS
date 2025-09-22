package com.umc.edison.presentation.edison

import com.umc.edison.presentation.model.ContentBlockModel
import com.umc.edison.presentation.model.ContentType
import java.util.UUID

/**
 * 에디터의 콘텐츠 블록들을 연결 리스트로 관리하는 클래스
 * 텍스트와 이미지 블록의 순서와 관계를 관리합니다.
 */
class EditorChain {
    private val nodes = mutableMapOf<String, Node>()
    private var head: String? = null
    private var tail: String? = null

    fun clear() { 
        nodes.clear()
        head = null
        tail = null 
    }

    fun fromLinear(linear: List<ContentBlockModel>) {
        clear()
        var prevId: String? = null
        linear.forEach { block ->
            val id = block.id.ifBlank { UUID.randomUUID().toString() }
            val node = Node(id = id, block = block.copy(id = id, position = 0))
            nodes[node.id] = node
            link(prevId, node.id)
            prevId = node.id
        }
    }

    fun toLinear(): List<Node> {
        val result = mutableListOf<Node>()
        var current = head
        while (current != null) {
            val node = nodes[current] ?: break
            result += node
            current = node.next
        }
        result.forEachIndexed { index, node -> 
            node.block.position = index 
        }
        return result
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
        val newId = UUID.randomUUID().toString()
        val next = if (anchorId == null) head else nodes[anchorId]?.next
        val newBlock = block.copy(id = newId, position = 0)
        nodes[newId] = Node(id = newId, block = newBlock, prev = anchorId, next = next)
        link(anchorId, newId)
        link(newId, next)
        return newId
    }

    fun insertBetween(leftId: String?, rightId: String?, block: ContentBlockModel): String {
        val newId = UUID.randomUUID().toString()
        val newBlock = block.copy(id = newId, position = 0)
        nodes[newId] = Node(id = newId, block = newBlock, prev = leftId, next = rightId)
        link(leftId, newId)
        link(newId, rightId)
        return newId
    }

    fun remove(targetId: String) {
        val node = nodes[targetId] ?: return
        link(node.prev, node.next)
        nodes.remove(targetId)
    }
}

/**
 * 연결 리스트의 노드를 나타내는 데이터 클래스
 */
data class Node(
    val id: String = UUID.randomUUID().toString(),
    val block: ContentBlockModel,
    var prev: String? = null,
    var next: String? = null
)
