package it.leogalli.pathwise.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/* ── Paletta "notte alpina" (allineata alla landing page) ───── */

val Night950 = Color(0xFF05080D)
val Night900 = Color(0xFF0A1018)
val Night800 = Color(0xFF101926)
val Night700 = Color(0xFF1A2739)

val Mint300 = Color(0xFF6EE7B7)
val Mint400 = Color(0xFF34D399)
val Mint500 = Color(0xFF10B981)
val Mint600 = Color(0xFF059669)

val Sky400 = Color(0xFF38BDF8)
val Amber400 = Color(0xFFFBBF24)
val Rose400 = Color(0xFFFB7185)

val TextPrimary = Color(0xFFE2E8F0)
val TextSecondary = Color(0xFF94A3B8)

private val PathWiseDarkColors = darkColorScheme(
    primary = Mint400,
    onPrimary = Night950,
    primaryContainer = Mint600,
    onPrimaryContainer = Color.White,
    secondary = Sky400,
    onSecondary = Night950,
    background = Night950,
    onBackground = TextPrimary,
    surface = Night900,
    onSurface = TextPrimary,
    surfaceVariant = Night800,
    onSurfaceVariant = TextSecondary,
    outline = Night700,
    error = Rose400,
    onError = Night950,
)

/**
 * Tema Material 3 di PathWise.
 * Attualmente sempre scuro (coerente con il brand); il supporto light
 * può essere aggiunto senza stravolgere i colori della palette.
 */
@Composable
fun PathWiseTheme(content: @Composable () -> Unit) {
    // isSystemInDarkTheme() riservato per un futuro tema chiaro
    MaterialTheme(
        colorScheme = PathWiseDarkColors,
        content = content,
    )
}
