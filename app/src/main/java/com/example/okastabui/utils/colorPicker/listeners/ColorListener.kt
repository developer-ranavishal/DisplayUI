package com.example.okastabui.utils.colorPicker.listeners

import androidx.annotation.ColorInt
import com.example.okastabui.utils.colorPicker.listeners.ColorPickerViewListener


interface ColorListener: ColorPickerViewListener {
    fun onColorSelected(@ColorInt color: Int, fromUser: Boolean)

}