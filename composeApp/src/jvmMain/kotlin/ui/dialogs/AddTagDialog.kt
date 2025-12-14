package ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import ui.components.SimpleInput
import ui.theme.PrimaryColor

/**
 * Диалоговое окно для добавления нового тега.
 *
 * @param onDismiss Callback закрытия диалога.
 * @param onConfirm Callback с введенным названием тега.
 */
@Composable
fun AddTagDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var tagName by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.width(400.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(24.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = "Новый тег",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
                }
                Spacer(Modifier.height(16.dp))

                Text("Название тега", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
                SimpleInput(tagName, "Например: работа") { tagName = it }

                Spacer(Modifier.height(24.dp))

                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = onDismiss) { Text("Отмена") }
                    Spacer(Modifier.width(12.dp))
                    Button(
                        onClick = { if (tagName.isNotBlank()) onConfirm(tagName) },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                    ) {
                        Text("Добавить", color = Color.White)
                    }
                }
            }
        }
    }
}