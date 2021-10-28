package com.lollipop.passworddir.view

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.lollipop.passworddir.databinding.DialogEditBinding
import com.lollipop.passworddir.util.bind

/**
 * @author lollipop
 * @date 2021/10/28 20:35
 */
class EditDialog(
    context: Context,
    private val editInfo: CharSequence
) : Dialog(context) {

    companion object {
        fun show(context: Context, value: CharSequence) {
            EditDialog(context, value).show()
        }
    }

    private val binding: DialogEditBinding by lazy {
        LayoutInflater.from(context).bind()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.decorView?.setBackgroundColor(Color.TRANSPARENT)
        setContentView(
            binding.root,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        binding.editView.setText(editInfo)
    }

}