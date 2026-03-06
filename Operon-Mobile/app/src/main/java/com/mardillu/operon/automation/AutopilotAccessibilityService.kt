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
            ActionType.home -> {
                performGlobalAction(GLOBAL_ACTION_HOME)
            }
            ActionType.recent_apps -> {
                performGlobalAction(GLOBAL_ACTION_RECENTS)
            }
            ActionType.scroll -> {
                val rootNode = rootInActiveWindow
                var scrolled = false

                if (rootNode != null) {
                    // Try to find the specific target node if bounds were provided
                    val boundsArray = action.target?.bounds
                    if (boundsArray != null && boundsArray.size == 4) {
                        val targetNode = findNodeByBounds(rootNode, boundsArray)
                        if (targetNode != null && (targetNode.isScrollable || targetNode.actionList.contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD))) {
                            scrolled = targetNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                            if (!scrolled) {
                                // sometimes the parent is the scrollable one
                                scrolled = targetNode.parent?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) == true
                            }
                        }
                    }

                    // Fallback 1: Find the first scrollable node in the entire tree
                    if (!scrolled) {
                        val scrollableNode = findFirstScrollableNode(rootNode)
                        if (scrollableNode != null) {
                            scrolled = scrollableNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                        }
                    }
                }

                // Fallback 2: The generic physical swipe gesture if native scroll failed
                if (!scrolled) {
                    val metrics = resources.displayMetrics
                    val startX = metrics.widthPixels / 2f
                    val startY = metrics.heightPixels * 0.8f
                    val endY = metrics.heightPixels * 0.2f
                    dispatchSwipe(startX, startY, startX, endY)
                }
            }
            ActionType.wait -> {
                // Do nothing
            }
            ActionType.input_text -> {
                val boundsArray = action.target?.bounds
                val textToInput = action.inputText

                if (boundsArray != null && boundsArray.size == 4 && textToInput != null) {
                    val rootNode = rootInActiveWindow
                    if (rootNode != null) {
                        val centerX = (boundsArray[0] + boundsArray[2]) / 2
                        val centerY = (boundsArray[1] + boundsArray[3]) / 2

                        // Robust method 1: Find an editable node that contains the center point
                        var editableNode = findEditableNodeAtCoordinate(rootNode, centerX, centerY)
                        
                        // Fallback method 2: Find exact node by bounds
                        if (editableNode == null) {
                            val targetNode = findNodeByBounds(rootNode, boundsArray)
                            if (targetNode != null) {
                                editableNode = findEditableNode(targetNode) ?: targetNode
                            }
                        }

                        if (editableNode != null && (editableNode.isEditable || editableNode.actionList.contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_SET_TEXT))) {
                            val arguments = android.os.Bundle()
                            arguments.putCharSequence(
                                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                                textToInput
                            )
                            val success = editableNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
                            if (!success) {
                                // Ultimate Fallback: Just click the field to focus it
                                dispatchClick(centerX.toFloat(), centerY.toFloat())
                            }
                        } else {
                            // Ultimate Fallback: Just click the field to focus it
                            dispatchClick(centerX.toFloat(), centerY.toFloat())
                        }
                    }
                }
            }
        }
    }

    private fun findNodeByBounds(node: AccessibilityNodeInfo, targetBounds: List<Int>): AccessibilityNodeInfo? {
        val nodeBounds = Rect()
        node.getBoundsInScreen(nodeBounds)

        // Allow a tiny margin of error (e.g., 5px) for coordinate matching
        if (Math.abs(nodeBounds.left - targetBounds[0]) < 5 &&
            Math.abs(nodeBounds.top - targetBounds[1]) < 5 &&
            Math.abs(nodeBounds.right - targetBounds[2]) < 5 &&
            Math.abs(nodeBounds.bottom - targetBounds[3]) < 5) {
            return node
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                val found = findNodeByBounds(child, targetBounds)
                if (found != null) return found
            }
        }
        return null
    }

    private fun findEditableNodeAtCoordinate(node: AccessibilityNodeInfo, x: Int, y: Int): AccessibilityNodeInfo? {
        val nodeBounds = Rect()
        node.getBoundsInScreen(nodeBounds)

        if (!nodeBounds.contains(x, y)) {
            return null
        }

        // It contains the point. Does it have an editable child that contains the point?
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                val found = findEditableNodeAtCoordinate(child, x, y)
                if (found != null) return found
            }
        }

        // If no child is editable at this coordinate, check if this node itself is editable
        if (node.isEditable || node.actionList.contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_SET_TEXT)) {
            return node
        }
        
        return null
    }

    private fun findEditableNode(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isEditable || node.actionList.contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_SET_TEXT)) {
            return node
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                val found = findEditableNode(child)
                if (found != null) return found
            }
        }
        return null
    }

    private fun findFirstScrollableNode(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isScrollable || node.actionList.contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD)) {
            return node
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                val found = findFirstScrollableNode(child)
                if (found != null) return found
            }
        }
        return null
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
