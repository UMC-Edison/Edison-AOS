package com.umc.edison.presentation.label

import com.umc.edison.presentation.onboarding.OnboardingPositionState

data class LabelListOnboardingState(
    val show: Boolean,
    val labelBound: OnboardingPositionState,
    val draggedIndex: Int,
) {
    companion object {
        val DEFAULT = LabelListOnboardingState(
            show = false,
            labelBound = OnboardingPositionState.DEFAULT,
            draggedIndex = -1,
        )
    }
}
