package top.autoget.autokit

import androidx.annotation.IntRange
import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or
import kotlin.experimental.xor

object BitKit : LoggerKit {
    fun checkBitValue(source: Byte, @IntRange(from = 0, to = 7) pos: Int): Boolean =
        source.toInt() ushr pos and 1 == 1

    fun getBitValue(source: Byte, @IntRange(from = 0, to = 7) pos: Int): Byte =
        (source.toInt() shr pos and 1).toByte()

    fun reverseBitValue(source: Byte, @IntRange(from = 0, to = 7) pos: Int): Byte =
        source xor (1 shl pos).toByte()

    fun setBitValue(source: Byte, @IntRange(from = 0, to = 7) pos: Int, value: Byte): Byte =
        (1 shl pos).toByte().let { if (value > 0) source or it else source and it.inv() }

    @JvmStatic
    fun main() {
        val source: Byte = 11//0000 1011
        for (i in 7 downTo 0) {
            debug("$loggerTag->${getBitValue(source, i)}")
        }//0000 1011
        debug("$loggerTag->${setBitValue(source, 6, 1.toByte())}")//0100 1011
        debug("$loggerTag->${reverseBitValue(source, 6)}")//0100 1011
        debug("$loggerTag->${checkBitValue(source, 6)}")//false
        for (i in 0..7) {
            if (checkBitValue(source, i)) debug("$loggerTag->$i")
        }//0 1 3
    }
}