package ca.uwaterloo.cs346project

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomePage(curPage : MutableState<CurrentPage>) {
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
}