package com.qaspro.worldexplorer.audio

import android.content.Context
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

/**
 * Loops a soft background atmosphere under each lesson.
 * ExoPlayer is created lazily on the first play() call so it never
 * blocks or crashes Activity startup.
 */
class AmbientSoundPlayer(private val context: Context) {

    private var player: ExoPlayer? = null
    private var currentKey: String? = null

    private fun getOrCreatePlayer(): ExoPlayer? = runCatching {
        player ?: ExoPlayer.Builder(context.applicationContext).build().also { p ->
            p.repeatMode = Player.REPEAT_MODE_ONE
            p.volume = 0.35f
            p.addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    Log.w("Ambient", "Playback error: ${error.message}")
                    p.stop()
                    currentKey = null
                }
            })
            player = p
        }
    }.getOrElse { e ->
        Log.e("Ambient", "ExoPlayer init failed", e)
        null
    }

    fun play(key: String?) {
        if (key == currentKey) return
        currentKey = key
        val p = getOrCreatePlayer() ?: return
        p.stop()
        p.clearMediaItems()
        if (key.isNullOrBlank()) return
        runCatching {
            p.setMediaItem(MediaItem.fromUri("asset:///ambient/$key.mp3"))
            p.prepare()
            p.playWhenReady = true
        }
    }

    fun pause() { player?.playWhenReady = false }
    fun resume() { if (currentKey != null) player?.playWhenReady = true }

    fun release() {
        currentKey = null
        runCatching { player?.release() }
        player = null
    }
}
