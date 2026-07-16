package com.ertan.projecrmanagerapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ertan.projecrmanagerapp.ui.theme.*

@Composable
fun PriorityBadge(priority: String, modifier: Modifier = Modifier) {
    val (bg, text, label) = when (priority) {
        "High" -> Triple(PriorityHighBg, PriorityHighText, "High priority")
        "Low" -> Triple(PriorityLowBg, PriorityLowText, "Low priority")
        else -> Triple(PriorityMediumBg, PriorityMediumText, "Medium priority")
    }

    Text(
        text = label,
        color = text,
        style = MaterialTheme.typography.labelSmall,
        modifier = modifier
            .background(bg, shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}