package com.umc.edison.presentation.edison

import android.net.Uri
import com.umc.edison.presentation.label.LabelEditMode
import com.umc.edison.presentation.model.BubbleModel
import com.umc.edison.presentation.model.LabelModel
import com.umc.edison.ui.components.IconType
import com.umc.edison.ui.components.ListStyle
import com.umc.edison.ui.components.TextStyle

data class BubbleInputState(
    val bubble: BubbleModel,
    val bubbles: List<BubbleModel>,
    val labelEditMode: LabelEditMode,
    val labels: List<LabelModel>,
    val selectedLabels: List<LabelModel>,
    val selectedIcon: IconType,
    val selectedTextStyles: List<TextStyle>,
    val selectedListStyle: ListStyle,
    val isGalleryOpen: Boolean,
    val isCameraOpen: Boolean,
    val cameraImagePath: Uri?,
    val canSave: Boolean,
    val focusedTextIndex: Int? = null, // 포커스 줄 텍스트 블록 position (없으면 null)
) {
    companion object {
        val DEFAULT = BubbleInputState(
            bubble = BubbleModel.DEFAULT,
            bubbles = emptyList(),
            labelEditMode = LabelEditMode.NONE,
            labels = emptyList(),
            selectedLabels = emptyList(),
            selectedIcon = IconType.NONE,
            selectedTextStyles = emptyList(),
            selectedListStyle = ListStyle.NONE,
            isGalleryOpen = false,
            isCameraOpen = false,
            cameraImagePath = null,
            canSave = false,
            focusedTextIndex = null,
        )
    }
}
