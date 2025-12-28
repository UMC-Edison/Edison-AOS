package com.umc.edison.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.umc.edison.presentation.edison.BubbleInputOnboardingPage
import com.umc.edison.presentation.edison.BubbleInputOnboardingState
import com.umc.edison.presentation.onboarding.OnboardingPositionState
import com.umc.edison.ui.components.ToolbarPopupConstants
import com.umc.edison.ui.theme.Black000
import com.umc.edison.ui.theme.White000
import kotlin.math.roundToInt

@Composable
fun BubbleInputOnboardingScreen(
    onboardingState: BubbleInputOnboardingState,
    onNextPage: () -> Unit,
    onDismiss: () -> Unit,
) {
    val density = LocalDensity.current
    val statusBarHeightPx = with(density) { WindowInsets.statusBars.getTop(density) }

    Popup {
        when (onboardingState.currentPage) {
            BubbleInputOnboardingPage.LABEL -> {
                LabelButtonOnboarding(
                    labelButtonBound = onboardingState.labelButtonBound,
                    onNextPage = onNextPage,
                    statusBarHeightPx = statusBarHeightPx
                )
            }

            BubbleInputOnboardingPage.LINK -> {
                LinkButtonOnboarding(
                    linkButtonBound = onboardingState.linkButtonBound,
                    onNextPage = onNextPage,
                    statusBarHeightPx = statusBarHeightPx
                )
            }

            BubbleInputOnboardingPage.LINK_MENU -> {
                LinkMenuOnboarding(
                    linkButtonBound = onboardingState.linkButtonBound,
                    linkMenuBound = onboardingState.linkMenuBound,
                    onDismiss = onDismiss,
                    statusBarHeightPx = statusBarHeightPx
                )
            }
        }
    }
}

@Composable
fun LabelButtonOnboarding(
    labelButtonBound: OnboardingPositionState,
    onNextPage: () -> Unit,
    statusBarHeightPx: Int,
) {
    ButtonOnboarding(
        buttonBound = labelButtonBound,
        text = "라벨을 통해 버블을 관리할 수 있어요! 최대 3개까지, 버블에 맞는 태그를 걸어보세요!",
        offsetDp = OnboardingConstants.OFFSET_MEDIUM,
        onDismiss = onNextPage,
        statusBarHeightPx = statusBarHeightPx
    )
}

@Composable
fun LinkButtonOnboarding(
    linkButtonBound: OnboardingPositionState,
    onNextPage: () -> Unit,
    statusBarHeightPx: Int,
) {
    ButtonOnboarding(
        buttonBound = linkButtonBound,
        text = "백링크를 통해 버블을 연결하세요!\n이 버블과 연결된 새로운 아이디어는\n링크버블로 작성할 수 있어요!",
        offsetDp = OnboardingConstants.OFFSET_LARGE,
        onDismiss = onNextPage,
        statusBarHeightPx = statusBarHeightPx
    )
}

@Composable
fun LinkMenuOnboarding(
    linkButtonBound: OnboardingPositionState,
    linkMenuBound: OnboardingPositionState,
    onDismiss: () -> Unit,
    statusBarHeightPx: Int,
) {
    val density = LocalDensity.current

    // 링크 버튼 위치
    val buttonCenterX = linkButtonBound.offset.x + linkButtonBound.size.width / 2f
    val buttonCenterY = linkButtonBound.offset.y - statusBarHeightPx + linkButtonBound.size.height / 2f
    val buttonRadius = linkButtonBound.size.width.toFloat() / 2f

    // 팝업 위치
    val buttonBottom = linkButtonBound.offset.y - statusBarHeightPx + linkButtonBound.size.height
    val popupOffsetY = with(density) { ToolbarPopupConstants.POPUP_OFFSET_Y.dp.toPx() }
    val popupBottom = buttonBottom + popupOffsetY
    val popupTop = popupBottom - linkMenuBound.size.height

    val textPadding = with(density) { OnboardingConstants.TEXT_TO_POPUP_PADDING.dp.toPx() }
    val textBoxY = (popupTop - textPadding).roundToInt()
        .coerceAtLeast(OnboardingConstants.TEXT_BOX_MIN_TOP_MARGIN)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        event.changes.forEach { it.consume() }
                    }
                }
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
            .drawWithContent {
                val overlayRect = Rect(Offset.Zero, size)
                val holePath = Path().apply {
                    addRect(overlayRect)

                    addOval(
                        Rect(
                            left = buttonCenterX - buttonRadius,
                            top = buttonCenterY - buttonRadius,
                            right = buttonCenterX + buttonRadius,
                            bottom = buttonCenterY + buttonRadius
                        )
                    )

                    fillType = PathFillType.EvenOdd
                }

                clipPath(holePath) {
                    drawRect(color = Black000.copy(alpha = OnboardingConstants.OVERLAY_ALPHA))
                }

                drawContent()
            }
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset {
                    IntOffset(
                        x = 0,
                        y = textBoxY
                    )
                }
                .background(
                    color = White000,
                    shape = RoundedCornerShape(OnboardingConstants.TEXT_BOX_CORNER_RADIUS)
                )
                .padding(OnboardingConstants.TEXT_BOX_INNER_PADDING.dp)
        ) {
            Text(
                text = "백링크를 통해 기존 메모를 연결하세요!\n자동으로 연결되는 새로운 버블을\n만들어보세요!",
                color = Color.Black,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}
