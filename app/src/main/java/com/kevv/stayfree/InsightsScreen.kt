package com.kevv.stayfree

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private fun hourOf(t: Long): Int = Calendar.getInstance().apply { timeInMillis = t }.get(Calendar.HOUR_OF_DAY)

@Composable
fun InsightsScreen(vm: MainViewModel) {
    val habit by vm.selected.collectAsState()
    val cravings by vm.cravings.collectAsState()
    val relapses by vm.relapses.collectAsState()
    val now by vm.now.collectAsState()
    var confirmRemove by remember { mutableStateOf(false) }
    val h = habit ?: return
    val bar = MaterialTheme.colorScheme.primary
    val dayMs = 86_400_000L
    val todayStart = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    val starts = (6 downTo 0).map { todayStart - it * dayMs }
    val counts = starts.map { s -> cravings.count { it.time >= s && it.time < s + dayMs } }
    val dayFmt = remember { SimpleDateFormat("EEE", Locale.getDefault()) }
    val dateFmt = remember { SimpleDateFormat("d MMM yyyy", Locale.getDefault()) }

    val resistedPct = if (cravings.isEmpty()) 0 else cravings.count { it.resisted } * 100 / cravings.size
    val avgIntensity = if (cravings.isEmpty()) 0.0 else cravings.map { it.intensity }.average()
    val riskyHour = cravings.groupingBy { hourOf(it.time) }.eachCount().maxByOrNull { it.value }
    val topTriggers = cravings.groupingBy { it.trigger }.eachCount().entries.sortedByDescending { it.value }.take(5)
    val longest = maxOf(h.bestStreakMs, (now - h.startTime).coerceAtLeast(0))

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Panel {
            Text("Cravings, last 7 days", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Canvas(Modifier.fillMaxWidth().height(120.dp)) {
                val n = counts.size
                val gap = 12.dp.toPx()
                val bw = (size.width - gap * (n - 1)) / n
                val mx = maxOf(1, counts.max())
                counts.forEachIndexed { i, c ->
                    val hgt = maxOf(size.height * (c / mx.toFloat()), 6f)
                    drawRoundRect(bar, Offset(i * (bw + gap), size.height - hgt), Size(bw, hgt), CornerRadius(8f, 8f))
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                starts.forEachIndexed { i, s -> Text("${dayFmt.format(s)}\n${counts[i]}", style = MaterialTheme.typography.labelSmall) }
            }
        }

        Panel {
            Text("Your patterns", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            if (cravings.isEmpty()) {
                Text("Log a few cravings to unlock insights.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Text("Total logged: ${cravings.size}")
                Text("Resisted: $resistedPct%")
                Text("Average intensity: ${String.format(Locale.US, "%.1f", avgIntensity)} / 10")
                riskyHour?.let {
                    Text("Danger time: around %02d:00\u2013%02d:00 (%d cravings)".format(it.key, (it.key + 1) % 24, it.value))
                }
                if (topTriggers.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Text("Top triggers", fontWeight = FontWeight.SemiBold)
                    val top = topTriggers.first().value.toFloat()
                    topTriggers.forEach { (name, n) ->
                        Row(Modifier.padding(top = 6.dp)) {
                            Text(name, Modifier.weight(1f)); Text("$n")
                        }
                        LinearProgressIndicator(progress = { n / top }, modifier = Modifier.fillMaxWidth())
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Tip: plan a replacement action for \"${topTriggers.first().key}\" before it happens.",
                        color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Panel {
            Text("Streaks and slips", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text("Longest streak: ${fmtShort(longest)}")
            Text("Slips: ${relapses.size}")
            relapses.take(8).forEach { r ->
                Text("\u2022 ${dateFmt.format(r.time)}: after ${fmtShort(r.streakMs)} (${r.trigger})",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        OutlinedButton(onClick = { confirmRemove = true }, modifier = Modifier.fillMaxWidth()) {
            Text("Remove this tracker")
        }
    }

    if (confirmRemove) {
        AlertDialog(
            onDismissRequest = { confirmRemove = false },
            title = { Text("Remove ${h.name}?") },
            text = { Text("This hides the tracker from the app.") },
            confirmButton = { Button(onClick = { vm.archive(); confirmRemove = false }) { Text("Remove") } },
            dismissButton = { TextButton(onClick = { confirmRemove = false }) { Text("Cancel") } }
        )
    }
}
