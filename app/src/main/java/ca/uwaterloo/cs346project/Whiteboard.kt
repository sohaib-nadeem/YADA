package ca.uwaterloo.cs346project

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch


// Helper function to draw a given DrawnItem
fun DrawScope.drawTransformedItem(item: DrawnItem, viewportOffset: Offset) {
    // Create a new item with translated points
    val translatedItem = item.copy(
        segmentPoints = item.segmentPoints.map { point ->
            Offset(
                x = point.x - viewportOffset.x,
                y = point.y - viewportOffset.y
            )
        }.toMutableStateList()
    )

    when (translatedItem.shape) {
        Shape.Line, Shape.StraightLine -> {
            for (i in 1 until translatedItem.segmentPoints.size) {
                drawLine(
                    color = translatedItem.color,
                    start = translatedItem.segmentPoints[i - 1],
                    end = translatedItem.segmentPoints[i],
                    strokeWidth = translatedItem.strokeWidth,
                    cap = StrokeCap.Round
                )
            }
        }
        Shape.Rectangle -> {
            val start = translatedItem.segmentPoints[0]
            val end = translatedItem.segmentPoints[1]
            drawRect(
                color = translatedItem.color,
                topLeft = start,
                size = Size(
                    width = end.x - start.x,
                    height = end.y - start.y
                ),
                style = Stroke(width = translatedItem.strokeWidth)
            )
        }
        Shape.Oval -> {
            val start = translatedItem.segmentPoints[0]
            val end = translatedItem.segmentPoints[1]
            drawOval(
                color = translatedItem.color,
                topLeft = start,
                size = Size(
                    width = end.x - start.x,
                    height = end.y - start.y
                ),
                style = Stroke(width = translatedItem.strokeWidth)
            )
        }
    }
}



fun linesIntersect(segment1: Pair<Offset, Offset>, segment2: Pair<Offset, Offset>): Boolean {
    fun orientation(p: Offset, q: Offset, r: Offset): Int {
        val value = (q.y - p.y) * (r.x - q.x) - (q.x - p.x) * (r.y - q.y)
        return when {
            value.toDouble() == 0.0 -> 0  // collinear
            value > 0 -> 1  // clockwise
            else -> 2  // counterclockwise
        }
    }

    fun onSegment(p: Offset, q: Offset, r: Offset): Boolean {
        return q.x <= maxOf(p.x, r.x) && q.x >= minOf(p.x, r.x) &&
                q.y <= maxOf(p.y, r.y) && q.y >= minOf(p.y, r.y)
    }

    val (p1, q1) = segment1
    val (p2, q2) = segment2

    // Find the four orientations needed for general and special cases
    val o1 = orientation(p1, q1, p2)
    val o2 = orientation(p1, q1, q2)
    val o3 = orientation(p2, q2, p1)
    val o4 = orientation(p2, q2, q1)

    // General case
    if (o1 != o2 && o3 != o4) return true

    // Special Cases
    // p1, q1 and p2 are collinear and p2 lies on segment p1q1
    if (o1 == 0 && onSegment(p1, p2, q1)) return true

    // p1, q1 and p2 are collinear and q2 lies on segment p1q1
    if (o2 == 0 && onSegment(p1, q2, q1)) return true

    // p2, q2 and p1 are collinear and p1 lies on segment p2q2
    if (o3 == 0 && onSegment(p2, p1, q2)) return true

    // p2, q2 and q1 are collinear and q1 lies on segment p2q2
    if (o4 == 0 && onSegment(p2, q1, q2)) return true

    // Doesn't fall in any of the above cases
    return false
}


fun checkIntersection(item1: DrawnItem, item2: DrawnItem): Boolean {
    // Check if item1 is a line
    if (item1.shape != Shape.Line) return false

    // If item2 is a line or straight line, check intersection between each pair of consecutive points
    if (item2.shape == Shape.Line || item2.shape == Shape.StraightLine) {
        for (i in 1 until item1.segmentPoints.size) {
            for (j in 1 until item2.segmentPoints.size) {
                if (linesIntersect(
                                Pair(item1.segmentPoints[i - 1], item1.segmentPoints[i]),
                                Pair(item2.segmentPoints[j - 1], item2.segmentPoints[j])
                        )
                ) {
                    Log.d("Intersection", "Intersection detected between lines.")
                    return true
                }
            }
        }
    }
    // If item2 is a rectangle, calculate its edges and check intersection
    else if (item2.shape == Shape.Rectangle || item2.shape == Shape.Oval) {
        val topLeft = item2.segmentPoints[0]
        val bottomRight = item2.segmentPoints[1]
        val topRight = Offset(bottomRight.x, topLeft.y)
        val bottomLeft = Offset(topLeft.x, bottomRight.y)

        val rectangleEdges = listOf(
                Pair(topLeft, topRight),
                Pair(topRight, bottomRight),
                Pair(bottomRight, bottomLeft),
                Pair(bottomLeft, topLeft)
        )

        for (i in 1 until item1.segmentPoints.size) {
            rectangleEdges.forEach { edge ->
                if (linesIntersect(
                                Pair(item1.segmentPoints[i - 1], item1.segmentPoints[i]),
                                edge
                        )
                ) {
                    Log.d("Intersection", "Intersection detected between line and rectangle.")
                    return true
                }
            }
        }
    }

    return false
}


@Composable
fun Whiteboard(drawInfo: DrawInfo, undoStack: MutableList<List<DrawnItem>>, redoStack: MutableList<List<DrawnItem>>) {
    val canvasColor = Color.White
    var cachedDrawInfo by remember { mutableStateOf(DrawInfo()) }
    cachedDrawInfo = drawInfo

    var tempItem by remember { mutableStateOf<DrawnItem?>(null) }
    var selectedItemIndex by remember { mutableStateOf(-1) }
    val scope = rememberCoroutineScope()
    var tempOffset by remember { mutableStateOf(Offset(0f,0f)) }

    // Left upper corner offset of the current screen relative to the canvas
    var viewportOffset by remember { mutableStateOf(Offset(1500f, 1500f)) }

    // Default canvas size (3000px * 3000px)
    // Note: Emulator screen size is 1080px * 2154px
    val canvasWidth = 3000f
    val canvasHeight = 3000f

    fun constrainOffset(offset: Offset): Offset {
        val x = offset.x.coerceIn(0f, canvasWidth)
        val y = offset.y.coerceIn(0f, canvasHeight)
        return Offset(x, y)
    }


    Canvas(modifier = Modifier
        .fillMaxSize()
        .background(canvasColor) // Default: White
        .pointerInput(Unit) {
            detectTapGestures(
                onTap = { point ->
                    val canvasRelativeOffset = point + viewportOffset // NEW

                    if (cachedDrawInfo.drawMode == DrawMode.Selection) {
                        val index = drawnItems.indexOfFirst { item ->
                            ((item.shape == Shape.Rectangle || item.shape == Shape.Oval) && isPointCloseToRectangle(canvasRelativeOffset, item)) ||
                                    ((item.shape == Shape.StraightLine && isPointCloseToLine(canvasRelativeOffset, item)))
                        }
                        selectedItemIndex = index
                    }
                    else if (cachedDrawInfo.drawMode == DrawMode.Pen) {
                        drawnItems.add(DrawnItem(
                            shape = cachedDrawInfo.shape,
                            color = cachedDrawInfo.color,
                            strokeWidth = cachedDrawInfo.strokeWidth,
                            segmentPoints = mutableStateListOf(canvasRelativeOffset, canvasRelativeOffset)
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
                            Unit
                        }

                        DrawMode.CanvasDrag -> {
                            Unit // TO BE IMPLEMENTED (Drag canvas)
                        }

                        DrawMode.Eraser -> {
                            if (tempItem != null) {
                                val erasedItems = mutableListOf<DrawnItem>()
                                val remainingItems = mutableListOf<DrawnItem>()

                                drawnItems.forEach { item ->
                                    if (checkIntersection(tempItem!!, item)) {
                                        erasedItems.add(item)
                                    } else {
                                        remainingItems.add(item)
                                    }
                                }

                                drawnItems.clear()
                                drawnItems.addAll(remainingItems)

                                tempItem = null

                                // Need to somehow find a way to send the "remove" action to server
                            }
                        }

                        else -> {
                            if (tempItem != null) {
                                drawnItems.add(tempItem!!)
                                tempItem = null

                                // Silenced the server sync, testing prototype
//                                scope.launch {
//                                    //Client().send(user_id, drawnItems.last())
//                                    Client().fakeSend(user_id, drawnItems.last())
//                                }
                            }
                        }

                    }
                    undoStack.add(drawnItems.toList())
                    redoStack.clear()
                },

                onDragStart = { change ->
                    if (cachedDrawInfo.drawMode != DrawMode.Selection && cachedDrawInfo.drawMode != DrawMode.CanvasDrag) {
                        tempOffset = change + viewportOffset
                        tempItem = DrawnItem(
                            shape = cachedDrawInfo.shape,
//                          color = if (cachedDrawInfo.drawMode == DrawMode.Eraser) canvasColor else cachedDrawInfo.color, // Original
                            color = if (cachedDrawInfo.drawMode == DrawMode.Eraser) Color.Red else cachedDrawInfo.color, // TESTING (TO BE REMOVED)
                            strokeWidth = cachedDrawInfo.strokeWidth,
                            segmentPoints = mutableStateListOf(tempOffset, tempOffset)
                        )

                    }
                },

                onDrag = { change, amount ->
                    if (cachedDrawInfo.drawMode == DrawMode.CanvasDrag) {
                        val newOffset = constrainOffset((viewportOffset - amount))
                        if (newOffset != viewportOffset) {
                            viewportOffset = newOffset
                        } else {
                            Log.d("CanvasDrag", "Boundary reached") // DEBUG & TEST PURPOSE (can be removed)
                        }
                    }

                    else if (cachedDrawInfo.drawMode == DrawMode.Selection) {
                        if (selectedItemIndex != -1) {
                            val item = drawnItems[selectedItemIndex]
                            val updatedSegmentPoints = item.segmentPoints.map { offset ->
                                Offset(offset.x + amount.x, offset.y + amount.y)
                            }.toMutableStateList()
                            val updatedItem = item.copy(segmentPoints = updatedSegmentPoints)
                            drawnItems[selectedItemIndex] = updatedItem
                        }
                    }

                    else {
                        tempOffset = change.position + viewportOffset

                        if (cachedDrawInfo.drawMode == DrawMode.Pen || cachedDrawInfo.drawMode == DrawMode.Eraser) {
                            change.consume()
                            if (tempItem != null) {
                                tempItem!!.segmentPoints.add(tempOffset)
                            }
                        }

                        else if (cachedDrawInfo.drawMode == DrawMode.Shape) {
                            if (tempItem != null && tempItem!!.segmentPoints.size == 2) {
                                tempItem!!.segmentPoints[1] = tempOffset
                            }
                        }
                    }
                }
            )
        }
    ) {
        drawnItems.forEach { item ->
            drawTransformedItem(item, viewportOffset)
        }

        tempItem?.let {
            drawTransformedItem(it, viewportOffset)
        }

        if (cachedDrawInfo.drawMode == DrawMode.Selection && selectedItemIndex != -1) {
            if (selectedItemIndex < drawnItems.size) {
                val item = drawnItems[selectedItemIndex]
                if (item.shape == Shape.Rectangle || item.shape == Shape.Oval) {
                    val cornerSize = 40f
                    // Assuming the first and last points are the corners of the rectangle/oval
                    val start = item.segmentPoints.first() - viewportOffset
                    val end = item.segmentPoints.last() - viewportOffset
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
                            style = Stroke(width = 3f)
                        )
                    }
                } else if (item.shape == Shape.StraightLine) {
                    val radius = 20f
                    val circleColor = Color.Red
                    // Draw circles at each segment point
                    item.segmentPoints.forEach { point ->
                        drawCircle(
                            color = circleColor,
                            center = point - viewportOffset,
                            radius = radius,
                            style = Stroke(width = 5f)
                        )
                    }
                }
            } else {
                selectedItemIndex = -1
            }
        }
    }
}