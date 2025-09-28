package com.umc.edison.ui.space

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.umc.edison.R
import com.umc.edison.presentation.model.BubbleModel
import com.umc.edison.presentation.space.BubbleSpaceMode
import com.umc.edison.presentation.space.BubbleSpaceViewModel
import com.umc.edison.ui.BaseContent
import com.umc.edison.ui.NeedLoginScreen
import com.umc.edison.ui.components.Bubble
import com.umc.edison.ui.components.BubbleType
import com.umc.edison.ui.components.BubblesLayout
import com.umc.edison.ui.components.LabelTagList
import com.umc.edison.ui.components.SearchBar
import com.umc.edison.ui.components.calculateBubbleSize
import com.umc.edison.ui.label.LabelTabScreen
import com.umc.edison.ui.navigation.NavRoute
import com.umc.edison.ui.theme.Gray100
import com.umc.edison.ui.theme.Gray200
import com.umc.edison.ui.theme.Gray300
import com.umc.edison.ui.theme.Gray800
import com.umc.edison.ui.theme.Gray900
import kotlinx.coroutines.launch

@Composable
fun BubbleSpaceScreen(
    navHostController: NavHostController,
    updateShowBottomNav: (Boolean) -> Unit,
    viewModel: BubbleSpaceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val baseState by viewModel.baseState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.checkLoginState()
        updateShowBottomNav(true)
    }


    BackHandler {
        if (uiState.selectedBubble != null) {
            viewModel.selectBubble(null)
            updateShowBottomNav(true)
        } else if (uiState.mode == BubbleSpaceMode.SEARCH) {
            viewModel.updateBubbleSpaceMode(BubbleSpaceMode.DEFAULT)
            viewModel.updateQuery("")
        }
        else {
            navHostController.navigate(NavRoute.MyEdison.route)
        }
    }


    BaseContent(
        baseState = baseState,
    ) {
        if (uiState.isLoggedIn) {
            val onShowBubble: (BubbleModel) -> Unit = { bubble ->
                viewModel.selectBubble(bubble)
                val bubbleSize = calculateBubbleSize(bubble)
                if (bubbleSize == BubbleType.BubbleDoor) {
                    updateShowBottomNav(false)
                }
            }

            if (uiState.mode == BubbleSpaceMode.DEFAULT) {
                BubbleGraphScreen(
                    showBubble = onShowBubble,
                    onShowKeywordMap = {
                        viewModel.showKeywordMap()                     }
                )
            } else if (uiState.mode == BubbleSpaceMode.KEYWORD) {
                KeywordMapScreen(
                    showBubble = onShowBubble,
                    onNavigateBack = { viewModel.switchToGraph() }
                )
            }
        } else {
            NeedLoginScreen(
                navHostController = navHostController,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 18.dp, top = 66.dp, end = 18.dp),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Row(
                modifier = Modifier
                    .wrapContentHeight()
                    .padding(start = 24.dp, top = 30.dp, end = 24.dp)
                    .align(Alignment.CenterHorizontally)
                    .zIndex(1f),
            ) {
                if (uiState.mode == BubbleSpaceMode.DEFAULT) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_topbar_search),
                        contentDescription = "Search",
                        tint = Gray800,
                        modifier = Modifier
                            .size(32.dp)
                            .clickable {
                                viewModel.updateBubbleSpaceMode(BubbleSpaceMode.SEARCH)
                            }
                    )
                }
                Spacer(modifier = Modifier.weight(1f))

                Spacer(modifier = Modifier.size(32.dp))
            }
        }

        if (uiState.mode == BubbleSpaceMode.SEARCH) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5F5F5).copy(alpha = 0.8f))
                    .blur(10.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 20.dp),
            ) {
                // 검색 바
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(horizontal = 24.dp),
                ) {
                    SearchBar(
                        value = uiState.query,
                        onValueChange = { newQuery ->
                            viewModel.updateQuery(newQuery)
                        },
                        onSearch = {
                            viewModel.searchBubbles()
                        },
                        placeholder = "찰나의 영감을 검색해보세요",
                    )
                }

                // 검색 결과
                BubblesLayout(
                    bubbles = uiState.searchResults,
                    onBubbleClick = { bubble ->
                        viewModel.selectBubble(bubble)
                    },
                    searchKeyword = uiState.query
                )
            }


        }

        if (uiState.selectedBubble != null) {
            val bubble = uiState.selectedBubble!!
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Gray800.copy(alpha = 0.5f))
                    .clickable(onClick = {
                        viewModel.selectBubble(null)
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
        }
    }
}