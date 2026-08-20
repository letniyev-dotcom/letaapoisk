package com.letaa.app.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.letaa.app.R
import com.letaa.app.ui.theme.LetaaText
import com.letaa.app.ui.theme.Onest

/**
 * Безграничная страница: WebView занимает весь экран (fillMaxSize, без
 * дополнительных отступов сверху/снизу) — реальный движок Chromium
 * (android.webkit.WebView) грузит настоящие сайты. Плавающая пилюля
 * с управлением не обрезает контент, а лежит поверх и прячется при скролле.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PageScreen(url: String, onBack: () -> Unit) {
    val context = LocalContext.current
    var currentUrl by remember { mutableStateOf(url) }
    var isHttps by remember { mutableStateOf(url.startsWith("https://")) }
    var pillVisible by remember { mutableStateOf(true) }
    var lastScrollY by remember { mutableFloatStateOf(0f) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Color.White)) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView, u: String) {
                            currentUrl = u
                            isHttps = u.startsWith("https://")
                        }
                    }
                    setOnScrollChangeListener { _, _, scrollY, _, _ ->
                        val dy = scrollY - lastScrollY
                        if (dy > 12 && scrollY > 80) pillVisible = false
                        if (dy < -12 || scrollY <= 80) pillVisible = true
                        lastScrollY = scrollY.toFloat()
                    }
                    loadUrl(url)
                    webViewRef = this
                }
            },
        )

        AnimatedVisibility(
            visible = pillVisible,
            enter = slideInVertically(tween(360)) { it }.plus(fadeIn(tween(300))),
            exit = slideOutVertically(tween(360)) { it + 110 }.plus(fadeOut(tween(300))),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = 18.dp),
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.White.copy(alpha = 0.9f))
                    .padding(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                PillButton(R.drawable.ic_arrow_left_outline, "назад", onBack)
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (isHttps) {
                        Icon(
                            ImageVector.vectorResource(R.drawable.ic_lock_keyhole_bold),
                            contentDescription = null,
                            tint = Color(0xFF38B26E),
                            modifier = Modifier.size(15.dp),
                        )
                    }
                    Text(
                        domainOf(currentUrl), color = LetaaText, fontFamily = Onest,
                        fontWeight = FontWeight.W600, fontSize = 14.5.sp,
                    )
                }
                PillButton(R.drawable.ic_refresh_outline, "обновить") { webViewRef?.reload() }
                PillButton(R.drawable.ic_share_outline, "поделиться") {
                    val send = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, currentUrl)
                    }
                    context.startActivity(Intent.createChooser(send, null))
                }
            }
        }
    }
}

@Composable
private fun PillButton(icon: Int, description: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(ImageVector.vectorResource(icon), contentDescription = description, tint = LetaaText)
    }
}

private fun domainOf(url: String): String = try {
    Uri.parse(url).host?.removePrefix("www.") ?: url
} catch (_: Exception) {
    url
}
