package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppStrings
import com.example.ui.ChatViewModel
import com.example.ui.theme.ErrorRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ChatViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }

    val settings by viewModel.settings.collectAsState()
    val isFa = AppStrings.isPersian(settings.language)
    val activeBackend by viewModel.activeBackend.collectAsState()
    val availableRam = viewModel.getAvailableRamMb()
    val modelsList by viewModel.modelsList.collectAsState()
    val activeModel = modelsList.find { it.id == settings.activeModelId }

    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var tempSliderValue by remember(settings.temperature) { mutableFloatStateOf(settings.temperature) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = AppStrings.settings(isFa),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Language Setting
            item {
                SettingsCard(title = AppStrings.languageSetting(isFa)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FilterChip(
                            selected = settings.language == "system",
                            onClick = { viewModel.updateLanguage("system") },
                            label = { Text(AppStrings.systemDefault(isFa), fontSize = 12.sp) }
                        )
                        FilterChip(
                            selected = settings.language == "fa",
                            onClick = { viewModel.updateLanguage("fa") },
                            label = { Text("فارسی (Persian)", fontSize = 12.sp) }
                        )
                        FilterChip(
                            selected = settings.language == "en",
                            onClick = { viewModel.updateLanguage("en") },
                            label = { Text("English", fontSize = 12.sp) }
                        )
                    }
                }
            }

            // Theme Setting
            item {
                SettingsCard(title = AppStrings.themeSetting(isFa)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FilterChip(
                            selected = settings.theme == "system",
                            onClick = { viewModel.updateTheme("system") },
                            label = { Text(AppStrings.systemDefault(isFa), fontSize = 12.sp) }
                        )
                        FilterChip(
                            selected = settings.theme == "light",
                            onClick = { viewModel.updateTheme("light") },
                            label = { Text(AppStrings.lightTheme(isFa), fontSize = 12.sp) }
                        )
                        FilterChip(
                            selected = settings.theme == "dark",
                            onClick = { viewModel.updateTheme("dark") },
                            label = { Text(AppStrings.darkTheme(isFa), fontSize = 12.sp) }
                        )
                    }
                }
            }

            // Hardware Backend Setting
            item {
                SettingsCard(title = AppStrings.backendSetting(isFa)) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = settings.preferredBackend == "auto",
                            onClick = { viewModel.updatePreferredBackend("auto") },
                            label = { Text(AppStrings.backendAuto(isFa), fontSize = 12.sp) }
                        )
                        FilterChip(
                            selected = settings.preferredBackend == "cpu",
                            onClick = { viewModel.updatePreferredBackend("cpu") },
                            label = { Text(AppStrings.backendCpu(isFa), fontSize = 12.sp) }
                        )
                    }
                }
            }

            // Temperature (Sampling)
            item {
                SettingsCard(
                    title = "${AppStrings.temperatureSetting(isFa)}: ${String.format("%.1f", tempSliderValue)}"
                ) {
                    Slider(
                        value = tempSliderValue,
                        onValueChange = { tempSliderValue = it },
                        onValueChangeFinished = {
                            viewModel.updateTemperature(tempSliderValue)
                        },
                        valueRange = 0.1f..1.5f,
                        steps = 13,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isFa) "دقیق‌تر (۰.۱)" else "Focused (0.1)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (isFa) "خلاق‌تر (۱.۵)" else "Creative (1.5)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Max Tokens
            item {
                SettingsCard(title = AppStrings.maxTokensSetting(isFa)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(256, 512, 1024, 2048).forEach { tokenLimit ->
                            FilterChip(
                                selected = settings.maxResponseTokens == tokenLimit,
                                onClick = { viewModel.updateMaxTokens(tokenLimit) },
                                label = { Text("$tokenLimit", fontSize = 12.sp) }
                            )
                        }
                    }
                }
            }

            // Hardware Backend (CPU / Auto / GPU)
            item {
                SettingsCard(title = AppStrings.backendSetting(isFa)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            FilterChip(
                                selected = settings.preferredBackend == "cpu",
                                onClick = { viewModel.updatePreferredBackend("cpu") },
                                label = { Text(if (isFa) "CPU (پایدار)" else "CPU (Stable)", fontSize = 12.sp) }
                            )
                            FilterChip(
                                selected = settings.preferredBackend == "auto",
                                onClick = { viewModel.updatePreferredBackend("auto") },
                                label = { Text(if (isFa) "خودکار (Auto)" else "Auto", fontSize = 12.sp) }
                            )
                            FilterChip(
                                selected = settings.preferredBackend == "gpu",
                                onClick = { viewModel.updatePreferredBackend("gpu") },
                                label = { Text("GPU (OpenCL)", fontSize = 12.sp) }
                            )
                        }
                        Text(
                            text = if (isFa) {
                                "حالت CPU روی تمام گوشی‌ها ۱۰۰٪ پایدار است و نیازی به درایور OpenCL ندارد."
                            } else {
                                "CPU mode is 100% stable across all devices and requires no OpenCL driver."
                            },
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Model & Device Info Card
            item {
                SettingsCard(title = AppStrings.modelInfoSection(isFa)) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Model: ${activeModel?.name ?: "None"}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = AppStrings.activeBackend(isFa, activeBackend),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (availableRam > 0) {
                            Text(
                                text = AppStrings.availableRam(isFa, availableRam),
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Clear Chat History
            item {
                OutlinedButton(
                    onClick = { showClearHistoryDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("clear_all_history_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = null,
                        tint = ErrorRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = AppStrings.clearAllHistory(isFa),
                        color = ErrorRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text(AppStrings.clearAllHistory(isFa)) },
            text = { Text(AppStrings.clearHistoryConfirm(isFa)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllChatHistory()
                        showClearHistoryDialog = false
                    }
                ) {
                    Text(AppStrings.clearChat(isFa), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text(AppStrings.cancel(isFa))
                }
            }
        )
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                RoundedCornerShape(16.dp)
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}
