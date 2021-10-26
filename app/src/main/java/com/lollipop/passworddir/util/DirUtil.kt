package com.lollipop.passworddir.util

import java.io.File
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
        var result = 0
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
        }
        return result
    }

    fun makeDirs(parent: File, option: Option): Int {
        var allDirCount = 0
        val pendingDir = LinkedList<File>()
        pendingDir.add(parent)
        val nextLayer = LinkedList<File>()
        val makeOption = option.createMakeOption()
        for (layer in 0 until option.layersCount) {
            while (pendingDir.isNotEmpty()) {
                val dir = pendingDir.removeFirst()
                val count = option.randomCount()
                val children = makeDirs(dir, count, makeOption)
                nextLayer.addAll(children)
            }
            allDirCount += nextLayer.size
            pendingDir.addAll(nextLayer)
            nextLayer.clear()
        }
        return allDirCount
    }

    private fun makeDirs(parent: File, count: Int, option: MakeOption): List<File> {
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
        val layersCount: Int,
        val nameLength: Int,
        val useNumber: Boolean,
        val useLowercase: Boolean,
        val useUppercase: Boolean
    ) {
        fun createMakeOption(): MakeOption {
            return MakeOption(
                nameLength,
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

    class MakeOption(
        val nameLength: Int,
        val keyMap: CharArray
    )

}