package com.umc.edison.presentation.edison

import com.umc.edison.domain.DataResource
import com.umc.edison.domain.model.bubble.Bubble
import com.umc.edison.domain.model.label.Label
import com.umc.edison.domain.usecase.bubble.AddBubbleUseCase
import com.umc.edison.domain.usecase.bubble.GetAllBubblesUseCase
import com.umc.edison.domain.usecase.bubble.GetBubbleUseCase
import com.umc.edison.domain.usecase.bubble.UpdateBubbleUseCase
import com.umc.edison.domain.usecase.label.AddLabelUseCase
import com.umc.edison.domain.usecase.label.GetAllLabelsUseCase
import com.umc.edison.presentation.model.BubbleModel
import com.umc.edison.presentation.model.LabelModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 버블 데이터 관리를 담당하는 매니저 클래스
 * 버블 CRUD, 라벨 관리, 백링크 관리 등을 처리합니다.
 */
class BubbleDataManager @Inject constructor(
    private val getBubbleUseCase: GetBubbleUseCase,
    private val getAllBubblesUseCase: GetAllBubblesUseCase,
    private val addBubbleUseCase: AddBubbleUseCase,
    private val updateBubbleUseCase: UpdateBubbleUseCase,
    private val getAllLabelsUseCase: GetAllLabelsUseCase,
    private val addLabelUseCase: AddLabelUseCase
) {

    /**
     * 특정 버블 조회
     */
    fun getBubble(bubbleId: String): Flow<DataResource<Bubble>> {
        return getBubbleUseCase(bubbleId)
    }

    /**
     * 모든 버블 조회
     */
    fun getAllBubbles(): Flow<DataResource<List<Bubble>>> {
        return getAllBubblesUseCase()
    }

    /**
     * 모든 라벨 조회
     */
    fun getAllLabels(): Flow<DataResource<List<Label>>> {
        return getAllLabelsUseCase()
    }

    /**
     * 버블 저장 (새로 생성)
     */
    fun addBubble(bubble: BubbleModel): Flow<DataResource<Bubble>> {
        return addBubbleUseCase(bubble.toDomain())
    }

    /**
     * 버블 업데이트
     */
    fun updateBubble(bubble: BubbleModel): Flow<DataResource<Bubble>> {
        return updateBubbleUseCase(bubble.toDomain())
    }

    /**
     * 라벨 저장
     */
    fun addLabel(label: LabelModel): Flow<DataResource<Unit>> {
        return addLabelUseCase(label.toDomain())
    }

    /**
     * 백링크 추가
     */
    fun addBackLink(currentBubble: BubbleModel, targetBubble: BubbleModel): BubbleModel {
        val existingBackLinks = currentBubble.backLinks
        return if (existingBackLinks.any { it.id == targetBubble.id }) {
            currentBubble // 이미 존재하는 경우
        } else {
            currentBubble.copy(backLinks = existingBackLinks + targetBubble)
        }
    }

    /**
     * 백링크 삭제
     */
    fun removeBackLink(currentBubble: BubbleModel, targetBackLink: BubbleModel): BubbleModel {
        val updatedBackLinks = currentBubble.backLinks.filterNot { it.id == targetBackLink.id }
        return currentBubble.copy(backLinks = updatedBackLinks)
    }

    /**
     * 링크 버블 삭제
     */
    fun removeLinkBubble(currentBubble: BubbleModel, targetLinkBubble: BubbleModel): BubbleModel {
        return if (currentBubble.linkedBubble?.id == targetLinkBubble.id) {
            currentBubble.copy(linkedBubble = null)
        } else {
            currentBubble
        }
    }

    /**
     * 메인 이미지 선택/해제
     */
    fun toggleMainImage(currentBubble: BubbleModel, imageUri: String?): BubbleModel {
        return currentBubble.copy(
            mainImage = if (currentBubble.mainImage == imageUri) null else imageUri
        )
    }

    /**
     * 선택된 라벨 업데이트
     */
    fun updateSelectedLabels(currentBubble: BubbleModel, labels: List<LabelModel>): BubbleModel {
        return currentBubble.copy(labels = labels)
    }

    /**
     * 버블 제목 업데이트
     */
    fun updateTitle(currentBubble: BubbleModel, title: String): BubbleModel {
        return currentBubble.copy(title = title)
    }
}
