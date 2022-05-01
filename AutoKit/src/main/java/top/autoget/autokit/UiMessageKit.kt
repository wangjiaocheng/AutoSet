package top.autoget.autokit

import android.os.Handler
import android.os.Message
import android.util.SparseArray
import top.autoget.autokit.ApplicationKit.isAppDebug
import top.autoget.autokit.HandleKit.backgroundHandler

object UiMessageKit : LoggerKit, Handler.Callback {
    fun send(id: Int): Boolean = backgroundHandler.sendEmptyMessage(id)
    fun send(id: Int, obj: Any): Boolean =
        backgroundHandler.run { sendMessage(obtainMessage(id, obj)) }

    class UiMessage @JvmOverloads constructor(private var mMessage: Message? = null) {
        fun setMessage(message: Message?) {
            mMessage = message
        }

        private val isUiThread: Message
            get() = checkNotNull(mMessage)
            { "You can't use LocalMessage instance from a non-UI thread. Extract the data from LocalMessage and don't hold a reference to it outside of handleMessage()" }
        val id: Int
            get() = mMessage?.what?.apply { isUiThread } ?: 0
        val any: Any?
            get() = mMessage?.obj?.apply { isUiThread }

        override fun toString(): String = StringBuilder().apply {
            append("{ id=")
            append(id)
            any?.let {
                append(" obj=")
                append(any)
            }
            append(" }")
        }.toString().apply { isUiThread }
    }

    interface UiMessageCallback {
        fun handleMessage(localMessage: UiMessage)
    }

    private val listenersUniversal: MutableList<UiMessageCallback> = mutableListOf()
    fun addListener(listener: UiMessageCallback) = synchronized(listenersUniversal) {
        when {
            listenersUniversal.contains(listener) -> when {
                isAppDebug -> warn("$loggerTag->Listener is already added. $listener")
                else -> Unit
            }
            else -> listenersUniversal.add(listener)
        }
    }

    private val listenersSpecific = SparseArray<MutableList<UiMessageCallback>>()
    fun addListener(id: Int, listener: UiMessageCallback) = synchronized(listenersSpecific) {
        var idListeners: MutableList<UiMessageCallback>? = listenersSpecific[id]
        if (idListeners == null) {
            idListeners = mutableListOf()
            listenersSpecific.put(id, idListeners)
        }
        if (!idListeners.contains(listener)) idListeners.add(listener)
    }

    fun removeListener(listener: UiMessageCallback) = synchronized(listenersUniversal) {
        when {
            listenersUniversal.contains(listener) -> listenersUniversal.remove(listener)
            else -> when {
                isAppDebug -> warn("$loggerTag->Trying to remove a listener that is not registered. $listener")
                else -> Unit
            }
        }
    }

    fun removeListener(id: Int, listener: UiMessageCallback) = synchronized(listenersSpecific) {
        val idListeners: MutableList<UiMessageCallback>? = listenersSpecific[id]
        when {
            idListeners == null || idListeners.isEmpty() -> when {
                isAppDebug -> warn("$loggerTag->Trying to remove specific listener that is not registered. ID $id, $listener")
                else -> Unit
            }
            else -> when {
                idListeners.contains(listener) -> idListeners.remove(listener)
                else -> when {
                    isAppDebug -> warn("$loggerTag->Trying to remove specific listener that is not registered. ID $id, $listener")
                    else -> Unit
                }
            }
        }
    }

    fun removeListener(id: Int) {
        if (isAppDebug) listenersSpecific[id].run {
            if (this == null || isEmpty())
                warn("$loggerTag->Trying to remove specific listeners that are not registered. ID $id")
        }
        synchronized(listenersSpecific) { listenersSpecific.delete(id) }
    }

    private val uiMessage = UiMessage()
    private val defensiveCopyList: MutableList<UiMessageCallback> = mutableListOf()
    override fun handleMessage(msg: Message): Boolean {
        uiMessage.setMessage(msg)
        if (isAppDebug) logMessageHandling(uiMessage)
        synchronized(listenersSpecific) {
            listenersSpecific[msg.what]?.let { idListeners ->
                when {
                    idListeners.isEmpty() -> listenersSpecific.remove(msg.what)
                    else -> defensiveCopyList.apply { addAll(idListeners) }.run {
                        for (uiMessageCallback in this) {
                            uiMessageCallback.handleMessage(uiMessage)
                        }
                        clear()
                    }
                }
            }
        }
        synchronized(listenersUniversal) {
            if (listenersUniversal.size > 0) defensiveCopyList.apply { addAll(listenersUniversal) }
                .run {
                    for (uiMessageCallback in this) {
                        uiMessageCallback.handleMessage(uiMessage)
                    }
                    clear()
                }
        }
        uiMessage.setMessage(null)
        return true
    }

    private fun logMessageHandling(uiMessage: UiMessage) =
        listenersSpecific[uiMessage.id].let { idListeners ->
            when {
                (idListeners == null || idListeners.isEmpty()) && listenersUniversal.size == 0 ->
                    warn("$loggerTag->Delivering FAILED for message ID ${uiMessage.id}. No listeners. $uiMessage")
                else -> StringBuilder().apply {
                    append("Delivering message ID ${uiMessage.id}, Specific listeners: ")
                    when {
                        idListeners == null || idListeners.isEmpty() -> append(0)
                        else -> {
                            append("${idListeners.size} [")
                            for (idListener in idListeners) {
                                append(idListener.javaClass.simpleName)
                                if (idListener != idListeners[idListeners.size - 1]) append(",")
                            }
                            append("]")
                        }
                    }
                    append(", Universal listeners: ")
                    synchronized(listenersUniversal) {
                        when (listenersUniversal.size) {
                            0 -> append(0)
                            else -> {
                                append(listenersUniversal.size)
                                append(" [")
                                for (listenerUniversal in listenersUniversal) {
                                    append(listenerUniversal.javaClass.simpleName)
                                    if (listenerUniversal != listenersUniversal[listenersUniversal.size - 1])
                                        append(",")
                                }
                                append("], Message: ")
                            }
                        }
                    }
                    append(uiMessage.toString())
                }.run { verbose("$loggerTag->$this") }
            }
        }
}