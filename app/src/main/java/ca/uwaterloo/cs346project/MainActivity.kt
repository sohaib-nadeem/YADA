package ca.uwaterloo.cs346project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ca.uwaterloo.cs346project.ui.theme.CS346ProjectTheme

// Line attributes
data class Line (
    val start: Offset,
    val end: Offset,
    val color: Color,
    val strokeWidth: Dp
)

// Pen attributes
data class Pen (
    val color: Color = Color.Black,
    val strokeWidth: Float = 2f
)

// Eraser attributes
data class Eraser (
    val active: Boolean = false,
    val strokeWidth: Float = 6f
)

const val MAX_STROKE_WIDTH = 140f

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val pen = remember { mutableStateOf(Pen()) }
            var eraser by remember { mutableStateOf(Eraser()) }

            CS346ProjectTheme {
                Column() {
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.9f)
                    ) {
                        Whiteboard(pen.value, eraser)
                    }
                    Row(modifier = Modifier
                        .fillMaxWidth()
                    ) {
                        Toolbar(pen = pen.value, setPen = { pen.value = it }, eraser, { eraser = it })
                    }
                    //println(pen.value.strokeWidth.toString())
                }
            }
        }
    }
}

@Composable
fun Whiteboard(pen: Pen, eraser: Eraser) {
    val lines = remember { mutableStateListOf<Line>() }
    val canvasColor = MaterialTheme.colorScheme.background
    var cachedEraser by remember { mutableStateOf(Eraser()) }
    cachedEraser = eraser
    val cachedPen = remember { mutableStateOf(Pen()) } // why do we even need to do this???
    cachedPen.value = pen

    Canvas(modifier = Modifier
        .fillMaxSize()
        .background(canvasColor) // Default: White
        .pointerInput(Unit) {
            detectDragGestures(onDrag = { change, dragAmount ->
                change.consume()
                val line = Line(
                    start = change.position - dragAmount,
                    end = change.position,
                    color = if (cachedEraser.active) canvasColor else cachedPen.value.color,
                    strokeWidth = if (cachedEraser.active) cachedEraser.strokeWidth.dp else cachedPen.value.strokeWidth.dp
                )

                lines.add(line)
            })
        }
    ) {
        lines.forEach { line ->
            drawLine(
                color = line.color,
                start = line.start,
                end = line.end,
                strokeWidth = line.strokeWidth.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun EraserToggleButton(eraserActive: Boolean, onToggle: () -> Unit) {
    Button(
        onClick = { onToggle() },
        modifier = Modifier
            .padding(8.dp)
            .size(27.dp)
            .background(
                color = if (eraserActive) Color.LightGray else Color.White,
                shape = CircleShape
            )
    ) {}
}

@Composable
fun Toolbar(pen: Pen, setPen: (Pen) -> Unit, eraser: Eraser, setEraser: (Eraser) -> Unit) {
    var penThicknessSliderOn = remember { mutableStateOf(false) }
    Row(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.primaryContainer),
        verticalAlignment = Alignment.CenterVertically
    ) {
        //Spacer(Modifier.weight(1f, true))

        IconButton(onClick = { penThicknessSliderOn.value = !penThicknessSliderOn.value }) {
            Icon(Icons.Filled.Create, contentDescription = "Localized description")
        }
        if (penThicknessSliderOn.value) {
            Slider(value = pen.strokeWidth, onValueChange = { setPen(Pen(pen.color, it)) }, valueRange = 1f..MAX_STROKE_WIDTH)
        }

        EraserToggleButton(eraser.active) {
            setEraser(Eraser(!eraser.active, eraser.strokeWidth))
        }
        if (eraser.active) {
            Slider(value = eraser.strokeWidth, onValueChange = { setEraser(Eraser(eraser.active, it)) }, valueRange = 1f..MAX_STROKE_WIDTH)
        }
    }
}
