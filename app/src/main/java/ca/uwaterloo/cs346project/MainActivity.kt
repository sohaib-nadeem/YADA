package ca.uwaterloo.cs346project
import android.content.res.Resources
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.uwaterloo.cs346project.ui.theme.CS346ProjectTheme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dev.shreyaspatil.capturable.controller.rememberCaptureController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File

var user_id = -1
val client = Client()
var offline = true
class MainActivity : ComponentActivity() {
    override fun onStop() {
        super.onStop()
        //val json = Gson().toJson(drawnItems)
        //val file = File(filesDir, "drawnItems.json")
        //file.writeText(json)
    }
    override fun onDestroy() {
        super.onDestroy()
        val sessionIdFile = File(filesDir, "session_id.txt")
        sessionIdFile.writeText(client.session_id.toString())
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val drawnItemsDataFile = File(filesDir, "drawnItemsData.json")
        val sessionIdFile = File(filesDir, "session_id.txt")
        if (drawnItemsDataFile.exists()) {
            val json = drawnItemsDataFile.readText()
            val type = object : TypeToken<List<DrawnItem>>() {}.type
            //drawnItems = Gson().fromJson(json, type)
        }

        if (sessionIdFile.exists()) {
            val text = sessionIdFile.readText()
            client.session_id = text.toInt()
        }

        setContent {
            var page by remember { mutableStateOf(Pg()) }
            CS346ProjectTheme {
                if (page.curPage == CurrentPage.HomePage) {
                    HomePage(page, setPage = {page = it})
                } else if (page.curPage == CurrentPage.WhiteboardPage) {
                    WhiteboardScreen(page, setPage = {page = it})
                }
            }
        }
    }
}
