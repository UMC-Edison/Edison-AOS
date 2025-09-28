package com.umc.edison.ui.label

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.umc.edison.presentation.label.LabelEditMode
import com.umc.edison.presentation.label.LabelListViewModel
import com.umc.edison.presentation.model.LabelModel
import com.umc.edison.ui.BaseContent
import com.umc.edison.ui.components.AddLabelButton
import com.umc.edison.ui.components.BottomSheet
import com.umc.edison.ui.components.BottomSheetPopUp
import com.umc.edison.ui.components.LabelListItem
import com.umc.edison.ui.components.LabelModalContent
import com.umc.edison.ui.navigation.NavRoute
import com.umc.edison.ui.onboarding.LabelListOnboardingScreen
import com.umc.edison.ui.theme.Aqua100
import com.umc.edison.ui.theme.Gray600
import com.umc.edison.ui.theme.Gray800

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabelTabScreen(
    navHostController: NavHostController,
    viewModel: LabelListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val baseState by viewModel.baseState.collectAsState()
    val onboardingState by viewModel.onboardingState.collectAsState()

    val draggedIndex = remember { mutableIntStateOf(-1) }

    LaunchedEffect(Unit) {
        viewModel.fetchTotalBubbleCount()
        viewModel.fetchLabels()
    }

    BaseContent(
        modifier = Modifier
            .clickable(
                onClick = {
                    draggedIndex.intValue = -1
                },
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        baseState = baseState,
    ) {
        if (uiState.labelEditMode == LabelEditMode.ADD || uiState.labelEditMode == LabelEditMode.EDIT) {
            BottomSheet(
                onDismiss = {
                    viewModel.updateEditMode(LabelEditMode.NONE)
                },
            ) {
                LabelModalContent(
                    editMode = uiState.labelEditMode,
                    onDismiss = {
                        viewModel.updateEditMode(LabelEditMode.NONE)
                    },
                    onConfirm = { label ->
                        viewModel.confirmLabelModal(label)
                    },
                    label = uiState.selectedLabel
                )
            }
        }

        if (uiState.labelEditMode == LabelEditMode.DELETE) {
            BottomSheetPopUp(
                title = "${uiState.selectedLabel.name} 라벨을 삭제하시겠습니까?",
                cancelText = "취소",
                confirmText = "삭제",
                onDismiss = {
                    viewModel.updateEditMode(LabelEditMode.NONE)
                },
                onConfirm = {
                    viewModel.deleteSelectedLabel()
                },
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, top = 100.dp),

            horizontalAlignment = Alignment.Start
        ) {
            Text(
                modifier = Modifier.padding(start = 4.dp),
                text = "버블 ${uiState.bubbleCount}개",
                style = MaterialTheme.typography.labelMedium,
                color = Gray600
            )
            Spacer(modifier = Modifier.height(10.dp))

            AddLabelButton(
                onClick = {
                    viewModel.updateEditMode(LabelEditMode.ADD)
                }
            )
            val labels = if (uiState.labels.isEmpty() && onboardingState.show) {
                listOf(LabelModel(id = "", name = "감상", color = Aqua100, bubbleCnt = 7))
            } else {
                uiState.labels
            }

            LabelList(
                labels = labels,
                draggedIndex = draggedIndex.intValue,
                onLabelClick = { labelId ->
                    navHostController.navigate(NavRoute.LabelDetail.createRoute(labelId))
                },
                onEditClick = { index ->
                    viewModel.updateEditMode(LabelEditMode.EDIT)
                    viewModel.updateSelectedLabel(uiState.labels[index])
                },
                onDeleteClick = { index ->
                    viewModel.updateEditMode(LabelEditMode.DELETE)
                    viewModel.updateSelectedLabel(uiState.labels[index])
                },
                onDrag = { index ->
                    draggedIndex.intValue = index
                },
                resetDrag = {
                    draggedIndex.intValue = -1
                },
                setLabelItemPosition = { offset, size ->
                    viewModel.setLabelListItemBound(offset, size)
                },
                showOnboarding = onboardingState.show,
            )
        }

        if (onboardingState.show) {
            if (uiState.labels.isNotEmpty()) {
                draggedIndex.intValue = 1
            } else {
                draggedIndex.intValue = 0
            }

            LabelListOnboardingScreen(
                onDismiss = {
                    viewModel.setHasSeenOnboarding()
                    draggedIndex.intValue = -1
                },
                labelListItemComponent = onboardingState.labelBound
            )
        }
    }
}

@Composable
fun LabelList(
    labels: List<LabelModel>,
    draggedIndex: Int,
    onLabelClick: (String?) -> Unit,
    onEditClick: (Int) -> Unit,
    onDeleteClick: (Int) -> Unit,
    onDrag: (Int) -> Unit,
    resetDrag: () -> Unit,
    setLabelItemPosition: (Offset, IntSize) -> Unit,
    showOnboarding: Boolean = false,
) {
    Column {
        labels.forEachIndexed { index, label ->
            val modifier = if ((labels.size > 1 && index == 1) || index == 0) {
                Modifier.onGloballyPositioned { coordinates ->
                    setLabelItemPosition(
                        coordinates.positionOnScreen(),
                        coordinates.size
                    )
                }
            } else {
                Modifier
            }

            Log.i("LabelList", "index: $index, draggedIndex: $draggedIndex, label: ${label.name}")
            LabelListItem(
                labelColor = label.color,
                labelText = label.name,
                count = label.bubbleCnt,
                isDragged = index == draggedIndex,
                onClick = { onLabelClick(label.id) },
                onEditClick = { onEditClick(index) },
                onDeleteClick = { onDeleteClick(index) },
                onDrag = { onDrag(index) },
                resetDrag = resetDrag,
                modifier = modifier,
                showOnboarding = showOnboarding && index == draggedIndex
            )
        }
    }
}
