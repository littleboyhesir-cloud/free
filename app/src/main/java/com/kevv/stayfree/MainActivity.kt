package com.kevv.stayfree

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { StayFreeTheme { App() } }
    }
}

@Composable
fun App(vm: MainViewModel = viewModel()) {
    val habits by vm.habits.collectAsState()
    val selected by vm.selected.collectAsState()
    var adding by remember { mutableStateOf(false) }
    var sos by remember { mutableStateOf(false) }
    val list = habits

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        when {
            list == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            adding || list.isEmpty() -> AddHabitScreen(vm, canCancel = list.isNotEmpty(), onDone = { adding = false })
            sos -> SosScreen(vm, onClose = { sos = false })
            else -> MainShell(vm, list, selected?.id, onAdd = { adding = true }, onSos = { sos = true })
        }
    }
}

@Composable
fun MainShell(vm: MainViewModel, habits: List<Habit>, selectedId: Long?, onAdd: () -> Unit, onSos: () -> Unit) {
    var tab by remember { mutableIntStateOf(0) }
    val tabs = listOf("\uD83C\uDFE0" to "Home", "\uD83D\uDCDD" to "Cravings", "\uD83D\uDCCA" to "Insights", "\uD83D\uDCDA" to "Learn")
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background)
                    .horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                habits.forEach { h ->
                    FilterChip(
                        selected = h.id == selectedId,
                        onClick = { vm.select(h.id) },
                        label = { Text("${presetFor(h.category).emoji} ${h.name}") }
                    )
                }
                AssistChip(onClick = onAdd, label = { Text("+ Add") })
            }
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                tabs.forEachIndexed { i, (emoji, label) ->
                    NavigationBarItem(selected = tab == i, onClick = { tab = i },
                        icon = { Text(emoji) }, label = { Text(label) })
                }
            }
        }
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {
            when (tab) {
                0 -> HomeScreen(vm, onSos)
                1 -> CravingsScreen(vm)
                2 -> InsightsScreen(vm)
                else -> LearnScreen(vm)
            }
        }
    }
}

@Composable
private fun NumField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value, onValueChange = onChange, label = { Text(label) }, singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )
}

@Composable
fun AddHabitScreen(vm: MainViewModel, canCancel: Boolean, onDone: () -> Unit) {
    var preset by remember { mutableStateOf(presets.first()) }
    var name by remember(preset) { mutableStateOf(if (preset.name == "Custom") "" else preset.name) }
    var unit by remember(preset) { mutableStateOf(preset.unitName) }
    var perDay by remember(preset) { mutableStateOf(fmtNum(preset.units)) }
    var cost by remember(preset) { mutableStateOf(fmtNum(preset.cost)) }
    var daysClean by remember { mutableStateOf("0") }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Start your journey", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("What do you want to take control of?", color = MaterialTheme.colorScheme.onSurfaceVariant)
        ChipFlow(presets.map { "${it.emoji} ${it.name}" }, "${preset.emoji} ${preset.name}") { label ->
            presets.firstOrNull { "${it.emoji} ${it.name}" == label }?.let { preset = it }
        }
        preset.warning?.let {
            Panel { Text("\u26A0\uFE0F $it", color = MaterialTheme.colorScheme.secondary) }
        }
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") },
            singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Unit (cigarettes, drinks, hours...)") },
            singleLine = true, modifier = Modifier.fillMaxWidth())
        NumField("How many $unit per day (before quitting)?", perDay) { perDay = it }
        NumField("Cost per unit (0 if none)", cost) { cost = it }
        NumField("Days already clean (0 = starting now)", daysClean) { daysClean = it }
        Button(
            enabled = name.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                val d = daysClean.replace(',', '.').toDoubleOrNull() ?: 0.0
                vm.addHabit(
                    name.trim(), preset.name,
                    System.currentTimeMillis() - (d * 86_400_000).toLong(),
                    cost.replace(',', '.').toDoubleOrNull() ?: 0.0,
                    perDay.replace(',', '.').toDoubleOrNull() ?: 0.0,
                    unit.ifBlank { "times" }
                )
                onDone()
            }
        ) { Text("Start tracking") }
        if (canCancel) TextButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) { Text("Cancel") }
        Text(
            "StayFree is a support tool, not medical advice. If you are in crisis or worried about withdrawal, contact a doctor or a local helpline.",
            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
