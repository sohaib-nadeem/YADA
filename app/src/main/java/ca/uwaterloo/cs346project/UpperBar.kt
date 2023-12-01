package ca.uwaterloo.cs346project

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import dev.shreyaspatil.capturable.controller.CaptureController
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun UpperBarIconButton(icon: ImageVector, color: Color, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(icon, contentDescription = "Localized description", modifier = Modifier
            .background(
                color = color,
                shape = CircleShape,
            )
            .padding(5.dp)
        )
    }
}
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


@Composable
fun UpperBar(
    drawnItems: SnapshotStateList<DrawnItem>,
    undoStack: MutableList<Action<DrawnItem>>,
    redoStack: MutableList<Action<DrawnItem>>,
    page: Pg,
    setPage: (Pg) -> Unit,
    captureController: CaptureController
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        Box(modifier = Modifier.weight(3f)) {
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.Top,
                modifier = Modifier
                    .fillMaxWidth()

            ) {
                UpperBarIconButton(
                    icon = ImageVector.vectorResource(id = R.drawable.arrow_back_24px),
                    color = Color.LightGray
                ) {
                    if (!offline) {
                        val sessionIdFile = File(context.filesDir, "session_id.txt")
                        sessionIdFile.writeText(client.session_id.toString())
                    }
                    setPage(page.copy(curPage = CurrentPage.HomePage))
                }
                Button(onClick = {}) {
                    if (offline) Text("Offline")
                    else Text("Session ID: ${client.session_id.toString()}")
                }


                /*UpperBarIconButton(ImageVector.vectorResource(id = R.drawable.save_24px), color = Color.LightGray) {
                    // save button
                    captureController.capture()
                }*/

                // Undo button
                UpperBarIconButton(
                    ImageVector.vectorResource(id = R.drawable.undo_24px),
                    color = Color.LightGray
                ) {
                    if (undoStack.size >= 1) {
                        performUndo(drawnItems, undoStack, redoStack)

                        // also send action to server if online
                        if (!offline) {
                            val actionToSend = redoStack.last()
                            scope.launch {
                                client.sendAction(createReversedAction(actionToSend))
                            }
                        }
                    }
                }

                // Redo button
                UpperBarIconButton(
                    ImageVector.vectorResource(id = R.drawable.redo_24px),
                    color = Color.LightGray
                ) {
                    if (redoStack.isNotEmpty()) {
                        performRedo(drawnItems, undoStack, redoStack)

                        // also send action to server if online
                        if (!offline) {
                            val actionToSend = undoStack.last()
                            scope.launch {
                                client.sendAction(actionToSend)
                            }
                        }
                    }
                }

                UpperBarIconButton(
                    ImageVector.vectorResource(id = R.drawable.delete_24px),
                    color = Color.LightGray
                ) {
                    if (drawnItems.isNotEmpty()) {
                        val deleteAction = Action(
                            type = ActionType.REMOVE,
                            items = drawnItems.toList()
                        )

                        undoStack.add(deleteAction)
                        drawnItems.clear()
                        redoStack.clear()

                        // also send action to server if online
                        if (!offline) {
                            scope.launch {
                                client.sendAction(deleteAction)
                            }
                        }
                    }
                }

                UpperBarIconButton(
                    ImageVector.vectorResource(id = R.drawable.save_24px),
                    color = Color.LightGray
                ) {
                    // save button
                    captureController.capture()
                    // Display toast after saving
                    val text = "Saved as PDF in Downloads"
                    makeToast(context, text)
                }

                UpperBarIconButton(
                    ImageVector.vectorResource(id = R.drawable.upload_file_24px),
                    color = Color.LightGray
                ) {
                    // save button
                    //captureController.capture()
                }
            }
        }
    }
}