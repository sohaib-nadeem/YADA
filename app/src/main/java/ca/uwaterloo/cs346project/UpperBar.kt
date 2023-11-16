package ca.uwaterloo.cs346project

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import dev.shreyaspatil.capturable.controller.CaptureController

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
    undoStack: MutableList<List<DrawnItem>>,
    redoStack: MutableList<List<DrawnItem>>,
    page: Pg,
    setPage: (Pg) -> Unit,
    captureController: CaptureController
) {
    Row(horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ){
        Box(modifier = Modifier.weight(1f)){
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.Top,
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
                UpperBarIconButton(icon = ImageVector.vectorResource(id = R.drawable.arrow_back_24px), color = Color.LightGray) {
                    setPage(page.copy(curPage = CurrentPage.HomePage))
                }
            }
        }
        Box(modifier = Modifier.weight(1f)) {
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.Top,
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
                // Undo button
                UpperBarIconButton(ImageVector.vectorResource(id = R.drawable.undo_24px), color = Color.LightGray) {
                    if (undoStack.size>1) {
                        drawnItems.clear()
                        redoStack.add(undoStack.removeAt(undoStack.lastIndex))
                        drawnItems.addAll(undoStack.last())
                    }
                }

                // Redo button
                UpperBarIconButton(ImageVector.vectorResource(id = R.drawable.redo_24px), color = Color.LightGray) {
                    if (redoStack.isNotEmpty()) {
                        drawnItems.clear()
                        undoStack.add(redoStack.removeAt(redoStack.lastIndex))
                        drawnItems.addAll(undoStack.last())
                    }
                }

                UpperBarIconButton(ImageVector.vectorResource(id = R.drawable.delete_24px), color = Color.LightGray) {
                    drawnItems.clear()
                    undoStack.add(drawnItems.toList())
                    redoStack.clear()
                }

                UpperBarIconButton(ImageVector.vectorResource(id = R.drawable.save_24px), color = Color.LightGray) {
                // save button
                    captureController.capture()
                }
            }
        }
    }
}