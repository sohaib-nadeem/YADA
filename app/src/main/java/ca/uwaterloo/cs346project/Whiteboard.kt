package ca.uwaterloo.cs346project

import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import dev.shreyaspatil.capturable.Capturable
import dev.shreyaspatil.capturable.controller.CaptureController
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Float.max
import java.text.SimpleDateFormat
import java.util.Date


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
        // Add other shapes as needed
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


fun checkIntersection(line: Pair<Offset, Offset>, item: DrawnItem): Boolean {
    // If item2 is a line or straight line, check intersection between each pair of consecutive points
    if (item.shape == Shape.Line || item.shape == Shape.StraightLine) {
        for (i in 1 until item.segmentPoints.size) {
            if (linesIntersect(line, Pair(item.segmentPoints[i - 1], item.segmentPoints[i]))) {
                Log.d("Intersection", "Intersection detected between lines.")
                return true
            }
        }
    }
    // If item is a rectangle, calculate its edges and check intersection
    else if (item.shape == Shape.Rectangle || item.shape == Shape.Oval) {
        val topLeft = item.segmentPoints[0]
        val bottomRight = item.segmentPoints[1]
        val topRight = Offset(bottomRight.x, topLeft.y)
        val bottomLeft = Offset(topLeft.x, bottomRight.y)

        val rectangleEdges = listOf(
            Pair(topLeft, topRight),
            Pair(topRight, bottomRight),
            Pair(bottomRight, bottomLeft),
            Pair(bottomLeft, topLeft)
        )

        rectangleEdges.forEach { edge ->
            if (linesIntersect(line, edge)) {
                Log.d("Intersection", "Intersection detected between line and rectangle.")
                return true
            }
        }
    }
    return false
}



@Composable
fun Whiteboard(
    drawInfo: DrawInfo,
    drawnItems: SnapshotStateList<DrawnItem>,
    undoStack: MutableList<Action>,
    redoStack: MutableList<Action>,
    captureController: CaptureController,
    screenWidth: Float,
    screenHeight: Float
){
    val canvasColor = Color.White
    var cachedDrawInfo by remember { mutableStateOf(DrawInfo()) }
    cachedDrawInfo = drawInfo

    var tempItem by remember { mutableStateOf<DrawnItem?>(null) }
    var selectedItemIndex by remember { mutableStateOf(-1) }
    val scope = rememberCoroutineScope()
    var tempOffset by remember { mutableStateOf(Offset(0f,0f)) }
    var tempAction by remember { mutableStateOf<Action?>(null) }

    // Left upper corner offset of the current screen relative to the canvas
    var viewportOffset by remember { mutableStateOf(Offset.Zero) }

    // Default canvas size (3000px * 3000px)
    // Note: Emulator screen size is 1080px * 2154px
    val canvasWidth = 3000f
    val canvasHeight = 6000f

    val maxViewportOffset = Offset(max(canvasWidth - screenWidth, 0f), max(canvasHeight - screenHeight, 0f))

    fun constrainOffset(offset: Offset): Offset {
        val x = offset.x.coerceIn(0f, maxViewportOffset.x)
        val y = offset.y.coerceIn(0f, maxViewportOffset.y)
        return Offset(x, y)
    }

    fun eraseIntersectingItems() {
        val erasedItems = mutableListOf<DrawnItem>()
        val remainingItems = mutableListOf<DrawnItem>()

        if (tempItem != null && tempItem!!.segmentPoints.size >= 2) {
            drawnItems.forEach { item ->
                if (checkIntersection(Pair(tempItem!!.segmentPoints[tempItem!!.segmentPoints.size - 2], tempItem!!.segmentPoints.last()), item)) {
                    erasedItems.add(item)
                } else {
                    remainingItems.add(item)
                }
            }
            drawnItems.clear()
            drawnItems.addAll(remainingItems)
        }

        if (erasedItems.isNotEmpty()) {
            if (tempAction == null) {
                tempAction = Action(
                    type =  ActionType.REMOVE,
                    items = emptyList()
                )
            }
            tempAction = tempAction!!.copy(
                items = tempAction!!.items + erasedItems
            )
        }
    }

    Capturable(
        controller = captureController,
        onCaptured = { bitmap, error ->
            // This is captured bitmap
            if (bitmap != null) {
                // Bitmap is captured successfully.
                println("Bitmap successful")
                val pdfDocument = PdfDocument()
                val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas : Canvas = page.canvas
                canvas.drawBitmap(bitmap.asAndroidBitmap(), 0f, 0f, null)
                pdfDocument.finishPage(page)

                try {
                    val timestamp = System.currentTimeMillis()
                    val sdf = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")
                    val date = Date(timestamp)
                    val formattedDate = sdf.format(date)
                    val filename = "Canvas$formattedDate.pdf"
                    val pdfFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename)
                    pdfDocument.writeTo(FileOutputStream(pdfFile))
                    // Handle success - the PDF is saved
                    println("save successful")
                } catch (e: IOException) {
                    // Handle the error
                    println("exception")
                    println(e)
                } finally {
                    pdfDocument.close()
                }

            }

            if (error != null) {
                // Error occurred.
                println("abc")
            }
        }
    ) {
        Canvas(modifier = Modifier
            .fillMaxSize()
            .background(canvasColor) // Default: White
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { point ->
                        val canvasRelativeOffset = point + viewportOffset // NEW

                        if (cachedDrawInfo.drawMode == DrawMode.Selection) {
                            val index = drawnItems.indexOfFirst { item ->
                                ((item.shape == Shape.Rectangle || item.shape == Shape.Oval) && isPointCloseToRectangle(
                                    canvasRelativeOffset,
                                    item
                                )) ||
                                        ((item.shape == Shape.StraightLine && isPointCloseToLine(
                                            canvasRelativeOffset,
                                            item
                                        )))
                            }
                            selectedItemIndex = index
                        } else if (cachedDrawInfo.drawMode == DrawMode.Pen) {
                            val item = DrawnItem(
                                shape = cachedDrawInfo.shape,
                                color = cachedDrawInfo.color,
                                strokeWidth = cachedDrawInfo.strokeWidth,
                                segmentPoints = mutableStateListOf(
                                    canvasRelativeOffset,
                                    canvasRelativeOffset
                                )
                            )
                            drawnItems.add(item)

                            scope.launch {
                                client.send(drawnItems.last())
                            }

                            undoStack.add(
                                Action(
                                    type = ActionType.ADD,
                                    items = listOf(item)
                                )
                            )
                            redoStack.clear()

                        }
                    }
                )
            }

            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { change ->
                        if (cachedDrawInfo.drawMode != DrawMode.Selection && cachedDrawInfo.drawMode != DrawMode.CanvasDrag) {
                            tempOffset = change + viewportOffset
                            tempItem = DrawnItem(
                                shape = cachedDrawInfo.shape,
                                color = if (cachedDrawInfo.drawMode == DrawMode.Eraser) Color.White else cachedDrawInfo.color, // Original
                                strokeWidth = cachedDrawInfo.strokeWidth,
                                segmentPoints = mutableStateListOf(tempOffset, tempOffset)
                            )

                            if (cachedDrawInfo.drawMode == DrawMode.Pen || cachedDrawInfo.drawMode == DrawMode.Shape) {
                                tempAction = Action(
                                    type = ActionType.ADD,
                                    items = listOf(tempItem!!)
                                )
                            }
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

                        } else if (cachedDrawInfo.drawMode == DrawMode.Selection) {
                            if (selectedItemIndex != -1) {
                                val item = drawnItems[selectedItemIndex]

                                if (tempAction == null) {
                                    tempAction = Action(
                                        type = ActionType.MODIFY,
                                        items = listOf(item.copy()), //  store initial state of the dragged shape
                                    )
                                }

                                val updatedSegmentPoints = item.segmentPoints.map { offset ->
                                    Offset(offset.x + amount.x, offset.y + amount.y)
                                }.toMutableStateList()
                                val updatedItem = item.copy(segmentPoints = updatedSegmentPoints)
                                drawnItems[selectedItemIndex] = updatedItem

                                tempAction = tempAction!!.copy(items = listOf(updatedItem))
                            }
                        } else {
                            tempOffset = change.position + viewportOffset

                            if (cachedDrawInfo.drawMode == DrawMode.Pen || cachedDrawInfo.drawMode == DrawMode.Eraser) {
                                change.consume()
                                if (tempItem != null) {
                                    tempItem!!.segmentPoints.add(tempOffset)
                                    if (cachedDrawInfo.drawMode == DrawMode.Eraser) {
                                        eraseIntersectingItems()
                                    }
                                }
                            } else if (cachedDrawInfo.drawMode == DrawMode.Shape) {
                                if (tempItem != null && tempItem!!.segmentPoints.size == 2) {
                                    tempItem!!.segmentPoints[1] = tempOffset
                                }
                            }
                        }
                    },

                    onDragEnd = {
                        when (cachedDrawInfo.drawMode) {
                            DrawMode.Selection, DrawMode.CanvasDrag, DrawMode.Eraser -> {
                                tempItem = null
                            }

                            else -> { // Drawing mode
                                if (tempItem != null) {
                                    drawnItems.add(tempItem!!)

                                    scope.launch {
                                        client.send(drawnItems.last())
                                    }

                                    tempItem = null

                                    // Silenced the server sync, testing prototype
//                                scope.launch {
//                                    //Client().send(user_id, drawnItems.last())
//                                    Client().fakeSend(user_id, drawnItems.last())
//                                }
                                }
                            }

                        }

                        if (tempAction != null) {
                            undoStack.add(tempAction!!)
                            redoStack.clear()
                            tempAction = null
                        }

//                    undoStack.add(drawnItems.toList())
//                    redoStack.clear()
                    }
                )
            }
        ) {
            drawnItems.forEach { item ->
                drawTransformedItem(item, viewportOffset)
            }

            tempItem?.let {
                if (cachedDrawInfo.drawMode != DrawMode.Eraser) {
                    drawTransformedItem(it, viewportOffset)
                }
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
                                topLeft = Offset(
                                    corner.x - cornerSize / 2,
                                    corner.y - cornerSize / 2
                                ),
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
}