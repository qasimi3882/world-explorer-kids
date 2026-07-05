package com.qaspro.worldexplorer.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val KidsColorScheme = lightColorScheme(
    primary = SkyBlueDark,
    onPrimary = CardWhite,
    secondary = GrassGreen,
    tertiary = CoralPink,
    background = CreamBackground,
    onBackground = InkText,
    surface = CardWhite,
    onSurface = InkText
)

// Big, friendly, rounded corners everywhere.
private val KidsShapes = Shapes(
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(32.dp)
)

@Composable
fun WorldExplorerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = KidsColorScheme,
        typography = AppTypography,
        shapes = KidsShapes,
        content = content
    )
}
