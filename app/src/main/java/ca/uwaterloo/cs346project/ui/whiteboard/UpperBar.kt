package ca.uwaterloo.cs346project.ui.whiteboard

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import ca.uwaterloo.cs346project.R
import ca.uwaterloo.cs346project.client
import ca.uwaterloo.cs346project.model.Action
import ca.uwaterloo.cs346project.model.ActionType
import ca.uwaterloo.cs346project.offline
import ca.uwaterloo.cs346project.ui.home.makeToast
import ca.uwaterloo.cs346project.ui.util.DrawnItem
import ca.uwaterloo.cs346project.ui.util.PageType
import ca.uwaterloo.cs346project.ui.util.createReversedAction
import ca.uwaterloo.cs346project.ui.util.performRedo
import ca.uwaterloo.cs346project.ui.util.performUndo
import dev.shreyaspatil.capturable.controller.CaptureController
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream

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

@Composable
fun UpperBar(
    drawnItems: SnapshotStateList<DrawnItem>,
    undoStack: MutableList<Action<DrawnItem>>,
    redoStack: MutableList<Action<DrawnItem>>,
    setPage: (PageType) -> Unit,
    captureController: CaptureController,
    setSelectedImage: (ImageBitmap) -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                inputStream?.let {
                    val imageBitmap = BitmapFactory.decodeStream(it).asImageBitmap()
                    setSelectedImage(imageBitmap)
                }
            }
        }
    }

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
                    setPage(PageType.HomePage)
                }
                Button(onClick = {}) {
                    if (offline) Text("Offline")
                    else Text("Session ID: ${client.session_id.toString()}")
                }

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
                    val intent = Intent(Intent.ACTION_PICK)
                    intent.type = "image/*"
                    launcher.launch(intent)
                }
            }
        }
    }
}