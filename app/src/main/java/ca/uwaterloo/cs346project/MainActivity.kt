package ca.uwaterloo.cs346project
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.Rectangle
import androidx.compose.material.icons.outlined.ShapeLine
import androidx.compose.material.icons.outlined.LineWeight
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import ca.uwaterloo.cs346project.ui.theme.CS346ProjectTheme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

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


        setContent {
            scope.launch {
                while(true) {
                    Client().receive()
                };
            }
            var drawInfo by remember { mutableStateOf(DrawInfo()) }
            var setting by remember { mutableStateOf(Settings.NULL) }
            CS346ProjectTheme {
                Box {
                    Row(modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.TopCenter)

                    ) {
                        Whiteboard(drawInfo)
                    }

                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                    ) {
                        Toolbar(drawInfo = drawInfo, setDrawInfo = { drawInfo = it },
                            setting = setting, setSetting = { setting = it})
                    }
                   /* Row {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = Color.White
                        ) {
                            val scope = rememberCoroutineScope()
                            var text by remember { mutableStateOf("Loading") }
                            LaunchedEffect(true) {
                                scope.launch {
                                    text = try {
                                        Client().receive()
                                    } catch (e: Exception) {
                                        e.localizedMessage ?: "error"
                                    }
                                }
                            }
                            Text(text)
                        }
                    }*/
                }
            }
        }
    }
}
