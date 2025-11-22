package com.umc.edison.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.umc.edison.R
import com.umc.edison.presentation.onboarding.OnboardingPositionState
import com.umc.edison.ui.theme.Black000
import com.umc.edison.ui.theme.White000
import kotlin.math.roundToInt

@Composable
fun LabelListOnboardingScreen(
    onDismiss: () -> Unit,
    labelListItemComponent: OnboardingPositionState,
) {
    val density = LocalDensity.current
    val statusBarHeightPx = with(density) { WindowInsets.statusBars.getTop(density) }

    val rectTop = labelListItemComponent.offset.y - statusBarHeightPx
    val rectHeight = labelListItemComponent.size.height.toFloat()

    Popup(alignment = Alignment.TopStart) {
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

                    val clipRect = Rect(
                        left = 0f,
                        top = rectTop,
                        right = size.width,
                        bottom = rectTop + rectHeight
                    )

                    val path = Path().apply {
                        addRect(overlayRect)
                        addRect(clipRect)
                        fillType = PathFillType.EvenOdd
                    }

                    clipPath(path) {
                        drawRect(color = Black000.copy(alpha = 0.5f))
                    }

                    drawContent()
                }
        ) {
            val offsetY = with(density) { 20.dp.toPx() }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .align(Alignment.TopCenter)
                    .offset {
                        IntOffset(
                            x = -12.dp.roundToPx(),
                            y = (rectTop + rectHeight + offsetY).roundToInt()
                        )
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                val imageRequest = ImageRequest.Builder(LocalContext.current)
                    .data(R.drawable.ic_up_slide)
                    .build()

                AsyncImage(
                    model = imageRequest,
                    contentDescription = "Slide Up Icon",
                    modifier = Modifier
                        .rotate(-90f)
                        .size(44.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .background(color = White000, shape = RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = "옆으로 밀어 라벨을 관리하세요!",
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.Black,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
