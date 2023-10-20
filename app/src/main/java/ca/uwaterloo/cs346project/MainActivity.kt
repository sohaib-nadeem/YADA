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
import androidx.compose.material.icons.filled.LineWeight
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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

// Contains attributes for pen and eraser as well as the drawing mode
data class DrawInfo (
    val drawMode: DrawMode = DrawMode.Pen,
    val color: Color = Color.Black,
    val strokeWidth: Float = 2f
)

const val MAX_STROKE_WIDTH = 140f

enum class DrawMode { Pen, Eraser, Shape, LineWeight, ColorPicker }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var drawInfo by remember { mutableStateOf(DrawInfo()) }

            CS346ProjectTheme {
                Box() {
                    Row(modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.TopCenter)

                    ) {
                        Whiteboard(drawInfo)
                    }

                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                    ) {
                        Toolbar(drawInfo = drawInfo, setDrawInfo = { drawInfo = it })
                    }

                    //println(pen.value.strokeWidth.toString())
                }
            }
        }
    }
}

@Composable
fun Whiteboard(drawInfo: DrawInfo) {
    val lines = remember { mutableStateListOf<Line>() }
    val canvasColor = Color.White
    var cachedDrawInfo by remember { mutableStateOf(DrawInfo()) }
    cachedDrawInfo = drawInfo

    Canvas(modifier = Modifier
        .fillMaxSize()
        .background(canvasColor) // Default: White
        .pointerInput(Unit) {
            detectDragGestures(onDrag = { change, dragAmount ->
                if (cachedDrawInfo.drawMode == DrawMode.Pen || cachedDrawInfo.drawMode == DrawMode.Eraser) {
                    change.consume()
                    val line = Line(
                        start = change.position - dragAmount,
                        end = change.position,
                        color = if (cachedDrawInfo.drawMode == DrawMode.Pen) cachedDrawInfo.color else canvasColor,
                        strokeWidth = cachedDrawInfo.strokeWidth.dp
                    )

                    lines.add(line)
                }
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
fun Toolbar(drawInfo: DrawInfo, setDrawInfo: (DrawInfo) -> Unit) {
    Column(modifier = Modifier
        .background(MaterialTheme.colorScheme.secondaryContainer)
    ) {
        if (drawInfo.drawMode == DrawMode.LineWeight || drawInfo.drawMode == DrawMode.ColorPicker) {
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (drawInfo.drawMode == DrawMode.LineWeight) {
                    Slider(
                        value = drawInfo.strokeWidth,
                        onValueChange = {
                            setDrawInfo(drawInfo.copy(strokeWidth = it))
                        },
                        valueRange = 1f..MAX_STROKE_WIDTH
                    )
                }

                // penColorOn.value
                if (drawInfo.drawMode == DrawMode.ColorPicker) {
                    val colors = listOf<Color>(Color.Black, Color.Gray, Color.Red,
                            Color.Yellow, Color.Green, Color.Blue, Color.Cyan, Color.Magenta)
                    colors.forEach { color ->
                        Button(
                            onClick = { setDrawInfo(drawInfo.copy(color = color)) },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(color),
                            modifier = Modifier.size(25.dp)
                        ) {}
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            //Spacer(Modifier.weight(1f, true))
            IconButton(
                onClick = { setDrawInfo(drawInfo.copy(drawMode = DrawMode.Pen)) }
            ) {
                Icon(Icons.Filled.Create, contentDescription = "Localized description", modifier = Modifier
                    .background(
                        color = if (drawInfo.drawMode == DrawMode.Pen) Color.LightGray else MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape,
                    )
                    .padding(5.dp)
                )
            }

            EraserToggleButton(drawInfo.drawMode == DrawMode.Eraser) {
                setDrawInfo(drawInfo.copy(drawMode = DrawMode.Eraser))
            }

            IconButton(onClick = { setDrawInfo(drawInfo.copy(drawMode = DrawMode.LineWeight)) }) {
                Icon(Icons.Filled.LineWeight, contentDescription = "Localized description", modifier = Modifier
                    .background(
                        color = if (drawInfo.drawMode == DrawMode.LineWeight) Color.LightGray else MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape,
                    )
                    .padding(5.dp)
                )
            }

            IconButton(onClick = { setDrawInfo(drawInfo.copy(drawMode = DrawMode.ColorPicker)) }) {
                Icon(Icons.Filled.Palette, contentDescription = "Localized description", modifier = Modifier
                    .background(
                        color = if (drawInfo.drawMode == DrawMode.ColorPicker) Color.LightGray else MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape,
                    )
                    .padding(5.dp)
                )
            }
        }
    }
}
