package com.fontchanger

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.fontchanger.ui.screens.MainScreen
import com.fontchanger.ui.screens.SettingsScreen
import com.fontchanger.ui.theme.FontChangerTheme

class MainActivity : ComponentActivity() {

    private var currentScreen by mutableStateOf("main")
    private var themeMode by mutableIntStateOf(AppSettings.THEME_SYSTEM)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themeMode = AppSettings.getThemeMode(this)

        setContent {
            val isDark = when (themeMode) {
                AppSettings.THEME_LIGHT -> false
                AppSettings.THEME_DARK -> true
                else -> isSystemInDarkTheme()
            }

            FontChangerTheme(darkTheme = isDark) {
                when (currentScreen) {
                    "main" -> MainScreen(
                        onStartFloating = { startFloatingWindow() },
                        onOpenSettings = { currentScreen = "settings" }
                    )
                    "settings" -> SettingsScreen(
                        onBack = { currentScreen = "main" },
                        onThemeChanged = { themeMode = it }
                    )
                }
            }
        }
    }

    private fun startFloatingWindow() {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "「他のアプリの上に表示」を許可してください", Toast.LENGTH_LONG).show()
            startActivity(Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            ))
            return
        }
        startForegroundService(Intent(this, FloatingService::class.java))
        moveTaskToBack(true)
    }
}
