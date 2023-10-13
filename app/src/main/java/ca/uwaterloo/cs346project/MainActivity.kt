package ca.uwaterloo.cs346project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.*
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.*
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.ColumnScopeInstance.*
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.ui.graphics.RectangleShape
import ca.uwaterloo.cs346project.ui.theme.CS346ProjectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CS346ProjectTheme {
                var eraserActive by remember { mutableStateOf(false) }
                var sliderValue by remember { mutableStateOf(6f) }

                Box(modifier = Modifier.fillMaxSize()) {
                    Whiteboard(eraserActive, sliderValue)
                }
                Column(
                    modifier = Modifier
                        //.align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Toolbar(eraserActive, sliderValue, {it -> eraserActive = it},  {it -> sliderValue = it})
                }
            }
        }
    }


    @Composable
    fun Whiteboard(eraserActive: Boolean, sliderValue: Float) {
        val lines = remember {
            mutableStateListOf<Line>()
        }
        val eraserLines = remember { mutableStateListOf<Line>() }
        val eraserColor = MaterialTheme.colorScheme.background
        var lineValue by remember { mutableStateOf(6f) }

        var cachedEraserActive by remember { mutableStateOf(false) }
        var cachedSliderValue by remember { mutableStateOf(6f) }
        cachedEraserActive = eraserActive
        cachedSliderValue = sliderValue

        Canvas(modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Default: White

            .pointerInput(Unit) {
                detectDragGestures(onDrag = { change, dragAmount ->
                    change.consume()
                    val line = Line(
                        start = change.position - dragAmount,
                        end = change.position,
                        color = if (cachedEraserActive) eraserColor else Color.Black,
                        strokeWidth = if (cachedEraserActive) cachedSliderValue.dp else 6.dp
                    )

                    if (cachedEraserActive) {
                        eraserLines.add(line)
                    } else {
                        lines.add(line)
                    }
                })

            }
        ) {

            lines.forEach { line ->
                drawLine(
                    //color = if (eraserActive) Color.White else Color.Black,
                    color = line.color,
                    start = line.start,
                    end = line.end,
                    strokeWidth = line.strokeWidth.toPx(),
                    cap = StrokeCap.Round

                )
            }

            eraserLines.forEach { line ->
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
    fun Toolbar(eraserActive: Boolean, sliderValue: Float, setEraserActive: (Boolean) -> Unit, setSliderValue: (Float)->Unit) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            EraserToggleButton(eraserActive) {
                setEraserActive(!eraserActive)
            }
            if (eraserActive) {
                Slider(
                    value = sliderValue,
                    onValueChange = { newSize ->
                        setSliderValue(newSize)
                    },
                    valueRange = 1f..140f
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


    // Line attributes
    data class Line(
        val start: Offset,
        val end: Offset,
        val color: Color = Color.Black,
        var strokeWidth: Dp = 6.dp
    )
}




