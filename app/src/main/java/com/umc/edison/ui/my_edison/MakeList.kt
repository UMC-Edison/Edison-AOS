package com.umc.edison.ui.my_edison

import android.content.Context
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import com.umc.edison.R

class MakeList{

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
}