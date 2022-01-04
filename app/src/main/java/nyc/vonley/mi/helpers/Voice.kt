package nyc.vonley.mi.helpers


import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import nyc.vonley.mi.utils.SharedPreferenceManager

/**
 * Text to speech
 */
interface Voice : TextToSpeech.OnInitListener {
    val tts: TextToSpeech
    val voices: List<Voice>
    val speaking: Boolean
    val initialized: Boolean
    val manager: SharedPreferenceManager

    fun say(message: String) {
        say(message, "default")
    }

    fun setVoice(string: String){
        val find = voices.find { string == it.name }
        if(find != null){
            setVoice(find)
        }
    }

    fun setVoice(voice: Voice) {
        tts.voice = voice
        manager.voice = voice.name
    }

    fun say(message: String, utteranceId: String)

    fun stop() {
        tts.stop()
    }

    val engines: List<TextToSpeech.EngineInfo>
}