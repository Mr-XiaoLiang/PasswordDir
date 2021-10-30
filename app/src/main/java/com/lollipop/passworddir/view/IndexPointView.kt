package com.lollipop.passworddir.view

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import com.lollipop.passworddir.R
import kotlin.math.min

/**
 * @author lollipop
 * @date 2021/10/30 20:56
 */
class IndexPointView(
    context: Context, attr: AttributeSet?, style: Int
) : androidx.appcompat.widget.AppCompatImageView(context, attr, style) {

    constructor(context: Context, attr: AttributeSet?) : this(context, attr, 0)
    constructor(context: Context) : this(context, null)

    private val pointDrawable = PointDrawable()

    init {
        setImageDrawable(pointDrawable)
    }

    var selectedColor: Int
        get() {
            return pointDrawable.selectedColor
        }
        set(value) {
            pointDrawable.selectedColor = value
        }

    var defaultColor: Int
        get() {
            return pointDrawable.defaultColor
        }
        set(value) {
            pointDrawable.defaultColor = value
        }

    var selectedIndex: Int
        get() {
            return pointDrawable.selectedIndex
        }
        set(value) {
            pointDrawable.selectedIndex = value
        }

    var pointCount: Int
        get() {
            return pointDrawable.pointCount
        }
        set(value) {
            pointDrawable.pointCount = value
        }

    var pointInterval: Int
        get() {
            return pointDrawable.pointInterval
        }
        set(value) {
            pointDrawable.pointInterval = value
        }

    init {
        attr?.let { attrs ->
            val typedArray =
                context.obtainStyledAttributes(attrs, R.styleable.IndexPointView)
            selectedColor = typedArray.getColor(R.styleable.IndexPointView_selectedColor, Color.WHITE)
            defaultColor = typedArray.getColor(R.styleable.IndexPointView_defaultColor, Color.WHITE)
            selectedIndex = typedArray.getInteger(R.styleable.IndexPointView_selectedIndex, 0)
            pointCount = typedArray.getInteger(R.styleable.IndexPointView_pointCount, 0)
            pointInterval = typedArray.getDimensionPixelSize(R.styleable.IndexPointView_pointInterval, 0)
            typedArray.recycle()
        }
    }

    fun applyChange(callback: IndexPointView.() -> Unit) {
        callback(this)
        pointDrawable.notifyPointChanged()
    }

    private class PointDrawable : Drawable() {

        var selectedColor: Int = Color.WHITE

        var defaultColor: Int = Color.GRAY

        var selectedIndex: Int = 0

        var pointCount = 0

        var pointInterval: Int = 5

        private var radius = 0F
        private var pointLeft = 0F
        private var pointY = 0F

        private val paint = Paint().apply {
            isAntiAlias = true
            isDither = true
            style = Paint.Style.FILL
        }

        override fun onBoundsChange(bounds: Rect) {
            super.onBoundsChange(bounds)
            calculate()
        }

        fun notifyPointChanged() {
            calculate()
            invalidateSelf()
        }

        private fun calculate() {
            if (pointCount == 0) {
                return
            }
            val width = bounds.width()
            val radiusW = (width - pointInterval) * 1F / pointCount - pointInterval
            val radiusH = bounds.height().toFloat()
            val diameter = min(radiusW, radiusH)
            radius = diameter / 2
            val contentLength = (diameter + pointInterval) * pointCount - pointInterval
            pointLeft = (width - contentLength) * 0.5F
            pointY = bounds.exactCenterY()
        }

        override fun draw(canvas: Canvas) {
            if (pointCount == 0) {
                return
            }
            val diameter = radius * 2
            val step = pointInterval + diameter
            for (i in 0 until pointCount) {
                paint.color = if (i == selectedIndex) {
                    selectedColor
                } else {
                    defaultColor
                }
                val pointX = pointLeft + radius + (step * i)
                canvas.drawCircle(pointX, pointY, radius, paint)
            }
        }

        override fun setAlpha(alpha: Int) {
            paint.alpha = alpha
        }

        override fun setColorFilter(colorFilter: ColorFilter?) {
            paint.colorFilter = colorFilter
        }

        override fun getOpacity(): Int {
            return PixelFormat.TRANSPARENT
        }

    }

}