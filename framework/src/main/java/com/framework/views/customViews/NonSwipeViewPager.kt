package com.framework.views.customViews

import android.content.Context
import android.util.AttributeSet
import androidx.viewpager.widget.ViewPager
import android.view.MotionEvent
import android.view.View
import android.view.View.MeasureSpec
import java.lang.Exception


class NonSwipeViewPager : ViewPager {

    private var mCurrentPagePosition = 0

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var heightMeasureSpecLoc = heightMeasureSpec
        try {
            val wrapHeight = MeasureSpec.getMode(heightMeasureSpecLoc) == MeasureSpec.AT_MOST
            if (wrapHeight) {
                val child: View? = getChildAt(mCurrentPagePosition)
                if (child != null) {
                    child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
                    val h: Int = child.measuredHeight
                    heightMeasureSpecLoc = MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpecLoc)
    }

    fun reMeasureCurrentPage(position: Int) {
        mCurrentPagePosition = position
        requestLayout()
    }

    override fun onInterceptTouchEvent(event: MotionEvent?): Boolean {
        // Never allow swiping to switch between pages
        return false
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        // Never allow swiping to switch between pages
        return false
    }
}