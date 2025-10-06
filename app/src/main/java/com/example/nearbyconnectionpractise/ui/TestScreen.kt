package com.example.nearbyconnectionpractise.ui

import android.graphics.BlurMaskFilter
import android.graphics.LinearGradient
import android.graphics.Shader
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TestScreen() {

    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        animationProgress.animateTo(1f, tween(1000))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Canvas (modifier = Modifier.fillMaxSize())  {

            val strokeWidth = 8.dp.toPx()
            val radius = size.minDimension / 2 - strokeWidth

            // Create a rotating sweep gradient
            val brush = Brush.verticalGradient(
                colors = listOf(Color.Cyan, Color.Magenta, Color.Blue, Color.Cyan),
            )


            // Add a blurred outer glow
            drawIntoCanvas {
                val paint = Paint().asFrameworkPaint()
                paint.shader = LinearGradient(
                    0f,0f,
                    0f,size.height,
                    intArrayOf(
                        android.graphics.Color.CYAN,
                        android.graphics.Color.MAGENTA,
                        android.graphics.Color.BLUE,
//                        android.graphics.Color.CYAN
                    ),
                    null,
                    Shader.TileMode.CLAMP
                )
                paint.style = android.graphics.Paint.Style.STROKE
                paint.strokeWidth = strokeWidth + 24.dp.toPx()
                paint.maskFilter = BlurMaskFilter(65f, BlurMaskFilter.Blur.NORMAL)
                it.nativeCanvas.drawLine(
                    0F+(strokeWidth/2),
                    0F+(strokeWidth/2),
                    0F+(strokeWidth/2),
                    ((size.height-(strokeWidth/2))*animationProgress.value),
                    paint
                )
            }
        }

        Text(
            text = "Hello!",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
)
@Composable
fun PreviewTestScreen(){
    TestScreen()
}
