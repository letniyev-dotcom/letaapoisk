package com.letaa.app.data

import android.content.Context

/**
 * Реальная (не фейковая) история поисковых запросов — SharedPreferences,
 * без сети и без сторонних SDK. «Сжечь всё» стирает этот файл целиком.
 */
class HistoryStore(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("letaa_history", Context.MODE_PRIVATE)

    companion object {
        private const val KEY = "queries"
        private const val SEP = "\u0001"
        private const val MAX_ITEMS = 24
    }

    fun all(): List<String> {
        val raw = prefs.getString(KEY, null) ?: return emptyList()
        if (raw.isEmpty()) return emptyList()
        return raw.split(SEP).filter { it.isNotBlank() }
    }

    fun add(query: String) {
        val q = query.trim()
        if (q.isEmpty()) return
        val current = all().toMutableList()
        current.remove(q)
        current.add(0, q)
        while (current.size > MAX_ITEMS) current.removeAt(current.lastIndex)
        prefs.edit().putString(KEY, current.joinToString(SEP)).apply()
    }

    /** Сжечь всё — прямо всё-всё-всё. */
    fun clear() {
        prefs.edit().clear().apply()
    }
}
