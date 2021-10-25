package com.lollipop.passworddir

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.lollipop.passworddir.databinding.ActivityMainBinding
import com.lollipop.passworddir.util.DirUtil
import com.lollipop.passworddir.util.lazyBind
import com.lollipop.passworddir.view.ListDialogHelper
import com.lollipop.passworddir.view.MessageViewHelper
import com.lollipop.passworddir.view.StatusViewHelper
import com.lollipop.passworddir.view.StatusViewHelper.Companion.bindColorStatus

class MainActivity : AppCompatActivity() {

    companion object {
        private const val DEF_MIN_COUNT = 9
        private const val DEF_MAX_COUNT = 9
        private const val DEF_NAME_LENGTH = 1
        private const val DEF_LAYERS_COUNT = 4
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

    private val dirCountList by lazy {
        NumberList.createNumberList(1..62)
    }

    private val dirNameLengthList by lazy {
        NumberList.createNumberList(1..32)
    }

    private val dirLayersCountList by lazy {
        NumberList.createNumberList(1..10)
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

    private val listDialog by lazy {
        ListDialogHelper<NumberInfo>(
            binding.listDialogGroup,
            binding.listDialogBackground,
            binding.listDialogCard,
            binding.listDialogContent
        )
    }

    private var minDirCount = DEF_MIN_COUNT

    private var maxDirCount = DEF_MAX_COUNT

    private var nameLength = DEF_NAME_LENGTH

    private var layersCount = DEF_LAYERS_COUNT

    private val isNomedia: Boolean
        get() {
            return nomediaBtnStatus.statusIsOn()
        }

    private val useNumber: Boolean
        get() {
            return useNumberBtnStatus.statusIsOn()
        }

    private val useLowercase: Boolean
        get() {
            return useLowercaseBtnStatus.statusIsOn()
        }

    private val useUppercase: Boolean
        get() {
            return useUppercaseBtnStatus.statusIsOn()
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        nomediaBtnStatus.statusOn()
        useNumberBtnStatus.statusOn()
        useLowercaseBtnStatus.statusOn()
        useUppercaseBtnStatus.statusOn()

        initNumberView(binding.dirCountMinView, DEF_MIN_COUNT, dirCountList) {
            minDirCount = it
        }
        initNumberView(binding.dirCountMaxView, DEF_MAX_COUNT, dirCountList) {
            maxDirCount = it
        }
        initNumberView(binding.dirNameLengthView, DEF_NAME_LENGTH, dirNameLengthList) {
            nameLength = it
        }
        initNumberView(binding.dirLayersCountView, DEF_LAYERS_COUNT, dirLayersCountList) {
            layersCount = it
        }

        onParameterChanged()
    }

    private fun initNumberView(view: TextView, def: Int, data: NumberList, save: (Int) -> Unit) {
        val number = dirNameLengthList.findOrAdd(def)
        view.text = number.name
        view.setOnClickListener {
            listDialog.setData(data)
            listDialog.onSelectedChanged {
                view.text = it.name
                save(it.number)
                onParameterChanged()
            }
            listDialog.show()
        }
    }

    private fun onParameterChanged() {
        if (maxDirCount < minDirCount) {
            maxDirCount = minDirCount
            binding.dirCountMaxView.text = binding.dirCountMinView.text
            messageViewHelper.post(getString(R.string.max_less_than_min))
        }

        val qcDir = DirUtil.dirCoexistenceQuantity(
            nameLength, useNumber, useLowercase, useUppercase
        )
        if (qcDir == 0) {
            useNumberBtnStatus.statusOn()
            messageViewHelper.post(getString(R.string.select_at_least_one))
        } else if (qcDir < maxDirCount) {
            messageViewHelper.post(getString(R.string.too_few_elements))
        }

        val nomedia = isNomedia
        val min = DirUtil.getDirCount(nomedia, minDirCount, layersCount)
        val max = DirUtil.getDirCount(nomedia, maxDirCount, layersCount)
        val result = if (min == max) {
            getString(R.string.expected_generation_results, min)
        } else {
            getString(R.string.expected_generation_results2, min, max)
        }
        binding.dirInfoView.text = result
    }

    private fun Int.getColorById(): Int {
        return ContextCompat.getColor(this@MainActivity, this)
    }

    private class NumberList : MutableList<NumberInfo> by ArrayList() {

        companion object {
            fun createNumberList(intRange: IntRange): NumberList {
                return NumberList().apply {
                    for (i in intRange) {
                        add(NumberInfo(i.toString(), i))
                    }
                }
            }
        }

        fun findByNumber(int: Int): NumberInfo? {
            forEach {
                if (it.number == int) {
                    return it
                }
            }
            return null
        }

        fun findOrAdd(int: Int): NumberInfo {
            val number = findByNumber(int)
            if (number != null) {
                return number
            }
            val newNumber = NumberInfo(int.toString(), int)
            add(newNumber)
            return newNumber
        }

    }

    private class NumberInfo(name: String, val number: Int) : ListDialogHelper.ItemInfo(name)

}