package com.ud.finalproyect.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.ud.finalproyect.ui.theme.*

private val LightColors = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = OnPrimaryWhite,
    primaryContainer = PrimaryContainerBlue,
    onPrimaryContainer = OnPrimaryContainerBlue,
    secondary = SecondaryTeal,
    onSecondary = OnSecondaryWhite,
    secondaryContainer = SecondaryContainerTeal,
    onSecondaryContainer = OnSecondaryContainerTeal,
    tertiary = TertiaryCyan,
    onTertiary = OnTertiaryWhite,
    tertiaryContainer = TertiaryContainerCyan,
    onTertiaryContainer = OnTertiaryContainerCyan,
    error = ErrorRed,
    onError = OnErrorWhite,
    errorContainer = ErrorContainerRed,
    onErrorContainer = OnErrorContainerRed,
    background = BackgroundGray,
    onBackground = OnBackgroundDark,
    surface = SurfaceGray,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantGray,
    onSurfaceVariant = OnSurfaceVariantGray,
    outline = OutlineGray
)

private val DarkColors = darkColorScheme(
    primary = PrimaryBlueDark,
    onPrimary = OnPrimaryBlueDark,
    primaryContainer = PrimaryContainerBlueDark,
    onPrimaryContainer = OnPrimaryContainerBlueDark,
    secondary = SecondaryTealDark,
    onSecondary = OnSecondaryTealDark,
    secondaryContainer = SecondaryContainerTealDark,
    onSecondaryContainer = OnSecondaryContainerTealDark,
    tertiary = TertiaryCyanDark,
    onTertiary = OnTertiaryCyanDark,
    tertiaryContainer = TertiaryContainerCyanDark,
    onTertiaryContainer = OnTertiaryContainerCyanDark,
    background = BackgroundDark,
    onBackground = OnBackgroundWhite,
    surface = SurfaceDark,
    onSurface = OnSurfaceWhite,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantWhite,
    outline = OutlineDark
)

@Composable
fun MedicControlTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}
