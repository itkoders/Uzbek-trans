package uz.itkoders.translate

import android.accessibilityservice.AccessibilityService
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class ScreenTextService : AccessibilityService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val handler = Handler(Looper.getMainLooper())
    private var panel: TextView? = null
    private var lastText = ""
    private var lastTranslated = ""
    private var busy = false

    override fun onServiceConnected() {
        super.onServiceConnected()
        showPanel("ITKODERS Translate faol. Matn aniqlanmoqda…")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || !Settings.canDrawOverlays(this) || busy) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) return

        handler.removeCallbacksAndMessages(null)
        handler.postDelayed({
            val root = rootInActiveWindow ?: return@postDelayed
            val collected = StringBuilder()
            collectText(root, collected, 0)
            val text = collected.toString().trim().take(450)
            if (text.length < 3 || text == lastText) return@postDelayed
            lastText = text
            busy = true
            showPanel("Tarjima qilinmoqda…")
            scope.launch {
                try {
                    val translated = TranslationClient.toUzbek(text)
                    lastTranslated = translated
                    showPanel(translated)
                } catch (e: Exception) {
                    showPanel("Tarjima mavjud emas. Internet yoki tarjima serverini tekshiring.")
                } finally { busy = false }
            }
        }, 900)
    }

    private fun collectText(node: AccessibilityNodeInfo?, out: StringBuilder, depth: Int) {
        if (node == null || depth > 12 || out.length > 450) return
        val value = node.text?.toString()?.trim().orEmpty()
        if (value.length >= 2 && !value.contains("ITKODERS Translate")) {
            if (out.isNotEmpty()) out.append(" · ")
            out.append(value)
        }
        for (i in 0 until node.childCount) collectText(node.getChild(i), out, depth + 1)
    }

    private fun showPanel(message: String) {
        if (!Settings.canDrawOverlays(this)) return
        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        if (panel == null) {
            panel = TextView(this).apply {
                setTextColor(0xFFFFFFFF.toInt())
                setBackgroundColor(0xEE102238.toInt())
                textSize = 14f
                setPadding(18, 14, 18, 14)
                elevation = 12f
                text = message
            }
            val type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP
                y = 52
                x = 12
                width = resources.displayMetrics.widthPixels - 24
            }
            try { wm.addView(panel, params) } catch (_: Exception) { panel = null }
        } else {
            panel?.text = message
        }
    }

    override fun onInterrupt() { showPanel("Tarjima vaqtincha to‘xtatildi.") }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        scope.cancel()
        try { panel?.let { (getSystemService(WINDOW_SERVICE) as WindowManager).removeView(it) } } catch (_: Exception) {}
        panel = null
    }
}
