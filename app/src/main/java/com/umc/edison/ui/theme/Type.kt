package com.umc.edison.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.umc.edison.R

val Pretendard = FontFamily(
    Font(R.font.pretendard_thin, FontWeight.Thin),
    Font(R.font.pretendard_extra_light, FontWeight.ExtraLight),
    Font(R.font.pretendard_light, FontWeight.Light),
    Font(R.font.pretendard_regular, FontWeight.Normal),
    Font(R.font.pretendard_medium, FontWeight.Medium),
    Font(R.font.pretendard_semi_bold, FontWeight.SemiBold),
    Font(R.font.pretendard_bold, FontWeight.Bold),
    Font(R.font.pretendard_extra_bold, FontWeight.ExtraBold),
    Font(R.font.pretendard_black, FontWeight.Black)
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
    ),
    displayMedium = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Medium,
        fontSize = 40.sp,
    ),
    displaySmall = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Normal,
        fontSize = 40.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 22.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 19.sp
    ),
    titleLarge = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    titleMedium = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 17.sp
    ),
    titleSmall = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 14.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 17.sp
    ),
    bodySmall = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Light,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    labelMedium = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Light,
        fontSize = 14.sp,
        lineHeight = 17.sp
    ),
    labelSmall = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Light,
        fontSize = 12.sp,
        lineHeight = 14.sp
    )
)
