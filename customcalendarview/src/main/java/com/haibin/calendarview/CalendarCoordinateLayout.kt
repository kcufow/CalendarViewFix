package com.haibin.calendarview

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout

/**
 *created by ldw
 * 2020/6/29
 */
class CalendarCoordinateLayout(context: Context, attributeSet: AttributeSet) :
    LinearLayout(context, attributeSet) {
    /**
     * 自定义ViewPager，月视图
     */
    var mMonthView: MonthViewPager? = null

    /**
     * 日历
     */
    var mCalendarView: CalendarView? = null

    /**
     * 自定义的周视图
     */
    var mWeekPager: WeekViewPager? = null

    /**
     * 周月视图
     */
    private val CALENDAR_SHOW_MODE_BOTH_MONTH_WEEK_VIEW = 0


    /**
     * 仅周视图
     */
    private val CALENDAR_SHOW_MODE_ONLY_WEEK_VIEW = 1

    /**
     * 仅月视图
     */
    private val CALENDAR_SHOW_MODE_ONLY_MONTH_VIEW = 2

    /**
     * 默认展开
     */
    private val STATUS_EXPAND = 0

    /**
     * 默认收缩
     */
    private val STATUS_SHRINK = 1

    /**
     * 默认状态
     */
    private var mDefaultStatus = 0

    /**
     * 默认手势
     */
    private val GESTURE_MODE_DEFAULT = 0
    private var isWeekView = false

    /**
     * 内容布局id
     */
    private var mContentViewId = 0

    /**
     * 手势模式
     */
    private var mGestureMode = 0

    private var mCalendarShowMode = 0

    private var mDelegate: CalendarViewDelegate? = null
    init {
        orientation = VERTICAL
        val array: TypedArray =
            context.obtainStyledAttributes(attributeSet, R.styleable.CalendarCoordinateLayout)
        mContentViewId = array.getResourceId(R.styleable.CalendarCoordinateLayout_calendar_content_view_id, 0)
        mDefaultStatus =
            array.getInt(R.styleable.CalendarCoordinateLayout_default_status, STATUS_EXPAND)
        mCalendarShowMode = array.getInt(
            R.styleable.CalendarCoordinateLayout_calendar_show_mode,
            CALENDAR_SHOW_MODE_BOTH_MONTH_WEEK_VIEW
        )
        mGestureMode = array.getInt(
            R.styleable.CalendarCoordinateLayout_gesture_mode,GESTURE_MODE_DEFAULT
        )
        array.recycle()

    }


    private fun setup (delegate: CalendarViewDelegate) {
        this.mDelegate = delegate
    }

    /**
     * 隐藏日历
     */
    fun hideCalendarView() {
        if (mCalendarView == null) {
            return
        }
        mCalendarView!!.visibility = View.GONE
    /*    if (!isExpand()) {
            expand(0)
        }*/
        requestLayout()
    }

    /**
     * 显示日历
     */
    fun showCalendarView() {
        mCalendarView!!.visibility = View.VISIBLE
        requestLayout()
    }
    override fun onFinishInflate() {
        super.onFinishInflate()
        mMonthView = findViewById(R.id.vp_month)
        mWeekPager = findViewById(R.id.vp_week)
        if (childCount > 0) {
            mCalendarView = getChildAt(0) as CalendarView
        }

    }

}