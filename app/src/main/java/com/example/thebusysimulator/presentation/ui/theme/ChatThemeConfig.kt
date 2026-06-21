package com.example.thebusysimulator.presentation.ui.theme

import androidx.compose.ui.graphics.Color

enum class ChatPattern {
    DOTS,       // Grid of small dots
    WAVES,      // Horizontal sine waves
    CROSSES,    // Plus/cross grid
    HEXAGONS,   // Hexagonal dot grid
    DIAGONAL,   // Diagonal parallel lines
    CIRCLES,    // Hollow concentric ring stamps
    DIAMONDS,   // Diamond grid
    STARS       // Small 4-point star scatter
}

data class ChatThemeConfig(
    val id: String,
    val displayName: String,
    val bubbleFromMe: Color,
    val bgGradientStart: Color,
    val bgGradientMid: Color,
    val bgGradientEnd: Color,
    val pattern: ChatPattern,
    val patternColor: Color,
    val bubbleFromContact: Color,
    val bubbleFromMeText: Color = Color.White,
    val bubbleFromContactText: Color = Color(0xFFEEEEEE)
)

object ChatThemes {
    val Default = ChatThemeConfig(
        id = "default",
        displayName = "Cosmos",
        bubbleFromMe = Color(0xFF7C6FFF),
        bgGradientStart = Color(0xFF0D0D1A),
        bgGradientMid = Color(0xFF1A1535),
        bgGradientEnd = Color(0xFF0D1B2A),
        pattern = ChatPattern.STARS,
        patternColor = Color(0x14FFFFFF),
        bubbleFromContact = Color(0xFF252040)
    )

    val Sunset = ChatThemeConfig(
        id = "sunset",
        displayName = "Sunset",
        bubbleFromMe = Color(0xFFFF6B35),
        bgGradientStart = Color(0xFF0A0500),
        bgGradientMid = Color(0xFF2C0D00),
        bgGradientEnd = Color(0xFF1A0D1A),
        pattern = ChatPattern.WAVES,
        patternColor = Color(0x10FF6B35),
        bubbleFromContact = Color(0xFF261200)
    )

    val Ocean = ChatThemeConfig(
        id = "ocean",
        displayName = "Ocean",
        bubbleFromMe = Color(0xFF00B4D8),
        bgGradientStart = Color(0xFF03045E),
        bgGradientMid = Color(0xFF023E8A),
        bgGradientEnd = Color(0xFF0A1628),
        pattern = ChatPattern.DOTS,
        patternColor = Color(0x1200B4D8),
        bubbleFromContact = Color(0xFF03267A)
    )

    val Forest = ChatThemeConfig(
        id = "forest",
        displayName = "Forest",
        bubbleFromMe = Color(0xFF52B788),
        bgGradientStart = Color(0xFF020C07),
        bgGradientMid = Color(0xFF0B2818),
        bgGradientEnd = Color(0xFF081C15),
        pattern = ChatPattern.HEXAGONS,
        patternColor = Color(0x0F52B788),
        bubbleFromContact = Color(0xFF0F2E1E)
    )

    val Candy = ChatThemeConfig(
        id = "candy",
        displayName = "Candy",
        bubbleFromMe = Color(0xFFFF2D78),
        bgGradientStart = Color(0xFF1A0020),
        bgGradientMid = Color(0xFF2D0038),
        bgGradientEnd = Color(0xFF0D0020),
        pattern = ChatPattern.CIRCLES,
        patternColor = Color(0x0EFF2D78),
        bubbleFromContact = Color(0xFF300040)
    )

    val Midnight = ChatThemeConfig(
        id = "midnight",
        displayName = "Midnight",
        bubbleFromMe = Color(0xFF9B5DE5),
        bgGradientStart = Color(0xFF02020E),
        bgGradientMid = Color(0xFF0A0520),
        bgGradientEnd = Color(0xFF050210),
        pattern = ChatPattern.DIAGONAL,
        patternColor = Color(0x0C9B5DE5),
        bubbleFromContact = Color(0xFF130828)
    )

    val Rose = ChatThemeConfig(
        id = "rose",
        displayName = "Rose",
        bubbleFromMe = Color(0xFFE63946),
        bgGradientStart = Color(0xFF150006),
        bgGradientMid = Color(0xFF280010),
        bgGradientEnd = Color(0xFF0F0008),
        pattern = ChatPattern.CROSSES,
        patternColor = Color(0x0EE63946),
        bubbleFromContact = Color(0xFF2A0012)
    )

    val Gold = ChatThemeConfig(
        id = "gold",
        displayName = "Gold",
        bubbleFromMe = Color(0xFFF4A300),
        bgGradientStart = Color(0xFF0A0800),
        bgGradientMid = Color(0xFF1E1500),
        bgGradientEnd = Color(0xFF120C00),
        pattern = ChatPattern.DIAMONDS,
        patternColor = Color(0x10F4A300),
        bubbleFromContact = Color(0xFF201600)
    )

    val all = listOf(Default, Sunset, Ocean, Forest, Candy, Midnight, Rose, Gold)

    fun fromId(id: String) = all.find { it.id == id } ?: Default
}
