package com.umc.edison.presentation.space

import com.umc.edison.presentation.onboarding.OnboardingPositionState

data class BubbleGraphOnboardingState(
    val show: Boolean,
    val keywordMapButtonBound: OnboardingPositionState
) {
    companion object {
        val DEFAULT = BubbleGraphOnboardingState(
            show = false,
            keywordMapButtonBound = OnboardingPositionState.DEFAULT
        )
    }
}

