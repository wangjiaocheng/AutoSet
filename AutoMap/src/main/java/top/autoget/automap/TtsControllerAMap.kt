package top.autoget.automap

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import com.amap.api.navi.AMapNavi
import com.iflytek.cloud.*
import top.autoget.autokit.ToastKit.showShort
import java.util.*

class TtsControllerAMap private constructor(context: Context?) {
    private val ttsPlay: Int = 1
    private val ttsPlayCheck: Int = 2
    private var speechSynthesizer: SpeechSynthesizer? = null
    private val mContext: Context? = context?.applicationContext
    private val createSynthesizer = {
        if (speechSynthesizer == null) speechSynthesizer =
            SpeechSynthesizer.createSynthesizer(mContext) {
                when (it) {
                    ErrorCode.SUCCESS -> {
                    }
                    else -> showShort("语音合成初始化失败!")
                }
            }
    }
    private var isPlaying: Boolean = false
    private val wordList: LinkedList<String?> = LinkedList<String?>()
    private var ttsHandler: Handler? = null
        get() = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when (msg.what) {
                    ttsPlay -> speechSynthesizer?.let {
                        synchronized(it) {
                            if (!isPlaying && wordList.size > 0) {
                                isPlaying = true
                                createSynthesizer
                                it.startSpeaking(wordList.removeFirst(), object :
                                    SynthesizerListener {
                                    override fun onSpeakBegin() {
                                        AMapNavi.setTtsPlaying(true.apply { isPlaying = this })
                                    }

                                    override fun onBufferProgress(
                                        arg0: Int, arg1: Int, arg2: Int, arg3: String?
                                    ) {
                                        isPlaying = true
                                    }

                                    override fun onSpeakPaused() {}
                                    override fun onSpeakResumed() {
                                        isPlaying = true
                                    }

                                    override fun onSpeakProgress(arg0: Int, arg1: Int, arg2: Int) {
                                        isPlaying = true
                                    }

                                    override fun onCompleted(arg0: SpeechError?) {
                                        AMapNavi.setTtsPlaying(false.apply { isPlaying = this })
                                        field?.obtainMessage(1)?.sendToTarget()
                                    }

                                    override fun onEvent(
                                        arg0: Int, arg1: Int, arg2: Int, arg3: Bundle?
                                    ) {
                                    }
                                })
                            }
                        }
                    }
                    ttsPlayCheck -> if (!isPlaying) field?.obtainMessage(1)?.sendToTarget()
                }
            }
        }
    private val appId: String = "5ebebb88"

    init {
        SpeechUtility.createUtility(mContext, "${SpeechConstant.APPID}=$appId")
        createSynthesizer
    }

    fun init() = speechSynthesizer?.apply {
        setParameter(SpeechConstant.VOICE_NAME, "xiaoyan")//发音人
        setParameter(SpeechConstant.SPEED, "55")//语速[0-100]，默认50
        setParameter(SpeechConstant.VOLUME, "tts_volume")//音量
        setParameter(SpeechConstant.PITCH, "tts_pitch")//语调
    }

    fun onGetNavigationText(arg1: String?) {
        wordList.addLast(arg1)
        ttsHandler?.obtainMessage(ttsPlayCheck)?.sendToTarget()
    }

    fun stopSpeaking() {
        wordList.clear()
        speechSynthesizer?.stopSpeaking()
        isPlaying = false
    }

    fun destroy() {
        wordList.clear()
        speechSynthesizer?.destroy()
    }

    companion object {
        var ttsManagerAMap: TtsControllerAMap? = null
        fun getInstance(context: Context?): TtsControllerAMap? {
            if (ttsManagerAMap == null) ttsManagerAMap = TtsControllerAMap(context)
            return ttsManagerAMap
        }
    }
}