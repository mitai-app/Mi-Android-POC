package nyc.vonley.mi.helpers.impl


import android.app.Application
import android.speech.tts.TextToSpeech
import android.util.Log
import nyc.vonley.mi.helpers.Voice
import nyc.vonley.mi.utils.SharedPreferenceManager
import java.util.*

/**
 * Test to speech
 */
class VoiceImpl(val context: Application, override val manager: SharedPreferenceManager) : Voice {

    override val tts: TextToSpeech = TextToSpeech(context, this)

    override val voices: List<android.speech.tts.Voice>
        get() = tts.voices.filter {
            when (it.locale) {
                Locale.US, Locale.UK -> !it.isNetworkConnectionRequired
                else -> false
            }
        }

    override val engines: List<TextToSpeech.EngineInfo>
        get() = tts.engines


    override val speaking: Boolean
        get() = tts.isSpeaking

    override var initialized: Boolean = false
        private set


    override fun say(message: String, utteranceId: String) {
        tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    override fun onInit(status: Int) {
        when (status) {
            TextToSpeech.SUCCESS -> {
                initialized = true
                tts.language = Locale.getDefault()
                if(manager.voice != null){
                    tts.voice = tts.voices.find { manager.voice == it.name }
                    Log.e(javaClass.name, "Has Voice")
                }else{
                    Log.e(javaClass.name, "No Voice")
                }
                tts.setPitch(manager.pitch)
                tts.setSpeechRate(manager.speed)
                Log.e(javaClass.name, "Voice Synthesizer Initialized")
            }
            else -> Log.e(javaClass.name, "Failed to Initialize Text To Speech")
        }
    }

    companion object {
        val TAG = VoiceImpl::class.java.name
    }

}