package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import com.example.ui.AppStrings
import com.example.ui.ChatViewModel
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.ModelManagerScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.LocalMindTheme

enum class CurrentScreen {
    CHAT,
    MODEL_MANAGER,
    SETTINGS
}

class MainActivity : ComponentActivity() {

    private val chatViewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settings by chatViewModel.settings.collectAsState()

            val isDarkTheme = when (settings.theme) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            val layoutDirection = AppStrings.getLayoutDirection(settings.language)

            LocalMindTheme(darkTheme = isDarkTheme) {
                CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        LocalMindApp(viewModel = chatViewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun LocalMindApp(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf(CurrentScreen.CHAT) }

    when (currentScreen) {
        CurrentScreen.CHAT -> {
            ChatScreen(
                viewModel = viewModel,
                onNavigateToModelManager = { currentScreen = CurrentScreen.MODEL_MANAGER },
                onNavigateToSettings = { currentScreen = CurrentScreen.SETTINGS },
                modifier = modifier
            )
        }
        CurrentScreen.MODEL_MANAGER -> {
            ModelManagerScreen(
                viewModel = viewModel,
                onNavigateBack = { currentScreen = CurrentScreen.CHAT },
                modifier = modifier
            )
        }
        CurrentScreen.SETTINGS -> {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { currentScreen = CurrentScreen.CHAT },
                modifier = modifier
            )
        }
    }
}
