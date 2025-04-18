package com.example.okastabui.utils.customSwitch;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;


class ThumbView extends View {

    private PointF center;
    private float radius;
    private Paint paint;

    public ThumbView(Context context) {
        super(context);
    }

    public ThumbView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ThumbView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ThumbView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.GRAY);
        center = new PointF();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        center.set(w * 0.5f, h * 0.5f);
        radius = Math.min(w, h) * 0.46f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(center.x, center.y, radius, paint);
    }

    public void setColor(int color) {
        paint.setColor(color);
        invalidate();
    }

    public int getColor() {
        return paint.getColor();
    }
}