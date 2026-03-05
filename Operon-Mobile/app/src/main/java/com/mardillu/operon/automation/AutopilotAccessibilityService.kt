package com.mardillu.operon.automation

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.mardillu.operon.data.ActionType
import com.mardillu.operon.data.AgentAction
import com.mardillu.operon.data.UiNodeInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AutopilotAccessibilityService : AccessibilityService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    companion object {
        var instance: AutopilotAccessibilityService? = null
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // We observe events here if necessary, but we mostly pull the active window tree on demand
    }

    override fun onInterrupt() {}

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        instance = null
        job.cancel()
        return super.onUnbind(intent)
    }

    /**
     * Extracts the UI Tree from the root of the active window.
     */
    fun captureUiTree(): UiNodeInfo? {
        val rootNode = rootInActiveWindow ?: return null
        return traverseNode(rootNode)
    }

    private fun traverseNode(node: AccessibilityNodeInfo): UiNodeInfo {
        val bounds = Rect()
        node.getBoundsInScreen(bounds)

        val children = mutableListOf<UiNodeInfo>()
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                children.add(traverseNode(child))
            }
        }

        return UiNodeInfo(
            text = node.text?.toString(),
            contentDescription = node.contentDescription?.toString(),
            className = node.className?.toString(),
            bounds = listOf(bounds.left, bounds.top, bounds.right, bounds.bottom),
            children = children
        )
    }

    /**
     * Executes the action predicted by the Agent Brain.
     */
    fun executeAction(action: AgentAction) {
        when (action.type) {
            ActionType.click -> {
                action.target?.bounds?.let { boundsArray ->
                    if (boundsArray.size == 4) {
                        val centerX = (boundsArray[0] + boundsArray[2]) / 2f
                        val centerY = (boundsArray[1] + boundsArray[3]) / 2f
                        dispatchClick(centerX, centerY)
                    }
                }
            }
            ActionType.back -> {
                performGlobalAction(GLOBAL_ACTION_BACK)
            }
            ActionType.scroll -> {
                // Simplified scroll: Swipe UP to scroll down
                val metrics = resources.displayMetrics
                val startX = metrics.widthPixels / 2f
                val startY = metrics.heightPixels * 0.8f
                val endY = metrics.heightPixels * 0.2f
                dispatchSwipe(startX, startY, startX, endY)
            }
            ActionType.wait -> {
                // Do nothing
            }
            ActionType.input_text -> {
               // To perform actual text input reliably, we would match the text/bounds to the node and call ACTION_SET_TEXT
               // For simplicity in this demo, it's omitted or requires finding the AccessibilityNodeInfo again to inject text
            }
        }
    }

    private fun dispatchClick(x: Float, y: Float) {
        val path = Path().apply { moveTo(x, y) }
        val stroke = GestureDescription.StrokeDescription(path, 0, 100)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        dispatchGesture(gesture, null, null)
    }

    private fun dispatchSwipe(startX: Float, startY: Float, endX: Float, endY: Float) {
        val path = Path().apply {
            moveTo(startX, startY)
            lineTo(endX, endY)
        }
        val stroke = GestureDescription.StrokeDescription(path, 0, 500)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        dispatchGesture(gesture, null, null)
    }
}
