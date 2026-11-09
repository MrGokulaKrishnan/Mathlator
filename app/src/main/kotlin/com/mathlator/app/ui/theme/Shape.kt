package com.mathlator.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val MathlatorShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small      = RoundedCornerShape(8.dp),
    medium     = RoundedCornerShape(16.dp),
    large      = RoundedCornerShape(20.dp),    // cards
    extraLarge = RoundedCornerShape(28.dp),     // bottom sheets
)

/** Pill shape for buttons. */
val PillShape = RoundedCornerShape(50)

/** Circle shape for the equals button. */
val CircleShape = RoundedCornerShape(50)
