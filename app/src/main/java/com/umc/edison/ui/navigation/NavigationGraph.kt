package com.umc.edison.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.umc.edison.ui.art_letter.ArtLetterScreen
import com.umc.edison.ui.my_edison.MyEdisonScreen
import com.umc.edison.ui.mypage.MyPageScreen
import com.umc.edison.ui.space.BubbleSpaceScreen

@Composable
fun NavigationGraph(navHostController: NavHostController) {
    NavHost(navHostController, startDestination = BottomNavItem.MyEdison.route) {
        composable(BottomNavItem.MyEdison.route) {
            MyEdisonScreen()
        }
        composable(BottomNavItem.Space.route) {
            BubbleSpaceScreen()
        }
        composable(BottomNavItem.Bubble.route) {
//            BubbleScreen()
        }
        composable(BottomNavItem.ArtLetter.route) {
            ArtLetterScreen()
        }
        composable(BottomNavItem.MyPage.route) {
            MyPageScreen()
        }
    }
}