package com.lollipop.passworddir

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.lollipop.passworddir.databinding.ActivityMainBinding
import com.lollipop.passworddir.util.lazyBind
import com.lollipop.passworddir.view.HorizontalWheelHelper
import com.lollipop.passworddir.view.HorizontalWheelHelper.Companion.bindHorizontalWheel
import com.lollipop.passworddir.view.MessageViewHelper
import com.lollipop.passworddir.view.StatusViewHelper
import com.lollipop.passworddir.view.StatusViewHelper.Companion.bindColorStatus

class MainActivity : AppCompatActivity() {

    companion object {
        private const val DEF_MIN_COUNT = "9"
        private const val DEF_MAX_COUNT = "9"
        private const val DEF_NAME_LENGTH = "1"
        private const val DEF_LAYERS_COUNT = "4"
    }

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

    private val dirCountArray by lazy {
        Array(62) {
            val value = (it + 1).toString()
            HorizontalWheelHelper.WheelInfo(value, value)
        }
    }

    private val dirNameLengthArray by lazy {
        Array(32) {
            val value = (it + 1).toString()
            HorizontalWheelHelper.WheelInfo(value, value)
        }
    }

    private val dirLayersCountArray by lazy {
        Array(10) {
            val value = (it + 1).toString()
            HorizontalWheelHelper.WheelInfo(value, value)
        }
    }

    private val nomediaBtnStatus by lazy {
        binding.nomediaBtn.bindColorStatus(
            binding.nomediaStateView,
            defaultStatusMap
        ) {
            onParameterChanged()
        }
    }

    private val useNumberBtnStatus by lazy {
        binding.useNumberBtn.bindColorStatus(
            binding.useNumberStateView,
            defaultStatusMap
        ) {
            onParameterChanged()
        }
    }

    private val useLowercaseBtnStatus by lazy {
        binding.useLowercaseBtn.bindColorStatus(
            binding.useLowercaseStateView,
            defaultStatusMap
        ) {
            onParameterChanged()
        }
    }

    private val useUppercaseBtnStatus by lazy {
        binding.useUppercaseBtn.bindColorStatus(
            binding.useUppercaseStateView,
            defaultStatusMap
        ) {
            onParameterChanged()
        }
    }

    private val minDirCountWheel by lazy {
        binding.dirCountMinView.bindHorizontalWheel(
            dirCountArray
        ) {
            onParameterChanged()
        }
    }

    private val maxDirCountWheel by lazy {
        binding.dirCountMaxView.bindHorizontalWheel(
            dirCountArray
        ) {
            onParameterChanged()
        }
    }

    private val nameLengthWheel by lazy {
        binding.dirNameLengthView.bindHorizontalWheel(
            dirNameLengthArray
        ) {
            onParameterChanged()
        }
    }

    private val layersCountViewWheel by lazy {
        binding.dirLayersCountView.bindHorizontalWheel(
            dirLayersCountArray
        ) {
            onParameterChanged()
        }
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
        minDirCountWheel.selectedTo(DEF_MIN_COUNT)
        maxDirCountWheel.selectedTo(DEF_MAX_COUNT)
        nameLengthWheel.selectedTo(DEF_NAME_LENGTH)
        layersCountViewWheel.selectedTo(DEF_LAYERS_COUNT)

        var count = 0
        binding.mkdirBtn.setOnClickListener {
            count++
            messageViewHelper.post("添加了一条测试信息，count=$count")
        }
    }

    private fun onParameterChanged() {
        // TODO
        messageViewHelper.post("参数信息发生了改变")
    }

    private fun Int.getColorById(): Int {
        return ContextCompat.getColor(this@MainActivity, this)
    }
}