package com.lollipop.passworddir.util

/**
 * @author lollipop
 * @date 2021/10/25 22:24
 */
object DirUtil {

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

}