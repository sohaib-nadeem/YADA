package ca.uwaterloo.cs346project
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import ca.uwaterloo.cs346project.data.Client
import ca.uwaterloo.cs346project.ui.home.DynamicUI
import ca.uwaterloo.cs346project.ui.util.PageType
import ca.uwaterloo.cs346project.ui.whiteboard.WhiteboardScreen
import ca.uwaterloo.cs346project.ui.theme.CS346ProjectTheme
import java.io.File

val client = Client()
var offline = true
class MainActivity : ComponentActivity() {
    override fun onDestroy() {
        super.onDestroy()
        val sessionIdFile = File(filesDir, "session_id.txt")
        sessionIdFile.writeText(client.session_id.toString())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sessionIdFile = File(filesDir, "session_id.txt")

        if (sessionIdFile.exists()) {
            val text = sessionIdFile.readText()
            client.session_id = text.toInt()
        }

        setContent {
            var curPageType by remember { mutableStateOf(PageType.HomePage) }
            CS346ProjectTheme {
                if (curPageType == PageType.HomePage) {
                    DynamicUI(setPage = {curPageType = it})
                    //HomePage(page, setPage = {page = it})
                } else if (curPageType == PageType.WhiteboardPage) {
                    WhiteboardScreen(setPage = {curPageType = it})
                }
            }
        }
    }
}
