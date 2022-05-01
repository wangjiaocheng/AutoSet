package top.autoget.autokit

import android.os.Build.VERSION_CODES.JELLY_BEAN
import android.os.Build.VERSION_CODES.JELLY_BEAN_MR2
import android.util.SparseArray
import android.util.SparseBooleanArray
import android.util.SparseIntArray
import android.util.SparseLongArray
import androidx.annotation.RequiresApi
import androidx.collection.LongSparseArray
import androidx.collection.SimpleArrayMap
import top.autoget.autokit.VersionKit.aboveJellyBean
import top.autoget.autokit.VersionKit.aboveJellyBeanMR2

object EmptyKit {
    fun isEmptyAny(any: Any?): Boolean = any?.let {
        when {
            any.javaClass.isArray && (any as Array<*>).size == 0 -> true
            any is CharSequence && any.toString().isEmpty() -> true
            any is Collection<*> && any.isEmpty() -> true
            any is Map<*, *> && any.isEmpty() -> true
            any is SimpleArrayMap<*, *> && any.isEmpty -> true
            any is SparseArray<*> && any.size() == 0 -> true
            any is SparseBooleanArray && any.size() == 0 -> true
            any is SparseIntArray && any.size() == 0 -> true
            any is LongSparseArray<*> && any.size() == 0 -> true
            aboveJellyBeanMR2 && any is SparseLongArray && any.size() == 0 -> true
            aboveJellyBean && any is android.util.LongSparseArray<*> && any.size() == 0 -> true
            else -> false
        }
    } ?: true

    fun isEmptyAny(any: CharSequence?): Boolean = any == null || any.toString().isEmpty()
    fun isEmptyAny(any: Collection<*>?): Boolean = any == null || any.isEmpty()
    fun isEmptyAny(any: Map<*, *>?): Boolean = any == null || any.isEmpty()
    fun isEmptyAny(any: SimpleArrayMap<*, *>?): Boolean = any == null || any.isEmpty
    fun isEmptyAny(any: SparseArray<*>?): Boolean = any == null || any.size() == 0
    fun isEmptyAny(any: SparseBooleanArray?): Boolean = any == null || any.size() == 0
    fun isEmptyAny(any: SparseIntArray?): Boolean = any == null || any.size() == 0
    fun isEmptyAny(any: LongSparseArray<*>?): Boolean = any == null || any.size() == 0

    @RequiresApi(JELLY_BEAN_MR2)
    fun isEmptyAny(any: SparseLongArray?): Boolean = any == null || any.size() == 0

    @RequiresApi(JELLY_BEAN)
    fun isEmptyAny(any: android.util.LongSparseArray<*>?): Boolean = any == null || any.size() == 0

    fun isNotEmptyAny(any: Any): Boolean = !isEmptyAny(any)
    fun isNotEmptyAny(any: CharSequence): Boolean = !isEmptyAny(any)
    fun isNotEmptyAny(any: Collection<*>): Boolean = !isEmptyAny(any)
    fun isNotEmptyAny(any: Map<*, *>): Boolean = !isEmptyAny(any)
    fun isNotEmptyAny(any: SimpleArrayMap<*, *>): Boolean = !isEmptyAny(any)
    fun isNotEmptyAny(any: SparseArray<*>): Boolean = !isEmptyAny(any)
    fun isNotEmptyAny(any: SparseBooleanArray): Boolean = !isEmptyAny(any)
    fun isNotEmptyAny(any: SparseIntArray): Boolean = !isEmptyAny(any)
    fun isNotEmptyAny(any: LongSparseArray<*>): Boolean = !isEmptyAny(any)

    @RequiresApi(JELLY_BEAN_MR2)
    fun isNotEmptyAny(any: SparseLongArray): Boolean = !isEmptyAny(any)

    @RequiresApi(JELLY_BEAN)
    fun isNotEmptyAny(any: android.util.LongSparseArray<*>): Boolean = !isEmptyAny(any)

    fun hashCode(any: Any?): Int = any?.hashCode() ?: 0
    fun <T> getOrDefault(any: T?, defaultAny: T): T = any ?: defaultAny
    fun equalsAny(any1: Any?, any2: Any): Boolean = any1 === any2 || any1 != null && any1 == any2
    fun requireNonNull(arrayOfAny: Array<Any?>?) = arrayOfAny?.let {
        for (any in it) {
            any ?: throw NullPointerException()
        }
    } ?: throw NullPointerException()
}