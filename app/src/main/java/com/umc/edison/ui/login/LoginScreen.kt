package com.umc.edison.ui.login

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.umc.edison.R
import com.umc.edison.presentation.login.LoginViewModel
import com.umc.edison.ui.BaseContent
import com.umc.edison.ui.components.BasicFullButton
import com.umc.edison.ui.navigation.NavRoute
import com.umc.edison.ui.theme.Gray300
import com.umc.edison.ui.theme.Gray800
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.umc.edison.ui.theme.Gray700

@Composable
fun LoginScreen(
    navHostController: NavHostController,
    updateShowBottomNav: (Boolean) -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val context = LocalContext.current

    val baseState by viewModel.baseState.collectAsState()
    var showPopup by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        updateShowBottomNav(false)
    }

    BackHandler {
        (context as? Activity)?.finish()
    }

    BaseContent(
        baseState = baseState,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.weight(0.4f))

                    Image(
                        painter = painterResource(id = R.drawable.ic_big_bubble_logo),
                        contentDescription = "app logo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(192.7.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Image(
                        painter = painterResource(id = R.drawable.ic_text_logo),
                        contentDescription = "app logo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(width = 135.dp, height = 31.dp)
                    )

                    Spacer(modifier = Modifier.weight(0.6f))
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter), // 하단에 정렬
                verticalArrangement = Arrangement.spacedBy(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (showPopup) {
                        BubbleMessage(
                            Modifier.align(Alignment.TopCenter),
                            onDismiss = { showPopup = false })
                    }

                    Text(
                        text = "로그인하면 사용할 수 있는 기능",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray800,
                        modifier = Modifier
                            .padding(vertical = 5.dp)
                            .clickable { showPopup = true }
                    )
                }

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicFullButton(
                        text = "구글 로그인",
                        modifier = Modifier.fillMaxWidth(),
                        enabled = true,
                        onClick = {
                            viewModel.signInWithGoogle(
                                context = context,
                                navController = navHostController
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Gray300),
                        textStyle = TextStyle(color = Gray800)
                    )

                    Image(
                        painter = painterResource(id = R.drawable.google),
                        contentDescription = "google image",
                        modifier = Modifier.padding(start = 24.dp)
                    )
                }

                BasicFullButton(
                    text = "로그인 없이 긴급 시작",
                    enabled = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    onClick = { navHostController.navigate(NavRoute.TermsOfUse.createRoute(fromSignUp = false, idToken = "")) },
                )
            }
        }
    }
}

@Composable
fun BubbleMessage(modifier: Modifier, onDismiss: () -> Unit) {
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
            modifier = modifier
                .wrapContentSize()
                .shadow(8.dp, shape = RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(14.dp)
        ) {
            Column {
                Text(
                    text = "로그인하면 뭐가 좋을까요?",
                    style = MaterialTheme.typography.titleMedium,
                    color = Gray800,
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "나에게 맞는 버블 정리\n" +
                            "스페이스 기능 완벽 사용\n" +
                            "맞춤형 레터 추천 및 북마크 지원",
                    style = MaterialTheme.typography.labelLarge,
                    color = Gray700,
                )
            }
        }
    }
}
