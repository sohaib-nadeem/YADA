package ca.uwaterloo.cs346project.ui.util

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import ca.uwaterloo.cs346project.client
import ca.uwaterloo.cs346project.model.Shape

data class DrawnItem(
    // pair of user id and a user-specific object id
    val userObjectId: Pair<Int, Int> = Pair(-1, 0),
    val shape: Shape = Shape.Line,
    val color: Color = Color.Black,
    val strokeWidth: Float = 4f,
    val segmentPoints: SnapshotStateList<Offset> = mutableStateListOf()
) {
    companion object {
        var maxUserObjectId = 0
        fun newUserObjectId(): Pair<Int, Int> {
            val userObjectId = maxUserObjectId
            maxUserObjectId++
            return Pair(client.user_id, userObjectId)
        }
    }
}

// Contains attributes for pen and eraser as well as the drawing mode
data class DrawInfo (
    val drawMode: DrawMode = DrawMode.Pen,
    val shape: Shape = Shape.Line,
    val color: Color = Color.Black,
    val strokeWidth: Float = 4f,
)

const val MAX_STROKE_WIDTH = 140f

enum class DrawMode { Pen, Eraser, Shape, CanvasDrag, Selection }

enum class ToolbarExtensionSetting { ColorSelection, StrokeWidthAdjustment, ShapeSelection, Hidden }

enum class PageType {HomePage, WhiteboardPage}
