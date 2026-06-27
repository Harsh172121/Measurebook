package com.example.util

import androidx.compose.ui.graphics.Color

object AvatarUtil {
    private val avatarColors = listOf(
        Color(0xFF9F1239), // Rose Velvet
        Color(0xFF134E4A), // Emerald Sage
        Color(0xFF1E3A8A), // Indigo Silk
        Color(0xFF7C2D12), // Terracotta
        Color(0xFF581C87)  // Deep Mulberry
    )

    fun getColorForName(name: String): Color {
        if (name.isBlank()) return avatarColors[0]
        val hash = name.hashCode()
        val index = kotlin.math.abs(hash) % avatarColors.size
        return avatarColors[index]
    }

    fun getInitials(name: String): String {
        val parts = name.trim().split("\\s+".toRegex())
        return if (parts.isEmpty()) {
            ""
        } else if (parts.size == 1) {
            parts[0].take(1).uppercase()
        } else {
            (parts[0].take(1) + parts[1].take(1)).uppercase()
        }
    }
}
