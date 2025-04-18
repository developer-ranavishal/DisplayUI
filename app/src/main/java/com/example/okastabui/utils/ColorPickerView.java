package com.example.okastabui.utils;


import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.FloatRange;
import androidx.annotation.MainThread;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleObserver;

import com.example.okastabui.R;
import com.example.okastabui.databinding.PointerColorPickerBinding;
import com.example.okastabui.utils.colorPicker.ActionMode;
import com.example.okastabui.utils.colorPicker.ColorEnvelope;
import com.example.okastabui.utils.colorPicker.ColorHsvPalette;
import com.example.okastabui.utils.colorPicker.listeners.ColorEnvelopeListener;
import com.example.okastabui.utils.colorPicker.listeners.ColorListener;
import com.example.okastabui.utils.colorPicker.listeners.ColorPickerViewListener;


public class ColorPickerView extends FrameLayout implements LifecycleObserver {

  @ColorInt
  private int selectedPureColor;
  @ColorInt
  private int selectedColor;
  private Point selectedPoint;
  private ImageView palette;
  private Drawable paletteDrawable;
  public ColorPickerViewListener colorListener;
  private long debounceDuration = 0;
  private final Handler debounceHandler = new Handler();

  private ActionMode actionMode = ActionMode.ALWAYS;

  @FloatRange(from = 0.0, to = 1.0)
  private float selector_alpha = 1.0f;

  public ColorPickerView(Context context) {
    super(context);
  }

  public ColorPickerView(Context context, AttributeSet attrs) {
    super(context, attrs);
    getAttrs(attrs);
    onCreate();
  }

  public ColorPickerView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    getAttrs(attrs);
    onCreate();
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public ColorPickerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    getAttrs(attrs);
    onCreate();
  }

  private void getAttrs(AttributeSet attrs) {
    TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ColorPickerView);
    try {
      if (a.hasValue(R.styleable.ColorPickerView_palette)) {
        this.paletteDrawable = a.getDrawable(R.styleable.ColorPickerView_palette);
      }
      if (a.hasValue(R.styleable.ColorPickerView_selector_alpha)) {
        this.selector_alpha =
          a.getFloat(R.styleable.ColorPickerView_selector_alpha, selector_alpha);
      }
      if (a.hasValue(R.styleable.ColorPickerView_actionMode)) {
        int actionMode = a.getInteger(R.styleable.ColorPickerView_actionMode, 0);
        if (actionMode == 0) {
          this.actionMode = ActionMode.ALWAYS;
        } else if (actionMode == 1) this.actionMode = ActionMode.LAST;
      }
      if (a.hasValue(R.styleable.ColorPickerView_debounceDuration)) {
        this.debounceDuration =
          a.getInteger(R.styleable.ColorPickerView_debounceDuration, (int) debounceDuration);
      }
      if (a.hasValue(R.styleable.ColorPickerView_initialColor)) {
        setInitialColor(a.getColor(R.styleable.ColorPickerView_initialColor, Color.WHITE));
      }
    } finally {
      a.recycle();
    }
  }

  private PointerColorPickerBinding binding;

  private void onCreate() {
    setPadding(0, 0, 0, 0);
    palette = new ImageView(getContext());
    if (paletteDrawable != null) {
      palette.setImageDrawable(paletteDrawable);
    }

    LayoutParams paletteParam =
      new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    paletteParam.gravity = Gravity.CENTER;
    addView(palette, paletteParam);

    binding = PointerColorPickerBinding.inflate(LayoutInflater.from(getContext()), this, true);

    getViewTreeObserver()
      .addOnGlobalLayoutListener(
        new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override
          public void onGlobalLayout() {
            getViewTreeObserver().removeOnGlobalLayoutListener(this);
            onFinishInflated();
          }
        });
  }

  @Override
  protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
    super.onSizeChanged(width, height, oldWidth, oldHeight);

    if (palette.getDrawable() == null) {
      Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
      palette.setImageDrawable(new ColorHsvPalette(getResources(), bitmap));
    }
  }

  private void onFinishInflated() {
    if (getParent() != null && getParent() instanceof ViewGroup) {
      ((ViewGroup) getParent()).setClipChildren(false);
    }

    selectCenter();
  }


  @SuppressLint("ClickableViewAccessibility")
  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (!this.isEnabled()) {
      return false;
    }
    switch (event.getActionMasked()) {
      case MotionEvent.ACTION_DOWN:
      case MotionEvent.ACTION_MOVE:
      case MotionEvent.ACTION_UP:
        binding.getRoot().setPressed(true);
        return onTouchReceived(event);
      default:
        binding.getRoot().setPressed(false);
        return false;
    }
  }

  /**
   * notify to the other views by the onTouchEvent.
   *
   * @param event {@link MotionEvent}.
   * @return notified or not.
   */
  @MainThread
  private boolean onTouchReceived(final MotionEvent event) {
    Point snapPoint =
      PointMapper.getColorPoint(this, new Point((int) event.getX(), (int) event.getY()));
    int pixelColor = getColorFromBitmap(snapPoint.x, snapPoint.y);

    this.selectedPureColor = pixelColor;
    this.selectedColor = pixelColor;
    this.selectedPoint = PointMapper.getColorPoint(this, new Point(snapPoint.x, snapPoint.y));
    setCoordinate(snapPoint.x, snapPoint.y);

    if (actionMode == ActionMode.LAST) {
      if (event.getAction() == MotionEvent.ACTION_UP) {
        notifyColorChanged();
      }
    } else {
      notifyColorChanged();
    }
    return true;
  }

  public boolean isHuePalette() {
    return palette.getDrawable() != null && palette.getDrawable() instanceof ColorHsvPalette;
  }

  /**
   * notifies color changes to {@link ColorListener},
   */
  private void notifyColorChanged() {
    this.debounceHandler.removeCallbacksAndMessages(null);
    Runnable debounceRunnable =
      () -> {
        fireColorListener(getColor(), true);
      };
    this.debounceHandler.postDelayed(debounceRunnable, this.debounceDuration);
  }

  /**
   * gets a pixel color on the specific coordinate from the bitmap.
   *
   * @param x coordinate x.
   * @param y coordinate y.
   * @return selected color.
   */
  protected int getColorFromBitmap(float x, float y) {
    Matrix invertMatrix = new Matrix();
    palette.getImageMatrix().invert(invertMatrix);

    float[] mappedPoints = new float[]{x, y};
    invertMatrix.mapPoints(mappedPoints);

    if (palette.getDrawable() != null
      && palette.getDrawable() instanceof BitmapDrawable
      && mappedPoints[0] >= 0
      && mappedPoints[1] >= 0
      && mappedPoints[0] < palette.getDrawable().getIntrinsicWidth()
      && mappedPoints[1] < palette.getDrawable().getIntrinsicHeight()) {

      invalidate();

      if (palette.getDrawable() instanceof ColorHsvPalette) {
        x = x - getWidth() * 0.5f;
        y = y - getHeight() * 0.5f;
        double r = Math.sqrt(x * x + y * y);
        float radius = Math.min(getWidth(), getHeight()) * 0.5f;
        float[] hsv = {0, 0, 1};
        hsv[0] = (float) (Math.atan2(y, -x) / Math.PI * 180f) + 180;
        hsv[1] = Math.max(0f, Math.min(1f, (float) (r / radius)));
        return Color.HSVToColor(hsv);
      } else {
        Rect rect = palette.getDrawable().getBounds();
        float scaleX = mappedPoints[0] / rect.width();
        int x1 = (int) (scaleX * ((BitmapDrawable) palette.getDrawable()).getBitmap().getWidth());
        float scaleY = mappedPoints[1] / rect.height();
        int y1 = (int) (scaleY * ((BitmapDrawable) palette.getDrawable()).getBitmap().getHeight());
        return ((BitmapDrawable) palette.getDrawable()).getBitmap().getPixel(x1, y1);
      }
    }
    return 0;
  }

  /**
   * sets a {@link ColorPickerViewListener} on the {@link ColorPickerView}.
   *
   * @param colorListener {@link ColorListener} or {@link ColorEnvelopeListener}.
   */
  public void setColorListener(ColorPickerViewListener colorListener) {
    this.colorListener = colorListener;
  }

  /**
   * invokes {@link ColorListener} or {@link ColorEnvelopeListener} with a color value.
   *
   * @param color    color.
   * @param fromUser triggered by user or not.
   */
  public void fireColorListener(@ColorInt int color, final boolean fromUser) {
    this.selectedColor = color;
    binding.circleColor.setBackgroundColor(selectedColor);

    if (this.colorListener != null) {
      if (colorListener instanceof ColorListener) {
        ((ColorListener) colorListener).onColorSelected(selectedColor, fromUser);
      } else if (colorListener instanceof ColorEnvelopeListener) {
        ColorEnvelope envelope = new ColorEnvelope(selectedColor);
        ((ColorEnvelopeListener) colorListener).onColorSelected(envelope, fromUser);
      }
    }
  }

  /**
   * gets the selected color.
   *
   * @return the selected color.
   */
  public @ColorInt int getColor() {
    return selectedColor;
  }

  /**
   * gets an alpha value from the selected color.
   *
   * @return alpha from the selected color.
   */
  public @FloatRange(from = 0.0, to = 1.0) float getAlpha() {
    return Color.alpha(getColor()) / 255f;
  }

  /**
   * gets the selected pure color without alpha and brightness.
   *
   * @return the selected pure color.
   */
  public @ColorInt int getPureColor() {
    return selectedPureColor;
  }

  /**
   * sets the pure color.
   *
   * @param color the pure color.
   */
  public void setPureColor(@ColorInt int color) {
    this.selectedPureColor = color;
  }

  /**
   * gets the {@link ColorEnvelope} of the selected color.
   *
   * @return {@link ColorEnvelope}.
   */
  public ColorEnvelope getColorEnvelope() {
    return new ColorEnvelope(getColor());
  }

  /**
   * gets a debounce duration.
   *
   * <p>only emit a color to the listener if a particular timespan has passed without it emitting
   * another value.
   *
   * @return debounceDuration.
   */
  public long getDebounceDuration() {
    return this.debounceDuration;
  }

  /**
   * sets a debounce duration.
   *
   * <p>only emit a color to the listener if a particular timespan has passed without it emitting
   * another value.
   *
   * @param debounceDuration intervals.
   */
  public void setDebounceDuration(long debounceDuration) {
    this.debounceDuration = debounceDuration;
  }

  /**
   * gets a selector's selected coordinate.
   *
   * @return a selected coordinate {@link Point}.
   */
  public Point getSelectedPoint() {
    return selectedPoint;
  }

  /**
   * changes selector's selected point with notifies about changes manually.
   *
   * @param x coordinate x of the selector.
   * @param y coordinate y of the selector.
   */
  public void setSelectorPoint(int x, int y) {
    Point mappedPoint = PointMapper.getColorPoint(this, new Point(x, y));
    int color = getColorFromBitmap(mappedPoint.x, mappedPoint.y);
    selectedPureColor = color;
    selectedColor = color;
    selectedPoint = new Point(mappedPoint.x, mappedPoint.y);
    setCoordinate(mappedPoint.x, mappedPoint.y);
    fireColorListener(getColor(), false);
  }

  /**
   * moves selector's selected point with notifies about changes manually.
   *
   * @param x coordinate x of the selector.
   * @param y coordinate y of the selector.
   */
  public void moveSelectorPoint(int x, int y, @ColorInt int color) {
    selectedPureColor = color;
    selectedColor = color;
    selectedPoint = new Point(x, y);
    setCoordinate(x, y);
    fireColorListener(getColor(), false);
  }

  /**
   * changes selector's selected point without notifies.
   *
   * @param x coordinate x of the selector.
   * @param y coordinate y of the selector.
   */
  public void setCoordinate(int x, int y) {
    binding.getRoot().setX(x - (binding.getRoot().getWidth() * 0.5f));
    binding.getRoot().setY(y - (binding.getRoot().getHeight()));
  }

  public void setCircleDotColor(int color){
    binding.circleColor.setBackgroundColor(color);
  }


  public void setPointerPosition(float x, float y) {
    // Get the center coordinates of the circular image
    int cx = paletteDrawable.getIntrinsicWidth() / 2;
    int cy = paletteDrawable.getIntrinsicHeight() / 2;
    int radius = paletteDrawable.getIntrinsicWidth() / 2;

    // Calculate the distance from the center of the circle to the point (x, y)
    float distance = (float) Math.sqrt(Math.pow(x - cx, 2) + Math.pow(y - cy, 2));

    // Check if the point is inside the circle
    if (distance <= radius) {
      // Set the pointer's position (adjust to ensure the pointer is centered on the coordinates)
      float xNew = x - (float) binding.getRoot().getWidth() / 2;
      float yNew = y - (float) binding.getRoot().getHeight() / 2;
      setCoordinate((int) xNew, (int) yNew);
    } else {
      // If the point is outside the circle, handle it (e.g., place it at the edge)
      // You can compute a position along the circle's edge
      float ratio = radius / distance;
      float newX = cx + (x - cx) * ratio;
      float newY = cy + (y - cy) * ratio;
      // Place pointer on the edge of the circle
      float xNew = newX - (float) binding.getRoot().getWidth() / 2;
      float yNew = newY - (float) binding.getRoot().getHeight() / 2;
      setCoordinate((int) xNew, (int) yNew);
    }
  }

  public void setPointerYPosition(float y) {
    int cx = paletteDrawable.getIntrinsicWidth() / 2;
    int cy = paletteDrawable.getIntrinsicHeight() / 2;
    int radius = paletteDrawable.getIntrinsicWidth() / 2;


    // Calculate the distance from the center (cy) to the provided Y coordinate
    float distance = (float) Math.sqrt(Math.pow(cx - cx, 2) + Math.pow(y - cy, 2));

    // Ensure the Y position falls within the circle (i.e., distance should not exceed the radius)
    if (distance <= radius) {
      // Set pointer's X coordinate to always be at the center (cx)
      float xNew = cx - (float) binding.getRoot().getWidth() / 2;
      float yNew = y - (float) binding.getRoot().getHeight() / 2;
      setCoordinate((int) xNew, (int) yNew);
    } else {
      // If the Y coordinate is outside the circle, set it at the top or bottom edge of the circle
      float newY = (y < cy) ? (cy - radius) : (cy + radius);  // Top or bottom edge of the circle
      // Set the pointer's position at the edge
      float xNew = cx - (float) binding.getRoot().getWidth() / 2;
      float yNew = newY - (float) binding.getRoot().getHeight() / 2;
      setCoordinate((int) xNew, (int) yNew);
    }
  }


  /**
   * select a point by a specific color. this method will not work if the default palette drawable
   * is not {@link ColorHsvPalette}.
   *
   * @param color a starting color.
   */
  public void setInitialColor(@ColorInt final int color) {
    post(() -> {
      try {
        selectByHsvColor(color);
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
    });
  }

  /**
   * select a point by a specific color resource. this method will not work if the default palette
   * drawable is not {@link ColorHsvPalette}.
   *
   * @param colorRes a starting color resource.
   */
  public void setInitialColorRes(@ColorRes final int colorRes) {
    setInitialColor(ContextCompat.getColor(getContext(), colorRes));
  }

  /**
   * changes selector's selected point by a specific color.
   *
   * <p>It will throw an exception if the default palette drawable is not {@link ColorHsvPalette}.
   *
   * @param color color.
   */
  public void selectByHsvColor(@ColorInt int color) throws IllegalAccessException {
    if (palette.getDrawable() instanceof ColorHsvPalette) {
      float[] hsv = new float[3];
      Color.colorToHSV(color, hsv);

      float centerX = getWidth() * 0.5f;
      float centerY = getHeight() * 0.5f;
      float radius = hsv[1] * Math.min(centerX, centerY);
      int pointX = (int) (radius * Math.cos(Math.toRadians(hsv[0])) + centerX);
      int pointY = (int) (-radius * Math.sin(Math.toRadians(hsv[0])) + centerY);

      Point mappedPoint = PointMapper.getColorPoint(this, new Point(pointX, pointY));
      selectedPureColor = color;
      selectedColor = color;
      selectedPoint = new Point(mappedPoint.x, mappedPoint.y);

      setCoordinate(mappedPoint.x, mappedPoint.y);
      fireColorListener(getColor(), false);
    } else {
      throw new IllegalAccessException(
        "selectByHsvColor(@ColorInt int color) can be called only "
          + "when the palette is an instance of ColorHsvPalette. Use setHsvPaletteDrawable();");
    }
  }

  /**
   * changes selector's selected point by a specific color resource.
   *
   * <p>It may not work properly if change the default palette drawable.
   *
   * @param resource a color resource.
   */
  public void selectByHsvColorRes(@ColorRes int resource) throws IllegalAccessException {
    selectByHsvColor(ContextCompat.getColor(getContext(), resource));
  }

  /**
   * The default palette drawable is {@link ColorHsvPalette} if not be set the palette drawable
   * manually. This method can be used for changing as {@link ColorHsvPalette} from another palette
   * drawable.
   */
  public void setHsvPaletteDrawable() {
    Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
    setPaletteDrawable(new ColorHsvPalette(getResources(), bitmap));
  }

  /**
   * changes palette drawable manually.
   *
   * @param drawable palette drawable.
   */
  public void setPaletteDrawable(Drawable drawable) {
    removeView(palette);
    palette = new ImageView(getContext());
    paletteDrawable = drawable;
    palette.setImageDrawable(paletteDrawable);
    addView(palette);
    selectedPureColor = Color.WHITE;
  }

  /**
   * selects the center of the palette manually.
   */
  public void selectCenter() {
    setSelectorPoint(getWidth() / 2, getMeasuredHeight() / 2);
  }

  /**
   * sets enabling or not the ColorPickerView and slide bars.
   *
   * @param enabled true/false flag for making enable or not.
   */
  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
//    binding.getRoot().setVisibility(enabled ? VISIBLE : INVISIBLE);

    if (enabled) {
      palette.clearColorFilter();
    } else {
      int color = Color.argb(70, 255, 255, 255);
      palette.setColorFilter(color);
    }
  }

  /**
   * gets an {@link ActionMode}.
   *
   * @return {@link ActionMode}.
   */
  public ActionMode getActionMode() {
    return this.actionMode;
  }

  /**
   * sets an {@link ActionMode}.
   *
   * @param actionMode {@link ActionMode}.
   */
  public void setActionMode(ActionMode actionMode) {
    this.actionMode = actionMode;
  }

   public void animatePointerToHorizontalCenter(float pointerX) {
    // Animate only the X position to the center, keeping Y unchanged
    ValueAnimator xAnimator = ValueAnimator.ofFloat(pointerX, getWidth() / 2f);
    xAnimator.setDuration(500L); // Duration of 500ms for horizontal movement
    xAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

    xAnimator.addUpdateListener(animation -> {
      float value = (float) animation.getAnimatedValue();
      binding.getRoot().setX(value);
    });

    // Start the X-axis animation
    xAnimator.start();
  }

  public void animateYCoordinatePointer(float startPointerX, float endPointerY) {
    ValueAnimator xAnimator = ValueAnimator.ofFloat(startPointerX, endPointerY);
    xAnimator.setDuration(100L);
    xAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

    xAnimator.addUpdateListener(animation -> {
      float value = (float) animation.getAnimatedValue();
      if (endPointerY == (float) animation.getAnimatedValue()){
        binding.getRoot().setY(value - (binding.getRoot().getHeight()));
      }
    });
    xAnimator.start();
  }

  public Pair<Float, Float> getMarkerPosition() {
    return new Pair(binding.getRoot().getX(), binding.getRoot()
            .getY());
  }
}
