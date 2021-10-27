package com.lollipop.passworddir.view

import com.lollipop.passworddir.util.task
import java.util.*

/**
 * @author lollipop
 * @date 2021/10/27 22:39
 */
class SmoothMessageHelper(private val messageViewHelper: MessageViewHelper) {

    private var isRunning = false

    private val cacheList = LinkedList<MessageViewHelper.MessageInfo>()

    private val messageLooper = task {
        if (cacheList.isNotEmpty()) {
            val first = cacheList.removeFirst()
            messageViewHelper.post(first)
        }
        checkNext()
    }

    fun post(info: MessageViewHelper.MessageInfo) {
        cacheList.addLast(info)
    }

    fun post(value: String, isSub: Boolean = false) {
        post(MessageViewHelper.MessageInfo(value, isSub))
    }

    private fun checkNext() {
        if (isRunning) {
            messageLooper.delay(100L)
        } else if (cacheList.isNotEmpty()) {
            messageViewHelper.postAll(cacheList)
        }
    }

    fun start() {
        isRunning = true
        messageLooper.sync()
    }

    fun stop() {
        isRunning = false
        messageLooper.cancel()
        checkNext()
    }

}