package com.umc.edison.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.umc.edison.ui.theme.Gray800
import com.umc.edison.ui.theme.White000
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet(
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    content: @Composable () -> Unit
) {
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss,
        containerColor = White000,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetPopUp(
    title: String,
    cancelText: String,
    confirmText: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    BottomSheet(
        onDismiss = onDismiss,
        sheetState = sheetState,
    ) {
        BottomSheetPopUpContent(
            title = title,
            cancelText = cancelText,
            confirmText = confirmText,
            onDismiss = onDismiss,
            onConfirm = onConfirm
        )
    }
}

@Composable
private fun BottomSheetPopUpContent(
    title: String,
    cancelText: String,
    confirmText: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 26.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.displaySmall,
            color = Gray800,
            textAlign = TextAlign.Center
        )

        Row(
            modifier = Modifier.padding(vertical = 17.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MiddleCancelButton(
                text = cancelText,
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            )

            MiddleConfirmButton(
                text = confirmText,
                enabled = true,
                onClick = onConfirm,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun BottomSheetForDelete(
    onButtonClick: () -> Unit,
    onDelete: () -> Unit,
    buttonEnabled: Boolean,
    buttonText: String,
    selectedCnt: Int = 0,
    showSelectedCnt: Boolean = false,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(White000)
            .padding(horizontal = 24.dp, vertical = 8.dp),
    ) {
        if (showSelectedCnt) {
            Text(
                text = "선택 ${selectedCnt}개",
                style = MaterialTheme.typography.labelLarge,
                color = Gray800,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(end = 8.dp)
            )
        }
        Row(
            modifier = Modifier
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(17.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            MiddleConfirmButton(
                text = buttonText,
                enabled = buttonEnabled,
                onClick = { onButtonClick() },
                modifier = Modifier.weight(1f)
            )

            MiddleDeleteButton(
                enabled = buttonEnabled,
                onClick = { onDelete() },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun CustomDraggableBottomSheet(
    collapsedOffset: Float, // sheet가 collapsed 상태일 때의 y-offset (px)
    expandedOffset: Float,  // sheet가 완전히 펼쳐진 상태일 때의 y-offset (보통 0)
    onOffsetChanged: (Float) -> Unit = {},
    sheetContent: @Composable (topPadding: Dp) -> Unit
) {
    val scope = rememberCoroutineScope()
    val dragOffset = remember { Animatable(collapsedOffset) }

    // 드래그 진행에 따른 fraction (0: expanded, 1: collapsed)
    val fraction = ((dragOffset.value - expandedOffset) / (collapsedOffset - expandedOffset)).coerceIn(0f, 1f)

    val currentTopPadding = if (fraction == 0f) 0.dp else 40.dp
    val currentCornerRadius = if (fraction < 0.3f) (fraction * 20).dp else 20.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            // 현재 드래그 오프셋만큼 아래로 이동
            .offset { IntOffset(x = 0, y = dragOffset.value.roundToInt()) }
            .draggable(
                orientation = Orientation.Vertical,
                state = rememberDraggableState { delta ->
                    scope.launch {
                        val newOffset = (dragOffset.value + delta).coerceIn(expandedOffset, collapsedOffset)
                        dragOffset.snapTo(newOffset)
                    }
                },
                onDragStopped = { _ ->
                    scope.launch {
                        // 중간값을 기준으로 스냅: 중간보다 위면 expanded, 아니면 collapsed
                        val midPoint = (collapsedOffset + expandedOffset) / 2
                        if (dragOffset.value < midPoint) {
                            dragOffset.animateTo(expandedOffset, tween(300))
                        } else {
                            dragOffset.animateTo(collapsedOffset, tween(300))
                        }
                    }
                }
            )
            .background(
                color = White000,
                shape = RoundedCornerShape(
                    topStart = currentCornerRadius,
                    topEnd = currentCornerRadius
                )
            )
    ) {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(start = 24.dp, top = currentTopPadding, end = 24.dp, bottom = 12.dp)
        ) {
            sheetContent(currentTopPadding)

            LaunchedEffect(dragOffset.value) {
                onOffsetChanged(dragOffset.value)
            }
        }
    }
}

