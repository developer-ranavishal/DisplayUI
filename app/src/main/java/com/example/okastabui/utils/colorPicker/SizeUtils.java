package com.example.okastabui.utils.colorPicker;

import android.content.Context;

class SizeUtils {
  /** changes dp size to px size. */
  protected static int dp2Px(Context context, int dp) {
    final float scale = context.getResources().getDisplayMetrics().density;
    return (int) (dp * scale + 0.5f);
  }
}