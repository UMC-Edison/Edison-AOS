package com.umc.edison.ui.space

import android.annotation.SuppressLint
import android.graphics.BlurMaskFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.umc.edison.R
import com.umc.edison.presentation.model.BubbleModel
import com.umc.edison.presentation.model.KeywordBubbleModel
import com.umc.edison.presentation.model.getDisplayTitle
import com.umc.edison.presentation.space.BubbleGraphViewModel
import com.umc.edison.presentation.space.KeywordViewModel
import com.umc.edison.ui.components.BubblePreview
import com.umc.edison.ui.components.BubbleType
import com.umc.edison.ui.components.calculateBubblePreviewSize
import com.umc.edison.ui.components.extractPlainText
import com.umc.edison.ui.theme.Gray100
import com.umc.edison.ui.theme.Gray300
import com.umc.edison.ui.theme.Gray400
import com.umc.edison.ui.theme.Gray500
import com.umc.edison.ui.theme.Gray800
import com.umc.edison.ui.theme.Gray900
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun BubbleGraphScreen(
    onShowKeywordMap :()->Unit,
    showBubble: (BubbleModel) -> Unit,
    viewModel: BubbleGraphViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(Unit) { viewModel.fetchClusteredBubbles() }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    BoxWithConstraints(
        modifier = Modifier
            .size(screenWidth, screenHeight)
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    val oldScale = scale
                    val newScale = (scale * zoom).coerceIn(0.2f, 3f)
                    offset = (offset - centroid) * (newScale / oldScale) + centroid + pan
                    scale = newScale
                }
            }
    ) {
        val density = LocalDensity.current
        val screenCenter = with(density) {
            Offset(maxWidth.toPx() / 2, maxHeight.toPx() / 2)
        }
        val blurRadius = 100f

        LaunchedEffect(uiState.bubbles) {
            if (uiState.bubbles.isNotEmpty()) {
                val targetBubble = uiState.bubbles.first().position
                offset = screenCenter - targetBubble * scale
            }
        }

        if (scale < 1.6f) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y,
                        transformOrigin = TransformOrigin(0f, 0f)
                    )
            ) {
                // 클러스터 구름 그리기
                uiState.clusters.forEach { cluster ->
                    if (cluster.colors.size <= 2) {
                        drawGradientBlurCircle(
                            center = cluster.position,
                            radius = cluster.radius,
                            colors = cluster.colors,
                            blurRadius = blurRadius,
                        )
                    } else {
                        drawOverlappingBlurCircles(
                            center = cluster.position,
                            bigRadius = cluster.radius,
                            colors = cluster.colors,
                            blurRadius = blurRadius,
                        )
                    }
                }

                // 연결선 그리기
                uiState.edges.forEach { edge ->
                    val start =
                        uiState.bubbles.find { it.bubble.id == edge.startBubbleId }?.position
                            ?: Offset.Zero
                    val end = uiState.bubbles.find { it.bubble.id == edge.endBubbleId }?.position
                        ?: Offset.Zero
                    drawLine(color = Gray500, start = start, end = end, strokeWidth = 1.dp.toPx())
                }

                // 버블 점 그리기
                val radius = 12f
                uiState.bubbles.forEach { positionedBubble ->
                    val colors: List<Color> = positionedBubble.bubble.labels.map { it.color }
                    if (colors.size <= 1) {
                        drawCircle(
                            color = colors.firstOrNull() ?: Gray500,
                            radius = radius,
                            center = positionedBubble.position
                        )
                    } else {
                        drawCircle(
                            brush = Brush.linearGradient(
                                colors,
                                start = positionedBubble.position - Offset(radius * 2, 0f),
                                end = positionedBubble.position + Offset(radius * 2, 0f),
                            ),
                            radius = radius,
                            center = positionedBubble.position
                        )
                    }
                    drawContext.canvas.nativeCanvas.drawText(
                        extractPlainText(positionedBubble.bubble).first.take(10)
                            .ifEmpty { "내용 없음" },
                        positionedBubble.position.x,
                        positionedBubble.position.y + radius.dp.toPx() + 10.dp.toPx(),
                        Paint().apply {
                            color = Gray800.toArgb()
                            textSize = 35f
                            textAlign = Paint.Align.CENTER
                        }
                    )
                }
            }
        } else {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // 클러스터 구름 그리기
                uiState.clusters.forEach { cluster ->
                    if (cluster.colors.size <= 2) {
                        drawGradientBlurCircle(
                            center = cluster.position * scale + offset,
                            radius = cluster.radius * scale,
                            colors = cluster.colors,
                            blurRadius = blurRadius,
                        )
                    } else {
                        drawOverlappingBlurCircles(
                            center = cluster.position * scale + offset,
                            bigRadius = cluster.radius * scale,
                            colors = cluster.colors,
                            blurRadius = blurRadius,
                        )
                    }
                }

                uiState.edges.forEach { edge ->
                    val startBubble = uiState.bubbles.find { it.bubble.id == edge.startBubbleId }
                    val endBubble = uiState.bubbles.find { it.bubble.id == edge.endBubbleId }
                    if (startBubble != null && endBubble != null) {
                        val start = startBubble.position * scale + offset
                        val end = endBubble.position * scale + offset
                        drawLine(
                            color = Gray500,
                            start = start,
                            end = end,
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                }
            }

            uiState.bubbles.forEach { positionedBubble ->
                val screenPosition = positionedBubble.position * scale + offset
                val previewSize = calculateBubblePreviewSize(positionedBubble.bubble)
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = screenPosition.x.roundToInt() - (previewSize.size.roundToPx() / 2),
                                y = screenPosition.y.roundToInt() - (previewSize.size.roundToPx() / 2)
                            )
                        }
                        .animateContentSize()
                ) {
                    BubblePreview(
                        bubble = positionedBubble.bubble,
                        size = previewSize,
                        onClick = {
                            showBubble(positionedBubble.bubble)
                        },
                    )
                }
            }

        }


        FloatingActionButton(
            onClick = onShowKeywordMap,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .size(64.dp),
            shape = CircleShape,
            containerColor = Gray100
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_orbit_dark),
                contentDescription = "키워드로 매핑",
                tint = Color.Unspecified
            )
        }

    }



}

private fun DrawScope.drawGradientBlurCircle(
    center: Offset,
    radius: Float,
    colors: List<Color>,
    blurRadius: Float,
) {
    drawIntoCanvas { canvas ->
        val alphaRatio = 0.4f
        val paint = Paint().apply {
            isAntiAlias = true
            when (colors.size) {
                0 -> {
                    color = Gray300.copy(alpha = alphaRatio).toArgb()
                }

                1 -> {
                    // 단일 색상
                    color = colors.first().copy(alpha = alphaRatio).toArgb()
                }

                else ->
                    shader = LinearGradient(
                        center.x,
                        center.y - radius,
                        center.x,
                        center.y + radius,
                        colors.map { it.copy(alpha = alphaRatio).toArgb() }.toIntArray(),
                        null,
                        Shader.TileMode.CLAMP
                    )
            }
            maskFilter = BlurMaskFilter(
                blurRadius,
                BlurMaskFilter.Blur.NORMAL
            )
        }

        canvas.nativeCanvas.drawCircle(center.x, center.y, radius, paint)
    }
}

private fun DrawScope.drawOverlappingBlurCircles(
    center: Offset,
    bigRadius: Float,
    colors: List<Color>,
    blurRadius: Float,
) {
    // 작은 원의 반지름과 중심으로부터의 거리 (비율은 디자인에 맞게 조정 가능)
    val smallCircleRadius = bigRadius * 0.6f
    val distanceFromCenter = bigRadius * 0.5f
    val count = colors.size
    for (i in 0 until count) {
        val angleDegrees = i * (360f / count)
        val angleRadians = Math.toRadians(angleDegrees.toDouble())
        val dx = (distanceFromCenter * cos(angleRadians)).toFloat()
        val dy = (distanceFromCenter * sin(angleRadians)).toFloat()
        val smallCenter = center + Offset(dx, dy)

        // 각 작은 원은 단일 색상으로 drawGradientBlurCircle을 호출
        drawGradientBlurCircle(
            center = smallCenter,
            radius = smallCircleRadius,
            colors = listOf(colors[i]),
            blurRadius = blurRadius
        )
    }
}

