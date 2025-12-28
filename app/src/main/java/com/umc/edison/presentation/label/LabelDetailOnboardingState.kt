package com.umc.edison.presentation.label

import com.umc.edison.presentation.onboarding.OnboardingPositionState

data class LabelDetailOnboardingState(
    val show: Boolean,
    val bubbleBound: OnboardingPositionState,
    val isReady: Boolean = false
) {
    companion object {
        val DEFAULT = LabelDetailOnboardingState(
            show = false,
            bubbleBound = OnboardingPositionState.DEFAULT,
            isReady = false
        )
    }
}

