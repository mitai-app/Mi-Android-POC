package io.vonley.mi.intents


import android.os.Binder
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.*
import io.vonley.mi.helpers.Voice
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext


class VoiceServiceBinder @Inject constructor(
    private val voice: Voice
) : Binder(), Observer<List<Any>>, CoroutineScope {

    private var schedule: ScheduledFuture<*>? = null
    private val utteranceProgressListener = object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) {

        }

        override fun onDone(utteranceId: String?) {
            start()
        }

        override fun onError(utteranceId: String?) {

        }
    }

    val TAG = VoiceServiceBinder::class.java.name

    interface ProcessCallback {
        fun onProcessed(model: Any)
        fun onStackEmpty()
    }

    init {
        voice.tts.setOnUtteranceProgressListener(utteranceProgressListener)
    }

    private val job = Job()
    private var thread_id: String? = null
    override val coroutineContext: CoroutineContext = job + Dispatchers.IO
    private val stack = Stack<Any>()
    private val processed = Stack<Any>()
    private var callback: ProcessCallback? = null
    private var liveData: LiveData<List<Any>>? = null
    val isActive: Boolean
        get() {
            return this.liveData != null && this.voice.speaking
        }
    val lastProcessedComment: Any?
        get() {
            return processed.lastOrNull()
        }
    val processedCount: Int
        get() {
            return processed.size
        }
    val toBeProcessCount: Int
        get() {
            return stack.size
        }

    fun setKarmaFilter(karmaValue: Int): Boolean {
        if (thread_id != null) {
            setLiveData(thread_id!!, karmaValue)
            return true
        }
        return false
    }

    fun setLiveData(thread_id: String, karmaValue: Int) {
        if(thread_id != this.thread_id){
            processed.clear()
        }
        if (liveData != null) {
            stack.clear()
            liveData?.removeObserver(this)
        }
        stack.clear()
        this.thread_id = thread_id
       // liveData = commentDao.getFiltered(thread_id, karmaValue)
        liveData?.observeForever(this)
    }


    fun start() {
        if (!stack.empty()) {
            val pop = stack.pop()
            processed.push(pop)
            launch {
                withContext(Dispatchers.Main) {
                    callback!!.onProcessed(pop)
                }
            }
            //voice.say(pop.body)
        } else {
            if(thread_id != null) {
                val id = thread_id!!
                launch {
                    withContext(Dispatchers.Main){
                        callback?.onStackEmpty()
                    }
                }
                if(this.schedule == null || this.schedule?.isDone == true) {
                    this.schedule = Executors.newSingleThreadScheduledExecutor().schedule({
                        Log.e(TAG, "Calling wsbNetwork.sendCommentRequest($id)")
                        //wsbNetwork.sendCommentRequest(id)
                    }, 10, TimeUnit.SECONDS)
                }
            }
        }
    }

    fun skip() {
        start()
    }

    fun setCallback(callback: ProcessCallback) {
        this.callback = callback
    }

    private fun clear() {
        voice.stop()
        stack.clear()
        /*if(wsbNetwork.manager.dontReprocess) {
            processed.clear()
        }*/
    }

    fun destroy() {
        clear()
        liveData?.removeObserver(this)
        liveData = null
        callback = null
    }

    // Stop play audio.
    fun stopAudio() {
        /*lastProcessedComment?.let {
            processed.remove(it)
            stack.add(it)
        }
        voice.stop()*/
    }



    fun detach() {
        callback = null
    }

    override fun onChanged(list: List<Any>?) {

        //val result = (list - stack) - processed
        val result = list?: emptyList()
        stack.addAll(result)
        if (!voice.speaking) {
            start()
        }
    }
}
