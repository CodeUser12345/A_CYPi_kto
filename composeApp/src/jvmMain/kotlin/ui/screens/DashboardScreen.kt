package ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import model.PasswordEntry
import ui.components.PasswordCard
import ui.dialogs.ImportExportDialog
import ui.dialogs.PasswordDialog
import ui.dialogs.AddTagDialog
import ui.theme.AccentColor
import ui.theme.PrimaryColor
import ui.theme.TextColor
import utils.SecurityUtils

/**
 * Главный экран приложения (Панель управления).
 *
 * Содержит:
 * - Боковое меню с фильтрацией по тегам.
 * - Верхнюю панель с поиском и кнопками действий (добавить, импорт/экспорт, смена пароля).
 * - Список карточек паролей с поддержкой прокрутки.
 *
 * @param passwords Список паролей для отображения (MutableList для реактивного обновления).
 * @param masterPassword Мастер-пароль для операций шифрования/дешифрования.
 * @param onLogout Callback выхода из системы.
 * @param onChangePassword Callback открытия диалога смены пароля.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    passwords: MutableList<PasswordEntry>,
    masterPassword: String,
    onLogout: () -> Unit,
    onChangePassword: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var entryToEdit by remember { mutableStateOf<PasswordEntry?>(null) }
    var showImportExportDialog by remember { mutableStateOf(false) }
    var showAddTagDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedTags by remember { mutableStateOf<Set<String>>(emptySet()) }
    var tagsRefreshTrigger by remember { mutableIntStateOf(0) }

    val scope = rememberCoroutineScope()

    val allTags = remember(passwords, tagsRefreshTrigger) {
        data.DatabaseManager.getAllTags()
    }

    val filteredPasswords = passwords.filter { entry ->
        val matchesSearch = entry.name.contains(searchQuery, ignoreCase = true) ||
                entry.login.contains(searchQuery, ignoreCase = true) ||
                entry.url.contains(searchQuery, ignoreCase = true)
        val matchesTags = selectedTags.isEmpty() || selectedTags.all { tag ->
            entry.tags.contains(tag)
        }
        matchesSearch && matchesTags
    }

    fun refreshTags() {
        tagsRefreshTrigger++
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Боковая панель
            Surface(
                color = Color.White,
                modifier = Modifier
                    .width(250.dp)
                    .fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = AccentColor,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Lock, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Менеджер", fontWeight = FontWeight.Bold)
                            Text("Всего: ${passwords.size}", fontSize = 12.sp, color = Color.Gray)
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Теги", fontWeight = FontWeight.SemiBold)

                            if (selectedTags.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            selectedTags.forEach { tag ->
                                                data.DatabaseManager.deleteTag(tag)
                                            }
                                            passwords.forEachIndexed { index, entry ->
                                                val updatedTags = entry.tags.filterNot { it in selectedTags }
                                                if (updatedTags != entry.tags) {
                                                    val updatedEntry = entry.copy(tags = updatedTags)
                                                    passwords[index] = updatedEntry
                                                    data.DatabaseManager.savePassword(updatedEntry)
                                                }
                                            }
                                            selectedTags = emptySet()
                                            refreshTags()
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Delete, null, tint = Color(0xFFEF4444))
                                }
                            } else {
                                IconButton(
                                    onClick = { showAddTagDialog = true },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Add, null, tint = AccentColor)
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = if (selectedTags.isEmpty()) Color.Black else Color(0xFFF3F4F6),
                                shape = RoundedCornerShape(16.dp),
                                onClick = { selectedTags = emptySet() }
                            ) {
                                Text(
                                    "Все",
                                    color = if (selectedTags.isEmpty()) Color.White else Color.Black,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }

                            allTags.forEach { tag ->
                                val isSelected = tag in selectedTags
                                Surface(
                                    color = if (isSelected) Color.Black else Color(0xFFF3F4F6),
                                    shape = RoundedCornerShape(16.dp),
                                    onClick = {
                                        selectedTags = if (isSelected) {
                                            selectedTags - tag
                                        } else {
                                            selectedTags + tag
                                        }
                                    }
                                ) {
                                    Text(
                                        tag,
                                        color = if (isSelected) Color.White else Color.Black,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B7280))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("Выйти", color = Color.White)
                    }
                }
            }

            // Основной контент
            Column(modifier = Modifier.weight(1f).padding(24.dp)) {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Search, null, tint = Color.Gray)
                        Spacer(Modifier.width(8.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) Text("Поиск", color = Color.Gray)
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                textStyle = TextStyle(color = TextColor, fontSize = 16.sp),
                                cursorBrush = SolidColor(AccentColor),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        OutlinedButton(onClick = onChangePassword) {
                            Icon(Icons.Default.Key, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Сменить пароль")
                        }

                        Spacer(Modifier.width(12.dp))

                        OutlinedButton(onClick = { showImportExportDialog = true }) {
                            Icon(Icons.Default.ImportExport, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Импорт/Экспорт")
                        }

                        Spacer(Modifier.width(12.dp))

                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                        ) {
                            Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Добавить пароль", color = Color.White)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                if (filteredPasswords.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Lock, null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
                            Spacer(Modifier.height(16.dp))
                            Text("Пароли не найдены", color = Color.Gray)
                            if (searchQuery.isNotEmpty() || selectedTags.isNotEmpty()) {
                                Text("Измените параметры поиска", fontSize = 14.sp, color = Color.LightGray)
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredPasswords, key = { it.id }) { entry ->
                            PasswordCard(
                                entry = entry,
                                masterPassword = masterPassword,
                                onEdit = { entryToEdit = entry },
                                onDelete = {
                                    passwords.remove(entry)
                                    data.DatabaseManager.deletePassword(entry.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        PasswordDialog(
            onDismiss = { showAddDialog = false },
            onSave = { newEntry ->
                val encryptedEntry = newEntry.copy(
                    passwordEncrypted = SecurityUtils.encrypt(newEntry.passwordEncrypted, masterPassword)
                )
                passwords.add(encryptedEntry)
                data.DatabaseManager.savePassword(encryptedEntry)
                showAddDialog = false
                refreshTags()
            },
            masterPassword = masterPassword
        )
    }

    if (entryToEdit != null) {
        PasswordDialog(
            existingEntry = entryToEdit,
            masterPassword = masterPassword,
            onDismiss = { entryToEdit = null },
            onSave = { updatedEntry ->
                val index = passwords.indexOfFirst { it.id == updatedEntry.id }
                if (index != -1) {
                    val encryptedEntry = updatedEntry.copy(
                        passwordEncrypted = SecurityUtils.encrypt(updatedEntry.passwordEncrypted, masterPassword)
                    )
                    passwords[index] = encryptedEntry
                    data.DatabaseManager.savePassword(encryptedEntry)
                }
                entryToEdit = null
                refreshTags()
            }
        )
    }

    if (showImportExportDialog) {
        ImportExportDialog(
            onDismiss = { showImportExportDialog = false },
            passwords = passwords,
            masterPassword = masterPassword,
            onImport = { importedPasswords ->
                importedPasswords.forEach { imported ->
                    imported.tags.forEach { tag ->
                        data.DatabaseManager.saveTag(tag)
                    }

                    val existingIndex = passwords.indexOfFirst { it.id == imported.id }
                    if (existingIndex != -1) {
                        passwords[existingIndex] = imported
                        data.DatabaseManager.savePassword(imported)
                    } else {
                        passwords.add(imported)
                        data.DatabaseManager.savePassword(imported)
                    }
                }
                refreshTags()
            }
        )
    }

    if (showAddTagDialog) {
        AddTagDialog(
            onDismiss = { showAddTagDialog = false },
            onConfirm = { newTag ->
                scope.launch {
                    data.DatabaseManager.saveTag(newTag)
                    refreshTags()
                    showAddTagDialog = false
                }
            }
        )
    }
}