package xyz.travitia.lister.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import xyz.travitia.lister.ListerApplication
import xyz.travitia.lister.data.model.PrimaryColor

private fun getDarkColorScheme(primaryColor: Color) = darkColorScheme(
    primary = primaryColor,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    primaryContainer = primaryColor,
    onPrimary = Color.White
)

private fun getLightColorScheme(primaryColor: Color) = lightColorScheme(
    primary = primaryColor,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    primaryContainer = primaryColor,
    onPrimary = Color.White,
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F)
)

@Composable
fun ListerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as ListerApplication
    val selectedPrimaryColor by application.settingsPreferences.primaryColor.collectAsState(
        initial = PrimaryColor.DEFAULT
    )
    val useMaterialYou by application.settingsPreferences.useMaterialYou.collectAsState(initial = false)

    val colorScheme = when {
        useMaterialYou && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> getDarkColorScheme(selectedPrimaryColor.toColor())
        else -> getLightColorScheme(selectedPrimaryColor.toColor())
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

