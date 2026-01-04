package com.umc.edison.presentation.edison

import com.umc.edison.presentation.onboarding.OnboardingPositionState

data class MyEdisonOnboardingState(
    val show: Boolean,
    val bubbleInputBound: OnboardingPositionState,
    val myEdisonNavBarBounds: List<OnboardingPositionState>,
) {
    companion object {
        val DEFAULT = MyEdisonOnboardingState(
            show = false,
            bubbleInputBound = OnboardingPositionState.DEFAULT,
            myEdisonNavBarBounds = List(3) { OnboardingPositionState.DEFAULT },
        )
    }
}
