package com.umc.edison.ui.edison

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.umc.edison.R
import com.umc.edison.presentation.edison.MyEdisonViewModel
import com.umc.edison.presentation.onboarding.OnboardingPositionState
import com.umc.edison.ui.BaseContent
import com.umc.edison.ui.bubblestorage.BubbleStorageScreen
import com.umc.edison.ui.components.BubbleInput
import com.umc.edison.ui.components.MyEdisonNavBar
import com.umc.edison.ui.label.LabelTabScreen
import com.umc.edison.ui.login.PrefsHelper
import com.umc.edison.ui.navigation.NavRoute
import com.umc.edison.ui.onboarding.MyEdisonOnboarding
import kotlinx.coroutines.launch

@Composable
fun MyEdisonScreen(
    navController: NavHostController,
    updateShowBottomNav: (Boolean) -> Unit,
    bottomNavBarBounds: List<OnboardingPositionState>,
    viewModel: MyEdisonViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val baseState by viewModel.baseState.collectAsState()
    val onboardingState by viewModel.onboardingState.collectAsState()
    var isViewMode by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val pagerState = rememberPagerState(
        pageCount = { 3 },
        initialPageOffsetFraction = 0f,
        initialPage = 0,
    )

    BackHandler {
        (context as? Activity)?.finish()
    }

    LaunchedEffect(Unit) {
        viewModel.fetchBubbles()
        updateShowBottomNav(true)
        PrefsHelper.setMainScreenVisited(context)
    }

    LaunchedEffect(uiState.bubbles) {
        if (pagerState.currentPage == 0 && uiState.bubbles.isNotEmpty()) {
            pagerState.scrollToPage(1)
        }
    }

    BaseContent(
        baseState = baseState,
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            when (page) {


                0 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Spacer(modifier = Modifier.weight(0.6f))

                        BubbleInput(
                            modifier = Modifier
                                .onGloballyPositioned { coordinates ->
                                    viewModel.setBubbleInputBound(
                                        offset = coordinates.positionOnScreen(),
                                        size = coordinates.size,
                                    )
                                },
                            onClick = { navController.navigate(NavRoute.BubbleEdit.createRoute("")) },
                        )

                        Spacer(modifier = Modifier.weight(1f))
                    }
                }

                1 -> {
                    if (onboardingState.show) {
                        val context = LocalContext.current
                        val imageRequest = ImageRequest.Builder(context)
                            .data(R.drawable.bubble_ex)
                            .crossfade(true)
                            .build()
                        AsyncImage(
                            model = imageRequest,
                            contentDescription = "Bubble Example",
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            alignment = Alignment.Center,
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        )
                    } else {
                        BubbleStorageScreen(
                            navHostController = navController,
                            updateShowBottomNav = updateShowBottomNav,
                            searchResults = uiState.searchResults,
                            searchKeyword = uiState.query,
                            updateViewMode = { flag -> isViewMode = flag },
                        )
                    }
                }


                2 -> {
                    LabelTabScreen(
                        navHostController = navController,
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(15.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            MyEdisonNavBar(

                onBubbleClick = {
                    coroutineScope.launch {
                        pagerState.scrollToPage(0)
                    }
                    viewModel.resetSearchResults()
                },
                onStorageClick = {
                    coroutineScope.launch {
                        pagerState.scrollToPage(1)
                    }
                    viewModel.resetSearchResults()
                },
                onLabelClick = {
                    coroutineScope.launch {
                        pagerState.scrollToPage(2)
                    }
                    viewModel.resetSearchResults()
                },

                currentPage = pagerState.currentPage,
                isViewMode = isViewMode,
                setNavBarPosition = { idx: Int, offset: Offset, size: IntSize ->
                    viewModel.setNavBarPosition(
                        idx = idx,
                        offset = offset,
                        size = size
                    )
                },
            )
        }
    }

    if (onboardingState.show) {
        MyEdisonOnboarding(
            onboardingState = onboardingState,
            bottomNavBarBounds = bottomNavBarBounds,
            changeToStorageMode = {
                coroutineScope.launch {
                    pagerState.scrollToPage(1)
                }
            },
            changeToBubbleInputMode = {
                coroutineScope.launch {
                    pagerState.scrollToPage(0)
                }
            },
            onDismiss = {
                viewModel.setHasSeenOnboarding()
            },
        )
    }
}
