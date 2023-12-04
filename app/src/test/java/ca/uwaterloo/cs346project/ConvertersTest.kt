package ca.uwaterloo.cs346project

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import ca.uwaterloo.cs346project.data.toCanvasObject
import ca.uwaterloo.cs346project.data.toDrawnItem
import ca.uwaterloo.cs346project.model.CanvasObject
import ca.uwaterloo.cs346project.model.Shape
import ca.uwaterloo.cs346project.ui.util.DrawnItem
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test

class ConvertersTest {
    @Test
    fun testToCanvasObject() {
        val drawnItem = DrawnItem(
            userObjectId = Pair(1, 2),
            shape = Shape.Line,
            color = Color(0xFF00FF00UL),
            strokeWidth = 5f,
            segmentPoints = mutableStateListOf(Offset(10f, 20f), Offset(30f, 40f))
        )

        val canvasObject = toCanvasObject(drawnItem)

        assertEquals(drawnItem.userObjectId, canvasObject.userObjectId)
        assertEquals(drawnItem.shape, canvasObject.shape)
        assertEquals(drawnItem.color.value, canvasObject.color)
        assertEquals(drawnItem.strokeWidth, canvasObject.strokeWidth)
        assertEquals(drawnItem.segmentPoints.toList(), canvasObject.segmentPoints.map { Offset(it.x, it.y) })
    }

    @Test
    fun testToDrawnItem() {
        val canvasObject = CanvasObject(
            userObjectId = Pair(1, 2),
            shape = Shape.Line,
            color = 0xFF00FF00UL,
            strokeWidth = 5f,
            segmentPoints = listOf(CanvasObject.Offset(10f, 20f), CanvasObject.Offset(30f, 40f))
        )

        val drawnItem = toDrawnItem(canvasObject)

        assertEquals(canvasObject.userObjectId, drawnItem.userObjectId)
        assertEquals(canvasObject.shape, drawnItem.shape)
        assertEquals(Color(canvasObject.color), drawnItem.color)
        assertEquals(canvasObject.strokeWidth, drawnItem.strokeWidth)
        assertEquals(canvasObject.segmentPoints.map { Offset(it.x, it.y) }, drawnItem.segmentPoints)
    }

    // helper function to compare two drawnItems
    private fun areDrawnItemsEqual(item1: DrawnItem, item2: DrawnItem): Boolean {
        if (item1.userObjectId != item2.userObjectId ||
            item1.shape != item2.shape ||
            item1.color != item2.color ||
            item1.strokeWidth != item2.strokeWidth ||
            item1.segmentPoints.size != item2.segmentPoints.size) {
            return false
        }

        return item1.segmentPoints.zip(item2.segmentPoints).all { (offset1, offset2) ->
            offset1 == offset2
        }
    }

    // Round-trip conversion tests
    @Test
    fun testDrawnItemToCanvasObjectAndBack() {
        val originalDrawnItem = DrawnItem(
            userObjectId = Pair(1, 2),
            shape = Shape.Line,
            color = Color(0xFF00FF00UL),
            strokeWidth = 5f,
            segmentPoints = mutableStateListOf(Offset(10f, 20f), Offset(30f, 40f))
        )

        val canvasObject = toCanvasObject(originalDrawnItem)
        val convertedBackDrawnItem = toDrawnItem(canvasObject)

        assertTrue(areDrawnItemsEqual(originalDrawnItem, convertedBackDrawnItem))
    }


    @Test
    fun testCanvasObjectToDrawnItemAndBack() {
        val originalCanvasObject = CanvasObject(
            userObjectId = Pair(1, 2),
            shape = Shape.Line,
            color = 0xFF00FF00UL,
            strokeWidth = 5f,
            segmentPoints = listOf(CanvasObject.Offset(10f, 20f), CanvasObject.Offset(30f, 40f))
        )

        val drawnItem = toDrawnItem(originalCanvasObject)
        val convertedBackCanvasObject = toCanvasObject(drawnItem)

        assertEquals(originalCanvasObject, convertedBackCanvasObject)
    }

}