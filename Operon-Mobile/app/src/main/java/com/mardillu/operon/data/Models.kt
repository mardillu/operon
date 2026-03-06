package com.mardillu.operon.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AgentStepPayload(
    val sessionId: String,
    val goal: String,
    val screenshotBase64: String?,
    val uiTree: UiNodeInfo?
)

@JsonClass(generateAdapter = true)
data class UiNodeInfo(
    val type: String = "AccessibilityNodeInfo",
    val text: String?,
    val contentDescription: String?,
    val className: String?,
    val bounds: List<Int>?,
    val children: List<UiNodeInfo>
)

@JsonClass(generateAdapter = true)
data class StructuredActionResponse(
    val goalStatus: GoalStatus,
    val reasoning: String,
    val nextAction: AgentAction,
    val confidence: Double
)

enum class GoalStatus {
    in_progress, completed, failed
}

@JsonClass(generateAdapter = true)
data class AgentAction(
    val type: ActionType,
    val target: ActionTarget?,
    val inputText: String?
)

enum class ActionType {
    click, scroll, input_text, back, home, recent_apps, wait
}

@JsonClass(generateAdapter = true)
data class ActionTarget(
    val text: String?,
    val contentDescription: String?,
    val bounds: List<Int>?
)
