package com.lollipop.passworddir.view

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.passworddir.databinding.ItemWheelBinding
import com.lollipop.passworddir.util.bind

/**
 * @author lollipop
 * @date 2021/10/24 21:00
 */
@SuppressLint("NotifyDataSetChanged")
class HorizontalWheelHelper(private val recyclerView: RecyclerView) {

    companion object {
        val EMPTY = WheelInfo("", "")

        fun RecyclerView.bindHorizontalWheel(
            info: Array<WheelInfo>,
            listener: OnSelectedChangedListener? = null
        ): HorizontalWheelHelper {
            return HorizontalWheelHelper(this).apply {
                addItem(info)
                if (listener != null) {
                    onSelectedChanged(listener)
                }
            }
        }
    }

    private val itemList = ArrayList<WheelInfo>()
    private var selectedChangedListener: OnSelectedChangedListener? = null
    private val layoutManager: LBannerLayoutManager by lazy {
        LBannerLayoutManager().apply {
            onSelectedListener = LBannerLayoutManager.OnSelectedListener { pos, state ->
                if (state == LBannerLayoutManager.ScrollState.IDLE) {
                    onSelectedChanged(pos)
                }
            }
        }
    }

    init {
        recyclerView.layoutManager = layoutManager
        val wheelAdapter = WheelAdapter(itemList)
        recyclerView.adapter = wheelAdapter
        PagerSnapHelper().attachToRecyclerView(recyclerView)
        wheelAdapter.notifyDataSetChanged()
    }

    val currentItem: WheelInfo
        get() {
            val selectedPosition = layoutManager.selectedPosition
            return if (selectedPosition in itemList.indices) {
                itemList[selectedPosition]
            } else {
                EMPTY
            }
        }

    fun selectedTo(tag: String) {
        for (index in itemList.indices) {
            if (itemList[index].tag == tag) {
                recyclerView.post {
                    recyclerView.scrollToPosition(index)
                }
                return
            }
        }
    }

    fun addItem(info: Array<WheelInfo>) {
        val index = itemList.size
        itemList.addAll(info)
        recyclerView.adapter?.notifyItemRangeInserted(index, info.size)
    }

    fun onSelectedChanged(listener: OnSelectedChangedListener) {
        this.selectedChangedListener = listener
    }

    private fun onSelectedChanged(position: Int) {
        if (position in itemList.indices) {
            selectedChangedListener?.onSelectedChanged(itemList[position])
        }
    }

    private class WheelAdapter(
        private val data: List<WheelInfo>
    ) : RecyclerView.Adapter<WheelItemHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WheelItemHolder {
            return WheelItemHolder.create(parent)
        }

        override fun onBindViewHolder(holder: WheelItemHolder, position: Int) {
            holder.bind(data[position])
        }

        override fun getItemCount(): Int {
            return data.size
        }
    }

    private class WheelItemHolder(
        private val binding: ItemWheelBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun create(parent: ViewGroup): WheelItemHolder {
                return WheelItemHolder(parent.bind(true))
            }
        }

        fun bind(info: WheelInfo) {
            binding.messageItemView.text = info.name
        }

    }

    data class WheelInfo(
        val name: String,
        val tag: String
    )

    fun interface OnSelectedChangedListener {
        fun onSelectedChanged(info: WheelInfo)
    }

}