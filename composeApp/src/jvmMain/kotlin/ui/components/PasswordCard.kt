package ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import kotlinx.coroutines.launch
import model.PasswordEntry
import ui.theme.AccentColor
import ui.theme.WeakColor
import utils.SecurityUtils
import java.awt.datatransfer.StringSelection

@Composable
fun PasswordCard(
    entry: PasswordEntry,
    masterPassword: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDragStart: (Offset) -> Unit = {},
    onDrag: (Offset) -> Unit = {},
    onDragEnd: () -> Unit = {},
    onPositioned: ((Rect) -> Unit)? = null
) {
    var isPasswordVisible by remember { mutableStateOf(false) }

    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    val decryptedPassword = remember(entry.passwordEncrypted, masterPassword) {
        SecurityUtils.decrypt(entry.passwordEncrypted, masterPassword)
    }

    Card(
        modifier = Modifier
            .onGloballyPositioned { coordinates ->
                val position = coordinates.localToWindow(Offset.Zero)
                val size = coordinates.size.toSize()
                onPositioned?.invoke(Rect(position, size))
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset -> onDragStart(offset) },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount)
                    },
                    onDragEnd = { onDragEnd() },
                    onDragCancel = { onDragEnd() }
                )
            },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(entry.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Логин: ", color = Color.Gray, modifier = Modifier.width(75.dp))
                        Box(Modifier.background(Color(0xFFF3F4F6), RoundedCornerShape(4.dp)).padding(4.dp)) {
                            Text(entry.login, fontFamily = FontFamily.Monospace)
                        }
                        IconButton(
                            onClick = {
                                scope.launch {
                                    val selection = StringSelection(entry.login)
                                    clipboard.setClipEntry(ClipEntry(selection))
                                }
                            },
                            modifier = Modifier.size(32.dp).padding(start = 4.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Пароль: ", color = Color.Gray, modifier = Modifier.width(75.dp))
                        Box(Modifier.background(Color(0xFFF3F4F6), RoundedCornerShape(4.dp)).padding(4.dp)) {
                            Text(
                                if (isPasswordVisible) decryptedPassword else "••••••••••••",
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        IconButton(
                            onClick = { isPasswordVisible = !isPasswordVisible },
                            modifier = Modifier.size(32.dp).padding(start = 4.dp)
                        ) {
                            Icon(if(isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                        }
                        IconButton(
                            onClick = {
                                scope.launch {
                                    val selection = StringSelection(decryptedPassword)
                                    clipboard.setClipEntry(ClipEntry(selection))
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("URL: ", color = Color.Gray, modifier = Modifier.width(75.dp))
                        Text(entry.url, color = AccentColor)
                    }
                }

                Row {
                    IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, null, tint = Color.Gray) }
                    IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = Color(0xFFEF4444)) }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                entry.tags.forEach { TagChip(it) }
                if(entry.isWeak) {
                    Box(Modifier.background(WeakColor, RoundedCornerShape(16.dp)).padding(horizontal = 12.dp, vertical = 4.dp)) {
                        Text("Слабый", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}