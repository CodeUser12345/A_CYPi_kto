package org.example.project.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import org.example.project.model.PasswordEntry
import org.example.project.ui.components.CheckOption
import org.example.project.ui.components.InputLabel
import org.example.project.ui.components.SimpleInput
import org.example.project.ui.components.TabButton
import org.example.project.ui.components.TagChip
import org.example.project.ui.theme.AccentColor
import org.example.project.ui.theme.PrimaryColor
import org.example.project.utils.SecurityUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PasswordDialog(
    onDismiss: () -> Unit,
    onSave: (PasswordEntry) -> Unit,
    existingEntry: PasswordEntry? = null,
    masterPassword: String
) {
    var selectedTab by remember { mutableStateOf(0) }

    var name by remember { mutableStateOf(existingEntry?.name ?: "") }
    var login by remember { mutableStateOf(existingEntry?.login ?: "") }
    var url by remember { mutableStateOf(existingEntry?.url ?: "") }

    var tags by remember { mutableStateOf(existingEntry?.tags ?: emptyList()) }
    var tagInput by remember { mutableStateOf("") }

    var password by remember {
        mutableStateOf(
            if (existingEntry != null) SecurityUtils.decrypt(existingEntry.passwordEncrypted, masterPassword)
            else ""
        )
    }

    var passLength by remember { mutableStateOf(16f) }
    var useUpper by remember { mutableStateOf(true) }
    var useLower by remember { mutableStateOf(true) }
    var useDigits by remember { mutableStateOf(true) }
    var useSymbols by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.width(600.dp).height(750.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(24.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = if (existingEntry == null) "Добавить новый пароль" else "Редактировать пароль",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
                }
                Text("Заполните информацию о записи пароля", color = Color.Gray)

                Spacer(Modifier.height(24.dp))

                Row(Modifier.fillMaxWidth().background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp)).padding(4.dp)) {
                    TabButton("Детали", selectedTab == 0) { selectedTab = 0 }
                    TabButton("Генератор", selectedTab == 1) { selectedTab = 1 }
                }

                Spacer(Modifier.height(24.dp))

                if (selectedTab == 0) {
                    Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                        InputLabel("Название *")
                        SimpleInput(name, "Например: Gmail") { name = it }

                        InputLabel("Имя пользователя / Email")
                        SimpleInput(login, "user@example.com") { login = it }

                        InputLabel("Пароль *")
                        SimpleInput(password, "........") { password = it }

                        InputLabel("URL / Веб-сайт")
                        SimpleInput(url, "https://example.com") { url = it }

                        InputLabel("Теги")
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = tagInput,
                                onValueChange = { tagInput = it },
                                placeholder = { Text("Например: работа", color = Color.LightGray) },
                                modifier = Modifier.weight(1f).background(Color(0xFFF9FAFB)),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AccentColor,
                                    unfocusedBorderColor = Color.Transparent
                                ),
                                trailingIcon = {
                                    IconButton(onClick = {
                                        if (tagInput.isNotBlank() && !tags.contains(tagInput.trim())) {
                                            tags = tags + tagInput.trim()
                                            tagInput = ""
                                        }
                                    }) {
                                        Icon(Icons.Default.Add, null, tint = AccentColor)
                                    }
                                }
                            )
                        }
                        if (tags.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                tags.forEach { tag ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .clickable {
                                                tags = tags - tag
                                            }
                                    ) {
                                        TagChip(text = "$tag ×", bg = Color(0xFFE5E7EB))
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Column(Modifier.weight(1f)) {
                        Text("Длина пароля: ${passLength.toInt()}")
                        Slider(
                            value = passLength,
                            onValueChange = { passLength = it },
                            valueRange = 8f..32f,
                            colors = SliderDefaults.colors(thumbColor = PrimaryColor, activeTrackColor = PrimaryColor)
                        )

                        Spacer(Modifier.height(16.dp))
                        CheckOption("Заглавные буквы (A-Z)", useUpper) { useUpper = it }
                        CheckOption("Строчные буквы (a-z)", useLower) { useLower = it }
                        CheckOption("Цифры (0-9)", useDigits) { useDigits = it }
                        CheckOption("Специальные символы (!@#$%)", useSymbols) { useSymbols = it }

                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = {
                                val chars = buildString {
                                    if(useUpper) append("ABCDEFGHIJKLMNOPQRSTUVWXYZ")
                                    if(useLower) append("abcdefghijklmnopqrstuvwxyz")
                                    if(useDigits) append("0123456789")
                                    if(useSymbols) append("!@#$%^&*()_+")
                                }
                                if (chars.isNotEmpty()) {
                                    password = (1..passLength.toInt()).map { chars.random() }.joinToString("")
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                        ) {
                            Icon(Icons.Default.Refresh, null, tint = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text("Сгенерировать пароль", color = Color.White)
                        }

                        Spacer(Modifier.height(16.dp))
                        if (password.isNotEmpty()) {
                            Box(Modifier.fillMaxWidth().background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp)).padding(16.dp)) {
                                Text(password, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.height(40.dp)) { Text("Отмена") }
                    Spacer(Modifier.width(12.dp))
                    Button(
                        onClick = {
                            val entryToSave = if (existingEntry != null) {
                                existingEntry.copy(name = name, login = login, passwordEncrypted = password, url = url, tags = tags)
                            } else {
                                PasswordEntry(name = name, login = login, passwordEncrypted = password, url = url, tags = tags)
                            }
                            onSave(entryToSave)
                        },
                        modifier = Modifier.height(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B7280))
                    ) {
                        Text("Сохранить", color = Color.White)
                    }
                }
            }
        }
    }
}