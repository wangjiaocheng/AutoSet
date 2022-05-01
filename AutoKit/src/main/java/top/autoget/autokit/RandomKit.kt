package top.autoget.autokit

import android.graphics.Color
import top.autoget.autokit.StringKit.isSpace
import java.util.*

object RandomKit {
    private const val NUMBERS_AND_LETTERS: String =
        "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"

    fun getRandomNumbersAndLetters(length: Int): String? = getRandom(NUMBERS_AND_LETTERS, length)
    private const val NUMBERS: String = "0123456789"
    fun getRandomNumbers(length: Int): String? = getRandom(NUMBERS, length)
    private const val LETTERS: String = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
    fun getRandomLetters(length: Int): String? = getRandom(LETTERS, length)
    private const val LETTERS_CAPITAL: String = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    fun getRandomCapitalLetters(length: Int): String? = getRandom(LETTERS_CAPITAL, length)
    private const val LETTERS_LOWER_CASE: String = "abcdefghijklmnopqrstuvwxyz"
    fun getRandomLowerCaseLetters(length: Int): String? = getRandom(LETTERS_LOWER_CASE, length)
    fun getRandom(source: String, length: Int): String? =
        if (isSpace(source)) null else getRandom(source.toCharArray(), length)

    fun getRandom(sourceChar: CharArray?, length: Int): String? = when {
        sourceChar == null || sourceChar.isEmpty() || length < 0 -> null
        else -> StringBuilder(length).apply {
            val random = Random()
            for (i in 0 until length) {
                append(sourceChar[random.nextInt(sourceChar.size)])
            }
        }.toString()
    }

    fun getRandom(max: Int): Int = getRandom(0, max)
    fun getRandom(min: Int, max: Int): Int = when {
        min > max -> 0
        min == max -> min
        else -> min + Random().nextInt(max - min)
    }

    val randomColor: Int = Random().run { Color.rgb(nextInt(256), nextInt(256), nextInt(256)) }
    fun shuffle(anyArray: Array<Any?>?): Boolean =
        if (anyArray == null) false else shuffle(anyArray, getRandom(anyArray.size))

    fun shuffle(anyArray: Array<Any?>?, shuffleCount: Int): Boolean {
        val length: Int = anyArray?.size ?: 0
        return when {
            anyArray == null || shuffleCount < 0 || length < shuffleCount -> false
            else -> {
                for (i in 1..shuffleCount) {
                    val random = getRandom(length - i)
                    val temp = anyArray[length - i]
                    anyArray[length - i] = anyArray[random]
                    anyArray[random] = temp
                }
                true
            }
        }
    }//随机打乱Any数组内容

    fun shuffle(intArray: IntArray?): IntArray? =
        if (intArray == null) null else shuffle(intArray, getRandom(intArray.size))

    fun shuffle(intArray: IntArray?, shuffleCount: Int): IntArray? {
        val length: Int = intArray?.size ?: 0
        return when {
            intArray == null || shuffleCount < 0 || length < shuffleCount -> null
            else -> IntArray(shuffleCount).apply {
                for (i in 1..shuffleCount) {
                    val random = getRandom(length - i)
                    this[i - 1] = intArray[random]
                    val temp = intArray[length - i]
                    intArray[length - i] = intArray[random]
                    intArray[random] = temp
                }
            }
        }
    }//随机打乱Int数组内容
}