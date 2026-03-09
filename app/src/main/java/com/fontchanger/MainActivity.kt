package com.fontchanger

import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Rational
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
    }

    private var isInPipMode by mutableStateOf(false)
    private var lastConvertedText by mutableStateOf("Hello World")
    private var lastStyleName by mutableStateOf("Bold Script")

    private fun buildPipParams(): PictureInPictureParams {
        val builder = PictureInPictureParams.Builder()
            .setAspectRatio(Rational(16, 9))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setAutoEnterEnabled(true)
            builder.setSeamlessResizeEnabled(true)
        }

        return builder.build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // PiPパラメータを事前設定（スワイプPiP用）
        try {
            setPictureInPictureParams(buildPipParams())
            Log.d(TAG, "PiP params set successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set PiP params", e)
        }

        setContent {
            FontChangerTheme {
                if (isInPipMode) {
                    PipContent(
                        convertedText = FontConverter.convert(
                            lastConvertedText,
                            FontStyle.BOLD_SCRIPT
                        ),
                        styleName = lastStyleName
                    )
                } else {
                    MainScreen(
                        onEnterPip = { enterPipMode() }
                    )
                }
            }
        }
    }

    private fun enterPipMode() {
        try {
            Log.d(TAG, "Entering PiP mode...")
            val result = enterPictureInPictureMode(buildPipParams())
            Log.d(TAG, "enterPictureInPictureMode result: $result")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to enter PiP mode", e)
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // Android 11以下でホームボタン時に自動PiP
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            enterPipMode()
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        Log.d(TAG, "PiP mode changed: $isInPictureInPictureMode")
        isInPipMode = isInPictureInPictureMode
    }
}
