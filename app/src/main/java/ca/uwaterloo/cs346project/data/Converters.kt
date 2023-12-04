package ca.uwaterloo.cs346project.data

import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import ca.uwaterloo.cs346project.model.CanvasObject
import ca.uwaterloo.cs346project.ui.util.DrawnItem

fun toCanvasObject(item: DrawnItem): CanvasObject {
    return CanvasObject(
        item.userObjectId,
        item.shape,
        item.color.value,
        item.strokeWidth,
        item.segmentPoints.toList().map{
            CanvasObject.Offset(it.x, it.y)
        }
    )
}

fun toDrawnItem(canvasobject: CanvasObject): DrawnItem {
    return DrawnItem(
        canvasobject.userObjectId,
        canvasobject.shape,
        Color(canvasobject.color),
        canvasobject.strokeWidth,
        canvasobject.segmentPoints.map {
            Offset(it.x,it.y)
        }.toMutableStateList()
    )
}