package com.lollipop.passworddir.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PointF
import android.text.*
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


/**
 * @author lollipop
 * @date 2021/10/11 20:33
 */
class RichTextHelper {

    companion object {
        /**
         * 从空白开始一个富翁本解析工作
         */
        fun startRichFlow(): RichTextHelper {
            return RichTextHelper()
        }

    }

    /**
     * span信息的集合
     */
    private val spanList = ArrayList<CharSequence>()

    /**
     * 用于设置TextView的链接相关的属性
     * 理论上构建的过程不需要TextView参与，
     * 但是部分富文本的设置需要对View的属性进行设置，因此此处进行记录，
     * 当必要的时候，进行设置
     */
    var hasLink = false
        private set

    /**
     * 点击事件的聚合接口
     */
    private var onClickListener: OnClickListener? = null

    /**
     * 添加一个点击的内容
     */
    fun <T : Any> addClickInfo(
        value: String,
        textColor: Int,
        info: T,
        type: ClickEvent,
        clickListener: (T, ClickEvent) -> Unit
    ): RichTextHelper {
        hasLink = true
        addSpan(
            value,
            ForegroundColorSpan(textColor),
            ClickSpan(textColor, ClickWrapper(info, type, clickListener))
        )
        return this
    }

    fun addInfo(info: String, color: Int): RichTextHelper {
        addSpan(info, ForegroundColorSpan(color))
        return this
    }

    /**
     * 添加一个原始的内容信息
     */
    fun addInfo(info: CharSequence): RichTextHelper {
        addSpan(info)
        return this
    }

    /**
     * 用于部分需要判断条件的可选富文本条件
     */
    fun optional(value: Boolean, callback: RichTextHelper.() -> Unit): RichTextHelper {
        if (value) {
            callback(this)
        }
        return this
    }

    /**
     * 添加一个Span到集合中
     */
    private fun addSpan(charSequence: CharSequence) {
        spanList.add(charSequence)
    }

    /**
     * 添加一个字符串以及它的span信息
     * 将会为它创建一个Builder，完成组合后添加到span的集合中
     * 允许对同一段位置设置多个span
     */
    private fun addSpan(
        value: String,
        vararg span: Any
    ) {
        addSpan(SpannableStringBuilder(value).addSpan(0, value.length, *span))
    }

    /**
     * Span的Builder的扩展方法，可以直接指定开始位置以及长度的方式来设置span
     * 它允许对同一段文字设置多个sapn
     */
    private fun SpannableStringBuilder.addSpan(
        start: Int,
        length: Int,
        vararg spans: Any
    ): SpannableStringBuilder {
        val startIndex = min(this.length, max(start, 0))
        val endIndex = min(this.length, max(length + startIndex, 0))
        for (span in spans) {
            try {
                setSpan(span, startIndex, endIndex, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
        return this
    }

    /**
     * 绑定一个点击的监听器
     * @param listener 监听的回调函数
     */
    fun onClick(listener: OnClickListener): RichTextHelper {
        this.onClickListener = listener
        return this
    }

    private fun onClick(data: String, event: ClickEvent) {
        onClickListener?.onClick(data, event)
    }

    private fun getColor(context: Context, @ColorRes colorId: Int): Int {
        return ContextCompat.getColor(context, colorId)
    }

    /**
     * 将现有的调整内容，转换为TextView可以使用的内容
     */
    fun build(): SpannableStringBuilder {
        val spannableBuilder = SpannableStringBuilder()
        spanList.forEach {
            spannableBuilder.append(it)
        }
        return spannableBuilder
    }

    /**
     * 将自身绑定到TextView中，并且提供一些默认到属性设置
     */
    fun into(textView: TextView) {
        val spannableText = build()
        textView.text = spannableText
        if (hasLink) {
            SpanClickHelper.bindTo(textView, spannableText, true)
        } else {
            SpanClickHelper.clean(textView)
        }
    }

    /**
     * 聚合的点击事件接口
     */
    fun interface OnClickListener {
        fun onClick(data: String, event: ClickEvent)
    }

    private class ClickSpan<T : Any>(
        private val textColor: Int,
        private val clickListener: ClickWrapper<T>
    ) : ClickableSpan() {

        override fun onClick(widget: View) {
            clickListener.invoke()
        }

        override fun updateDrawState(ds: TextPaint) {
            ds.color = if (textColor == 0) {
                ds.linkColor
            } else {
                textColor
            }
            ds.isUnderlineText = false
        }
    }

    private data class ClickWrapper<T : Any>(
        private val data: T,
        private val event: ClickEvent = ClickEvent.LINK,
        private val listener: (T, ClickEvent) -> Unit
    ) {

        fun invoke() {
            listener.invoke(data, event)
        }

    }

    enum class ClickEvent {
        LINK,
    }

    /**
     * 为了解决TextView布局以及ClickSpan的矛盾问题，
     * 所以使用TouchListener来手动触发Span的点击事件
     * 问题场景：常规情况下使用ClickSpan，需要设置setMovementMethod，
     * 但是此方法会导致TextView的最大行数设置产生的末尾省略号丢失，
     * 并且TextView会发生滑动，导致在列表中文本显示不完整。
     * 因此使用OnTouchListener来代替LinkMovementMethod
     */
    private class SpanClickHelper
    private constructor(
        private val spannable: Spannable,
        private val touchSlop: Int,
        private val clickFirst: Boolean
    ) : View.OnTouchListener {

        companion object {
            @SuppressLint("ClickableViewAccessibility")
            fun bindTo(textView: TextView, spannable: Spannable, clickFirst: Boolean) {
                textView.setOnTouchListener(
                    SpanClickHelper(
                        spannable,
                        ViewConfiguration.get(textView.context).scaledTouchSlop,
                        clickFirst
                    )
                )
            }

            @SuppressLint("ClickableViewAccessibility")
            fun clean(textView: TextView) {
                textView.setOnTouchListener(null)
            }
        }

        private var cancelClick = false
        private val touchDownLocation = PointF()

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            v ?: return false
            if (v !is TextView) {
                return false
            }
            when (event?.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    // 按下时，重制状态，并且如果此时没有被选中的ClickableSpan也直接放弃
                    cancelClick = false
                    touchDownLocation.set(event.x, event.y)
                    val offset = getSpanOffset(v, event)
                    val spans = spannable.getSpans(offset, offset, ClickableSpan::class.java)
                    if (spans.isNotEmpty()) {
                        Selection.setSelection(
                            spannable,
                            spannable.getSpanStart(spans[0]),
                            spannable.getSpanEnd(spans[0])
                        )
                    } else {
                        cancelClick = true
                        Selection.removeSelection(spannable)
                        return false
                    }
                }
                MotionEvent.ACTION_POINTER_DOWN -> {
                    // 多指操作，放弃点击事件
                    cancelClick = true
                    Selection.removeSelection(spannable)
                    return false
                }
                MotionEvent.ACTION_MOVE -> {
                    // 如果已经放弃，那么就直接返回，如果没有，那么就检查手指的移动范围，
                    // 超过范围之后，认为发生滑动事件，放弃点击事件的触发
                    if (cancelClick) {
                        return false
                    }
                    val x = event.x
                    val y = event.y
                    // 滑动超过阈值，放弃事件
                    if (abs(touchDownLocation.x - x) > touchSlop
                        || abs(touchDownLocation.y - y) > touchSlop
                    ) {
                        cancelClick = true
                        Selection.removeSelection(spannable)
                        return false
                    }
                }
                MotionEvent.ACTION_UP -> {
                    // 如果已经被放弃事件，那么不再处理
                    if (cancelClick) {
                        return false
                    }
                    // 获取点击位置相对于Span的偏移量，以此来获取点击的span，并且触发它
                    val offset = getSpanOffset(v, event)
                    val spans = spannable.getSpans(offset, offset, ClickableSpan::class.java)
                    if (spans.isNotEmpty()) {
                        if (clickFirst) {
                            spans[0].onClick(v)
                        } else {
                            spans.forEach {
                                it.onClick(v)
                            }
                        }
                    }
                    Selection.removeSelection(spannable)
                }
            }
            return true
        }

        private fun getSpanOffset(v: TextView, event: MotionEvent): Int {
            var x = event.x
            var y = event.y.toInt()
            x -= v.totalPaddingLeft
            y -= v.totalPaddingTop
            x += v.scrollX
            y += v.scrollY

            val layout = v.layout
            val lineForVertical = layout.getLineForVertical(y)
            return layout.getOffsetForHorizontal(lineForVertical, x)
        }

    }

}