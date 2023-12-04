package ca.uwaterloo.cs346project.model

import kotlinx.serialization.Serializable

@Serializable
data class Action<T>(
    val type: ActionType,
    // MODIFY: items stores 2 (only 2) DrawnItem objects, [oldState, newState] (order preserved)
    // REMOVE: items stores the DrawnItem objects to be removed from drawnItems array
    // ADD: items stores a list of DrawnItem objects to be added to drawnItems array
    val items: List<T>, // The items involved in the action
)

@Serializable
enum class ActionType(val value: UInt) {
    ADD(0U), // When a new item is added
    REMOVE(1U), // When an item is removed (e.g., erasing)
    MODIFY(2U); // When an item is modified (e.g., moved or resized)
}
