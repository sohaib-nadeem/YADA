package ca.uwaterloo.cs346project

import android.content.res.Configuration
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(page : Pg, setPage: (Pg) -> Unit) {
    var showSessionIDBox by remember { mutableStateOf(false)}
    BoxWithConstraints {
        if (maxWidth < maxHeight) {
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
                        OutlinedButton(onClick = { setPage(page.copy(curPage = CurrentPage.WhiteboardPage)) },
                            modifier = Modifier
                                .width(300.dp)
                                .height(80.dp)) {
                            Text("New Canvas", fontSize=25.sp)
                        }
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        Button(onClick = { setPage(page.copy(curPage = CurrentPage.WhiteboardPage)) },
                            modifier = Modifier
                                .width(300.dp)
                                .height(80.dp)) {
                            Text("Open Previous", fontSize=25.sp)
                        }
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(onClick = { showSessionIDBox = !showSessionIDBox},
                            modifier = Modifier
                                .width(300.dp)
                                .height(80.dp)) {
                            Text("Join Session", fontSize=25.sp)
                        }
                    }
                    var text by remember { mutableStateOf("") }
                    Box(modifier = Modifier.weight(1f)) {
                        if (showSessionIDBox) {
                            TextField(
                                value = text,
                                onValueChange = { text = it },
                                label = { Text("Session ID") }
                            )
                        }
                    }
                }
            }
        } else {
            // "Landscape"
            Row(modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f).fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Whiteboard", fontSize = 50.sp, modifier = Modifier.offset(y=(100).dp))
                        if (showSessionIDBox) {
                            var text by remember { mutableStateOf("") }
                            TextField(
                                value = text,
                                onValueChange = { text = it },
                                label = { Text("Session ID")},
                                modifier = Modifier.offset(y=130.dp)
                            )
                        }
                    }


                Box(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        OutlinedButton(onClick = { setPage(page.copy(curPage = CurrentPage.WhiteboardPage)) },
                            modifier = Modifier
                                .width(300.dp)
                                .height(80.dp)) {
                            Text("New Canvas", fontSize=25.sp)
                        }
                        Button(onClick = { setPage(page.copy(curPage = CurrentPage.WhiteboardPage)) },
                            modifier = Modifier
                                .width(300.dp)
                                .height(80.dp)) {
                            Text("Open Previous", fontSize=25.sp)
                        }
                        OutlinedButton(onClick = { showSessionIDBox = !showSessionIDBox},
                            modifier = Modifier
                                .width(300.dp)
                                .height(80.dp)) {
                            Text("Join Session", fontSize=25.sp)
                        }
                    }
                }
            }
        }
    }
}