package com.kcufow.calendardemo.custom

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

/**
 *created by ldw
 * 2020/6/28
 */
class CustomRecyclerView(context: Context,attributeSet: AttributeSet):RecyclerView(context,attributeSet) {
    private val TAG = "CustomRecyclerView "

    override fun onTouchEvent(e: MotionEvent?): Boolean {
        Log.e(TAG,"onTouchEvent ${e?.action}")
        return super.onTouchEvent(e)
    }
}