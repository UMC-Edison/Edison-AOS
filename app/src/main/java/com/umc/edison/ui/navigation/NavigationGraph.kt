package com.umc.edison.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.umc.edison.presentation.onboarding.OnboardingPositionState
import com.umc.edison.ui.artboard.ArtLetterHomeScreen
import com.umc.edison.ui.artboard.ArtLetterDetailScreen
import com.umc.edison.ui.artboard.ArtLetterSearchScreen
import com.umc.edison.ui.edison.MyEdisonScreen
import com.umc.edison.ui.mypage.MyPageScreen
import com.umc.edison.ui.space.BubbleSpaceScreen
import com.umc.edison.ui.edison.BubbleInputScreen
import com.umc.edison.ui.label.LabelDetailScreen
import com.umc.edison.ui.login.IdentityTestScreen
import com.umc.edison.ui.login.LoginScreen
import com.umc.edison.ui.login.MakeNickNameScreen
import com.umc.edison.ui.login.SplashScreen
import com.umc.edison.ui.login.TermsOfUseScreen
import com.umc.edison.ui.mypage.AccountManagementScreen
import com.umc.edison.ui.mypage.DeleteAccountScreen
import com.umc.edison.ui.mypage.EditProfileScreen
import com.umc.edison.ui.mypage.IdentityEditScreen
import com.umc.edison.ui.mypage.MenuScreen
import com.umc.edison.ui.mypage.ScrapBoardDetailScreen
import com.umc.edison.ui.mypage.ScrapBoardScreen
import com.umc.edison.ui.mypage.TrashScreen

@Composable
fun NavigationGraph(
    navHostController: NavHostController,
    updateShowBottomNav: (Boolean) -> Unit,
    bottomNavBarBounds: List<OnboardingPositionState>
) {
    NavHost(navHostController, startDestination = NavRoute.Splash.route) {
        composable(NavRoute.MyEdison.route) {
            MyEdisonScreen(navHostController, updateShowBottomNav, bottomNavBarBounds)
        }

        composable(NavRoute.Space.route) {
            BubbleSpaceScreen(navHostController, updateShowBottomNav)
        }

        composable(NavRoute.ArtLetter.route) {
            ArtLetterHomeScreen(navHostController)
        }

        composable(NavRoute.ArtLetterSearch.route) {
            ArtLetterSearchScreen(navHostController)
        }

        composable(
            route = "${NavRoute.ArtLetter.route}?artLetterId={artLetterId}",
            arguments = listOf(navArgument("artLetterId") { type = NavType.IntType })
        ) {
            ArtLetterDetailScreen(navHostController)
        }

        composable(NavRoute.MyPage.route) {
            MyPageScreen(navHostController, updateShowBottomNav)
        }

        composable(NavRoute.ProfileEdit.route) {
            EditProfileScreen(navHostController, updateShowBottomNav)
        }

        composable(NavRoute.Menu.route) {
            MenuScreen(navHostController, updateShowBottomNav)
        }

        composable(NavRoute.Trash.route) {
            TrashScreen(navHostController, updateShowBottomNav)
        }

        composable(NavRoute.AccountManagement.route) {
            AccountManagementScreen(navHostController, updateShowBottomNav)
        }

        composable(NavRoute.DeleteAccount.route) {
            DeleteAccountScreen(navHostController, updateShowBottomNav)
        }

        composable(NavRoute.ScrapBoard.route) {
            ScrapBoardScreen(navHostController)
        }

        composable(
            route = "${NavRoute.ScrapBoard.route}/categories?name={name}",
            arguments = listOf(navArgument("name") { type = NavType.StringType })
        ) {
            ScrapBoardDetailScreen(navHostController)
        }

        composable(NavRoute.Login.route) {
            LoginScreen(navHostController, updateShowBottomNav)
        }

        composable(NavRoute.Splash.route) {
            SplashScreen(navHostController, updateShowBottomNav)
        }

        composable(NavRoute.MakeNickName.route) {
            MakeNickNameScreen(navHostController, updateShowBottomNav)
        }

        composable(NavRoute.IdentityTest.route) {
            IdentityTestScreen(navHostController, updateShowBottomNav)
        }

        composable(NavRoute.TermsOfUse.route) {
            TermsOfUseScreen(navHostController, updateShowBottomNav)
        }

        composable(
            route = "${NavRoute.IdentityEdit.route}?identityId={identityId}",
            arguments = listOf(navArgument("identityId") { type = NavType.IntType })
        ) {
            IdentityEditScreen(navHostController, updateShowBottomNav)
        }

        composable(
            route = "${NavRoute.LabelDetail.route}?labelId={labelId}",
            arguments = listOf(navArgument("labelId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) {
            LabelDetailScreen(navHostController, updateShowBottomNav)
        }

        composable(
            route = "${NavRoute.BubbleEdit.route}?bubbleId={bubbleId}",
            arguments = listOf(navArgument("bubbleId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) {
            BubbleInputScreen(navHostController, updateShowBottomNav)
        }
    }
}
