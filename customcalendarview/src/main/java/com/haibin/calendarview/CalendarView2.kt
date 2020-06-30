package com.haibin.calendarview

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import java.util.*

/**
 *created by ldw
 * 2020/6/29
 */
class CalendarView2(context: Context, attributeSet: AttributeSet) :
    FrameLayout(context, attributeSet) {
    private val TAG = "CalendarView2 ===="

    /**
     * 抽取自定义属性
     */
    private  var mDelegate: CalendarViewDelegate = CalendarViewDelegate(context, attributeSet)

    /**
     * 自定义自适应高度的ViewPager
     */
    var mMonthPager: MonthViewPager

    /**
     * 日历周视图
     */
    var mWeekPager: WeekViewPager

    /**
     * 星期栏的线
     */
    var mWeekLine: View


    /**
     * 星期栏
     */
    lateinit var mWeekBar: WeekBar
    //默认折叠日历
     var isExpand = false

    init {
        LayoutInflater.from(context)
            .inflate(R.layout.cv_layout_calendar_view2, this, true)

        mWeekPager = findViewById(R.id.vp_week)
        mWeekPager.setup(mDelegate)
        try {
            val constructor = mDelegate.weekBarClass.getConstructor(Context::class.java)
            mWeekBar = constructor.newInstance(getContext()) as WeekBar
        } catch (e: Exception) {
            e.printStackTrace()
        }
        addView(mWeekBar, 2)
        mWeekBar.setup(mDelegate)
        mWeekBar.onWeekStartChange(mDelegate.weekStart)
        mWeekLine = findViewById<View>(R.id.line)
        mWeekLine.setBackgroundColor(mDelegate.weekLineBackground)
        val lineParams = mWeekLine.layoutParams as LayoutParams
        lineParams.setMargins(
            mDelegate.weekLineMargin,
            mDelegate.weekBarHeight,
            mDelegate.weekLineMargin,
            0
        )
        mWeekLine.layoutParams = lineParams
        mMonthPager = findViewById(R.id.vp_month)
        mMonthPager.mWeekPager = mWeekPager
        mMonthPager.mWeekBar = mWeekBar
        val params = mMonthPager.layoutParams as LayoutParams
        params.setMargins(
            0,
            mDelegate.weekBarHeight + CalendarUtil.dipToPx(context, 1f),
            0,
            0
        )
        mWeekPager.layoutParams = params
        mDelegate.mInnerListener = object : CalendarView.OnInnerDateSelectedListener {
            override fun onMonthDateSelected(calendar: Calendar?, isClick: Boolean) {
                Log.e(TAG,"onMonthDateSelected $calendar")
                if (calendar == null) return
                if (calendar.year == mDelegate.currentDay.year && calendar.month == mDelegate.currentDay.month
                    && mMonthPager.currentItem != mDelegate.mCurrentMonthViewItem
                ) {
                    return
                }
                mDelegate.mIndexCalendar = calendar
                if (mDelegate.selectMode == CalendarViewDelegate.SELECT_MODE_DEFAULT || isClick) {
                    mDelegate.mSelectedCalendar = calendar
                }
                mWeekPager.updateSelected(mDelegate.mIndexCalendar, false)
                mMonthPager.updateSelected()
                if (mDelegate.selectMode == CalendarViewDelegate.SELECT_MODE_DEFAULT || isClick
                ) {
                    mWeekBar.onDateSelected(calendar, mDelegate.weekStart, isClick)
                }
            }

            /**
             * 周视图选择事件
             * @param calendar calendar
             * @param isClick 是否是点击
             */
            override fun onWeekDateSelected(calendar: Calendar?, isClick: Boolean) {
                if (calendar == null) return
                mDelegate.mIndexCalendar = calendar
                if (mDelegate.selectMode == CalendarViewDelegate.SELECT_MODE_DEFAULT || isClick
                    || mDelegate.mIndexCalendar == mDelegate.mSelectedCalendar
                ) {
                    mDelegate.mSelectedCalendar = calendar
                }
                val y = calendar.year - mDelegate.minYear
                val position =
                    12 * y + mDelegate.mIndexCalendar.month - mDelegate.minYearMonth
                mWeekPager.updateSingleSelect()
                mMonthPager.setCurrentItem(position, false)
                mMonthPager.updateSelected()
                if (mDelegate.selectMode == CalendarViewDelegate.SELECT_MODE_DEFAULT || isClick
                    || mDelegate.mIndexCalendar == mDelegate.mSelectedCalendar
                ) {
                    mWeekBar.onDateSelected(calendar, mDelegate.weekStart, isClick)
                }
            }
        }
        if (mDelegate.selectMode == CalendarViewDelegate.SELECT_MODE_DEFAULT) {
            if (isInRange(mDelegate.currentDay)) {
                mDelegate.mSelectedCalendar = mDelegate.createCurrentDate()
            } else {
                mDelegate.mSelectedCalendar = mDelegate.minRangeCalendar
            }
        } else {
            mDelegate.mSelectedCalendar = Calendar()
        }
        mDelegate.mIndexCalendar = mDelegate.mSelectedCalendar
        mWeekBar.onDateSelected(mDelegate.mSelectedCalendar, mDelegate.weekStart, false)
        mMonthPager.setup(mDelegate)
        mMonthPager.currentItem = mDelegate.mCurrentMonthViewItem
        mWeekPager.updateSelected(mDelegate.createCurrentDate(), false)

        if (isExpand){
            hideWeek(true)
        }else{
            showWeek()
        }
    }
    fun updateExpand(expand:Boolean){
        this.isExpand = expand
        if (isExpand){
            hideWeek()
        }else{
           showWeek()
        }
    }

    /**
     * 显示周视图
     */
    private fun showWeek() {
        onShowWeekView()
        if (mWeekPager.adapter != null) {
            mWeekPager.adapter!!.notifyDataSetChanged()
            mWeekPager.visibility = View.VISIBLE
        }
        mMonthPager.visibility = View.GONE
    }

    /**
     * 隐藏周视图
     */
    private fun hideWeek(isNotify: Boolean=true) {
        if (isNotify) {
            onShowMonthView()
        }
        if (mMonthPager.adapter != null) {
            mMonthPager.adapter!!.notifyDataSetChanged()
        }
        mWeekPager.visibility = View.GONE
        mMonthPager.visibility = View.VISIBLE
    }

    /**
     * 周视图显示事件
     */
    private fun onShowMonthView() {
        if (mDelegate.mViewChangeListener != null ) {
            mDelegate.mViewChangeListener.onViewChange(true)
        }
    }


    /**
     * 周视图显示事件
     */
    private fun onShowWeekView() {
        if (mDelegate.mViewChangeListener != null ) {
            mDelegate.mViewChangeListener.onViewChange(false)
        }
    }

    /**
     * 设置日期范围
     *
     * @param minYear      最小年份
     * @param minYearMonth 最小年份对应月份
     * @param minYearDay   最小年份对应天
     * @param maxYear      最大月份
     * @param maxYearMonth 最大月份对应月份
     * @param maxYearDay   最大月份对应天
     */
    fun setRange(
        minYear: Int, minYearMonth: Int, minYearDay: Int,
        maxYear: Int, maxYearMonth: Int, maxYearDay: Int
    ) {
        if (CalendarUtil.compareTo(
                minYear, minYearMonth, minYearDay,
                maxYear, maxYearMonth, maxYearDay
            ) > 0
        ) {
            return
        }
        mDelegate.setRange(
            minYear, minYearMonth, minYearDay,
            maxYear, maxYearMonth, maxYearDay
        )
        mWeekPager.notifyDataSetChanged()

        mMonthPager.notifyDataSetChanged()
        if (!isInRange(mDelegate.mSelectedCalendar)) {
            mDelegate.mSelectedCalendar = mDelegate.minRangeCalendar
            mDelegate.updateSelectCalendarScheme()
            mDelegate.mIndexCalendar = mDelegate.mSelectedCalendar
        }
        mWeekPager.updateRange()
        mMonthPager.updateRange()
    }

    /**
     * 获取当天
     *
     * @return 返回今天
     */
    fun getCurDay(): Int {
        return mDelegate.currentDay.day
    }

    /**
     * 获取本月
     *
     * @return 返回本月
     */
    fun getCurMonth(): Int {
        return mDelegate.currentDay.month
    }

    /**
     * 获取本年
     *
     * @return 返回本年
     */
    fun getCurYear(): Int {
        return mDelegate.currentDay.year
    }


    /**
     * 滚动到当前
     */
    fun scrollToCurrent() {
        scrollToCurrent(false)
    }

    /**
     * 滚动到当前
     *
     * @param smoothScroll smoothScroll
     */
    fun scrollToCurrent(smoothScroll: Boolean) {
        if (!isInRange(mDelegate.currentDay)) {
            return
        }
        val calendar = mDelegate.createCurrentDate()
        if (mDelegate.mCalendarInterceptListener != null &&
            mDelegate.mCalendarInterceptListener.onCalendarIntercept(calendar)
        ) {
            mDelegate.mCalendarInterceptListener.onCalendarInterceptClick(calendar, false)
            return
        }
        mDelegate.mSelectedCalendar = mDelegate.createCurrentDate()
        mDelegate.mIndexCalendar = mDelegate.mSelectedCalendar
        mDelegate.updateSelectCalendarScheme()
        mWeekBar.onDateSelected(mDelegate.mSelectedCalendar, mDelegate.weekStart, false)
        if (mMonthPager.visibility == View.VISIBLE) {
            mMonthPager.scrollToCurrent(smoothScroll)
            mWeekPager.updateSelected(mDelegate.mIndexCalendar, false)
        } else {
            mWeekPager.scrollToCurrent(smoothScroll)
        }
    }


    /**
     * 滚动到下一个月
     */
    fun scrollToNext() {
        scrollToNext(false)
    }

    /**
     * 滚动到下一个月
     *
     * @param smoothScroll smoothScroll
     */
    fun scrollToNext(smoothScroll: Boolean) {
        if (mWeekPager.visibility == View.VISIBLE) {
            mWeekPager.setCurrentItem(mWeekPager.currentItem + 1, smoothScroll)
        } else {
            mMonthPager.setCurrentItem(mMonthPager.currentItem + 1, smoothScroll)
        }
    }

    /**
     * 滚动到上一个月
     */
    fun scrollToPre() {
        scrollToPre(false)
    }

    /**
     * 滚动到上一个月
     *
     * @param smoothScroll smoothScroll
     */
    private fun scrollToPre(smoothScroll: Boolean) {
        when (mWeekPager.visibility) {
            View.VISIBLE -> {
                mWeekPager.setCurrentItem(mWeekPager.currentItem - 1, smoothScroll)
            }
            else -> {
                mMonthPager.setCurrentItem(mMonthPager.currentItem - 1, smoothScroll)
            }
        }
    }

    /**
     * 滚动到选择的日历
     */
    fun scrollToSelectCalendar() {
        if (!mDelegate.mSelectedCalendar.isAvailable) {
            return
        }
        scrollToCalendar(
            mDelegate.mSelectedCalendar.year,
            mDelegate.mSelectedCalendar.month,
            mDelegate.mSelectedCalendar.day,
            smoothScroll = false,
            invokeListener = true
        )
    }

    /**
     * 滚动到指定日期
     *
     * @param year  year
     * @param month month
     * @param day   day
     */
    fun scrollToCalendar(year: Int, month: Int, day: Int) {
        scrollToCalendar(year, month, day, false, true)
    }

    /**
     * 滚动到指定日期
     *
     * @param year         year
     * @param month        month
     * @param day          day
     * @param smoothScroll smoothScroll
     */
    fun scrollToCalendar(
        year: Int,
        month: Int,
        day: Int,
        smoothScroll: Boolean
    ) {
        scrollToCalendar(year, month, day, smoothScroll, true)
    }

    /**
     * 滚动到指定日期
     *
     * @param year           year
     * @param month          month
     * @param day            day
     * @param smoothScroll   smoothScroll
     * @param invokeListener 调用日期事件
     */
    fun scrollToCalendar(
        year: Int,
        month: Int,
        day: Int,
        smoothScroll: Boolean,
        invokeListener: Boolean
    ) {
        val calendar = Calendar()
        calendar.year = year
        calendar.month = month
        calendar.day = day
        if (!calendar.isAvailable) {
            return
        }
        if (!isInRange(calendar)) {
            return
        }
        if (mDelegate.mCalendarInterceptListener != null &&
            mDelegate.mCalendarInterceptListener.onCalendarIntercept(calendar)
        ) {
            mDelegate.mCalendarInterceptListener.onCalendarInterceptClick(calendar, false)
            return
        }
        if (mWeekPager.visibility == View.VISIBLE) {
            mWeekPager.scrollToCalendar(year, month, day, smoothScroll, invokeListener)
        } else {
            mMonthPager.scrollToCalendar(year, month, day, smoothScroll, invokeListener)
        }
    }


    /**
     * 设置月视图是否可滚动
     *
     * @param monthViewScrollable 设置月视图是否可滚动
     */
    fun setMonthViewScrollable(monthViewScrollable: Boolean) {
        mDelegate.isMonthViewScrollable = monthViewScrollable
    }


    /**
     * 设置周视图是否可滚动
     *
     * @param weekViewScrollable 设置周视图是否可滚动
     */
    fun setWeekViewScrollable(weekViewScrollable: Boolean) {
        mDelegate.isWeekViewScrollable = weekViewScrollable
    }

    /**
     * 设置年视图是否可滚动
     *
     * @param yearViewScrollable 设置年视图是否可滚动
     */
    fun setYearViewScrollable(yearViewScrollable: Boolean) {
        mDelegate.isYearViewScrollable = yearViewScrollable
    }


    fun setDefaultMonthViewSelectDay() {
        mDelegate.defaultCalendarSelectDay = CalendarViewDelegate.FIRST_DAY_OF_MONTH
    }

    fun setLastMonthViewSelectDay() {
        mDelegate.defaultCalendarSelectDay = CalendarViewDelegate.LAST_MONTH_VIEW_SELECT_DAY
    }

    fun setLastMonthViewSelectDayIgnoreCurrent() {
        mDelegate.defaultCalendarSelectDay =
            CalendarViewDelegate.LAST_MONTH_VIEW_SELECT_DAY_IGNORE_CURRENT
    }

    /**
     * 清除选择范围
     */
    fun clearSelectRange() {
        mDelegate.clearSelectRange()
        mMonthPager.clearSelectRange()
        mWeekPager.clearSelectRange()
    }

    /**
     * 清除单选
     */
    fun clearSingleSelect() {
        mDelegate.mSelectedCalendar = Calendar()
        mMonthPager.clearSingleSelect()
        mWeekPager.clearSingleSelect()
    }

    /**
     * 清除多选
     */
    fun clearMultiSelect() {
        mDelegate.mSelectedCalendars.clear()
        mMonthPager.clearMultiSelect()
        mWeekPager.clearMultiSelect()
    }

    /**
     * 添加选择
     *
     * @param calendars calendars
     */
    fun putMultiSelect(vararg calendars: Calendar?) {
        if (calendars == null || calendars.size == 0) {
            return
        }
        for (calendar in calendars) {
            if (calendar == null || mDelegate.mSelectedCalendars.containsKey(calendar.toString())) {
                continue
            }
            mDelegate.mSelectedCalendars[calendar.toString()] = calendar
        }
        update()
    }

    /**
     * 清楚一些多选日期
     *
     * @param calendars calendars
     */
    fun removeMultiSelect(vararg calendars: Calendar?) {
        if (calendars.isEmpty()) {
            return
        }
        for (calendar in calendars) {
            if (calendar == null) {
                continue
            }
            if (mDelegate.mSelectedCalendars.containsKey(calendar.toString())) {
                mDelegate.mSelectedCalendars.remove(calendar.toString())
            }
        }
        update()
    }


    fun getMultiSelectCalendars(): List<Calendar> {
        val calendars: MutableList<Calendar> =
            ArrayList()
        if (mDelegate.mSelectedCalendars.isEmpty()) {
            return calendars
        }
        calendars.addAll(mDelegate.mSelectedCalendars.values)
        calendars.sort()
        return calendars
    }

    /**
     * 获取选中范围
     *
     * @return return
     */
    fun getSelectCalendarRange(): List<Calendar> {
        return mDelegate.selectCalendarRange
    }

    /**
     * 设置月视图项高度
     *
     * @param calendarItemHeight MonthView item height
     */
    private fun setCalendarItemHeight(calendarItemHeight: Int) {
        if (mDelegate.calendarItemHeight == calendarItemHeight) {
            return
        }
        mDelegate.calendarItemHeight = calendarItemHeight
        mMonthPager.updateItemHeight()
        mWeekPager.updateItemHeight()
    }


    /**
     * 设置月视图
     *
     * @param cls MonthView.class
     */
    fun setMonthView(cls: Class<*>?) {
        if (cls == null) {
            return
        }
        if (mDelegate.monthViewClass == cls) {
            return
        }
        mDelegate.monthViewClass = cls
        mMonthPager.updateMonthViewClass()
    }

    /**
     * 设置周视图
     *
     * @param cls WeekView.class
     */
    fun setWeekView(cls: Class<*>?) {
        if (cls == null) {
            return
        }
        if (mDelegate.weekBarClass == cls) {
            return
        }
        mDelegate.weekViewClass = cls
        mWeekPager.updateWeekViewClass()
    }

    /**
     * 设置周栏视图
     *
     * @param cls WeekBar.class
     */
    fun setWeekBar(cls: Class<*>?) {
        if (cls == null) {
            return
        }
        if (mDelegate.weekBarClass == cls) {
            return
        }
        mDelegate.weekBarClass = cls

        this.removeView(mWeekBar)
        try {
            val constructor =
                cls.getConstructor(Context::class.java)
            mWeekBar = constructor.newInstance(context) as WeekBar
        } catch (e: Exception) {
            e.printStackTrace()
        }
        addView(mWeekBar, 2)
        mWeekBar.setup(mDelegate)
        mWeekBar.onWeekStartChange(mDelegate.weekStart)
        mMonthPager.mWeekBar = mWeekBar
        mWeekBar.onDateSelected(mDelegate.mSelectedCalendar, mDelegate.weekStart, false)
    }


    /**
     * 添加日期拦截事件
     * 使用此方法，只能基于select_mode = single_mode
     * 否则的话，如果标记全部日期为不可点击，那是没有意义的，
     * 框架本身也不可能在滑动的过程中全部去判断每个日期的可点击性
     *
     * @param listener listener
     */
    fun setOnCalendarInterceptListener(listener: CalendarView.OnCalendarInterceptListener?) {
        if (listener == null) {
            mDelegate.mCalendarInterceptListener = null
        }
        if (listener == null || mDelegate.selectMode == CalendarViewDelegate.SELECT_MODE_DEFAULT) {
            return
        }
        mDelegate.mCalendarInterceptListener = listener
        if (!listener.onCalendarIntercept(mDelegate.mSelectedCalendar)) {
            return
        }
        mDelegate.mSelectedCalendar = Calendar()
    }

    /**
     * 年份改变事件
     *
     * @param listener listener
     */
    fun setOnYearChangeListener(listener: CalendarView.OnYearChangeListener?) {
        mDelegate.mYearChangeListener = listener
    }

    /**
     * 月份改变事件
     *
     * @param listener listener
     */
    fun setOnMonthChangeListener(listener: CalendarView.OnMonthChangeListener?) {
        mDelegate.mMonthChangeListener = listener
    }


    /**
     * 周视图切换监听
     *
     * @param listener listener
     */
    fun setOnWeekChangeListener(listener: CalendarView.OnWeekChangeListener?) {
        mDelegate.mWeekChangeListener = listener
    }

    /**
     * 日期选择事件
     *
     * @param listener listener
     */
    fun setOnCalendarSelectListener(listener: CalendarView.OnCalendarSelectListener?) {
        mDelegate.mCalendarSelectListener = listener
        if (mDelegate.mCalendarSelectListener == null) {
            return
        }
        if (mDelegate.selectMode != CalendarViewDelegate.SELECT_MODE_DEFAULT) {
            return
        }
        if (!isInRange(mDelegate.mSelectedCalendar)) {
            return
        }
        mDelegate.updateSelectCalendarScheme()
    }


    /**
     * 日期选择事件
     *
     * @param listener listener
     */
    fun setOnCalendarRangeSelectListener(listener: CalendarView.OnCalendarRangeSelectListener?) {
        mDelegate.mCalendarRangeSelectListener = listener
    }

    /**
     * 日期多选事件
     *
     * @param listener listener
     */
    fun setOnCalendarMultiSelectListener(listener: CalendarView.OnCalendarMultiSelectListener?) {
        mDelegate.mCalendarMultiSelectListener = listener
    }

    /**
     * 设置最小范围和最大访问，default：minRange = -1，maxRange = -1 没有限制
     *
     * @param minRange minRange
     * @param maxRange maxRange
     */
    fun setSelectRange(minRange: Int, maxRange: Int) {
        if (minRange > maxRange) {
            return
        }
        mDelegate.setSelectRange(minRange, maxRange)
    }


    fun setSelectStartCalendar(startYear: Int, startMonth: Int, startDay: Int) {
        if (mDelegate.selectMode != CalendarViewDelegate.SELECT_MODE_RANGE) {
            return
        }
        val startCalendar = Calendar()
        startCalendar.year = startYear
        startCalendar.month = startMonth
        startCalendar.day = startDay
        setSelectStartCalendar(startCalendar)
    }

    fun setSelectStartCalendar(startCalendar: Calendar?) {
        if (mDelegate.selectMode != CalendarViewDelegate.SELECT_MODE_RANGE) {
            return
        }
        if (startCalendar == null) {
            return
        }
        if (!isInRange(startCalendar)) {
            if (mDelegate.mCalendarRangeSelectListener != null) {
                mDelegate.mCalendarRangeSelectListener.onSelectOutOfRange(startCalendar, true)
            }
            return
        }
        if (onCalendarIntercept(startCalendar)) {
            if (mDelegate.mCalendarInterceptListener != null) {
                mDelegate.mCalendarInterceptListener.onCalendarInterceptClick(
                    startCalendar,
                    false
                )
            }
            return
        }
        mDelegate.mSelectedEndRangeCalendar = null
        mDelegate.mSelectedStartRangeCalendar = startCalendar
        scrollToCalendar(
            startCalendar.year,
            startCalendar.month,
            startCalendar.day
        )
    }

    fun setSelectEndCalendar(endYear: Int, endMonth: Int, endDay: Int) {
        if (mDelegate.selectMode != CalendarViewDelegate.SELECT_MODE_RANGE) {
            return
        }
        if (mDelegate.mSelectedStartRangeCalendar == null) {
            return
        }
        val endCalendar = Calendar()
        endCalendar.year = endYear
        endCalendar.month = endMonth
        endCalendar.day = endDay
        setSelectEndCalendar(endCalendar)
    }

    fun setSelectEndCalendar(endCalendar: Calendar?) {
        if (mDelegate.selectMode != CalendarViewDelegate.SELECT_MODE_RANGE) {
            return
        }
        if (mDelegate.mSelectedStartRangeCalendar == null) {
            return
        }
        setSelectCalendarRange(mDelegate.mSelectedStartRangeCalendar, endCalendar)
    }

    /**
     * 直接指定选择范围，set select calendar range
     *
     * @param startYear  startYear
     * @param startMonth startMonth
     * @param startDay   startDay
     * @param endYear    endYear
     * @param endMonth   endMonth
     * @param endDay     endDay
     */
    fun setSelectCalendarRange(
        startYear: Int, startMonth: Int, startDay: Int,
        endYear: Int, endMonth: Int, endDay: Int
    ) {
        if (mDelegate.selectMode != CalendarViewDelegate.SELECT_MODE_RANGE) {
            return
        }
        val startCalendar = Calendar()
        startCalendar.year = startYear
        startCalendar.month = startMonth
        startCalendar.day = startDay
        val endCalendar = Calendar()
        endCalendar.year = endYear
        endCalendar.month = endMonth
        endCalendar.day = endDay
        setSelectCalendarRange(startCalendar, endCalendar)
    }

    /**
     * 设置选择日期范围
     *
     * @param startCalendar startCalendar
     * @param endCalendar   endCalendar
     */
    fun setSelectCalendarRange(
        startCalendar: Calendar?,
        endCalendar: Calendar?
    ) {
        if (mDelegate.selectMode != CalendarViewDelegate.SELECT_MODE_RANGE) {
            return
        }
        if (startCalendar == null || endCalendar == null) {
            return
        }
        if (onCalendarIntercept(startCalendar)) {
            if (mDelegate.mCalendarInterceptListener != null) {
                mDelegate.mCalendarInterceptListener.onCalendarInterceptClick(
                    startCalendar,
                    false
                )
            }
            return
        }
        if (onCalendarIntercept(endCalendar)) {
            if (mDelegate.mCalendarInterceptListener != null) {
                mDelegate.mCalendarInterceptListener.onCalendarInterceptClick(
                    endCalendar,
                    false
                )
            }
            return
        }
        val minDiffer = endCalendar.differ(startCalendar)
        if (minDiffer < 0) {
            return
        }
        if (!isInRange(startCalendar) || !isInRange(endCalendar)) {
            return
        }


        //优先判断各种直接return的情况，减少代码深度
        if (mDelegate.minSelectRange != -1 && mDelegate.minSelectRange > minDiffer + 1) {
            if (mDelegate.mCalendarRangeSelectListener != null) {
                mDelegate.mCalendarRangeSelectListener.onSelectOutOfRange(endCalendar, true)
            }
            return
        } else if (mDelegate.maxSelectRange != -1 && mDelegate.maxSelectRange <
            minDiffer + 1
        ) {
            if (mDelegate.mCalendarRangeSelectListener != null) {
                mDelegate.mCalendarRangeSelectListener.onSelectOutOfRange(endCalendar, false)
            }
            return
        }
        if (mDelegate.minSelectRange == -1 && minDiffer == 0) {
            mDelegate.mSelectedStartRangeCalendar = startCalendar
            mDelegate.mSelectedEndRangeCalendar = null
            if (mDelegate.mCalendarRangeSelectListener != null) {
                mDelegate.mCalendarRangeSelectListener.onCalendarRangeSelect(
                    startCalendar,
                    false
                )
            }
            scrollToCalendar(
                startCalendar.year,
                startCalendar.month,
                startCalendar.day
            )
            return
        }
        mDelegate.mSelectedStartRangeCalendar = startCalendar
        mDelegate.mSelectedEndRangeCalendar = endCalendar
        if (mDelegate.mCalendarRangeSelectListener != null) {
            mDelegate.mCalendarRangeSelectListener.onCalendarRangeSelect(startCalendar, false)
            mDelegate.mCalendarRangeSelectListener.onCalendarRangeSelect(endCalendar, true)
        }
        scrollToCalendar(
            startCalendar.year,
            startCalendar.month,
            startCalendar.day
        )
    }

    /**
     * 是否拦截日期，此设置续设置mCalendarInterceptListener
     *
     * @param calendar calendar
     * @return 是否拦截日期
     */
    fun onCalendarIntercept(calendar: Calendar?): Boolean {
        return mDelegate.mCalendarInterceptListener != null &&
                mDelegate.mCalendarInterceptListener.onCalendarIntercept(calendar)
    }


    /**
     * 获得最大多选数量
     *
     * @return 获得最大多选数量
     */
    fun getMaxMultiSelectSize(): Int {
        return mDelegate.maxMultiSelectSize
    }

    /**
     * 设置最大多选数量
     *
     * @param maxMultiSelectSize 最大多选数量
     */
    fun setMaxMultiSelectSize(maxMultiSelectSize: Int) {
        mDelegate.maxMultiSelectSize = maxMultiSelectSize
    }

    /**
     * 最小选择范围
     *
     * @return 最小选择范围
     */
    fun getMinSelectRange(): Int {
        return mDelegate.minSelectRange
    }

    /**
     * 最大选择范围
     *
     * @return 最大选择范围
     */
    fun getMaxSelectRange(): Int {
        return mDelegate.maxSelectRange
    }

    /**
     * 日期长按事件
     *
     * @param listener listener
     */
    fun setOnCalendarLongClickListener(listener: CalendarView.OnCalendarLongClickListener?) {
        mDelegate.mCalendarLongClickListener = listener
    }

    /**
     * 日期长按事件
     *
     * @param preventLongPressedSelect 防止长按选择日期
     * @param listener                 listener
     */
    fun setOnCalendarLongClickListener(
        listener: CalendarView.OnCalendarLongClickListener?,
        preventLongPressedSelect: Boolean
    ) {
        mDelegate.mCalendarLongClickListener = listener
        mDelegate.isPreventLongPressedSelected = preventLongPressedSelect
    }

    /**
     * 视图改变事件
     *
     * @param listener listener
     */
    fun setOnViewChangeListener(listener: CalendarView.OnViewChangeListener?) {
        mDelegate.mViewChangeListener = listener
    }


    fun setOnYearViewChangeListener(listener: CalendarView.OnYearViewChangeListener?) {
        mDelegate.mYearViewChangeListener = listener
    }

    /**
     * 保持状态
     *
     * @return 状态
     */
    override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        val parcelable = super.onSaveInstanceState()
        bundle.putParcelable("super", parcelable)
        bundle.putSerializable("selected_calendar", mDelegate.mSelectedCalendar)
        bundle.putSerializable("index_calendar", mDelegate.mIndexCalendar)
        return bundle
    }

    /**
     * 恢复状态
     *
     * @param state 状态
     */
    override fun onRestoreInstanceState(state: Parcelable) {
        val bundle = state as Bundle
        val superData =
            bundle.getParcelable<Parcelable>("super")
        mDelegate.mSelectedCalendar =
            bundle.getSerializable("selected_calendar") as Calendar
        mDelegate.mIndexCalendar =
            bundle.getSerializable("index_calendar") as Calendar
        if (mDelegate.mCalendarSelectListener != null) {
            mDelegate.mCalendarSelectListener.onCalendarSelect(
                mDelegate.mSelectedCalendar,
                false
            )
        }
        if (mDelegate.mIndexCalendar != null) {
            scrollToCalendar(
                mDelegate.mIndexCalendar.year,
                mDelegate.mIndexCalendar.month,
                mDelegate.mIndexCalendar.day
            )
        }
        update()
        super.onRestoreInstanceState(superData)
    }

    private var hasInit = false
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val height = MeasureSpec.getSize(heightMeasureSpec)
        if (!mDelegate.isFullScreenCalendar) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }
        if (!hasInit){
            setCalendarItemHeight((height - mDelegate.weekBarHeight) / 6)
            hasInit=true
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    /**
     * 标记哪些日期有事件
     *
     * @param mSchemeDates mSchemeDatesMap 通过自己的需求转换即可
     */
    fun setSchemeDate(mSchemeDates: Map<String?, Calendar?>?) {
        mDelegate.mSchemeDatesMap = mSchemeDates
        mDelegate.updateSelectCalendarScheme()
        mMonthPager.updateScheme()
        mWeekPager.updateScheme()
    }

    /**
     * 清空日期标记
     */
    fun clearSchemeDate() {
        mDelegate.mSchemeDatesMap = null
        mDelegate.clearSelectedScheme()
        mMonthPager.updateScheme()
        mWeekPager.updateScheme()
    }

    /**
     * 添加事物标记
     *
     * @param calendar calendar
     */
    fun addSchemeDate(calendar: Calendar?) {
        if (calendar == null || !calendar.isAvailable) {
            return
        }
        if (mDelegate.mSchemeDatesMap == null) {
            mDelegate.mSchemeDatesMap =
                HashMap()
        }
        mDelegate.mSchemeDatesMap.remove(calendar.toString())
        mDelegate.mSchemeDatesMap[calendar.toString()] = calendar
        mDelegate.updateSelectCalendarScheme()
        mMonthPager.updateScheme()
        mWeekPager.updateScheme()
    }

    /**
     * 添加事物标记
     *
     * @param mSchemeDates mSchemeDates
     */
    fun addSchemeDate(mSchemeDates: Map<String?, Calendar?>?) {
        if (mSchemeDates == null || mSchemeDates.isEmpty()) {
            return
        }
        if (mDelegate.mSchemeDatesMap == null) {
            mDelegate.mSchemeDatesMap =
                HashMap<String, Calendar>()
        }
        mDelegate.addSchemes(mSchemeDates)
        mDelegate.updateSelectCalendarScheme()
        mMonthPager.updateScheme()
        mWeekPager.updateScheme()
    }

    /**
     * 移除某天的标记
     * 这个API是安全的
     *
     * @param calendar calendar
     */
    fun removeSchemeDate(calendar: Calendar?) {
        if (calendar == null) {
            return
        }
        if (mDelegate.mSchemeDatesMap == null || mDelegate.mSchemeDatesMap.size == 0) {
            return
        }
        mDelegate.mSchemeDatesMap.remove(calendar.toString())
        if (mDelegate.mSelectedCalendar == calendar) {
            mDelegate.clearSelectedScheme()
        }
        mMonthPager.updateScheme()
        mWeekPager.updateScheme()
    }

    /**
     * 设置背景色
     *
     * @param yearViewBackground 年份卡片的背景色
     * @param weekBackground     星期栏背景色
     * @param lineBg             线的颜色
     */
    fun setBackground(
        yearViewBackground: Int,
        weekBackground: Int,
        lineBg: Int
    ) {
        mWeekBar.setBackgroundColor(weekBackground)
        mWeekLine.setBackgroundColor(lineBg)
    }


    /**
     * 设置文本颜色
     *
     * @param currentDayTextColor      今天字体颜色
     * @param curMonthTextColor        当前月份字体颜色
     * @param otherMonthColor          其它月份字体颜色
     * @param curMonthLunarTextColor   当前月份农历字体颜色
     * @param otherMonthLunarTextColor 其它农历字体颜色
     */
    fun setTextColor(
        currentDayTextColor: Int,
        curMonthTextColor: Int,
        otherMonthColor: Int,
        curMonthLunarTextColor: Int,
        otherMonthLunarTextColor: Int
    ) {
        mDelegate.setTextColor(
            currentDayTextColor, curMonthTextColor,
            otherMonthColor, curMonthLunarTextColor, otherMonthLunarTextColor
        )
        mMonthPager.updateStyle()
        mWeekPager.updateStyle()
    }

    /**
     * 设置选择的效果
     *
     * @param selectedThemeColor     选中的标记颜色
     * @param selectedTextColor      选中的字体颜色
     * @param selectedLunarTextColor 选中的农历字体颜色
     */
    fun setSelectedColor(
        selectedThemeColor: Int,
        selectedTextColor: Int,
        selectedLunarTextColor: Int
    ) {
        mDelegate.setSelectColor(selectedThemeColor, selectedTextColor, selectedLunarTextColor)
        mMonthPager.updateStyle()
        mWeekPager.updateStyle()
    }

    /**
     * 定制颜色
     *
     * @param selectedThemeColor 选中的标记颜色
     * @param schemeColor        标记背景色
     */
    fun setThemeColor(selectedThemeColor: Int, schemeColor: Int) {
        mDelegate.setThemeColor(selectedThemeColor, schemeColor)
        mMonthPager.updateStyle()
        mWeekPager.updateStyle()
    }

    /**
     * 设置标记的色
     *
     * @param schemeLunarTextColor 标记农历颜色
     * @param schemeColor          标记背景色
     * @param schemeTextColor      标记字体颜色
     */
    fun setSchemeColor(
        schemeColor: Int,
        schemeTextColor: Int,
        schemeLunarTextColor: Int
    ) {
        mDelegate.setSchemeColor(schemeColor, schemeTextColor, schemeLunarTextColor)
        mMonthPager.updateStyle()
        mWeekPager.updateStyle()
    }


    /**
     * 设置星期栏的背景和字体颜色
     *
     * @param weekBackground 背景色
     * @param weekTextColor  字体颜色
     */
    fun setWeeColor(weekBackground: Int, weekTextColor: Int) {
        mWeekBar.setBackgroundColor(weekBackground)
        mWeekBar.setTextColor(weekTextColor)
    }

    /**
     * 默认选择模式
     */
    fun setSelectDefaultMode() {
        if (mDelegate.selectMode == CalendarViewDelegate.SELECT_MODE_DEFAULT) {
            return
        }
        mDelegate.mSelectedCalendar = mDelegate.mIndexCalendar
        mDelegate.selectMode = CalendarViewDelegate.SELECT_MODE_DEFAULT
        mWeekBar.onDateSelected(mDelegate.mSelectedCalendar, mDelegate.weekStart, false)
        mMonthPager.updateDefaultSelect()
        mWeekPager.updateDefaultSelect()
    }

    /**
     * 范围模式
     */
    fun setSelectRangeMode() {
        if (mDelegate.selectMode == CalendarViewDelegate.SELECT_MODE_RANGE) {
            return
        }
        mDelegate.selectMode = CalendarViewDelegate.SELECT_MODE_RANGE
        clearSelectRange()
    }

    /**
     * 多选模式
     */
    fun setSelectMultiMode() {
        if (mDelegate.selectMode == CalendarViewDelegate.SELECT_MODE_MULTI) {
            return
        }
        mDelegate.selectMode = CalendarViewDelegate.SELECT_MODE_MULTI
        clearMultiSelect()
    }

    /**
     * 单选模式
     */
    fun setSelectSingleMode() {
        if (mDelegate.selectMode == CalendarViewDelegate.SELECT_MODE_SINGLE) {
            return
        }
        mDelegate.selectMode = CalendarViewDelegate.SELECT_MODE_SINGLE
        mWeekPager.updateSelected()
        mMonthPager.updateSelected()
    }

    /**
     * 设置星期日周起始
     */
    fun setWeekStarWithSun() {
        setWeekStart(CalendarViewDelegate.WEEK_START_WITH_SUN)
    }

    /**
     * 设置星期一周起始
     */
    fun setWeekStarWithMon() {
        setWeekStart(CalendarViewDelegate.WEEK_START_WITH_MON)
    }

    /**
     * 设置星期六周起始
     */
    fun setWeekStarWithSat() {
        setWeekStart(CalendarViewDelegate.WEEK_START_WITH_SAT)
    }

    /**
     * 设置周起始
     * CalendarViewDelegate.WEEK_START_WITH_SUN
     * CalendarViewDelegate.WEEK_START_WITH_MON
     * CalendarViewDelegate.WEEK_START_WITH_SAT
     *
     * @param weekStart 周起始
     */
    fun setWeekStart(weekStart: Int) {
        if (weekStart != CalendarViewDelegate.WEEK_START_WITH_SUN && weekStart != CalendarViewDelegate.WEEK_START_WITH_MON && weekStart != CalendarViewDelegate.WEEK_START_WITH_SAT
        ) return
        if (weekStart == mDelegate.weekStart) return
        mDelegate.weekStart = weekStart
        mWeekBar.onWeekStartChange(weekStart)
        mWeekBar.onDateSelected(mDelegate.mSelectedCalendar, weekStart, false)
        mWeekPager.updateWeekStart()
        mMonthPager.updateWeekStart()
    }

    /**
     * 是否是单选模式
     *
     * @return isSingleSelectMode
     */
    fun isSingleSelectMode(): Boolean {
        return mDelegate.selectMode == CalendarViewDelegate.SELECT_MODE_SINGLE
    }

    /**
     * 设置显示模式为全部
     */
    fun setAllMode() {
        setShowMode(CalendarViewDelegate.MODE_ALL_MONTH)
    }

    /**
     * 设置显示模式为仅当前月份
     */
    fun setOnlyCurrentMode() {
        setShowMode(CalendarViewDelegate.MODE_ONLY_CURRENT_MONTH)
    }

    /**
     * 设置显示模式为填充
     */
    fun setFixMode() {
        setShowMode(CalendarViewDelegate.MODE_FIT_MONTH)
    }

    /**
     * 设置显示模式
     * CalendarViewDelegate.MODE_ALL_MONTH
     * CalendarViewDelegate.MODE_ONLY_CURRENT_MONTH
     * CalendarViewDelegate.MODE_FIT_MONTH
     *
     * @param mode 月视图显示模式
     */
    fun setShowMode(mode: Int) {
        if (mode != CalendarViewDelegate.MODE_ALL_MONTH && mode != CalendarViewDelegate.MODE_ONLY_CURRENT_MONTH && mode != CalendarViewDelegate.MODE_FIT_MONTH
        ) return
        if (mDelegate.monthViewShowMode == mode) return
        mDelegate.monthViewShowMode = mode
        mWeekPager.updateShowMode()
        mMonthPager.updateShowMode()
        mWeekPager.notifyDataSetChanged()
    }

    /**
     * 更新界面，
     * 重新设置颜色等都需要调用该方法
     */
    fun update() {
        mWeekBar.onWeekStartChange(mDelegate.weekStart)
        mMonthPager.updateScheme()
        mWeekPager.updateScheme()
    }

    /**
     * 更新周视图
     */
    fun updateWeekBar() {
        mWeekBar.onWeekStartChange(mDelegate.weekStart)
    }


    /**
     * 更新当前日期
     */
    fun updateCurrentDate() {
        val calendar = java.util.Calendar.getInstance()
        val day = calendar[java.util.Calendar.DAY_OF_MONTH]
        if (getCurDay() == day) {
            return
        }
        mDelegate.updateCurrentDay()
        mMonthPager.updateCurrentDate()
        mWeekPager.updateCurrentDate()
    }

    /**
     * 获取当前周数据
     *
     * @return 获取当前周数据
     */
    fun getCurrentWeekCalendars(): List<Calendar?>? {
        return mWeekPager.currentWeekCalendars
    }


    /**
     * 获取当前月份日期
     *
     * @return return
     */
    fun getCurrentMonthCalendars(): List<Calendar?>? {
        return mMonthPager.currentMonthCalendars
    }

    /**
     * 获取选择的日期
     *
     * @return 获取选择的日期
     */
    fun getSelectedCalendar(): Calendar? {
        return mDelegate.mSelectedCalendar
    }

    /**
     * 获得最小范围日期
     *
     * @return 最小范围日期
     */
    fun getMinRangeCalendar(): Calendar? {
        return mDelegate.minRangeCalendar
    }


    /**
     * 获得最大范围日期
     *
     * @return 最大范围日期
     */
    fun getMaxRangeCalendar(): Calendar? {
        return mDelegate.maxRangeCalendar
    }

    /**
     * MonthViewPager
     *
     * @return 获得月视图
     */
    fun getMonthViewPager(): MonthViewPager? {
        return mMonthPager
    }

    /**
     * 获得周视图
     *
     * @return 获得周视图
     */
    fun getWeekViewPager(): WeekViewPager? {
        return mWeekPager
    }

    /**
     * 是否在日期范围内
     *
     * @param calendar calendar
     * @return 是否在日期范围内
     */
    fun isInRange(calendar: Calendar?): Boolean {
        return CalendarUtil.isCalendarInRange(calendar, mDelegate)
    }


}