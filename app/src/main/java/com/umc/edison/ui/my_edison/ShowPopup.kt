package com.umc.edison.ui.my_edison

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.launch
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.umc.edison.R
import com.umc.edison.databinding.CameraPopupBinding
import com.umc.edison.databinding.TextStylePopupBinding

class ShowPopup {

    fun showDeletePopup(context: Context, anchorView: View, onDeleteConfirmed: () -> Unit) {

        val popupView = LayoutInflater.from(context).inflate(R.layout.delete_popup, null)

        val popupWindow = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )


        val deleteButton = popupView.findViewById<Button>(R.id.button5)

        deleteButton.setOnClickListener {
            onDeleteConfirmed() // 삭제 로직 실행
            popupWindow.dismiss() // 팝업 닫기
        }

        popupWindow.showAtLocation(anchorView, Gravity.CENTER, 0, 0)
    }




    fun showTextStylePopup(context: Context, anchorView: View, editTexts: List<EditText> ) {

        val popupBinding = TextStylePopupBinding.inflate(LayoutInflater.from(context))
        val popupWindow = PopupWindow(
            popupBinding.root,
            250.dpToPx(),
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        val location = IntArray(2)
        anchorView.getLocationOnScreen(location)

        val popupX = (anchorView.rootView.width - popupWindow.width) / 2
        val popupY = location[1] + anchorView.height + 50

        popupBinding.boldIv.setOnClickListener {
            editTexts.forEach { editText ->
                TextStyleEdit.applyTextStyle(editText, TextStyleEdit.StyleType.BOLD)
            }
        }

        popupBinding.italicIv.setOnClickListener {
            editTexts.forEach { editText ->
                TextStyleEdit.applyTextStyle(editText, TextStyleEdit.StyleType.ITALIC)
            }
        }

        popupBinding.underlineIv.setOnClickListener {
            editTexts.forEach { editText ->
                TextStyleEdit.applyTextStyle(editText, TextStyleEdit.StyleType.UNDERLINE)
            }
        }

        popupBinding.highlightIv.setOnClickListener {
            editTexts.forEach { editText ->
                TextStyleEdit.applyTextStyle(editText, TextStyleEdit.StyleType.HIGHLIGHT)
            }
        }

        popupWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY, popupX, popupY)
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun showCameraPopup(
        context: Context,
        anchorView: View,
        galleryPermissionLauncher: ActivityResultLauncher<String>, cameraPermissionLauncher: ActivityResultLauncher<String>,
        pickImageLauncher: ActivityResultLauncher<String>,
        takePictureLauncher: ManagedActivityResultLauncher<Void?, Bitmap? >,
    ){
        val popupBinding = CameraPopupBinding.inflate(LayoutInflater.from(context))

        val popupWindow = PopupWindow(
            popupBinding.root,
            200.dpToPx(),
            133.dpToPx(),
            true
        )


        val location = IntArray(2)
        anchorView.getLocationOnScreen(location)

        val popupX = (anchorView.rootView.width - popupWindow.width) / 2
        val popupY = location[1] + anchorView.height + 50

        popupBinding.takepicture.setOnClickListener {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                takePictureLauncher.launch() // 카메라 촬영 실행
            } else {
                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
        }

        popupBinding.gallery.setOnClickListener {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                pickImageLauncher.launch("image/*")
            } else {
                galleryPermissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
            }
        }

        popupWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY, popupX, popupY)
    }



    fun Int.dpToPx(): Int {
        return (this * Resources.getSystem().displayMetrics.density).toInt()
    }
}