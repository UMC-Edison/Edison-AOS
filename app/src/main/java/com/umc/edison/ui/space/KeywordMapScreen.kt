package com.umc.edison.ui.space

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.umc.edison.presentation.space.KeywordViewModel
import com.umc.edison.ui.theme.Gray300
import com.umc.edison.ui.theme.Gray800
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun KeywordMapScreen(viewModel: KeywordViewModel = hiltViewModel(),keyword: String?) {
    val rotationForCircle2 = remember { Animatable(0f) }
    val rotationForCircle3 = remember { Animatable(0f) }
    val rotationForCircle4 = remember { Animatable(0f) }
    val rotationForCircle5 = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        val duration = 3000

        launch {
            rotationForCircle2.animateTo(
                targetValue = (Random.nextFloat() * 2 - 1) * 360f,
                animationSpec = tween(durationMillis = duration)
            )
        }
        launch {
            rotationForCircle3.animateTo(
                targetValue = (Random.nextFloat() * 2 - 1) * 360f,
                animationSpec = tween(durationMillis = duration)
            )
        }
        launch {
            rotationForCircle4.animateTo(
                targetValue = (Random.nextFloat() * 2 - 1) * 360f,
                animationSpec = tween(durationMillis = duration)
            )
        }
        launch {
            rotationForCircle5.animateTo(
                targetValue = (Random.nextFloat() * 2 - 1) * 360f,
                animationSpec = tween(durationMillis = duration)
            )
        }
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val baseSize = min(maxWidth.value, maxHeight.value) * 0.9f

        val originalDiameters = listOf(530, 434, 330, 234, 100)
        val maxOriginalDiameter = 530f

        val edgeColor = Gray300
        val centerColor = Color.White

        originalDiameters.forEach { diameter ->
            val responsiveDiameter = (baseSize * (diameter / maxOriginalDiameter)).dp
            Circle(diameter = responsiveDiameter, edgeColor = edgeColor, centerColor = centerColor)
        }

        VerticesOverlay(
            baseSize, maxOriginalDiameter, 234f,
            baseAngles = listOf(-90f, 30f, 150f),
            rotationAngleDegrees = rotationForCircle2.value + 45f,
            vertexSize = 36.dp
        )
        VerticesOverlay(
            baseSize, maxOriginalDiameter, 330f,
            baseAngles = listOf(-90f, 30f, 150f),
            rotationAngleDegrees = rotationForCircle3.value + 30f,
            vertexSize = 50.dp
        )
        VerticesOverlay(
            baseSize, maxOriginalDiameter, 434f,
            baseAngles = listOf(240f),
            rotationAngleDegrees = rotationForCircle4.value,
            vertexSize = 64.dp
        )
        VerticesOverlay(
            baseSize, maxOriginalDiameter, 530f,
            baseAngles = listOf(75f, 110f, 300f),
            rotationAngleDegrees = rotationForCircle5.value,
            vertexSize = 64.dp
        )

        if (!keyword.isNullOrBlank()) {
            Text(
                text = keyword,
                style = MaterialTheme.typography.headlineLarge,
                color = Gray800
            )
        }
    }
}

@Composable
private fun VerticesOverlay(
    baseSize: Float,
    maxOriginalDiameter: Float,
    targetDiameter: Float,
    baseAngles: List<Float>,
    rotationAngleDegrees: Float = 0f,
    vertexSize: Dp
) {
    val targetResponsiveDiameter = baseSize * (targetDiameter / maxOriginalDiameter)
    val radiusInPx = targetResponsiveDiameter / 2f

    baseAngles.forEach { baseAngle ->
        val finalAngleDegrees = baseAngle + rotationAngleDegrees
        val finalAngleRadians = Math.toRadians(finalAngleDegrees.toDouble())

        val x = radiusInPx * cos(finalAngleRadians).toFloat()
        val y = radiusInPx * sin(finalAngleRadians).toFloat()

        VertexCircle(
            modifier = Modifier.offset(x = x.dp, y = y.dp),
            size = vertexSize
        )
    }
}

@Composable
private fun Circle(diameter: Dp, edgeColor: Color, centerColor: Color) {
    val radialGradientBrush = Brush.radialGradient(colors = listOf(centerColor, edgeColor))
    Box(
        modifier = Modifier
            .size(diameter)
            .clip(CircleShape)
            .background(brush = radialGradientBrush)
    )
}


@Composable
private fun VertexCircle(
    modifier: Modifier = Modifier,
    size: Dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.DarkGray)
    )
}