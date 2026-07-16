package com.ertan.projecrmanagerapp.ui.theme

import androidx.compose.ui.graphics.Color

// Dark theme background layers
val DarkBackground = Color(0xFF121317)
val DarkSurface = Color(0xFF1C1D23)
val DarkSurfaceElevated = Color(0xFF25262E)
val DarkBorder = Color(0xFF2E2F38)

// Primary accent (blue, matches reference design)
val AccentBlue = Color(0xFF5B8DEF)
val AccentBlueLight = Color(0xFF8CB0F5)

// Priority badge colors (background + text pairs, matching pill style)
val PriorityHighBg = Color(0xFF3D1F24)
val PriorityHighText = Color(0xFFF06A6A)
val PriorityMediumBg = Color(0xFF3D2E1A)
val PriorityMediumText = Color(0xFFE8A94D)
val PriorityLowBg = Color(0xFF1C3327)
val PriorityLowText = Color(0xFF5FBF8A)

// Status colors
val StatusTodoBg = Color(0xFF1A2A45)
val StatusTodoText = Color(0xFF6FA0F0)
val StatusDoneBg = Color(0xFF1C3327)
val StatusDoneText = Color(0xFF5FBF8A)

// Avatar background palette (cycled by user id for variety)
val AvatarColors = listOf(
    Color(0xFF5B8DEF), // blue
    Color(0xFF4DB380), // green
    Color(0xFF9B6FE0), // purple
    Color(0xFFE0916F), // orange
    Color(0xFFE05F87), // pink
    Color(0xFF5FBFBF)  // teal
)

// Text colors
val TextPrimary = Color(0xFFF2F3F5)
val TextSecondary = Color(0xFF9CA0AA)
val TextMuted = Color(0xFF6B6E78)

val ErrorColor = Color(0xFFE05F5F)