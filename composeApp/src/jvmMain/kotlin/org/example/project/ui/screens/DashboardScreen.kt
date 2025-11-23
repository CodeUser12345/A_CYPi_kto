package org.example.project.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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

    val filteredPasswords = passwords.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.login.contains(searchQuery, ignoreCase = true) ||
                it.url.contains(searchQuery, ignoreCase = true)
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
            SidebarItem("Все (${passwords.size})", true)
            SidebarItem("Основная", false)
            SidebarItem("Работа", false)
            SidebarItem("Финансы", false)

            Spacer(Modifier.height(24.dp))
            Text("Теги", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
            Row(modifier = Modifier.wrapContentWidth()) {
                TagChip("важное", Color.Black, Color.White)
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

        // Main Content
        Column(modifier = Modifier.weight(1f).padding(24.dp)) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(8.dp)).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, null, tint = Color.Gray)
                Spacer(Modifier.width(8.dp))

                // 3. Поле поиска вместо простого текста
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