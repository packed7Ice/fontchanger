package com.fontchanger

import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.util.Rational
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.fontchanger.ui.components.PipContent
import com.fontchanger.ui.screens.MainScreen
import com.fontchanger.ui.theme.FontChangerTheme

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "FontChanger"
        private const val ACTION_COPY = "com.fontchanger.ACTION_COPY"
        private const val ACTION_PREV = "com.fontchanger.ACTION_PREV"
        private const val ACTION_NEXT = "com.fontchanger.ACTION_NEXT"
    }

    private var isInPipMode by mutableStateOf(false)
    private var lastInputText by mutableStateOf("Hello World")
    private var styleIndex by mutableIntStateOf(0)

    private val currentStyle: FontStyle
        get() = FontStyle.entries[styleIndex]

    private val pipReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_COPY -> {
                    val text = FontConverter.convert(lastInputText, currentStyle)
                    val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("FontChanger", text))
                    Toast.makeText(context, "コピーしました", Toast.LENGTH_SHORT).show()
                }
                ACTION_PREV -> {
                    styleIndex = if (styleIndex > 0) styleIndex - 1
                        else FontStyle.entries.size - 1
                    updatePipParams()
                }
                ACTION_NEXT -> {
                    styleIndex = if (styleIndex < FontStyle.entries.size - 1) styleIndex + 1
                        else 0
                    updatePipParams()
                }
            }
        }
    }

    private fun createAction(iconRes: Int, title: String, action: String, requestCode: Int): RemoteAction {
        val intent = Intent(action).apply { setPackage(packageName) }
        val pendingIntent = PendingIntent.getBroadcast(
            this, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return RemoteAction(
            Icon.createWithResource(this, iconRes),
            title, title, pendingIntent
        )
    }

    private fun buildPipParams(): PictureInPictureParams {
        val actions = listOf(
            createAction(android.R.drawable.ic_media_previous, "前へ", ACTION_PREV, 0),
            createAction(android.R.drawable.ic_menu_save, "コピー", ACTION_COPY, 1),
            createAction(android.R.drawable.ic_media_next, "次へ", ACTION_NEXT, 2),
        )
        val builder = PictureInPictureParams.Builder()
            .setAspectRatio(Rational(16, 9))
            .setActions(actions)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setAutoEnterEnabled(true)
            builder.setSeamlessResizeEnabled(true)
        }
        return builder.build()
    }

    private fun updatePipParams() {
        try { setPictureInPictureParams(buildPipParams()) } catch (e: Exception) {
            Log.e(TAG, "Failed to update PiP params", e)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val filter = IntentFilter().apply {
            addAction(ACTION_COPY)
            addAction(ACTION_PREV)
            addAction(ACTION_NEXT)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(pipReceiver, filter, RECEIVER_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(pipReceiver, filter)
        }

        updatePipParams()

        setContent {
            FontChangerTheme {
                if (isInPipMode) {
                    PipContent(
                        convertedText = FontConverter.convert(lastInputText, currentStyle),
                        styleName = currentStyle.displayName
                    )
                } else {
                    MainScreen(
                        onEnterPip = { enterPipMode() },
                        onStartFloating = { startFloatingWindow() }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try { unregisterReceiver(pipReceiver) } catch (_: Exception) {}
    }

    private fun enterPipMode() {
        try {
            enterPictureInPictureMode(buildPipParams())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to enter PiP", e)
        }
    }

    private fun startFloatingWindow() {
        if (!Settings.canDrawOverlays(this)) {
            // 他のアプリの上に表示する権限をリクエスト
            Toast.makeText(this, "「他のアプリの上に表示」を許可してください", Toast.LENGTH_LONG).show()
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
            return
        }
        val intent = Intent(this, FloatingService::class.java)
        startForegroundService(intent)
        // アプリをバックグラウンドに移動
        moveTaskToBack(true)
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            enterPipMode()
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isInPipMode = isInPictureInPictureMode
    }
}
