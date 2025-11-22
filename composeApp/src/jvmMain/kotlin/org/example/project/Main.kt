package org.example.project

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import org.example.project.model.PasswordEntry
import org.example.project.model.ScreenState
import org.example.project.ui.screens.DashboardScreen
import org.example.project.ui.screens.LoginScreen
import org.example.project.ui.theme.BgColor
import org.example.project.ui.theme.PrimaryColor
import org.example.project.utils.SecurityUtils

fun main() = application {
    val windowState = rememberWindowState(width = 1200.dp, height = 800.dp, position = WindowPosition(Alignment.Center))
    Window(onCloseRequest = ::exitApplication, state = windowState, title = "Менеджер Паролей") {
        MaterialTheme(
            colorScheme = lightColorScheme(
                primary = PrimaryColor,
                background = BgColor,
                surface = Color.White,
                onPrimary = Color.White
            )
        ) {
            App()
        }
    }
}

@Composable
fun App() {
    var screen by remember { mutableStateOf(ScreenState.LOGIN) }
    var masterPassword by remember { mutableStateOf("") }
    val initialPasswords = remember { mutableStateListOf<PasswordEntry>() }

    when (screen) {
        ScreenState.LOGIN -> LoginScreen(
            onLogin = { pass ->
                masterPassword = pass
                if (initialPasswords.isEmpty()) {
                    initialPasswords.add(PasswordEntry(name = "Gmail", login = "user@gmail.com", passwordEncrypted = SecurityUtils.encrypt("ExamplePass123!", pass), url = "https://gmail.com", tags = listOf("email", "важное")))
                    initialPasswords.add(PasswordEntry(name = "GitHub", login = "developer", passwordEncrypted = SecurityUtils.encrypt("SecureGitHub456#", pass), url = "https://github.com", folder = "Работа", tags = listOf("разработка", "git")))
                    initialPasswords.add(PasswordEntry(name = "Банк", login = "ivan.ivanov", passwordEncrypted = SecurityUtils.encrypt("bank2024", pass), url = "https://online.bank.com", folder = "Финансы", tags = listOf("банк", "финансы"), isWeak = true))
                }
                screen = ScreenState.DASHBOARD
            }
        )
        ScreenState.DASHBOARD -> DashboardScreen(
            passwords = initialPasswords,
            masterPassword = masterPassword,
            onLogout = {
                masterPassword = ""
                screen = ScreenState.LOGIN
            }
        )
    }
}