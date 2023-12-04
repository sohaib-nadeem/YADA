package ca.uwaterloo.cs346project

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import ca.uwaterloo.cs346project.model.Shape
import ca.uwaterloo.cs346project.ui.util.DrawnItem
import ca.uwaterloo.cs346project.ui.util.calculateNewOffset
import ca.uwaterloo.cs346project.ui.util.checkIntersection
import ca.uwaterloo.cs346project.ui.util.eraseIntersectingItems
import ca.uwaterloo.cs346project.ui.util.isPointCloseToLine
import ca.uwaterloo.cs346project.ui.util.isPointCloseToRectangle
import ca.uwaterloo.cs346project.ui.util.linesIntersect
import ca.uwaterloo.cs346project.ui.util.transformAmount
import ca.uwaterloo.cs346project.ui.util.transformOffset
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

class GeometryTest {
    // Tests the lineIntercept() helper function
    // The two line segments intersect on the coordinate plane when they share at least one common point
    @Test
    fun testLinesIntersect() {
        val segment1 = Pair(Offset(0f,0f),Offset(5f,5f))
        val segment2 = Pair(Offset(5f,5f),Offset(4f,6f))
        assert(linesIntersect(segment1,segment2))
    }

    @Test
    fun testLinesNotIntersect() {
        val segment1 = Pair(Offset(0f,0f),Offset(5f,5f))
        val segment2 = Pair(Offset(5f,6f),Offset(4f,6f))
        assert(!linesIntersect(segment1,segment2))
    }

    @Test
    fun testLineIntersectWithPoint() {
        val point = Pair(Offset(3f,3f),Offset(3f,3f))
        val segment = Pair(Offset(5f,5f),Offset(1f,1f))
        assert(linesIntersect(point,segment))
    }

    // Tests for checkIntersection() helper function.
    @Test
    fun testLineIntersectionWithRectangle() {
        val line = Pair(Offset(5f, 5f), Offset(15f, 15f))
        val item = DrawnItem(
            shape = Shape.Rectangle,
            segmentPoints = mutableStateListOf(Offset(10f, 10f), Offset(20f, 20f))
        )

        assert(checkIntersection(line, item))
    }

    @Test
    fun testLineNoIntersectionWithLine() {
        val line = Pair(Offset(0f, 0f), Offset(10f, 10f))
        val item = DrawnItem(
            shape = Shape.Line,
            segmentPoints = mutableStateListOf(Offset(20f, 20f), Offset(30f, 30f))
        )

        assertFalse(checkIntersection(line, item))
    }
    @Test
    fun testLineNoIntersectionWithRectangle() {
        val line = Pair(Offset(0f, 0f), Offset(5f, 5f))
        val item = DrawnItem(
            shape = Shape.Rectangle,
            segmentPoints = mutableStateListOf(Offset(10f, 10f), Offset(20f, 20f))
        )

        assertFalse(checkIntersection(line, item))
    }
    @Test
    fun testLineNoIntersectionWithOval() {
        val line = Pair(Offset(0f, 0f), Offset(5f, 5f))
        val item = DrawnItem(
            shape = Shape.Oval,
            segmentPoints = mutableStateListOf(Offset(10f, 10f), Offset(20f, 20f))
        )

        assertFalse(checkIntersection(line, item))
    }

    @Test
    fun testLineNoIntersectionWithStraightLine() {
        val line = Pair(Offset(1f, 1f), Offset(2f, 2f))
        val item = DrawnItem(
            shape = Shape.StraightLine,
            segmentPoints = mutableStateListOf(Offset(3f, 3f), Offset(4f, 4f))
        )

        assertFalse(checkIntersection(line, item))
    }

    // Test cases for edge conditions like zero-length lines, points
    @Test
    fun testIntersectionWithZeroLengthLine() {
        val line = Pair(Offset(0f, 0f), Offset(0f, 0f))
        var item = DrawnItem(
            shape = Shape.Line,
            segmentPoints = mutableStateListOf(Offset(0f, 0f), Offset(5f, 5f))
        )
        assertTrue(checkIntersection(line, item))
        item = DrawnItem(
            shape = Shape.Rectangle,
            segmentPoints = mutableStateListOf(Offset(0f, 0f), Offset(5f, 5f))
        )
        assertTrue(checkIntersection(line, item))
    }

    @Test
    fun testNoIntersectionWithPoint() {
        val line = Pair(Offset(1f, 1f), Offset(5f, 5f))
        val item = DrawnItem(
            shape = Shape.Line,
            segmentPoints = mutableStateListOf(Offset(0f, 0f)) // Representing a point
        )

        assertFalse(checkIntersection(line, item))
    }

    // Tests for calculateNewOffset() helper function
    @Test
    fun testCalculateNewOffset() {
        val offset = Offset(200f, 150f)
        val centroid = Offset(100f, 75f)
        val pan = Offset(30f, 20f)
        val zoom = 2f
        val gestureZoom = 1.5f
        val size = IntSize(400, 300)

        val result = offset.calculateNewOffset(centroid, pan, zoom, gestureZoom, size)

        val expected = Offset(200f, 150f)
        assertEquals(expected, result)
    }

    @Test
    fun testCalculateNewOffset_HighZoomLevel() {
        val offset = Offset(100f, 100f)
        val centroid = Offset(50f, 50f)
        val pan = Offset(20f, 20f)
        val zoom = 10f
        val gestureZoom = 2f
        val size = IntSize(500, 500)

        val result = offset.calculateNewOffset(centroid, pan, zoom, gestureZoom, size)

        val expected = Offset(100.5f, 100.5f)
        assertEquals(expected, result)
    }

    @Test
    fun testCalculateNewOffset_ZeroSize() {
        val offset = Offset(100f, 100f)
        val centroid = Offset(50f, 50f)
        val pan = Offset(20f, 20f)
        val zoom = 2f
        val gestureZoom = 1f
        val size = IntSize(0, 0) // Edge case: Zero Size

        val result = offset.calculateNewOffset(centroid, pan, zoom, gestureZoom, size)

        val expected = Offset(0f, 0f) // Assuming the function handles zero size by returning the original offset
        assertEquals(expected, result)
    }

    @Test
    fun testCalculateNewOffset_ZeroPan() {
        val offset = Offset(100f, 100f)
        val centroid = Offset(50f, 50f)
        val pan = Offset(0f, 0f) // Edge case: Zero Pan Value
        val zoom = 2f
        val gestureZoom = 1f
        val size = IntSize(500, 500)

        val result = offset.calculateNewOffset(centroid, pan, zoom, gestureZoom, size)

        val expected = Offset(100f, 100f)
        assertEquals(expected, result)
    }

    // Tests for transformOffset() helper function
    @Test
    fun testTransformOffset_Typical() {
        val zoom = 2f
        val offset = Offset(50f, 50f)
        val offsetToTransform = Offset(100f, 100f)

        val result = transformOffset(zoom, offset, offsetToTransform)

        val expected = Offset(100f, 100f) // (100 / 2 + 50, 100 / 2 + 50)
        assertEquals(expected, result)
    }

    @Test
    fun testTransformOffset_ZoomLevelOne() {
        val zoom = 1f // Edge case: Zoom level exactly 1
        val offset = Offset(50f, 50f)
        val offsetToTransform = Offset(100f, 100f)

        val result = transformOffset(zoom, offset, offsetToTransform)

        val expected = Offset(150f, 150f) // (100 / 1 + 50, 100 / 1 + 50)
        assertEquals(expected, result)
    }

    @Test
    fun testTransformOffset_HighZoomLevel() {
        val zoom = 100f // Edge case: Very high zoom level
        val offset = Offset(50f, 50f)
        val offsetToTransform = Offset(100f, 100f)

        val result = transformOffset(zoom, offset, offsetToTransform)

        val expected = Offset(51f, 51f) // (100 / 100 + 50, 100 / 100 + 50)
        assertEquals(expected, result)
    }

    // Tests for transformAmount() helper function
    @Test
    fun testTransformAmount_Typical() {
        val zoom = 2f
        val amountToTransform = Offset(100f, 100f)

        val result = transformAmount(zoom, amountToTransform)

        val expected = Offset(50f, 50f) // (100 / 2, 100 / 2)
        assertEquals(expected, result)
    }

    @Test
    fun testTransformAmount_ZoomLevelOne() {
        val zoom = 1f // Edge case: Zoom level exactly 1
        val amountToTransform = Offset(100f, 100f)

        val result = transformAmount(zoom, amountToTransform)

        val expected = Offset(100f, 100f) // (100 / 1, 100 / 1)
        assertEquals(expected, result)
    }

    @Test
    fun testTransformAmount_HighZoomLevel() {
        val zoom = 100f // Edge case: Very high zoom level
        val amountToTransform = Offset(100f, 100f)

        val result = transformAmount(zoom, amountToTransform)

        val expected = Offset(1f, 1f) // (100 / 100, 100 / 100)
        assertEquals(expected, result)
    }



    // Tests for isPointCloseToRectangle function:

    // Helper function to create a rectangle drawn item
    private fun createRectangleItem(start: Offset, end: Offset): DrawnItem {
        return DrawnItem(
            shape = Shape.Rectangle,
            segmentPoints = mutableStateListOf(start, end)
        )
    }

    @Test
    fun testPointInsideRectangle() {
        val item = createRectangleItem(Offset(0f, 0f), Offset(100f, 100f))
        assertTrue(isPointCloseToRectangle(Offset(50f, 50f), item))
    }

    @Test
    fun testPointOutsideButCloseToRectangle() {
        val item = createRectangleItem(Offset(0f, 0f), Offset(100f, 100f))
        assertTrue(isPointCloseToRectangle(Offset(105f, 105f), item))
    }

    @Test
    fun testPointFarFromRectangle() {
        val item = createRectangleItem(Offset(0f, 0f), Offset(100f, 100f))
        assertFalse(isPointCloseToRectangle(Offset(201f, 201f), item))
    }

    @Test
    fun testPointOnRectangleEdge() {
        val item = createRectangleItem(Offset(0f, 0f), Offset(100f, 100f))
        assertTrue(isPointCloseToRectangle(Offset(0f, 50f), item))
    }

    @Test
    fun testPointExactlyAtMarginBoundary() {
        val item = createRectangleItem(Offset(100f, 100f), Offset(200f, 200f))
        assertTrue(isPointCloseToRectangle(Offset(0f, 100f), item))
    }

    @Test
    fun testPointExactlyAtInnerMarginBoundary() {
        val item = createRectangleItem(Offset(0f, 0f), Offset(200f, 200f))
        assertFalse(isPointCloseToRectangle(Offset(100f, 100f), item))
    }

    @Test
    fun testNegativePointCoordinates() {
        val item = createRectangleItem(Offset(-200f, -200f), Offset(-100f, -100f))
        assertTrue(isPointCloseToRectangle(Offset(-150f, -150f), item))
    }

    @Test
    fun testLargeRectangle() {
        val item = createRectangleItem(Offset(0f, 0f), Offset(5000f, 5000f))
        assertTrue(isPointCloseToRectangle(Offset(5050f, 5050f), item))
    }

    @Test
    fun testRectangleWithZeroSize() {
        val item = createRectangleItem(Offset(100f, 100f), Offset(100f, 100f))
        assertTrue(isPointCloseToRectangle(Offset(100f, 100f), item))
    }

    @Test
    fun testPointCloseToCornerInsideMargin() {
        val item = createRectangleItem(Offset(100f, 100f), Offset(200f, 200f))
        assertTrue(isPointCloseToRectangle(Offset(90f, 90f), item))
    }

    @Test
    fun testOvalShape() {
        val item = DrawnItem(
            shape = Shape.Oval,
            segmentPoints = mutableStateListOf(Offset(100f, 100f), Offset(200f, 200f))
        )
        assertTrue(isPointCloseToRectangle(Offset(150f, 150f), item))
    }

    @Test
    fun testNonRectangleOvalShapes() {
        val item = DrawnItem(
            shape = Shape.Line, // Non-rectangle shape
            segmentPoints = mutableStateListOf(Offset(100f, 100f), Offset(200f, 200f))
        )
        assertFalse(isPointCloseToRectangle(Offset(150f, 150f), item))
    }

    @Test
    fun testRectangleEdgesAtMargin() {
        val item = createRectangleItem(Offset(100f, 100f), Offset(200f, 200f))
        assertTrue(isPointCloseToRectangle(Offset(300f, 300f), item))
    }

    @Test
    fun testPointsOnTheAxis() {
        val item = createRectangleItem(Offset(100f, 100f), Offset(200f, 200f))
        assertTrue(isPointCloseToRectangle(Offset(100f, 0f), item))
        assertTrue(isPointCloseToRectangle(Offset(0f, 100f), item))
    }


    // Tests for isPointCloseToLine function:

    // Helper function to create a straight line drawn item
    private fun createStraightLineItem(start: Offset, end: Offset): DrawnItem {
        return DrawnItem(
            shape = Shape.StraightLine,
            segmentPoints = mutableStateListOf(start, end),
            strokeWidth = 4f  // Example stroke width, adjust as needed
        )
    }

    @Test
    fun testPointExactlyOnTheLine() {
        val item = createStraightLineItem(Offset(0f, 0f), Offset(100f, 100f))
        assertTrue(isPointCloseToLine(Offset(50f, 50f), item))
    }

    @Test
    fun testPointWithinThresholdDistance() {
        val item = createStraightLineItem(Offset(0f, 0f), Offset(100f, 100f))
        assertTrue(isPointCloseToLine(Offset(50f, 55f), item))  // Slightly above the line
    }

    @Test
    fun testPointOutsideThresholdDistance() {
        val item = createStraightLineItem(Offset(0f, 0f), Offset(100f, 100f))
        assertFalse(isPointCloseToLine(Offset(50f, 160f), item))  // Far above the line
    }

    @Test
    fun testPointCloseToLineStart() {
        val item = createStraightLineItem(Offset(0f, 0f), Offset(100f, 100f))
        assertTrue(isPointCloseToLine(Offset(5f, 5f), item))  // Close to start
    }

    @Test
    fun testPointCloseToLineEnd() {
        val item = createStraightLineItem(Offset(0f, 0f), Offset(100f, 100f))
        assertTrue(isPointCloseToLine(Offset(95f, 95f), item))  // Close to end
    }

    @Test
    fun testPointBeyondLineStart() {
        val item = createStraightLineItem(Offset(0f, 0f), Offset(100f, 100f))
        assertTrue(isPointCloseToLine(Offset(-5f, -5f), item))  // Just before the line starts
    }

    @Test
    fun testPointBeyondLineEnd() {
        val item = createStraightLineItem(Offset(0f, 0f), Offset(100f, 100f))
        assertTrue(isPointCloseToLine(Offset(105f, 105f), item))  // Just beyond the line ends
    }

    @Test
    fun testPointPerpendicularToLineMiddle() {
        val item = createStraightLineItem(Offset(0f, 0f), Offset(100f, 0f))
        assertTrue(isPointCloseToLine(Offset(50f, 10f), item))  // Perpendicular to the middle
    }

    @Test
    fun testHorizontalLine() {
        val item = createStraightLineItem(Offset(0f, 100f), Offset(100f, 100f))
        assertTrue(isPointCloseToLine(Offset(50f, 105f), item))  // Close to a horizontal line
    }

    @Test
    fun testVerticalLine() {
        val item = createStraightLineItem(Offset(100f, 0f), Offset(100f, 100f))
        assertTrue(isPointCloseToLine(Offset(105f, 50f), item))  // Close to a vertical line
    }

    @Test
    fun testDiagonalLine() {
        val item = createStraightLineItem(Offset(0f, 0f), Offset(100f, 100f))
        assertTrue(isPointCloseToLine(Offset(75f, 75f), item))  // On a diagonal line
    }

    @Test
    fun testZeroLengthLine() {
        val item = createStraightLineItem(Offset(100f, 100f), Offset(100f, 100f))
        assertTrue(isPointCloseToLine(Offset(100f, 100f), item))  // Point on a zero-length line
    }

    @Test
    fun testNegativeCoordinates() {
        val item = createStraightLineItem(Offset(-100f, -100f), Offset(-200f, -200f))
        assertTrue(isPointCloseToLine(Offset(-150f, -150f), item))  // Point with negative coordinates
    }


    // Tests for eraseIntersectingItems function

    private lateinit var drawnItems: MutableList<DrawnItem>

    @Before
    fun setUp() {
        drawnItems = mutableListOf()
    }

    // Helper method to create a DrawnItem with specific points
    private fun createDrawnItemWithPoints(segmentPoints: List<Offset>): DrawnItem {
        // Assuming DrawnItem takes a list of Offset points in its constructor
        return DrawnItem(userObjectId = Pair(1, 1), // Example ID
            shape = Shape.Line, // Assuming it's a line
            segmentPoints = mutableStateListOf(*segmentPoints.toTypedArray()))
    }

    @Test
    fun testEraseLineIsNull() {
        val eraseLine: DrawnItem? = null
        val erasedItems = eraseIntersectingItems(eraseLine, drawnItems)
        assertTrue(erasedItems.isEmpty())
    }

    @Test
    fun testEraseLineHasLessThanTwoPoints() {
        val eraseLine = createDrawnItemWithPoints(listOf(Offset(0f, 0f)))
        val erasedItems = eraseIntersectingItems(eraseLine, drawnItems)
        assertTrue(erasedItems.isEmpty())
    }

    @Test
    fun testNoIntersection() {
        val eraseLine = createDrawnItemWithPoints(listOf(Offset(0f, 0f), Offset(10f, 10f)))
        drawnItems.add(createDrawnItemWithPoints(listOf(Offset(20f, 20f), Offset(30f, 30f))))
        val erasedItems = eraseIntersectingItems(eraseLine, drawnItems)
        assertTrue(erasedItems.isEmpty())
    }

    @Test
    fun testSingleIntersection() {
        val eraseLine = createDrawnItemWithPoints(listOf(Offset(0f, 0f), Offset(10f, 10f)))
        val intersectingItem = createDrawnItemWithPoints(listOf(Offset(5f, 5f), Offset(15f, 15f)))
        drawnItems.add(intersectingItem)
        val erasedItems = eraseIntersectingItems(eraseLine, drawnItems)
        assertEquals(1, erasedItems.size)
        assertTrue(intersectingItem in erasedItems)
    }

    @Test
    fun testMultipleIntersections() {
        val eraseLine = createDrawnItemWithPoints(listOf(Offset(0f, 0f), Offset(10f, 10f)))
        val intersectingItem1 = createDrawnItemWithPoints(listOf(Offset(5f, 5f), Offset(15f, 15f)))
        val intersectingItem2 = createDrawnItemWithPoints(listOf(Offset(3f, 3f), Offset(7f, 7f)))
        val nonIntersectingItem = createDrawnItemWithPoints(listOf(Offset(20f, 20f), Offset(30f, 30f)))

        drawnItems.addAll(listOf(intersectingItem1, intersectingItem2, nonIntersectingItem))
        val erasedItems = eraseIntersectingItems(eraseLine, drawnItems)

        assertEquals(2, erasedItems.size)
        assertTrue(erasedItems.containsAll(listOf(intersectingItem1, intersectingItem2)))
        assertFalse(erasedItems.contains(nonIntersectingItem))
        assertFalse(drawnItems.containsAll(listOf(intersectingItem1, intersectingItem2)))
        assertTrue(drawnItems.contains(nonIntersectingItem))
    }


    @Test
    fun testAllItemsIntersect() {
        val eraseLine = createDrawnItemWithPoints(listOf(Offset(0f, 0f), Offset(10f, 10f)))
        val intersectingItem1 = createDrawnItemWithPoints(listOf(Offset(5f, 5f), Offset(15f, 15f)))
        val intersectingItem2 = createDrawnItemWithPoints(listOf(Offset(2f, 2f), Offset(8f, 8f)))

        drawnItems.addAll(listOf(intersectingItem1, intersectingItem2))
        val erasedItems = eraseIntersectingItems(eraseLine, drawnItems)

        assertEquals(2, erasedItems.size)
        assertTrue(erasedItems.containsAll(listOf(intersectingItem1, intersectingItem2)))
        assertTrue(drawnItems.isEmpty())
    }

}