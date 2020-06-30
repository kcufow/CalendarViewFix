package com.kcufow.calendardemo.custom

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import com.haibin.calendarview.CalendarView

/**
 *created by ldw
 * 2020/6/28
 */
class CustomCalendarView(context: Context,attributeSet: AttributeSet):CalendarView(context,attributeSet) {
    private val TAG = "CustomCalendarView"
    var expand =false


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        Log.e(TAG,"onTouchEvent ${event?.action}")
        if (event==null) return super.onTouchEvent(event)

        if (event.action == MotionEvent.ACTION_MOVE && !expand) return false
        return super.onTouchEvent(event)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        Log.e(TAG,"onInterceptTouchEvent ${ev?.action}")
            if (!expand)return false
        return super.onInterceptTouchEvent(ev)
    }
}