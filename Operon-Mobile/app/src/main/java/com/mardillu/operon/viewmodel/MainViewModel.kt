package com.mardillu.operon.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.CompletableDeferred
import com.mardillu.operon.automation.AgentOrchestrator
import com.mardillu.operon.data.AgentAction
import com.mardillu.operon.data.ExecutionMode

class MainViewModel : ViewModel() {

    private val orchestrator = AgentOrchestrator()

    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    fun toggleAgent(goal: String, currentMode: ExecutionMode) {
        if (orchestrator.isRunning) {
            orchestrator.stop()
            _isRunning.value = false
            addLog("Agent stopped by user.")
        } else {
            if (goal.isBlank()) {
                addLog("Error: Goal cannot be empty.")
                return
            }
            _isRunning.value = true
            _logs.value = emptyList() // Clear previous logs
            orchestrator.start(
                goal = goal,
                executionMode = currentMode,
                onLog = { logMessage ->
                    addLog(logMessage)
                },
                onFinish = {
                    _isRunning.value = false
                }
            )
        }
    }

    private fun addLog(message: String) {
        viewModelScope.launch {
            val current = _logs.value.toMutableList()
            current.add(0, message) // Add to top
            _logs.value = current
        }
    }

    override fun onCleared() {
        super.onCleared()
        orchestrator.stop()
    }
}
