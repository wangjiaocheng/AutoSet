package top.autoget.automap

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.*

class TtsSystem private constructor(context: Context?) : Tts {
    private val mContext: Context? = context?.applicationContext
    private var isSuccess = true
    private var textToSpeech: TextToSpeech? = null
        get() = TextToSpeech(mContext, TextToSpeech.OnInitListener {
            if (it == TextToSpeech.SUCCESS) field?.apply {
                setPitch(1f)//音调：值越大声音越尖（女生），值越小声音越粗（男生），1f是常规
                setSpeechRate(1f)//语速
                setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onDone(utteranceId: String?) {}
                    override fun onError(utteranceId: String?) {}
                    override fun onStart(utteranceId: String?) {}
                })
                setOnUtteranceCompletedListener { }
                setLanguage(Locale.CHINA).let { result ->
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
                        isSuccess = false
                }
            }
        })

    override fun initTts() {}
    override fun playText(playText: String?) {
        if (isSuccess) textToSpeech?.speak(playText, TextToSpeech.QUEUE_ADD, null, null)
    }

    override fun stopSpeak() {
        textToSpeech?.stop()
    }

    companion object {
        private var ttsSystem: TtsSystem? = null
        fun getInstance(context: Context?): TtsSystem? {
            if (ttsSystem == null) synchronized(TtsSystem::class.java) {
                if (ttsSystem == null) ttsSystem = TtsSystem(context)
            }
            return ttsSystem
        }
    }

    override fun destroy() {
        stopSpeak()
        textToSpeech?.shutdown()
        ttsSystem = null
    }

    override fun isPlaying(): Boolean = textToSpeech?.isSpeaking ?: false
    var callBack: TtsCallBack? = null
    override fun setCallback(ttsCallBack: TtsCallBack?) {
        callBack = ttsCallBack
    }
}