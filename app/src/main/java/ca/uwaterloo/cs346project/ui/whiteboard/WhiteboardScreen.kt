package ca.uwaterloo.cs346project.ui.whiteboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import ca.uwaterloo.cs346project.client
import ca.uwaterloo.cs346project.model.Action
import ca.uwaterloo.cs346project.offline
import ca.uwaterloo.cs346project.ui.util.DrawInfo
import ca.uwaterloo.cs346project.ui.util.DrawnItem
import ca.uwaterloo.cs346project.ui.util.PageType
import ca.uwaterloo.cs346project.ui.util.applyAction
import dev.shreyaspatil.capturable.controller.rememberCaptureController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun WhiteboardScreen(setPage: (PageType) -> Unit) {
    var drawInfo by remember { mutableStateOf(DrawInfo()) }
    val scope = rememberCoroutineScope()
    val undoStack = remember { mutableListOf<Action<DrawnItem>>() }
    val redoStack = remember { mutableListOf<Action<DrawnItem>>() }
    var drawnItems = remember { mutableStateListOf<DrawnItem>() }
    val capturableController = rememberCaptureController()
    var selectedImage by remember { mutableStateOf<ImageBitmap?>(null) }

    if (!offline) {
        LaunchedEffect(true) {
            scope.launch {
                while (true) {
                    val items = client.receiveAction()
                    items.forEach { applyAction(it, drawnItems) }
                    delay(100L)
                }
            }
        }
    }

    Box {
        Row(modifier = Modifier
            .fillMaxSize()
            .align(Alignment.TopCenter)
        ) {
            Whiteboard(drawInfo, drawnItems, undoStack, redoStack, capturableController, selectedImage)
        }

        UpperBar(
            drawnItems,
            undoStack,
            redoStack,
            setPage,
            capturableController,
            { selectedImage = it }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            Toolbar(drawInfo = drawInfo, setDrawInfo = { drawInfo = it })
        }
    }
}
