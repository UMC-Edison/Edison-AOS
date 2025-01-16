package com.umc.edison.ui.my_edison

import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.text.Editable
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.navigation.NavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.umc.edison.R
import com.umc.edison.databinding.FragmentInputFieldBinding
import com.umc.edison.databinding.FragmentMyEdisonBinding
import com.umc.edison.local.model.BubbleLocal
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun MyEdisonScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        AndroidViewBinding(FragmentMyEdisonBinding::inflate) {
            //mainBubbleBt.text = "Composable 함수에 AndroidViewBinding 사용하기"

            mainBubbleBt.setOnClickListener {

                navController.navigate("input_field_screen")
            }


        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun InputFieldScreen(navController: NavController) {

    val showPopup = ShowPopup()

    val context = LocalContext.current

    val imageItems = remember { mutableStateListOf<Any?>() }


    val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            if (imageItems.size < 10) {
                imageItems.add(it) // Uri 저장
            } else {
                Toast.makeText(context, "이미지 최대 10개", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let {
            if (imageItems.size < 10) {
                imageItems.add(it) // Bitmap 저장
            } else {
                Toast.makeText(context, "이미지 최대 10개", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            takePictureLauncher.launch()
        } else {
            Toast.makeText(context, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    val galleryPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            pickImageLauncher.launch("image/*")
        } else {
            Toast.makeText(context, "갤러리 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }






    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)

    ){
        AndroidViewBinding(FragmentInputFieldBinding::inflate) {

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val bubbleList = arrayListOf(
                BubbleLocal("Title 1", "Content", dateFormat.parse("2025-01-16")!!,"https://example.com/image.jpg", listOf("https://example.com/image1_1.jpg", "https://example.com/image1_2.jpg"), false,true),
                BubbleLocal("Title 2", "Content", dateFormat.parse("2025-01-16")!!,"https://example.com/image.jpg", listOf("https://example.com/image1_1.jpg", "https://example.com/image1_2.jpg"), false,true)

            )

            val linkedList = mutableListOf<BubbleLocal>()
            val linkedAdapter = LinkRecyclerViewAdapter(linkedList)



            val adapter = TitleRecyclerViewAdapter(bubbleList){  selectedBubble ->
                linkedList.add(selectedBubble)
                linkedAdapter.updateItems(linkedList)
                Toast.makeText(context, "${selectedBubble.title} 클릭됨", Toast.LENGTH_SHORT).show()
                Log.d("RecyclerView", "Item clicked: ${selectedBubble.title}")

            }



            val recyclerView: RecyclerView =titleRv
            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(context)

            val recyclerView1: RecyclerView = linkedRv
            recyclerView1.adapter = linkedAdapter
            recyclerView1.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

            val texts = listOf(
                text1,text2,text3,text4,text5,text6,text7,text8,text9,text10,text11
            )


            val imageViews = listOf(
                imageView1, imageView2, imageView3,imageView4, imageView5,
                imageView6, imageView7, imageView8, imageView9, imageView10
            )

            textStyleIv.setOnClickListener {
                showPopup.showTextStylePopup(context, it, texts)
            }

            var isListMode = false;

            listIv.setOnClickListener{

                isListMode = !isListMode;

                if (isListMode) {
                    listIv.setImageResource(R.drawable.ic_space_selected)
                    texts.forEach { editText ->

                        val cursorPosition = editText.selectionStart
                        val textBeforeCursor = editText.text.substring(0, cursorPosition)

                        val currentLineStart = textBeforeCursor.lastIndexOf("\n") + 1
                        val currentLineEnd = cursorPosition

                        val currentLine = textBeforeCursor.substring(currentLineStart, currentLineEnd)
                        if (!currentLine.startsWith("□")) {
                            val updatedLine = "□ $currentLine"
                            editText.text.replace(
                                currentLineStart,
                                currentLineEnd,
                                updatedLine
                            )
                        }

                        editText.setSelection(editText.text.length)


                        editText.setOnKeyListener { _, keyCode, event ->
                            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                                editText.append("\n□ ")
                                editText.setSelection(editText.text.length)
                                true
                            } else {
                                false
                            }
                        }
                    }
                } else {

                    listIv.setImageResource(R.drawable.ic_list_tool_off)
                    texts.forEach { editText ->
                        editText.setOnKeyListener(null)
                    }
                } // 목록화



            }

            cameraIv.setOnClickListener {
                showPopup.showCameraPopup(context, it,galleryPermissionLauncher, cameraPermissionLauncher , pickImageLauncher, takePictureLauncher)
            }

            imageItems.forEachIndexed { index, item ->
                when (item) {
                    is Uri -> {
                        imageViews[index].setImageURI(item)
                    }
                    is Bitmap -> {
                        imageViews[index].setImageBitmap(item)
                    }
                }
                imageViews[index].visibility = View.VISIBLE
                texts[index+1].visibility = View.VISIBLE


                imageViews[index].setOnLongClickListener {
                    showPopup.showDeletePopup(context, it) {
                        val currentText = texts.getOrNull(index + 1)?.text?.toString() ?: ""
                        val previousText = texts[index].text.toString()

                        val combinedText = "$previousText\n$currentText"

                        texts[index].text = Editable.Factory.getInstance().newEditable(combinedText)

                        imageItems.removeAt(index)
                        imageViews[index].visibility = View.GONE
                        texts.getOrNull(index + 1)?.apply {

                            text = Editable.Factory.getInstance().newEditable("")
                            visibility = View.GONE  //사진 삭제 & edit text 칸 합치는 코드
                        }
                    }
                    true
                }
            }

            linkIv.setOnClickListener {
                if (titleRv.visibility == View.VISIBLE) {
                    titleRv.visibility = View.GONE
                } else {
                    titleRv.visibility = View.VISIBLE
                }

            }

            labelIv.setOnClickListener{

            }


        }
    }
}



