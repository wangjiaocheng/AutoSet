package top.autoget.autokit

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.util.Base64
import android.widget.ImageView
import top.autoget.autokit.AKit.app
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

object PreferenceKit {
    var fileName = "share_data"
    var all: Map<String, *> = app.getSharedPreferences(fileName, Context.MODE_PRIVATE).all
    fun putAll(values: Map<String, *>) = try {
        app.getSharedPreferences(fileName, Context.MODE_PRIVATE).edit().apply {
            for ((key, value) in values) {
                when (value) {
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Float -> putFloat(key, value)
                    is Boolean -> putBoolean(key, value)
                    is String -> putString(key, value)
                    else -> putString(key, value.toString())
                }
            }
        }.run { SharedPreferencesCompat.apply(this) }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    private object SharedPreferencesCompat {
        private val applyMethod: Method? = try {
            SharedPreferences.Editor::class.java.getMethod("apply")
        } catch (e: NoSuchMethodException) {
            null
        }

        fun apply(editor: SharedPreferences.Editor) {
            try {
                applyMethod?.invoke(editor) ?: editor.commit()
            } catch (e: IllegalArgumentException) {
            } catch (e: IllegalAccessException) {
            } catch (e: InvocationTargetException) {
            }
        }
    }

    @JvmOverloads
    fun getImage(key: String, imageView: ImageView? = null): Bitmap? =
        (this[key, ""] as String?)?.run {
            when (this) {
                "" -> (imageView?.drawable as BitmapDrawable?)?.bitmap
                else -> ByteArrayInputStream(Base64.decode(this, Base64.DEFAULT))
                    .use { byteArrayInputStream -> BitmapFactory.decodeStream(byteArrayInputStream) }
            }
        }

    fun putImage(key: String, imageView: ImageView) = ByteArrayOutputStream().use {
        (imageView.drawable as BitmapDrawable).bitmap.compress(Bitmap.CompressFormat.PNG, 80, it)
        put(key, String(Base64.encodeToString(it.toByteArray(), Base64.DEFAULT).toByteArray()))
    }

    operator fun get(key: String, defaultAny: Any): Any? =
        app.getSharedPreferences(fileName, Context.MODE_PRIVATE).run {
            when (defaultAny) {
                is Int -> getInt(key, defaultAny)
                is Long -> getLong(key, defaultAny)
                is Float -> getFloat(key, defaultAny)
                is Boolean -> getBoolean(key, defaultAny)
                is String -> getString(key, defaultAny)
                else -> null
            }
        }

    fun put(key: String, any: Any) =
        app.getSharedPreferences(fileName, Context.MODE_PRIVATE).edit().apply {
            when (any) {
                is Int -> putInt(key, any)
                is Long -> putLong(key, any)
                is Float -> putFloat(key, any)
                is Boolean -> putBoolean(key, any)
                is String -> putString(key, any)
                else -> putString(key, any.toString())
            }
        }.run { SharedPreferencesCompat.apply(this) }

    @JvmOverloads
    fun getStringSet(key: String, defaultSet: Set<String> = emptySet()): Set<String>? =
        app.getSharedPreferences(fileName, Context.MODE_PRIVATE)
            .getStringSet(key, defaultSet)

    fun putStringSet(key: String, set: Set<String>) =
        app.getSharedPreferences(fileName, Context.MODE_PRIVATE).edit()
            .apply { putStringSet(key, set) }.run { SharedPreferencesCompat.apply(this) }

    fun contains(key: String): Boolean =
        app.getSharedPreferences(fileName, Context.MODE_PRIVATE).contains(key)

    fun remove(key: String) = app.getSharedPreferences(fileName, Context.MODE_PRIVATE).edit()
        .apply { remove(key) }.run { SharedPreferencesCompat.apply(this) }

    fun clear() = app.getSharedPreferences(fileName, Context.MODE_PRIVATE).edit()
        .apply { clear() }.run { SharedPreferencesCompat.apply(this) }
}