package org.example.project.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.project.ui.theme.AccentColor
import org.example.project.ui.theme.TextColor

@Composable
fun SidebarItem(text: String, isSelected: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color(0xFFE0E7FF) else Color.Transparent)
            .clickable { }
            .padding(12.dp)
    ) {
        Text(text, color = if (isSelected) AccentColor else TextColor, fontWeight = if(isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}