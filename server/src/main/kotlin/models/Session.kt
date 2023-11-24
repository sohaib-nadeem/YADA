package models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.or

object Session: IntIdTable(columnName = "sessionId") {
    val sessionCode = char("sessionCode", 6)
}

object ActiveUser: IntIdTable(columnName = "userId") {
    val sessionId = integer("sessionId")
    val initialReceiveObjectId = integer("initialReceiveObjectId")
}

// make object Id local to sessions!!!
// (objectId should be a sequence but separate for each session)
object SessionObject: IntIdTable(columnName = "objectId") {
    val sessionId = integer("sessionId")
    val userId = integer("userId")
    val shape = uinteger("shape") //.check { it less CanvasObject.Shape.size} //(it eq 'R') or (it eq 'O') or (it eq 'L') or (it eq 'S')}
    val color = long("color")
    val strokeWidth = float("strokeWidth")
    // how to store segmentPoints???
    // array not supported; possible approaches: json, serialized object, use another table
}

// add index with object id and sequence number as key columns
object ObjectPoint: Table() {
    val objectId = integer("objectId")
    val sequenceNumber = integer("sequenceNumber") // make it auto increment/sequence???
    val xVal = float("xVal")
    val yVal = float("yVal") // array not supported; possible approaches: json, serialized object, use another table
}

// add foreign key references!!!

@Serializable
data class CanvasObject(
    val objectId: Int,
    val shape: Shape = Shape.Line,
    val color: ULong = 0UL,
    val strokeWidth: Float = 4f,
    var segmentPoints : List<Offset> = listOf()
) {
    @Serializable
    data class Offset (val x: Float, val y: Float)

    @Serializable
    enum class Shape(val value: UInt) {
        Rectangle(0U),
        Oval(1U),
        Line(2U),
        StraightLine(3U);

        fun toUInt() = this.value

        companion object {
            val size = Shape.values().size
            fun fromUInt(value: UInt) = Shape.values().first { it.value == value }
        }
    }
}