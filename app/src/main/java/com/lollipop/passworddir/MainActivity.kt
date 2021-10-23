package com.lollipop.passworddir

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.lollipop.passworddir.databinding.ActivityMainBinding
import com.lollipop.passworddir.util.lazyBind
import com.lollipop.passworddir.view.MessageViewHelper
import com.lollipop.passworddir.view.StatusViewHelper
import com.lollipop.passworddir.view.StatusViewHelper.Companion.bindColorStatus

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazyBind()

    private val messageViewHelper by lazy {
        MessageViewHelper(binding.messageListView)
    }

    private val defaultStatusMap by lazy {
        mapOf(
            StatusViewHelper.STATUS_ON to R.color.status_on.getColorById(),
            StatusViewHelper.STATUS_OFF to R.color.status_off.getColorById(),
        )
    }

    private val nomediaBtnStatus by lazy {
        binding.nomediaBtn.bindColorStatus(
            binding.nomediaStateView,
            defaultStatusMap
        )
    }

    private val useNumberBtnStatus by lazy {
        binding.useNumberBtn.bindColorStatus(
            binding.useNumberStateView,
            defaultStatusMap
        )
    }

    private val useLowercaseBtnStatus by lazy {
        binding.useLowercaseBtn.bindColorStatus(
            binding.useLowercaseStateView,
            defaultStatusMap
        )
    }

    private val useUppercaseBtnStatus by lazy {
        binding.useUppercaseBtn.bindColorStatus(
            binding.useUppercaseStateView,
            defaultStatusMap
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        nomediaBtnStatus.setStatus(StatusViewHelper.STATUS_ON)
        useNumberBtnStatus.setStatus(StatusViewHelper.STATUS_ON)
        useLowercaseBtnStatus.setStatus(StatusViewHelper.STATUS_ON)
        useUppercaseBtnStatus.setStatus(StatusViewHelper.STATUS_ON)
        var count = 0
        binding.mkdirBtn.setOnClickListener {
            count++
            messageViewHelper.post("添加了一条测试信息，count=$count")
        }
    }

    private fun Int.getColorById(): Int {
        return ContextCompat.getColor(this@MainActivity, this)
    }
}