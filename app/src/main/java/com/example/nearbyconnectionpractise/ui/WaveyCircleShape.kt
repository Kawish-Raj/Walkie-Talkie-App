package com.example.nearbyconnectionpractise.ui

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.graphics.PathParser
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path

class WaveyCircleShape: Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        // Parse the path from SVG
        val originalPath = PathParser.createPathFromPathData(
            "M142.87,15.01C152.67,6.42 167.33,6.42 177.13,15.01V15.01C184.04,21.07 193.63,23.02 202.36,20.16L204.32,19.52C216.12,15.65 228.93,21.32 234,32.65V32.65C237.66,40.81 245.51,46.3 254.44,46.93L257.42,47.14C269.42,47.97 278.61,58.14 278.23,70.16V70.16C277.95,78.91 282.81,87.02 290.66,90.91L292.41,91.78C303.25,97.15 307.56,110.4 301.95,121.13V121.13C297.9,128.88 298.95,138.35 304.58,145.04V145.04C312.5,154.48 311.06,168.66 301.35,176.24L300.68,176.76C293.8,182.13 290.79,191.08 293.02,199.52V199.52C296.09,211.12 289.2,223.02 277.61,226.15L274.99,226.85C266.46,229.15 259.98,236.09 258.26,244.76V244.76C255.88,256.69 244.7,264.77 232.62,263.28L229.88,262.94C220.88,261.83 211.97,265.64 206.56,272.92V272.92C198.96,283.13 184.96,286.08 173.89,279.82L173,279.31C164.93,274.74 155.07,274.74 147,279.31L146.11,279.82C135.04,286.08 121.04,283.13 113.44,272.92V272.92C108.03,265.64 99.12,261.83 90.12,262.94L87.38,263.28C75.3,264.77 64.12,256.69 61.75,244.76V244.76C60.02,236.09 53.53,229.15 45.01,226.85L42.39,226.15C30.8,223.02 23.91,211.12 26.98,199.52V199.52C29.21,191.08 26.19,182.13 19.32,176.76L18.65,176.24C8.94,168.66 7.5,154.48 15.42,145.04V145.04C21.05,138.35 22.1,128.88 18.05,121.13V121.13C12.44,110.4 16.75,97.15 27.59,91.78L29.34,90.91C37.19,87.02 42.04,78.91 41.77,70.16V70.16C41.39,58.14 50.58,47.97 62.58,47.14L65.56,46.93C74.49,46.3 82.34,40.81 86,32.65V32.65C91.07,21.32 103.88,15.65 115.68,19.52L117.64,20.16C126.37,23.02 135.96,21.07 142.87,15.01V15.01Z"
        ).asComposePath()

        // Compute bounding box of the original path
        val bounds = android.graphics.RectF()
        android.graphics.Path(originalPath.asAndroidPath()).computeBounds(bounds, true)

        val scaleX = size.width / bounds.width()
        val scaleY = size.height / bounds.height()
        val scale = minOf(scaleX, scaleY)

        val offsetX = (size.width - bounds.width() * scale) / 2f - bounds.left * scale
        val offsetY = (size.height - bounds.height() * scale) / 2f - bounds.top * scale

        val matrix = android.graphics.Matrix().apply {
            setScale(scale, scale)
            postTranslate(offsetX, offsetY)
        }

        val androidPath = originalPath.asAndroidPath()
        androidPath.transform(matrix) // scales and translates the path

        val transformPath = androidPath.asComposePath()
        return Outline.Generic(transformPath)
    }
}