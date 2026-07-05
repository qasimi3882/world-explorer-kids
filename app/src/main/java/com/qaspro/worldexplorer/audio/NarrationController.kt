package com.qaspro.worldexplorer.audio

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

/**
 * The friendly teacher's voice.
 *
 * Wraps Android TextToSpeech and speaks short sentences one after another with
 * a natural pause between each. Tuned to sound warm and unhurried for young
 * children (slower rate, slightly higher pitch).
 *
 * On-device TTS means zero bundled audio and correct-enough pronunciation for
 * English narration. Country/word phonetics are handled by respelling in the
 * JSON (e.g. "soo-shee") so the voice says names naturally.
 */
class NarrationController(context: Context) {

    private var tts: TextToSpeech? = null
    private var ready = false
    private var pendingLines: List<String>? = null

    /** Index of the sentence currently being spoken, or -1 when idle. */
    private val _spokenIndex = MutableStateFlow(-1)
    val spokenIndex: StateFlow<Int> = _spokenIndex

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                configureVoice()
                ready = true
                pendingLines?.let { speak(it); pendingLines = null }
            }
        }
    }

    private fun configureVoice() {
        val engine = tts ?: return
        engine.language = Locale.US
        engine.setSpeechRate(0.92f)   // a touch slower — easy to follow
        engine.setPitch(1.12f)        // a little brighter — friendly

        // Prefer a higher-quality, non-network voice when one is available.
        runCatching {
            val best: Voice? = engine.voices
                ?.filter { it.locale == Locale.US && !it.isNetworkConnectionRequired }
                ?.minByOrNull { it.quality * -1 }
            if (best != null) engine.voice = best
        }

        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                utteranceId?.toIntOrNull()?.let { _spokenIndex.value = it }
                _isSpeaking.value = true
            }
            override fun onDone(utteranceId: String?) {
                // Cleared when the whole queue finishes (last line).
            }
            @Deprecated("deprecated in API level 21")
            override fun onError(utteranceId: String?) { _isSpeaking.value = false }
            override fun onError(utteranceId: String?, errorCode: Int) {
                _isSpeaking.value = false
            }
        })
    }

    /** Speak a single sentence immediately, replacing anything in progress. */
    fun speak(line: String) = speak(listOf(line))

    /**
     * Speak several short sentences in order, with a gentle pause between each.
     * Automatically stops any narration already playing.
     */
    fun speak(lines: List<String>) {
        val engine = tts
        val clean = lines.map { it.trim() }.filter { it.isNotEmpty() }
        if (clean.isEmpty()) return
        if (!ready || engine == null) { pendingLines = clean; return }

        engine.stop()
        _spokenIndex.value = -1
        clean.forEachIndexed { index, line ->
            val params = Bundle()
            engine.speak(line, TextToSpeech.QUEUE_ADD, params, index.toString())
            // A short silence gives each sentence room to breathe.
            engine.playSilentUtterance(360L, TextToSpeech.QUEUE_ADD, "pause_$index")
        }
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
        _spokenIndex.value = -1
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        ready = false
    }
}
