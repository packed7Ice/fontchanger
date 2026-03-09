package com.fontchanger

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.text.TextWatcher
import android.text.Editable
import androidx.core.app.NotificationCompat

class FloatingService : Service() {

    companion object {
        private const val CHANNEL_ID = "floating_service"
        private const val NOTIFICATION_ID = 1
    }

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private lateinit var layoutParams: WindowManager.LayoutParams
    private var styleIndex = 0
    private var autoCopy = false
    private var isInputActive = false

    private val currentStyle: FontStyle
        get() = FontStyle.entries[styleIndex]

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        createFloatingWindow()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, "フローティングウィンドウ",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "フローティングウィンドウの実行中通知"
            setShowBadge(false)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("FontChanger")
            .setContentText("フローティングウィンドウ実行中")
            .setSmallIcon(android.R.drawable.ic_menu_edit)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun dp(value: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, value.toFloat(),
            resources.displayMetrics
        ).toInt()
    }

    /** フォーカスを有効にする (入力モード) */
    private fun enableFocus() {
        if (isInputActive) return
        isInputActive = true
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
        try { windowManager.updateViewLayout(floatingView, layoutParams) } catch (_: Exception) {}
    }

    /** ウィンドウ外タッチを処理 */
    private fun handleOutsideTouch() {
        if (isInputActive) {
            disableFocus()
        }
    }

    /** フォーカスを無効にする (他アプリに制御を返す) */
    private fun disableFocus() {
        if (!isInputActive) return
        isInputActive = false
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        try { windowManager.updateViewLayout(floatingView, layoutParams) } catch (_: Exception) {}
        // キーボードを閉じる
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(floatingView.windowToken, 0)
    }

    private fun createFloatingWindow() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // 設定値を読み込み
        styleIndex = AppSettings.getDefaultStyleIndex(this)
        autoCopy = AppSettings.getAutoCopy(this)
        val windowWidth = AppSettings.getFloatingWidth(this)
        val opacity = AppSettings.getFloatingOpacity(this)

        layoutParams = WindowManager.LayoutParams(
            dp(windowWidth),
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, // 初期状態: フォーカスなし
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = dp(20)
            y = dp(100)
        }

        // ルートレイアウト
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xF01E1E2E.toInt())
            setPadding(dp(12), dp(8), dp(12), dp(12))
            elevation = dp(8).toFloat()
            alpha = opacity
            // ウィンドウ外タッチで自動的にフォーカス解放
            setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_OUTSIDE) {
                    handleOutsideTouch()
                }
                false
            }
        }

        // ドラッグハンドル + 閉じるボタン
        val headerRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val dragHandle = TextView(this).apply {
            text = "⠿ FontChanger"
            setTextColor(0xFFA0A0B0.toInt())
            textSize = 12f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val closeBtn = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            setBackgroundColor(0x00000000)
            setColorFilter(0xFFA0A0B0.toInt())
            setOnClickListener { stopSelf() }
            layoutParams = LinearLayout.LayoutParams(dp(32), dp(32))
        }

        headerRow.addView(dragHandle)
        headerRow.addView(closeBtn)
        root.addView(headerRow)

        // テキスト入力
        val input = EditText(this).apply {
            hint = "タップして入力..."
            setHintTextColor(0xFF606080.toInt())
            setTextColor(0xFFE4E4EC.toInt())
            textSize = 14f
            setBackgroundColor(0xFF252535.toInt())
            setPadding(dp(12), dp(8), dp(12), dp(8))
            isSingleLine = true
            isFocusable = true
            isFocusableInTouchMode = true
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(8) }
        }

        // 入力欄タップ → フォーカスを有効化
        input.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                enableFocus()
                v.requestFocus()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT)
            }
            false
        }

        root.addView(input)

        // フォント名表示
        val styleLabel = TextView(this).apply {
            text = currentStyle.displayName
            setTextColor(0xFF6C63FF.toInt())
            textSize = 11f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(8) }
        }
        root.addView(styleLabel)

        // 変換結果表示
        val resultText = TextView(this).apply {
            text = ""
            setTextColor(0xFFE4E4EC.toInt())
            textSize = 16f
            maxLines = 3
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(4) }
        }
        root.addView(resultText)

        // ボタン行: [← 前へ] [コピー] [次へ →] [✓ 完了]
        val buttonRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(8) }
        }

        fun createButton(text: String, onClick: () -> Unit): TextView {
            return TextView(this).apply {
                this.text = text
                setTextColor(0xFFE4E4EC.toInt())
                textSize = 13f
                setBackgroundColor(0xFF353545.toInt())
                setPadding(dp(12), dp(8), dp(12), dp(8))
                gravity = Gravity.CENTER
                setOnClickListener { onClick() }
                layoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                ).apply { marginStart = dp(2); marginEnd = dp(2) }
            }
        }

        fun updateResult() {
            val inputStr = input.text.toString().ifEmpty { "Hello World" }
            resultText.text = FontConverter.convert(inputStr, currentStyle)
            styleLabel.text = currentStyle.displayName
        }

        fun copyToClipboard() {
            val text = resultText.text.toString()
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("FontChanger", text))
            Toast.makeText(this, "コピーしました", Toast.LENGTH_SHORT).show()
            disableFocus()
        }

        val prevBtn = createButton("◀") {
            styleIndex = if (styleIndex > 0) styleIndex - 1 else FontStyle.entries.size - 1
            updateResult()
            if (autoCopy) copyToClipboard()
        }
        val copyBtn = createButton("コピー") {
            copyToClipboard()
        }
        val nextBtn = createButton("▶") {
            styleIndex = if (styleIndex < FontStyle.entries.size - 1) styleIndex + 1 else 0
            updateResult()
            if (autoCopy) copyToClipboard()
        }

        buttonRow.addView(prevBtn)
        buttonRow.addView(copyBtn)
        buttonRow.addView(nextBtn)
        root.addView(buttonRow)

        // テキスト変更リスナー
        input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { updateResult() }
        })

        // ドラッグ処理
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f

        headerRow.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = layoutParams.x
                    initialY = layoutParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    layoutParams.x = initialX + (event.rawX - initialTouchX).toInt()
                    layoutParams.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(root, layoutParams)
                    true
                }
                else -> false
            }
        }

        // 初回変換
        updateResult()

        floatingView = root
        windowManager.addView(root, layoutParams)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::floatingView.isInitialized) {
            try { windowManager.removeView(floatingView) } catch (_: Exception) {}
        }
    }
}
