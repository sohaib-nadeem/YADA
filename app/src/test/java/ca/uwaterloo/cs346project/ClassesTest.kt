package ca.uwaterloo.cs346project

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.geometry.Offset
import org.junit.Test
import org.junit.Assert.*
class ClassesTest {
    // isPointCloseToRectangle Tests:

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


    // isPointCloseToLine Tests:

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
}
