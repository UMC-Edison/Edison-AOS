package com.umc.edison.ui.onboarding

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.window.Popup
import com.umc.edison.presentation.onboarding.OnboardingPositionState
import com.umc.edison.presentation.storage.BubbleStorageOnboardingState

@Composable
fun BubbleSpaceOnboarding(
    onboardingState: BubbleStorageOnboardingState,
    onDismiss: () -> Unit,
) {
    val density = LocalDensity.current
    val statusBarHeightPx = with(density) { WindowInsets.statusBars.getTop(density) }

    Popup {
        BubbleDeleteOnboarding(
            bubbleComponent = onboardingState.bubbleBound,
            onDismiss = onDismiss,
            statusBarHeightPx = statusBarHeightPx
        )
    }
}

@Composable
fun BubbleDeleteOnboarding(
    bubbleComponent: OnboardingPositionState,
    onDismiss: () -> Unit,
    statusBarHeightPx: Int,
) {
    ButtonOnboarding(
        buttonBound = bubbleComponent,
        text = "꾹 누르면 버블을 삭제할 수 있어요!",
        offsetDp = 20,
        textPosition = TextPosition.BELOW,
        onDismiss = onDismiss,
        statusBarHeightPx = statusBarHeightPx
    )
}