package xyz.travitia.lister.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.travitia.lister.ListerApplication
import xyz.travitia.lister.data.model.FontSize

@Composable
fun rememberBodyFontSize(): androidx.compose.ui.unit.TextUnit {
    val context = LocalContext.current
    val application = context.applicationContext as ListerApplication
    val fontSize by application.settingsPreferences.fontSize.collectAsState(initial = FontSize.DEFAULT)
    return fontSize.getBodyTextSize().sp
}

@Composable
fun rememberHeaderFontSize(): androidx.compose.ui.unit.TextUnit {
    val context = LocalContext.current
    val application = context.applicationContext as ListerApplication
    val fontSize by application.settingsPreferences.fontSize.collectAsState(initial = FontSize.DEFAULT)
    return fontSize.getHeaderTextSize().sp
}

@Composable
fun rememberBadgeFontSize(): androidx.compose.ui.unit.TextUnit {
    val context = LocalContext.current
    val application = context.applicationContext as ListerApplication
    val fontSize by application.settingsPreferences.fontSize.collectAsState(initial = FontSize.DEFAULT)
    // Badge uses slightly smaller font than body
    return when (fontSize) {
        FontSize.SMALL -> 11.sp
        FontSize.MEDIUM -> 12.sp
        FontSize.LARGE -> 14.sp
    }
}

@Composable
fun rememberBadgePadding(): Dp {
    val context = LocalContext.current
    val application = context.applicationContext as ListerApplication
    val fontSize by application.settingsPreferences.fontSize.collectAsState(initial = FontSize.DEFAULT)
    return when (fontSize) {
        FontSize.SMALL -> 6.dp
        FontSize.MEDIUM -> 8.dp
        FontSize.LARGE -> 10.dp
    }
}

