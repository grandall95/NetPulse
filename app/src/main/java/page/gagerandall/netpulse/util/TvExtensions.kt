package page.gagerandall.netpulse.util

import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/**
 * Custom Modifier for Android TV to handle focus state visuals.
 * Adds a glowing border and scaling effect when an element receives focus or is hovered.
 * @param isTv Only applies effects if true.
 * @param shape The shape of the focus border.
 * @param focusable If true, adds a focus node. Set false for items that are already clickable.
 */
@Composable
fun Modifier.tvFocusable(
    isTv: Boolean,
    shape: Shape = RoundedCornerShape(28.dp),
    focusable: Boolean = true
): Modifier {
    if (!isTv) return this
    var isFocused by remember { mutableStateOf(false) }
    val base = this
        .onFocusChanged { isFocused = it.isFocused }
        .graphicsLayer {
            scaleX = if (isFocused) 1.02f else 1.0f
            scaleY = if (isFocused) 1.02f else 1.0f
        }
    
    val withFocus = if (focusable) base.focusable() else base
    
    return withFocus.border(
        width = if (isFocused) 2.5.dp else 0.dp,
        color = if (isFocused) MaterialTheme.colorScheme.primary else Color.Transparent,
        shape = shape
    )
}
