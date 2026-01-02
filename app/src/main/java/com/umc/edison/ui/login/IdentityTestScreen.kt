package com.umc.edison.ui.login

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.umc.edison.presentation.login.IdentityTestViewModel
import com.umc.edison.ui.BaseContent
import com.umc.edison.ui.components.BasicFullButton
import com.umc.edison.ui.theme.Gray800
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.IntOffset
import com.umc.edison.ui.components.KeywordChip
import com.umc.edison.ui.theme.Gray300
import com.umc.edison.ui.theme.Gray500
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun IdentityTestScreen(
    navHostController: NavHostController,
    updateShowBottomNav: (Boolean) -> Unit,
    viewModel: IdentityTestViewModel = hiltViewModel(),
) {
    val baseState by viewModel.baseState.collectAsState()

    val scrollState = rememberScrollState()

    val tabs = listOf("아이덴티티 테스트1", "아이덴티티 테스트2", "아이덴티티 테스트3", "아이덴티티 테스트4")
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val pagerState = rememberPagerState(
        pageCount = { tabs.size },
        initialPageOffsetFraction = 0f,
        initialPage = 0,
    )

    LaunchedEffect(pagerState.currentPage) {
        viewModel.updateTabIndex(pagerState.currentPage)
    }

    val coroutineScope = rememberCoroutineScope()
    val indicatorOffset by animateDpAsState(
        targetValue = (192.dp / tabs.size) * selectedTabIndex,
        label = "Indicator Animation"
    )

    LaunchedEffect(Unit) {
        updateShowBottomNav(false)
    }

    BaseContent(
        baseState = baseState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(33.dp))

            Box(
                modifier = Modifier
                    .height(4.dp)
                    .size(width = 192.dp, height = 4.dp)
                    .align(Alignment.CenterHorizontally)
                    .clip(RoundedCornerShape(100.dp))
                    .background(Gray300)
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 192.dp / tabs.size, height = 4.dp)
                        .align(Alignment.CenterStart)
                        .offset { IntOffset(x = indicatorOffset.roundToPx(), y = 0) }
                        .clip(RoundedCornerShape(100.dp))
                        .background(Gray800)
                )
            }


            Box(
                modifier = Modifier.weight(1f)
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = false
                ) { page ->
                    selectedTabIndex = pagerState.currentPage
                    when (page) {
                        0 -> IdentityTest1(pagerState, coroutineScope)
                        1 -> IdentityTest2(pagerState, coroutineScope)
                        2 -> IdentityTest3(pagerState, coroutineScope)
                        3 -> IdentityTest4(navHostController, pagerState, coroutineScope)
                    }
                }
            }
        }
    }
}

@Composable
fun IdentityTest1(
    pagerState: PagerState,
    coroutineScope: CoroutineScope,
    viewModel: IdentityTestViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val baseState by viewModel.baseState.collectAsState()

    BaseContent(
        baseState = baseState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.Top,
        ) {
            Text(
                text = uiState.currentIdentity.question,
                color = Gray800,
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.padding(top = 30.dp, bottom = 48.dp)
            )

            FlowRow(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 48.dp)

            ) {
                uiState.currentIdentity.options.forEach { keyword ->
                    KeywordChip(
                        keyword = keyword.name,
                        isSelected = uiState.currentIdentity.selectedKeywords.contains(keyword),
                        onClick = { viewModel.toggleIdentityKeyword(keyword) }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            BasicFullButton(
                text = "다음으로",
                enabled = true,
                onClick = {
                    viewModel.setIdentityTestResult(pagerState, coroutineScope)
                },
            )
        }
    }
}

@Composable
fun IdentityTest2(
    pagerState: PagerState,
    coroutineScope: CoroutineScope,
    viewModel: IdentityTestViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
    ) {
        Text(
            text = uiState.currentIdentity.question,
            color = Gray800,
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.padding(top = 30.dp, bottom = 12.dp)
        )

        Text(
            text = uiState.currentIdentity.questionTip ?: "",
            color = Gray500,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        FlowRow(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 48.dp)
        ) {
            uiState.currentIdentity.options.forEach { keyword ->
                KeywordChip(
                    keyword = keyword.name,
                    isSelected = uiState.currentIdentity.selectedKeywords.contains(keyword),
                    onClick = { viewModel.toggleIdentityKeyword(keyword) }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        BasicFullButton(
            text = "다음으로",
            enabled = true,
            onClick = {
                viewModel.setIdentityTestResult(pagerState, coroutineScope)
            },
        )
    }
}

@Composable
fun IdentityTest3(
    pagerState: PagerState,
    coroutineScope: CoroutineScope,
    viewModel: IdentityTestViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
    ) {
        Text(
            text = uiState.currentIdentity.question,
            color = Gray800,
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.padding(top = 30.dp, bottom = 48.dp)
        )

        FlowRow(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 48.dp)
        ) {
            uiState.currentIdentity.options.forEach { keyword ->
                KeywordChip(
                    keyword = keyword.name,
                    isSelected = uiState.currentIdentity.selectedKeywords.contains(keyword),
                    onClick = { viewModel.toggleIdentityKeyword(keyword) }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        BasicFullButton(
            text = "다음으로",
            enabled = true,
            onClick = {
                viewModel.setIdentityTestResult(pagerState, coroutineScope)
            },
        )
    }
}
@Composable
fun IdentityTest4(
    navHostController: NavHostController,
    pagerState: PagerState,
    coroutineScope: CoroutineScope,
    viewModel: IdentityTestViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentIdentity = uiState.currentIdentity

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
    ) {
        Text(
            text = currentIdentity.question,
            color = Gray800,
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.padding(top = 30.dp, bottom = 12.dp)
        )

        currentIdentity.questionTip?.let { tip ->
            Text(
                text = tip,
                color = Gray500,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 48.dp)
            )
        }

        FlowRow(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 48.dp)
        ) {
            currentIdentity.options.forEach { keyword ->
                KeywordChip(
                    keyword = keyword.name,
                    isSelected = currentIdentity.selectedKeywords.contains(keyword),
                    onClick = { viewModel.toggleIdentityKeyword(keyword) }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        BasicFullButton(
            text = "다음으로",
            enabled = true,
            onClick = {
                viewModel.submitIdentityTestResult(navHostController)
                coroutineScope.launch {
                    if (pagerState.currentPage < 3) {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                }
            },
        )
    }
}


