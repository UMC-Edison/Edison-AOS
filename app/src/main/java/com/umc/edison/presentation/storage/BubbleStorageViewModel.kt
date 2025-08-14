package com.umc.edison.presentation.storage

import android.app.Application
import android.content.Intent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import androidx.core.text.HtmlCompat
import com.umc.edison.domain.usecase.bubble.GetAllRecentBubblesUseCase
import com.umc.edison.domain.usecase.bubble.TrashBubblesUseCase
import com.umc.edison.domain.usecase.onboarding.GetHasSeenOnboardingUseCase
import com.umc.edison.domain.usecase.onboarding.SetHasSeenOnboardingUseCase
import com.umc.edison.presentation.ToastManager
import com.umc.edison.presentation.baseBubble.BaseBubbleViewModel
import com.umc.edison.presentation.baseBubble.BubbleStorageMode
import com.umc.edison.presentation.model.toPresentation
import com.umc.edison.presentation.onboarding.OnboardingPositionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class BubbleStorageViewModel @Inject constructor(
    toastManager: ToastManager,
    private val context: Application,
    private val getAllRecentBubblesUseCase: GetAllRecentBubblesUseCase,
    override val trashBubblesUseCase: TrashBubblesUseCase,
    getHasSeenOnboardingUseCase: GetHasSeenOnboardingUseCase,
    private val setHasSeenOnboardingUseCase: SetHasSeenOnboardingUseCase,
) : BaseBubbleViewModel<BubbleStorageMode, BubbleStorageState>(toastManager) {

    override val _uiState = MutableStateFlow(BubbleStorageState.DEFAULT)
    override val uiState = _uiState.asStateFlow()

    private val _onboardingState = MutableStateFlow(BubbleStorageOnboardingState.DEFAULT)
    val onboardingState = _onboardingState.asStateFlow()

    companion object {
        const val SCREEN_NAME = "bubble_storage"
        const val EDISON_SCREEN_NAME = "my_edison"
    }

    init {
        collectDataResource(
            flow = getHasSeenOnboardingUseCase(SCREEN_NAME),
            onSuccess = { hasSeen ->
                _onboardingState.update { it.copy(show = !hasSeen) }
            },
        )

        collectDataResource(
            flow = getHasSeenOnboardingUseCase(EDISON_SCREEN_NAME),
            onSuccess = { hasSeen ->
                _onboardingState.update { it.copy(edisonOnboardingShow = !hasSeen) }
            },
        )
    }

    fun fetchStorageBubbles() {
        _uiState.update { BubbleStorageState.DEFAULT }
        collectDataResource(
            flow = getAllRecentBubblesUseCase(),
            onSuccess = { bubbles ->
                val sortedBubbles = bubbles.sortedBy { it.date }
                _uiState.update { it.copy(bubbles = sortedBubbles.toPresentation()) }
            },
        )
    }

    fun shareImages() {
        _uiState.update {
            it.copy(
                mode = BubbleStorageMode.EDIT,
            )
        }
    }

    fun shareTexts() {
        val context = this.context
        val selectedBubbles = _uiState.value.selectedBubbles

        if (selectedBubbles.isEmpty()) return

        val textsToShare = selectedBubbles.joinToString("---\n\n") { bubble ->
            val title = bubble.title ?: ""
            val content = bubble.contentBlocks
                .filter { it.type.name == "TEXT" }
                .sortedBy { it.position }
                .joinToString {
                    HtmlCompat.fromHtml(it.content, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
                }

            "$title\n\n$content"
        }


        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, textsToShare)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // context가 Application이기 때문에 필요함
        context.startActivity(shareIntent)
    }

    override fun refreshDataAfterDeletion() {
        fetchStorageBubbles()
    }

    fun setHasSeenOnboarding() {
        collectDataResource(
            flow = setHasSeenOnboardingUseCase(SCREEN_NAME),
            onSuccess = {
                _onboardingState.update { it.copy(show = false) }
            },
        )
    }

    fun setBubbleBound(offset: Offset, size: IntSize) {
        _onboardingState.update { it.copy(bubbleBound = OnboardingPositionState(offset, size)) }
    }
}