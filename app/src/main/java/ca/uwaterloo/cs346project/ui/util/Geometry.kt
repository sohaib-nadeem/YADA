package ca.uwaterloo.cs346project.ui.util

import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import ca.uwaterloo.cs346project.model.Shape
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

// Helper functions for zoom and drag on canvas
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

fun transformAmount(zoom: Float, amountToTransform: Offset): Offset {
    return Offset(amountToTransform.x / zoom, amountToTransform.y / zoom)
}

// Helper functions for erasing and shape selection

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

// removes the erased items (those that intersect the last segment in eraseLine) from drawnItems and returns them
fun eraseIntersectingItems(eraseLine: DrawnItem?, drawnItems: MutableList<DrawnItem>): List<DrawnItem> {
    val erasedItems = mutableListOf<DrawnItem>()
    val remainingItems = mutableListOf<DrawnItem>()

    if (eraseLine != null && eraseLine!!.segmentPoints.size >= 2) {
        drawnItems.forEach { item ->
            if (checkIntersection(Pair(eraseLine!!.segmentPoints[eraseLine!!.segmentPoints.size - 2], eraseLine!!.segmentPoints.last()), item)) {
                erasedItems.add(item)
            } else {
                remainingItems.add(item)
            }
        }
        drawnItems.clear()
        drawnItems.addAll(remainingItems)
    }

    return erasedItems.toList()
}
