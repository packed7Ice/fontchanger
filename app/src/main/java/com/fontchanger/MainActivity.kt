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
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Rational
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.fontchanger.ui.components.PipContent
import com.fontchanger.ui.screens.MainScreen
import com.fontchanger.ui.theme.FontChangerTheme

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "FontChanger"
        private const val ACTION_COPY = "com.fontchanger.ACTION_COPY"
        private const val REQUEST_COPY = 1
    }

    private var isInPipMode by mutableStateOf(false)
    private var lastInputText by mutableStateOf("Hello World")
    private var lastStyle by mutableStateOf(FontStyle.BOLD_SCRIPT)

    private val copyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_COPY) {
                val text = FontConverter.convert(lastInputText, lastStyle)
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("FontChanger", text))
                Toast.makeText(context, "コピーしました", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun buildPipParams(): PictureInPictureParams {
        val copyIntent = Intent(ACTION_COPY)
        val copyPendingIntent = PendingIntent.getBroadcast(
            this, REQUEST_COPY, copyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val copyAction = RemoteAction(
            Icon.createWithResource(this, android.R.drawable.ic_menu_save),
            "コピー", "変換テキストをコピー",
            copyPendingIntent
        )

        val builder = PictureInPictureParams.Builder()
            .setAspectRatio(Rational(16, 9))
            .setActions(listOf(copyAction))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setAutoEnterEnabled(true)
            builder.setSeamlessResizeEnabled(true)
        }

        return builder.build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(copyReceiver, IntentFilter(ACTION_COPY), RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(copyReceiver, IntentFilter(ACTION_COPY))
        }

        try {
            setPictureInPictureParams(buildPipParams())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set PiP params", e)
        }

        setContent {
            FontChangerTheme {
                if (isInPipMode) {
                    PipContent(
                        convertedText = FontConverter.convert(lastInputText, lastStyle),
                        styleName = lastStyle.displayName
                    )
                } else {
                    MainScreen(
                        onEnterPip = { enterPipMode() }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(copyReceiver)
        } catch (_: Exception) {}
    }

    private fun enterPipMode() {
        try {
            enterPictureInPictureMode(buildPipParams())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to enter PiP mode", e)
        }
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
