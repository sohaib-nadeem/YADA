package ca.uwaterloo.cs346project

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import org.junit.Test
import org.junit.Assert.*

class WhiteboardTest {
    // Tests the lineIntercept() helper function
    // The two line segments intersect on the coordinate plane when they share at least one common point
    @Test
    fun testLinesIntersect() {
        var segment1 = Pair(Offset(0f,0f),Offset(5f,5f))
        var segment2 = Pair(Offset(5f,5f),Offset(4f,6f))
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

        val result = transformAmount(zoom, Offset(0f, 0f), amountToTransform)

        val expected = Offset(50f, 50f) // (100 / 2, 100 / 2)
        assertEquals(expected, result)
    }

    @Test
    fun testTransformAmount_ZoomLevelOne() {
        val zoom = 1f // Edge case: Zoom level exactly 1
        val amountToTransform = Offset(100f, 100f)

        val result = transformAmount(zoom, Offset(0f, 0f), amountToTransform)

        val expected = Offset(100f, 100f) // (100 / 1, 100 / 1)
        assertEquals(expected, result)
    }

    @Test
    fun testTransformAmount_HighZoomLevel() {
        val zoom = 100f // Edge case: Very high zoom level
        val amountToTransform = Offset(100f, 100f)

        val result = transformAmount(zoom, Offset(0f, 0f), amountToTransform)

        val expected = Offset(1f, 1f) // (100 / 100, 100 / 100)
        assertEquals(expected, result)
    }

}