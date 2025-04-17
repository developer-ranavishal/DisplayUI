package com.example.okastabui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import com.example.okastabui.databinding.CustomButtonProgressBarBinding


class CustomProgressButton(context: Context, attrs: AttributeSet) :
    RelativeLayout(context, attrs) {

    private var mText = ""
    private var binding: CustomButtonProgressBarBinding

    init {
        val typedArray =
            context.theme.obtainStyledAttributes(attrs, R.styleable.CustomProgressBarButton, 0, 0)
        try {
            if (R.styleable.CustomProgressBarButton != null && typedArray.getString(R.styleable.CustomProgressBarButton_text) != null) {
                mText = typedArray.getString(R.styleable.CustomProgressBarButton_text)!!
            }
        } finally {
            typedArray.recycle()
        }
        binding = CustomButtonProgressBarBinding.inflate(LayoutInflater.from(context), this, true)
    }


    fun enabled(isEnabled: Boolean) {
        if (isEnabled) {
            binding.customBtn.background.setTint(
                ContextCompat.getColor(
                    context,
                    R.color.custom_btn_on_boarding_enable_color
                )
            )
            binding.customBtn.elevation = 14f
        } else {
            binding.customBtn.background.setTint(
                ContextCompat.getColor(
                    context,
                    R.color.custom_btn_on_boarding_disable_color
                )
            )
            binding.customBtn.elevation = 0f
        }
    }

    fun startAnimation() {
        binding.customBtn.apply {
            visibility = View.GONE
            animate().alpha(0.0f)
        }
        binding.customProgressBar.apply {
            visibility = View.VISIBLE
            animate().alpha(1.0f)
        }
    }

    fun revertAnimation() {
        binding.customProgressBar.apply {
            visibility= View.GONE
            animate().alpha(0.0f)
        }
        binding.customBtn.apply {
            visibility= View.VISIBLE
            animate().alpha(1.0f)
        }
    }

    fun success() {
        binding.customProgressBar.apply {
            visibility = View.GONE
            animate().alpha(0.0f)
        }
        binding.customBtn.apply {
            background.setTint(
                ContextCompat.getColor(
                    context,
                    R.color.custom_btn_on_boarding_enable_color
                )
            )
            visibility = View.VISIBLE
            animate().alpha(1.0f)
        }
    }
}