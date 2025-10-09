package com.umc.edison.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.umc.edison.ui.theme.Gray300
import com.umc.edison.ui.theme.Gray600
import com.umc.edison.ui.theme.Gray800


@Composable
fun KeywordMapBar(
    value: String,
    onValueChange: (String) -> Unit,
    onMap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(Gray300)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onMap() }),
                textStyle = MaterialTheme.typography.bodySmall.copy(color = Gray800),
                modifier = Modifier.weight(1f),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Box(
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (value.isEmpty()) {
                            Text(
                                text = "키워드를 입력하세요",
                                style = MaterialTheme.typography.bodySmall,
                                color = Gray600
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(100.dp))
                .background(Color.White)
                .border(
                    2.dp,
                    Brush.horizontalGradient(listOf(Color(0xFFFFFBE4), Color(0xFFFF6FDF), Color(0xFF85C9FF))),
                    RoundedCornerShape(100.dp)
                )
                .clickable { onMap() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "키워드로 매핑",
                style = MaterialTheme.typography.titleSmall,
                color = Gray800,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 15.dp)
            )
        }
    }
}