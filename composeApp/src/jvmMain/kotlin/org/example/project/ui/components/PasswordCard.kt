package org.example.project.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.model.PasswordEntry
import org.example.project.ui.theme.AccentColor
import org.example.project.ui.theme.WeakColor
import org.example.project.utils.SecurityUtils

@Composable
fun PasswordCard(entry: PasswordEntry, masterPassword: String) {
    var isPasswordVisible by remember { mutableStateOf(false) }
    val decryptedPassword = remember(entry.passwordEncrypted, masterPassword) {
        SecurityUtils.decrypt(entry.passwordEncrypted, masterPassword)
    }

    Card(shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(entry.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Логин: ", color = Color.Gray, modifier = Modifier.width(60.dp))
                        Box(Modifier.background(Color(0xFFF3F4F6), RoundedCornerShape(4.dp)).padding(4.dp)) {
                            Text(entry.login, fontFamily = FontFamily.Monospace)
                        }
                    }
                    Spacer(Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Пароль: ", color = Color.Gray, modifier = Modifier.width(60.dp))
                        Box(Modifier.background(Color(0xFFF3F4F6), RoundedCornerShape(4.dp)).padding(4.dp)) {
                            Text(
                                if (isPasswordVisible) decryptedPassword else "••••••••••••",
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }, modifier = Modifier.size(24.dp)) {
                            Icon(if(isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = Color.Gray)
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Row {
                        Text("URL: ", color = Color.Gray, modifier = Modifier.width(60.dp))
                        Text(entry.url, color = AccentColor)
                    }
                }

                Row {
                    IconButton(onClick = {}) { Icon(Icons.Default.Edit, null, tint = Color.Gray) }
                    IconButton(onClick = {}) { Icon(Icons.Default.Delete, null, tint = Color(0xFFEF4444)) }
                }
            }

            Spacer(Modifier.height(16.dp))
            Row {
                entry.tags.forEach { TagChip(it) }
                if(entry.isWeak) {
                    Spacer(Modifier.width(8.dp))
                    Box(Modifier.background(WeakColor, RoundedCornerShape(16.dp)).padding(horizontal = 12.dp, vertical = 4.dp)) {
                        Text("Слабый", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}