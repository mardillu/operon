package com.mardillu.operon.ui.components

import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import com.mardillu.operon.R
import kotlinx.coroutines.CompletableDeferred
import com.mardillu.operon.data.AgentAction

object GlobalOverlayManager {
    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var currentDeferred: CompletableDeferred<Boolean>? = null

    fun showApprovalDialog(context: Context, action: AgentAction): CompletableDeferred<Boolean> {
        val deferred = CompletableDeferred<Boolean>()
        
        Handler(Looper.getMainLooper()).post {
            // If a dialog is already showing, resolve it as rejected to prevent overlap
            currentDeferred?.complete(false)
            removeOverlayInternal()
            
            currentDeferred = deferred
            windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

            val inflater = LayoutInflater.from(context)
            overlayView = inflater.inflate(R.layout.overlay_approval_dialog, null)

            val messageText = overlayView?.findViewById<TextView>(R.id.text_message)
            val btnApprove = overlayView?.findViewById<Button>(R.id.btn_approve)
            val btnReject = overlayView?.findViewById<Button>(R.id.btn_reject)

            messageText?.text = "The agent wants to execute a potentially risky action:\n\n${action.type}\n\nDo you approve?"

            btnApprove?.setOnClickListener {
                deferred.complete(true)
                removeOverlay()
            }

            btnReject?.setOnClickListener {
                deferred.complete(false)
                removeOverlay()
            }

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
            params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            params.y = 100 // Offset slightly from the very top

            try {
                windowManager?.addView(overlayView, params)
            } catch (e: Exception) {
                e.printStackTrace()
                // If we lack permission or crash, auto-reject to prevent freezing
                deferred.complete(false)
            }
        }

        return deferred
    }

    private fun removeOverlay() {
        Handler(Looper.getMainLooper()).post {
            removeOverlayInternal()
        }
    }

    private fun removeOverlayInternal() {
        try {
            overlayView?.let { view ->
                windowManager?.removeView(view)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            overlayView = null
            windowManager = null
            currentDeferred = null
        }
    }
}
