package com.kevv.stayfree

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun LearnScreen(vm: MainViewModel) {
    val habit by vm.selected.collectAsState()
    val wiki by vm.wiki.collectAsState()
    val loading by vm.wikiLoading.collectAsState()
    val uri = LocalUriHandler.current
    val h = habit ?: return
    val p = presetFor(h.category)

    LaunchedEffect(h.id) { vm.loadWiki(p.wiki) }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        p.warning?.let {
            Panel { Text("\u26A0\uFE0F $it", color = MaterialTheme.colorScheme.secondary) }
        }

        Text("Understand it (from Wikipedia)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (p.wiki.isEmpty()) {
            Text("No articles for custom trackers. The coping toolkit below still applies.",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        p.wiki.forEach { t ->
            val info = wiki[t]
            Panel {
                Text(info?.title ?: t, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                when {
                    info != null -> {
                        Text(info.extract, style = MaterialTheme.typography.bodyMedium)
                        info.url?.let { u -> TextButton(onClick = { uri.safeOpen(u) }) { Text("Read more") } }
                    }
                    loading -> LinearProgressIndicator(Modifier.fillMaxWidth())
                    else -> {
                        Text("Couldn't load this article. Check your connection.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        TextButton(onClick = { vm.loadWiki(p.wiki, force = true) }) { Text("Retry") }
                    }
                }
            }
        }

        Panel {
            Text("Coping toolkit", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            (p.tips + genericTips).forEach {
                Text("\u2022 $it", Modifier.padding(vertical = 3.dp), style = MaterialTheme.typography.bodyMedium)
            }
        }

        Panel {
            Text("Need to talk to someone?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text("Free, confidential help lines (US numbers shown). Outside the US, use the website to find a line in your country.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton(onClick = { uri.safeOpen("tel:18006624357") }) { Text("SAMHSA Helpline: 1-800-662-4357") }
            TextButton(onClick = { uri.safeOpen("tel:988") }) { Text("Crisis line: call or text 988") }
            TextButton(onClick = { uri.safeOpen("https://findahelpline.com") }) { Text("findahelpline.com (worldwide)") }
        }

        Text("StayFree is a support tool, not medical advice.",
            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
