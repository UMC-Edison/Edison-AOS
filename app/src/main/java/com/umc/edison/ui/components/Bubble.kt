package com.umc.edison.ui.components

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.umc.edison.presentation.model.BubbleModel
import com.umc.edison.presentation.model.LabelModel
import com.umc.edison.ui.theme.Aqua100
import com.umc.edison.ui.theme.EdisonTheme
import com.umc.edison.ui.theme.Gray100
import com.umc.edison.ui.theme.Gray300
import com.umc.edison.ui.theme.Gray500
import com.umc.edison.ui.theme.Gray800
import com.umc.edison.ui.theme.Pretendard
import com.umc.edison.ui.theme.Red100
import com.umc.edison.ui.theme.White000
import com.umc.edison.ui.theme.Yellow100
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun BubbleInput(
    onClick: () -> Unit
) {
    val bubbleSize = BubbleType.BubbleMain
    val canvasSize = bubbleSize.size

    Box(
        modifier = Modifier
            .size(canvasSize)
            .clip(CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        SingleBubble(bubbleSize = bubbleSize, color = Gray300)

        Box(
            modifier = Modifier.size(
                bubbleSize.textBoxSize.first.dp,
                bubbleSize.textBoxSize.second.dp
            ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "버블을 입력해주세요.",
                style = bubbleSize.fontStyle,
                color = Gray500,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun Bubble(
    bubble: BubbleModel,
    onClick: () -> Unit, // 편집 모드로 들어가는 클릭 리스너
) {
    val bubbleSize = calculateBubbleSize(bubble)

    if (bubble.images.isNotEmpty() && bubbleSize == BubbleType.BubbleDoor) {
        BubbleDoor(
            bubble = bubble,
            isEditable = false,
            onClick = onClick
        )
    } else {
        TextContentBubble(
            bubble = bubble,
            colors = bubble.labels.map { it.color },
            onClick = onClick,
            bubbleSize = BubbleType.BubbleMain
        )
    }
}

@Composable
fun BubblePreview(
    onClick: () -> Unit,
    bubble: BubbleModel
) {
    val bubbleSize = calculateBubbleSize(bubble)

    if (bubble.title != null || bubble.content != null) {
        TextContentBubble(
            bubble = bubble,
            colors = bubble.labels.map { it.color },
            onClick = onClick,
            bubbleSize = bubbleSize
        )
    } else {
        // TODO: 이미지 1개를 배경으로 하는 버블 컴포저블
        ImageBubble(bubbleSize = bubbleSize, imageUrl = bubble.mainImage ?: bubble.images[0])
    }
}

@Composable
fun BubbleDoor(
    bubble: BubbleModel,
    isEditable: Boolean = false,
    onClick: () -> Unit,
) {
    // TODO: 버블 문 형태 개발 필요
    when (bubble.labels.size) {
    // 0 -> // TODO: color에 해당하는 door 컴포저블
    // 1 -> // TODO: color에 해당하는 door 컴포저블
    // 2 -> // TODO: color 2개에 해당하는 door 컴포저블
    // 3 -> // TODO: color 3개에 해당하는 door 컴포저블
    }

    // 제목, 본문, 이미지 그려지는...? -> 보류
}

@Composable
private fun TextContentBubble(
    bubble: BubbleModel,
    colors: List<Color>,
    onClick: () -> Unit,
    bubbleSize: BubbleType.BubbleSize,
) {
    val canvasSize = bubbleSize.size

    Box(
        modifier = Modifier
            .size(canvasSize)
            .clip(CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (bubble.mainImage != null) {
            ImageBubble(bubbleSize = bubbleSize, imageUrl = bubble.mainImage)
        } else {
            when (colors.size) {
                0 -> SingleBubble(bubbleSize = bubbleSize, color = Gray100)
                1 -> SingleBubble(bubbleSize = bubbleSize, color = colors[0])
                2 -> DoubleBubble(bubbleSize = bubbleSize, colors = colors)
                3 -> TripleBubble(bubbleSize = bubbleSize, colors = colors)
            }
        }

        Box(
            modifier = Modifier.size(
                bubbleSize.textBoxSize.first.dp,
                bubbleSize.textBoxSize.second.dp
            ),
            contentAlignment = Alignment.Center
        ) {
            val text = if (bubbleSize == BubbleType.BubbleMain) bubble.content ?: bubble.title!!
            else bubble.title ?: bubble.content!!

            Text(
                text = text,
                style = bubbleSize.fontStyle,
                color = Gray800,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ImageBubble(
    bubbleSize: BubbleType.BubbleSize,
    imageUrl: String,
) {
    // TODO: 이미지 관련 정리되면 개발
}

@Composable
private fun SingleBubble(
    bubbleSize: BubbleType.BubbleSize,
    color: Color
) {
    Canvas(modifier = Modifier.size(bubbleSize.size)) {
        // Layer Blur 효과의 반지름
        val blurRadius = 60f
        val outerRadius = bubbleSize.size.toPx() / 2
        val innerRadius = bubbleSize.innerSize.toPx() / 2

        // 큰 원 그리기
        drawCircle(color = color, radius = outerRadius, center = center)

        // LayerBlur를 사용한 흰색 원
        drawLayerBlur(
            center = center,
            radius = innerRadius,
            colors = listOf(color),
            blurRadius = blurRadius,
        )
    }
}

@Composable
fun DoubleBubble(
    bubbleSize: BubbleType.BubbleSize,
    colors: List<Color>,
) {
    require(colors.size == 2) { "DoubleBubble requires exactly two colors." }

    Canvas(modifier = Modifier.size(bubbleSize.size)) {
        // Layer Blur 효과의 반지름
        val blurRadius = 60f
        val outerRadius = bubbleSize.size.toPx() / 2
        val innerRadius = bubbleSize.innerSize.toPx() / 2

        // 큰 원 그리기
        drawOuterGradientCircle(
            center = center,
            radius = outerRadius,
            colors = colors
        )

        // 내부 원 그리기
        drawLayerBlur(
            center = center,
            radius = innerRadius,
            colors = colors,
            blurRadius = blurRadius
        )
    }
}

@Composable
private fun TripleBubble(
    bubbleSize: BubbleType.BubbleSize,
    colors: List<Color>,
) {
    Canvas(modifier = Modifier.size(bubbleSize.size)) {

        // Layer Blur 효과의 반지름
        val blurRadius = 60f
        val outerRadius = bubbleSize.size.toPx() / 2
        val innerRadius = bubbleSize.innerSize.toPx() / 2

        // 큰 원 그리기
        drawOuterGradientCircle(
            center = center,
            radius = outerRadius,
            colors = colors
        )

        // 2번째 레이어 블러 그리기
        drawLayerBlur(
            center = center,
            radius = innerRadius,
            colors = colors,
            blurRadius = blurRadius
        )

        // 내부 원 그리기
        clipPath(Path().apply {
            addOval(
                Rect(
                    center - Offset(outerRadius * 0.9f, outerRadius * 0.9f),
                    center + Offset(outerRadius, outerRadius),
                )
            )
        }) {
            val radius = innerRadius * 0.55f
            val blur = 200f
            val offsets = listOf(
                Offset(center.x + radius * 1.3f, center.y - radius * 0.2f),
                Offset(center.x - radius * 0.2f, center.y - radius * 0.2f),
                Offset(center.x + radius * 0.55f, center.y + radius * 1.55f)
            )

            drawCircleWithBlur(colors[1], offsets[1], radius, blur)
            drawCircleWithBlur(colors[0], offsets[0], radius, blur)
            drawCircleWithBlur(colors[2], offsets[2], radius, blur)
        }
    }
}

/**
 * 버블 텍스트 길이에 따른 사이즈 계산 함수
 */
private fun calculateBubbleSize(bubble: BubbleModel): BubbleType.BubbleSize {
    val text = bubble.title ?: bubble.content ?: ""

    fun calculateLineCount(text: String, textBoxWidthDp: Int, fontSizeSp: Float): Int {
        val charPerLine = (textBoxWidthDp / (fontSizeSp * 0.57)).toInt()

        val lines = text.split("\n").sumOf { line ->
            val lineLength = line.length
            val lineCount = lineLength / charPerLine
            if (lineLength % charPerLine == 0) lineCount else lineCount + 1
        }

        return lines
    }

    listOf(
        BubbleType.Bubble100,
        BubbleType.Bubble160,
        BubbleType.Bubble230,
        BubbleType.Bubble300,
        BubbleType.BubbleMain
    ).forEach { bubbleType ->
        val (textBoxWidth, _) = bubbleType.textBoxSize
        val fontSizeSp = bubbleType.fontStyle.fontSize.value
        val lineCount = calculateLineCount(text, textBoxWidth, fontSizeSp)
        Log.d("BubbleDebug", "Text: $text, Calculated Line Count: $lineCount")

        when {
            text.length <= 5 && bubbleType == BubbleType.Bubble100 -> return BubbleType.Bubble100
            lineCount <= 2 && bubbleType == BubbleType.Bubble160 -> return BubbleType.Bubble160
            lineCount <= 3 && bubbleType == BubbleType.Bubble230 -> return BubbleType.Bubble230
            lineCount <= 4 && bubbleType == BubbleType.Bubble300 -> return BubbleType.Bubble300
            lineCount <= 6 && bubbleType == BubbleType.BubbleMain -> return BubbleType.BubbleMain
        }
    }

    return BubbleType.Bubble300
}

/**
 * 가장 바깥 원에 gradient가 적용된 원
 */
private fun DrawScope.drawOuterGradientCircle(center: Offset, radius: Float, colors: List<Color>) {
    val angleRadians = Math.toRadians(30.0)
    val startX = center.x - radius * cos(angleRadians).toFloat()
    val startY = center.y - radius * sin(angleRadians).toFloat()
    val endX = center.x + radius * cos(angleRadians).toFloat()
    val endY = center.y + radius * sin(angleRadians).toFloat()

    val gradient = Brush.linearGradient(
        colors = colors,
        start = Offset(startX, startY),
        end = Offset(endX, endY)
    )
    drawCircle(
        brush = gradient,
        radius = radius,
        center = center
    )
}

/**
 * 2번째 레이어에 해당하는 블러 효과가 적용된 원
 */
private fun DrawScope.drawLayerBlur(
    center: Offset,
    radius: Float,
    colors: List<Color>,
    blurRadius: Float
) {
    drawIntoCanvas { canvas ->
        val paint = Paint().apply {
            this.asFrameworkPaint().apply {
                isAntiAlias = true
                when (colors.size) {
                    1 -> {
                        // 단일 색상
                        shader = android.graphics.LinearGradient(
                            center.x - radius,
                            center.y,
                            center.x + radius,
                            center.y,
                            intArrayOf(White000.toArgb(), colors[0].toArgb()),
                            floatArrayOf(0f, 1f),
                            android.graphics.Shader.TileMode.CLAMP
                        )
                    }

                    2 -> {
                        // 두 개의 색상 - 대각선 방향 Gradient
                        val angleRadians = Math.toRadians(30.0)
                        val startX = center.x - radius * cos(angleRadians).toFloat()
                        val startY = center.y - radius * sin(angleRadians).toFloat()
                        val endX = center.x + radius * cos(angleRadians).toFloat()
                        val endY = center.y + radius * sin(angleRadians).toFloat()
                        shader = android.graphics.LinearGradient(
                            startX, startY,
                            endX, endY,
                            intArrayOf(
                                White000.toArgb(),
                                colors[1].toArgb(),
                                colors[0].toArgb()
                            ),
                            floatArrayOf(0f, 0.46f, 1f),
                            android.graphics.Shader.TileMode.CLAMP
                        )
                    }

                    else -> {
                        // 기본 - 흰색 블러 처리만
                        color = android.graphics.Color.WHITE
                    }
                }
                // 블러 효과 추가
                maskFilter = android.graphics.BlurMaskFilter(
                    blurRadius,
                    android.graphics.BlurMaskFilter.Blur.NORMAL
                )
            }
        }
        canvas.drawCircle(center, radius, paint)
    }
}

/**
 * 라벨 3개 달리는 버블에서 내부 원들에 블러 처리된 원
 */
private fun DrawScope.drawCircleWithBlur(
    color: Color,
    center: Offset,
    radius: Float,
    blurRadius: Float
) {
    drawIntoCanvas { canvas ->
        val paint = Paint().apply {
            this.color = color
            this.asFrameworkPaint().apply {
                isAntiAlias = true
                maskFilter = android.graphics.BlurMaskFilter(
                    blurRadius,
                    android.graphics.BlurMaskFilter.Blur.NORMAL
                )
            }
        }
        canvas.drawCircle(center, radius, paint)
    }
}


object BubbleType {
    // TODO: 디자인 시스템 참고해서 수정
    val BubbleDoor = BubbleSize(
        size = 364.dp,
        innerSize = 326.dp,
        fontStyle = TextStyle(
            fontFamily = Pretendard,
            fontWeight = FontWeight.Medium,
            fontSize = 20.sp,
            lineHeight = 24.sp
        ), // headingSmall
        textBoxSize = Pair(258, 144)
    )

    val BubbleMain = BubbleSize(
        size = 364.dp,
        innerSize = 326.dp,
        fontStyle = TextStyle(
            fontFamily = Pretendard,
            fontWeight = FontWeight.Medium,
            fontSize = 20.sp,
            lineHeight = 24.sp
        ), // headingSmall
        textBoxSize = Pair(258, 144)
    )

    val Bubble300 = BubbleSize(
        size = 300.dp,
        innerSize = 270.dp,
        fontStyle = TextStyle(
            fontFamily = Pretendard,
            fontWeight = FontWeight.Medium,
            fontSize = 18.sp,
            lineHeight = 24.sp
        ), // titleLarge
        textBoxSize = Pair(181, 96)
    )

    val Bubble230 = BubbleSize(
        size = 230.dp,
        innerSize = 206.dp,
        fontStyle = TextStyle(
            fontFamily = Pretendard,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            lineHeight = 20.sp
        ), // titleMedium
        textBoxSize = Pair(146, 57)
    )

    val Bubble160 = BubbleSize(
        size = 160.dp,
        innerSize = 144.dp,
        fontStyle = TextStyle(
            fontFamily = Pretendard,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 18.sp
        ), // titleSmall
        textBoxSize = Pair(80, 36)
    )

    val Bubble100 = BubbleSize(
        size = 100.dp,
        innerSize = 90.dp,
        fontStyle = TextStyle(
            fontFamily = Pretendard,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 18.sp
        ), // titleSmall
        textBoxSize = Pair(61, 17)
    )

    data class BubbleSize(
        val size: Dp,
        val innerSize: Dp,
        val fontStyle: TextStyle,
        val textBoxSize: Pair<Int, Int>,
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewBubbleInput() {
    EdisonTheme {
        BubbleInput(onClick = { })
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSingleBubble() {
    EdisonTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            BubblePreview(
                onClick = {},
                bubble = BubbleModel(
                    title = "5글자끼지",
                    content = "버블 내용",
                    images = listOf(),
                    labels = listOf(
                        LabelModel(0, "Label", Aqua100),
                    ),
                    date = "2021-09-01"
                )
            )
            BubblePreview(
                onClick = {},
                bubble = BubbleModel(
                    title = "14pt 2줄 가능\n160 버블크기",
                    content = "버블 내용",
                    images = listOf(),
                    labels = listOf(
                        LabelModel(0, "Label", Aqua100)
                    ),
                    date = "2021-09-01"
                )
            )
            BubblePreview(
                onClick = {},
                bubble = BubbleModel(
                    title = "제목은  bold 서체 입니다. 3줄까지 가능합니다. 2번째로 큰 버블",
                    content = "버블 내용",
                    images = listOf(),
                    labels = listOf(
                        LabelModel(0, "Label", Aqua100),
                    ),
                    date = "2021-09-01"
                )
            )
            BubblePreview(
                onClick = {},
                bubble = BubbleModel(
                    title = "나의 에디슨 페이지 가장 큰 버블입니다. 제목은 bold 서체 입니다. 최대 4줄까지 가능합니다.",
                    content = "버블 내용",
                    images = listOf(),
                    labels = listOf(
                        LabelModel(0, "Label", Aqua100),
                    ),
                    date = "2021-09-01"
                )
            )
            BubblePreview(
                onClick = {},
                bubble = BubbleModel(
                    title = "메인 버블 2가지에 사용됩니다. 버블 입력 초기화면에 사용됩니다. 또, 해당 버블 클릭 시에 세부 내용 확인할 수 있는 버블입니다. 메인버블을 364px 크기입니다.",
                    content = "버블 내용",
                    images = listOf(),
                    labels = listOf(
                        LabelModel(0, "Label", Aqua100),
                    ),
                    date = "2021-09-01"
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDoubleBubble() {
    EdisonTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            BubblePreview(
                onClick = {},
                bubble = BubbleModel(
                    title = "5글자끼지",
                    content = "버블 내용",
                    images = listOf(),
                    labels = listOf(
                        LabelModel(0, "Label", Aqua100),
                        LabelModel(1, "Label", Yellow100),
                    ),
                    date = "2021-09-01"
                )
            )
            BubblePreview(
                onClick = {},
                bubble = BubbleModel(
                    title = "14pt 2줄 가능\n160 버블크기",
                    content = "버블 내용",
                    images = listOf(),
                    labels = listOf(
                        LabelModel(0, "Label", Aqua100),
                        LabelModel(1, "Label", Yellow100),
                    ),
                    date = "2021-09-01"
                )
            )
            BubblePreview(
                onClick = {},
                bubble = BubbleModel(
                    title = "제목은  bold 서체 입니다. 3줄까지 가능합니다. 2번째로 큰 버블",
                    content = "버블 내용",
                    images = listOf(),
                    labels = listOf(
                        LabelModel(0, "Label", Aqua100),
                        LabelModel(1, "Label", Yellow100),
                    ),
                    date = "2021-09-01"
                )
            )
            BubblePreview(
                onClick = {},
                bubble = BubbleModel(
                    title = "나의 에디슨 페이지 가장 큰 버블입니다. 제목은 bold 서체 입니다. 최대 4줄까지 가능합니다.",
                    content = "버블 내용",
                    images = listOf(),
                    labels = listOf(
                        LabelModel(0, "Label", Aqua100),
                        LabelModel(1, "Label", Yellow100),
                    ),
                    date = "2021-09-01"
                )
            )
            BubblePreview(
                onClick = {},
                bubble = BubbleModel(
                    title = "메인 버블 2가지에 사용됩니다. 버블 입력 초기화면에 사용됩니다. 또, 해당 버블 클릭 시에 세부 내용 확인할 수 있는 버블입니다. 메인버블을 364px 크기입니다.",
                    content = "버블 내용",
                    images = listOf(),
                    labels = listOf(
                        LabelModel(0, "Label", Aqua100),
                        LabelModel(1, "Label", Yellow100),
                    ),
                    date = "2021-09-01"
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTripleBubble() {
    EdisonTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            BubblePreview(
                onClick = {},
                bubble = BubbleModel(
                    title = "5글자끼지",
                    content = "버블 내용",
                    images = listOf(),
                    labels = listOf(
                        LabelModel(0, "Label", Aqua100),
                        LabelModel(1, "Label", Yellow100),
                        LabelModel(2, "Label", Red100),
                    ),
                    date = "2021-09-01"
                )
            )
            BubblePreview(
                onClick = {},
                bubble = BubbleModel(
                    title = "14pt 2줄 가능\n160 버블크기",
                    content = "버블 내용",
                    images = listOf(),
                    labels = listOf(
                        LabelModel(0, "Label", Aqua100),
                        LabelModel(1, "Label", Yellow100),
                        LabelModel(2, "Label", Red100),
                    ),
                    date = "2021-09-01"
                )
            )
            BubblePreview(
                onClick = {},
                bubble = BubbleModel(
                    title = "제목은  bold 서체 입니다. 3줄까지 가능합니다. 2번째로 큰 버블",
                    content = "버블 내용",
                    images = listOf(),
                    labels = listOf(
                        LabelModel(0, "Label", Aqua100),
                        LabelModel(1, "Label", Yellow100),
                        LabelModel(2, "Label", Red100),
                    ),
                    date = "2021-09-01"
                )
            )
            BubblePreview(
                onClick = {},
                bubble = BubbleModel(
                    title = "나의 에디슨 페이지 가장 큰 버블입니다. 제목은 bold 서체 입니다. 최대 4줄까지 가능합니다.",
                    content = "버블 내용",
                    images = listOf(),
                    labels = listOf(
                        LabelModel(0, "Label", Aqua100),
                        LabelModel(1, "Label", Yellow100),
                        LabelModel(2, "Label", Red100),
                    ),
                    date = "2021-09-01"
                )
            )
            BubblePreview(
                onClick = {},
                bubble = BubbleModel(
                    title = "메인 버블 2가지에 사용됩니다. 버블 입력 초기화면에 사용됩니다. 또, 해당 버블 클릭 시에 세부 내용 확인할 수 있는 버블입니다. 메인버블을 364px 크기입니다.",
                    content = "버블 내용",
                    images = listOf(),
                    labels = listOf(
                        LabelModel(0, "Label", Aqua100),
                        LabelModel(1, "Label", Yellow100),
                        LabelModel(2, "Label", Red100),
                    ),
                    date = "2021-09-01"
                )
            )
        }
    }
}
