package com.letaa.app.ui.theme

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.letaa.app.R

// Onest — единственный шрифт вариативный (fvar); подключаем нужные насечки
// веса через FontVariation, как рекомендует Compose для variable-фонтов.
@OptIn(ExperimentalTextApi::class)
val Onest = FontFamily(
    Font(R.font.onest, FontWeight.W400, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.onest, FontWeight.W500, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(R.font.onest, FontWeight.W600, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
    Font(R.font.onest, FontWeight.W700, variationSettings = FontVariation.Settings(FontVariation.weight(700))),
    Font(R.font.onest, FontWeight.W800, variationSettings = FontVariation.Settings(FontVariation.weight(800))),
)
