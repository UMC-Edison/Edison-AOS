package com.umc.edison.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.umc.edison.R
import com.umc.edison.ui.theme.Gray300
import com.umc.edison.ui.theme.Gray600
import com.umc.edison.ui.theme.Gray800

@Composable
fun SearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit,
    placeholder: String = "",
    showKeywordMappingButton: Boolean = false,
    onKeywordMappingClick: (String) -> Unit = {}
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        textStyle = MaterialTheme.typography.bodySmall,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(RoundedCornerShape(100.dp))
            .background(Gray300)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_topbar_search),
                    contentDescription = "Search",
                    tint = Gray800,
                    modifier = Modifier.size(32.dp)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                ){
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = Gray600,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }

                    innerTextField()
                }

                if(showKeywordMappingButton){

                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clip(RoundedCornerShape(100.dp))
                            .background(Color.White)
                            .clickable { onKeywordMappingClick(value) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "키워드로 매핑",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray800,
                        )
                    }

                }

            }
        }
    )
}