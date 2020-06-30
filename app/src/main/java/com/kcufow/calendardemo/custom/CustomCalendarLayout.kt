package com.kcufow.calendardemo.custom

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import com.haibin.calendarview.CalendarLayout

/**
 *created by ldw
 * 2020/6/28
 */
class CustomCalendarLayout(context: Context,attributeSet: AttributeSet):CalendarLayout(context,attributeSet) {
    private val TAG = "CustomCalendarLayout"
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        Log.e(TAG,"onTouchEvent ${event?.action}")
        return super.onTouchEvent(event)
    }
    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        val onInterceptTouchEvent = super.onInterceptTouchEvent(ev)
        Log.e(TAG,"onInterceptTouchEvent ${ev?.action}  $onInterceptTouchEvent")
        return onInterceptTouchEvent
    }
}