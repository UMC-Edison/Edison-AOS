package com.umc.edison.ui.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.umc.edison.R
import com.umc.edison.presentation.model.UserModel
import com.umc.edison.presentation.mypage.EditProfileViewModel
import com.umc.edison.ui.components.ImageGallery
import com.umc.edison.ui.theme.*

@Composable
fun EditProfileScreen(
    navHostController: NavHostController,
    updateShowBottomNav: (Boolean) -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var showGallery by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { updateShowBottomNav(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White000)
            .padding(start = 24.dp, top = 12.dp, end = 24.dp, bottom = 18.dp),
    ) {
        Text(
            text = "저장",
            modifier = Modifier
                .align(Alignment.End)
                .clickable(onClick = {
                    viewModel.updateUserProfile()
                    navHostController.popBackStack()
                }),
            style = MaterialTheme.typography.titleMedium,
            color = Gray800
        )

        Spacer(modifier = Modifier.height(24.dp))

        EditProfileImage(
            user = uiState.user,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            onImageClick = { showGallery = true }
        )

        Spacer(modifier = Modifier.height(32.dp))

        EditProfileNameInput(
            nickname = uiState.user.nickname ?: "",
            onValueChange = { viewModel.updateUserNickname(it) }
        )

        if (showGallery) {
            ImageGallery(
                onConfirmed = { selectedImages ->
                    selectedImages.firstOrNull()?.let { uri ->
                        viewModel.updateUserProfileImage(uri)
                    }
                    true
                },
                onClose = { showGallery = false },
            )
        }
    }
}

@Composable
private fun EditProfileImage(
    user: UserModel,
    modifier: Modifier,
    onImageClick: () -> Unit
) {
    Box(
        modifier = modifier
            .width(120.dp)
            .height(125.dp)
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Gray200)
        ) {
            AsyncImage(
                model = user.profileImage ?: R.drawable.ic_profile_default_small,
                contentDescription = "Profile Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Box(
            modifier = Modifier
                .size(32.dp)
                .align(Alignment.BottomEnd)
                .clip(CircleShape)
                .background(White000)
                .border(1.dp, Gray200, CircleShape)
                .clickable { onImageClick() },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = R.drawable.ic_camera,
                contentDescription = "Camera Icon",
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun EditProfileNameInput(
    nickname: String,
    onValueChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "닉네임",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray800
            )
            Text(
                text = " *",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Red
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${nickname.length} / 20",
                style = MaterialTheme.typography.bodySmall,
                color = Gray600
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = nickname,
            onValueChange = { if (it.length <= 20) onValueChange(it) },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Gray100),
            placeholder = {
                Text(
                    text = nickname.ifEmpty { "닉네임을 입력해주세요." },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray600,
                )
            },
            textStyle = MaterialTheme.typography.bodyMedium,
            singleLine = true,
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                cursorColor = Gray800,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = Gray800,
                unfocusedTextColor = Gray800
            )
        )
    }
}
