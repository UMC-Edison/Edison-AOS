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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.umc.edison.presentation.edison.MyEdisonOnboardingState
import com.umc.edison.presentation.onboarding.OnboardingPositionState
import com.umc.edison.ui.theme.Black000
import com.umc.edison.ui.theme.White000
import kotlin.math.roundToInt

internal enum class MyEdisonOnboardingPage {
    MY_EDISON_BOTTOM_TAB, BUBBLE_INPUT, BUBBLE_STORAGE, BUBBLE_LABEL,
    SPACE_BOTTOM_TAB, BUBBLE_BOTTOM_TAB, ART_LETTER_BOTTOM_TAB, MY_PAGE_BOTTOM_TAB,
}

@Composable
fun MyEdisonOnboarding(
    onboardingState: MyEdisonOnboardingState,
    bottomNavBarBounds: List<OnboardingPositionState>,
    changeToStorageMode: () -> Unit,
    changeToLabelMode: () -> Unit,
    changeToBubbleInputMode: () -> Unit,
    onDismiss: () -> Unit,
) {
    var currentPage by remember { mutableStateOf(MyEdisonOnboardingPage.MY_EDISON_BOTTOM_TAB) }
    val density = LocalDensity.current
    val statusBarHeightPx = with(density) { WindowInsets.statusBars.getTop(density) }

    Popup {
        when (currentPage) {
            MyEdisonOnboardingPage.MY_EDISON_BOTTOM_TAB -> {
                BottomTabOnboarding(
                    bottomTabComponent = bottomNavBarBounds[0],
                    description = "버블을 작성하고 일주일 내 작성한\n" + "버블을 모아보세요.",
                    onNextPage = { currentPage = MyEdisonOnboardingPage.BUBBLE_INPUT },
                    statusBarHeightPx = statusBarHeightPx
                )
            }

            MyEdisonOnboardingPage.BUBBLE_INPUT -> {
                BubbleInputOnboarding(
                    edisonNavBarComponent = onboardingState.myEdisonNavBarBounds[0],
                    bubbleInputComponent = onboardingState.bubbleInputBound,
                    onNextPage = {
                        currentPage = MyEdisonOnboardingPage.BUBBLE_STORAGE
                        changeToStorageMode()
                    },
                    statusBarHeightPx = statusBarHeightPx
                )
            }

            MyEdisonOnboardingPage.BUBBLE_STORAGE -> {
                EdisonNavBarOnboarding(
                    edisonNavBarComponent = onboardingState.myEdisonNavBarBounds[1],
                    onNextPage = {
                        currentPage = MyEdisonOnboardingPage.BUBBLE_LABEL
                        changeToLabelMode()
                    },
                    statusBarHeightPx = statusBarHeightPx,
                    text = "일주일 간 작성한 버블을 모아볼 수 있어요."
                )
            }

            MyEdisonOnboardingPage.BUBBLE_LABEL -> {
                EdisonNavBarOnboarding(
                    edisonNavBarComponent = onboardingState.myEdisonNavBarBounds[2],
                    onNextPage = {
                        currentPage = MyEdisonOnboardingPage.SPACE_BOTTOM_TAB
                        changeToBubbleInputMode()
                    },
                    statusBarHeightPx = statusBarHeightPx,
                    text = "라벨별로 버블을 모아볼 수 있어요."
                )
            }

            MyEdisonOnboardingPage.SPACE_BOTTOM_TAB -> {
                BottomTabOnboarding(
                    bottomTabComponent = bottomNavBarBounds[1],
                    description = "모든 버블을 맵 형태로 확인해요.\n" + "키워드 맵핑으로 지금 필요한 아이디어를 찾아보세요.",
                    onNextPage = { currentPage = MyEdisonOnboardingPage.BUBBLE_BOTTOM_TAB },
                    statusBarHeightPx = statusBarHeightPx
                )
            }

            MyEdisonOnboardingPage.BUBBLE_BOTTOM_TAB -> {
                BottomTabOnboarding(
                    bottomTabComponent = bottomNavBarBounds[2],
                    description = "버블 PLUS 버튼을 누르면 어떤\n" + "페이지에서도 버블을 작성할 수 있어요.",
                    onNextPage = { currentPage = MyEdisonOnboardingPage.ART_LETTER_BOTTOM_TAB },
                    statusBarHeightPx = statusBarHeightPx
                )
            }

            MyEdisonOnboardingPage.ART_LETTER_BOTTOM_TAB -> {
                BottomTabOnboarding(
                    bottomTabComponent = bottomNavBarBounds[3],
                    description = "영감을 자극하는 아트 레터를 읽어요!",
                    onNextPage = { currentPage = MyEdisonOnboardingPage.MY_PAGE_BOTTOM_TAB },
                    statusBarHeightPx = statusBarHeightPx
                )
            }

            MyEdisonOnboardingPage.MY_PAGE_BOTTOM_TAB -> {
                BottomTabOnboarding(
                    bottomTabComponent = bottomNavBarBounds[4],
                    description = "내 프로필을 수정하고 북마크한\n" + "아트레터를 확인할 수 있어요.",
                    onNextPage = onDismiss,
                    statusBarHeightPx = statusBarHeightPx
                )
            }
        }
    }
}

@Composable
fun BottomTabOnboarding(
    bottomTabComponent: OnboardingPositionState,
    description: String,
    onNextPage: () -> Unit,
    statusBarHeightPx: Int,
) {
    val density = LocalDensity.current

    val widthPx = bottomTabComponent.size.width.toFloat()
    val centerX = bottomTabComponent.offset.x + widthPx / 2f
    val centerY =
        bottomTabComponent.offset.y - statusBarHeightPx + bottomTabComponent.size.height / 2f
    val radius = widthPx / 2f

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
                onClick = onNextPage
            )
            .drawWithContent {
                val overlayRect = Rect(Offset.Zero, size)
                val holePath = Path().apply {
                    addRect(overlayRect)
                    addOval(
                        Rect(
                            left = centerX - radius,
                            top = centerY - radius,
                            right = centerX + radius,
                            bottom = centerY + radius
                        )
                    )
                    fillType = PathFillType.EvenOdd
                }

                clipPath(holePath) {
                    drawRect(color = Black000.copy(alpha = 0.5f))
                }

                drawContent()
            }) {
        val offsetY = with(density) { 30.dp.toPx() }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset {
                    IntOffset(
                        x = 0,
                        y = (bottomTabComponent.offset.y - statusBarHeightPx - radius * 2 - offsetY).roundToInt()
                    )
                }
                .background(color = White000, shape = RoundedCornerShape(16))) {
            Text(
                text = description,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.Center),
                color = Color.Black,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun BubbleInputOnboarding(
    edisonNavBarComponent: OnboardingPositionState,
    bubbleInputComponent: OnboardingPositionState,
    onNextPage: () -> Unit,
    statusBarHeightPx: Int,
) {
    val density = LocalDensity.current

    val edisonCenterX = edisonNavBarComponent.offset.x + edisonNavBarComponent.size.width / 2f
    val edisonCenterY =
        edisonNavBarComponent.offset.y - statusBarHeightPx + edisonNavBarComponent.size.height / 2f
    val edisonRadius = edisonNavBarComponent.size.width.toFloat() / 2f

    val bubbleCenterX = bubbleInputComponent.offset.x + bubbleInputComponent.size.width / 2f
    val bubbleCenterY = bubbleInputComponent.offset.y - statusBarHeightPx + bubbleInputComponent.size.height / 2f
    val bubbleRadius = bubbleInputComponent.size.width.toFloat() / 2f

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
                onClick = onNextPage
            )
            .drawWithContent {
                val overlayRect = Rect(Offset.Zero, size)
                val holePath = Path().apply {
                    addRect(overlayRect)

                    addOval(
                        Rect(
                            left = edisonCenterX - edisonRadius,
                            top = edisonCenterY - edisonRadius,
                            right = edisonCenterX + edisonRadius,
                            bottom = edisonCenterY + edisonRadius
                        )
                    )

                    addOval(
                        Rect(
                            left = bubbleCenterX - bubbleRadius,
                            top = bubbleCenterY - bubbleRadius,
                            right = bubbleCenterX + bubbleRadius,
                            bottom = bubbleCenterY + bubbleRadius
                        )
                    )

                    fillType = PathFillType.EvenOdd
                }

                clipPath(holePath) {
                    drawRect(color = Black000.copy(alpha = 0.5f))
                }

                drawContent()
            }) {
        val offsetY = with(density) { 20.dp.toPx() }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset {
                    IntOffset(
                        x = 0,
                        y = (edisonNavBarComponent.offset.y - statusBarHeightPx + edisonRadius * 2 + offsetY).roundToInt()
                    )
                }
                .background(color = White000, shape = RoundedCornerShape(16))
        ) {
            Text(
                text = "버블을 눌러 아이디어 작성을 시작할 수 있어요!",
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.Center),
                color = Color.Black,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun EdisonNavBarOnboarding(
    edisonNavBarComponent: OnboardingPositionState,
    onNextPage: () -> Unit,
    statusBarHeightPx: Int,
    text: String,
) {
    val density = LocalDensity.current

    val edisonCenterX = edisonNavBarComponent.offset.x + edisonNavBarComponent.size.width / 2f
    val edisonCenterY =
        edisonNavBarComponent.offset.y - statusBarHeightPx + edisonNavBarComponent.size.height / 2f
    val edisonRadius = edisonNavBarComponent.size.width.toFloat() / 2f

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
                onClick = onNextPage
            )
            .drawWithContent {
                val overlayRect = Rect(Offset.Zero, size)
                val holePath = Path().apply {
                    addRect(overlayRect)

                    addOval(
                        Rect(
                            left = edisonCenterX - edisonRadius,
                            top = edisonCenterY - edisonRadius,
                            right = edisonCenterX + edisonRadius,
                            bottom = edisonCenterY + edisonRadius
                        )
                    )

                    fillType = PathFillType.EvenOdd
                }

                clipPath(holePath) {
                    drawRect(color = Black000.copy(alpha = 0.5f))
                }

                drawContent()
            }
    ) {
        val offsetY = with(density) { 20.dp.toPx() }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset {
                    IntOffset(
                        x = 0,
                        y = (edisonNavBarComponent.offset.y - statusBarHeightPx + edisonRadius * 2 + offsetY).roundToInt()
                    )
                }
                .background(color = White000, shape = RoundedCornerShape(16))
        ) {
            Text(
                text = text,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.Center),
                color = Color.Black,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}
