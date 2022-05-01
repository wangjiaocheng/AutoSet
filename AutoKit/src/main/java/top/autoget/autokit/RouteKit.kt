package top.autoget.autokit

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import java.io.Serializable

object RouteKit {
    private var requestCode = -1
    fun requestCode(requestCode: Int): RouteKit? = apply { this.requestCode = requestCode }
    private var fromActivity: Activity? = null
    fun newIntent(context: Activity?): RouteKit? = apply { fromActivity = context }
    private var fromFragment: android.app.Fragment? = null
    fun newIntent(fragment: android.app.Fragment?): RouteKit? = apply { fromFragment = fragment }
    private var fromFragmentX: Fragment? = null
    fun newIntent(fragment: Fragment?): RouteKit? = apply { fromFragmentX = fragment }
    private var intent: Intent? = Intent()
    fun addFlags(flags: Int): RouteKit? = apply { intent?.addFlags(flags) }
    fun putExtraParam(key: String, value: Any?): RouteKit? =
        apply { intent = putExtra(intent, key, value) }

    private fun putExtra(intent: Intent?, key: String?, param: Any?): Intent? = intent?.apply {
        when (param) {
            is Serializable -> putExtra(key, param as Serializable?)
            is String -> putExtra(key, param as String?)
            is Array<*> -> putExtra(key, param as Array<*>?)
            is BooleanArray -> putExtra(key, param as BooleanArray?)
            is ShortArray -> putExtra(key, param as ShortArray?)
            is IntArray -> putExtra(key, param as IntArray?)
            is LongArray -> putExtra(key, param as LongArray?)
            is FloatArray -> putExtra(key, param as FloatArray?)
            is DoubleArray -> putExtra(key, param as DoubleArray?)
            is Bundle -> putExtra(key, param as Bundle?)
            is ByteArray -> putExtra(key, param as ByteArray?)
            is CharArray -> putExtra(key, param as CharArray?)
            is Parcelable -> putExtra(key, param as Parcelable?)
            is CharSequence -> putExtra(key, param as CharSequence?)
        }
    }

    interface RouterCallback {
        fun onBefore(from: Context?, to: Class<*>?)
        fun onNext(from: Context?, to: Class<*>?)
        fun onError(from: Context?, to: Class<*>?, throwable: Throwable?)
    }

    private var callback: RouterCallback? = null
    fun setCallback(callback: RouterCallback?) = apply { this.callback = callback }
    private var to: Class<*>? = null
    fun to(to: Class<*>?): RouteKit? = apply { this.to = to }
    private var data: Bundle? = null
    fun putBundle(data: Bundle?): RouteKit? = apply { this.data = data }
    fun putBundleParam(key: String, value: Any?): RouteKit? = apply {
        if (data == null) data = Bundle()
        data = putBundle(data, key, value)
    }

    private fun putBundle(bundle: Bundle?, key: String?, param: Any?): Bundle? = bundle?.apply {
        when (param) {
            is Serializable -> putSerializable(key, param as Serializable?)
            is String -> putString(key, param as String?)
            is Array<*> -> putStringArray(key, param as Array<String?>?)
            is BooleanArray -> putBooleanArray(key, param as BooleanArray?)
            is ShortArray -> putShortArray(key, param as ShortArray?)
            is IntArray -> putIntArray(key, param as IntArray?)
            is LongArray -> putLongArray(key, param as LongArray?)
            is FloatArray -> putFloatArray(key, param as FloatArray?)
            is DoubleArray -> putDoubleArray(key, param as DoubleArray?)
            is Bundle -> putBundle(key, param as Bundle?)
            is ByteArray -> putByteArray(key, param as ByteArray?)
            is CharArray -> putCharArray(key, param as CharArray?)
            is Parcelable -> putParcelable(key, param as Parcelable?)
            is CharSequence -> putCharSequence(key, param as CharSequence?)
        }
    }

    private var options: ActivityOptionsCompat? = null
    fun options(options: ActivityOptionsCompat?): RouteKit? = apply { this.options = options }
    private val startActivity = when {
        requestCode < 0 -> when {
            fromActivity != null -> fromActivity?.startActivity(intent)
            fromFragment != null -> fromFragment?.startActivity(intent)
            fromFragmentX != null -> fromFragmentX?.startActivity(intent)
            else -> {
            }
        }
        else -> when {
            fromActivity != null -> fromActivity?.startActivityForResult(intent, requestCode)
            fromFragment != null -> fromFragment?.startActivityForResult(intent, requestCode)
            fromFragmentX != null -> fromFragmentX?.startActivityForResult(intent, requestCode)
            else -> {
            }
        }
    }
    private const val RES_NONE = -1
    private const val ROUTER_ANIM_ENTER = RES_NONE
    private const val ROUTER_ANIM_EXIT = RES_NONE
    private var enterAnim = ROUTER_ANIM_ENTER
    private var exitAnim = ROUTER_ANIM_EXIT
    fun anim(enterAnim: Int, exitAnim: Int): RouteKit? = apply {
        this.enterAnim = enterAnim
        this.exitAnim = exitAnim
    }

    val launch = (fromActivity ?: fromFragment?.activity ?: fromFragmentX?.context).let { context ->
        try {
            when {
                intent == null || context == null || to == null -> Unit
                else -> {
                    callback?.onBefore(context, to)
                    to?.let { intent?.setClass(context, it) }
                    intent?.putExtras(data ?: Bundle())
                    options?.run {
                        intent?.let {
                            when {
                                requestCode < 0 ->
                                    ActivityCompat.startActivity(context, it, toBundle())
                                else -> ActivityCompat.startActivityForResult(
                                    context as Activity, it, requestCode, toBundle()
                                )
                            }
                        }
                    } ?: run {
                        startActivity
                        if (enterAnim > 0 && exitAnim > 0)
                            (context as Activity?)?.overridePendingTransition(enterAnim, exitAnim)
                    }
                    callback?.onNext(context, to)
                }
            }
        } catch (throwable: Throwable) {
            callback?.onError(context, to, throwable)
        }
    }

    fun pop(activity: Activity?) = activity?.finish()
}