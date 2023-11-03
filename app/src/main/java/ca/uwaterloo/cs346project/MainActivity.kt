package ca.uwaterloo.cs346project
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt


data class DrawnItem(
    val drawMode: DrawMode = DrawMode.Pen,
    val shape: Shape = Shape.Line,
    val color: Color = Color.Black,
    val strokeWidth: Float = 4f,
    val filled: Boolean = false,
    var start: Offset = Offset(0f, 0f),
    var end: Offset = Offset(0f, 0f),

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

// Stores all the drawn items (lines, straight lines, rectangles, ovals, erasing lines)
var drawnItems = mutableStateListOf<DrawnItem>()


// Checks if a point is close enough to a rectangle/oval
fun isPointCloseToRectangle(point: Offset, item: DrawnItem): Boolean {
    if (item.shape != Shape.Rectangle && item.shape != Shape.Oval) {
        return false
    }
    val margin = 100f
    val minX = min(item.start.x, item.end.x)
    val maxX = max(item.start.x, item.end.x)
    val minY = min(item.start.y, item.end.y)
    val maxY = max(item.start.y, item.end.y)
    return point.x in (minX - margin)..(maxX + margin) && point.y in (minY - margin)..(maxY + margin) &&
            !(point.x in (minX + margin)..(maxX - margin) && point.y in (minY + margin)..(maxY - margin))
}


// Checks if a point is close enough to a straight line
fun isPointCloseToLine(point: Offset, item: DrawnItem): Boolean {
    if (item.shape != Shape.StraightLine) {
        return false
    }

    val x1 = item.start.x
    val y1 = item.start.y
    val x2 = item.end.x
    val y2 = item.end.y
    val px = point.x
    val py = point.y

    val dx = x2 - x1
    val dy = y2 - y1
    val l2 = dx * dx + dy * dy
    val t = ((px - x1) * dx + (py - y1) * dy) / l2
    val tt = max(0f, min(1f, t))
    val projX = x1 + tt * dx
    val projY = y1 + tt * dy
    val distance = sqrt((projX - px) * (projX - px) + (projY - py) * (projY - py))

    val threshold = item.strokeWidth + 50f
    return distance < threshold
}


class MainActivity : ComponentActivity() {
    override fun onStop() {
        super.onStop()
        val json = Gson().toJson(drawnItems)
        val file = File(filesDir, "drawnItems.json")
        file.writeText(json)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val drawnItemsDataFile = File(filesDir, "drawnItemsData.json")

        if (drawnItemsDataFile.exists()) {
            val json = drawnItemsDataFile.readText()
            val type = object : TypeToken<List<DrawnItem>>() {}.type
            drawnItems = Gson().fromJson(json, type)
        }


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


@Composable
fun Whiteboard(drawInfo: DrawInfo) {
    val canvasColor = Color.White
    var cachedDrawInfo by remember { mutableStateOf(DrawInfo()) }
    cachedDrawInfo = drawInfo

    var tempItem by remember { mutableStateOf<DrawnItem?>(null) }
    var selectedItemIndex by remember { mutableStateOf(-1) }


    var prevDrawingState by remember { mutableStateOf<List<DrawnItem>>(listOf()) }

    var undoStack by remember { mutableStateOf<MutableList<List<DrawnItem>>>(mutableListOf(emptyList())) }
    var redoStack by remember { mutableStateOf<MutableList<List<DrawnItem>>>(mutableListOf()) }



    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopEnd
    ) {
        Canvas(modifier = Modifier
            .fillMaxSize()
            .background(canvasColor) // Default: White
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { point ->
                        val index = drawnItems.indexOfFirst { item ->
                            ((item.shape == Shape.Rectangle || item.shape == Shape.Oval) && isPointCloseToRectangle(point, item)) ||
                                    ((item.shape == Shape.StraightLine && isPointCloseToLine(point, item)))
                        }
                        selectedItemIndex = index
                    }
                )
            }


            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        if (cachedDrawInfo.drawMode == DrawMode.Shape) {
                            if (tempItem != null) {
                                drawnItems.add(tempItem!!) // Add the single drawn item to the drawnItems
                                tempItem = null
                            }

                        }
                        undoStack.add(drawnItems.toList())
                        redoStack.clear()
                        //prevDrawingState = drawnItems.toList()
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


                    onDrag = { change, amount ->
                        // Selecting mode
                        if (selectedItemIndex != -1) {
                            val updatedItem = drawnItems[selectedItemIndex].copy(
                                start = Offset(drawnItems[selectedItemIndex].start.x + amount.x, drawnItems[selectedItemIndex].start.y + amount.y),
                                end = Offset(drawnItems[selectedItemIndex].end.x + amount.x, drawnItems[selectedItemIndex].end.y + amount.y)
                            )
                            drawnItems[selectedItemIndex] = updatedItem
                        }
                        // Drawing mode
                        else {
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

            if (selectedItemIndex != -1) {
                if (selectedItemIndex < drawnItems.size) {
                    val item = drawnItems[selectedItemIndex]
                    if (item.shape == Shape.Rectangle || item.shape == Shape.Oval) {
                        val cornerSize = 40f
                        val corners = listOf(
                            item.start,
                            Offset(item.end.x, item.start.y),
                            Offset(item.start.x, item.end.y),
                            item.end
                        )
                        corners.forEach { corner ->
                            drawRect(
                                color = Color.Red,
                                topLeft = Offset(corner.x - cornerSize / 2, corner.y - cornerSize / 2),
                                size = Size(cornerSize, cornerSize),
                                style = Stroke(width = 5f)
                            )
                        }
                        if (item.shape == Shape.Oval) {
                            drawRect(
                                color = Color.Red,
                                topLeft = item.start,
                                size = Size(item.end.x - item.start.x, item.end.y - item.start.y),
                                style = Stroke(
                                    width = 3f
                                )
                            )
                        }
                    } else if (item.shape == Shape.StraightLine) {
                        val radius = 20f
                        val circleColor = Color.Red

                        drawCircle(
                            color = circleColor,
                            center = item.start,
                            radius = radius,
                            style = Stroke(width = 5f)
                        )

                        drawCircle(
                            color = circleColor,
                            center = item.end,
                            radius = radius,
                            style = Stroke(width = 5f)
                        )
                    }
                } else {
                    selectedItemIndex = -1
                }

            }

        }


        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.Top,
            modifier = Modifier.padding(8.dp)
        ) {
            // Undo button
            IconButton(onClick = { /* Handle undo */
                if (undoStack.size>1) {
                    //redoStack.add(drawnItems.toList()) // Add the current state to redo stack
                    drawnItems.clear()
                    redoStack.add(undoStack.removeAt(undoStack.lastIndex))
                    drawnItems.addAll(undoStack.last())
                }


            }) {
                Icon(painterResource(id = R.drawable.undo_24px), contentDescription = "Undo", modifier = Modifier
                    .background(
                        color = Color.LightGray,
                        shape = CircleShape,
                    )
                    .padding(5.dp)
                )
            }

            // Redo button
            IconButton(onClick = { /* Handle redo */
                if (redoStack.isNotEmpty()) {
                    //undoStack.add(drawnItems.toList()) // Add the current state to undo stack
                    drawnItems.clear()
                    undoStack.add(redoStack.removeAt(redoStack.lastIndex))
                    drawnItems.addAll(undoStack.last())
                }

            }) {
                Icon(painterResource(id = R.drawable.redo_24px), contentDescription = "Redo", modifier = Modifier
                    .background(
                        color = Color.LightGray,
                        shape = CircleShape,
                    )
                    .padding(5.dp)
                )
            }

            // Delete page button
            IconButton(onClick = {
                drawnItems.clear()
                undoStack.add(drawnItems.toList())
                redoStack.clear()
            }) {
                Icon(painterResource(id = R.drawable.delete_24px), contentDescription = "Delete", modifier = Modifier
                    .background(
                        color = Color.LightGray,
                        shape = CircleShape,
                    )
                    .padding(5.dp)
                )
            }

            IconButton(onClick = { /* Handle redo */ }) {
                Icon(painterResource(id = R.drawable.settings_24px), contentDescription = "Settings", modifier = Modifier
                    .background(
                        color = Color.LightGray,
                        shape = CircleShape,
                    )
                    .padding(5.dp)
                )
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
