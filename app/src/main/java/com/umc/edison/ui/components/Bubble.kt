package com.umc.edison.ui.components

import android.graphics.BlurMaskFilter
import android.graphics.LinearGradient
import android.graphics.Shader
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mohamedrejeb.richeditor.annotation.ExperimentalRichTextApi
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichText
import com.umc.edison.presentation.edison.util.parseHtml
import com.umc.edison.presentation.model.BubbleModel
import com.umc.edison.presentation.model.ContentType
import com.umc.edison.ui.theme.EdisonTypography
import com.umc.edison.ui.theme.Gray100
import com.umc.edison.ui.theme.Gray200
import com.umc.edison.ui.theme.Gray300
import com.umc.edison.ui.theme.Gray500
import com.umc.edison.ui.theme.Gray700
import com.umc.edison.ui.theme.Gray800
import com.umc.edison.ui.theme.White000
import kotlin.math.cos
import kotlin.math.sin

/**
 * 마이 에디슨 메인 화면의 버블 입력 컴포저블
 */
@Composable
fun BubbleInput(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isBlur: Boolean = false,
    onBackScreenClick: () -> Unit = {}
) {
    val bubbleSize = BubbleType.BubbleMain
    val canvasSize = bubbleSize.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = if (isBlur) Gray800.copy(alpha = 0.5f) else White000
            )
            .clickable(
                onClick = onBackScreenClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {


        Spacer(modifier = Modifier.height(48.dp))

        Box(
            modifier = modifier
                .size(canvasSize)
                .clip(CircleShape)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            SingleBubble(bubbleSize = bubbleSize, color = Gray300)

            Box(
                modifier = Modifier
                    .width(bubbleSize.textBoxWidth)
                    .wrapContentHeight(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "버블을 입력해주세요.",
                    style = bubbleSize.bodyFontStyle,
                    color = Gray500,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * 버블 전체 내용 확인 가능한 컴포저블
 */
@Composable
fun Bubble(
    bubble: BubbleModel,
    onBubbleClick: () -> Unit,
    onLinkedBubbleClick: (String) -> Unit,
) {
    val bubbleSize = calculateBubbleSize(bubble)

    if (checkBubbleContainImage(bubble) || bubbleSize == BubbleType.BubbleDoor) {
        BubbleDoor(
            bubble = bubble,
            isEditable = false,
            onClick = onBubbleClick,
            onLinkClick = onLinkedBubbleClick,
        )
    } else {
        TextBubble(
            bubble = bubble,
            colors = bubble.labels.map { it.color },
            onClick = onBubbleClick,
            bubbleSize = BubbleType.BubbleMain,
            isPreview = false
        )
    }
}

/**
 * 버블 보관함 화면에서 사용되는 버블 미리보기 컴포저블
 */
@Composable
fun BubblePreview(
    onClick: () -> Unit,
    size: BubbleType.BubbleSize,
    bubble: BubbleModel,
    modifier: Modifier = Modifier,
    onLongClick: () -> Unit = {},
    searchKeyword: String = "",
) {
    if (bubble.mainImage != null) {
        ImageBubble(
            bubble = bubble,
            bubbleSize = size,
            imageUrl = bubble.mainImage,
            onClick = onClick,
            onLongClick = onLongClick,
            isPreview = true,
            modifier = modifier
        )
    } else if (bubble.title != null || checkBubbleContainText(bubble)) {
        // 제목이 있거나 본문에 텍스트가 있는 경우
        TextBubble(
            bubble = bubble,
            colors = bubble.labels.map { it.color },
            onClick = onClick,
            onLongClick = onLongClick,
            bubbleSize = size,
            isPreview = true,
            searchKeyword = searchKeyword,
            modifier = modifier
        )
    } else {
        // 그 외의 경우 - 본문 이미지만 있는 경우
        ImageBubble(
            bubble = bubble,
            bubbleSize = size,
            imageUrl = bubble.contentBlocks.firstOrNull()?.content ?: "",
            onClick = onClick,
            onLongClick = onLongClick,
            isPreview = true,
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TextBubble(
    bubble: BubbleModel,
    colors: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: () -> Unit = {},
    bubbleSize: BubbleType.BubbleSize,
    isPreview: Boolean,
    searchKeyword: String = ""
) {
    Box(
        modifier = Modifier
            .size(bubbleSize.size)
            .clip(CircleShape)
            .combinedClickable(
                onClick = { onClick() },
                onLongClick = { onLongClick() },
            ),
        contentAlignment = Alignment.Center
    ) {
        if (bubble.mainImage != null) {
            ImageBubble(
                bubble = bubble,
                bubbleSize = bubbleSize,
                imageUrl = bubble.mainImage,
                onClick = onClick,
                onLongClick = onLongClick,
                isPreview = isPreview,
                modifier = modifier
            )
        } else {
            when (colors.size) {
                0 -> SingleBubble(bubbleSize = bubbleSize, color = Gray100, modifier = modifier)
                1 -> SingleBubble(bubbleSize = bubbleSize, color = colors[0], modifier = modifier)
                2 -> DoubleBubble(bubbleSize = bubbleSize, colors = colors, modifier = modifier)
                3 -> TripleBubble(bubbleSize = bubbleSize, colors = colors, modifier = modifier)
            }
        }

        // 본문 내용 채우기
        TextContentBubble(
            bubble = bubble,
            bubbleSize = bubbleSize,
            isPreview = isPreview,
            searchKeyword = searchKeyword
        )
    }
}

@OptIn(ExperimentalRichTextApi::class)
@Composable
private fun TextContentBubble(
    bubble: BubbleModel,
    bubbleSize: BubbleType.BubbleSize,
    isPreview: Boolean,
    searchKeyword: String = ""
) {
    Box(
        modifier = if (bubbleSize == BubbleType.Bubble300)
            Modifier
                .size(bubbleSize.textBoxWidth, 96.5.dp)
        else
            Modifier
                .width(bubbleSize.textBoxWidth)
                .wrapContentHeight(),
        contentAlignment = Alignment.Center
    ) {
        // 미리보기일 때는 제목 or 본문 - 스타일 적용 안 됨
        if (isPreview) {
            val (text, isTitle) = extractPlainText(bubble)

            var annotatedString = buildAnnotatedString {
                append(text)
            }
            if (searchKeyword.isNotEmpty()) {
                annotatedString = buildAnnotatedString {
                    val startIndex = text.indexOf(searchKeyword)
                    val endIndex = startIndex + searchKeyword.length
                    append(text)
                    addStyle(
                        style = SpanStyle(
                            color = White000,
                            background = Gray700
                        ),
                        start = startIndex,
                        end = endIndex
                    )
                }
            }

            Text(
                text = annotatedString,
                style = if (isTitle) bubbleSize.titleFontStyle else bubbleSize.bodyFontStyle,
                color = Gray800,
                textAlign = TextAlign.Center
            )
        } else { // 미리보기 아닐 때는 무조건 본문 - 스타일 적용
            val text = extractContentText(bubble)

            // 본문이 null인 경우에는 제목으로 보여주기
            if (text.isEmpty()) {
                val title = bubble.title ?: ""
                Text(
                    text = title,
                    style = bubbleSize.titleFontStyle,
                    color = Gray800,
                    textAlign = TextAlign.Center
                )
            } else {
                val richTextState = rememberRichTextState()
                richTextState.setHtml(text)

                RichText(
                    state = richTextState,
                    style = bubbleSize.bodyFontStyle,
                    color = Gray800,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ImageBubble(
    bubble: BubbleModel,
    bubbleSize: BubbleType.BubbleSize,
    imageUrl: String,
    onClick: () -> Unit,
    modifier: Modifier,
    onLongClick: () -> Unit = {},
    isPreview: Boolean
) {
    Box(
        modifier = Modifier
            .size(bubbleSize.size)
            .clip(CircleShape)
            .combinedClickable(
                onClick = { onClick() },
                onLongClick = { onLongClick() },
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = modifier.size(bubbleSize.size)) {
            val outerRadius = bubbleSize.size.toPx() / 2

            // 큰 원 그리기
            drawCircle(color = Gray200, radius = outerRadius, center = center)
        }

        // 이미지 & 블러 레이어
        Box(
            modifier = Modifier
                .size(bubbleSize.size)
                .clip(CircleShape)
                .drawWithContent {
                    clipPath(
                        Path().apply {
                            addOval(
                                Rect(
                                    center - Offset(
                                        bubbleSize.size.toPx() / 2,
                                        bubbleSize.size.toPx() / 2
                                    ),
                                    center + Offset(
                                        bubbleSize.size.toPx() / 2,
                                        bubbleSize.size.toPx() / 2
                                    )
                                )
                            )
                        }
                    ) {
                        val colors = listOf(White000, Gray200)
                        drawCircle(
                            brush = Brush.verticalGradient(colors),
                            blendMode = BlendMode.SrcIn,
                        )
                        this@drawWithContent.drawContent()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .size(bubbleSize.size)
                    .clip(CircleShape)
            )

            Canvas(modifier = Modifier.size(bubbleSize.size * 0.95f)) {
                drawIntoCanvas { canvas ->
                    val paint = Paint().apply {
                        this.asFrameworkPaint().apply {
                            isAntiAlias = true
                            shader = LinearGradient(
                                center.x - bubbleSize.size.toPx() / 2,
                                center.y,
                                center.x + bubbleSize.size.toPx() / 2,
                                center.y,
                                intArrayOf(
                                    White000.copy(alpha = 0.5f).toArgb(),
                                    Gray200.copy(alpha = 0.5f).toArgb()
                                ),
                                floatArrayOf(0f, 1f),
                                Shader.TileMode.CLAMP
                            )
                            maskFilter = BlurMaskFilter(
                                30f,
                                BlurMaskFilter.Blur.NORMAL
                            )
                        }
                    }
                    canvas.drawCircle(center, (bubbleSize.size * 0.95f).toPx() / 2, paint)
                }
            }
        }

        // 텍스트 있는 경우
        if (checkBubbleContainText(bubble)) {
            TextContentBubble(bubble = bubble, bubbleSize = bubbleSize, isPreview = isPreview)
        }
    }
}

@Composable
private fun SingleBubble(
    bubbleSize: BubbleType.BubbleSize,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(bubbleSize.size)) {
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
private fun DoubleBubble(
    bubbleSize: BubbleType.BubbleSize,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    require(colors.size == 2) { "DoubleBubble requires exactly two colors." }

    Canvas(modifier = modifier.size(bubbleSize.size)) {
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
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(bubbleSize.size)) {

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
                    center - Offset(outerRadius, outerRadius),
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
 * 버블 내용에 이미지가 포함되어 있는지 확인하는 함수
 */
private fun checkBubbleContainImage(bubble: BubbleModel): Boolean {
    bubble.contentBlocks.forEach {
        if (it.type == ContentType.IMAGE) {
            return true
        }
    }
    return false
}

/**
 * 버블 내용에 텍스트가 포함되어 있는지 확인하는 함수
 */
private fun checkBubbleContainText(bubble: BubbleModel): Boolean {
    bubble.contentBlocks.forEach {
        if (it.type == ContentType.TEXT) {
            return true
        }
    }
    return false
}

/**
 * 버블 프리뷰 사이즈 결정 함수
 */
fun calculateBubblePreviewSize(bubble: BubbleModel): BubbleType.BubbleSize {
    if (bubble.title == null && !bubble.contentBlocks.map { it.type }.contains(ContentType.TEXT)) {
        return BubbleType.Bubble100
    }

    val (text, isTitle) = extractPlainText(bubble)

    listOf(
        BubbleType.Bubble100,
        BubbleType.Bubble160,
        BubbleType.Bubble230,
        BubbleType.Bubble300,
    ).forEach { bubbleType ->
        val textBoxWidth = bubbleType.textBoxWidth
        val fontSizeSp =
            if (isTitle) bubbleType.titleFontStyle.fontSize.value else bubbleType.bodyFontStyle.fontSize.value
        val lineCount = calculateLineCount(text, textBoxWidth, fontSizeSp)

        when {
            text.length <= 5 && bubbleType == BubbleType.Bubble100 -> return BubbleType.Bubble100
            lineCount <= 2 && bubbleType == BubbleType.Bubble160 -> return BubbleType.Bubble160
            lineCount <= 3 && bubbleType == BubbleType.Bubble230 -> return BubbleType.Bubble230
            lineCount <= 4 && bubbleType == BubbleType.Bubble300 -> return BubbleType.Bubble300
        }
    }

    return BubbleType.Bubble300
}

/**
 * 버블 텍스트 길이에 따른 버블 사이즈 계산
 */
fun calculateBubbleSize(bubble: BubbleModel): BubbleType.BubbleSize {
    if (checkBubbleContainImage(bubble)) {
        return BubbleType.BubbleDoor
    }

    // title && content text가 없는 경우
    if (bubble.title == null && !bubble.contentBlocks.map { it.type }.contains(ContentType.TEXT)) {
        return BubbleType.Bubble100
    }

    var text = extractContentText(bubble)
    var isTitle = false

    if (text.isEmpty()) {
        text = bubble.title ?: ""
        isTitle = true
    }

    listOf(
        BubbleType.Bubble100,
        BubbleType.Bubble160,
        BubbleType.Bubble230,
        BubbleType.Bubble300,
        BubbleType.BubbleMain
    ).forEach { bubbleType ->
        val textBoxWidth = bubbleType.textBoxWidth
        val fontSizeSp =
            if (isTitle) bubbleType.titleFontStyle.fontSize.value else bubbleType.bodyFontStyle.fontSize.value
        val lineCount = calculateLineCount(text, textBoxWidth, fontSizeSp)

        when {
            text.length <= 5 && bubbleType == BubbleType.Bubble100 -> return BubbleType.Bubble100
            lineCount <= 2 && bubbleType == BubbleType.Bubble160 -> return BubbleType.Bubble160
            lineCount <= 3 && bubbleType == BubbleType.Bubble230 -> return BubbleType.Bubble230
            lineCount <= 4 && bubbleType == BubbleType.Bubble300 -> return BubbleType.Bubble300
            lineCount <= 5 && bubbleType == BubbleType.BubbleMain -> return BubbleType.BubbleMain
        }
    }

    return BubbleType.BubbleDoor
}

/**
 * 버블 텍스트 길이에 따른 라인 수 계산
 */
private fun calculateLineCount(text: String, textBoxWidthDp: Dp, fontSizeSp: Float): Int {
    val charPerLine = (textBoxWidthDp.value / fontSizeSp * 1.5).toInt()

    val lines = text.split("\n").sumOf { line ->
        val lineLength = line.length
        val lineCount = lineLength / charPerLine
        if (lineLength % charPerLine == 0) lineCount else lineCount + 1
    }

    return lines
}

/**
 * 버블의 스타일 지정 안 된 텍스트 추출 함수
 */
fun extractPlainText(bubble: BubbleModel): Pair<String, Boolean> {
    var text = bubble.title ?: ""
    var isTitle = true

    if (text.isEmpty()) {
        text = extractContentText(bubble)
        text = text.parseHtml().replace("\n\n", "\n").trim()
        isTitle = false
    }

    return text to isTitle
}

/**
 * 버블의 본문 텍스트 추출 함수
 */
private fun extractContentText(bubble: BubbleModel): String {
    return bubble.contentBlocks.firstOrNull { it.type == ContentType.TEXT }?.content ?: ""
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
                        shader = LinearGradient(
                            center.x - radius,
                            center.y,
                            center.x + radius,
                            center.y,
                            intArrayOf(White000.toArgb(), colors[0].toArgb()),
                            floatArrayOf(0f, 1f),
                            Shader.TileMode.CLAMP
                        )
                    }

                    2 -> {
                        // 두 개의 색상 - 대각선 방향 Gradient
                        val angleRadians = Math.toRadians(30.0)
                        val startX = center.x - radius * cos(angleRadians).toFloat()
                        val startY = center.y - radius * sin(angleRadians).toFloat()
                        val endX = center.x + radius * cos(angleRadians).toFloat()
                        val endY = center.y + radius * sin(angleRadians).toFloat()
                        shader = LinearGradient(
                            startX, startY,
                            endX, endY,
                            intArrayOf(
                                White000.toArgb(),
                                colors[1].toArgb(),
                                colors[0].toArgb()
                            ),
                            floatArrayOf(0f, 0.46f, 1f),
                            Shader.TileMode.CLAMP
                        )
                    }

                    else -> {
                        // 기본 - 흰색 블러 처리만
                        color = android.graphics.Color.WHITE
                    }
                }
                // 블러 효과 추가
                maskFilter = BlurMaskFilter(
                    blurRadius,
                    BlurMaskFilter.Blur.NORMAL
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
                maskFilter = BlurMaskFilter(
                    blurRadius,
                    BlurMaskFilter.Blur.NORMAL
                )
            }
        }
        canvas.drawCircle(center, radius, paint)
    }
}

object BubbleType {
    val BubbleDoor = BubbleSize(
        size = 364.dp,
        innerSize = 326.dp,
        titleFontStyle = EdisonTypography.displayMedium,
        bodyFontStyle = EdisonTypography.headlineSmall,
        textBoxWidth = 258.dp
    )

    val BubbleMain = BubbleSize(
        size = 364.dp,
        innerSize = 326.dp,
        titleFontStyle = EdisonTypography.displayMedium,
        bodyFontStyle = EdisonTypography.headlineSmall,
        textBoxWidth = 258.dp
    )

    val Bubble300 = BubbleSize(
        size = 300.dp,
        innerSize = 270.dp,
        titleFontStyle = EdisonTypography.displaySmall,
        bodyFontStyle = EdisonTypography.titleLarge,
        textBoxWidth = 182.dp
    )

    val Bubble230 = BubbleSize(
        size = 230.dp,
        innerSize = 206.dp,
        titleFontStyle = EdisonTypography.headlineLarge,
        bodyFontStyle = EdisonTypography.titleMedium,
        textBoxWidth = 146.dp
    )

    val Bubble160 = BubbleSize(
        size = 160.dp,
        innerSize = 144.dp,
        titleFontStyle = EdisonTypography.headlineMedium,
        bodyFontStyle = EdisonTypography.titleSmall,
        textBoxWidth = 82.dp
    )

    val Bubble100 = BubbleSize(
        size = 100.dp,
        innerSize = 90.dp,
        titleFontStyle = EdisonTypography.headlineMedium,
        bodyFontStyle = EdisonTypography.titleSmall,
        textBoxWidth = 58.dp
    )

    data class BubbleSize(
        val size: Dp,
        val innerSize: Dp,
        val titleFontStyle: TextStyle,
        val bodyFontStyle: TextStyle,
        val textBoxWidth: Dp,
    )
}
