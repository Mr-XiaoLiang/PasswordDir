package com.lollipop.passworddir.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.Settings
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random

/**
 * @author lollipop
 * @date 2021/10/25 22:24
 */
object DirUtil {

    private val NUMBER_KEY_MAP by lazy {
        val list = ArrayList<Char>()
        for (char in '0'..'9') {
            list.add(char)
        }
        list.toTypedArray()
    }

    private val LOWER_KEY_MAP by lazy {
        val list = ArrayList<Char>()
        for (char in 'a'..'z') {
            list.add(char)
        }
        list.toTypedArray()
    }

    private val UPPER_KEY_MAP by lazy {
        val list = ArrayList<Char>()
        for (char in 'A'..'Z') {
            list.add(char)
        }
        list.toTypedArray()
    }

    private const val PERMISSION_REQUEST_CODE = 996

    val dateFormat by lazy {
        SimpleDateFormat("HH_mm_ss_yyyy_MM_dd", Locale.getDefault())
    }

    fun rootDir(): File {
        return File(Environment.getExternalStorageDirectory(), "PasswordDir")
    }

    /**
     * 请求管理所有文件的权限
     */
    fun requestManageAllFile(activity: Activity) {
        if (versionThen(Build.VERSION_CODES.M) && !canWriteFile(activity)) {
            activity.requestPermissions(
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                PERMISSION_REQUEST_CODE
            )
            return
        }
        if (versionThen(Build.VERSION_CODES.R) && !canManageFile()) {
            activity.startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
        }
    }

    /**
     * 判断是否获取MANAGE_EXTERNAL_STORAGE权限：
     */
    fun canManageAllFile(context: Context): Boolean {
        if (!canWriteFile(context)) {
            return false
        }
        if (!canManageFile()) {
            return false
        }
        return true
    }

    private fun canWriteFile(context: Context): Boolean {
        if (versionThen(Build.VERSION_CODES.M)
            && context.checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        return true
    }

    private fun canManageFile(): Boolean {
        if (versionThen(Build.VERSION_CODES.R) && !Environment.isExternalStorageManager()) {
            return false
        }
        return true
    }

    fun getDirCount(isNomedia: Boolean, dirCount: Int, layersCount: Int): Int {
        var oneLayer = if (isNomedia) {
            2
        } else {
            1
        }
        var result = 0
        for (count in 1..layersCount) {
            oneLayer *= dirCount
            result += oneLayer
        }
        return result
    }

    fun dirCoexistenceQuantity(
        nameLength: Int,
        useNumber: Boolean,
        useLowercase: Boolean,
        useUppercase: Boolean
    ): Int {
        var result = 0L
        if (useNumber) {
            result += 10
        }
        if (useLowercase) {
            result += 26
        }
        if (useUppercase) {
            result += 26
        }
        val e = result
        for (i in 2..nameLength) {
            result *= e
            if (result > Int.MAX_VALUE) {
                return Int.MAX_VALUE
            }
        }
        return result.toInt()
    }

    fun makeDirs(parent: File, option: Option, onFileCreate: (File) -> Unit, onZipStart: () -> Unit): MakeResult {
        var allDirCount = 0
        val pendingDir = LinkedList<File>()
        pendingDir.add(parent)
        val nextLayer = LinkedList<File>()
        val makeOption = option.createMakeOption()
        for (layer in 0 until option.layersCount) {
            while (pendingDir.isNotEmpty()) {
                val dir = pendingDir.removeFirst()
                val count = option.randomCount()
                val children = makeDirs(dir, count, makeOption, onFileCreate)
                nextLayer.addAll(children)
            }
            allDirCount += nextLayer.size
            pendingDir.addAll(nextLayer)
            nextLayer.clear()
        }
        if (option.noMedia) {
            allDirCount *= 2
        }
        var zipDir: File? = null
        if (option.zipDir) {
            val parentFile = parent.parentFile
            if (parentFile != null) {
                onZipStart()
                zipDir = ZipHelper.zipTo(parentFile, parent.name).addFile(parent).start()
            }
        }
        return MakeResult(parent, allDirCount, zipDir)
    }

    private fun makeDirs(
        parent: File,
        count: Int,
        option: MakeOption,
        onFileCreate: (File) -> Unit
    ): List<File> {
        if (count < 1) {
            return emptyList()
        }
        val keyMap = option.keyMap
        val indexArray = IntArray(keyMap.size) { 0 }
        if (indexArray.isEmpty()) {
            return emptyList()
        }
        val nameList = Array(count) { CharArray(option.nameLength) }
        // 每一位尽量不重复
        for (nameIndex in 0 until option.nameLength) {
            // 每次换到新的一位时，重制标识符
            for (i in indexArray.indices) {
                indexArray[i] = 0
            }
            // 循环为每个文件夹加名字
            for (dirIndex in 0 until count) {
                var nextChar = getNextChar(indexArray)
                if (nextChar < 0) {
                    for (i in indexArray.indices) {
                        indexArray[i] = 0
                    }
                    nextChar = getNextChar(indexArray)
                }
                if (nextChar < 0) {
                    nameList[dirIndex][nameIndex] = '_'
                } else {
                    indexArray[nextChar] = 1
                    nameList[dirIndex][nameIndex] = keyMap[nextChar]
                }
            }
        }
        // 名字的集合转为文件的集合
        val fileList = nameList.map {
            File(parent, String(it))
        }
        // 创建每个文件夹
        fileList.forEach {
            it.mkdirs()
            onFileCreate(it)
            if (option.noMedia) {
                val noMediaFile = File(it, ".nomedia")
                noMediaFile.createNewFile()
                onFileCreate(noMediaFile)
            }
        }
        // 返回集合
        return fileList
    }

    private fun getNextChar(indexArray: IntArray): Int {
        val random = Random.Default
        val maxCount = indexArray.size / 3 * 2
        // 最多尝试总数的三分之二次
        for (count in 1..maxCount) {
            val nextInt = random.nextInt(indexArray.size)
            if (indexArray[nextInt] == 0) {
                return nextInt
            }
        }
        // 找不到就遍历拿吧
        for (i in indexArray.indices) {
            if (indexArray[i] == 0) {
                return i
            }
        }
        // 如果还是找不到，那么返回-1吧
        return -1
    }

    class Option(
        val minCount: Int,
        val maxCount: Int,
        val noMedia: Boolean,
        val zipDir: Boolean,
        val layersCount: Int,
        val nameLength: Int,
        val useNumber: Boolean,
        val useLowercase: Boolean,
        val useUppercase: Boolean
    ) {
        fun createMakeOption(): MakeOption {
            return MakeOption(
                nameLength,
                noMedia,
                createKeyMap()
            )
        }

        fun randomCount(): Int {
            return if (minCount == maxCount) {
                minCount
            } else {
                Random.nextInt(maxCount - minCount) + minCount
            }
        }

        private fun createKeyMap(): CharArray {
            val list = ArrayList<Char>()
            if (useNumber) {
                list.addAll(NUMBER_KEY_MAP)
            }
            if (useLowercase) {
                list.addAll(LOWER_KEY_MAP)
            }
            if (useUppercase) {
                list.addAll(UPPER_KEY_MAP)
            }
            return list.toCharArray()
        }
    }

    class MakeResult(
        val dirRoot: File,
        val fileCount: Int,
        val zipDir: File?
    )

    class MakeOption(
        val nameLength: Int,
        val noMedia: Boolean,
        val keyMap: CharArray
    )

}