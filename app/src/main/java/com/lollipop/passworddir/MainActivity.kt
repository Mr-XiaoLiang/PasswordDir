package com.lollipop.passworddir

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.lollipop.passworddir.databinding.ActivityMainBinding
import com.lollipop.passworddir.util.*
import com.lollipop.passworddir.view.ListDialogHelper
import com.lollipop.passworddir.view.MessageViewHelper
import com.lollipop.passworddir.view.SmoothMessageHelper
import com.lollipop.passworddir.view.StatusViewHelper
import com.lollipop.passworddir.view.StatusViewHelper.Companion.bindColorStatus
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintWriter
import java.util.*
import kotlin.collections.ArrayList

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

    private val smoothMessageHelper by lazy {
        SmoothMessageHelper(messageViewHelper)
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
        WindowInsetsHelper.initWindowFlag(this)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.root.fixInsetsByPadding(WindowInsetsHelper.Edge.HEADER)
        binding.operationPanel.fixInsetsByPadding(WindowInsetsHelper.Edge.CONTENT)
        initView()
    }

    private fun initView() {
        binding.mkdirBtn.setOnClickListener {
            makeDirs()
        }

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

        switchToLoading(false)
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

        val noMedia = isNomedia
        val min = DirUtil.getDirCount(noMedia, minDirCount, layersCount)
        val max = DirUtil.getDirCount(noMedia, maxDirCount, layersCount)
        val result = if (min == max) {
            getString(R.string.expected_generation_results, min)
        } else {
            getString(R.string.expected_generation_results2, min, max)
        }
        binding.dirInfoView.text = result
    }

    private fun makeDirs() {
        if (!DirUtil.canManageAllFile(this)) {
            messageViewHelper.post(getString(R.string.no_permission))
            DirUtil.requestManageAllFile(this)
            return
        }
        messageViewHelper.post(getString(R.string.mkdir_start))

        val rootDir = DirUtil.rootDir()
        val parentFile = File(rootDir, DirUtil.dateFormat.format(Date()))

        onFileCreateStart()
        doAsync({ error ->
            onFileCreateError(error)
        }) {
            val fileCount = DirUtil.makeDirs(parentFile, createOption()) { newFile ->
                onUI {
                    onNewFileCreated(newFile)
                }
            }
            onUI {
                onFileCreateEnd(parentFile, fileCount)
            }
        }
    }

    private fun onNewFileCreated(file: File) {
        smoothMessageHelper.post(getString(R.string.make_new, file.name), true)
    }

    private fun onFileCreateStart() {
        switchToLoading(true)
        smoothMessageHelper.start()
    }

    private fun onFileCreateEnd(root: File, count: Int) {
        switchToLoading(false)
        smoothMessageHelper.stop()
        messageViewHelper.post(getString(R.string.mkdir_end, count, root.absolutePath))
        messageViewHelper.post(getString(R.string.dir_path_hint))
    }

    private fun onFileCreateError(error: Throwable) {
        val outputStream = ByteArrayOutputStream()
        val printWriter = PrintWriter(outputStream)
        error.printStackTrace(printWriter)
        printWriter.flush()
        val errorValue = outputStream.toString()
        onUI {
            switchToLoading(false)
            smoothMessageHelper.stop()
            messageViewHelper.post(getString(R.string.error), isError = true)
            messageViewHelper.post(errorValue, isError = true)
        }
    }

    private fun switchToLoading(isEnable: Boolean) {
        binding.contentLoadingView.apply {
            if (isEnable) {
                show()
            } else {
                hide()
            }
        }
        binding.contentGroup.isVisible = !isEnable
    }

    private fun createOption(): DirUtil.Option {
        /**
        val minCount: Int,
        val maxCount: Int,
        val noMedia: Boolean,
        val layersCount: Int,
        val nameLength: Int,
        val useNumber: Boolean,
        val useLowercase: Boolean,
        val useUppercase: Boolean
         */
        return DirUtil.Option(
            minCount = minDirCount,
            maxCount = maxDirCount,
            noMedia = isNomedia,
            layersCount = layersCount,
            nameLength = nameLength,
            useNumber = useNumber,
            useLowercase = useLowercase,
            useUppercase = useUppercase
        )
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