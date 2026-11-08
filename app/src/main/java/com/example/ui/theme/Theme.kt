package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue80,
    onPrimary = Color(0xFF003258),
    primaryContainer = PrimaryBlueDark,
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = SecondaryTeal80,
    onSecondary = Color(0xFF003730),
    tertiary = TertiaryAmber80,
    onTertiary = Color(0xFF452B00),
    background = BackgroundDark,
    onBackground = Color(0xFFE2E2E6),
    surface = SurfaceDark,
    onSurface = Color(0xFFE2E2E6),
    surfaceVariant = Color(0xFF282C30),
    onSurfaceVariant = Color(0xFFC3C7CF)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1E4FF),
    onPrimaryContainer = PrimaryBlueDark,
    secondary = SecondaryTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCCE8E4),
    onSecondaryContainer = Color(0xFF00201C),
    tertiary = TertiaryAmber,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDEA3),
    onTertiaryContainer = Color(0xFF261900),
    background = SlateLight,
    onBackground = SlateDark,
    surface = SurfaceCard,
    onSurface = SlateDark,
    surfaceVariant = Color(0xFFE8EDF4),
    onSurfaceVariant = Color(0xFF44474E)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set false to maintain consistent educational identity
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
