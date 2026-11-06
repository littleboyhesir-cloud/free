package com.kevv.stayfree

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDb.get(app).dao()
    private val prefs = app.getSharedPreferences("cache", Context.MODE_PRIVATE)

    /** null = still loading from the database */
    val habits: StateFlow<List<Habit>?> = dao.habits()
        .map { it as List<Habit>? }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val selectedId = MutableStateFlow<Long?>(prefs.getLong("selected", -1L).takeIf { it >= 0 })

    val selected: StateFlow<Habit?> = combine(habits, selectedId) { list, id ->
        list?.let { l -> l.firstOrNull { it.id == id } ?: l.firstOrNull() }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val cravings: StateFlow<List<Craving>> = selected.map { it?.id }.distinctUntilChanged()
        .flatMapLatest { id -> if (id == null) flowOf(emptyList<Craving>()) else dao.cravings(id) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val relapses: StateFlow<List<Relapse>> = selected.map { it?.id }.distinctUntilChanged()
        .flatMapLatest { id -> if (id == null) flowOf(emptyList<Relapse>()) else dao.relapses(id) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val now: StateFlow<Long> = flow {
        while (true) { emit(System.currentTimeMillis()); delay(1000) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), System.currentTimeMillis())

    // ---- Internet data: quotes ----
    val quote = MutableStateFlow<Pair<String, String>?>(null)
    val quoteLoading = MutableStateFlow(false)
    val quoteOnline = MutableStateFlow(true)

    fun refreshQuote() {
        viewModelScope.launch {
            quoteLoading.value = true
            val q = Net.quote()
            quoteOnline.value = q != null
            quote.value = q ?: fallbackQuotes.random()
            quoteLoading.value = false
        }
    }

    // ---- Internet data: Wikipedia articles (cached for offline use) ----
    val wiki = MutableStateFlow<Map<String, WikiInfo?>>(emptyMap())
    val wikiLoading = MutableStateFlow(false)

    fun loadWiki(titles: List<String>, force: Boolean = false) {
        viewModelScope.launch {
            wikiLoading.value = true
            for (t in titles) {
                if (!force && wiki.value[t] != null) continue
                val fresh = Net.wiki(t)
                val info = fresh?.also { writeCache(t, it) } ?: readCache(t)
                wiki.value = wiki.value + (t to info)
            }
            wikiLoading.value = false
        }
    }

    private fun writeCache(t: String, i: WikiInfo) {
        val o = JSONObject().put("title", i.title).put("extract", i.extract).put("url", i.url ?: "")
        prefs.edit().putString("wiki_$t", o.toString()).apply()
    }

    private fun readCache(t: String): WikiInfo? = runCatching {
        val o = JSONObject(prefs.getString("wiki_$t", null) ?: return null)
        WikiInfo(o.getString("title"), o.getString("extract"), o.optString("url").ifBlank { null })
    }.getOrNull()

    init { refreshQuote() }

    // ---- Actions ----
    fun select(id: Long) {
        selectedId.value = id
        prefs.edit().putLong("selected", id).apply()
    }

    fun addHabit(name: String, category: String, startTime: Long, cost: Double, perDay: Double, unit: String) {
        viewModelScope.launch {
            val id = dao.insertHabit(Habit(name = name, category = category, startTime = startTime,
                costPerUnit = cost, unitsPerDay = perDay, unitName = unit))
            select(id)
        }
    }

    fun logCraving(intensity: Int, trigger: String, note: String, resisted: Boolean) {
        val h = selected.value ?: return
        viewModelScope.launch {
            dao.insertCraving(Craving(habitId = h.id, time = System.currentTimeMillis(),
                intensity = intensity, trigger = trigger, note = note, resisted = resisted))
        }
    }

    fun deleteCraving(c: Craving) { viewModelScope.launch { dao.deleteCraving(c) } }

    fun slip(trigger: String) {
        val h = selected.value ?: return
        viewModelScope.launch {
            val t = System.currentTimeMillis()
            val streak = (t - h.startTime).coerceAtLeast(0)
            dao.insertRelapse(Relapse(habitId = h.id, time = t, streakMs = streak, trigger = trigger))
            dao.updateHabit(h.copy(startTime = t, bestStreakMs = maxOf(h.bestStreakMs, streak)))
        }
    }

    fun archive() {
        val h = selected.value ?: return
        viewModelScope.launch { dao.archiveHabit(h.id) }
    }
}
