package com.letaa.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.letaa.app.R
import com.letaa.app.data.HistoryStore
import com.letaa.app.ui.components.BurnOverlay
import com.letaa.app.ui.components.LetaaSearchBar
import com.letaa.app.ui.theme.LetaaAccent
import com.letaa.app.ui.theme.LetaaBg
import com.letaa.app.ui.theme.LetaaCard
import com.letaa.app.ui.theme.LetaaGradient
import com.letaa.app.ui.theme.LetaaMuted
import com.letaa.app.ui.theme.LetaaToastBg
import com.letaa.app.ui.theme.Onest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    history: HistoryStore,
    onOpenSearch: () -> Unit,
    onDataBurned: () -> Unit,
) {
    var burning by remember { mutableStateOf(false) }
    var showToast by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize().background(LetaaBg)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "летаа",
                style = TextStyle(
                    brush = Brush.linearGradient(LetaaGradient),
                    fontSize = 44.sp,
                    fontFamily = Onest,
                    fontWeight = FontWeight.W800,
                    letterSpacing = (-1).sp,
                ),
                modifier = Modifier.padding(bottom = 24.dp),
            )
            LetaaSearchBar(
                leading = ImageVector.vectorResource(R.drawable.ic_magnifer_outline),
                trailing = ImageVector.vectorResource(R.drawable.ic_microphone_outline),
                placeholder = "поиск или адрес",
                onClick = onOpenSearch,
            )
        }

        Column(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 56.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .clip(CircleShape)
                    .background(LetaaCard)
                    .clickable {
                        if (!burning) {
                            burning = true
                            scope.launch {
                                delay(1150)
                                history.clear()
                                onDataBurned()
                                burning = false
                                showToast = true
                                delay(2100)
                                showToast = false
                            }
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    ImageVector.vectorResource(R.drawable.ic_fire_bold),
                    contentDescription = "сжечь всё",
                    tint = LetaaAccent,
                )
            }
            Text(
                text = "сжечь всё",
                color = LetaaMuted,
                fontFamily = Onest,
                fontWeight = FontWeight.W500,
                fontSize = 12.5.sp,
                modifier = Modifier.padding(top = 10.dp),
            )
        }

        AnimatedVisibility(
            visible = burning,
            enter = fadeIn(tween(150)),
            exit = fadeOut(tween(200)),
            modifier = Modifier.fillMaxSize(),
        ) {
            BurnOverlay()
        }

        AnimatedVisibility(
            visible = showToast,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200)),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 44.dp),
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(LetaaToastBg)
                    .padding(horizontal = 20.dp, vertical = 13.dp),
            ) {
                Text(
                    text = "🔥 всё сожжено — прямо всё-всё-всё",
                    color = Color.White,
                    fontFamily = Onest,
                    fontWeight = FontWeight.W500,
                    fontSize = 14.5.sp,
                )
            }
        }
    }
}
