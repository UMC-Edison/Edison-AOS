package com.umc.edison.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.umc.edison.R
import com.umc.edison.ui.theme.Gray300
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.IntSize
import com.umc.edison.ui.theme.Gray600
import com.umc.edison.ui.theme.Gray800
import com.umc.edison.ui.theme.White000

@Composable
fun MyEdisonNavBar(
    onLabelClick: () -> Unit,
    onBubbleClick: () -> Unit,
    onStorageClick: () -> Unit,
    currentPage: Int,
    isViewMode: Boolean,
    setNavBarPosition: (Int, Offset, IntSize) -> Unit,
) {


    if (!isViewMode) {
        Box(
            modifier = Modifier
                .wrapContentSize()
                .clip(RoundedCornerShape(30.dp))
                .background(Gray300),
        ) {
            Row(
                modifier = Modifier.padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {




                Box(
                    modifier = Modifier.wrapContentSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (currentPage == 0) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(30.dp))
                                .background(White000)
                        )
                    }

                    IconButton(
                        onClick = {
                            onBubbleClick()
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .onGloballyPositioned { coordinates ->
                                setNavBarPosition(
                                    0,
                                    coordinates.positionOnScreen(),
                                    coordinates.size
                                )
                            }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_topbar_bubble),
                            contentDescription = "Profile Icon",
                            tint = Color.Unspecified
                        )
                    }
                }

                Box(
                    modifier = Modifier.wrapContentSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (currentPage == 1) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(30.dp))
                                .background(White000)
                        )
                    }

                    IconButton(
                        onClick = {
                            onStorageClick()
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .onGloballyPositioned { coordinates ->
                                setNavBarPosition(
                                    1,
                                    coordinates.positionOnScreen(),
                                    coordinates.size
                                )
                            }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_topbar_storage),
                            contentDescription = "Compass Icon",
                            tint = Color.Unspecified
                        )
                    }
                }

                Box(
                    modifier = Modifier.wrapContentSize(),
                    contentAlignment = Alignment.Center
                ) {

                    if (currentPage == 2) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(30.dp))
                                .background(White000)
                        )
                    }
                    IconButton(
                        onClick = {
                            onLabelClick()
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .onGloballyPositioned { coordinates ->
                                setNavBarPosition(
                                    2,
                                    coordinates.positionOnScreen(),
                                    coordinates.size
                                )
                            }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_tag),
                            contentDescription = "Label Icon",
                            tint = Gray800
                        )
                    }
                }
            }
        }
    }
}
