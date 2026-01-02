package com.umc.edison.presentation.edison

import com.umc.edison.presentation.onboarding.OnboardingPositionState

data class BubbleInputOnboardingState(
    val show: Boolean,
    val currentPage: BubbleInputOnboardingPage,
    val labelButtonBound: OnboardingPositionState,
    val linkButtonBound: OnboardingPositionState,
    val linkMenuBound: OnboardingPositionState
) {
    companion object {
        val DEFAULT = BubbleInputOnboardingState(
            show = false,
            currentPage = BubbleInputOnboardingPage.LABEL,
            labelButtonBound = OnboardingPositionState.DEFAULT,
            linkButtonBound = OnboardingPositionState.DEFAULT,
            linkMenuBound = OnboardingPositionState.DEFAULT
        )
    }
}

enum class BubbleInputOnboardingPage {
    LABEL,      // 1단계: 라벨 버튼
    LINK,       // 2단계: 링크 버튼
    LINK_MENU   // 3단계: 링크 팝업 메뉴
}

