package ca.uwaterloo.cs346project

import android.content.Context
import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.runBlocking
import java.io.File

fun makeToast(context: Context, text: String){
    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(page : Pg, setPage: (Pg) -> Unit) {
    var joinSessionID by remember { mutableStateOf("") }
    val context = LocalContext.current
    BoxWithConstraints {
            // "Portrait"
            Box(modifier = Modifier
                .fillMaxSize()
                .background(Color.White)) {
                Column(modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(text = "Whiteboard", fontSize = 50.sp)
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        Button(onClick = {
                            setPage(page.copy(curPage = CurrentPage.WhiteboardPage))
                            offline = true
                        },
                            modifier = Modifier
                                .width(200.dp)
                                .height(40.dp)) {
                            Text("Offline Mode", fontSize=20.sp)
                        }
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(onClick = { runBlocking {
                            if (client.create()) {
                                offline = false
                                setPage(page.copy(curPage = CurrentPage.WhiteboardPage))
                            } else {
                                val text = "Failed to create"
                                makeToast(context,text)
                            } }
                            },
                            modifier = Modifier
                                .width(200.dp)
                                .height(40.dp)) {
                            Text("New Canvas", fontSize=20.sp)
                        }
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        Button(onClick = { runBlocking {
                                if (client.join(client.session_id.toString())) {
                                    offline = false
                                    setPage(page.copy(curPage = CurrentPage.WhiteboardPage))
                                } else {
                                    val text = "Failed to open previous canvas"
                                    makeToast(context,text)
                                }
                            }},
                            modifier = Modifier
                                .width(200.dp)
                                .height(40.dp)) {
                            Text("Open Previous", fontSize=20.sp)
                        }
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(onClick =
                            {
                                runBlocking {
                                    if (client.join(joinSessionID)) {
                                        offline = false
                                        setPage(page.copy(curPage = CurrentPage.WhiteboardPage))
                                    } else {
                                        val text = "invalid Session ID"
                                        makeToast(context,text)
                                    }
                                }
                            },
                            modifier = Modifier
                                .width(200.dp)
                                .height(40.dp)) {
                            Text("Join Session", fontSize=20.sp)
                        }
                    }
                    Box(modifier = Modifier.weight(1f)) {
                            TextField(
                                modifier = Modifier.width(200.dp),
                                value = joinSessionID,
                                onValueChange = { joinSessionID = it },
                                label = { Text("Session ID") }
                            )
                    }
                }
            }
    }
}