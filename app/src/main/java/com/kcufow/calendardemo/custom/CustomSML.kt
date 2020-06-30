package com.kcufow.calendardemo.custom

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import com.scwang.smartrefresh.layout.SmartRefreshLayout

/**
 * created by ldw
 * 2020/6/28
 */
internal class CustomSML : SmartRefreshLayout {
    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(
        context,
        attrs
    ) {
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
    }

    override fun dispatchTouchEvent(e: MotionEvent): Boolean {
        val touchEvent = super.dispatchTouchEvent(e)
        Log.e(
            TAG,
            "dispatchTouchEvent: " + e.action + " touchEvent " + touchEvent
        )
        return touchEvent
    }

    companion object {
        private const val TAG = "CustomSML"
    }
}