package com.umc.edison.ui.space

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.umc.edison.ui.theme.EdisonTheme
import com.umc.edison.ui.theme.Gray300
import com.umc.edison.ui.theme.Gray800
import kotlinx.coroutines.launch

@Composable
fun BubbleSpaceScreen() {
    val tabs = listOf("스페이스", "라벨")
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val pagerState = rememberPagerState(
        pageCount = { tabs.size },
        initialPageOffsetFraction = 0f,
        initialPage = 0,
    )
    val coroutineScope = rememberCoroutineScope()

    val indicatorOffset by animateDpAsState(
        targetValue = (192.dp / tabs.size) * selectedTabIndex,
        label = "Indicator Animation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Spacer(modifier = Modifier.height(33.dp))

        Row(
            modifier = Modifier
                .size(width = 192.dp, height = 28.dp)
                .align(Alignment.CenterHorizontally),
        ) {
            tabs.forEachIndexed { index, text ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(100.dp))
                        .clickable {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                                selectedTabIndex = index
                            }
                        }
                        .padding(4.dp)
                        .wrapContentSize(Alignment.Center)
                ) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.titleMedium,
                        color = Gray800,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

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
                    .offset {
                        IntOffset(
                            x = indicatorOffset.roundToPx(),
                            y = 0
                        )
                    }
                    .clip(RoundedCornerShape(100.dp))
                    .background(Gray800)
            )
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            selectedTabIndex = pagerState.currentPage
            when (page) {
                0 -> SpaceTabScreen()
                1 -> LabelTabScreen()
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun BubbleSpaceScreenPreview() {
    EdisonTheme {
        BubbleSpaceScreen()
    }
}
