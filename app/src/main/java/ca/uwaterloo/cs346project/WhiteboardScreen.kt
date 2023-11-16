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

@Composable
fun WhiteboardScreen(page : Pg, setPage: (Pg) -> Unit) {
    var drawInfo by remember { mutableStateOf(DrawInfo()) }
    val scope = rememberCoroutineScope()
    var undoStack by remember { mutableStateOf<MutableList<List<DrawnItem>>>(mutableListOf(emptyList())) }
    var redoStack by remember { mutableStateOf<MutableList<List<DrawnItem>>>(mutableListOf()) }
    var drawnItems = remember { mutableStateListOf<DrawnItem>() }

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
        Row(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.TopCenter)
        ) {
            Whiteboard(drawInfo, drawnItems, undoStack, redoStack)
        }
        if (page.curPage == CurrentPage.WhiteboardPage) {
            UpperBar(drawnItems, undoStack, redoStack, page, setPage)

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