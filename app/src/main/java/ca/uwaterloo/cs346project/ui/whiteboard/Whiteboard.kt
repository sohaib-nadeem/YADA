package ca.uwaterloo.cs346project.ui.whiteboard

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import ca.uwaterloo.cs346project.client
import ca.uwaterloo.cs346project.model.Action
import ca.uwaterloo.cs346project.model.ActionType
import ca.uwaterloo.cs346project.model.Shape
import ca.uwaterloo.cs346project.ui.util.detectTransformGesturesCustom
import ca.uwaterloo.cs346project.ui.util.isPointCloseToLine
import ca.uwaterloo.cs346project.ui.util.isPointCloseToRectangle
import ca.uwaterloo.cs346project.offline
import ca.uwaterloo.cs346project.ui.util.DrawInfo
import ca.uwaterloo.cs346project.ui.util.DrawMode
import ca.uwaterloo.cs346project.ui.util.DrawnItem
import ca.uwaterloo.cs346project.ui.util.calculateNewOffset
import ca.uwaterloo.cs346project.ui.util.drawItem
import ca.uwaterloo.cs346project.ui.util.drawShapeCorners
import ca.uwaterloo.cs346project.ui.util.eraseIntersectingItems
import ca.uwaterloo.cs346project.ui.util.transformAmount
import ca.uwaterloo.cs346project.ui.util.transformOffset
import dev.shreyaspatil.capturable.controller.CaptureController
import kotlinx.coroutines.launch


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

    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidthPx = with(density) {configuration.screenWidthDp.dp.roundToPx()}
    val screenHeightPx = with(density) {configuration.screenHeightDp.dp.roundToPx()}

    ExportCapturable(captureController) {
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
                        val transformedAmount = transformAmount(zoom, amount)
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
                                        val erasedItems = eraseIntersectingItems(tempItem, drawnItems)

                                        // add the additional erased objects added to the items field of the tempAction
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
                    drawShapeCorners(item, 40f / zoom)
                } else {
                    selectedItemIndex = -1
                }
            }

        }
    }
}
