package com.umc.edison.ui.space

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.umc.edison.ui.theme.EdisonTheme

@Composable
fun LabelTabScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "라벨 화면")
    }
}

@Preview(showBackground = true)
@Composable
fun LabelTabScreenPreview() {
    EdisonTheme {
        LabelTabScreen()
    }
}