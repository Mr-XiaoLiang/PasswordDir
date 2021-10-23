package com.lollipop.passworddir.view

import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.ImageView

/**
 * @author lollipop
 * @date 2021/10/23 21:16
 */
class StatusViewHelper : View.OnClickListener {

    companion object {
        const val STATUS_OFF = "STATUS_OFF"
        const val STATUS_ON = "STATUS_ON"
        const val STATUS_EMPTY = ""

        /**
         * @param btn 点击事件绑定的对象
         * @param statusView 状态显示的View
         * @param map key: status value，value：status color
         * @param listener 状态的监听器
         */
        fun View.bindColorStatus(
            statusView: ImageView,
            map: Map<String, Int>,
            listener: OnStatusChangedListener? = null
        ): StatusViewHelper {
            val btn = this
            return StatusViewHelper().apply {
                btn.setOnClickListener(this)
                val statusMap = HashMap<String, Int>()
                map.forEach { entry ->
                    addStatusType(entry.key)
                    statusMap[entry.key] = entry.value
                }
                onStatusChanged(ColorStatusImpl(statusView, statusMap, listener))
            }
        }
    }

    private val statusList = ArrayList<String>()
    private var statusChangedListener: OnStatusChangedListener? = null

    private var statusIndex = 0

    val currentStatus: String
        get() {
            return if (statusList.isEmpty()) {
                STATUS_EMPTY
            } else {
                statusList[statusIndex]
            }
        }

    fun update() {
        statusChangedListener?.onStatusChanged(currentStatus)
    }

    fun setStatus(status: String) {
        val index = statusList.indexOf(status)
        statusIndex = if (index < 0) {
            statusList.add(status)
            statusList.indexOf(status)
        } else {
            index
        }
        update()
    }

    fun addStatusType(status: String) {
        statusList.add(status)
    }

    override fun onClick(v: View?) {
        if (statusList.isEmpty()) {
            return
        }
        statusIndex++
        statusIndex %= statusList.size
        statusChangedListener?.onStatusChanged(currentStatus)
    }

    fun onStatusChanged(listener: OnStatusChangedListener?) {
        this.statusChangedListener = listener
    }

    fun interface OnStatusChangedListener {
        fun onStatusChanged(status: String)
    }

    class ColorStatusImpl(
        private val statusView: ImageView,
        private val colorMap: Map<String, Int>,
        private val statusChangedListener: OnStatusChangedListener? = null
    ) : OnStatusChangedListener {

        override fun onStatusChanged(status: String) {
            val color = colorMap[status] ?: return
            val drawable = statusView.drawable
            if (drawable is ColorDrawable) {
                drawable.color = color
                drawable.invalidateSelf()
            } else {
                statusView.setImageDrawable(ColorDrawable(color))
            }
            statusChangedListener?.onStatusChanged(status)
        }

    }

}