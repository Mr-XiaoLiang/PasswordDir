package com.lollipop.passworddir.view

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

    private val messageList = ArrayList<MessageInfo>()

    init {
        recyclerView.layoutManager = LinearLayoutManager(
            recyclerView.context, RecyclerView.VERTICAL, false
        )
        recyclerView.adapter = MessageAdapter(messageList)
    }

    private fun addMessage(info: MessageInfo) {
        val index = messageList.size
        messageList.add(info)
        recyclerView.adapter?.notifyItemInserted(index)
        recyclerView.smoothScrollToPosition(index)
    }

    fun post(info: MessageInfo) {
        recyclerView.post {
            addMessage(info)
        }
    }

    fun post(info: CharSequence) {
        recyclerView.post {
            addMessage(MessageInfo(info))
        }
    }

    private class MessageAdapter(
        private val data: List<MessageInfo>
    ) : RecyclerView.Adapter<MessageHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageHolder {
            return MessageHolder.create(parent)
        }

        override fun onBindViewHolder(holder: MessageHolder, position: Int) {
            holder.bind(data[position])
        }

        override fun getItemCount(): Int {
            return data.size
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

        fun bind(info: MessageInfo) {
            RichTextHelper.startRichFlow()
                .optional(!info.isSub) {
                    addInfo("➜ ", startArrowColor)
                }
                .addInfo(info.info)
                .into(binding.messageItemView)
        }

    }

    data class MessageInfo(
        val info: CharSequence,
        val isSub: Boolean = false
    )

}