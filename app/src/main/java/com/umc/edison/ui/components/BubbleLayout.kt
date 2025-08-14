package com.umc.edison.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.umc.edison.presentation.model.BubbleModel
import com.umc.edison.ui.theme.White000
import kotlinx.coroutines.android.awaitFrame
import kotlin.math.sqrt
import kotlin.random.Random

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun BubblesLayout(
    bubbles: List<BubbleModel>,
    onBubbleClick: (BubbleModel) -> Unit,
    onBubbleLongClick: (BubbleModel) -> Unit = {},
    isBlur: Boolean = false,
    selectedBubble: List<BubbleModel> = emptyList(),
    searchKeyword: String = "",
    isReversed: Boolean = false,
    setGlobalBubblePosition: (Offset, IntSize) -> Unit = { _, _ -> },
) {
    val bubbleOffsets = remember { mutableStateMapOf<BubbleModel, Pair<Dp, Dp>>() }
    val scrollState = rememberScrollState()

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp - 16.dp
    val columnWidth = screenWidth / 3
    val rowHeight = 90.dp
    val interBubblePadding = 4.dp

    val placedBubbles = remember(bubbles) { mutableListOf<PlacedBubble>() }
    var maxYOffset by remember { mutableStateOf(0.dp) }

    LaunchedEffect(isReversed, bubbles.size) {
        if (isReversed) {
            awaitFrame()
            scrollState.scrollTo(scrollState.maxValue)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .verticalScroll(scrollState)
    ) {
        val verticalPadding = if (isReversed) 50.dp else 0.dp

        bubbles.forEach { bubble ->
            val bubbleSize = calculateBubblePreviewSize(bubble)

            val (xOffset, yOffset) = bubbleOffsets.getOrPut(bubble) {
                calculateGridOffset(
                    bubbleSize = bubbleSize.size,
                    placedBubbles = placedBubbles,
                    columnWidth = columnWidth,
                    rowHeight = rowHeight,
                    screenWidth = screenWidth,
                    padding = interBubblePadding
                )
            }

            placedBubbles.add(PlacedBubble(xOffset, yOffset, bubbleSize.size))

            if (yOffset + bubbleSize.size > maxYOffset) {
                maxYOffset = yOffset + bubbleSize.size
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(maxYOffset + verticalPadding * 2)
                .padding(vertical = verticalPadding)
        ) {
            bubbles.forEachIndexed { index, bubble ->
                val bubbleSize = calculateBubblePreviewSize(bubble)
                val (xOffset, yOffset) = bubbleOffsets[bubble] ?: (0.dp to 0.dp)

                Box(
                    modifier = Modifier
                        .size(bubbleSize.size)
                        .offset(x = xOffset, y = yOffset)
                ) {
                    BubblePreview(
                        bubble = bubble,
                        size = bubbleSize,
                        onClick = { onBubbleClick(bubble) },
                        onLongClick = { onBubbleLongClick(bubble) },
                        searchKeyword = searchKeyword,
                        modifier = Modifier.onGloballyPositioned { coordinates ->
                            if (index == bubbles.size / 2) {
                                val position = coordinates.positionOnScreen()
                                val size = coordinates.size
                                setGlobalBubblePosition(position, size)
                            }
                        }
                    )

                    if (isBlur && !selectedBubble.contains(bubble)) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(color = White000.copy(alpha = 0.5f), shape = CircleShape)
                                .graphicsLayer {
                                    renderEffect = BlurEffect(
                                        radiusX = 16.dp.toPx(),
                                        radiusY = 16.dp.toPx()
                                    )
                                }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun calculateGridOffset(
    bubbleSize: Dp,
    placedBubbles: List<PlacedBubble>,
    columnWidth: Dp,
    rowHeight: Dp,
    screenWidth: Dp,
    padding: Dp
): Pair<Dp, Dp> {
    val columns = listOf(0, 1, 2)
    val random = remember { Random(System.currentTimeMillis()) }
    val maxX = screenWidth - bubbleSize - padding
    val usedYOffsets = placedBubbles.map { it.y }.toSet()

    for (row in 0..100) {
        val yRandomOffset = Dp(random.nextInt(0, (rowHeight.value / 2).toInt()).toFloat())
        val yOffset = rowHeight * row + yRandomOffset
        // 같은 yOffset이 있으면 건너뜀
        if (usedYOffsets.contains(yOffset)) continue
        val shuffledColumns = columns.shuffled(random)
        for (col in shuffledColumns) {
            val rawX = columnWidth * col + padding
            val xOffset = min(rawX, maxX)
            // 바로 전 버블과 xOffset이 같으면 건너뜀
            if (placedBubbles.isNotEmpty() && placedBubbles.last().x == xOffset) continue
            val trial = PlacedBubble(xOffset, yOffset, bubbleSize)
            val overlaps = placedBubbles.any { isOverlapping(it, trial) }
            if (!overlaps) {
                return xOffset to yOffset
            }
        }
    }
    return 0.dp to (rowHeight * 100)
}

fun isOverlapping(b1: PlacedBubble, b2: PlacedBubble): Boolean {
    val dx = (b1.x.value + b1.size.value / 2) - (b2.x.value + b2.size.value / 2)
    val dy = (b1.y.value + b1.size.value / 2) - (b2.y.value + b2.size.value / 2)
    val distance = sqrt(dx * dx + dy * dy)
    val threshold = (b1.size.value + b2.size.value) / 2 + 4.dp.value
    return distance < threshold
}

data class PlacedBubble(val x: Dp, val y: Dp, val size: Dp)
