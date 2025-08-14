package com.umc.edison.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.umc.edison.R
import com.umc.edison.ui.navigation.NavRoute
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController, updateShowBottomNav: (Boolean) -> Unit) {

    val context = LocalContext.current

    LaunchedEffect(Unit) {

        updateShowBottomNav(false)

        delay(2000)

        if (PrefsHelper.hasVisitedMainScreen(context)) {
            navController.navigate(NavRoute.MyEdison.route) {
                popUpTo("splash") { inclusive = true }
            }
        } else {
            navController.navigate(NavRoute.Login.route) {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White),
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
}
