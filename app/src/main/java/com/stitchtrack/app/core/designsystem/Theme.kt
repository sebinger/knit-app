package com.stitchtrack.app.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Plum,
    onPrimary = OnPlum,
    primaryContainer = PlumContainer,
    onPrimaryContainer = OnPlumContainer,
    inversePrimary = InversePrimary,
    secondary = Terracotta,
    onSecondary = OnTerracotta,
    secondaryContainer = TerracottaContainer,
    onSecondaryContainer = OnTerracottaContainer,
    tertiary = Sage,
    onTertiary = OnSage,
    tertiaryContainer = SageContainer,
    onTertiaryContainer = OnSageContainer,
    background = Cream,
    onBackground = OnCream,
    surface = Cream,
    onSurface = OnCream,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    surfaceTint = Plum,
    inverseSurface = InverseSurface,
    inverseOnSurface = InverseOnSurface,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    outline = Outline,
    outlineVariant = OutlineVariant,
    scrim = Scrim,
)

@Composable
fun StitchTrackTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = StitchTrackTypography,
        shapes = StitchTrackShapes,
        content = content,
    )
}
