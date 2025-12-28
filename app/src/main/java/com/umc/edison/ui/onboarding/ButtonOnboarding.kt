package com.umc.edison.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
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
import com.umc.edison.presentation.onboarding.OnboardingPositionState
import com.umc.edison.ui.theme.Black000
import com.umc.edison.ui.theme.White000
import kotlin.math.roundToInt

enum class TextPosition {
    ABOVE,  // 버튼 위쪽
    ABOVE_FAR,  // 버튼 위쪽 (버튼 높이만큼 추가 간격, bottom navigation용)
    BELOW   // 버튼 아래쪽
}

@Composable
fun ButtonOnboarding(
    buttonBound: OnboardingPositionState,
    text: String,
    offsetDp: Int = OnboardingConstants.OFFSET_MEDIUM,
    textPosition: TextPosition = TextPosition.ABOVE,
    onDismiss: () -> Unit,
    statusBarHeightPx: Int,
) {
    val density = LocalDensity.current

    val buttonCenterX = buttonBound.offset.x + buttonBound.size.width.toFloat() / 2f
    val buttonCenterY = buttonBound.offset.y - statusBarHeightPx + buttonBound.size.height / 2f
    val buttonRadius = buttonBound.size.width.toFloat() / 2f

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
        val offsetY = with(density) { offsetDp.dp.toPx() }

        when (textPosition) {
            TextPosition.ABOVE, TextPosition.ABOVE_FAR -> {
                val buttonTop = when (textPosition) {
                    TextPosition.ABOVE -> buttonCenterY - buttonRadius
                    TextPosition.ABOVE_FAR -> buttonCenterY - buttonRadius * 2
                    else -> 0f
                }
                val textBoxBottomY = (buttonTop - offsetY).roundToInt()
                    .coerceAtLeast(OnboardingConstants.TEXT_BOX_MIN_TOP_MARGIN)

                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset {
                            IntOffset(
                                x = 0,
                                y = textBoxBottomY
                            )
                        }
                        .padding(horizontal = OnboardingConstants.TEXT_BOX_HORIZONTAL_MARGIN.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .background(
                                color = White000,
                                shape = RoundedCornerShape(OnboardingConstants.TEXT_BOX_CORNER_RADIUS.dp)
                            )
                            .padding(OnboardingConstants.TEXT_BOX_INNER_PADDING.dp)
                    ) {
                        Text(
                            text = text,
                            color = Color.Black,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            TextPosition.BELOW -> {
                // 텍스트 박스의 top이 버튼 하단에서 offsetY만큼 떨어지도록
                val textBoxTopY = (buttonCenterY + buttonRadius + offsetY).roundToInt()

                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset {
                            IntOffset(
                                x = 0,
                                y = textBoxTopY
                            )
                        }
                        .padding(horizontal = OnboardingConstants.TEXT_BOX_HORIZONTAL_MARGIN.dp)
                        .background(
                            color = White000,
                            shape = RoundedCornerShape(OnboardingConstants.TEXT_BOX_CORNER_RADIUS.dp)
                        )
                        .padding(OnboardingConstants.TEXT_BOX_INNER_PADDING.dp)
                ) {
                    Text(
                        text = text,
                        color = Color.Black,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
