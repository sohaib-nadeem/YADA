package models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.or

/************************ Database Entities ************************/

// Used to store information about all sessions
object Session: IntIdTable(columnName = "sessionId") {
    val sessionCode = char("sessionCode", 6)
}

// Used to store information about users active in each session
object ActiveUser: IntIdTable(columnName = "userId") {
    val sessionId = integer("sessionId") // foreign key reference

    // initialReceiveActionId indicates which starting action id to use for next receive call
    val initialReceiveActionId = integer("initialReceiveActionId")
}

// Used to store all actions performed in each session
object SessionAction: IntIdTable(columnName = "actionId") {
    val sessionId = integer("sessionId") // foreign key reference
    val userId = integer("userId") // foreign key reference

    val actionType = uinteger("actionType")
}

// used to store the items in an Action<CanvasObject>
//  segmentPoints stored in ObjectPoint table
object ActionObject: IntIdTable(columnName = "objectId") {
    val actionId = integer("actionId") // foreign key reference

    val shape = uinteger("shape")
    val color = long("color")
    val strokeWidth = float("strokeWidth")
    val userObjectIdUser = integer("userObjectIdUser")
    val userObjectIdObject = integer("userObjectIdObject")
}

// used to store the segmentPoints in a CanvasObject
object ObjectPoint: Table() {
    val objectId = integer("objectId") // foreign key reference

    val sequenceNumber = integer("sequenceNumber")
    val xVal = float("xVal")
    val yVal = float("yVal")
}

/************************ Domain Models ************************/

@Serializable
data class CanvasObject(
    val userObjectId: Pair<Int, Int>,
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

@Serializable
data class Action<T>(
    val type: ActionType,
    // MODIFY: items stores 2 (only 2) DrawnItem objects, [oldState, newState] (order preserved)
    // REMOVE: items stores the DrawnItem objects to be removed from drawnItems array
    // ADD: items stores a list of DrawnItem objects to be added to drawnItems array
    var items: List<T>, // The items involved in the action
)

@Serializable
enum class ActionType(val value: UInt) {
    ADD(0U), // When a new item is added
    REMOVE(1U), // When an item is removed (e.g., erasing)
    MODIFY(2U); // When an item is modified (e.g., moved or resized)

    fun toUInt() = this.value

    companion object {
        val size = ActionType.values().size
        fun fromUInt(value: UInt) = ActionType.values().first { it.value == value }
    }
}
