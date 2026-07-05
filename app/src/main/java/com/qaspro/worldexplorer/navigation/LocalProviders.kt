package com.qaspro.worldexplorer.navigation

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import com.qaspro.worldexplorer.audio.AmbientSoundPlayer
import com.qaspro.worldexplorer.audio.NarrationController
import com.qaspro.worldexplorer.data.CountryRepository

/**
 * App-wide singletons made available to every screen without prop-drilling.
 * Created once in MainActivity and provided at the top of the composition.
 */
val LocalNarration = staticCompositionLocalOf<NarrationController> {
    error("NarrationController not provided")
}

val LocalAmbient = staticCompositionLocalOf<AmbientSoundPlayer> {
    error("AmbientSoundPlayer not provided")
}

val LocalRepository = compositionLocalOf<CountryRepository> {
    error("CountryRepository not provided")
}
