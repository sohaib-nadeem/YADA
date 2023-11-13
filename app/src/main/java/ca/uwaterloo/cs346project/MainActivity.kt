package ca.uwaterloo.cs346project
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.uwaterloo.cs346project.ui.theme.CS346ProjectTheme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
            var drawInfo by remember { mutableStateOf(DrawInfo()) }
            val scope = rememberCoroutineScope()
            var undoStack by remember { mutableStateOf<MutableList<List<DrawnItem>>>(mutableListOf(emptyList())) }
            var redoStack by remember { mutableStateOf<MutableList<List<DrawnItem>>>(mutableListOf()) }
            var curPage = remember { mutableStateOf(CurrentPage.HomePage) }
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
                        if (curPage.value == CurrentPage.HomePage) {
                            Box(modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White)) {
                                Column(modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceEvenly,
                                    horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = "Whiteboard", fontSize = 50.sp)
                                        OutlinedButton(onClick = { curPage.value = CurrentPage.WhiteboardPage },
                                            modifier = Modifier.width(140.dp)) {
                                            Text("New Canvas")
                                        }
                                        Button(onClick = { curPage.value = CurrentPage.WhiteboardPage },
                                            modifier = Modifier.offset(y = (-150).dp)
                                                .width(140.dp)) {
                                            Text("Open Previous")
                                        }
                                    }
                                }
                        } else if (curPage.value == CurrentPage.WhiteboardPage) {
                            Whiteboard(drawInfo, undoStack, redoStack)
                        }
                    }
                    if (curPage.value == CurrentPage.WhiteboardPage) {
                        UpperBar(undoStack,redoStack,curPage)

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
