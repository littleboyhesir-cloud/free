package com.kevv.stayfree

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
private fun Stat(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier, shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(12.dp)) {
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun HomeScreen(vm: MainViewModel, onSos: () -> Unit) {
    val habit by vm.selected.collectAsState()
    val now by vm.now.collectAsState()
    val quote by vm.quote.collectAsState()
    val quoteLoading by vm.quoteLoading.collectAsState()
    val online by vm.quoteOnline.collectAsState()
    var showSlip by remember { mutableStateOf(false) }
    var trig by remember { mutableStateOf<String?>(null) }
    val h = habit ?: return

    val p = presetFor(h.category)
    val elapsed = (now - h.startTime).coerceAtLeast(0)
    val days = elapsed / 86_400_000.0
    val ms = milestonesFor(h.category)
    val next = ms.firstOrNull { it.hours * 3_600_000 > elapsed }
    val prevMs = ms.lastOrNull { it.hours * 3_600_000 <= elapsed }?.let { (it.hours * 3_600_000).toLong() } ?: 0L
    val progress = next?.let { (elapsed - prevMs).toFloat() / ((it.hours * 3_600_000).toLong() - prevMs) } ?: 1f

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("${p.emoji} ${h.name}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Ring(progress, 250.dp) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("FREE FOR", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(fmtClock(elapsed), fontSize = 28.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Text(
                    next?.let { "Next: ${it.title}" } ?: "All milestones reached \uD83C\uDFC6",
                    style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Stat("saved", money(days * h.unitsPerDay * h.costPerUnit), Modifier.weight(1f))
            Stat("${h.unitName} avoided", fmtNum(Math.round(days * h.unitsPerDay * 10) / 10.0), Modifier.weight(1f))
            Stat("best streak", fmtShort(maxOf(h.bestStreakMs, elapsed)), Modifier.weight(1f))
        }

        Button(
            onClick = onSos, modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = androidx.compose.ui.graphics.Color(0xFF3B2A00))
        ) { Text("\uD83C\uDD98  I'm having a craving", fontWeight = FontWeight.Bold) }

        Panel {
            Text("Today's motivation", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            val q = quote
            if (q == null) LinearProgressIndicator(Modifier.fillMaxWidth())
            else {
                Text("\u201C${q.first}\u201D", style = MaterialTheme.typography.bodyLarge)
                Text("\u2014 ${q.second}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { vm.refreshQuote() }, enabled = !quoteLoading) { Text("New quote") }
                if (!online) Text("offline mode", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Panel {
            Text("Recovery timeline", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            ms.forEach { m ->
                val done = m.hours * 3_600_000 <= elapsed
                Row(Modifier.padding(vertical = 5.dp), verticalAlignment = Alignment.Top) {
                    Text(if (done) "\u2705" else "\u23F3", modifier = Modifier.width(30.dp))
                    Column {
                        Text(m.title, fontWeight = FontWeight.SemiBold,
                            color = if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                        Text(m.desc, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        OutlinedButton(onClick = { trig = null; showSlip = true }, modifier = Modifier.fillMaxWidth()) {
            Text("I slipped")
        }
    }

    if (showSlip) {
        AlertDialog(
            onDismissRequest = { showSlip = false },
            title = { Text("Log a slip") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("A slip isn't the end. Your best streak is saved and you can start again right now.")
                    Text("What triggered it?", fontWeight = FontWeight.SemiBold)
                    ChipFlow(p.triggers, trig) { trig = it }
                }
            },
            confirmButton = {
                Button(onClick = { vm.slip(trig ?: "Unknown"); showSlip = false }) { Text("Reset streak") }
            },
            dismissButton = { TextButton(onClick = { showSlip = false }) { Text("Cancel") } }
        )
    }
}
