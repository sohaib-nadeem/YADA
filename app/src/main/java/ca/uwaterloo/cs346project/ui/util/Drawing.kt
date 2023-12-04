package ca.uwaterloo.cs346project.ui.util

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import ca.uwaterloo.cs346project.model.Shape

// Drawing functions

// Helper function to draw a given DrawnItem
fun DrawScope.drawItem(item: DrawnItem) {
    when (item.shape) {
        Shape.Line, Shape.StraightLine -> {
            for (i in 1 until item.segmentPoints.size) {
                drawLine(
                    color = item.color,
                    start = item.segmentPoints[i - 1],
                    end = item.segmentPoints[i],
                    strokeWidth = item.strokeWidth,
                    cap = StrokeCap.Round
                )
            }
        }
        Shape.Rectangle -> {
            val start = item.segmentPoints[0]
            val end = item.segmentPoints[1]
            drawRect(
                color = item.color,
                topLeft = start,
                size = Size(
                    width = end.x - start.x,
                    height = end.y - start.y
                ),
                style = Stroke(width = item.strokeWidth)
            )
        }
        Shape.Oval -> {
            val start = item.segmentPoints[0]
            val end = item.segmentPoints[1]
            drawOval(
                color = item.color,
                topLeft = start,
                size = Size(
                    width = end.x - start.x,
                    height = end.y - start.y
                ),
                style = Stroke(width = item.strokeWidth)
            )
        }
        // Add other shapes as needed
    }
}

// Helper function to draw boxes on corners/endpoints of a shape
fun DrawScope.drawShapeCorners(item: DrawnItem, cornerSize: Float = 40f) {
    if (item.shape == Shape.Rectangle || item.shape == Shape.Oval) {
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
}