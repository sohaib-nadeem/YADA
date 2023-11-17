package ca.uwaterloo.cs346project

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.shreyaspatil.capturable.controller.CaptureController
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


fun performUndo(drawnItems: MutableList<DrawnItem>, undoStack: MutableList<Action>, redoStack: MutableList<Action>) {
    Log.d("performUndo", "performUndo Triggered")

    undoStack.removeLastOrNull()?.let { lastAction ->
        Log.d("performUndo", "${lastAction}")

        when (lastAction.type) {
            ActionType.ADD -> {
                Log.d("performUndo", "Undo ADD operation triggered")
                // Remove the items added by the last action
                drawnItems.removeAll(lastAction.items)
            }
            ActionType.REMOVE -> {
                Log.d("performUndo", "Undo REMOVE operation triggered")
                // Add back the items removed by the last action
                drawnItems.addAll(lastAction.items)
            }
            ActionType.MODIFY -> {
                Log.d("performUndo", "Undo MODIFY operation triggered")
                // Find and replace the modified item with its original state
                lastAction.items.firstOrNull()?.let { modifiedItem ->
                    val index = drawnItems.indexOfFirst { it == modifiedItem }
                    if (index != -1) {
                        drawnItems[index] = lastAction.additionalInfo as DrawnItem
                    }
                }
            }
        }
        // Move the action to redoStack
        redoStack.add(lastAction)
    }
}


fun performRedo(drawnItems: MutableList<DrawnItem>, undoStack: MutableList<Action>, redoStack: MutableList<Action>) {
    redoStack.removeLastOrNull()?.let { lastAction ->
        when (lastAction.type) {
            ActionType.ADD -> {
                // Add back the items that were previously added and then undone
                drawnItems.addAll(lastAction.items)
            }
            ActionType.REMOVE -> {
                // Remove the items that were previously removed and then undone
                drawnItems.removeAll(lastAction.items)
            }
            ActionType.MODIFY -> {
                // Find and revert the item to its modified state
                lastAction.additionalInfo?.let { originalItem ->
                    val index = drawnItems.indexOfFirst { it == originalItem }
                    if (index != -1) {
                        drawnItems[index] = lastAction.items.first()
                    }
                }
            }
        }
        // Move the action back to undoStack
        undoStack.add(lastAction)
    }
}





@Composable
fun UpperBar(
    drawnItems: SnapshotStateList<DrawnItem>,
    undoStack: MutableList<Action>,
    redoStack: MutableList<Action>,
    page: Pg,
    setPage: (Pg) -> Unit,
    captureController: CaptureController
){
    val context = LocalContext.current
    Row(horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ){
        Box(modifier = Modifier.weight(3f)){
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.Top,
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
                UpperBarIconButton(icon = ImageVector.vectorResource(id = R.drawable.arrow_back_24px), color = Color.LightGray) {
                    val sessionIdFile = File(context.filesDir, "session_id.txt")
                    sessionIdFile.writeText(client.session_id.toString())
                    setPage(page.copy(curPage = CurrentPage.HomePage))
                }
                Button(onClick = {}) {
                    Text("Session ID: ${client.session_id.toString()}")
                }

                // Undo button
                UpperBarIconButton(ImageVector.vectorResource(id = R.drawable.undo_24px), color = Color.LightGray) {
                    if (undoStack.size>=1) {
                        performUndo(drawnItems, undoStack, redoStack)
                    }
                }

                // Redo button
                UpperBarIconButton(ImageVector.vectorResource(id = R.drawable.redo_24px), color = Color.LightGray) {
                    if (redoStack.isNotEmpty()) {
                        performRedo(drawnItems, undoStack, redoStack)
                    }
                }

                UpperBarIconButton(ImageVector.vectorResource(id = R.drawable.delete_24px), color = Color.LightGray) {
                    if (drawnItems.isNotEmpty()) {
                        val deleteAction = Action(
                            type = ActionType.REMOVE,
                            items = drawnItems.toList()
                        )

                        undoStack.add(deleteAction)
                        drawnItems.clear()
                        redoStack.clear()
                    }
                }

                UpperBarIconButton(ImageVector.vectorResource(id = R.drawable.save_24px), color = Color.LightGray) {
                    // save button
                    captureController.capture()
                }
            }
        }
    }
}