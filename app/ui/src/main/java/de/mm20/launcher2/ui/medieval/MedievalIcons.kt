package de.mm20.launcher2.ui.medieval

/** SVG path data copied from docs/design/C5.html (24 x 24 view box). */
internal object MedievalIcons {
    val Wifi = listOf("M3 9a14 14 0 0 1 18 0M6 13a9 9 0 0 1 12 0M9.5 17a4 4 0 0 1 5 0")
    val Bluetooth = listOf("M7 7l10 10-5 5V2l5 5L7 17")
    val Hushed = listOf("M4 9v6h4l5 4V5L8 9zM17 9l4 6M21 9l-4 6")
    val Moon = listOf("M17 3a9 9 0 1 0 4 15A9 9 0 0 1 17 3z")
    val Voicemail = listOf(svgCircle(7f, 12f, 4f), svgCircle(17f, 12f, 4f), "M7 16h10")
    val Flame = listOf("M12 3c1 3 5 5 5 10a5 5 0 0 1-10 0c0-2 1-3 2-4 0 2 1 3 2 3 0-3-1-5 1-9z")
    val Quill = listOf(
        "M20 4c-9 0-14 5-14 12l-2 4 4-2c7 0 12-5 12-14z",
        "M8 16c3-4 6-7 9-9",
    )
    val Bird = listOf("M3 14c3-1 5-3 6-6 2 1 4 1 6 0l5 3-4 1c0 4-3 7-8 7H6l2-3-5-2z")
    val Mug = listOf("M6 7h9v10a3 3 0 0 1-3 3H9a3 3 0 0 1-3-3V7z", "M15 9h2a2 2 0 0 1 0 5h-2")
    val Play = listOf(svgCircle(12f, 12f, 9f), "M10 8.5l5 3.5-5 3.5z")
    val Letter = listOf("M4 6h16v12H4z", "M4 7l8 6 8-6")
    val Phone = listOf("M5 4h4l2 5-2.5 1.5a11 11 0 0 0 5 5L15 13l5 2v4a1 1 0 0 1-1 1A15 15 0 0 1 4 5a1 1 0 0 1 1-1z")
    val Camera = listOf("M3 8h4l2-3h6l2 3h4v11H3z", svgCircle(12f, 13f, 3.5f))
    val Compass = listOf(svgCircle(12f, 12f, 9f), "M15.5 8.5l-2 5-5 2 2-5z")
    val Anvil = listOf("M12 3v18M7 8h10M9 21h6")
}
