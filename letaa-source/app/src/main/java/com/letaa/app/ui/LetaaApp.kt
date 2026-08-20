package com.letaa.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.letaa.app.data.HistoryStore
import com.letaa.app.ui.components.PushHost
import com.letaa.app.ui.components.PushOverlay
import com.letaa.app.ui.components.rememberSlideProgress
import com.letaa.app.ui.screens.HomeScreen
import com.letaa.app.ui.screens.PageScreen
import com.letaa.app.ui.screens.ResultsScreen
import com.letaa.app.ui.screens.SearchScreen
import com.letaa.app.ui.theme.LetaaBg

/**
 * Навигация — цепочка push-оверлеев (главная → поиск → результаты →
 * страница), как в Pace/Letify: один Animatable<Float> на переход,
 * predictive back и свайп-назад работают на каждом уровне из коробки.
 */
@Composable
fun LetaaApp() {
    val context = LocalContext.current
    val history = remember { HistoryStore(context.applicationContext) }
    var historyVersion by remember { mutableStateOf(0) }

    var searchOpen by remember { mutableStateOf(false) }
    val searchSlide = rememberSlideProgress()

    Box(modifier = Modifier.fillMaxSize().background(LetaaBg)) {
        PushHost(progress = searchSlide) {
            HomeScreen(
                history = history,
                onOpenSearch = { searchOpen = true },
                onDataBurned = { historyVersion++; searchOpen = false },
            )
        }

        if (searchOpen) {
            PushOverlay(progress = searchSlide, onDismissed = { searchOpen = false }) { backToHome ->
                var resultsQuery by remember { mutableStateOf<String?>(null) }
                val resultsSlide = rememberSlideProgress()

                PushHost(progress = resultsSlide) {
                    key(historyVersion) {
                        SearchScreen(
                            history = history,
                            onSubmit = { q ->
                                history.add(q)
                                historyVersion++
                                resultsQuery = q
                            },
                        )
                    }
                }

                if (resultsQuery != null) {
                    PushOverlay(progress = resultsSlide, onDismissed = { resultsQuery = null }) { backToSearch ->
                        var pageUrl by remember { mutableStateOf<String?>(null) }
                        val pageSlide = rememberSlideProgress()

                        PushHost(progress = pageSlide) {
                            ResultsScreen(
                                query = resultsQuery.orEmpty(),
                                onBack = backToHome,
                                onOpenQuery = backToSearch,
                                onOpenPage = { url, _ -> pageUrl = url },
                            )
                        }

                        if (pageUrl != null) {
                            PushOverlay(progress = pageSlide, onDismissed = { pageUrl = null }) { backToResults ->
                                PageScreen(url = pageUrl.orEmpty(), onBack = backToResults)
                            }
                        }
                    }
                }
            }
        }
    }
}

