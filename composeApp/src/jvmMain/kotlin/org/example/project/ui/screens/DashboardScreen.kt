package org.example.project.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.model.PasswordEntry
import org.example.project.ui.components.PasswordCard
import org.example.project.ui.components.SidebarItem
import org.example.project.ui.components.TagChip
import org.example.project.ui.dialogs.PasswordDialog
import org.example.project.ui.dialogs.ImportExportDialog
import org.example.project.ui.theme.AccentColor
import org.example.project.ui.theme.BgColor
import org.example.project.ui.theme.PrimaryColor
import org.example.project.ui.theme.TextColor
import org.example.project.ui.theme.WeakColor
import org.example.project.utils.SecurityUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    passwords: MutableList<PasswordEntry>,
    masterPassword: String,
    onLogout: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var entryToEdit by remember { mutableStateOf<PasswordEntry?>(null) }
    var showImportExportDialog by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf<String?>(null) }
    var selectedFolder by remember { mutableStateOf<String?>(null) }

    val allTags = remember(passwords.toList()) {
        passwords.flatMap { it.tags }.toSet().sorted()
    }

    val filteredPasswords = passwords.filter { entry ->
        val matchesSearch = entry.name.contains(searchQuery, ignoreCase = true) ||
                entry.login.contains(searchQuery, ignoreCase = true) ||
                entry.url.contains(searchQuery, ignoreCase = true)

        val matchesTag = selectedTag == null || entry.tags.contains(selectedTag)

        val matchesFolder = selectedFolder == null || entry.folder == selectedFolder

        matchesSearch && matchesTag && matchesFolder
    }

    Row(modifier = Modifier.fillMaxSize().background(BgColor)) {
        Column(
            modifier = Modifier.width(250.dp).fillMaxHeight().background(Color.White).padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(32.dp).background(AccentColor, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Lock, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Менеджер", fontWeight = FontWeight.Bold)
                    Text("Всего записей: ${passwords.size}", fontSize = 12.sp, color = Color.Gray)
                }
            }
            Spacer(Modifier.height(32.dp))

            Text("Папки", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))

            Box(modifier = Modifier.clickable { selectedFolder = null }) {
                SidebarItem("Все (${passwords.size})", selectedFolder == null)
            }
            Box(modifier = Modifier.clickable { selectedFolder = "Основная" }) {
                SidebarItem("Основная", selectedFolder == "Основная")
            }
            Box(modifier = Modifier.clickable { selectedFolder = "Работа" }) {
                SidebarItem("Работа", selectedFolder == "Работа")
            }
            Box(modifier = Modifier.clickable { selectedFolder = "Финансы" }) {
                SidebarItem("Финансы", selectedFolder == "Финансы")
            }

            Spacer(Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Теги", fontWeight = FontWeight.SemiBold)
                if (selectedTag != null) {
                    IconButton(onClick = { selectedTag = null }, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Default.Close, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    }
                }
            }
            Spacer(Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allTags.forEach { tag ->
                    val isSelected = tag == selectedTag

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { selectedTag = if (isSelected) null else tag }
                    ) {
                        TagChip(
                            text = tag,
                            bg = if (isSelected) Color.Black else Color(0xFFF3F4F6),
                            content = if (isSelected) Color.White else Color.Black
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            if (passwords.any { it.isWeak }) {
                Card(
                    border = BorderStroke(1.dp, Color(0xFFFFEBEB)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, null, tint = WeakColor, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Обнаружено слабых паролей.", fontSize = 12.sp, color = WeakColor, lineHeight = 14.sp)
                    }
                }
            }
        }

        Column(modifier = Modifier.weight(1f).padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(8.dp)).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, null, tint = Color.Gray)
                Spacer(Modifier.width(8.dp))

                Box(modifier = Modifier.weight(1f)) {
                    if (searchQuery.isEmpty()) {
                        Text("Поиск по названию, логину или URL...", color = Color.Gray)
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        textStyle = TextStyle(color = TextColor, fontSize = 16.sp),
                        cursorBrush = SolidColor(AccentColor),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

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

            Spacer(Modifier.height(24.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(filteredPasswords) { entry ->
                    PasswordCard(
                        entry = entry,
                        masterPassword = masterPassword,
                        onEdit = { entryToEdit = entry },
                        onDelete = { passwords.remove(entry) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        PasswordDialog(
            masterPassword = masterPassword,
            onDismiss = { showAddDialog = false },
            onSave = { newEntry ->
                val encryptedEntry = newEntry.copy(passwordEncrypted = SecurityUtils.encrypt(newEntry.passwordEncrypted, masterPassword))
                passwords.add(encryptedEntry)
                showAddDialog = false
            }
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
                    val encryptedEntry = updatedEntry.copy(passwordEncrypted = SecurityUtils.encrypt(updatedEntry.passwordEncrypted, masterPassword))
                    passwords[index] = encryptedEntry
                }
                entryToEdit = null
            }
        )
    }

    if (showImportExportDialog) {
        ImportExportDialog(onDismiss = { showImportExportDialog = false })
    }
}