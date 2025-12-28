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
import com.umc.edison.presentation.space.BubbleGraphOnboardingState
import com.umc.edison.ui.theme.Black000
import com.umc.edison.ui.theme.White000
import kotlin.math.roundToInt

@Composable
fun BubbleGraphOnboardingScreen(
    onboardingState: BubbleGraphOnboardingState,
    onDismiss: () -> Unit,
) {
    val density = LocalDensity.current
    val statusBarHeightPx = with(density) { WindowInsets.statusBars.getTop(density) }

    Popup {
        KeywordMapButtonOnboarding(
            keywordMapButtonBound = onboardingState.keywordMapButtonBound,
            onDismiss = onDismiss,
            statusBarHeightPx = statusBarHeightPx
        )
    }
}

@Composable
fun KeywordMapButtonOnboarding(
    keywordMapButtonBound: com.umc.edison.presentation.onboarding.OnboardingPositionState,
    onDismiss: () -> Unit,
    statusBarHeightPx: Int,
) {
    val density = LocalDensity.current

    val buttonCenterX = keywordMapButtonBound.offset.x + keywordMapButtonBound.size.width / 2f
    val buttonCenterY = keywordMapButtonBound.offset.y - statusBarHeightPx + keywordMapButtonBound.size.height / 2f
    val buttonRadius = keywordMapButtonBound.size.width.toFloat() / 2f

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
                    drawRect(color = Black000.copy(alpha = 0.5f))
                }

                drawContent()
            }
    ) {
        val offsetY = with(density) { 80.dp.toPx() }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset {
                    IntOffset(
                        x = 0,
                        y = (buttonCenterY - buttonRadius - offsetY).roundToInt()
                    )
                }
                .background(color = White000, shape = RoundedCornerShape(16.dp))
        ) {
            Text(
                text = "모든 버블을 맵 형태로 확인해요.\n키워드 맵핑으로 지금 필요한 아이디어를 찾아보세요.",
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

