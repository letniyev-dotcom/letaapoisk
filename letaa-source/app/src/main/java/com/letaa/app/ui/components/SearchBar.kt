package com.letaa.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.letaa.app.ui.theme.LetaaCard
import com.letaa.app.ui.theme.LetaaMuted
import com.letaa.app.ui.theme.LetaaText
import com.letaa.app.ui.theme.Onest

/**
 * Поисковая строка со скруглёнными, но не полностью круглыми углами —
 * ключевая деталь дизайна «летаа» (RoundedCornerShape(18.dp), не pill).
 */
@Composable
fun LetaaSearchBar(
    modifier: Modifier = Modifier,
    leading: ImageVector,
    trailing: ImageVector? = null,
    placeholder: String,
    text: String = "",
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(LetaaCard)
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(leading, contentDescription = null, tint = LetaaMuted)
        Text(
            text = text.ifEmpty { placeholder },
            color = if (text.isEmpty()) LetaaMuted else LetaaText,
            fontFamily = Onest,
            fontWeight = FontWeight.W500,
            fontSize = 16.5.sp,
            maxLines = 1,
            modifier = Modifier.weight(1f),
        )
        if (trailing != null) {
            Icon(trailing, contentDescription = null, tint = LetaaMuted)
        } else {
            androidx.compose.foundation.layout.Spacer(Modifier.width(1.dp))
        }
    }
}
