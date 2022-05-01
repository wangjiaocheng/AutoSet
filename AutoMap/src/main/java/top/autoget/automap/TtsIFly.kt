package top.autoget.automap

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import com.iflytek.cloud.*
import top.autoget.autokit.audioManager

class TtsIFly private constructor(context: Context?) : Tts, SynthesizerListener,
    AudioManager.OnAudioFocusChangeListener {
    private val mContext: Context? = context
    private val appId: String = "57b3c4a9"//替换科大讯飞应用ID字符串

    init {
        SpeechUtility.createUtility(mContext, "${SpeechConstant.APPID}=$appId")
    }

    private val speechSynthesizer: SpeechSynthesizer =
        SpeechSynthesizer.createSynthesizer(mContext) { errorCode ->
            if (errorCode == ErrorCode.SUCCESS) {
            }
        }

    override fun initTts() {
        speechSynthesizer.apply {
            setParameter(SpeechConstant.VOICE_NAME, "xiaoyan")//发音人
            setParameter(SpeechConstant.SPEED, "55")//语速[0,100]，默认50
            setParameter(SpeechConstant.VOLUME, "tts_volume")//音量
            setParameter(SpeechConstant.PITCH, "tts_pitch")//语调
            setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "false")//与其他音频软件冲突时是否暂停其他音频
            setParameter(SpeechConstant.VOICE_NAME, "vixy")//发音人，女生仅vixy支持多音字播报
        }
    }

    private val manager: AudioManager? = mContext?.audioManager
    private var isPlaying = false
    override fun playText(playText: String?) { //多音字处理
        when {
            playText?.contains("京藏") == true -> playText.replace("京藏", "京藏[=zang4]")
            else -> playText
        }?.let {
            if (it.isNotEmpty() && manager?.requestAudioFocus(
                    this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
                ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            ) {
                speechSynthesizer.startSpeaking(it, this)
                isPlaying = true
            }
        }
    }

    override fun stopSpeak() {
        speechSynthesizer.stopSpeaking()
        isPlaying = false
    }

    companion object {
        private var ttsIFly: TtsIFly? = null
        fun getInstance(context: Context?): TtsIFly? {
            if (ttsIFly == null) synchronized(TtsIFly::class.java) {
                if (ttsIFly == null) ttsIFly = TtsIFly(context)
            }
            return ttsIFly
        }
    }

    override fun destroy() {
        stopSpeak()
        speechSynthesizer.destroy()
        ttsIFly = null
    }

    override fun isPlaying(): Boolean = isPlaying
    var callBack: TtsCallBack? = null
    override fun setCallback(ttsCallBack: TtsCallBack?) {
        callBack = ttsCallBack
    }

    override fun onSpeakBegin() {
        isPlaying = true
    }

    override fun onBufferProgress(arg0: Int, arg1: Int, arg2: Int, arg3: String?) {}
    override fun onSpeakPaused() {
        isPlaying = false
    }

    override fun onSpeakResumed() {}
    override fun onSpeakProgress(arg0: Int, arg1: Int, arg2: Int) {}
    override fun onCompleted(arg0: SpeechError?) {
        isPlaying = false
        manager?.abandonAudioFocus(this)
        if (arg0 == null) callBack?.onCompleted(0)
    }

    override fun onEvent(arg0: Int, arg1: Int, arg2: Int, arg3: Bundle?) {}
    override fun onAudioFocusChange(focusChange: Int) {}
}