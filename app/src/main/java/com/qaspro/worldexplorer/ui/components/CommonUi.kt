package com.qaspro.worldexplorer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.qaspro.worldexplorer.ui.theme.CardWhite
import com.qaspro.worldexplorer.ui.theme.SkyBlueDark

/** Big round tappable button — the child's main control. */
@Composable
fun RoundIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Int = 64,
    background: Color = CardWhite,
    tint: Color = SkyBlueDark,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .shadow(6.dp, CircleShape)
            .background(background, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.material3.LocalContentColor provides tint
        ) { content() }
    }
}

@Composable
fun SpeakerButton(onClick: () -> Unit, modifier: Modifier = Modifier, size: Int = 64) {
    RoundIconButton(onClick = onClick, modifier = modifier, size = size) {
        Icon(
            Icons.Rounded.VolumeUp,
            contentDescription = "Listen",
            modifier = Modifier.size((size * 0.5).dp)
        )
    }
}

@Composable
fun BackButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    RoundIconButton(onClick = onClick, modifier = modifier, size = 56) {
        Icon(
            Icons.Rounded.ArrowBack,
            contentDescription = "Back",
            modifier = Modifier.size(28.dp)
        )
    }
}

/** Big friendly emoji shown as a fallback while a flag/photo loads or is absent. */
@Composable
fun EmojiBadge(emoji: String, sizeDp: Int, modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(sizeDp.dp), contentAlignment = Alignment.Center) {
        Text(emoji, style = MaterialTheme.typography.displayLarge)
    }
}

@Composable
fun LoadingSpinner(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = SkyBlueDark)
    }
}
