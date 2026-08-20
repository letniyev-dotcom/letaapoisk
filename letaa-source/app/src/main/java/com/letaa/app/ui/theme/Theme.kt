package com.letaa.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle

private val LetaaColorScheme = lightColorScheme(
    background = LetaaBg,
    surface = LetaaCard,
    primary = LetaaAccent,
    onBackground = LetaaText,
    onSurface = LetaaText,
)

private val LetaaTypography = Typography(
    bodyLarge = TextStyle(fontFamily = Onest, fontWeight = androidx.compose.ui.text.font.FontWeight.W500),
    bodyMedium = TextStyle(fontFamily = Onest, fontWeight = androidx.compose.ui.text.font.FontWeight.W500),
    titleLarge = TextStyle(fontFamily = Onest, fontWeight = androidx.compose.ui.text.font.FontWeight.W700),
)

@Composable
fun LetaaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LetaaColorScheme,
        typography = LetaaTypography,
        content = content,
    )
}
