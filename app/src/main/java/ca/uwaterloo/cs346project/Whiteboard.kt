package ca.uwaterloo.cs346project

import android.util.Log
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
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch


// Helper function to draw a given DrawnItem
fun DrawScope.drawItem(item: DrawnItem) {
    val start = item.segmentPoints.first().first
    val end = item.segmentPoints.first().second

    when (item.shape) {
        Shape.Line, Shape.StraightLine -> {
            item.segmentPoints.forEach { pair ->
                drawLine(
                    color = item.color,
                    start = pair.first,
                    end = pair.second,
                    strokeWidth = item.strokeWidth,
                    cap = StrokeCap.Round
                )
            }
        }
        Shape.Rectangle -> {
            drawRect(
                color = item.color,
                topLeft = start,
                size = Size(end.x - start.x, end.y - start.y),
                style = Stroke(width = item.strokeWidth)
            )
        }
        Shape.Oval -> {
            drawOval(
                color = item.color,
                topLeft = start,
                size = Size(end.x - start.x, end.y - start.y),
                style = Stroke(width = item.strokeWidth)
            )
        }
    }
}


@Composable
fun Whiteboard(drawInfo: DrawInfo, undoStack: MutableList<List<DrawnItem>>, redoStack: MutableList<List<DrawnItem>>) {
    val canvasColor = Color.White
    var cachedDrawInfo by remember { mutableStateOf(DrawInfo()) }
    cachedDrawInfo = drawInfo

    var tempItem by remember { mutableStateOf<DrawnItem?>(null) }
    var selectedItemIndex by remember { mutableStateOf(-1) }
    val scope = rememberCoroutineScope()
    var tempOffsetPair by remember {mutableStateOf<Pair<Offset, Offset>?>(null)}

    Canvas(modifier = Modifier
        .fillMaxSize()
        .background(canvasColor) // Default: White
        .pointerInput(Unit) {
            detectTapGestures(
                onTap = { point ->
                    if (cachedDrawInfo.drawMode == DrawMode.Selection) {
                        val index = drawnItems.indexOfFirst { item ->
                            ((item.shape == Shape.Rectangle || item.shape == Shape.Oval) && isPointCloseToRectangle(point, item)) ||
                                    ((item.shape == Shape.StraightLine && isPointCloseToLine(point, item)))
                        }
                        selectedItemIndex = index
                    }
                    else if (cachedDrawInfo.drawMode == DrawMode.Pen) {
                        drawnItems.add(DrawnItem(
                            shape = cachedDrawInfo.shape,
                            color = cachedDrawInfo.color,
                            strokeWidth = cachedDrawInfo.strokeWidth,
                            segmentPoints = mutableStateListOf(Pair(point, point))
                        ))
                        undoStack.add(drawnItems.toList())
                        redoStack.clear()
                    }
                }
            )
        }

        .pointerInput(Unit) {
            detectDragGestures(
                onDragEnd = {
                    when (cachedDrawInfo.drawMode) {
                        DrawMode.Selection -> {
                            undoStack.add(drawnItems.toList())
                            redoStack.clear()
                        }

                        DrawMode.CanvasDrag -> {
                            Unit // TO BE IMPLEMENTED (Drag canvas)
                        }

                        else -> {
                            if (tempItem != null) {
                                drawnItems.add(tempItem!!)
                                tempItem = null
                                tempOffsetPair = null
                                scope.launch {
                                    Client().send(user_id, drawnItems.last())
                                }
                            }
                            undoStack.add(drawnItems.toList())
                            redoStack.clear()
                        }
                    }
                },

                onDragStart = { change ->
                    if (cachedDrawInfo.drawMode != DrawMode.Selection && cachedDrawInfo.drawMode != DrawMode.CanvasDrag) {
                            tempItem = DrawnItem(
                                shape = cachedDrawInfo.shape,
                                color = if (cachedDrawInfo.drawMode == DrawMode.Eraser) canvasColor else cachedDrawInfo.color,
                                strokeWidth = cachedDrawInfo.strokeWidth,
                                segmentPoints = mutableStateListOf(Pair(change, change))
                            )
                        tempOffsetPair = Pair(change, change)
                    }
                },

                onDrag = { change, amount ->
                    if (cachedDrawInfo.drawMode == DrawMode.CanvasDrag) {
                        Unit // do something (drag canvas)
                    }

                    else if (cachedDrawInfo.drawMode == DrawMode.Selection) {
                        if (selectedItemIndex != -1) {
                            val item = drawnItems[selectedItemIndex]
                            val updatedSegmentPoints = item.segmentPoints.map { (start, end) ->
                                Pair(
                                    Offset(start.x + amount.x, start.y + amount.y),
                                    Offset(end.x + amount.x, end.y + amount.y)
                                )
                            }.toMutableStateList()
                            val updatedItem = item.copy(segmentPoints = updatedSegmentPoints)
                            drawnItems[selectedItemIndex] = updatedItem
                        }
                    }

                    else {
                        tempOffsetPair = tempOffsetPair!!.copy( second = change.position )

                        if (cachedDrawInfo.drawMode == DrawMode.Pen || cachedDrawInfo.drawMode == DrawMode.Eraser) {
                            change.consume()
                            if (tempOffsetPair != null && tempItem != null) {
                                tempItem!!.segmentPoints.add(tempOffsetPair!!)
                                tempOffsetPair = Pair(change.position, change.position)

                            }
                        }
                        else if (cachedDrawInfo.drawMode == DrawMode.Shape) {
                            if (tempItem != null && tempOffsetPair != null && tempItem!!.segmentPoints.isNotEmpty()) {
                                tempItem!!.segmentPoints[0] = tempOffsetPair!!
                            }
                        }
                    }
                }
            )
        }
    ) {
        drawnItems.forEach { item ->
            drawItem(item)
        }

        // Draw the temporary item
        tempItem?.let {
            drawItem(it)
        }

        if (cachedDrawInfo.drawMode == DrawMode.Selection && selectedItemIndex != -1) {
            if (selectedItemIndex < drawnItems.size) {
                val item = drawnItems[selectedItemIndex]
                val start = item.segmentPoints[0].first
                val end = item.segmentPoints[0].second
                if (item.shape == Shape.Rectangle || item.shape == Shape.Oval) {
                    val cornerSize = 40f
                    val corners = listOf(
                        start,
                        Offset(end.x, start.y),
                        Offset(start.x, end.y),
                        end
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
                            topLeft = start,
                            size = Size(end.x - start.x, end.y - start.y),
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
                        center = start,
                        radius = radius,
                        style = Stroke(width = 5f)
                    )

                    drawCircle(
                        color = circleColor,
                        center = end,
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