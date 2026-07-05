package com.qaspro.worldexplorer.audio

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

/**
 * Loops a soft background atmosphere under each lesson (forest, ocean, city...).
 *
 * Sounds live in assets/ambient/<key>.mp3 and are referenced by key from the
 * country JSON (LessonItem.ambient). If a sound file isn't bundled yet the
 * player simply stays silent — lessons still work, they're just quieter.
 * Drop CC0 loops into assets/ambient/ to bring them to life (Phase 3/4).
 */
class AmbientSoundPlayer(context: Context) {

    private val player: ExoPlayer = ExoPlayer.Builder(context.applicationContext).build().apply {
        repeatMode = Player.REPEAT_MODE_ONE
        volume = 0.35f   // sits gently beneath the narration
    }

    private var currentKey: String? = null

    /** Cross-fade-free swap to a new ambient loop. No-op if the file is absent. */
    fun play(key: String?) {
        if (key == currentKey) return
        currentKey = key
        player.stop()
        player.clearMediaItems()
        if (key.isNullOrBlank()) return

        val uri = "asset:///ambient/$key.mp3"
        runCatching {
            player.setMediaItem(MediaItem.fromUri(uri))
            player.prepare()
            player.playWhenReady = true
        }
    }

    fun pause() { player.playWhenReady = false }
    fun resume() { if (currentKey != null) player.playWhenReady = true }

    fun release() {
        currentKey = null
        player.release()
    }

    init {
        // Missing/short assets should never crash a child's session.
        player.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                player.stop()
                currentKey = null
            }
        })
    }
}
