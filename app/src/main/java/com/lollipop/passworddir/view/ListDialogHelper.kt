package com.lollipop.passworddir.view

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.passworddir.databinding.ItemListDialogBinding
import com.lollipop.passworddir.util.bind

/**
 * @author lollipop
 * @date 2021/10/25 21:14
 */
class ListDialogHelper<T: ListDialogHelper.ItemInfo>(
    private val rootGroup: View,
    private val backgroundView: View,
    private val dialogCard: View,
    private val listContentView: RecyclerView
): Animator.AnimatorListener, ValueAnimator.AnimatorUpdateListener {

    companion object {
        private const val DURATION = 300L
        private const val THRESHOLD_CLOSE = 0.001F
        private const val THRESHOLD_OPEN = 0.999F
    }

    private val itemList = ArrayList<T>()
    private var selectedListener: OnSelectedListener<T>? = null

    private var animationProgress = 0F

    private val animator by lazy {
        ValueAnimator().apply {
            addUpdateListener(this@ListDialogHelper)
            addListener(this@ListDialogHelper)
        }
    }

    init {
        listContentView.layoutManager = LinearLayoutManager(
            listContentView.context,
            RecyclerView.VERTICAL,
            false
        )
        listContentView.adapter = ListAdapter(itemList, ::onSelected)
        dialogCard.setOnClickListener {
            // do nothing
        }
        backgroundView.setOnClickListener {
            dismiss()
        }

    }

    fun show() {
        rootGroup.post {
            animator.cancel()
            animator.duration = ((1 - animationProgress) / 1F * DURATION).toLong()
            animator.setFloatValues(animationProgress, 1F)
            animator.start()
        }
    }

    fun dismiss() {
        selectedListener = null
        rootGroup.post {
            animator.cancel()
            animator.duration = ((animationProgress) / 1F * DURATION).toLong()
            animator.setFloatValues(animationProgress, 0F)
            animator.start()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(info: List<T>) {
        itemList.clear()
        itemList.addAll(info)
        listContentView.adapter?.notifyDataSetChanged()
    }

    fun onSelectedChanged(listener: OnSelectedListener<T>) {
        this.selectedListener = listener
    }

    private fun onSelected(position: Int) {
        if (position in itemList.indices) {
            selectedListener?.onSelected(itemList[position])
        }
        dismiss()
    }

    override fun onAnimationStart(animation: Animator?) {
        if (animation != animator) {
            return
        }
        rootGroup.isVisible = true
    }

    override fun onAnimationEnd(animation: Animator?) {
        if (animation != animator) {
            return
        }
        if (animationProgress < THRESHOLD_CLOSE) {
            rootGroup.isVisible = false
        }
    }

    override fun onAnimationCancel(animation: Animator?) {
    }

    override fun onAnimationRepeat(animation: Animator?) {
    }

    override fun onAnimationUpdate(animation: ValueAnimator?) {
        if (animation != animator) {
            return
        }
        animationProgress = animator.animatedValue as Float
        backgroundView.alpha = animationProgress
        dialogCard.translationX = (animationProgress - 1) * dialogCard.right
    }

    private class ListAdapter(
        private val data: List<ItemInfo>,
        private val onItemClick: (Int) -> Unit
    ) : RecyclerView.Adapter<ItemHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
            return ItemHolder.create(parent, ::onItemClick)
        }

        override fun onBindViewHolder(holder: ItemHolder, position: Int) {
            holder.bind(data[position])
        }

        override fun getItemCount(): Int {
            return data.size
        }

        private fun onItemClick(holder: ItemHolder) {
            onItemClick(holder.adapterPosition)
        }
    }

    private class ItemHolder(
        private val binding: ItemListDialogBinding,
        private val onItemClick: (ItemHolder) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun create(
                parent: ViewGroup,
                onItemClick: (ItemHolder) -> Unit
            ): ItemHolder {
                return ItemHolder(parent.bind(true), onItemClick)
            }
        }

        init {
            itemView.setOnClickListener {
                onItemClick.invoke(this)
            }
        }

        fun bind(info: ItemInfo) {
            binding.listDialogItemView.text = info.name
        }

    }

    open class ItemInfo(val name: String)

    fun interface OnSelectedListener<T: ItemInfo> {
        fun onSelected(info: T)
    }

}