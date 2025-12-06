package com.example.lifeguide

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar

object UsageStatsManager {

    private val _usageMapFlow = MutableStateFlow<Map<String, Int>>(emptyMap())
    val usageMapFlow: StateFlow<Map<String, Int>> = _usageMapFlow.asStateFlow()

    fun initialize(context: Context) {
        _usageMapFlow.value = getUsageMap(context)
    }

    private fun getUsageMap(context: Context): Map<String, Int> {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, -1)
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        val usageEvents = usageStatsManager.queryEvents(startTime, endTime)
        val usageMap = mutableMapOf<String, Int>()
        val event = UsageEvents.Event()

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                usageMap[event.packageName] = (usageMap[event.packageName] ?: 0) + 1
            }
        }
        return usageMap
    }
}