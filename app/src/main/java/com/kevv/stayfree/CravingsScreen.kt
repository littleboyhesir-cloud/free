package com.kevv.stayfree

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun CravingsScreen(vm: MainViewModel) {
    val habit by vm.selected.collectAsState()
    val cravings by vm.cravings.collectAsState()
    var intensity by remember { mutableFloatStateOf(5f) }
    var trigger by remember { mutableStateOf<String?>(null) }
    var note by remember { mutableStateOf("") }
    var resisted by remember { mutableStateOf(true) }
    val h = habit ?: return
    val p = presetFor(h.category)
    val fmt = remember { SimpleDateFormat("EEE d MMM, HH:mm", Locale.getDefault()) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Panel {
                Text("Log a craving", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Intensity: ${intensity.toInt()} / 10")
                Slider(value = intensity, onValueChange = { intensity = it }, valueRange = 1f..10f, steps = 8)
                Text("Trigger", fontWeight = FontWeight.SemiBold)
                ChipFlow(p.triggers, trigger) { trigger = it }
                OutlinedTextField(
                    value = note, onValueChange = { note = it }, label = { Text("Note (optional)") },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = resisted, onCheckedChange = { resisted = it })
                    Spacer(Modifier.width(8.dp))
                    Text("I resisted it")
                }
                Button(
                    onClick = {
                        vm.logCraving(intensity.toInt(), trigger ?: "Unknown", note.trim(), resisted)
                        note = ""; trigger = null; intensity = 5f; resisted = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Save") }
            }
        }
        if (cravings.isEmpty()) {
            item {
                Text("No cravings logged yet. Logging them shows you your patterns on the Insights tab.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        items(cravings, key = { it.id }) { c ->
            Panel {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("${if (c.resisted) "\uD83D\uDCAA" else "\uD83D\uDE14"}  ${c.trigger} \u2022 ${c.intensity}/10",
                            fontWeight = FontWeight.SemiBold)
                        Text(fmt.format(c.time), style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (c.note.isNotBlank()) Text(c.note, style = MaterialTheme.typography.bodySmall)
                    }
                    TextButton(onClick = { vm.deleteCraving(c) }) { Text("Delete") }
                }
            }
        }
    }
}
