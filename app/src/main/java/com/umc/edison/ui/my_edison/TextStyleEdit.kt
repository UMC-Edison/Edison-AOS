package com.umc.edison.ui.my_edison

import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.style.BackgroundColorSpan
import android.text.style.MetricAffectingSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.widget.EditText

class TextStyleEdit {

    enum class StyleType {
        BOLD, ITALIC, UNDERLINE, HIGHLIGHT
    }

    companion object {
        fun applyTextStyle(editText: EditText, styleType: StyleType) {
            val start = editText.selectionStart
            val end = editText.selectionEnd
            val spannable = SpannableStringBuilder(editText.text)


            val existingSpans = spannable.getSpans(start, end, getSpanClass(styleType))
            val isFullyStyled =
                existingSpans.any { spannable.getSpanStart(it) <= start && spannable.getSpanEnd(it) >= end }

            if (isFullyStyled) {
                for (span in existingSpans) {
                    val spanStart = spannable.getSpanStart(span)
                    val spanEnd = spannable.getSpanEnd(span)

                    spannable.removeSpan(span)
                    if (spanStart < start) {
                        spannable.setSpan(
                            createSpan(styleType),
                            spanStart,
                            start,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                    if (end < spanEnd) {
                        spannable.setSpan(
                            createSpan(styleType),
                            end,
                            spanEnd,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }
            } else {

                spannable.setSpan(
                    createSpan(styleType),
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            editText.setText(spannable)

            editText.setSelection(start, end)
        }

        private fun getSpanClass(styleType: StyleType): Class<out Any> {
            return when (styleType) {
                StyleType.BOLD -> FakeBoldSpan::class.java
                StyleType.ITALIC -> StyleSpan::class.java
                StyleType.UNDERLINE -> UnderlineSpan::class.java
                StyleType.HIGHLIGHT -> BackgroundColorSpan::class.java
            }
        }

        private fun createSpan(styleType: StyleType): Any {
            return when (styleType) {
                StyleType.BOLD -> FakeBoldSpan()
                StyleType.ITALIC -> StyleSpan(Typeface.ITALIC)
                StyleType.UNDERLINE -> UnderlineSpan()
                StyleType.HIGHLIGHT -> BackgroundColorSpan(Color.parseColor("#F2F5A9"))
            }
        }


    }

    class FakeBoldSpan : MetricAffectingSpan() {
        override fun updateDrawState(textPaint: TextPaint) {
            textPaint.isFakeBoldText = true
        }

        override fun updateMeasureState(textPaint: TextPaint) {
            textPaint.isFakeBoldText = true
        }
    }
}