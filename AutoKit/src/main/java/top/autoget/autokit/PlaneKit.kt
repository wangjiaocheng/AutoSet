package top.autoget.autokit

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

object PlaneKit {
    fun pointToDegrees(x: Double, y: Double): Double = Math.toDegrees(atan2(x, y))
    fun distance(x1: Double, y1: Double, x2: Double, y2: Double): Double =
        sqrt(abs(x1 - x2).pow(2) + abs(y1 - y2).pow(2))

    fun checkInRound(x1: Double, y1: Double, x2: Double, y2: Double, r: Double): Boolean =
        sqrt(abs(x1 - x2).pow(2) + abs(y1 - y2).pow(2)) < r
}