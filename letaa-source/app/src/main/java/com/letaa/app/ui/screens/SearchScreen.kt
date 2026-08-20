package com.letaa.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.letaa.app.R
import com.letaa.app.data.HistoryStore
import com.letaa.app.ui.theme.LetaaAccent
import com.letaa.app.ui.theme.LetaaBg
import com.letaa.app.ui.theme.LetaaCard
import com.letaa.app.ui.theme.LetaaMuted
import com.letaa.app.ui.theme.LetaaText
import com.letaa.app.ui.theme.Onest
import kotlinx.coroutines.launch

@Composable
fun SearchScreen(
    history: HistoryStore,
    onSubmit: (String) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val items = remember(history.all()) { history.all() }
    val keyboard = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LetaaBg)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(top = 8.dp),
    ) {
        TextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .height(56.dp)
                .clip(RoundedCornerShape(18.dp))
                .focusRequester(focusRequester),
            placeholder = { Text("поиск или адрес", color = LetaaMuted, fontFamily = Onest, fontWeight = FontWeight.W500) },
            leadingIcon = { Icon(ImageVector.vectorResource(R.drawable.ic_magnifer_outline), null, tint = LetaaMuted) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    Icon(
                        ImageVector.vectorResource(R.drawable.ic_close_circle_bold),
                        contentDescription = "очистить",
                        tint = Color(0xFFC9C9D0),
                        modifier = Modifier.clickable { query = "" },
                    )
                }
            },
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(
                fontFamily = Onest, fontWeight = FontWeight.W500, fontSize = 16.5.sp, color = LetaaText,
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = LetaaCard,
                unfocusedContainerColor = LetaaCard,
                disabledContainerColor = LetaaCard,
                cursorColor = LetaaAccent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                if (query.isNotBlank()) { keyboard?.hide(); onSubmit(query) }
            }),
        )

        LaunchedEffectFocus(focusRequester)

        if (items.isEmpty() && query.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        ImageVector.vectorResource(R.drawable.ic_fire_bold),
                        contentDescription = null,
                        tint = Color(0xFFD3D3DA),
                        modifier = Modifier.height(34.dp),
                    )
                    Text(
                        "чисто. ни единого следа",
                        color = LetaaMuted,
                        fontFamily = Onest,
                        fontWeight = FontWeight.W500,
                        fontSize = 14.5.sp,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
            }
        } else {
            val filtered = if (query.isBlank()) items else items.filter { it.contains(query.trim(), ignoreCase = true) }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (query.isNotBlank()) {
                    item {
                        HistoryRow(
                            icon = R.drawable.ic_magnifer_outline,
                            text = query.trim(),
                            onClick = { onSubmit(query) },
                        )
                    }
                }
                items(filtered) { q ->
                    HistoryRow(icon = R.drawable.ic_history_outline, text = q, onClick = { onSubmit(q) })
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(icon: Int, text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Icon(ImageVector.vectorResource(icon), contentDescription = null, tint = Color(0xFFB4B4BC))
            Text(text, color = LetaaText, fontFamily = Onest, fontWeight = FontWeight.W500, fontSize = 15.5.sp)
        }
    }
}

@Composable
private fun LaunchedEffectFocus(focusRequester: FocusRequester) {
    androidx.compose.runtime.LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(420)
        runCatching { focusRequester.requestFocus() }
    }
}
