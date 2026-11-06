package com.kevv.stayfree

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun SosScreen(vm: MainViewModel, onClose: () -> Unit) {
    val habit by vm.selected.collectAsState()
    val quote by vm.quote.collectAsState()
    val uri = LocalUriHandler.current
    val p = presetFor(habit?.category ?: "Custom")
    val tips = remember(p) { (p.tips + genericTips).shuffled() }
    var remaining by remember { mutableLongStateOf(600_000L) }
    var tip by remember { mutableIntStateOf(0) }
    var phase by remember { mutableStateOf("Breathe in") }
    var intensity by remember { mutableFloatStateOf(6f) }
    val scale = remember { Animatable(0.55f) }
    val primary = MaterialTheme.colorScheme.primary

    // 4-7-8 breathing: in for 4s, hold 7s, out for 8s
    LaunchedEffect(Unit) {
        vm.refreshQuote()
        while (true) {
            phase = "Breathe in"; scale.animateTo(1f, tween(4000, easing = LinearEasing))
            phase = "Hold"; delay(7000)
            phase = "Breathe out"; scale.animateTo(0.55f, tween(8000, easing = LinearEasing))
        }
    }
    LaunchedEffect(Unit) { while (remaining > 0) { delay(1000); remaining -= 1000 } }
    LaunchedEffect(Unit) { while (true) { delay(15000); tip++ } }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Urge SOS", Modifier.weight(1f), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            TextButton(onClick = onClose) { Text("Close \u2715") }
        }
        Text("Urges usually rise, peak and fade within 10\u201320 minutes. You only have to outlast this one.",
            textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Box(Modifier.size(230.dp), contentAlignment = Alignment.Center) {
            Box(Modifier.size(230.dp).scale(scale.value).background(primary.copy(alpha = 0.22f), CircleShape))
            Box(Modifier.size(130.dp).scale(scale.value).background(primary.copy(alpha = 0.55f), CircleShape))
            Text(phase, fontWeight = FontWeight.Bold)
        }

        val sec = remaining / 1000
        Text(
            if (remaining > 0) String.format(Locale.US, "%d:%02d left in your 10-minute delay", sec / 60, sec % 60)
            else "10 minutes passed. Notice how the urge has changed.",
            style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center
        )

        Panel {
            Text("Try this now", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(tips[tip % tips.size], style = MaterialTheme.typography.bodyLarge)
        }

        quote?.let { q ->
            Panel {
                Text("\u201C${q.first}\u201D", style = MaterialTheme.typography.bodyMedium)
                Text("\u2014 ${q.second}", style = MaterialTheme.typography.labelMedium, color = primary)
            }
        }

        Panel {
            Text("How strong was the urge?  ${intensity.toInt()} / 10")
            Slider(value = intensity, onValueChange = { intensity = it }, valueRange = 1f..10f, steps = 8)
            Button(
                onClick = { vm.logCraving(intensity.toInt(), "SOS", "Used urge SOS", true); onClose() },
                modifier = Modifier.fillMaxWidth()
            ) { Text("I rode it out \uD83D\uDCAA") }
        }
        TextButton(onClick = { uri.safeOpen("https://findahelpline.com") }) { Text("Need to talk to someone? Find a helpline") }
    }
}
