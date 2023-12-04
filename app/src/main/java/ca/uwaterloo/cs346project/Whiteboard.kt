package ca.uwaterloo.cs346project

import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.os.Environment
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import dev.shreyaspatil.capturable.Capturable
import dev.shreyaspatil.capturable.controller.CaptureController
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

fun Offset.calculateNewOffset(
    centroid: Offset,
    pan: Offset,
    zoom: Float,
    gestureZoom: Float,
    size: IntSize
): Offset {
    val newScale = maxOf(1f, zoom * gestureZoom)
    val newOffset = (this + centroid / zoom) -
            (centroid / newScale + pan / zoom)
    return Offset(
        newOffset.x.coerceIn(0f, (size.width / zoom) * (zoom - 1f)),
        newOffset.y.coerceIn(0f, (size.height / zoom) * (zoom - 1f))
    )
}

fun transformOffset(zoom: Float, offset: Offset, offsetToTransform: Offset): Offset {
    return Offset(offsetToTransform.x / zoom + offset.x, offsetToTransform.y / zoom + offset.y)
}

fun transformAmount(zoom: Float, offset: Offset, amountToTransform: Offset): Offset {
    return Offset(amountToTransform.x / zoom, amountToTransform.y / zoom)
}

// Helper function to draw a given DrawnItem
fun DrawScope.drawItem(item: DrawnItem) {
    // Create a new item with translated points
    val translatedItem = item.copy(
        segmentPoints = item.segmentPoints.map { point ->
            Offset(
                x = point.x,
                y = point.y
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
    undoStack: MutableList<Action<DrawnItem>>,
    redoStack: MutableList<Action<DrawnItem>>,
    captureController: CaptureController,
    selectedImage: ImageBitmap?,
){
    val canvasColor = Color.White
    var cachedDrawInfo by remember { mutableStateOf(DrawInfo()) }
    cachedDrawInfo = drawInfo

    var tempItem by remember { mutableStateOf<DrawnItem?>(null) }
    var selectedItemIndex by remember { mutableStateOf(-1) }
    val scope = rememberCoroutineScope()
    var tempOffset by remember { mutableStateOf(Offset(0f,0f)) }
    var tempAction by remember { mutableStateOf<Action<DrawnItem>?>(null) }

    var zoom by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val density = LocalDensity.current;
    val configuration = LocalConfiguration.current;
    var screenWidthPx = with(density) {configuration.screenWidthDp.dp.roundToPx()}
    var screenHeightPx = with(density) {configuration.screenHeightDp.dp.roundToPx()}


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
                tempAction = Action<DrawnItem>(
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
                        val canvasRelativeOffset = transformOffset(zoom, offset, point) // NEW

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
                            // add dot to drawnItems
                            val item = DrawnItem(
                                userObjectId = DrawnItem.newUserObjectId(),
                                shape = cachedDrawInfo.shape,
                                color = cachedDrawInfo.color,
                                strokeWidth = cachedDrawInfo.strokeWidth,
                                segmentPoints = mutableStateListOf(
                                    canvasRelativeOffset,
                                    canvasRelativeOffset
                                )
                            )
                            drawnItems.add(item)

                            // add action performed to undo stack and clear redo stack
                            val tapAction = Action(
                                type = ActionType.ADD,
                                items = listOf(item)
                            )
                            undoStack.add(tapAction)
                            redoStack.clear()

                            // also send action to server if online
                            if (!offline) {
                                scope.launch {
                                    client.sendAction(tapAction)
                                }
                            }

                        }
                    }
                )
            }

            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { change ->
                        if (cachedDrawInfo.drawMode != DrawMode.Selection && cachedDrawInfo.drawMode != DrawMode.CanvasDrag) {
                            tempOffset = transformOffset(zoom, offset, change)
                            tempItem = DrawnItem(
                                userObjectId = DrawnItem.newUserObjectId(),
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
                        val transformedAmount = transformAmount(zoom, offset, amount)
                        if (cachedDrawInfo.drawMode == DrawMode.Selection) {
                            if (selectedItemIndex != -1) {
                                val item = drawnItems[selectedItemIndex]

                                if (tempAction == null) {
                                    tempAction = Action(
                                        type = ActionType.MODIFY,
                                        items = listOf(
                                            item.copy(),
                                            item.copy()
                                        ) //  store initial state of the dragged shape
                                    )
                                }

                                val updatedSegmentPoints = item.segmentPoints
                                    .map { offset ->
                                        Offset(
                                            offset.x + transformedAmount.x,
                                            offset.y + transformedAmount.y
                                        )
                                    }
                                    .toMutableStateList()
                                val updatedItem = item.copy(segmentPoints = updatedSegmentPoints)
                                drawnItems[selectedItemIndex] = updatedItem

                                tempAction = tempAction!!.copy(
                                    items = listOf(
                                        tempAction!!.items[0],
                                        updatedItem
                                    )
                                )
                            }
                        } else {
                            tempOffset = transformOffset(zoom, offset, change.position)

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
                                    tempItem = null
                                }
                            }
                        }

                        if (tempAction != null) {
                            undoStack.add(tempAction!!)
                            redoStack.clear()

                            // also send action to server if online
                            if (!offline) {
                                val actionToSend = tempAction!!
                                scope.launch {
                                    client.sendAction(actionToSend)
                                }
                            }

                            tempAction = null
                        }

//                    undoStack.add(drawnItems.toList())
//                    redoStack.clear()
                    }
                )
            }
            .pointerInput(Unit) {
                detectTransformGesturesCustom(
                    onGesture = { centroid, pan, gestureZoom, _ ->
                        if (cachedDrawInfo.drawMode == DrawMode.CanvasDrag) {
                            offset = offset.calculateNewOffset(
                                centroid, pan, zoom, gestureZoom, size
                            )
                            zoom = maxOf(1f, zoom * gestureZoom)
                        }
                    }
                )
            }
            .graphicsLayer {
                translationX = -offset.x * zoom
                translationY = -offset.y * zoom
                scaleX = zoom; scaleY = zoom
                transformOrigin = TransformOrigin(0f, 0f)
            }
        ) {
            if (selectedImage != null) {
                drawImage(
                    image = selectedImage,
                    dstSize = IntSize(screenWidthPx,screenHeightPx)

                )
            }

            drawnItems.forEach { item ->
                drawItem(item)
            }

            tempItem?.let {
                if (cachedDrawInfo.drawMode != DrawMode.Eraser) {
                    drawItem(it)
                }
            }

            if (cachedDrawInfo.drawMode == DrawMode.Selection && selectedItemIndex != -1) {
                if (selectedItemIndex < drawnItems.size) {
                    val item = drawnItems[selectedItemIndex]
                    if (item.shape == Shape.Rectangle || item.shape == Shape.Oval) {
                        val cornerSize = 40f
                        // Assuming the first and last points are the corners of the rectangle/oval
                        val start = item.segmentPoints.first()
                        val end = item.segmentPoints.last()
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
                                center = point,
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