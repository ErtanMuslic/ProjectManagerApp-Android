package com.ertan.projecrmanagerapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ertan.projecrmanagerapp.ui.theme.AvatarColors

// Generates consistent initials from a full name, e.g. "Maja Nikolić" -> "MN"
fun initialsFromName(name: String): String {
    val parts = name.trim().split(" ").filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts[0].take(2).uppercase()
        else -> (parts[0].take(1) + parts[1].take(1)).uppercase()
    }
}

// Picks a stable color for a given user id, so the same person always gets the same avatar color
fun colorForUserId(userId: Int): Color {
    return AvatarColors[userId % AvatarColors.size]
}

@Composable
fun Avatar(
    name: String,
    userId: Int,
    size: androidx.compose.ui.unit.Dp = 32.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .background(colorForUserId(userId), shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initialsFromName(name),
            color = Color.White,
            style = MaterialTheme.typography.labelSmall
        )
    }
}