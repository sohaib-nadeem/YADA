package ca.uwaterloo.cs346project
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ca.uwaterloo.cs346project.ui.theme.CS346ProjectTheme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dev.shreyaspatil.capturable.controller.rememberCaptureController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File

var user_id = -1
class MainActivity : ComponentActivity() {
    override fun onStop() {
        super.onStop()
        val json = Gson().toJson(drawnItems)
        val file = File(filesDir, "drawnItems.json")
        file.writeText(json)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val drawnItemsDataFile = File(filesDir, "drawnItemsData.json")

        if (drawnItemsDataFile.exists()) {
            val json = drawnItemsDataFile.readText()
            val type = object : TypeToken<List<DrawnItem>>() {}.type
            drawnItems = Gson().fromJson(json, type)
        }

        runBlocking {
            //user_id = Client().join()
            user_id = Client().fakeJoin()
        }

        setContent {
            val capturableController = rememberCaptureController()
            var drawInfo by remember { mutableStateOf(DrawInfo()) }
            val scope = rememberCoroutineScope()
            var undoStack by remember { mutableStateOf<MutableList<List<DrawnItem>>>(mutableListOf(emptyList())) }
            var redoStack by remember { mutableStateOf<MutableList<List<DrawnItem>>>(mutableListOf()) }
            var page by remember { mutableStateOf(Pg()) }
            LaunchedEffect(true) {
                scope.launch {
                    while (true) {
                        //val item = Client().receive(user_id)
                        val item = Client().fakeReceive(user_id)
                        /*
                        for (i in item) {
                            val drawing = DrawnItem(
                                i.shape,
                                Color.Black,
                                i.strokeWidth,
                                mutableStateListOf()
                            )
                            drawnItems.add(drawing)
                        }
                        */
                        //drawnItems.addAll(items)
                        delay(100L)
                    }
                }
            }

            CS346ProjectTheme {

                Box {
                    Row(modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.TopCenter)
                    ) {
                        if (page.curPage == CurrentPage.HomePage) {
                            HomePage(page, setPage = {page = it})
                        } else if (page.curPage == CurrentPage.WhiteboardPage) {
                            Whiteboard(drawInfo, undoStack, redoStack, capturableController)
                        }
                    }
                    if (page.curPage == CurrentPage.WhiteboardPage) {
                        UpperBar(undoStack, redoStack, page, setPage = {page = it}, capturableController)

                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                        ) {
                            Toolbar(drawInfo = drawInfo, setDrawInfo = { drawInfo = it })
                        }
                    }
                }
            }
        }
    }
}
