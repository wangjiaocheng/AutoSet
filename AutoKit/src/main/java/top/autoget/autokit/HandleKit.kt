package top.autoget.autokit

import android.os.CountDownTimer
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import java.util.regex.PatternSyntaxException

object HandleKit {
    val mainHandler: Handler
        get() = Handler(Looper.getMainLooper())
    val backgroundHandler: Handler
        get() = Handler(HandlerThread("background").apply { start() }.looper, UiMessageKit)

    interface OnSimpleListener {
        fun doSomething()
    }

    fun delayToDo(delayTime: Long, onSimpleListener: OnSimpleListener) =
        mainHandler.postDelayed({ onSimpleListener.doSomething() }, delayTime)

    fun runOnUiThread(runnable: Runnable): Any = when {
        Looper.myLooper() == Looper.getMainLooper() -> runnable.run()
        else -> backgroundHandler.post(runnable)
    }

    fun runOnUiThreadDelayed(runnable: Runnable, delayMillis: Long): Boolean =
        backgroundHandler.postDelayed(runnable, delayMillis)

    fun fixListViewHeight(listView: ListView) = listView.adapter?.let { listAdapter ->
        var totalHeight = 0
        for (index in 0 until listAdapter.count) {
            totalHeight += listAdapter.getView(index, null, listView)
                .apply { measure(0, 0) }.measuredHeight
        }
        (totalHeight + listView.dividerHeight * (listAdapter.count - 1))
            .apply { listView.layoutParams.height = this }
    }//手动计算ListView高度，不再具有滚动效果

    fun countDown(textView: TextView, waitTime: Long, interval: Long, hint: String): TextView =
        textView.apply {
            isEnabled = false
            object : CountDownTimer(waitTime, interval) {
                override fun onTick(millisUntilFinished: Long) {
                    text = String.format("剩下 %d S", millisUntilFinished / 1000)
                }

                override fun onFinish() {
                    isEnabled = true
                    text = hint
                }
            }.start()
        }

    fun setEditNumberAuto(editText: EditText, number: Int, isStartForZero: Boolean): EditText =
        editText.apply {
            onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) setEditNumber(editText, number, isStartForZero)
            }
        }

    fun setEditNumber(editText: EditText, number: Int, isStartForZero: Boolean) {
        var stringBuilder: StringBuilder = StringBuilder(editText.text.toString()).apply {
            for (i in length until number) {
                insert(0, "0")
            }//00001234
        }
        if (!isStartForZero) StringBuilder().apply {
            for (i in 0 until number) {
                append("0")
            }//12340000
        }.let {
            if (stringBuilder.toString() == it.toString())//00000000
                stringBuilder = StringBuilder(it.substring(1) + "1")//00000001
        }
        editText.setText(stringBuilder.toString())
    }//输入全零情况下：true位数0、00、000、0000......起，false位数1、01、001、0001......起。

    @JvmOverloads
    fun setEditDecimal(editText: EditText, count: Int = 2): EditText = editText.apply {
        inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_CLASS_NUMBER
        filters = arrayOf(InputFilter { source, _, _, dest, _, _ ->
            dest.toString().run {
                when {
                    ".".contentEquals(source) && isEmpty() -> return@InputFilter "0."
                    contains(".") && substring(indexOf(".")).length == (if (count < 0) 0 else count) + 1 ->
                        return@InputFilter ""
                    this == "0" && source == "0" -> ""
                    else -> null
                }
            }
        })//设置字符过滤
    }

    fun setEditType(editText: EditText) = editText.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun afterTextChanged(s: Editable) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            editText.text.toString().let { editable ->
                stringFilter(editable).let { editableFilter ->
                    if (editable != editableFilter) editText.apply {
                        setText(editableFilter)
                        setSelection(editableFilter.length)//设新光标位置
                    }
                }//只许数字汉字
            }
        }
    })

    @Throws(PatternSyntaxException::class)
    fun stringFilter(string: String): String =
        "[^0-9\u4E00-\u9FA5]".toPattern().matcher(string).replaceAll("").trim { it <= ' ' }
}