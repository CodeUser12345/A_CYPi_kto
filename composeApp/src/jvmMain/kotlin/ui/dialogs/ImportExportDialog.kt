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
import ui.components.TabButton
import ui.theme.PrimaryColor
import ui.theme.WeakColor

@Composable
fun ImportExportDialog(onDismiss: () -> Unit) {
    var isExport by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.width(500.dp).height(450.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(Modifier.padding(24.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Импорт / Экспорт данных", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
                }
                Text("Экспортируйте или импортируйте ваши пароли", color = Color.Gray)
                Spacer(Modifier.height(20.dp))

                Row(Modifier.fillMaxWidth().background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp)).padding(4.dp)) {
                    TabButton("Экспорт", isExport) { isExport = true }
                    TabButton("Импорт", !isExport) { isExport = false }
                }

                Spacer(Modifier.weight(1f))

                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        if(isExport) Icons.Outlined.Description else Icons.Outlined.UploadFile,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.LightGray
                    )
                    Spacer(Modifier.height(16.dp))

                    if (isExport) {
                        Text("Экспортируйте все ваши пароли в JSON файл.", textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        Text("Внимание: файл будет содержать незашифрованные пароли.", color = WeakColor, fontSize = 12.sp)
                    } else {
                        Text("Импортируйте пароли из JSON файла.", textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        Text("Формат должен соответствовать экспортированному файлу.", fontSize = 12.sp, color = Color.Gray)
                    }
                }

                Spacer(Modifier.weight(1f))

                Button(
                    onClick = { /* Logic for file I/O */ },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                ) {
                    Icon(if(isExport) Icons.Default.Download else Icons.Default.Upload, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if(isExport) "Экспортировать в JSON" else "Выбрать файл", color = Color.White)
                }
            }
        }
    }
}