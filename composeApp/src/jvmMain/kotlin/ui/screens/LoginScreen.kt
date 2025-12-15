package ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ui.theme.AccentColor
import ui.theme.BgColor
import ui.theme.PrimaryColor
import utils.SecurityUtils

/**
 * Экран авторизации пользователя.
 * Обрабатывает два сценария:
 * 1. Первый запуск: Создание и сохранение нового мастер-пароля (с проверкой длины).
 * 2. Обычный вход: Проверка введенного пароля с сохраненным хешем (без проверки длины).
 *
 * @param masterPasswordHash Сохраненный хеш пароля (null при первом запуске).
 * @param masterPasswordSalt Сохраненная соль (null при первом запуске).
 * @param onLogin Callback успешного входа.
 * @param onFirstSetup Callback успешной установки нового пароля.
 */
@Composable
fun LoginScreen(
    masterPasswordHash: String?,
    masterPasswordSalt: String?,
    onLogin: (String) -> Unit,
    onFirstSetup: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    val isFirstTime = masterPasswordHash == null

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BgColor
    ) {
        Box(contentAlignment = Alignment.Center) {
            Card(
                modifier = Modifier.width(400.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(64.dp),
                        color = AccentColor,
                        shape = CircleShape
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    Text("Менеджер Паролей", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))

                    if (isFirstTime) {
                        Text("Установите мастер-пароль", fontSize = 14.sp, color = Color.Gray)
                        Text("Этот пароль будет использоваться для шифрования всех данных",
                            fontSize = 12.sp, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    } else {
                        Text("Введите мастер-пароль для разблокировки", fontSize = 14.sp, color = Color.Gray)
                    }

                    Spacer(Modifier.height(24.dp))

                    if (error.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2))
                        ) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(error, color = Color(0xFFDC2626))
                            }
                        }
                    }

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; error = "" },
                        label = { Text("Мастер-пароль") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentColor,
                            focusedLabelColor = AccentColor
                        )
                    )

                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = {
                            handleLogin(password, isFirstTime, masterPasswordHash, masterPasswordSalt, onLogin, onFirstSetup) { errorMsg ->
                                error = errorMsg
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                    ) {
                        Text(if (isFirstTime) "Установить пароль" else "Разблокировать", color = Color.White)
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Все пароли зашифрованы с использованием AES-256", fontSize = 11.sp, color = Color.Gray)
                }
            }
        }
    }
}

private fun handleLogin(
    password: String,
    isFirstTime: Boolean,
    masterPasswordHash: String?,
    masterPasswordSalt: String?,
    onLogin: (String) -> Unit,
    onFirstSetup: (String) -> Unit,
    onError: (String) -> Unit
) {
    if (isFirstTime) {
        // Проверка длины ТОЛЬКО при создании нового пароля
        if (password.length < 8) {
            onError("Пароль должен быть не менее 8 символов")
            return
        }
        onFirstSetup(password)
    } else {
        // При входе проверяем только совпадение хеша, независимо от длины
        if (password.isEmpty()) {
            onError("Введите пароль")
            return
        }

        if (SecurityUtils.verifyPassword(password, masterPasswordHash!!, masterPasswordSalt!!)) {
            onLogin(password)
        } else {
            onError("Неверный мастер-пароль")
        }
    }
}