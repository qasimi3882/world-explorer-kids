package com.qaspro.worldexplorer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import com.qaspro.worldexplorer.audio.AmbientSoundPlayer
import com.qaspro.worldexplorer.audio.NarrationController
import com.qaspro.worldexplorer.data.CountryRepository
import com.qaspro.worldexplorer.navigation.LocalAmbient
import com.qaspro.worldexplorer.navigation.LocalNarration
import com.qaspro.worldexplorer.navigation.LocalRepository
import com.qaspro.worldexplorer.navigation.WorldExplorerApp
import com.qaspro.worldexplorer.ui.theme.WorldExplorerTheme

class MainActivity : ComponentActivity() {

    // Held at Activity scope so they survive recomposition and are cleaned up
    // deterministically in the lifecycle callbacks below.
    private lateinit var narration: NarrationController
    private lateinit var ambient: AmbientSoundPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        narration = NarrationController(this)
        ambient = AmbientSoundPlayer(this)
        val repository = CountryRepository(applicationContext)

        setContent {
            WorldExplorerTheme {
                val repo = remember { repository }
                CompositionLocalProvider(
                    LocalNarration provides narration,
                    LocalAmbient provides ambient,
                    LocalRepository provides repo
                ) {
                    WorldExplorerApp()
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Never let a voice keep talking after the child leaves the app.
        narration.stop()
        ambient.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        narration.shutdown()
        ambient.release()
    }
}
