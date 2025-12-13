package org.example.project.ui.screens

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.example.project.model.PasswordEntry
import org.example.project.ui.components.PasswordCard
import org.example.project.ui.components.SidebarItem
import org.example.project.ui.components.TagChip
import org.example.project.ui.dialogs.FolderDialog
import org.example.project.ui.dialogs.PasswordDialog
import org.example.project.ui.dialogs.ImportExportDialog
import org.example.project.ui.theme.AccentColor
import org.example.project.ui.theme.BgColor
import org.example.project.ui.theme.PrimaryColor
import org.example.project.ui.theme.TextColor
import org.example.project.ui.theme.WeakColor
import org.example.project.utils.SecurityUtils

@OptIn(ExperimentalLayoutApi::class, ExperimentalComposeUiApi::class)
@Composable
fun DashboardScreen(
    passwords: MutableList<PasswordEntry>,
    folders: MutableList<String>,
    masterPassword: String,
    onLogout: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var entryToEdit by remember { mutableStateOf<PasswordEntry?>(null) }
    var showImportExportDialog by remember { mutableStateOf(false) }

    var showFolderDialog by remember { mutableStateOf(false) }
    var folderToRename by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf<String?>(null) }
    var selectedFolder by remember { mutableStateOf<String?>(null) }

    var draggedEntry by remember { mutableStateOf<PasswordEntry?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var draggedEntryInitialBounds by remember { mutableStateOf<Rect?>(null) }
    var dragStartLocalOffset by remember { mutableStateOf(Offset.Zero) }
    var currentCursorPosition by remember { mutableStateOf(Offset.Zero) }

    val folderBounds = remember { mutableStateMapOf<String, Rect>() }
    var hoveredFolder by remember { mutableStateOf<String?>(null) }
    val cardBounds = remember { mutableStateMapOf<String, Rect>() }

    val sidebarScrollState = rememberScrollState()
    var sidebarBounds by remember { mutableStateOf<Rect?>(null) }

    val scope = rememberCoroutineScope()
    val folderCounts = remember(passwords.toList()) { passwords.groupingBy { it.folder }.eachCount() }
    val allTags = remember(passwords.toList()) { passwords.flatMap { it.tags }.toSet().sorted() }
    val density = LocalDensity.current

    val filteredPasswords = passwords.filter { entry ->
        val matchesSearch = entry.name.contains(searchQuery, ignoreCase = true) ||
                entry.login.contains(searchQuery, ignoreCase = true) ||
                entry.url.contains(searchQuery, ignoreCase = true)
        val matchesTag = selectedTag == null || entry.tags.contains(selectedTag)
        val matchesFolder = selectedFolder == null || entry.folder == selectedFolder
        matchesSearch && matchesTag && matchesFolder
    }

    LaunchedEffect(draggedEntry) {
        if (draggedEntry != null) {
            while (true) {
                val bounds = sidebarBounds
                if (bounds != null) {
                    val y = currentCursorPosition.y
                    val scrollThreshold = with(density) { 50.dp.toPx() }
                    val scrollSpeed = 15

                    if (y < bounds.top + scrollThreshold && y > bounds.top) {
                        sidebarScrollState.scrollTo(sidebarScrollState.value - scrollSpeed)
                    } else if (y > bounds.bottom - scrollThreshold && y < bounds.bottom) {
                        sidebarScrollState.scrollTo(sidebarScrollState.value + scrollSpeed)
                    }
                }
                delay(16)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onPointerEvent(PointerEventType.Scroll) { event ->
                if (draggedEntry != null) {
                    val change = event.changes.first()
                    val delta = change.scrollDelta
                    val scrollAmount = (delta.y * 25).toInt()

                    scope.launch {
                        sidebarScrollState.scrollTo(sidebarScrollState.value + scrollAmount)
                    }
                    change.consume()
                }
            }
    ) {
        Row(modifier = Modifier.fillMaxSize().background(BgColor)) {
            Column(
                modifier = Modifier
                    .width(250.dp)
                    .fillMaxHeight()
                    .background(Color.White)
                    .padding(16.dp)
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

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .onGloballyPositioned { coordinates ->
                            sidebarBounds = coordinates.boundsInWindow()
                        }
                        .verticalScroll(sidebarScrollState)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Папки", fontWeight = FontWeight.SemiBold)
                        IconButton(onClick = { showFolderDialog = true }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Add, null, tint = AccentColor)
                        }
                    }

                    SidebarItem("Все (${passwords.size})", selectedFolder == null, onClick = { selectedFolder = null })

                    folders.forEach { folderName ->
                        val count = folderCounts[folderName] ?: 0
                        val isSystemFolder = folderName == "Основная"

                        SidebarItem(
                            text = "$folderName ($count)",
                            isSelected = selectedFolder == folderName,
                            isHovered = hoveredFolder == folderName,
                            onClick = { selectedFolder = folderName },
                            onPositioned = { rect -> folderBounds[folderName] = rect },
                            onEdit = { folderToRename = folderName },
                            onDelete = if (!isSystemFolder) {
                                {
                                    folderBounds.remove(folderName)
                                    folders.remove(folderName)
                                    if (selectedFolder == folderName) selectedFolder = null
                                    passwords.forEachIndexed { index, entry ->
                                        if (entry.folder == folderName) {
                                            passwords[index] = entry.copy(folder = "Основная")
                                        }
                                    }
                                }
                            } else null
                        )
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
                    FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        allTags.forEach { tag ->
                            val isSelected = tag == selectedTag
                            Box(modifier = Modifier.clip(RoundedCornerShape(16.dp)).clickable { selectedTag = if (isSelected) null else tag }) {
                                TagChip(text = tag, bg = if (isSelected) Color.Black else Color(0xFFF3F4F6), content = if (isSelected) Color.White else Color.Black)
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                }

                if (passwords.any { it.isWeak }) {
                    Spacer(Modifier.height(16.dp))
                    Card(border = BorderStroke(1.dp, Color(0xFFFFEBEB)), colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2))) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, null, tint = WeakColor, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Обнаружено слабых паролей.", fontSize = 12.sp, color = WeakColor, lineHeight = 14.sp)
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable { onLogout() }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Выйти", color = TextColor, fontWeight = FontWeight.Medium)
                }
            }

            Column(modifier = Modifier.weight(1f).padding(24.dp)) {
                Row(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(8.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Search, null, tint = Color.Gray); Spacer(Modifier.width(8.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) Text("Поиск по названию, логину или URL...", color = Color.Gray)
                        BasicTextField(value = searchQuery, onValueChange = { searchQuery = it }, textStyle = TextStyle(color = TextColor, fontSize = 16.sp), cursorBrush = SolidColor(AccentColor), modifier = Modifier.fillMaxWidth(), singleLine = true)
                    }
                    OutlinedButton(onClick = { showImportExportDialog = true }) { Icon(Icons.Default.ImportExport, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Импорт/Экспорт") }
                    Spacer(Modifier.width(12.dp))
                    Button(onClick = { showAddDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)) { Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Добавить пароль", color = Color.White) }
                }

                Spacer(Modifier.height(24.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    userScrollEnabled = draggedEntry == null
                ) {
                    items(filteredPasswords, key = { it.id }) { entry ->
                        val isDraggingThis = draggedEntry?.id == entry.id
                        Box(modifier = Modifier.alpha(if (isDraggingThis) 0.3f else 1f)) {
                            PasswordCard(
                                entry = entry,
                                masterPassword = masterPassword,
                                onEdit = { entryToEdit = entry },
                                onDelete = { passwords.remove(entry) },
                                onPositioned = { rect -> cardBounds[entry.id] = rect },
                                onDragStart = { offset ->
                                    draggedEntry = entry
                                    draggedEntryInitialBounds = cardBounds[entry.id]
                                    dragStartLocalOffset = offset
                                    dragOffset = Offset.Zero
                                },
                                onDrag = { dragAmount ->
                                    dragOffset += dragAmount
                                    val initial = draggedEntryInitialBounds
                                    if (initial != null) {
                                        val cursorX = initial.left + dragStartLocalOffset.x + dragOffset.x
                                        val cursorY = initial.top + dragStartLocalOffset.y + dragOffset.y
                                        val cursorPosition = Offset(cursorX, cursorY)

                                        currentCursorPosition = cursorPosition
                                        val target = folderBounds.entries.firstOrNull { (_, rect) -> rect.contains(cursorPosition) }?.key
                                        hoveredFolder = target
                                    }
                                },
                                onDragEnd = {
                                    if (hoveredFolder != null && draggedEntry != null) {
                                        val index = passwords.indexOfFirst { it.id == draggedEntry!!.id }
                                        if (index != -1) {
                                            passwords[index] = passwords[index].copy(folder = hoveredFolder!!)
                                        }
                                    }
                                    draggedEntry = null
                                    hoveredFolder = null
                                    draggedEntryInitialBounds = null
                                }
                            )
                        }
                    }
                }
            }
        }

        if (draggedEntry != null && draggedEntryInitialBounds != null) {
            val density = LocalDensity.current
            val initial = draggedEntryInitialBounds!!
            val cursorX = initial.left + dragStartLocalOffset.x + dragOffset.x
            val cursorY = initial.top + dragStartLocalOffset.y + dragOffset.y
            val ghostLeftDp = with(density) { (cursorX + 20).toDp() }
            val ghostTopDp = with(density) { (cursorY + 20).toDp() }

            Box(
                modifier = Modifier
                    .offset(ghostLeftDp, ghostTopDp)
                    .width(250.dp)
                    .shadow(16.dp, RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.95f), RoundedCornerShape(12.dp))
                    .border(1.dp, AccentColor, RoundedCornerShape(12.dp))
                    .padding(12.dp)
                    .zIndex(10f)
            ) {
                Column {
                    Text(draggedEntry!!.name, fontWeight = FontWeight.Bold, color = AccentColor, fontSize = 14.sp)
                    Text(draggedEntry!!.login, fontSize = 12.sp, color = Color.Gray, maxLines = 1)
                    if (hoveredFolder != null) {
                        Spacer(Modifier.height(4.dp))
                        Text("Переместить в: $hoveredFolder", fontSize = 11.sp, color = PrimaryColor, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showFolderDialog) {
        FolderDialog(
            onDismiss = { showFolderDialog = false },
            onConfirm = { newName ->
                if (!folders.contains(newName)) {
                    folders.add(newName)
                }
                showFolderDialog = false
            }
        )
    }

    if (folderToRename != null) {
        FolderDialog(
            initialName = folderToRename!!,
            onDismiss = { folderToRename = null },
            onConfirm = { newName ->
                val oldName = folderToRename!!
                if (newName != oldName && !folders.contains(newName)) {
                    folderBounds.remove(oldName)
                    val index = folders.indexOf(oldName)
                    if (index != -1) folders[index] = newName
                    if (selectedFolder == oldName) selectedFolder = newName
                    passwords.forEachIndexed { i, entry ->
                        if (entry.folder == oldName) {
                            passwords[i] = entry.copy(folder = newName)
                        }
                    }
                }
                folderToRename = null
            }
        )
    }

    if (showAddDialog) { PasswordDialog(onDismiss = { showAddDialog = false }, onSave = { newEntry -> val encryptedEntry = newEntry.copy(passwordEncrypted = SecurityUtils.encrypt(newEntry.passwordEncrypted, masterPassword)); passwords.add(encryptedEntry); showAddDialog = false }, masterPassword = masterPassword) }
    if (entryToEdit != null) { PasswordDialog(existingEntry = entryToEdit, masterPassword = masterPassword, onDismiss = { entryToEdit = null }, onSave = { updatedEntry -> val index = passwords.indexOfFirst { it.id == updatedEntry.id }; if (index != -1) { val encryptedEntry = updatedEntry.copy(passwordEncrypted = SecurityUtils.encrypt(updatedEntry.passwordEncrypted, masterPassword)); passwords[index] = encryptedEntry }; entryToEdit = null }) }
    if (showImportExportDialog) { ImportExportDialog(onDismiss = { showImportExportDialog = false }) }
}