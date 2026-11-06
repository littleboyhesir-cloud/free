package com.kevv.stayfree

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.util.Locale

private val Scheme = darkColorScheme(
    primary = Color(0xFF2DD4BF),
    onPrimary = Color(0xFF003731),
    primaryContainer = Color(0xFF134E4A),
    onPrimaryContainer = Color(0xFFCCFBF1),
    secondary = Color(0xFFFBBF24),
    background = Color(0xFF0B1B1E),
    onBackground = Color(0xFFE6F4F1),
    surface = Color(0xFF12292D),
    onSurface = Color(0xFFE6F4F1),
    surfaceVariant = Color(0xFF1E3E44),
    onSurfaceVariant = Color(0xFFA9C7C2),
    error = Color(0xFFF87171)
)

@Composable
fun StayFreeTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, content = content)
}

@Composable
fun Panel(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) { Column(Modifier.padding(16.dp), content = content) }
}

@Composable
fun Ring(progress: Float, diameter: Dp, content: @Composable () -> Unit) {
    val track = MaterialTheme.colorScheme.surfaceVariant
    val color = MaterialTheme.colorScheme.primary
    Box(Modifier.size(diameter), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val sw = 14.dp.toPx()
            val tl = Offset(sw / 2, sw / 2)
            val sz = Size(this.size.width - sw, this.size.height - sw)
            drawArc(track, -90f, 360f, false, topLeft = tl, size = sz, style = Stroke(sw, cap = StrokeCap.Round))
            drawArc(color, -90f, 360f * progress.coerceIn(0f, 1f), false, topLeft = tl, size = sz,
                style = Stroke(sw, cap = StrokeCap.Round))
        }
        content()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChipFlow(options: List<String>, selected: String?, onSelect: (String) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        options.forEach { o ->
            FilterChip(selected = o == selected, onClick = { onSelect(o) }, label = { Text(o) })
        }
    }
}

fun UriHandler.safeOpen(uri: String) { runCatching { openUri(uri) } }

fun fmtClock(ms: Long): String {
    val s = ms / 1000
    val d = s / 86400
    val hms = String.format(Locale.US, "%02d:%02d:%02d", (s % 86400) / 3600, (s % 3600) / 60, s % 60)
    return if (d > 0) "${d}d $hms" else hms
}

fun fmtShort(ms: Long): String {
    val m = ms / 60000
    val h = m / 60
    val d = h / 24
    return when {
        d >= 1 -> "${d}d ${h % 24}h"
        h >= 1 -> "${h}h ${m % 60}m"
        else -> "${m}m"
    }
}

private val currencySymbol: String by lazy {
    runCatching { java.util.Currency.getInstance(Locale.getDefault()).symbol }.getOrDefault("$")
}

fun money(v: Double): String = currencySymbol + String.format(Locale.US, "%,.2f", v)

fun fmtNum(d: Double): String = if (d % 1.0 == 0.0) d.toInt().toString() else d.toString()
