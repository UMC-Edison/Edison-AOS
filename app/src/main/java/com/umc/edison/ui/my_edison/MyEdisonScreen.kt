package com.umc.edison.ui.my_edison

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.umc.edison.databinding.FragmentMyEdisonBinding
import com.umc.edison.ui.theme.EdisonTheme

@Composable
fun MyEdisonScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        AndroidViewBinding(FragmentMyEdisonBinding::inflate) {
            titleTv.text = "Composable 함수에 AndroidViewBinding 사용하기"

            titleTv.setOnClickListener {
                titleTv.text = "클릭 리스너"
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BubbleSpaceScreenPreview() {
    EdisonTheme {
        MyEdisonScreen()
    }
}
