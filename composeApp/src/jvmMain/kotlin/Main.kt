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
import data.DatabaseManager
import model.PasswordEntry
import model.ScreenState
import ui.dialogs.ChangeMasterPasswordDialog
import ui.screens.DashboardScreen
import ui.screens.LoginScreen
import ui.theme.BgColor
import ui.theme.PrimaryColor
import utils.SecurityUtils

fun main() = application {
    val windowState = rememberWindowState(width = 1200.dp, height = 800.dp, position = WindowPosition(Alignment.Center))

    var screen by remember { mutableStateOf(ScreenState.LOGIN) }
    var masterPassword by remember { mutableStateOf("") }
    var masterPasswordHash by remember { mutableStateOf<String?>(null) }
    var masterPasswordSalt by remember { mutableStateOf<String?>(null) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }

    val passwords = remember { mutableStateListOf<PasswordEntry>() }
    val folders = remember { mutableStateListOf("Основная", "Работа", "Финансы") }

    // Загружаем сохраненные настройки при старте
    LaunchedEffect(Unit) {
        val savedHash = DatabaseManager.getSetting("master_password_hash")
        val savedSalt = DatabaseManager.getSetting("master_password_salt")

        if (savedHash != null && savedSalt != null) {
            masterPasswordHash = savedHash
            masterPasswordSalt = savedSalt
        } else {
            // Первый запуск - создаем пустую базу
            // Ничего не делаем, пользователь установит пароль
        }
    }

    // Функция для смены пароля
    fun changeMasterPassword(oldPassword: String, newPassword: String) {
        try {
            // Перешифровываем все пароли новым паролем
            val allPasswords = data.DatabaseManager.getAllPasswords()
            allPasswords.forEach { entry ->
                val decrypted = SecurityUtils.decrypt(entry.passwordEncrypted, oldPassword)
                val reencrypted = SecurityUtils.encrypt(decrypted, newPassword)
                data.DatabaseManager.savePassword(entry.copy(passwordEncrypted = reencrypted))
            }

            // Сохраняем новый хэш
            val (newHash, newSalt) = SecurityUtils.hashPassword(newPassword)
            data.DatabaseManager.saveSetting("master_password_hash", newHash)
            data.DatabaseManager.saveSetting("master_password_salt", newSalt)

            masterPasswordHash = newHash
            masterPasswordSalt = newSalt
            masterPassword = newPassword

            // ОБНОВЛЯЕМ список паролей из БД
            passwords.clear()
            passwords.addAll(data.DatabaseManager.getAllPasswords())
        } catch (e: Exception) {
            println("Ошибка при смене пароля: ${e.message}")
        }
    }

    Window(onCloseRequest = ::exitApplication, state = windowState, title = "Менеджер Паролей") {
        MaterialTheme(
            colorScheme = lightColorScheme(
                primary = PrimaryColor,
                background = BgColor,
                surface = Color.White,
                onPrimary = Color.White
            )
        ) {
            when (screen) {
                ScreenState.LOGIN -> LoginScreen(
                    masterPasswordHash = masterPasswordHash,
                    masterPasswordSalt = masterPasswordSalt,
                    onLogin = { pass ->
                        masterPassword = pass

                        // Если это первый запуск (нет сохраненного хэша)
                        if (masterPasswordHash == null) {
                            val (hash, salt) = SecurityUtils.hashPassword(pass)
                            DatabaseManager.saveSetting("master_password_hash", hash)
                            DatabaseManager.saveSetting("master_password_salt", salt)
                            masterPasswordHash = hash
                            masterPasswordSalt = salt

                            // Создаем демо-пароли для первого запуска
                            val demoPasswords = listOf(
                                PasswordEntry(
                                    name = "Gmail",
                                    login = "user@gmail.com",
                                    passwordEncrypted = SecurityUtils.encrypt("ExamplePass123!", pass),
                                    url = "https://gmail.com",
                                    tags = listOf("email", "важное")
                                ),
                                PasswordEntry(
                                    name = "GitHub",
                                    login = "developer",
                                    passwordEncrypted = SecurityUtils.encrypt("SecureGitHub456#", pass),
                                    url = "https://github.com",
                                    folder = "Работа",
                                    tags = listOf("разработка", "git")
                                ),
                                PasswordEntry(
                                    name = "Банк",
                                    login = "ivan.ivanov",
                                    passwordEncrypted = SecurityUtils.encrypt("bank2024", pass),
                                    url = "https://online.bank.com",
                                    folder = "Финансы",
                                    tags = listOf("банк", "финансы"),
                                    isWeak = true
                                )
                            )
                            demoPasswords.forEach {
                                DatabaseManager.savePassword(it)
                            }
                        }

                        // Загружаем пароли из БД
                        passwords.clear()
                        passwords.addAll(DatabaseManager.getAllPasswords())

                        // Загружаем папки из БД
                        val dbFolders = DatabaseManager.getAllFolders()
                        if (dbFolders.isNotEmpty()) {
                            folders.clear()
                            folders.addAll(dbFolders.distinct())
                        }

                        screen = ScreenState.DASHBOARD
                    },
                    onFirstSetup = { pass ->
                        val (hash, salt) = SecurityUtils.hashPassword(pass)
                        DatabaseManager.saveSetting("master_password_hash", hash)
                        DatabaseManager.saveSetting("master_password_salt", salt)
                        masterPasswordHash = hash
                        masterPasswordSalt = salt
                        masterPassword = pass
                        screen = ScreenState.DASHBOARD
                    }
                )

                ScreenState.DASHBOARD -> DashboardScreen(
                    passwords = passwords,
                    folders = folders,
                    masterPassword = masterPassword,
                    onLogout = {
                        masterPassword = ""
                        screen = ScreenState.LOGIN
                    },
                    onChangePassword = {
                        showChangePasswordDialog = true
                    }
                )
            }

            if (showChangePasswordDialog && masterPasswordHash != null && masterPasswordSalt != null) {
                ChangeMasterPasswordDialog(
                    onDismiss = { showChangePasswordDialog = false },
                    onPasswordChanged = { oldPass, newPass ->
                        changeMasterPassword(oldPass, newPass)
                        showChangePasswordDialog = false
                    },
                    currentHash = masterPasswordHash!!,
                    currentSalt = masterPasswordSalt!!
                )
            }
        }
    }
}