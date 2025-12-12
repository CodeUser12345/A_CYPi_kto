package org.example.project.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.ui.theme.AccentColor
import org.example.project.ui.theme.PrimaryColor

@Composable
fun TagChip(text: String, bg: Color = Color(0xFFF3F4F6), content: Color = Color.Black) {
    Box(modifier = Modifier
        .background(bg, RoundedCornerShape(16.dp))
        .padding(horizontal = 12.dp, vertical = 4.dp)) {
        Text(text, fontSize = 12.sp, color = content)
    }
}

@Composable
fun InputLabel(text: String) {
    Text(text, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp, top = 12.dp))
}

@Composable
fun SimpleInput(value: String, placeholder: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color.LightGray) },
        modifier = Modifier.fillMaxWidth().background(Color(0xFFF9FAFB)),
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AccentColor,
            unfocusedBorderColor = Color.Transparent
        )
    )
}

@Composable
fun CheckOption(text: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange, colors = CheckboxDefaults.colors(checkedColor = PrimaryColor))
        Text(text)
    }
}

@Composable
fun RowScope.TabButton(text: String, isActive: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        elevation = null,
        colors = ButtonDefaults.buttonColors(containerColor = if (isActive) Color.White else Color.Transparent),
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(text, color = if (isActive) Color.Black else Color.Gray)
    }
}