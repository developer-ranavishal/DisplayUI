package com.example.okastabui.utils

import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.util.AttributeSet
import android.view.View
import com.example.okastabui.R

class BlurredCircleView : View {
    private val paint by lazy { Paint(ANTI_ALIAS_FLAG) }
    private var blurRadius: Float = 0f
    private var circleRadius: Float = 0f
    private var centerX: Float = 0f
    private var centerY: Float = 0f

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init(colorValue: Int = R.color.teal_200) {
        // Initialize the paint object
        val clrTriple = getRGBFromHashCode(colorValue)
        paint.apply {
            color = Color.argb(255, clrTriple.first, clrTriple.second, clrTriple.third) // Semi-transparent red
            style = Paint.Style.FILL
        }

        // Disable hardware acceleration for the blur effect to work
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    fun updateColor(colorStr: String){
        init(colorValue = Color.parseColor(colorStr))
        invalidate()
    }

    fun updateColor(color: Int){
        init(colorValue = color)
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // Recalculate the center and radius when the view size changes
        val netWidth = w - paddingLeft - paddingRight
        val netHeight = h - paddingTop - paddingBottom
        circleRadius =
            netWidth.coerceAtMost(netHeight) * 0.42f // Adjusted to fit inside view with padding

        // Set blur radius proportional to circle size
        blurRadius = circleRadius * 0.19f // The blur effect scales with the circle's size

        // Recalculate the center of the circle
        centerX = w * 0.5f
        centerY = h * 0.5f

        // Update the BlurMaskFilter for dynamic blur effect
        paint.maskFilter = BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawCircle(centerX, centerY, circleRadius, paint)
    }

    private fun getRGBFromHashCode(colorHashCode: Int): Triple<Int, Int, Int> {
        val red = Color.red(colorHashCode)
        val green = Color.green(colorHashCode)
        val blue = Color.blue(colorHashCode)

        return Triple(red, green, blue)
    }
}


