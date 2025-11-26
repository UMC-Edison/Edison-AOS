package com.umc.edison.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.painterResource
import com.umc.edison.R
import com.umc.edison.presentation.edison.BubbleInputState
import com.umc.edison.presentation.edison.util.parseHtml
import com.umc.edison.presentation.model.BubbleModel
import com.umc.edison.presentation.model.ContentType
import com.umc.edison.ui.theme.Gray300
import com.umc.edison.ui.theme.Gray500
import com.umc.edison.ui.theme.Gray600
import com.umc.edison.ui.theme.Gray800
import com.umc.edison.ui.theme.White000

@Composable
fun Toolbar(
    uiState: BubbleInputState,
    onIconClicked: (IconType) -> Unit,
    onTextStylesClicked: (TextStyle) -> Unit,
    onListStyleClicked: (ListStyle) -> Unit,
    onGalleryOpen: () -> Unit,
    onCameraOpen: () -> Unit,
    onBackLinkClick: (BubbleModel) -> Unit,
    onLinkBubbleClick: () -> Unit
) {
    when (uiState.selectedIcon) {
        IconType.TEXT -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(White000)
                    .border(1.dp, color = Gray300)
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onTextStylesClicked(TextStyle.BOLD) }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_bold),
                        contentDescription = "BOLD",
                        tint = if (uiState.selectedTextStyles.contains(TextStyle.BOLD)) Gray800 else Gray500
                    )
                }

                IconButton(onClick = { onTextStylesClicked(TextStyle.ITALIC) }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_italic),
                        contentDescription = "ITALIC",
                        tint = if (uiState.selectedTextStyles.contains(TextStyle.ITALIC)) Gray800 else Gray500
                    )
                }

                IconButton(onClick = { onTextStylesClicked(TextStyle.UNDERLINE) }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_underline),
                        contentDescription = "UNDERLINE",
                        tint = if (uiState.selectedTextStyles.contains(TextStyle.UNDERLINE)) Gray800 else Gray500
                    )
                }

                IconButton(onClick = { onTextStylesClicked(TextStyle.HIGHLIGHT) }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_highlight),
                        contentDescription = "HIGHLIGHT",
                        tint = if (uiState.selectedTextStyles.contains(TextStyle.HIGHLIGHT)) Gray800 else Gray500
                    )
                }

                IconButton(onClick = { onIconClicked(IconType.NONE) }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close),
                        contentDescription = "CLOSE",
                        tint = Gray600
                    )
                }
            }
        }

        IconType.LIST -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(White000)
                    .border(1.dp, color = Gray300)
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onListStyleClicked(ListStyle.UNORDERED) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_list),
                        contentDescription = "LIST",
                        tint = if (uiState.selectedListStyle == ListStyle.UNORDERED) Gray800 else Gray500
                    )
                }

                IconButton(
                    onClick = { onListStyleClicked(ListStyle.ORDERED) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_list_num),
                        contentDescription = "ORDERED LIST",
                        tint = if (uiState.selectedListStyle == ListStyle.ORDERED) Gray800 else Gray500
                    )
                }

                Spacer(modifier = Modifier.weight(2f))

                IconButton(
                    onClick = { onIconClicked(IconType.NONE) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close),
                        contentDescription = "CLOSE",
                        tint = Gray600
                    )
                }
            }
        }

        else -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(White000)
                    .border(1.dp, color = Gray300)
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onIconClicked(IconType.TEXT) }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_text_tool_off),
                        contentDescription = "Text",
                        tint = Gray500
                    )
                }

                IconButton(onClick = { onIconClicked(IconType.LIST) }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_list),
                        contentDescription = "List",
                        tint = Gray500
                    )
                }

                Box {
                    IconButton(onClick = { onIconClicked(IconType.CAMERA) }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_camera),
                            contentDescription = "Camera",
                            tint = if (uiState.selectedIcon == IconType.CAMERA) Gray800 else Gray500
                        )
                    }

                    if (uiState.selectedIcon == IconType.CAMERA) {
                        CameraPopup(
                            onGalleryOpen = {
                                onGalleryOpen()
                                onIconClicked(IconType.NONE)
                            },
                            onCameraOpen = {
                                onCameraOpen()
                                onIconClicked(IconType.NONE)
                            },
                            onDismiss = { onIconClicked(IconType.NONE) }
                        )
                    }
                }

                IconButton(onClick = { onIconClicked(IconType.LINK) }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_link),
                        contentDescription = "Link",
                        tint = if (uiState.selectedIcon == IconType.LINK) Gray800 else Gray500
                    )

                    if (uiState.selectedIcon == IconType.LINK) {
                        LinkPopUp(
                            backLink = { onIconClicked(IconType.BACK_LINK) },
                            linkBubble = { onLinkBubbleClick() },
                            onDismiss = { onIconClicked(IconType.NONE) }
                        )
                    }

                    if (uiState.selectedIcon == IconType.BACK_LINK) {
                        BackLinkPopUp(
                            onDismiss = { onIconClicked(IconType.NONE) },
                            bubbles = uiState.bubbles,
                            onBackLinkClick = { bubble -> onBackLinkClick(bubble) }
                        )
                    }
                }

                IconButton(onClick = { onIconClicked(IconType.TAG) }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_tag),
                        contentDescription = "Tag",
                        tint = if (uiState.selectedIcon == IconType.TAG) Gray800 else Gray500
                    )
                }
            }
        }
    }
}

@Composable
private fun CameraPopup(
    onGalleryOpen: () -> Unit,
    onCameraOpen: () -> Unit,
    onDismiss: () -> Unit
) {
    Popup(
        alignment = Alignment.BottomCenter,
        offset = IntOffset(0, -150),
        properties = PopupProperties(
            dismissOnClickOutside = true,
            focusable = false
        ),
        onDismissRequest = { onDismiss() },
    ) {
        Box(
            modifier = Modifier
                .wrapContentSize()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Gray300, White000),
                    )
                )
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = Color(0xFF3A3D40).copy(alpha = 0.12f),
                    spotColor = Color(0xFF3A3D40).copy(alpha = 0.12f),
                )
                .padding(1.dp),
        ) {
            Column(
                modifier = Modifier
                    .width(150.dp)
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(16.dp))
                    .background(White000)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "사진 촬영",
                    modifier = Modifier.clickable { onCameraOpen() },
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray800,
                    textAlign = TextAlign.Center
                )

                HorizontalDivider(modifier = Modifier.border(1.dp, Gray300))

                Text(
                    text = "갤러리",
                    modifier = Modifier.clickable { onGalleryOpen() },
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray800,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun LinkPopUp(
    backLink: () -> Unit,
    linkBubble: () -> Unit,
    onDismiss: () -> Unit
) {
    Popup(
        alignment = Alignment.BottomCenter,
        offset = IntOffset(0, -150),
        properties = PopupProperties(
            dismissOnClickOutside = true,
            focusable = false
        ),
        onDismissRequest = { onDismiss() },
    ) {
        Box(
            modifier = Modifier
                .wrapContentSize()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Gray300, White000),
                    )
                )
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = Color(0xFF3A3D40).copy(alpha = 0.12f),
                    spotColor = Color(0xFF3A3D40).copy(alpha = 0.12f),
                )
                .padding(1.dp),
        ) {
            Column(
                modifier = Modifier
                    .width(150.dp)
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(16.dp))
                    .background(White000)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "[ ] 백링크",
                    modifier = Modifier.clickable { backLink() },
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray800,
                    textAlign = TextAlign.Center
                )

                HorizontalDivider(modifier = Modifier.border(1.dp, Gray300))

                Text(
                    text = "링크버블 만들기",
                    modifier = Modifier.clickable { linkBubble() },
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray800,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun BackLinkPopUp(
    onDismiss: () -> Unit,
    bubbles: List<BubbleModel>,
    onBackLinkClick: (BubbleModel) -> Unit
) {
    Popup(
        alignment = Alignment.BottomCenter,
        offset = IntOffset(0, -150),
        properties = PopupProperties(
            dismissOnClickOutside = true,
            focusable = false
        ),
        onDismissRequest = { onDismiss() },
    ) {
        Box(
            modifier = Modifier
                .wrapContentSize()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Gray300, White000),
                    )
                )
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = Color(0xFF3A3D40).copy(alpha = 0.12f),
                    spotColor = Color(0xFF3A3D40).copy(alpha = 0.12f),
                )
                .padding(1.dp),
        ) {

            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .width(150.dp)
                    .heightIn(max = 300.dp)
                    .verticalScroll(scrollState)
                    .clip(RoundedCornerShape(16.dp))
                    .background(White000)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                for (bubble in bubbles) {
                    val selectedTitle = bubble.title?.takeIf { it.isNotBlank() }
                        ?: bubble.contentBlocks
                            .filter { it.type == ContentType.TEXT }
                            .firstOrNull { it.content.parseHtml().isNotBlank() }
                            ?.content?.parseHtml()?.take(10)
                        ?: "내용 없음"

                    val splitTitle = selectedTitle.split("\n")

                    Text(
                        text = splitTitle.first(),
                        modifier = Modifier.clickable { onBackLinkClick(bubble) },
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray800,
                        textAlign = TextAlign.Center
                    )

                    if (bubbles.indexOf(bubble) != bubbles.size - 1) {
                        HorizontalDivider(modifier = Modifier.border(1.dp, Gray300))
                    }
                }
            }
        }
    }
}

enum class IconType {
    NONE, TEXT, LIST, CAMERA, LINK, TAG,
    BACK_LINK
}

enum class TextStyle {
    BOLD, ITALIC, UNDERLINE, HIGHLIGHT
}

enum class ListStyle {
    NONE, UNORDERED, ORDERED
}
