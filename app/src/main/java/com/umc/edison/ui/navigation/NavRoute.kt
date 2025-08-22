package com.umc.edison.ui.navigation

sealed class NavRoute(val route: String) {

    companion object {
        const val MY_EDISON_ROUTE = "my-edison"
        const val SPACE_ROUTE = "space"
        const val ART_LETTER_ROUTE = "art-letter"
        const val ART_LETTER_SEARCH_ROUTE = "art-letter/search"
        const val MY_PAGE_ROUTE = "my-page"
        const val SPACE_LABEL_ROUTE = "space/labels"
        const val SPLASH_ROUTE = "login/splash"
        const val LOGIN_ROUTE = "login"
        const val MAKE_NICKNAME_ROUTE = "login/make-nick-name"
        const val IDENTITY_TEST_ROUTE = "login/identity-test"
        const val TERMS_OF_USE_ROUTE = "login/terms-of-use"
        const val SCRAP_BOARD_ROUTE = "my-page/scrap-board"
    }

    /**
     * Base Routes
     */
    data object MyEdison : NavRoute(MY_EDISON_ROUTE)
    data object Space : NavRoute(SPACE_ROUTE)
    data object ArtLetter : NavRoute(ART_LETTER_ROUTE)
    data object ArtLetterSearch : NavRoute(ART_LETTER_SEARCH_ROUTE)
    data object MyPage : NavRoute(MY_PAGE_ROUTE)
    data object Splash : NavRoute(SPLASH_ROUTE)
    data object Login : NavRoute(LOGIN_ROUTE)
    data object MakeNickName : NavRoute(MAKE_NICKNAME_ROUTE)
    data object IdentityTest : NavRoute(IDENTITY_TEST_ROUTE)
    data object TermsOfUse : NavRoute(TERMS_OF_USE_ROUTE)
    data object ScrapBoard : NavRoute(SCRAP_BOARD_ROUTE)

    /**
     * Dynamic Routes
     */


    data object ArtLetterDetail : NavRoute(ART_LETTER_ROUTE) {
        fun createRoute(id: Int): String = "$route?artLetterId=$id"
    }

    data object LabelDetail : NavRoute("$SPACE_LABEL_ROUTE/detail") {
        fun createRoute(labelId: String?): String {
            return if (labelId != null) {
                "$route?labelId=$labelId"
            } else {
                route
            }
        }
    }

    data object ScrapBoardDetail : NavRoute("$SCRAP_BOARD_ROUTE/categories") {
        fun createRoute(categoryName: String): String = "$route?name=$categoryName"
    }

    data object BubbleEdit : NavRoute("$MY_EDISON_ROUTE/edit") {
        fun createRoute(bubbleId: String?): String {
            return if (bubbleId != null) {
                "$route?bubbleId=$bubbleId"
            } else {
                route
            }
        }
    }

    data object IdentityEdit : NavRoute("$MY_PAGE_ROUTE/identity") {
        fun createRoute(identityId: Int): String = "$route?identityId=$identityId"
    }

    data object ProfileEdit : NavRoute("$MY_PAGE_ROUTE/profile-edit")
    data object Menu : NavRoute("$MY_PAGE_ROUTE/menu")
    data object Trash : NavRoute("$MY_PAGE_ROUTE/trash")
    data object AccountManagement : NavRoute("$MY_PAGE_ROUTE/account-management")
    data object DeleteAccount : NavRoute("$MY_PAGE_ROUTE/delete-account")
}
