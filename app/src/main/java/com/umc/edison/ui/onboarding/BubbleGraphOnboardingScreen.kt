package com.umc.edison.ui.onboarding

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.window.Popup
import com.umc.edison.presentation.space.BubbleGraphOnboardingState

@Composable
fun BubbleGraphOnboardingScreen(
    onboardingState: BubbleGraphOnboardingState,
    onDismiss: () -> Unit,
) {
    val density = LocalDensity.current
    val statusBarHeightPx = with(density) { WindowInsets.statusBars.getTop(density) }

    Popup {
        ButtonOnboarding(
            buttonBound = onboardingState.keywordMapButtonBound,
            text = "모든 버블을 맵 형태로 확인해요.\n키워드 맵핑으로 지금 필요한 아이디어를 찾아보세요.",
            offsetDp = OnboardingConstants.OFFSET_MEDIUM,
            onDismiss = onDismiss,
            statusBarHeightPx = statusBarHeightPx
        )
    }
}

