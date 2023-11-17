package ca.uwaterloo.cs346project

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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import dev.shreyaspatil.capturable.controller.rememberCaptureController
import android.content.res.Resources


@Composable
fun WhiteboardScreen(page : Pg, setPage: (Pg) -> Unit) {
    var drawInfo by remember { mutableStateOf(DrawInfo()) }
    val scope = rememberCoroutineScope()
    val undoStack: MutableList<Action> = mutableListOf()
    val redoStack: MutableList<Action> = mutableListOf()
    var drawnItems = remember { mutableStateListOf<DrawnItem>() }
    val capturableController = rememberCaptureController()
    val metrics = Resources.getSystem().displayMetrics
    val screenWidth = metrics.widthPixels.toFloat()
    val screenHeight = metrics.heightPixels.toFloat()

    LaunchedEffect(true) {
        scope.launch {
            while (true) {
                val items = client.receive()
                drawnItems.addAll(items)
                delay(100L)
            }
        }
    }

    Box {
        Row(modifier = Modifier
            .fillMaxSize()
            .align(Alignment.TopCenter)
        ) {
            Whiteboard(drawInfo, drawnItems, undoStack, redoStack, capturableController, screenWidth, screenHeight)
        }

        if (page.curPage == CurrentPage.WhiteboardPage) {
            UpperBar(
                drawnItems,
                undoStack,
                redoStack,
                page,
                setPage,
                capturableController
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
}