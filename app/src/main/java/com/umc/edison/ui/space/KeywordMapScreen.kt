package com.umc.edison.ui.space

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.umc.edison.presentation.model.BubbleModel
import com.umc.edison.presentation.model.KeywordBubbleModel
import com.umc.edison.presentation.model.getDisplayTitle
import com.umc.edison.presentation.space.KeywordViewModel
import com.umc.edison.ui.components.BubblePreview
import com.umc.edison.ui.components.BubbleType
import com.umc.edison.ui.theme.Gray100
import com.umc.edison.ui.theme.Gray400
import com.umc.edison.ui.theme.Gray800
import com.umc.edison.ui.theme.Gray900
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random


@Composable
fun KeywordMapScreen(
    keyword: String?,
    showBubble: (BubbleModel) -> Unit,
    viewModel: KeywordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val rotationForCircle2 = remember { Animatable(0f) }
    val rotationForCircle3 = remember { Animatable(0f) }
    val rotationForCircle4 = remember { Animatable(0f) }
    val rotationForCircle5 = remember { Animatable(0f) }

    LaunchedEffect(keyword) {
        if (!keyword.isNullOrBlank()) {
            viewModel.fetchKeywordBubbles(keyword)
        }
    }

    LaunchedEffect(Unit) {
        val duration = 3000

        launch {
            rotationForCircle2.animateTo(
                targetValue = Random.nextFloat() * 40f,
                animationSpec = tween(durationMillis = duration)
            )
        }

        launch {
            rotationForCircle3.animateTo(
                targetValue = -(Random.nextFloat() * 40f),
                animationSpec = tween(durationMillis = duration)
            )
        }

        launch {
            val baseAngle4 = 240f
            val selectedZone = Random.nextInt(3)


            val safeFinalAngle = when (selectedZone) {
                0 -> {
                    50.1f + Random.nextFloat() * 29.8f
                }

                1 -> {
                    170.1f + Random.nextFloat() * 29.8f
                }

                else -> {
                    +290.1f + Random.nextFloat() * 29.8f
                }
            }

            val targetValue = safeFinalAngle - baseAngle4

            rotationForCircle4.animateTo(
                targetValue = targetValue,
                animationSpec = tween(durationMillis = duration)
            )
        }

        launch {
            val selectedZone = Random.nextInt(3)


            val targetValue = when (selectedZone) {
                0 -> {
                    +40.1f + Random.nextFloat() * 49.8f
                }

                1 -> {
                    160.1f + Random.nextFloat() * 49.8f
                }

                else -> {
                    280.1f + Random.nextFloat() * 49.8f
                }
            }

            rotationForCircle5.animateTo(
                targetValue = targetValue,
                animationSpec = tween(durationMillis = duration)
            )
        }
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val baseSize = min(maxWidth.value, maxHeight.value) * 0.85f
        val originalDiameters = listOf(530, 434, 330, 234, 100)
        val maxOriginalDiameter = 530f
        val edgeColors = listOf(
            Color(0xFFFCFDFE),
            Color(0xFFF9FBFC),
            Color(0xFFF7F8FB),
            Color(0xFFF4F6F9),
            Gray100
        )
        val centerColor = Color.White

        originalDiameters.forEachIndexed { index, diameter ->
            val responsiveDiameter = (baseSize * (diameter / maxOriginalDiameter)).dp
            val (elevation, shadowColor) = if (diameter == 100) {
                30.dp to Gray400.copy(alpha = 0.5f)
            } else {
                0.dp to Color.Transparent
            }

            val borderModifier = if (index == 0) {
                Modifier.border(1.dp, Gray100, CircleShape)
            } else {
                Modifier
            }

            Circle(
                modifier = borderModifier,
                diameter = responsiveDiameter,
                edgeColor = edgeColors[index],
                centerColor = centerColor,
                shadowElevation = elevation,
                shadowColor = shadowColor
            )
        }

        if (!keyword.isNullOrBlank()) {
            Text(
                text = keyword,
                style = MaterialTheme.typography.headlineLarge,
                color = Gray800
            )
        }

        val bubbles = uiState.bubbles
        val slots = remember {
            listOf(
                BubbleSlot(234f, -90f, 10f, 36.dp, rotationForCircle2),
                BubbleSlot(234f, 30f, 10f, 36.dp, rotationForCircle2),
                BubbleSlot(234f, 150f, 10f, 36.dp, rotationForCircle2),
                BubbleSlot(330f, -90f, 0f, 50.dp, rotationForCircle3),
                BubbleSlot(330f, 30f, 0f, 50.dp, rotationForCircle3),
                BubbleSlot(330f, 150f, 0f, 50.dp, rotationForCircle3),
                BubbleSlot(434f, 0f, 0f, 64.dp, rotationForCircle4),
                BubbleSlot(530f, -60f, 0f, 64.dp, rotationForCircle5),
                BubbleSlot(530f, 60f, 0f, 64.dp, rotationForCircle5),
                BubbleSlot(530f, 180f, 0f, 64.dp, rotationForCircle5)
            )
        }

        bubbles.zip(slots).forEachIndexed { index, (bubble, slot) ->
            val bubbleColors = bubble.bubble.labels.map { it.color }

            PositionedBubble(
                baseSize = baseSize,
                maxOriginalDiameter = maxOriginalDiameter,
                slot = slot,
                bubble = bubble,
                rank = index + 1,
                colors = bubbleColors,
                onClick = { viewModel.selectBubble(bubble) }
            )
        }




        uiState.selectedBubble?.let { selected ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        onClick = { viewModel.dismissBubble() },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.clickable(
                        onClick = {},
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() })
                ) {
                    BubblePreview(
                        bubble = selected.bubble,
                        size = BubbleType.Bubble300,
                        onClick = {
                            showBubble(selected.bubble)
                        }
                    )
                }
            }
        }
    }
}


@Composable
private fun PositionedBubble(
    baseSize: Float,
    maxOriginalDiameter: Float,
    slot: BubbleSlot,
    bubble: KeywordBubbleModel,
    rank: Int,
    colors: List<Color>,
    onClick: () -> Unit
) {
    val finalAngle = slot.baseAngle + slot.initialRotation + slot.rotationAnimatable.value
    val targetResponsiveDiameter = baseSize * (slot.targetDiameter / maxOriginalDiameter)
    val radiusInPx = targetResponsiveDiameter / 2f
    val angleRadians = Math.toRadians(finalAngle.toDouble())
    val x = radiusInPx * cos(angleRadians).toFloat()
    val y = radiusInPx * sin(angleRadians).toFloat()

    Column(
        modifier = Modifier
            .offset(x = x.dp, y = y.dp)
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {


        Box(contentAlignment = Alignment.Center) {
            BackgroundBubble(size = slot.size, colors = colors)

            RankCircle(
                rank = rank,
                baseColor = colors.firstOrNull() ?: Gray400
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = bubble.bubble.getDisplayTitle(),
            color = Gray800,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(slot.size * 1.5f)
        )
    }
}

@Composable
private fun BackgroundBubble(size: Dp, colors: List<Color>) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .then(
                when {
                    colors.isEmpty() -> Modifier.background(Gray400)
                    colors.size == 1 -> Modifier.background(colors.first())
                    else -> Modifier.background(Brush.verticalGradient(colors = colors))
                }
            )
    )
}

@Composable
private fun RankCircle(rank: Int, baseColor: Color) {
    val lightenedColor = lerp(baseColor, Color.White, 0.8f)

    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(lightenedColor),
        contentAlignment = Alignment.Center
    ) {
        val textStyle = MaterialTheme.typography.titleMedium.copy(
            color = Gray900,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = rank.toString(),
            style = textStyle
        )
    }
}

@Composable
private fun Circle(
    modifier: Modifier = Modifier,
    diameter: Dp,
    edgeColor: Color,
    centerColor: Color,
    shadowElevation: Dp = 0.dp,
    shadowColor: Color = Color.Black
) {
    val radialGradientBrush = Brush.radialGradient(
        colorStops = arrayOf(
            0.85f to centerColor,
            1.0f to edgeColor
        )
    )
    Box(
        modifier = modifier
            .size(diameter)
            .shadow(
                elevation = shadowElevation,
                shape = CircleShape,
                clip = false,
                ambientColor = shadowColor,
            )
            .clip(CircleShape)
            .background(brush = radialGradientBrush)
    )
}


private data class BubbleSlot(
    val targetDiameter: Float,
    val baseAngle: Float,
    val initialRotation: Float,
    val size: Dp,
    val rotationAnimatable: Animatable<Float, *>
)