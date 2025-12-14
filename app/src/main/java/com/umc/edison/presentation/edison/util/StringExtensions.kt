package com.umc.edison.presentation.edison.util

import android.text.Html

/**
 * HTML 문자열을 일반 텍스트로 파싱하는 확장 함수입니다.
 * Android의 Html.fromHtml을 사용하여 HTML 엔티티를 디코딩합니다.
 * @return 파싱된 일반 텍스트 문자열
 */
fun String.parseHtml(): String {
    return Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY).toString()
}