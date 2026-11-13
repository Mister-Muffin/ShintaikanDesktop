package composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance

@Composable
fun BeltIcon(
    beltColor: List<Color>,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier.aspectRatio(50f / 31f)) {
        val w = size.width
        val h = size.height
        val legH = h * 0.35f
        val legY = (h - legH) / 2f
        val corner = CornerRadius(legH / 2.5f)

        // Kontrast-Rand für helle Gürtel (Weiß/Gelb) – löst das Sichtbarkeitsproblem
        val needsBorder = beltColor.first().luminance() > 0.65f
        val border = if (needsBorder) Color.Gray.copy(alpha = 0.75f) else null

        // Gradient for middle knot in case of multicolor belts
        val gradient = Brush.horizontalGradient(
            colors = if (beltColor.size < 2) listOf(beltColor.first(), beltColor.last()) else beltColor,
            startX = w * 0.40f,
            endX = w * 0.60f
        )

        fun drawLeg(left: Float, color: Color) {
            if (border != null) {
                drawRoundRect(
                    border,
                    topLeft = Offset(left, legY),
                    size = Size(w * 0.28f, legH),
                    cornerRadius = corner,
                    style = Stroke(width = h * 0.075f),
                )
            }
            drawRoundRect(
                color = color,
                topLeft = Offset(left, legY),
                size = Size(w * 0.28f, legH),
                cornerRadius = corner,
            )
        }

        // zwei Gurtschenkel
        drawLeg(w * 0.02f, beltColor.first())
        drawLeg(w * 0.70f, beltColor.last())

        // Knoten in der Mitte
        if (border != null) {
            drawRoundRect(
                border,
                topLeft = Offset(w * 0.30f, h * 0.25f),
                size = Size(w * 0.40f, h * 0.50f),
                cornerRadius = CornerRadius(h * 0.13f),
                style = Stroke(width = h * 0.075f),
            )
        }
        drawRoundRect(
            gradient,
            topLeft = Offset(w * 0.30f, h * 0.25f),
            size = Size(w * 0.40f, h * 0.50f),
            cornerRadius = CornerRadius(h * 0.13f),
        )
        // "Loch" im Knoten
        drawCircle(
            color = if (needsBorder) Color.Gray.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.4f),
            radius = h * 0.08f,
            center = Offset(w * 0.5f, h * 0.5f),
        )
    }
}
