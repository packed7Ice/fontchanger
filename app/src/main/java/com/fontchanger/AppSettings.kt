package com.fontchanger

import android.content.Context
import android.content.SharedPreferences

/** アプリ設定を管理するシングルトン */
object AppSettings {

    private const val PREFS_NAME = "fontchanger_prefs"

    // キー
    private const val KEY_THEME_MODE = "theme_mode"
    private const val KEY_DEFAULT_STYLE = "default_style"
    private const val KEY_FLOATING_OPACITY = "floating_opacity"
    private const val KEY_AUTO_COPY = "auto_copy"
    private const val KEY_FLOATING_WIDTH = "floating_width"

    // テーマモード
    const val THEME_SYSTEM = 0
    const val THEME_LIGHT = 1
    const val THEME_DARK = 2

    private fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /** テーマモード (0=システム, 1=ライト, 2=ダーク) */
    fun getThemeMode(context: Context): Int =
        prefs(context).getInt(KEY_THEME_MODE, THEME_SYSTEM)

    fun setThemeMode(context: Context, mode: Int) =
        prefs(context).edit().putInt(KEY_THEME_MODE, mode).apply()

    /** デフォルトのフォントスタイルインデックス */
    fun getDefaultStyleIndex(context: Context): Int =
        prefs(context).getInt(KEY_DEFAULT_STYLE, 0)

    fun setDefaultStyleIndex(context: Context, index: Int) =
        prefs(context).edit().putInt(KEY_DEFAULT_STYLE, index).apply()

    /** フローティングウィンドウの不透明度 (0.3〜1.0) */
    fun getFloatingOpacity(context: Context): Float =
        prefs(context).getFloat(KEY_FLOATING_OPACITY, 0.95f)

    fun setFloatingOpacity(context: Context, opacity: Float) =
        prefs(context).edit().putFloat(KEY_FLOATING_OPACITY, opacity).apply()

    /** 自動コピー (変換結果を自動でクリップボードにコピー) */
    fun getAutoCopy(context: Context): Boolean =
        prefs(context).getBoolean(KEY_AUTO_COPY, false)

    fun setAutoCopy(context: Context, enabled: Boolean) =
        prefs(context).edit().putBoolean(KEY_AUTO_COPY, enabled).apply()

    /** フローティングウィンドウの幅 (dp) */
    fun getFloatingWidth(context: Context): Int =
        prefs(context).getInt(KEY_FLOATING_WIDTH, 300)

    fun setFloatingWidth(context: Context, width: Int) =
        prefs(context).edit().putInt(KEY_FLOATING_WIDTH, width).apply()
}
