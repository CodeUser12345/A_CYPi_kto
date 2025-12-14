package ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import model.PasswordEntry
import ui.components.TabButton
import ui.theme.PrimaryColor
import ui.theme.WeakColor
import utils.SecurityUtils
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
fun ImportExportDialog(
    onDismiss: () -> Unit,
    passwords: List<PasswordEntry>,
    masterPassword: String,
    onImport: (List<PasswordEntry>) -> Unit
) {
    var isExport by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf("") }
    var isSuccess by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.width(500.dp).height(500.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(24.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Импорт / Экспорт данных", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
                }
                Text("Экспортируйте или импортируйте ваши пароли", color = Color.Gray)
                Spacer(Modifier.height(20.dp))

                // ИСПРАВЛЕННАЯ СТРОКА - используем Card для фона
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(Modifier.padding(4.dp)) {
                        TabButton("Экспорт", isExport) { isExport = true; message = "" }
                        TabButton("Импорт", !isExport) { isExport = false; message = "" }
                    }
                }

                Spacer(Modifier.height(24.dp))

                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        if (isExport) Icons.Outlined.Description else Icons.Outlined.UploadFile,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = if (isSuccess) Color.Green else Color.LightGray
                    )
                    Spacer(Modifier.height(16.dp))

                    if (isExport) {
                        Text(
                            "Экспортируйте все ваши пароли в JSON файл.",
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Text(
                            "Внимание: файл будет содержать незашифрованные пароли!",
                            color = WeakColor,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    } else {
                        Text(
                            "Импортируйте пароли из JSON файла.",
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Text(
                            "Формат должен соответствовать экспортированному файлу.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }

                    if (message.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSuccess) Color(0xFFD1FAE5) else Color(0xFFFEF2F2)
                            )
                        ) {
                            Text(
                                message,
                                modifier = Modifier.padding(12.dp),
                                color = if (isSuccess) Color(0xFF065F46) else Color(0xFFDC2626)
                            )
                        }
                    }
                }

                Spacer(Modifier.weight(1f))

                Button(
                    onClick = {
                        if (isExport) {
                            exportToJson(passwords, masterPassword) { success, msg ->
                                message = msg
                                isSuccess = success
                            }
                        } else {
                            importFromJson(masterPassword) { success, msg, imported ->
                                message = msg
                                isSuccess = success
                                if (success && imported != null) {
                                    onImport(imported)
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                ) {
                    Icon(
                        if (isExport) Icons.Default.Download else Icons.Default.Upload,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(if (isExport) "Экспортировать в JSON" else "Выбрать файл", color = Color.White)
                }
            }
        }
    }
}

private fun exportToJson(
    passwords: List<PasswordEntry>,
    masterPassword: String,
    onComplete: (Boolean, String) -> Unit
) {
    try {
        val fileChooser = JFileChooser().apply {
            fileSelectionMode = JFileChooser.FILES_ONLY
            selectedFile = File("passwords_export_${System.currentTimeMillis()}.json")
            fileFilter = FileNameExtensionFilter("JSON Files", "json")
        }

        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            val file = fileChooser.selectedFile
            // Дешифруем пароли для экспорта
            val decryptedPasswords = passwords.map { entry ->
                entry.copy(
                    passwordEncrypted = SecurityUtils.decrypt(entry.passwordEncrypted, masterPassword)
                )
            }

            val json = Json { prettyPrint = true }
            val jsonString = json.encodeToString(decryptedPasswords)
            file.writeText(jsonString)
            onComplete(true, "Успешно экспортировано ${passwords.size} записей в ${file.name}")
        } else {
            onComplete(false, "Экспорт отменен")
        }
    } catch (e: Exception) {
        onComplete(false, "Ошибка экспорта: ${e.message}")
    }
}

private fun importFromJson(
    masterPassword: String,
    onComplete: (Boolean, String, List<PasswordEntry>?) -> Unit
) {
    try {
        val fileChooser = JFileChooser().apply {
            fileSelectionMode = JFileChooser.FILES_ONLY
            fileFilter = FileNameExtensionFilter("JSON Files", "json")
        }

        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            val file = fileChooser.selectedFile
            val jsonString = file.readText()
            val json = Json { ignoreUnknownKeys = true }
            val imported = json.decodeFromString<List<PasswordEntry>>(jsonString)

            // Шифруем пароли перед импортом
            val encryptedPasswords = imported.map { entry ->
                entry.copy(
                    passwordEncrypted = SecurityUtils.encrypt(entry.passwordEncrypted, masterPassword)
                )
            }

            onComplete(true, "Успешно импортировано ${imported.size} записей", encryptedPasswords)
        } else {
            onComplete(false, "Импорт отменен", null)
        }
    } catch (e: Exception) {
        onComplete(false, "Ошибка импорта: ${e.message}", null)
    }
}