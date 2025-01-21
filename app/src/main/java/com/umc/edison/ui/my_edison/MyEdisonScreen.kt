package com.umc.edison.ui.my_edison
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.text.Editable
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.umc.edison.R
import com.umc.edison.databinding.FragmentInputFieldBinding
import com.umc.edison.databinding.FragmentMyEdisonBinding
import com.umc.edison.domain.model.Bubble
import com.umc.edison.domain.model.Label
import com.umc.edison.local.model.BubbleLocal
import com.umc.edison.local.model.LabelLocal
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

@SuppressLint("NotifyDataSetChanged")
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun InputFieldScreen(navController: NavController) {

    val context = LocalContext.current
    val selectedLabel = mutableListOf<Label>()
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






     Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Canvas(
            modifier = Modifier
                .size(300.dp) // 적당한 크기 설정
                .align(Alignment.Center) // 화면 중앙에 배치
        ) {
            val gradientColors = selectedLabel.map {
                androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(it.color))
            } + List(3 - selectedLabel.size) { Color.White }

            drawCircle(
                brush = Brush.radialGradient(
                    colors = gradientColors,
                    center = Offset(size.width / 2, size.height / 2),
                    radius = size.minDimension / 2
                ),
                radius = size.minDimension / 2,
                center = Offset(size.width / 2, size.height / 2)
            )
        }
    }


        AndroidViewBinding(FragmentInputFieldBinding::inflate, modifier = Modifier.zIndex(1f)) {

            val labelList = listOf(
                Label(1,"peach","#FFE0B2"),
                Label(2,"mint","#CCFFCC"),
                Label(3,"lavender","#FFC0CB"),
                Label(4,"pink","#FFC0CB"),
                Label(5,"yellow","#FFFACD"),
                Label(6,"purple","#E6E6FA"),
                Label(7,"blue","#ADD8E6"),
                Label(8,"green","#98FB98"),
                Label(9,"lime","#DFFFBF"),
                Label(10,"sky","#AEE4FF"),
                Label(11,"lilac","#CBAACB"),
            )

            val bubbleList = listOf(
                Bubble(1, "Title1","Content", "mainimage", listOf("https://example.com/image1_1.jpg", "https://example.com/image1_2.jpg"), labelList,"2025.01.19"),
                Bubble(2, "Title2","Content", "mainimage", listOf("https://example.com/image1_1.jpg", "https://example.com/image1_2.jpg"), labelList,"2025.01.19"),
                Bubble(3, "Title3","Content", "mainimage", listOf("https://example.com/image1_1.jpg", "https://example.com/image1_2.jpg"), labelList,"2025.01.19"),
                Bubble(4, "Title4","Content", "mainimage", listOf("https://example.com/image1_1.jpg", "https://example.com/image1_2.jpg"), labelList,"2025.01.19"),
                Bubble(5, "Title5","Content", "mainimage", listOf("https://example.com/image1_1.jpg", "https://example.com/image1_2.jpg"), labelList,"2025.01.19"),
                Bubble(6, "Title6","Content", "mainimage", listOf("https://example.com/image1_1.jpg", "https://example.com/image1_2.jpg"), labelList,"2025.01.19"),
                Bubble(7, "Title7","Content", "mainimage", listOf("https://example.com/image1_1.jpg", "https://example.com/image1_2.jpg"), labelList,"2025.01.19"),
                Bubble(8, "Title8","Content", "mainimage", listOf("https://example.com/image1_1.jpg", "https://example.com/image1_2.jpg"), labelList,"2025.01.19"),

            )



            val linkedList = mutableListOf<Bubble>()
            val linkedAdapter = LinkRecyclerViewAdapter(linkedList){
                
            }

            val adapter = TitleRecyclerViewAdapter(bubbleList){  selectedBubble ->
                if (!linkedList.contains(selectedBubble)) {
                    linkedList.add(selectedBubble) // 중복이 없을 때만 추가
                    linkedAdapter.notifyItemInserted(linkedList.size - 1) // RecyclerView에 업데이트
                    Toast.makeText(context, "${selectedBubble.title} 추가됨", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "${selectedBubble.title} 이미 추가됨", Toast.LENGTH_SHORT).show()
                }
            }



            val recyclerView: RecyclerView =titleRv
            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(context)

           val recyclerView1: RecyclerView = linkedRv
            recyclerView1.adapter = linkedAdapter
            recyclerView1.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)



            val recyclerView2 : RecyclerView = labelRv

            val labelAdapter = LabelRecyclerViewAdapter(labelList, selectedLabel){label ->

                if (selectedLabel.contains(label)) {
                    selectedLabel.remove(label)
                } else if (selectedLabel.size < 3) {
                    selectedLabel.add(label)
                } else {
                    Toast.makeText(context, "최대 3개의 라벨만 선택 가능합니다.", Toast.LENGTH_SHORT).show()
                }

                recyclerView2.adapter?.notifyDataSetChanged()
            }

            recyclerView2.adapter = labelAdapter
            recyclerView2.layoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)

            val texts = listOf(
                text1,text2,text3,text4,text5,text6,text7,text8,text9,text10,text11
            )

            val imageViews = listOf(
                imageView1, imageView2, imageView3,imageView4, imageView5,
                imageView6, imageView7, imageView8, imageView9, imageView10
            )

            textStyleIv.setOnClickListener {
                showTextStylePopup(context, it, texts)
                if (titleRv.visibility == View.VISIBLE) {
                    titleRv.visibility = View.GONE
                }

                if (labelRv.visibility == View.VISIBLE) {
                    labelRv.visibility = View.GONE
                }
            }

            var isListMode = false;
            fun makeList(isListMode:Boolean, listIv: ImageView, editTexts: List<EditText>){

                if (isListMode) {
                    listIv.setImageResource(R.drawable.ic_space_selected)
                    editTexts.forEach { editText ->

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
                    editTexts.forEach { editText ->
                        editText.setOnKeyListener(null)
                    }
                }

            }
            listIv.setOnClickListener{
                isListMode = !isListMode
                makeList(isListMode,listIv,texts)
            }

            cameraIv.setOnClickListener {
                showCameraPopup(context, it,galleryPermissionLauncher, cameraPermissionLauncher , pickImageLauncher, takePictureLauncher)
                
                if (titleRv.visibility == View.VISIBLE) {
                    titleRv.visibility = View.GONE
                }

                if (labelRv.visibility == View.VISIBLE) {
                    labelRv.visibility = View.GONE
                }
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
                    showDeletePopup(context, it) {
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

                if (labelRv.visibility == View.VISIBLE) {
                    labelRv.visibility = View.GONE
                } else {
                    labelRv.visibility = View.VISIBLE
                }

            }


        }
    }




