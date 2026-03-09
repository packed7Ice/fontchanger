package com.fontchanger.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fontchanger.AppSettings
import com.fontchanger.FontStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onThemeChanged: (Int) -> Unit
) {
    val context = LocalContext.current

    var themeMode by remember { mutableIntStateOf(AppSettings.getThemeMode(context)) }
    var defaultStyleIndex by remember { mutableIntStateOf(AppSettings.getDefaultStyleIndex(context)) }
    var floatingOpacity by remember { mutableFloatStateOf(AppSettings.getFloatingOpacity(context)) }
    var autoCopy by remember { mutableStateOf(AppSettings.getAutoCopy(context)) }
    var floatingWidth by remember { mutableIntStateOf(AppSettings.getFloatingWidth(context)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("設定", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "戻る")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // --- テーマ設定 ---
            SectionTitle("テーマ")

            val themeOptions = listOf("システムに合わせる", "ライト", "ダーク")
            themeOptions.forEachIndexed { index, label ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            themeMode = index
                            AppSettings.setThemeMode(context, index)
                            onThemeChanged(index)
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = themeMode == index,
                        onClick = {
                            themeMode = index
                            AppSettings.setThemeMode(context, index)
                            onThemeChanged(index)
                        }
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // --- デフォルトフォント ---
            SectionTitle("デフォルトフォント")
            Text(
                text = "フローティングウィンドウ起動時に選択されるフォント",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))

            FontStyle.entries.forEachIndexed { index, style ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            defaultStyleIndex = index
                            AppSettings.setDefaultStyleIndex(context, index)
                        }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = defaultStyleIndex == index,
                        onClick = {
                            defaultStyleIndex = index
                            AppSettings.setDefaultStyleIndex(context, index)
                        }
                    )
                    Text(
                        text = "${style.displayName}  ${style.preview}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // --- フローティングウィンドウ ---
            SectionTitle("フローティングウィンドウ")

            // 不透明度
            Text(
                text = "不透明度: ${(floatingOpacity * 100).toInt()}%",
                style = MaterialTheme.typography.bodyLarge,
            )
            Slider(
                value = floatingOpacity,
                onValueChange = {
                    floatingOpacity = it
                    AppSettings.setFloatingOpacity(context, it)
                },
                valueRange = 0.3f..1f,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ウィンドウ幅
            Text(
                text = "ウィンドウ幅: ${floatingWidth}dp",
                style = MaterialTheme.typography.bodyLarge,
            )
            Slider(
                value = floatingWidth.toFloat(),
                onValueChange = {
                    floatingWidth = it.toInt()
                    AppSettings.setFloatingWidth(context, floatingWidth)
                },
                valueRange = 200f..400f,
                steps = 3,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // --- 動作設定 ---
            SectionTitle("動作")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("自動コピー", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "フォント切替時に自動でコピー",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = autoCopy,
                    onCheckedChange = {
                        autoCopy = it
                        AppSettings.setAutoCopy(context, it)
                    }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // --- このアプリについて ---
            SectionTitle("このアプリについて")

            Text(
                text = "FontChanger",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "© 2026 YorikawaAise",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = "Apache License 2.0",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            val uriHandler = LocalUriHandler.current

            Text(
                text = "GitHub で表示",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable { uriHandler.openUri("https://github.com/packed7Ice/fontchanger") }
                    .padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}
