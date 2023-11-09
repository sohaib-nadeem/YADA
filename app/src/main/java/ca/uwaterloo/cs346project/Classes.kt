package ca.uwaterloo.cs346project

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

data class DrawnItem(
    val shape: Shape = Shape.Line,
    var color: Color = Color.Black,
    val strokeWidth: Float = 4f,
//    val segmentPoints : SnapshotStateList<Pair<Offset, Offset>> = mutableStateListOf()
    val segmentPoints : SnapshotStateList<Offset> = mutableStateListOf()
)


// Contains attributes for pen and eraser as well as the drawing mode
data class DrawInfo (
    val drawMode: DrawMode = DrawMode.Pen,
    val shape: Shape = Shape.Line,
    val color: Color = Color.Black,
    val strokeWidth: Float = 4f,
)

const val MAX_STROKE_WIDTH = 140f

enum class DrawMode { Pen, Eraser, Shape, CanvasDrag, Selection }

enum class ToolbarExtensionSetting { ColorSelection, StrokeWidthAdjustment, ShapeSelection, SelectorTool, Hidden }

@Serializable
enum class Shape { Rectangle, Oval, Line, StraightLine }

// Stores all the drawn items (lines, straight lines, rectangles, ovals, erasing lines)
var drawnItems = mutableStateListOf<DrawnItem>()


// Checks if a point is close enough to a rectangle/oval
fun isPointCloseToRectangle(point: Offset, item: DrawnItem): Boolean {
    if (item.shape != Shape.Rectangle && item.shape != Shape.Oval) {
        return false
    }
    if (item.segmentPoints.size < 2) {
        return false
    }

    val start = item.segmentPoints[0]
    val end = item.segmentPoints[1]

    val margin = 100f
    val minX = min(start.x, end.x)
    val maxX = max(start.x, end.x)
    val minY = min(start.y, end.y)
    val maxY = max(start.y, end.y)
    return point.x in (minX - margin)..(maxX + margin) && point.y in (minY - margin)..(maxY + margin) &&
            !(point.x in (minX + margin)..(maxX - margin) && point.y in (minY + margin)..(maxY - margin))
}



// Checks if a point is close enough to a straight line
fun isPointCloseToLine(point: Offset, item: DrawnItem): Boolean {
    if (item.shape != Shape.StraightLine) {
        return false
    }
    if (item.segmentPoints.size < 2) {
        return false
    }

    val start = item.segmentPoints[0]
    val end = item.segmentPoints[1]

    val x1 = start.x
    val y1 = start.y
    val x2 = end.x
    val y2 = end.y
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