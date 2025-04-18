package com.example.okastabui.utils.colorPicker.listeners

import com.example.okastabui.utils.colorPicker.ColorEnvelope

interface ColorEnvelopeListener: ColorPickerViewListener {
    fun onColorSelected(envelope: ColorEnvelope, fromUser: Boolean)
}