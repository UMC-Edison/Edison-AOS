package com.umc.edison.ui.components

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.umc.edison.ui.theme.Gray500
import com.umc.edison.ui.theme.Gray800

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageGallery(
    selectedImages: List<Uri>,
    onImageSelected: (Uri) -> Unit,
    onConfirmed: () -> Unit = {},
    multiSelectMode: Boolean,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    var imageList by remember { mutableStateOf(loadGalleryImages(context, "Recent")) }
    var folderList by remember { mutableStateOf(loadGalleryFolders(context)) } // 폴더 리스트
    var selectedFolder by remember { mutableStateOf("Recent") } // 선택된 폴더
    var isExpand by remember { mutableStateOf(false) } // 폴더 리스트 다이얼로그 상태

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            imageList = (loadGalleryImages(context, selectedFolder))
            folderList = loadGalleryFolders(context)
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    BottomSheet(
        onDismiss = { onClose() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)

        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(1f))

                Box {
                    Text(
                        text = "$selectedFolder ▼",
                        modifier = Modifier.clickable(
                            onClick = { isExpand = true }
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray800
                    )
                    DropdownMenu(
                        expanded = isExpand,
                        onDismissRequest = { isExpand = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        folderList.forEachIndexed { index, folder ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        folder,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                onClick = {
                                    selectedFolder = folder
                                    imageList = loadGalleryImages(context, folder)
                                    isExpand = false
                                }
                            )

                            if (index < folderList.size - 1) {
                                HorizontalDivider(
                                    thickness = 1.dp,
                                    color = Color.Gray.copy(alpha = 0.3f)
                                )
                            }
                        }
                    }

                }

                Spacer(modifier = Modifier.weight(1f))

                if (multiSelectMode) {
                    Text(
                        text = "선택",
                        modifier = Modifier.clickable(
                            onClick = {
                                onConfirmed()
                                onClose()
                            },
                            enabled = selectedImages.isNotEmpty()
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (selectedImages.isEmpty()) Gray500 else Gray800
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyVerticalGrid(columns = GridCells.Fixed(3)) {
                items(
                    count = imageList.size,
                    key = { index -> imageList[index].hashCode() }
                ) { index ->
                    val uri = imageList[index]
                    val isSelected = selectedImages.contains(uri)
                    val selectedIndex = selectedImages.indexOf(uri) + 1

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clickable {
                                onImageSelected(uri)
                            }, contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(50.dp)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {

                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    onImageSelected(uri)
                                },
                            )

                            if (multiSelectMode) {
                                if (isSelected) {
                                    Text(
                                        text = selectedIndex.toString(),
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun loadGalleryImages(context: Context, folder: String): List<Uri> {
    val images = mutableListOf<Uri>()
    val selection =
        if (folder == "Recent") null else "${MediaStore.Images.Media.BUCKET_DISPLAY_NAME} = ?"
    val selectionArgs = if (folder == "Recent") null else arrayOf(folder)

    val projection = arrayOf(
        MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME
    )
    val uriExternal = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    val cursor = context.contentResolver.query(
        uriExternal,
        projection,
        selection,
        selectionArgs,
        "${MediaStore.Images.Media.DATE_MODIFIED} DESC"
    )

    cursor?.use {
        val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        val nameColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)

        while (it.moveToNext()) {
            val id = it.getLong(idColumn)
            val name = it.getString(nameColumn)
            val contentUri = ContentUris.withAppendedId(uriExternal, id)

            Log.d("GalleryImage", "Image: $name, URI: $contentUri")
            images.add(contentUri)
        }
    }
    return images
}

fun loadGalleryFolders(context: Context): List<String> {
    val folders = mutableSetOf<String>()
    val projection = arrayOf(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
    val uriExternal = MediaStore.Images.Media.EXTERNAL_CONTENT_URI


    val cursor = context.contentResolver.query(uriExternal, projection, null, null, null)
    cursor?.use {
        val folderColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
        while (it.moveToNext()) {
            val folderName = it.getString(folderColumn) ?: "Unknown"
            folders.add(folderName)
        }
    }

    return listOf("Recent") + folders.toList()
}

