package ca.uwaterloo.cs346project.model

import kotlinx.serialization.Serializable

@Serializable
data class CanvasObject(
    val userObjectId: Pair<Int, Int>,
    val shape: Shape = Shape.Line,
    val color: ULong = 0UL,
    val strokeWidth: Float = 4f,
    val segmentPoints : List<Offset> = listOf()
) {
    @Serializable
    data class Offset (val x: Float, val y: Float)

}

@Serializable
enum class Shape(val value: UInt) {
    Rectangle(0U),
    Oval(1U),
    Line(2U),
    StraightLine(3U);
}
