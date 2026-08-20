package com.letaa.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.onConsumedWindowInsetsChanged
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.letaa.app.R
import com.letaa.app.net.SearchClient
import com.letaa.app.net.SearchResult
import com.letaa.app.ui.theme.LetaaAvatarPalette
import com.letaa.app.ui.theme.LetaaBg
import com.letaa.app.ui.theme.LetaaCard
import com.letaa.app.ui.theme.LetaaMuted
import com.letaa.app.ui.theme.LetaaText
import com.letaa.app.ui.theme.Onest

/**
 * Экран результатов. Баг из HTML-прототипа («под поисковой строкой что-то
 * обрезает контент») здесь устранён на уровне архитектуры: высота шапки
 * (кнопка назад + строка запроса) измеряется реально через
 * onGloballyPositioned, и именно эта величина, а не подобранная на глаз
 * константа, идёт в contentPadding списка — так шапка и список никогда
 * не могут наехать друг на друга или обрезать контент.
 */
@Composable
fun ResultsScreen(
    query: String,
    onBack: () -> Unit,
    onOpenQuery: () -> Unit,
    onOpenPage: (url: String, title: String) -> Unit,
) {
    var results by remember(query) { mutableStateOf<List<SearchResult>?>(null) }
    var headerHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    LaunchedEffect(query) {
        results = null
        results = SearchClient.search(query)
    }

    Box(modifier = Modifier.fillMaxSize().background(LetaaBg)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = headerHeight + 18.dp, bottom = 40.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                WebSearchCard(query = query, onClick = { onOpenPage(SearchClient.webSearchUrl(query), "Поиск в интернете") })
            }
            val list = results
            if (list == null) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = LetaaMuted)
                    }
                }
            } else {
                items(list) { r ->
                    ResultCard(result = r, onClick = { onOpenPage(r.url, r.title) })
                }
            }
        }

        // шапка — измеряем её реальную высоту в px→dp и кормим этим числом
        // contentPadding списка выше (см. описание бага в шапке файла)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .onGloballyPositioned { coords ->
                    headerHeight = with(density) { coords.size.height.toDp() }
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(LetaaCard)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(ImageVector.vectorResource(R.drawable.ic_arrow_left_outline), contentDescription = "назад", tint = LetaaText)
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(LetaaCard)
                    .clickable(onClick = onOpenQuery)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(ImageVector.vectorResource(R.drawable.ic_magnifer_outline), contentDescription = null, tint = Color(0xFFB4B4BC))
                Text(
                    query, color = LetaaText, fontFamily = Onest, fontWeight = FontWeight.W500,
                    fontSize = 15.sp, maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun WebSearchCard(query: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(999.dp))
            .background(LetaaCard)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(ImageVector.vectorResource(R.drawable.ic_global_outline), contentDescription = null, tint = LetaaMuted)
        Text(
            "искать «$query» в интернете", color = LetaaText, fontFamily = Onest,
            fontWeight = FontWeight.W600, fontSize = 14.sp, maxLines = 1,
        )
    }
}

@Composable
private fun ResultCard(result: SearchResult, onClick: () -> Unit) {
    val color = remember(result.domain) { LetaaAvatarPalette[(result.domain.hashCode().let { if (it < 0) -it else it }) % LetaaAvatarPalette.size] }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(LetaaCard)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier.size(22.dp).clip(CircleShape).background(color),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    result.domain.take(1).uppercase(), color = Color.White,
                    fontFamily = Onest, fontWeight = FontWeight.W700, fontSize = 11.sp,
                )
            }
            Text(result.domain, color = LetaaMuted, fontFamily = Onest, fontWeight = FontWeight.W500, fontSize = 12.5.sp)
        }
        Text(
            result.title, color = LetaaText, fontFamily = Onest, fontWeight = FontWeight.W600,
            fontSize = 16.5.sp, modifier = Modifier.padding(top = 8.dp, bottom = 5.dp),
        )
        Text(
            result.snippet, color = LetaaMuted, fontFamily = Onest, fontWeight = FontWeight.W400,
            fontSize = 13.5.sp, maxLines = 3,
        )
    }
}
