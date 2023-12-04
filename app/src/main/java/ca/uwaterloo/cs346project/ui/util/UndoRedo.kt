package ca.uwaterloo.cs346project.ui.util

import android.util.Log
import ca.uwaterloo.cs346project.model.Action
import ca.uwaterloo.cs346project.model.ActionType


// Undo and Redo related helper functions
fun applyAction(action: Action<DrawnItem>, drawnItems: MutableList<DrawnItem>) {
    when (action.type) {
        ActionType.ADD -> {
            drawnItems.addAll(action.items)
        }
        ActionType.REMOVE -> {
            action.items.forEach { actionItem -> drawnItems.removeAll { actionItem.userObjectId == it.userObjectId }  }
        }
        ActionType.MODIFY -> {
            if (action.items.size != 2) {
                Log.d("performRedo", "ERROR: 'items' field does have 2 DrawnItem")
                throw IllegalArgumentException("ERROR: 'items' field does have 2 DrawnItem")
            } else {
                val index = drawnItems.indexOfFirst { it.userObjectId == action.items[0].userObjectId }
                if (index != -1) {
                    drawnItems[index] = action.items[1]
                }
            }
        }
    }
}

fun createReversedAction(action: Action<DrawnItem>): Action<DrawnItem> {
    when (action.type) {
        ActionType.ADD -> {
            return Action(ActionType.REMOVE, action.items)
        }
        ActionType.REMOVE -> {
            return Action(ActionType.ADD, action.items)
        }
        ActionType.MODIFY -> {
            if (action.items.size != 2) {
                Log.d("createReversedAction", "ERROR: 'items' field does have 2 DrawnItem")
                throw IllegalArgumentException("ERROR: 'items' field does have 2 DrawnItem")
            } else {
                return Action(ActionType.MODIFY, listOf(action.items[1], action.items[0]))
            }
        }
    }
}


fun performUndo(drawnItems: MutableList<DrawnItem>, undoStack: MutableList<Action<DrawnItem>>, redoStack: MutableList<Action<DrawnItem>>) {
    Log.d("performUndo", "performUndo Triggered")
    undoStack.removeLastOrNull()?.let { lastAction ->
        applyAction(createReversedAction(lastAction), drawnItems)
        // Move the action to redoStack
        redoStack.add(lastAction)
    }
}


fun performRedo(drawnItems: MutableList<DrawnItem>, undoStack: MutableList<Action<DrawnItem>>, redoStack: MutableList<Action<DrawnItem>>) {
    Log.d("performRedo", "performRedo Triggered")
    redoStack.removeLastOrNull()?.let { lastAction ->
        applyAction(lastAction, drawnItems)
        // Move the action back to undoStack
        undoStack.add(lastAction)
    }
}