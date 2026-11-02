package uz.itkoders.translate

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.accessibility.AccessibilityManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var status: TextView
    private lateinit var translateInput: EditText
    private lateinit var result: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(7, 17, 31)
        window.navigationBarColor = android.graphics.Color.rgb(7, 17, 31)
        buildUi()
    }

    override fun onResume() {
        super.onResume()
        if (::status.isInitialized) refreshStatus()
    }

    private fun buildUi() {
        val bg = android.graphics.Color.rgb(7, 17, 31)
        val cyan = android.graphics.Color.rgb(0, 200, 215)
        val white = android.graphics.Color.WHITE
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 28, 22, 24)
            setBackgroundColor(bg)
        }
        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val brand = TextView(this).apply {
            text = "ITKODERS"
            textSize = 13f
            setTextColor(cyan)
            letterSpacing = .22f
        }
        val title = TextView(this).apply {
            text = "Translate"
            textSize = 32f
            setTextColor(white)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        val subtitle = TextView(this).apply {
            text = "Har qanday tildan o‘zbek tiliga"
            textSize = 15f
            setTextColor(0xFFB7C5D5.toInt())
        }
        status = TextView(this).apply {
            textSize = 14f
            setTextColor(0xFFFFD166.toInt())
            setPadding(0, 22, 0, 12)
        }
        content.addView(brand)
        content.addView(title)
        content.addView(subtitle)
        content.addView(status)

        content.addView(actionButton("1. Accessibility xizmatini yoqish", cyan) {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        })
        content.addView(actionButton("2. Boshqa ilovalar ustida ko‘rsatishga ruxsat", cyan) {
            if (!Settings.canDrawOverlays(this)) {
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            }
        })
        content.addView(actionButton("Holatni yangilash", 0xFF20354B.toInt()) { refreshStatus() })

        val section = TextView(this).apply {
            text = "Matnni qo‘lda tarjima qilish"
            textSize = 18f
            setTextColor(white)
            setPadding(0, 24, 0, 8)
        }
        translateInput = EditText(this).apply {
            hint = "Tarjima qilinadigan matn..."
            setHintTextColor(0xFF8293A7.toInt())
            setTextColor(white)
            setBackgroundColor(0xFF142338.toInt())
            minLines = 3
            gravity = Gravity.TOP
            setPadding(14, 12, 14, 12)
        }
        result = TextView(this).apply {
            text = "Tarjima shu yerda ko‘rinadi."
            textSize = 16f
            setTextColor(0xFFBFEFF2.toInt())
            setPadding(0, 16, 0, 16)
        }
        content.addView(section)
        content.addView(translateInput)
        content.addView(actionButton("O‘zbekchaga tarjima qilish", cyan) { translateManual() })
        content.addView(result)
        val note = TextView(this).apply {
            text = "Eslatma: avtomatik tarjima faqat Android accessibility orqali matni ochiq berilgan ilovalarda ishlaydi. Himoyalangan ekranlar, ayrim o‘yinlar va rasm ko‘rinishidagi matnlar qo‘llab-quvvatlanmasligi mumkin. Matn tarjima serveriga yuboriladi."
            textSize = 12f
            setTextColor(0xFF8293A7.toInt())
            setPadding(0, 18, 0, 0)
        }
        content.addView(note)
        scroll.addView(content)
        root.addView(scroll)
        setContentView(root)
        refreshStatus()
    }

    private fun actionButton(label: String, color: Int, action: () -> Unit): Button =
        Button(this).apply {
            text = label
            isAllCaps = false
            setTextColor(if (color == 0xFF20354B.toInt()) android.graphics.Color.WHITE else 0xFF06131F.toInt())
            setBackgroundTintList(android.content.res.ColorStateList.valueOf(color))
            setOnClickListener { action() }
            val lp = LinearLayout.LayoutParams(-1, -2)
            lp.topMargin = 10
            layoutParams = lp
        }

    private fun refreshStatus() {
        val manager = getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabled = manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            .any { it.resolveInfo.serviceInfo.packageName == packageName }
        val overlay = Settings.canDrawOverlays(this)
        status.text = "Avtomatik tarjima: ${if (enabled && overlay) "tayyor" else "ruxsatlar kerak"}\nAccessibility: ${if (enabled) "yoqilgan" else "o‘chiq"} · Overlay: ${if (overlay) "ruxsat berilgan" else "ruxsat berilmagan"}"
        status.setTextColor(if (enabled && overlay) 0xFF65E6A5.toInt() else 0xFFFFD166.toInt())
    }

    private fun translateManual() {
        val source = translateInput.text.toString().trim()
        if (source.isEmpty()) { translateInput.error = "Matn kiriting"; return }
        result.text = "Tarjima qilinmoqda…"
        lifecycleScope.launch {
            try { result.text = TranslationClient.toUzbek(source) }
            catch (e: Exception) { result.text = "Tarjima xatosi: ${e.message ?: "Internetni tekshiring"}" }
        }
    }
}
