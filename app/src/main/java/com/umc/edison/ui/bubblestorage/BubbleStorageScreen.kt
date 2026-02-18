package com.umc.edison.ui.bubblestorage

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.umc.edison.ui.components.Bubble
import com.umc.edison.presentation.model.BubbleModel
import com.umc.edison.presentation.baseBubble.BubbleStorageMode
import com.umc.edison.presentation.storage.BubbleStorageViewModel
import com.umc.edison.ui.BaseContent
import com.umc.edison.ui.components.BottomSheet
import com.umc.edison.ui.components.BottomSheetForDelete
import com.umc.edison.ui.components.BottomSheetPopUp
import com.umc.edison.ui.components.BubbleType
import com.umc.edison.ui.components.BubblesLayout
import com.umc.edison.ui.components.LabelTagList
import com.umc.edison.ui.components.calculateBubbleSize
import com.umc.edison.ui.navigation.NavRoute
import com.umc.edison.ui.onboarding.BubbleSpaceOnboarding
import com.umc.edison.ui.theme.Gray300
import com.umc.edison.ui.theme.Gray500
import com.umc.edison.ui.theme.Gray700
import com.umc.edison.ui.theme.Gray800
import com.umc.edison.ui.theme.Gray900
import com.umc.edison.ui.theme.White000
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.umc.edison.R
import com.umc.edison.ui.theme.EdisonTypography
import com.umc.edison.ui.theme.EmptyViewCtaBg
import com.umc.edison.ui.theme.GradientBlue
import com.umc.edison.ui.theme.GradientPink
import com.umc.edison.ui.theme.GradientYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BubbleStorageScreen(
    navHostController: NavHostController,
    updateShowBottomNav: (Boolean) -> Unit,
    searchResults: List<BubbleModel>,
    searchKeyword: String,
    updateViewMode: (Boolean) -> Unit,
    viewModel: BubbleStorageViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val onboardingState by viewModel.onboardingState.collectAsState()
    val baseState by viewModel.baseState.collectAsState()
    val context = LocalContext.current


    LaunchedEffect(Unit) {
        updateShowBottomNav(true)
        updateViewMode(false)

        viewModel.fetchStorageBubbles()
    }

    BackHandler(enabled = true) {
        if (uiState.mode == BubbleStorageMode.NONE) {
            (context as? Activity)?.finish()
        } else {
            viewModel.updateEditMode(BubbleStorageMode.NONE)
            updateViewMode(false)
            updateShowBottomNav(true)
        }
    }

    BaseContent(
        baseState = baseState,
        bottomBar = {
            if (uiState.mode == BubbleStorageMode.EDIT) {
                BottomSheetForDelete(
                    selectedCnt = uiState.selectedBubbles.size,
                    showSelectedCnt = true,
                    onButtonClick = {
                        viewModel.updateEditMode(BubbleStorageMode.SHARE)
                        updateViewMode(false)
                    },
                    onDelete = {
                        viewModel.updateEditMode(BubbleStorageMode.DELETE)
                        updateViewMode(false)
                    },
                    buttonEnabled = uiState.selectedBubbles.isNotEmpty(),
                    buttonText = "공유하기",
                )
            }
        },
    ) {
        if (uiState.bubbles.isEmpty()) {
            BubbleStorageEmptyView(
                onCtaClick = {
                    navHostController.navigate(NavRoute.BubbleEdit.createRoute())
                }
            )
            return@BaseContent
        }

        var onBubbleClick: (BubbleModel) -> Unit = {}
        var onBubbleLongClick: (BubbleModel) -> Unit = {}

        if (uiState.mode == BubbleStorageMode.EDIT) {
            onBubbleClick = { bubble ->
                viewModel.toggleSelectBubble(bubble)
            }
        } else if (uiState.mode == BubbleStorageMode.NONE) {
            onBubbleClick = { bubble ->
                viewModel.selectBubble(bubble)
                viewModel.updateEditMode(BubbleStorageMode.VIEW)
                updateViewMode(true)
                val bubbleSize = calculateBubbleSize(bubble)

                if (bubbleSize == BubbleType.BubbleDoor) {
                    updateShowBottomNav(false)
                }
            }
            onBubbleLongClick = { bubble ->
                viewModel.selectBubble(bubble)
                viewModel.updateEditMode(BubbleStorageMode.EDIT)
                updateViewMode(false)
                updateShowBottomNav(false)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    if (uiState.mode == BubbleStorageMode.EDIT) {
                        viewModel.updateEditMode(BubbleStorageMode.NONE)
                        updateViewMode(false)
                        updateShowBottomNav(true)
                    }
                }
        ) {
            BubblesLayout(
                bubbles = if (searchKeyword.isEmpty() || searchResults.isEmpty()) uiState.bubbles else searchResults,
                onBubbleClick = onBubbleClick,
                onBubbleLongClick = onBubbleLongClick,
                isBlur = uiState.mode != BubbleStorageMode.NONE,
                selectedBubble = uiState.selectedBubbles,
                searchKeyword = searchKeyword,
                isReversed = true,
                setGlobalBubblePosition = { offset, size ->
                    viewModel.setBubbleBound(offset, size)
                }
            )

            if (searchKeyword.isEmpty() || searchResults.isEmpty()) {
                // Linear Gradient 효과가 적용된 배경
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val gradientHeight = size.height * 0.2f

                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(White000, White000.copy(alpha = 0f)),
                            startY = 0f,
                            endY = gradientHeight
                        ),
                        size = Size(size.width, gradientHeight)
                    )

                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(White000.copy(alpha = 0f), White000),
                            startY = size.height - gradientHeight,
                            endY = size.height
                        ),
                        topLeft = Offset(0f, size.height - gradientHeight),
                        size = Size(size.width, gradientHeight)
                    )
                }
            }
        }

        if (uiState.mode == BubbleStorageMode.VIEW && uiState.selectedBubbles.isNotEmpty()) {
            val bubble = uiState.selectedBubbles.first()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Gray800.copy(alpha = 0.5f))
                    .clickable(onClick = {
                        viewModel.updateEditMode(BubbleStorageMode.NONE)
                        updateViewMode(false)
                        updateShowBottomNav(true)
                    })
                    .padding(top = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Bubble(
                    bubble = bubble,
                    onBubbleClick = {
                        navHostController.navigate(NavRoute.BubbleEdit.createRoute(bubble.id))
                    },
                    onLinkedBubbleClick = { linkedBubbleId ->
                        navHostController.navigate(NavRoute.BubbleEdit.createRoute(linkedBubbleId))
                    }
                )

                LabelTagList(
                    labels = bubble.labels,
                    modifier = Modifier.align(Alignment.BottomStart)
                )
            }
        } else if (uiState.mode == BubbleStorageMode.DELETE) {
            BottomSheetPopUp(
                title = "${uiState.selectedBubbles.size} 개의 버블을 삭제하시겠습니까?",
                cancelText = "취소",
                confirmText = "삭제",
                onDismiss = {
                    viewModel.updateEditMode(BubbleStorageMode.EDIT)
                    updateViewMode(false)
                },
                onConfirm = {
                    viewModel.deleteSelectedBubbles(showBottomNav = updateShowBottomNav)
                },
            )
        } else if (uiState.mode == BubbleStorageMode.SHARE) {
            BottomSheet(
                onDismiss = {
                    viewModel.updateEditMode(BubbleStorageMode.EDIT)
                    updateViewMode(false)
                },
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    TextButton(
                        onClick = { viewModel.shareImages() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "이미지로 공유하기",
                                style = MaterialTheme.typography.titleLarge,
                                color = Gray900
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentWidth(Alignment.CenterHorizontally)
                    ) {
                        HorizontalDivider(
                            color = Gray300,
                            thickness = 1.dp,
                            modifier = Modifier.width(326.dp)
                        )
                    }

                    TextButton(
                        onClick = { viewModel.shareTexts() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 10.dp)

                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "텍스트로 공유하기",
                                style = MaterialTheme.typography.titleLarge,
                                color = Gray900
                            )
                        }
                    }
                }
            }
        }

        if (!onboardingState.edisonOnboardingShow && onboardingState.show && uiState.bubbles.isNotEmpty()) {
            BubbleSpaceOnboarding(
                onboardingState = onboardingState,
                onDismiss = {
                    viewModel.setHasSeenOnboarding()
                }
            )
        }
    }
}

@Composable
private fun BubbleStorageEmptyView(
    onCtaClick: () -> Unit,
    modifier: Modifier = Modifier,
) {

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.bubble_storage_empty_title),
            style = EdisonTypography.displayLarge,
            color = Gray700,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.bubble_storage_empty_subtitle),
            style = EdisonTypography.bodyMedium,
            color = Gray500,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(4.dp))

        val ctaTextBrush = Brush.horizontalGradient(
            colors = listOf(
                GradientYellow,
                GradientPink,
                GradientBlue,
            )
        )

        Surface(
            onClick = onCtaClick,
            shape = RoundedCornerShape(100.dp),
            color = EmptyViewCtaBg,
        ) {
            Text(
                text = stringResource(R.string.bubble_storage_empty_cta),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.displaySmall.copy(brush = ctaTextBrush),
                textAlign = TextAlign.Center,
            )
        }
    }
}
