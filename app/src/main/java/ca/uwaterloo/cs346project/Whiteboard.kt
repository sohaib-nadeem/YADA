package ca.uwaterloo.cs346project

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun Whiteboard(drawInfo: DrawInfo, undoStack: MutableList<List<DrawnItem>>, redoStack: MutableList<List<DrawnItem>>) {
    val canvasColor = Color.White
    var cachedDrawInfo by remember { mutableStateOf(DrawInfo()) }
    cachedDrawInfo = drawInfo

    var tempItem by remember { mutableStateOf<DrawnItem?>(null) }
    var selectedItemIndex by remember { mutableStateOf(-1) }
    val scope = rememberCoroutineScope()
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
                            drawnItems.add(tempItem!!)
                            tempItem = null
                            scope.launch {
                                Client().send(user_id, drawnItems.last())
                            }
                        }
                    }
                    undoStack.add(drawnItems.toList())
                    redoStack.clear()

                },

                onDragStart = { change ->
                    tempItem = DrawnItem(
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
                            scope.launch {
                                Client().send(user_id, drawnItems.last())
                            }
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
}