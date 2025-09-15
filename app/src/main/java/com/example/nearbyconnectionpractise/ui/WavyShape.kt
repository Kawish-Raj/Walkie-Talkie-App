import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

class WavyShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ) = androidx.compose.ui.graphics.Outline.Generic(
        path = Path().apply {
            val waveHeight = 20f
            val waveLength = 40f

            moveTo(0f, 0f)
            // top edge with waves
            var x = 0f
            while (x < size.width) {
                quadraticTo(
                    x + waveLength / 4, -waveHeight,
                    x + waveLength / 2, 0f
                )
                quadraticTo(
                    x + 3 * waveLength / 4, waveHeight,
                    x + waveLength, 0f
                )
                x += waveLength
            }
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
    )
}
