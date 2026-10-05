package com.infozatech.allinone.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Brand colours: InfozaTech blue and cyan.
private val Blue = Color(0xFF0B57F5)
private val Cyan = Color(0xFF00A3C4)

private val LightColors = lightColorScheme(
    primary = Blue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE6FF),
    onPrimaryContainer = Color(0xFF001A5C),
    secondary = Cyan,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCDF3FF),
    onSecondaryContainer = Color(0xFF001F28),
    tertiary = Color(0xFF7A4DFF),
    onTertiary = Color.White,
    background = Color(0xFFF6F8FC),
    onBackground = Color(0xFF14161F),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF14161F),
    surfaceVariant = Color(0xFFE8ECF5),
    onSurfaceVariant = Color(0xFF444A5A),
    outline = Color(0xFF747A8C),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9DB8FF),
    onPrimary = Color(0xFF002A87),
    primaryContainer = Color(0xFF0B3FC0),
    onPrimaryContainer = Color(0xFFDCE6FF),
    secondary = Color(0xFF5FD8F2),
    onSecondary = Color(0xFF003642),
    secondaryContainer = Color(0xFF004E5F),
    onSecondaryContainer = Color(0xFFCDF3FF),
    tertiary = Color(0xFFCFBDFF),
    onTertiary = Color(0xFF3A1A99),
    background = Color(0xFF0E1118),
    onBackground = Color(0xFFE4E7F0),
    surface = Color(0xFF151923),
    onSurface = Color(0xFFE4E7F0),
    surfaceVariant = Color(0xFF232837),
    onSurfaceVariant = Color(0xFFC3C8D6),
    outline = Color(0xFF8D93A5),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)

@Composable
fun InfozaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
