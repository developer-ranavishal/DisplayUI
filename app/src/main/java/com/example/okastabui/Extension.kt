package com.example.okastabui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Build
import android.os.Bundle
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.TooltipCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.google.android.material.tabs.TabLayout
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.INVISIBLE
}


fun View.gone() {
    visibility = View.GONE
}


fun View.fadeIn(duration: Long = 500, startAlpha: Float = 0f, endAlpha: Float = 1f) {
    this.visibility = View.VISIBLE
    val fadeIn = AlphaAnimation(startAlpha, endAlpha).apply {
        this.duration = duration
    }
    this.startAnimation(fadeIn)
}

fun View.fadeOut(
    duration: Long = 500,
    startAlpha: Float = 1f,
    endAlpha: Float = 0f,
    visibilityAfter: Int = View.GONE
) {
    val fadeOut = AlphaAnimation(startAlpha, endAlpha).apply {
        this.duration = duration
    }

    fadeOut.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationRepeat(animation: Animation?) {}

        override fun onAnimationEnd(animation: Animation?) {
            this@fadeOut.visibility = visibilityAfter
        }

        override fun onAnimationStart(animation: Animation?) {}
    })

    this.startAnimation(fadeOut)
}


@SuppressLint("ClickableViewAccessibility")
fun View.swipeDownFragmentAnimation(onDismiss: () -> Unit) {
    var startY = 0f
    val translationLimit = 500f
    this.setOnTouchListener(View.OnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Save the starting Y position when the user touches the screen
                startY = event.rawY.toInt().toFloat()
                return@OnTouchListener true
            }

            MotionEvent.ACTION_MOVE -> {
                val deltaY = event.rawY - startY
                if (deltaY > 0) {
                    v.translationY = deltaY
                }
                return@OnTouchListener true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (v.translationY > translationLimit) {
                    onDismiss()
                } else {
                    v.animate().translationY(0f).setDuration(300).start()
                }
                return@OnTouchListener true
            }
        }
        true
    })
}

fun View.dismissFragment(fragmentManager: FragmentManager, onDismiss: () -> Unit = {}) {
    this.animate()
        .translationY(this.height.toFloat())
        .setDuration(300)
        .withEndAction {
            onDismiss()
            fragmentManager.popBackStack()
        }
        .start()
}


fun View.wrapInCustomShadow(shadowColor: Int) {
    val resources = context.resources
    val shapeDrawable = ShapeDrawable()
    shapeDrawable.setTint(shadowColor)

    val shadowBlur = paddingBottom - 6.toDp(resources)
    shapeDrawable.paint.setShadowLayer(
        shadowBlur,
        0f,
        0f,
        getColorWithAlpha(shadowColor, 0.2f)
    )
    setLayerType(View.LAYER_TYPE_SOFTWARE, shapeDrawable.paint)

    val radius = 24.toDp(resources)
    val outerRadius = floatArrayOf(
        radius, radius,
        radius, radius,
        radius, radius,
        radius, radius
    )
    shapeDrawable.shape = RoundRectShape(outerRadius, null, null)

    val drawable = LayerDrawable(arrayOf<Drawable>(shapeDrawable))
    val inset = paddingBottom
    drawable.setLayerInset(
        0,
        inset,
        inset,
        inset,
        inset
    )
    background = drawable
}

private fun getColorWithAlpha(color: Int, ratio: Float): Int {
    val alpha = (Color.alpha(color) * ratio).roundToInt()
    val r = Color.red(color)
    val g = Color.green(color)
    val b = Color.blue(color)
    return Color.argb(alpha, r, g, b)
}

fun Int.toDp(resources: Resources): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        resources.displayMetrics
    )
}

    fun FragmentActivity.replaceFragment(
        fragment: Fragment,
        containerId: Int,
        addToBackStack: Boolean = true,
        enterAnim: Int = R.anim.slide_up,
        exitAnim: Int = R.anim.slide_down
    ) {
        supportFragmentManager.beginTransaction().apply {
            setCustomAnimations(enterAnim, exitAnim, enterAnim, exitAnim)
            replace(containerId, fragment)
            if (addToBackStack) addToBackStack(fragment::class.java.simpleName)
            commit()
        }
    }
    
    
    fun FragmentActivity.addFragment(
        fragment: Fragment,
        containerId: Int,
        addToBackStack: Boolean = true,
        enterAnim: Int = R.anim.slide_up,
        exitAnim: Int = R.anim.slide_down
    ) {
        supportFragmentManager.beginTransaction().apply {
            setCustomAnimations(enterAnim, exitAnim, enterAnim, exitAnim)
            add(containerId, fragment)
            if (addToBackStack) addToBackStack(fragment::class.java.simpleName)
            commit()
        }
    }
    
    fun AppCompatActivity.replaceFragment(
        fragment: Fragment,
        containerId: Int,
        addToBackStack: Boolean = true,
        enterAnim: Int = R.anim.slide_up,
        exitAnim: Int = R.anim.slide_down
    ) {
        supportFragmentManager.beginTransaction().apply {
            setCustomAnimations(enterAnim, exitAnim, enterAnim, exitAnim)
            replace(containerId, fragment)
            if (addToBackStack) addToBackStack(fragment::class.java.simpleName)
            commit()
        }
    }

fun Fragment.showDialog(fragment: DialogFragment, tag: String) {
    val existingFragment = parentFragmentManager.findFragmentByTag(tag)
    if (existingFragment == null) {
        fragment.show(parentFragmentManager, tag)
    }
}

fun ImageView.setImageTint(color: Int) {
    ImageViewCompat.setImageTintList(this, ColorStateList.valueOf(color))
}

fun View.backgroundTint(colorResId: Int) {
    val color = ResourcesCompat.getColor(this.context.resources, colorResId, null)
    this.backgroundTintList = ColorStateList.valueOf(color)
}

fun TabLayout.disableToolTipAndRipple() {
    this.tabRippleColor = null
    for (i in 0 until this.tabCount) {
        TooltipCompat.setTooltipText(this.getTabAt(i)!!.view, null)
    }
    this.disableToolTipAndRippleOnItemSelect()
}

fun TabLayout.disableToolTipAndRippleOnItemSelect(){
    this.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
        override fun onTabSelected(tab: TabLayout.Tab?) {
            this@disableToolTipAndRippleOnItemSelect.disableToolTipAndRipple()
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {
        }

        override fun onTabReselected(tab: TabLayout.Tab?) {
        }

    })
}

fun ImageView.setBlur(enabled: Boolean, radius: Float = 20f) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // API 31+ (Android 12+): Use RenderEffect
        setRenderEffect(if (enabled) RenderEffect.createBlurEffect(radius, radius, Shader.TileMode.CLAMP) else null)
    } else if (enabled) {
        // API < 31: Apply RenderScript blur
        post { setImageBitmap(context.blurBitmap(this, radius)) }
    } else {
        // Restore original image when blur is disabled
        setImageDrawable(drawable) // Reset the ImageView
    }
}

// RenderScript Blur for older Android versions (API < 31)
private fun Context.blurBitmap(imageView: ImageView, radius: Float): Bitmap {
    imageView.isDrawingCacheEnabled = true
    imageView.buildDrawingCache()
    val bitmap = Bitmap.createBitmap(imageView.drawingCache)
    imageView.isDrawingCacheEnabled = false

    val output = Bitmap.createBitmap(bitmap)
    val renderScript = RenderScript.create(this)
    val blurScript = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
    val tmpIn = Allocation.createFromBitmap(renderScript, bitmap)
    val tmpOut = Allocation.createFromBitmap(renderScript, output)

    blurScript.setRadius(radius)
    blurScript.setInput(tmpIn)
    blurScript.forEach(tmpOut)
    tmpOut.copyTo(output)

    renderScript.destroy()
    return output
}

fun Context.showOkDialog(message: String, title: String = "Alert", onOkClick: (() -> Unit)? = null) {
    val dialog = AlertDialog.Builder(this)
        .setTitle(title)
        .setMessage(message)
        .setCancelable(false) // Prevent closing on outside touch
        .setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
            onOkClick?.invoke() // Callback when OK is clicked
        }
        .create()
    dialog.show()
}



@RequiresApi(Build.VERSION_CODES.N_MR1)
private fun Context.createShortcut(
    id: String,
    shortLabel: String,
    longLabel: String,
    iconRes: Int,
    targetActivity: Class<*>,
    fragmentTag: String? = null
): ShortcutInfo {

    val intent = Intent(this, targetActivity).apply {
        action = Intent.ACTION_VIEW
        fragmentTag?.let { putExtra("SHORTCUT_FRAGMENT_TO_OPEN", it) } // Pass Fragment Tag
    }

    return ShortcutInfo.Builder(this, id)
        .setShortLabel(shortLabel)
        .setLongLabel(longLabel)
        .setIcon(Icon.createWithResource(this, iconRes))
        .setIntent(intent)
        .build()

}

fun String.toFormattedDate(
    inputPattern: String = "yyyy-MM-dd'T'HH:mm:ss",
    outputPattern: String = "MMM dd, yyyy h:mm a",
    locale: Locale = Locale.getDefault()
): String {
    return try {
        val inputFormat = SimpleDateFormat(inputPattern, locale)
        val outputFormat = SimpleDateFormat(outputPattern, locale)
        val date = inputFormat.parse(this)
        // Convert to desired format and then make the result uppercase
        outputFormat.format(date).toUpperCase(locale)
    } catch (e: Exception) {
        "INVALID DATE"
    }
}

fun View.setSwipeLeftAndClickListener(
    swipeThreshold: Int = 100,
    onSwipe: () -> Unit
) {
    this.setOnTouchListener(object : View.OnTouchListener {
        private var startX: Float = 0f
        private var isClick = true

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.x
                    isClick = true
                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    val diffX = abs(event.x - startX)
                    if (diffX > swipeThreshold) {
                        isClick = false
                    }
                }

                MotionEvent.ACTION_UP -> {
                    if (isClick) {
                        v?.performClick()
                    } else if (startX - event.x > swipeThreshold) {
                        onSwipe()
                    }
                    return true
                }
            }
            return false
        }
    })
}

@SuppressLint("ClickableViewAccessibility")
fun View.setSwipeRightToDismissListener(
    dismissThreshold: Float = 100f,  // Default swipe distance to trigger dismiss
    onDismiss: () -> Unit
) {
    var initialX = 0f

    this.setOnTouchListener { _, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = event.rawX
                true
            }

            MotionEvent.ACTION_UP -> {
                val deltaX = event.rawX - initialX
                if (deltaX > dismissThreshold) {
                    onDismiss()
                }
                true
            }

            else -> false
        }
    }
}

@SuppressLint("ClickableViewAccessibility")
fun View.setSwipeLeftToDismissListener(
    dismissThreshold: Float = 100f,
    onDismiss: () -> Unit
) {
    var initialX = 0f
    this.setOnTouchListener { _, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = event.rawX
                true
            }

            MotionEvent.ACTION_UP -> {
                val deltaX = event.rawX - initialX
                if (deltaX < -dismissThreshold) {
                    onDismiss()
                }
                true
            }

            else -> false
        }
    }
}


fun Activity?.setAnimation(enterAnim: Int, exitAnim: Int){
    this?.let {
        this.overridePendingTransition(
            enterAnim, exitAnim
        )
    }
}

inline fun <reified T : Activity> Fragment.startActivity() {
    activity?.let {
        val intent = Intent(it, T::class.java)
        startActivity(intent)
    }
}

inline fun <reified T : Activity> Fragment.startActivityWithBundleData(bundle: Bundle, key: String) {
    activity?.let {
        val intent = Intent(it, T::class.java)
        intent.putExtra(key, bundle)
        startActivity(intent)
    }
}









