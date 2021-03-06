package com.lollipop.passworddir.view

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.passworddir.R
import com.lollipop.passworddir.databinding.ItemMessageBinding
import com.lollipop.passworddir.util.RichTextHelper
import com.lollipop.passworddir.util.bind

/**
 * @author lollipop
 * @date 2021/10/23 20:46
 */
class MessageViewHelper(private val recyclerView: RecyclerView) {

    companion object {
        private const val MESSAGE_LIST_SIZE = 25000
        private const val MAX_LIST_SIZE = 20000
        private const val KEEP_LIST_SIZE = 5000
        private const val LOCK = "MessageViewHelper.LOCK"
    }

    private var messageList = ArrayList<MessageInfo>(MESSAGE_LIST_SIZE)

    init {
        recyclerView.layoutManager = LinearLayoutManager(
            recyclerView.context, RecyclerView.VERTICAL, false
        )
        recyclerView.adapter = MessageAdapter(::getDataCount, ::getDataByIndex)
    }

    private fun getDataCount(): Int {
        return messageList.size
    }

    private fun getDataByIndex(position: Int): MessageInfo {
        return messageList[position]
    }

    private fun addMessage(info: MessageInfo) {
        synchronized(LOCK) {
            val index = messageList.size
            messageList.add(info)
            recyclerView.adapter?.notifyItemInserted(index)
            recyclerView.smoothScrollToPosition(index)

            if (messageList.size > MAX_LIST_SIZE) {
                val size = messageList.size
                val removeCount = size - KEEP_LIST_SIZE
                val subList = messageList.subList(size - KEEP_LIST_SIZE, size - 1)
                messageList = ArrayList(subList)
                recyclerView.adapter?.notifyItemRangeRemoved(0, removeCount)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun addMessage(infoList: List<MessageInfo>) {
        synchronized(LOCK) {
            if (infoList.size + messageList.size > MAX_LIST_SIZE) {
                if (infoList.size > MAX_LIST_SIZE) {
                    val size = infoList.size
                    messageList = ArrayList(infoList.subList(size - KEEP_LIST_SIZE, size - 1))
                    recyclerView.adapter?.notifyDataSetChanged()
                    return
                }
                val newSize = infoList.size
                val oldSize = messageList.size
                messageList =
                    ArrayList(messageList.subList(oldSize - KEEP_LIST_SIZE + newSize, oldSize - 1))
                messageList.addAll(infoList)
                recyclerView.adapter?.notifyDataSetChanged()
                return
            }
            val index = messageList.size
            messageList.addAll(infoList)
            recyclerView.adapter?.notifyItemRangeInserted(index, infoList.size)
            recyclerView.smoothScrollToPosition(messageList.size - 1)
        }
    }

    fun post(info: MessageInfo) {
        recyclerView.post {
            addMessage(info)
        }
    }

    fun post(
        info: CharSequence,
        isSub: Boolean = false,
        isError: Boolean = false
    ) {
        recyclerView.post {
            addMessage(MessageInfo(info, isSub, isError))
        }
    }

    fun postAll(infoList: List<MessageInfo>) {
        recyclerView.post {
            addMessage(infoList)
        }
    }

    private class MessageAdapter(
        private val dataSize: () -> Int,
        private val getData: (Int) -> MessageInfo
    ) : RecyclerView.Adapter<MessageHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageHolder {
            return MessageHolder.create(parent)
        }

        override fun onBindViewHolder(holder: MessageHolder, position: Int) {
            holder.bind(getData(position))
        }

        override fun getItemCount(): Int {
            return dataSize()
        }

    }

    private class MessageHolder(private val binding: ItemMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun create(parent: ViewGroup): MessageHolder {
                return MessageHolder(parent.bind(true))
            }
        }

        private val startArrowColor: Int by lazy {
            ContextCompat.getColor(itemView.context, R.color.message_arrow)
        }

        private val errorColor: Int by lazy {
            ContextCompat.getColor(itemView.context, R.color.message_error)
        }

        init {
            binding.messageItemView.setOnLongClickListener {
                EditDialog.show(it.context, binding.messageItemView.text ?: "")
                true
            }
        }

        fun bind(info: MessageInfo) {
            RichTextHelper.startRichFlow()
                .optional(!info.isSub) {
                    addInfo("??? ", startArrowColor)
                }
                .optional(info.isError) {
                    addInfo(info.info.toString(), errorColor)
                }
                .optional(!info.isError) {
                    addInfo(info.info)
                }
                .into(binding.messageItemView)
        }

    }

    data class MessageInfo(
        val info: CharSequence,
        val isSub: Boolean = false,
        val isError: Boolean = false
    )

}