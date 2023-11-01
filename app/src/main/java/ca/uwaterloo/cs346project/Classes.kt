package ca.uwaterloo.cs346project

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
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
    var end: Offset = Offset(0f, 0f)
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
