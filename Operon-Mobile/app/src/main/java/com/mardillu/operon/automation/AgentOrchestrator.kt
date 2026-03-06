package com.mardillu.operon.automation

import android.util.Log
import com.mardillu.operon.api.NetworkModule
import com.mardillu.operon.data.AgentStepPayload
import com.mardillu.operon.data.GoalStatus
import com.mardillu.operon.data.AgentAction
import com.mardillu.operon.data.ActionType
import com.mardillu.operon.data.ExecutionMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

class AgentOrchestrator {

    private val api = NetworkModule.agentApi
    private val scope = CoroutineScope(Dispatchers.IO)
    private var automationJob: Job? = null

    var isRunning = false
        private set

    fun start(
        goal: String,
        executionMode: ExecutionMode,
        onRequireApproval: suspend (AgentAction) -> Boolean,
        onLog: (String) -> Unit
    ) {
        if (isRunning) return
        isRunning = true
        
        val sessionId = UUID.randomUUID().toString()

        automationJob = scope.launch {
            onLog("Starting session $sessionId for goal: $goal")
            
            while (isActive && isRunning) {
                try {
                    val accessibilityService = AutopilotAccessibilityService.instance
                    val screenCaptureService = ScreenCaptureService.instance

                    if (accessibilityService == null) {
                        onLog("Error: Accessibility Service is not running/bound.")
                        stop()
                        break
                    }

                    onLog("Capturing state...")
                    val uiTree = accessibilityService.captureUiTree()
                    val screenshot = screenCaptureService?.captureScreenshotBase64()

                    if (uiTree == null && screenshot == null) {
                        onLog("Warning: Could not capture state. Retrying in 2s...")
                        delay(2000)
                        continue
                    }

                    val payload = AgentStepPayload(
                        sessionId = sessionId,
                        goal = goal,
                        screenshotBase64 = screenshot,
                        uiTree = uiTree
                    )

                    onLog("Sending payload to Agent Brain...")
                    val response = api.processStep(payload)
                    
                    onLog("Agent reasoning: ${response.reasoning} (Confidence: ${response.confidence})")
                    
                    if (response.goalStatus == GoalStatus.completed) {
                        onLog("Goal Completed successfully!")
                        stop()
                        break
                    } else if (response.goalStatus == GoalStatus.failed) {
                        onLog("Agent reported failure to achieve goal.")
                        stop()
                        break
                    }

                    val action = response.nextAction
                    
                    var shouldExecute = true
                    when (executionMode) {
                        ExecutionMode.ALWAYS_ASK -> {
                            if (action.type != ActionType.wait) {
                                onLog("Awaiting user approval for action: ${action.type}")
                                shouldExecute = onRequireApproval(action)
                            }
                        }
                        ExecutionMode.ASK_SOME_RECOMMENDED -> {
                            if (action.type == ActionType.click) {
                                onLog("Awaiting user approval for sensitive action: ${action.type}")
                                shouldExecute = onRequireApproval(action)
                            }
                        }
                        ExecutionMode.ALWAYS_EXECUTE -> {
                            // Proceed
                        }
                    }

                    if (shouldExecute) {
                        onLog("Executing action: ${action.type}")
                        accessibilityService.executeAction(action)
                        delay(3000)
                    } else {
                        onLog("Action rejected by user.")
                        stop()
                        break
                    }

                } catch (e: Exception) {
                    onLog("Error during orchestration loop: ${e.message}")
                    Log.e("AgentOrchestrator", "Loop error", e)
                    delay(3000)
                }
            }
        }
    }

    fun stop() {
        isRunning = false
        automationJob?.cancel()
    }
}
