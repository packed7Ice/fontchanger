package com.fontchanger

import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.os.Bundle
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

    private var isInPipMode by mutableStateOf(false)
    private var lastConvertedText by mutableStateOf("Hello World")
    private var lastStyleName by mutableStateOf("Bold Script")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
        val params = PictureInPictureParams.Builder()
            .setAspectRatio(Rational(3, 1))
            .build()
        enterPictureInPictureMode(params)
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isInPipMode = isInPictureInPictureMode
    }
}
