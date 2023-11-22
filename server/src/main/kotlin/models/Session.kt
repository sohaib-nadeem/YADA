package models

import kotlinx.serialization.Serializable

@Serializable
data class CanvasObject(
    val shape: Shape = Shape.Line,
    val color: ULong = 0UL,
    val strokeWidth: Float = 4f,
    val segmentPoints : List<Offset> = listOf()
) {
    @Serializable
    data class Offset (val x: Float, val y: Float)

    @Serializable
    enum class Shape { Rectangle, Oval, Line, StraightLine }
}