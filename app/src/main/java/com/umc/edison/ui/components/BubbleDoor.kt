package com.umc.edison.ui.components

import android.graphics.BlurMaskFilter
import android.graphics.LinearGradient
import android.graphics.Shader
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Size
import com.umc.edison.presentation.model.BubbleModel
import com.umc.edison.presentation.model.ContentBlockModel
import com.umc.edison.ui.theme.Gray100
import com.umc.edison.ui.theme.Gray500
import com.umc.edison.ui.theme.Gray700
import com.umc.edison.ui.theme.Gray800
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import com.mohamedrejeb.richeditor.annotation.ExperimentalRichTextApi
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.ui.BasicRichText
import com.mohamedrejeb.richeditor.ui.BasicRichTextEditor
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import com.umc.edison.presentation.edison.BubbleInputState
import com.umc.edison.presentation.edison.parseHtml
import com.umc.edison.presentation.model.ContentType
import com.umc.edison.ui.theme.Red100
import com.umc.edison.ui.theme.Red500
import com.umc.edison.ui.theme.White000
import com.umc.edison.ui.theme.Yellow100

@Composable
fun BubbleDoor(
    bubble: BubbleModel,
    onClick: (() -> Unit)? = null,
    isEditable: Boolean = false,
    onBubbleUpdate: (BubbleModel) -> Unit = {},
    onImageDeleted: (ContentBlockModel) -> Unit = {},
    onMainSelected: (String?) -> Unit = {},
    bubbleInputState: BubbleInputState = BubbleInputState.DEFAULT,
    onLinkClick: (String) -> Unit = {},
    onBackLinkDeleted: (BubbleModel) -> Unit = {},
    onLinkBubbleDeleted: (BubbleModel) -> Unit = {},
    // 새로 추가: 기본값은 no-op
    onTextFocused: (Int) -> Unit = {},
    onEnterPressed: (Int) -> Unit = {},
    onGapTapped: (leftIndex: Int?, rightIndex: Int?) -> Unit = { _, _ -> },
) {
    val colors = bubble.labels.map { it.color }
    val outerColors = when (colors.size) {
        0 -> emptyList()
        1 -> listOf(colors[0])
        2 -> listOf(colors[0], colors[1])
        3 -> listOf(colors[0], colors[1], colors[2])
        else -> colors
    }
    val innerColors = when (colors.size) {
        0 -> listOf(White000)
        1 -> listOf(White000, colors[0])
        2 -> listOf(White000, colors[0], colors[1])
        3 -> listOf(White000, colors[0], colors[1], colors[2])
        else -> colors
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 24.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        // 배경 Canvas (전체 높이 채우기)
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawOuterGradientBubbleDoor(
                width = size.width,
                height = size.height,
                colors = outerColors
            )

            drawBlurredInnerGradientBubbleDoor(
                width = size.width,
                height = size.height,
                colors = innerColors
            )
        }

        // 콘텐츠 영역
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(start = 24.dp, top = 80.dp, end = 24.dp, bottom = 24.dp)
                .clickable(
                    onClick = onClick ?: {},
                    enabled = onClick != null
                ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BubbleContent(
                isEditable = isEditable,
                bubble = bubble,
                onBubbleChange = onBubbleUpdate,
                uiState = bubbleInputState,
                deleteClicked = onImageDeleted,
                mainClicked = onMainSelected,
                onLinkClick = onLinkClick,
                onBackLinkDeleted = onBackLinkDeleted,
                onLinkBubbleDeleted = onLinkBubbleDeleted,
                onTextFocused = onTextFocused,
                onEnterPressed = onEnterPressed,
                onGapTapped = onGapTapped
            )
        }
    }
}

@OptIn(ExperimentalRichTextApi::class, ExperimentalLayoutApi::class)
@Composable
private fun BubbleContent(
    isEditable: Boolean,
    bubble: BubbleModel,
    onBubbleChange: (BubbleModel) -> Unit,
    uiState: BubbleInputState,
    deleteClicked: (ContentBlockModel) -> Unit,
    mainClicked: (String?) -> Unit,
    onLinkClick: (String) -> Unit,
    onBackLinkDeleted: (BubbleModel) -> Unit,
    onLinkBubbleDeleted: (BubbleModel) -> Unit,
    onTextFocused: (Int) -> Unit,
    onEnterPressed: (Int) -> Unit,
    onGapTapped: (leftIndex: Int?, rightIndex: Int?) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        if (isEditable) {
            BasicTextField(
                value = bubble.title ?: "",
                onValueChange = { newTitle ->
                    onBubbleChange(
                        bubble.copy(title = newTitle)
                    )
                },
                textStyle = MaterialTheme.typography.displayMedium.copy(color = Gray800),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (bubble.title.isNullOrEmpty()) {
                            Text(
                                text = "제목",
                                style = MaterialTheme.typography.displayMedium.copy(color = Gray500),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        innerTextField()
                    }
                }
            )
        } else if (bubble.title != null) {
            Text(
                text = bubble.title,
                style = MaterialTheme.typography.displayMedium,
                color = Gray800,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        var deletedImageBlockId by remember { mutableIntStateOf(-1) }

        bubble.contentBlocks.forEach { contentBlock ->
            // position을 한 번만 계산해서 씁니다
            val pos = contentBlock.position

            key(contentBlock.id) { // 안정 키
                when (contentBlock.type) {
                    ContentType.TEXT -> {
                        val richTextState = rememberSaveable(
                            // index 키 → id 키 변경
                            key = "richTextState_${contentBlock.id}",
                            saver = RichTextState.Saver
                        ) {
                            RichTextState().apply { setHtml(contentBlock.content) }
                        }

                        val isInitialized = remember(contentBlock.id) { mutableStateOf(false) }

                        LaunchedEffect(deletedImageBlockId, contentBlock.id) {
                            // 이미지 삭제 등으로 강제 리셋 필요할 때
                            if (deletedImageBlockId >= 0) {
                                richTextState.setHtml(contentBlock.content)
                                deletedImageBlockId = -1
                            }
                        }

                        SideEffect {
                            if (!isInitialized.value) {
                                richTextState.setHtml(contentBlock.content)
                                isInitialized.value = true
                            }
                        }

                        LaunchedEffect(contentBlock.id, richTextState.toHtml()) {
                            onBubbleChange(
                                bubble.copy(
                                    contentBlocks = bubble.contentBlocks.map { b ->
                                        if (b.id == contentBlock.id) b.copy(content = richTextState.toHtml())
                                        else b
                                    }
                                )
                            )
                        }

                        if (uiState.selectedTextStyles.contains(TextStyle.BOLD)) {
                            richTextState.addSpanStyle(SpanStyle(fontWeight = FontWeight.Bold))
                        } else {
                            richTextState.removeSpanStyle(SpanStyle(fontWeight = FontWeight.Bold))
                        }
                        if (uiState.selectedTextStyles.contains(TextStyle.ITALIC)) {
                            richTextState.addSpanStyle(SpanStyle(fontStyle = FontStyle.Italic))
                        } else {
                            richTextState.removeSpanStyle(SpanStyle(fontStyle = FontStyle.Italic))
                        }
                        if (uiState.selectedTextStyles.contains(TextStyle.UNDERLINE)) {
                            richTextState.addSpanStyle(SpanStyle(textDecoration = TextDecoration.Underline))
                        } else {
                            richTextState.removeSpanStyle(SpanStyle(textDecoration = TextDecoration.Underline))
                        }
                        if (uiState.selectedTextStyles.contains(TextStyle.HIGHLIGHT)) {
                            richTextState.addSpanStyle(SpanStyle(background = Yellow100))
                        } else {
                            richTextState.removeSpanStyle(SpanStyle(background = Yellow100))
                        }
                        if (uiState.selectedListStyle == ListStyle.UNORDERED) {
                            richTextState.addUnorderedList()
                        } else {
                            richTextState.removeUnorderedList()
                        }
                        if (uiState.selectedListStyle == ListStyle.ORDERED) {
                            richTextState.addOrderedList()
                        } else {
                            richTextState.removeOrderedList()
                        }

                        if (isEditable) {
                            BasicRichTextEditor(
                                state = richTextState,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(color = Gray800),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    // index → pos
                                    .onFocusChanged { if (it.isFocused) onTextFocused(pos) }
                                    .onPreviewKeyEvent { ev ->
                                        if (ev.key == Key.Enter && ev.type == KeyEventType.KeyUp) {
                                            onEnterPressed(pos)
                                            true
                                        } else false
                                    },
                                decorationBox = { innerTextField ->
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        if (richTextState.toHtml() == "<br>" && bubble.contentBlocks.size == 1) {
                                            Text(
                                                text = "내용을 입력해주세요.",
                                                style = MaterialTheme.typography.bodyMedium.copy(color = Gray500),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                        } else {
                            BasicRichText(
                                state = richTextState,
                                style = MaterialTheme.typography.bodyMedium.copy(color = Gray800),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }

                    ContentType.IMAGE -> {
                        val aspectRatio = calculateAspectRatio(contentBlock.content)
                        var isLongPressed by remember(contentBlock.id) { mutableStateOf(false) }
                        val isMainImage = bubble.mainImage == contentBlock.content

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(aspectRatio)
                                .pointerInput(contentBlock.id) {
                                    detectTapGestures(onLongPress = { isLongPressed = true })
                                }
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(contentBlock.content)
                                        .crossfade(true)
                                        .size(Size.ORIGINAL)
                                        .build()
                                ),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(8.dp))
                            )

                            // 위쪽 갭: index → pos
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(24.dp)
                                    .align(Alignment.TopCenter)
                                    .clickable {
                                        val leftIndex = (pos - 1).takeIf { it >= 0 }
                                        val rightIndex = pos
                                        onGapTapped(leftIndex, rightIndex)
                                    }
                            )

                            // 아래쪽 갭: index → pos
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(24.dp)
                                    .align(Alignment.BottomCenter)
                                    .clickable {
                                        val leftIndex = pos
                                        val rightIndex = (pos + 1).takeIf { it < bubble.contentBlocks.size }
                                        onGapTapped(leftIndex, rightIndex)
                                    }
                            )

                            if (isLongPressed) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable { isLongPressed = false },
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Button(
                                        shape = RoundedCornerShape(100.dp),
                                        onClick = {
                                            isLongPressed = false
                                            mainClicked(contentBlock.content)
                                            deletedImageBlockId = pos // index → pos
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isMainImage) Gray100 else Gray700,
                                        )
                                    ) {
                                        Text(
                                            text = if (isMainImage) "대표 해제" else "대표 설정",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontSize = 14.sp,
                                            color = if (isMainImage) Red500 else Gray100
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(1.dp))

                                    Button(
                                        shape = RoundedCornerShape(100.dp),
                                        onClick = {
                                            isLongPressed = false
                                            deleteClicked(contentBlock)
                                            deletedImageBlockId = pos // index → pos
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Gray700,
                                        )
                                    ) {
                                        Text(
                                            text = "삭제하기",
                                            style = MaterialTheme.typography.bodyMedium.copy(color = Red100),
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (isEditable) {
                    // 공통 ‘갭’ 버튼: index → pos
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .clickable {
                                val left = pos
                                val right = (pos + 1).takeIf { it < bubble.contentBlocks.size }
                                onGapTapped(left, right)
                            }
                    )
                }
            }
        }



        FlowRow(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            bubble.backLinks.forEach { backLink ->

                var isLongPressed by remember(backLink.id) { mutableStateOf(false) }
                var layoutResult by remember(backLink.id) { mutableStateOf<TextLayoutResult?>(null) }

                val annotatedString = remember(backLink.id) {
                    val selectedTitle = backLink.title?.takeIf { it.isNotBlank() }
                        ?: backLink.contentBlocks
                            .filter { it.type == ContentType.TEXT }
                            .firstOrNull { it.content.parseHtml().isNotBlank() }
                            ?.content?.parseHtml()?.take(10)
                        ?: "내용 없음"

                    val displayText = selectedTitle.split("\n").first()

                    buildAnnotatedString {
                        pushStringAnnotation(
                            tag = "URL",
                            annotation = backLink.id.toString()
                        )
                        withStyle(SpanStyle(color = Color.Blue)) {
                            append("[[ ")
                            append(displayText)
                            append(" ]]")
                        }
                        pop()
                    }
                }

                Box(
                    modifier = Modifier
                        .wrapContentWidth() // FlowRow 대응
                        .pointerInput(backLink.id) {
                            detectTapGestures(
                                onLongPress = {
                                    isLongPressed = true
                                },
                                onTap = { offsetPos ->
                                    layoutResult?.let { layout ->
                                        val offset = layout.getOffsetForPosition(offsetPos)
                                        annotatedString.getStringAnnotations("URL", offset, offset)
                                            .firstOrNull()?.let { annotation ->
                                                onLinkClick(annotation.item)
                                            }
                                    }
                                }
                            )
                        }
                ) {

                    BasicText(
                        text = annotatedString,
                        onTextLayout = { layoutResult = it },
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (isLongPressed) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clickable { isLongPressed = false }, // 바깥 클릭 시 닫기
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Button(
                                shape = RoundedCornerShape(100.dp),
                                onClick = {
                                    isLongPressed = false
                                    onBackLinkDeleted(backLink)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor =  Gray100
                                )
                            ) {
                                Text(
                                    text = "백링크 삭제하기",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontSize = 14.sp,
                                    color = Red500
                                )
                            }

                            Spacer(modifier = Modifier.height(1.dp))

                            Button(
                                shape = RoundedCornerShape(100.dp),
                                onClick = {
                                    isLongPressed = false
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Gray700,
                                )
                            ) {
                                Text(
                                    text = "취소",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = Red100),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        bubble.linkedBubble?.let { linkedBubble ->

            var isLongPressed by remember(linkedBubble.id) { mutableStateOf(false) }
            var layoutResult by remember(linkedBubble.id) { mutableStateOf<TextLayoutResult?>(null) }

            val annotatedString = remember(linkedBubble.id) {
                val selectedTitle = linkedBubble.title?.takeIf { it.isNotBlank() }
                    ?: linkedBubble.contentBlocks
                        .filter { it.type == ContentType.TEXT }
                        .firstOrNull { it.content.parseHtml().isNotBlank() }
                        ?.content?.parseHtml()?.take(10)
                    ?: "내용 없음"

                val displayText = selectedTitle.split("\n").first()

                buildAnnotatedString {
                    pushStringAnnotation(
                        tag = "URL",
                        annotation = linkedBubble.id.toString()
                    )
                    withStyle(SpanStyle(color = Color.Blue)) {
                        append("[[ ")
                        append(displayText)
                        append(" ]]")
                    }
                    pop()
                }
            }

            Box(
                modifier = Modifier
                    .wrapContentWidth()
                    .pointerInput(linkedBubble.id) {
                        detectTapGestures(
                            onLongPress = { isLongPressed = true },
                            onTap = { offsetPos ->
                                layoutResult?.let { layout ->
                                    val offset = layout.getOffsetForPosition(offsetPos)
                                    annotatedString.getStringAnnotations("URL", offset, offset)
                                        .firstOrNull()?.let { annotation ->
                                            onLinkClick(annotation.item)
                                        }
                                }
                            }
                        )
                    }
            ) {
                BasicText(
                    text = annotatedString,
                    onTextLayout = { layoutResult = it },
                    style = MaterialTheme.typography.bodyMedium
                )

                if (isLongPressed) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable { isLongPressed = false },
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            shape = RoundedCornerShape(100.dp),
                            onClick = {
                                isLongPressed = false
                                onLinkBubbleDeleted(linkedBubble)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor =  Gray100
                            )
                        ) {
                            Text(
                                text = "링크버블 삭제하기",
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 14.sp,
                                color = Red500
                            )
                        }

                        Spacer(modifier = Modifier.height(1.dp))

                        Button(
                            shape = RoundedCornerShape(100.dp),
                            onClick = {
                                isLongPressed = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Gray700,
                            )
                        ) {
                            Text(
                                text = "취소",
                                style = MaterialTheme.typography.bodyMedium.copy(color = Red100),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

    }
}

@Composable
private fun calculateAspectRatio(content: String): Float {
    var aspectRatio by remember { mutableFloatStateOf(1f) }

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(content)
            .crossfade(true)
            .size(Size.ORIGINAL)
            .build(),
        onSuccess = { result ->
            val width = result.painter.intrinsicSize.width
            val height = result.painter.intrinsicSize.height
            if (width > 0 && height > 0) {
                aspectRatio = width / height
            }
        }
    )

    return aspectRatio
}

private fun DrawScope.drawOuterGradientBubbleDoor(
    width: Float,
    height: Float,
    colors: List<Color>
) {
    val circleCenter = Offset(x = width / 2, y = width)
    val rectTop = circleCenter.y - (width * 0.5f)

    val path = Path().apply {
        addOval(Rect(center = circleCenter, radius = width))
        addRect(Rect(Offset(0f, rectTop), androidx.compose.ui.geometry.Size(width, height)))
    }

    drawIntoCanvas { canvas ->
        val paint = Paint().apply {
            this.asFrameworkPaint().apply {
                isAntiAlias = true
                when (colors.size) {
                    0 -> color = Gray100.toArgb()
                    1 -> color = colors[0].toArgb()
                    2 -> {
                        val startX = circleCenter.x - width
                        val endX = circleCenter.x + width
                        val startY = rectTop - height / 2
                        val endY = rectTop - height / 2
                        shader = LinearGradient(
                            startX, startY,
                            endX, endY,
                            intArrayOf(
                                colors[0].toArgb(),
                                colors[1].toArgb()
                            ),
                            floatArrayOf(0.0f, 1.0f),
                            Shader.TileMode.CLAMP
                        )
                    }

                    3 -> {
                        val startX = circleCenter.x - width
                        val endX = circleCenter.x + width
                        val startY = rectTop - height / 2
                        val endY = rectTop - height / 2
                        shader = LinearGradient(
                            startX, startY,
                            endX, endY,
                            intArrayOf(
                                colors[0].toArgb(),
                                colors[1].toArgb(),
                                colors[2].toArgb()
                            ),
                            floatArrayOf(0.0f, 0.52f, 1.0f),
                            Shader.TileMode.CLAMP
                        )
                    }

                    else -> color = Gray800.toArgb()
                }
            }
        }
        canvas.clipPath(path)
        canvas.drawPath(path, paint)
    }
}

private fun DrawScope.drawBlurredInnerGradientBubbleDoor(
    width: Float,
    height: Float,
    colors: List<Color>
) {
    val circleCenter = Offset(x = width / 2, y = width * 1.04f)
    val rectTop = circleCenter.y - (width * 0.5f)

    val path = Path().apply {
        addOval(Rect(center = circleCenter, radius = width))
        addRect(Rect(Offset(0f, rectTop), androidx.compose.ui.geometry.Size(width, height)))
    }

    drawIntoCanvas { canvas ->
        val paint = Paint().apply {
            this.asFrameworkPaint().apply {
                isAntiAlias = true
                when (colors.size) {
                    1 -> {
                        val startX = circleCenter.x
                        val endX = circleCenter.x
                        val endY = rectTop + height
                        shader = LinearGradient(
                            startX, rectTop,
                            endX, endY,
                            intArrayOf(
                                colors[0].copy(alpha = 0.5f).toArgb(),
                                Gray100.copy(alpha = 0.5f).toArgb()
                            ),
                            floatArrayOf(0.0f, 1.0f),
                            Shader.TileMode.CLAMP
                        )
                    }

                    2 -> {
                        val startX = circleCenter.x
                        val endX = circleCenter.x
                        val endY = rectTop + height
                        shader = LinearGradient(
                            startX, rectTop,
                            endX, endY,
                            intArrayOf(
                                colors[0].copy(alpha = 0.5f).toArgb(),
                                Color(0xFFF7F9FB).copy(alpha = 0.5f).toArgb(),
                                colors[1].copy(alpha = 0.5f).toArgb(),
                            ),
                            floatArrayOf(0.0f, 0.54f, 1.0f),
                            Shader.TileMode.CLAMP
                        )
                    }

                    3 -> {
                        val startX = circleCenter.x - width
                        val endX = circleCenter.x + width
                        val endY = rectTop + height
                        shader = LinearGradient(
                            startX, rectTop,
                            endX, endY,
                            intArrayOf(
                                colors[0].copy(alpha = 0.5f).toArgb(),
                                colors[1].copy(alpha = 0.5f).toArgb(),
                                colors[2].copy(alpha = 0.5f).toArgb()
                            ),
                            floatArrayOf(0.0f, 0.49f, 1.0f),
                            Shader.TileMode.CLAMP
                        )
                    }

                    4 -> {
                        val startX = circleCenter.x - width
                        val endX = circleCenter.x + width
                        val endY = rectTop + height
                        shader = LinearGradient(
                            startX, rectTop,
                            endX, endY,
                            intArrayOf(
                                colors[0].copy(alpha = 0.5f).toArgb(),
                                colors[1].copy(alpha = 0.5f).toArgb(),
                                colors[2].copy(alpha = 0.5f).toArgb(),
                                colors[3].copy(alpha = 0.5f).toArgb()
                            ),
                            floatArrayOf(0.0f, 0.37f, 0.66f, 1.0f),
                            Shader.TileMode.CLAMP
                        )
                    }

                    else -> color = Gray800.toArgb()
                }
                maskFilter = BlurMaskFilter(80f, BlurMaskFilter.Blur.NORMAL)
            }
        }
        canvas.clipPath(path)
        canvas.drawPath(path, paint)
    }
}