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

/**
 * Точка входа в приложение.
 *
 * Управляет глобальным состоянием:
 * - Текущий экран ([ScreenState]).
 * - Данные текущей сессии (Мастер-пароль).
 * - Загрузка начальных данных из БД.
 * - Обработка глобальных событий (Смена пароля, Выход).
 */
fun main() = application {
    val windowState = rememberWindowState(width = 1200.dp, height = 800.dp, position = WindowPosition(Alignment.Center))

    var screen by remember { mutableStateOf(ScreenState.LOGIN) }
    var masterPassword by remember { mutableStateOf("") }
    var masterPasswordHash by remember { mutableStateOf<String?>(null) }
    var masterPasswordSalt by remember { mutableStateOf<String?>(null) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }

    val passwords = remember { mutableStateListOf<PasswordEntry>() }

    // Инициализация при запуске
    LaunchedEffect(Unit) {
        val savedHash = DatabaseManager.getSetting("master_password_hash")
        val savedSalt = DatabaseManager.getSetting("master_password_salt")

        if (savedHash != null && savedSalt != null) {
            masterPasswordHash = savedHash
            masterPasswordSalt = savedSalt
        }

        passwords.clear()
        passwords.addAll(DatabaseManager.getAllPasswords())

        // Миграция тегов (если требуется)
        val migrationDone = DatabaseManager.getSetting("tags_migration_done")
        if (migrationDone == null) {
            val existingTags = passwords.flatMap { it.tags }.toSet()
            existingTags.forEach { tag ->
                DatabaseManager.saveTag(tag)
            }
            DatabaseManager.saveSetting("tags_migration_done", "true")
        }
    }

    /**
     * Логика смены мастер-пароля.
     * Перешифровывает все записи в БД с использованием нового ключа.
     */
    fun changeMasterPassword(oldPassword: String, newPassword: String) {
        try {
            val allPasswords = DatabaseManager.getAllPasswords()
            allPasswords.forEach { entry ->
                val decrypted = SecurityUtils.decrypt(entry.passwordEncrypted, oldPassword)
                val reencrypted = SecurityUtils.encrypt(decrypted, newPassword)
                DatabaseManager.savePassword(entry.copy(passwordEncrypted = reencrypted))
            }

            val (newHash, newSalt) = SecurityUtils.hashPassword(newPassword)
            DatabaseManager.saveSetting("master_password_hash", newHash)
            DatabaseManager.saveSetting("master_password_salt", newSalt)

            masterPasswordHash = newHash
            masterPasswordSalt = newSalt
            masterPassword = newPassword

            passwords.clear()
            passwords.addAll(DatabaseManager.getAllPasswords())
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

                        if (masterPasswordHash == null) {
                            // Первый запуск
                            val (hash, salt) = SecurityUtils.hashPassword(pass)
                            DatabaseManager.saveSetting("master_password_hash", hash)
                            DatabaseManager.saveSetting("master_password_salt", salt)
                            masterPasswordHash = hash
                            masterPasswordSalt = salt

                            // Создаем демо-пароли
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
                                    tags = listOf("разработка", "git")
                                ),
                                PasswordEntry(
                                    name = "Банк",
                                    login = "ivan.ivanov",
                                    passwordEncrypted = SecurityUtils.encrypt("bank2024", pass),
                                    url = "https://online.bank.com",
                                    tags = listOf("банк", "финансы"),
                                    isWeak = true
                                )
                            )
                            demoPasswords.forEach {
                                DatabaseManager.savePassword(it)
                            }
                        }

                        passwords.clear()
                        passwords.addAll(DatabaseManager.getAllPasswords())
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