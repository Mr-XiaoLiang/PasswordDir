package com.lollipop.passworddir.util

import java.io.File

/**
 * @author lollipop
 * @date 2021/10/25 22:24
 */
object DirUtil {

    private val KEY_MAP by lazy {
        val list = ArrayList<String>()
        for (char in '0'..'9') {
            list.add(java.lang.String.valueOf(char))
        }
        for (char in 'a'..'z') {
            list.add(java.lang.String.valueOf(char))
        }
        for (char in 'A'..'Z') {
            list.add(java.lang.String.valueOf(char))
        }
        list.toTypedArray()
    }

    fun getDirCount(isNomedia: Boolean, dirCount: Int, layersCount: Int): Int {
        var result = if (isNomedia) {
            2
        } else {
            1
        }
        for (count in 1..layersCount) {
            result *= dirCount
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

    private fun makeDirs(): List<File> {
        TODO()
    }

}