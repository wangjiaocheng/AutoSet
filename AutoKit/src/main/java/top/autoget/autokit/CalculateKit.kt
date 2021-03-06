package top.autoget.autokit

import java.math.BigDecimal

object CalculateKit {
    private const val DEF_DIV_SCALE = 10

    @JvmOverloads
    fun add(v1: Double, v2: Double, scale: Int = DEF_DIV_SCALE): Double = when {
        scale < 0 -> throw IllegalArgumentException("The scale must be a positive integer or zero")
        else -> v1.toBigDecimal().add(v2.toBigDecimal())
            .setScale(scale, BigDecimal.ROUND_HALF_UP).toDouble()
    }

    @JvmOverloads
    fun subtract(v1: Double, v2: Double, scale: Int = DEF_DIV_SCALE): Double = when {
        scale < 0 -> throw IllegalArgumentException("The scale must be a positive integer or zero")
        else -> v1.toBigDecimal().subtract(v2.toBigDecimal())
            .setScale(scale, BigDecimal.ROUND_HALF_UP).toDouble()
    }

    @JvmOverloads
    fun multiply(v1: Double, v2: Double, scale: Int = DEF_DIV_SCALE): Double = when {
        scale < 0 -> throw IllegalArgumentException("The scale must be a positive integer or zero")
        else -> v1.toBigDecimal().multiply(v2.toBigDecimal())
            .setScale(scale, BigDecimal.ROUND_HALF_UP).toDouble()
    }

    @JvmOverloads
    fun divide(v1: Double, v2: Double, scale: Int = DEF_DIV_SCALE): Double = when {
        scale < 0 -> throw IllegalArgumentException("The scale must be a positive integer or zero")
        else -> v1.toBigDecimal().divide(v2.toBigDecimal())
            .setScale(scale, BigDecimal.ROUND_HALF_UP).toDouble()
    }

    @JvmOverloads
    fun remainder(v1: Double, v2: Double, scale: Int = DEF_DIV_SCALE): Double = when {
        scale < 0 -> throw IllegalArgumentException("The scale must be a positive integer or zero")
        else -> v1.toBigDecimal().remainder(v2.toBigDecimal())
            .setScale(scale, BigDecimal.ROUND_HALF_UP).toDouble()
    }

    @JvmOverloads
    fun round(v: Double, scale: Int = DEF_DIV_SCALE): Double = when {
        scale < 0 -> throw IllegalArgumentException("The scale must be a positive integer or zero")
        else -> v.toBigDecimal().setScale(scale, BigDecimal.ROUND_HALF_UP).toDouble()
    }

    fun compareBigDecimal(amount: Double, compare: Double): Boolean =
        amount.toBigDecimal().compareTo(compare.toBigDecimal()) != -1

    fun formatMoney(bigDecimal: BigDecimal?): String = bigDecimal?.let { bd ->
        when {
            bd.toDouble() == 0.0 -> "0.00"
            else -> (bd.toString().indexOf("-") != -1).let { isNegativeInteger ->
                when {
                    isNegativeInteger ->
                        bd.toString().run { substring(1, length).toBigDecimal() }//????????????
                    else -> bd
                }.setScale(2, BigDecimal.ROUND_HALF_UP).toString()
                    .split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    .let { strings ->
                        StringBuffer().apply {
                            var j = 1
                            for (i in strings[0].indices) {
                                append(strings[0][strings[0].length - i - 1])
                                if (j % 3 == 0 && i != strings[0].length - 1) append(",")
                                j++
                            }//?????????????????????????????????
                        }.toString().let { string ->
                            StringBuffer().apply {
                                for (i in string.indices) {
                                    append(string[string.length - 1 - i])
                                }//??????
                                append(".${strings[1]}")//????????????
                            }.toString().let { if (isNegativeInteger) "-$it" else it }//????????????
                        }
                    }
            }
        }
    } ?: "0.00"

    fun adjustDouble(string: String, numOfIntPart: Int, numOfDecimalPart: Int): String? =
        string.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().let { strings ->
            CharArray(numOfIntPart).let { tempInt ->
                CharArray(numOfDecimalPart).let { tempDecimal ->
                    when {
                        strings[0].length == numOfIntPart -> {
                            for (i in strings[0].indices) {
                                tempInt[i] = strings[0][i]
                            }//??????
                            when {
                                strings[1].length >= numOfDecimalPart -> for (i in 0 until numOfDecimalPart) {
                                    tempDecimal[i] = strings[1][i]
                                }
                                strings[1].length < numOfDecimalPart -> for (i in 0 until numOfDecimalPart) {
                                    when {
                                        i < strings[1].length -> tempDecimal[i] = strings[1][i]
                                        else -> tempDecimal[i] = '0'
                                    }
                                }
                            }//??????
                            when (numOfDecimalPart) {
                                0 -> "${String(tempInt)}${String(tempDecimal)}"
                                else -> "${String(tempInt)}.${String(tempDecimal)}"
                            }//??????
                        }//????????????????????????????????????????????????????????????????????????????????????????????????
                        strings[0].length > numOfIntPart -> {
                            run {
                                var i = strings[0].length - 1
                                var j = 0
                                while (i >= strings[0].length - numOfIntPart && j < numOfIntPart) {
                                    tempInt[j] = strings[0][i]
                                    println(tempInt[j])
                                    i--
                                    j++
                                }
                            }//????????????
                            val tempInt0 = CharArray(numOfIntPart)
                            run {
                                var i = tempInt.size - 1
                                var j = 0
                                while (i >= 0 && j < numOfIntPart) {
                                    tempInt0[j] = tempInt[i]
                                    println(tempInt0[j])
                                    i--
                                    j++
                                }
                            }//????????????
                            when {
                                strings[1].length >= numOfDecimalPart -> for (i in 0 until numOfDecimalPart) {
                                    tempDecimal[i] = strings[1][i]
                                }
                                strings[1].length < numOfDecimalPart -> for (i in 0 until numOfDecimalPart) {
                                    when {
                                        i < strings[1].length -> tempDecimal[i] = strings[1][i]
                                        else -> tempDecimal[i] = '0'
                                    }
                                }
                            }//??????
                            when (numOfDecimalPart) {
                                0 -> "${String(tempInt0)}${String(tempDecimal)}"
                                else -> "${String(tempInt0)}.${String(tempDecimal)}"
                            }//??????
                        }//????????????????????????????????????????????????????????????????????????????????????????????????
                        strings[0].length < numOfIntPart -> {
                            run {
                                var i = strings[0].length - 1
                                var j = 0
                                while (i >= 0 && j < numOfIntPart) {
                                    tempInt[j] = strings[0][i]
                                    println(tempInt[j])
                                    i--
                                    j++
                                }
                            }//????????????
                            for (i in strings[0].length until numOfIntPart) {
                                tempInt[i] = '0'
                                println(tempInt[i])
                            }//??????
                            val tempInt0 = CharArray(numOfIntPart)
                            run {
                                var i = tempInt.size - 1
                                var j = 0
                                while (i >= 0 && j < numOfIntPart) {
                                    tempInt0[j] = tempInt[i]
                                    println(tempInt0[j])
                                    i--
                                    j++
                                }
                            }//????????????
                            when {
                                strings[1].length >= numOfDecimalPart -> for (i in 0 until numOfDecimalPart) {
                                    tempDecimal[i] = strings[1][i]
                                }
                                strings[1].length < numOfDecimalPart -> for (i in 0 until numOfDecimalPart) {
                                    when {
                                        i < strings[1].length -> tempDecimal[i] = strings[1][i]
                                        else -> tempDecimal[i] = '0'
                                    }
                                }
                            }//??????
                            when (numOfDecimalPart) {
                                0 -> "${String(tempInt0)}${String(tempDecimal)}"
                                else -> "${String(tempInt0)}.${String(tempDecimal)}"
                            }//??????
                        }//????????????????????????????????????????????????????????????????????????????????????????????????
                        strings[0].length < numOfIntPart && strings[1].length < numOfDecimalPart -> {
                            var newString = string
                            for (i in 0 until numOfIntPart - strings[0].length) {
                                newString = "0$newString"
                            }
                            for (i in 0 until numOfDecimalPart - strings[1].length) {
                                newString += "0"
                            }
                            newString
                        }
                        else -> null
                    }
                }
            }
        }

    @JvmStatic
    fun main() {
        println("??????0???${formatMoney(0.toBigDecimal())}")
        println("??????0.0???${formatMoney(0.0.toBigDecimal())}")
        println("??????0.00???${formatMoney(0.00.toBigDecimal())}")
        println("??????0.58???${formatMoney(0.58.toBigDecimal())}")
        println("??????5.58???${formatMoney(5.58.toBigDecimal())}")
        println("??????5.54???${formatMoney(BigDecimal(5.54))}")
        println("??????512322.555555111???${formatMoney(512322.555555111.toBigDecimal())}")
        println("??????3423423425.54???${formatMoney(3423423425.54.toBigDecimal())}")
        println("??????3423423425.58???${formatMoney(3423423425.58.toBigDecimal())}")
        println("??????1000000.543453???${formatMoney(1000000.543453.toBigDecimal())}")
        println("??????9343788754.573453???${formatMoney((-9343788754.573453).toBigDecimal())}")
        println("??????9343788756.577???${formatMoney((-9343788756.577).toBigDecimal())}")
        println("??????-343788756.577???${formatMoney((-343788756.577).toBigDecimal())}")
        println("??????-34756.54???${formatMoney((-34756.54).toBigDecimal())}")
        println("??????-34756.556???${formatMoney((-34756.556).toBigDecimal())}")
    }
}