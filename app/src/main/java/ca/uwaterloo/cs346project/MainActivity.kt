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
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.Rectangle
import androidx.compose.material.icons.outlined.ShapeLine
import androidx.compose.material.icons.outlined.LineWeight
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import ca.uwaterloo.cs346project.ui.theme.CS346ProjectTheme


data class DrawnItem(
    val drawMode: DrawMode = DrawMode.Pen,
    val shape: Shape = Shape.Line,
    val color: Color = Color.Black,
    val strokeWidth: Float = 4f,
    val filled: Boolean = false,
    val start: Offset = Offset(0f, 0f),
    val end: Offset = Offset(0f, 0f)
)

// Contains attributes for pen and eraser as well as the drawing mode
data class DrawInfo (
    val drawMode: DrawMode = DrawMode.Pen,
    val shape: Shape = Shape.Line,
    val color: Color = Color.Black,
    val strokeWidth: Float = 4f
)

const val MAX_STROKE_WIDTH = 140f

enum class DrawMode { Pen, Eraser, Shape, NULL }

enum class Settings { ColorPicker, LineWeight, Shape, NULL }

enum class Shape { Rectangle, Oval, Line, StraightLine }


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var drawInfo by remember { mutableStateOf(DrawInfo()) }
            var setting by remember { mutableStateOf(Settings.NULL) }

            CS346ProjectTheme {
                Box {
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
                        Toolbar(drawInfo = drawInfo, setDrawInfo = { drawInfo = it },
                            setting = setting, setSetting = { setting = it})
                    }
                }
            }
        }
    }
}



// TESTING VERSION: Using Bitmap. (drawback: gaps between lines)
//@Composable
//fun Whiteboard(drawInfo: DrawInfo) {
//    var cachedDrawInfo by remember { mutableStateOf(DrawInfo()) }
//    cachedDrawInfo = drawInfo
//
//    var canvasWidth = ScreenSize.width
//    var canvasHeight = ScreenSize.height
//    var bitmap by remember(canvasWidth, canvasHeight) {
//        mutableStateOf(Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888))
//    }
//    val path = Path()
//
//    Canvas(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color.White)
//            .pointerInput(Unit) {
//                detectDragGestures { change, _ ->
//                    val startX = change.previousPosition.x.toInt()
//                    val startY = change.previousPosition.y.toInt()
//                    val endX = change.position.x.toInt()
//                    val endY = change.position.y.toInt()
//                    val paint = Paint().apply {
//                        color = cachedDrawInfo.color.toArgb()
//                        strokeWidth = cachedDrawInfo.strokeWidth
//                    }
//
//                    if (startX in 0 until canvasWidth && startY in 0 until canvasHeight &&
//                        endX in 0 until canvasWidth && endY in 0 until canvasHeight
//                    ) {
//
//                        val canvas = android.graphics.Canvas(bitmap)
//                        if (cachedDrawInfo.drawMode == DrawMode.Pen) {
//                            paint.color = cachedDrawInfo.color.toArgb()
//                        } else if (cachedDrawInfo.drawMode == DrawMode.Eraser) {
//                            paint.color = Color.White.toArgb()
//                        }
//                        canvas.drawLine(
//                            startX.toFloat(),
//                            startY.toFloat(),
//                            endX.toFloat(),
//                            endY.toFloat(),
//                            paint
//                        )
//                        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
//                    }
//                }
//            }
//    ) {
//        drawIntoCanvas { canvas ->
//            canvas.nativeCanvas.drawBitmap(bitmap, 0f, 0f, null)
//        }
//    }
//}


@Composable
fun Whiteboard(drawInfo: DrawInfo) {
    val drawnItems = remember { mutableStateListOf<DrawnItem>() }

    val canvasColor = Color.White
    var cachedDrawInfo by remember { mutableStateOf(DrawInfo()) }
    cachedDrawInfo = drawInfo

    var tempItem by remember { mutableStateOf<DrawnItem?>(null) }


    Canvas(modifier = Modifier
        .fillMaxSize()
        .background(canvasColor) // Default: White
        .pointerInput(Unit) {
            detectDragGestures(
                onDragEnd = {
                    if (cachedDrawInfo.drawMode == DrawMode.Shape) {
                        if (tempItem != null) {
                            drawnItems.add(tempItem!!)
                            tempItem = null
                        }
                    }
                },

                onDragStart = { change ->
                    tempItem = DrawnItem(
                        drawMode = cachedDrawInfo.drawMode,
                        shape = cachedDrawInfo.shape,
                        color = if (cachedDrawInfo.drawMode == DrawMode.Eraser) canvasColor else cachedDrawInfo.color,
                        strokeWidth = cachedDrawInfo.strokeWidth,
                        start = change,
                        end = change
                    )
                },


                onDrag = { change, _ ->
                    if (cachedDrawInfo.drawMode == DrawMode.Pen || cachedDrawInfo.drawMode == DrawMode.Eraser) {
                        change.consume()
                        tempItem = tempItem?.copy(end = change.position)
                        if (tempItem != null) {
                            drawnItems.add(tempItem!!)
                        }
                        tempItem = tempItem?.copy(start = change.position)
                    }
                    else if (cachedDrawInfo.drawMode == DrawMode.Shape) {
                        tempItem = tempItem?.copy(end = change.position)
                    }
                }
            )
        }
    ) {
        drawnItems.forEach { item ->
            when (item.shape) {
                Shape.Line, Shape.StraightLine -> {
                    drawLine(
                        color = item.color,
                        start = item.start,
                        end = item.end,
                        strokeWidth = item.strokeWidth,
                        cap = StrokeCap.Round
                    )
                }
                Shape.Rectangle -> {
                    drawRect(
                        color = item.color,
                        topLeft = item.start,
                        size = Size(item.end.x - item.start.x, item.end.y - item.start.y),
                        style = Stroke(
                            width = item.strokeWidth
                        )
                    )
                }
                Shape.Oval -> {
                    drawOval(
                        color = item.color,
                        topLeft = item.start,
                        size = Size(item.end.x - item.start.x, item.end.y - item.start.y),
                        style = Stroke(
                            width = item.strokeWidth
                        )
                    )
                }
            }
        }

        tempItem?.let {
            when (it.shape) {
                Shape.Rectangle -> {
                    drawRect(
                        color = it.color,
                        topLeft = it.start,
                        size = Size(it.end.x - it.start.x, it.end.y - it.start.y),
                        style = Stroke(
                            width = it.strokeWidth
                        )
                    )
                }
                Shape.Oval -> {
                    drawOval(
                        color = it.color,
                        topLeft = it.start,
                        size = Size(it.end.x - it.start.x, it.end.y - it.start.y),
                        style = Stroke(
                            width = it.strokeWidth
                        )
                    )
                }
                Shape.StraightLine -> {
                    drawLine(
                        color = it.color,
                        start = it.start,
                        end = it.end,
                        strokeWidth = it.strokeWidth,
                        cap = StrokeCap.Round
                    )
                }
                else -> Unit // Do nothing for other shapes
            }
        }
    }
}


@Composable
fun Toolbar(drawInfo: DrawInfo, setDrawInfo: (DrawInfo) -> Unit, setting: Settings, setSetting: (Settings) -> Unit) {
    Column(modifier = Modifier
        .background(MaterialTheme.colorScheme.secondaryContainer)
        .zIndex(1f)
    ) {
        if (setting != Settings.NULL) {
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Line Weight Setting
                if (setting == Settings.LineWeight) {
                    Slider(
                        value = drawInfo.strokeWidth,
                        onValueChange = {
                            setDrawInfo(drawInfo.copy(strokeWidth = it))
                        },
                        valueRange = 1f..MAX_STROKE_WIDTH
                    )
                }

                // Color Picker Setting
                if (setting == Settings.ColorPicker) {
                    val colors = listOf(Color.Black, Color.Gray, Color.Red,
                        Color.Yellow, Color.Green, Color.Blue, Color.Cyan, Color.Magenta)
                    colors.forEach { color ->
                        Button(
                            onClick = { setDrawInfo(drawInfo.copy(color = color)) },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(color),
                            modifier = Modifier
                                .size(45.dp)
                                .background(
                                    color = if (drawInfo.color == color) Color.White else MaterialTheme.colorScheme.secondaryContainer,
                                    shape = CircleShape
                                )
                                .padding(10.dp)
                        ) {}
                    }
                }

                // Shape Setting
                if (setting == Settings.Shape) {
                    IconButton(onClick = { setDrawInfo(drawInfo.copy(drawMode = DrawMode.Shape, shape = Shape.Rectangle)) }) {
                        Icon(Icons.Outlined.Rectangle, contentDescription = "Localized description", modifier = Modifier
                            .background(
                                color = if (drawInfo.shape == Shape.Rectangle) Color.White else MaterialTheme.colorScheme.secondaryContainer,
                                shape = CircleShape,
                            )
                            .padding(5.dp)
                        )
                    }

                    IconButton(onClick = { setDrawInfo(drawInfo.copy(drawMode = DrawMode.Shape, shape = Shape.Oval)) }) {
                        Icon(Icons.Outlined.Circle, contentDescription = "Localized description", modifier = Modifier
                            .background(
                                color = if (drawInfo.shape == Shape.Oval) Color.White else MaterialTheme.colorScheme.secondaryContainer,
                                shape = CircleShape,
                            )
                            .padding(5.dp)
                        )
                    }

                    IconButton(onClick = { setDrawInfo(drawInfo.copy(drawMode = DrawMode.Shape, shape = Shape.StraightLine)) }) {
                        Icon(painterResource(id = R.drawable.pen_size_3_24px), contentDescription = "Localized description", modifier = Modifier
                            .background(
                                color = if (drawInfo.shape == Shape.StraightLine) Color.White else MaterialTheme.colorScheme.secondaryContainer,
                                shape = CircleShape,
                            )
                            .padding(5.dp)
                        )
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
            // Pen Button
            IconButton(
                onClick = {
                    if (drawInfo.drawMode == DrawMode.Pen) {
                        setDrawInfo(drawInfo.copy(drawMode = DrawMode.NULL))
                    } else {
                        setDrawInfo(drawInfo.copy(drawMode = DrawMode.Pen, shape = Shape.Line))
                    }
                }
            ) {
                Icon(Icons.Filled.Create, contentDescription = "Localized description", modifier = Modifier
                    .background(
                        color = if (drawInfo.drawMode == DrawMode.Pen) Color.White else MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape,
                    )
                    .padding(5.dp)
                )
            }

            // Eraser Button
            IconButton(onClick = {
                if (drawInfo.drawMode == DrawMode.Eraser) {
                    setDrawInfo(drawInfo.copy(drawMode = DrawMode.NULL))
                } else {
                    setDrawInfo(drawInfo.copy(drawMode = DrawMode.Eraser, shape = Shape.Line))
                }
            }) {
                Icon(painterResource(id = R.drawable.ink_eraser_24px), contentDescription = "Localized description", modifier = Modifier
                    .background(
                        color = if (drawInfo.drawMode == DrawMode.Eraser) Color.White else MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape,
                    )
                    .padding(5.dp)
                )
            }

            // Line Weight Button
            IconButton(onClick = {
                if (setting == Settings.LineWeight) {
                    setSetting(Settings.NULL)
                } else {
                    setSetting(Settings.LineWeight)
                }
            }) {
                Icon(Icons.Outlined.LineWeight, contentDescription = "Localized description", modifier = Modifier
                    .background(
                        color = if (setting == Settings.LineWeight) Color.White else MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape,
                    )
                    .padding(5.dp)
                )
            }

            // Shapes Selection Button
            IconButton(onClick = {
                if (setting == Settings.Shape) {
                    setSetting(Settings.NULL)
                    setDrawInfo(drawInfo.copy(drawMode = DrawMode.NULL))
                } else {
                    setSetting(Settings.Shape)
                    setDrawInfo(drawInfo.copy(drawMode = DrawMode.Shape, shape = Shape.Rectangle)) // Default shape
                }
            }) {
                Icon(Icons.Outlined.ShapeLine, contentDescription = "Localized description", modifier = Modifier
                    .background(
                        color = if (setting == Settings.Shape) Color.White else MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape,
                    )
                    .padding(5.dp)
                )
            }

            // Color Picker Button
            IconButton(onClick = {
                if (setting == Settings.ColorPicker) {
                    setSetting(Settings.NULL)
                } else {
                    setSetting(Settings.ColorPicker)
                }
            }) {
                Icon(Icons.Outlined.Palette, contentDescription = "Localized description", modifier = Modifier
                    .background(
                        color = if (setting == Settings.ColorPicker) Color.White else MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape,
                    )
                    .padding(5.dp)
                )
            }
        }
    }
}
