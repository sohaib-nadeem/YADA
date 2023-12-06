package ca.uwaterloo.cs346project.ui.home

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.widget.Toast
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.Dimension
import ca.uwaterloo.cs346project.client
import ca.uwaterloo.cs346project.ui.util.PageType
import ca.uwaterloo.cs346project.offline
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.res.painterResource
import ca.uwaterloo.cs346project.R
import ca.uwaterloo.cs346project.ui.util.openPdfFromAssets

fun makeToast(context: Context, text: String){
    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
}

@Composable
fun DynamicUI(setPage: (PageType) -> Unit) {
    val configuration = LocalConfiguration.current
    val orientation = configuration.orientation

    if (orientation == Configuration.ORIENTATION_PORTRAIT) {
        // Call a composable function that sets up the UI for portrait mode
        //LandscapeUI(page, setPage)
        PortraitUI(setPage)
    } else {
        // Call a composable function that sets up the UI for landscape mode
        LandscapeUI(setPage)
    }
}

    @SuppressLint("RememberReturnType")
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun PortraitUI(setPage: (PageType) -> Unit) {
        var joinSessionID by remember { mutableStateOf("") }
        val context = LocalContext.current
        var loading by remember { mutableStateOf(false) }

        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp)
        ) {
            val (titleRef, buttonBoxRef) = createRefs()
    
            Text(
                text = "YADA",
                fontSize = 50.sp,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                lineHeight = 50.sp,
                modifier = Modifier.constrainAs(titleRef) {
                    top.linkTo(parent.top, margin = 50.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
                textAlign = TextAlign.Center // Center align text
            )
    
            BoxWithConstraints(
                modifier = Modifier
                    .constrainAs(buttonBoxRef) {
                        top.linkTo(titleRef.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                        width = Dimension.fillToConstraints
                        height = Dimension.value(500.dp)
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(50.dp, 0.dp),
                    verticalArrangement = Arrangement.spacedBy(30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            setPage(PageType.WhiteboardPage)
                            offline = true
                        },
                        modifier = Modifier
                            .width(250.dp)
                            .height(50.dp)
                    ) {
                        Text("Offline Mode", fontSize = 20.sp)
                    }
    
                    OutlinedButton(
                        onClick = {
                            loading = true
                            GlobalScope.launch() {
                                withContext(Dispatchers.IO) {
                                    if (client.create()) {
                                        loading = false
                                        offline = false
                                        setPage(PageType.WhiteboardPage)
                                    } else {
                                        loading = false
                                        val text = "Failed to create"
                                        withContext(Dispatchers.Main) {
                                            makeToast(context, text)
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .width(250.dp)
                            .height(50.dp)
                    ) {
                        Text("New Canvas", fontSize = 20.sp)
                    }
    
                    Button(
                        onClick = {
                            loading = true
                            GlobalScope.launch() {
                                withContext(Dispatchers.IO) {
                                    if (client.join(client.session_id.toString())) {
                                        loading = false
                                        offline = false
                                        setPage(PageType.WhiteboardPage)
                                    } else {
                                        loading = false
                                        val text = "Failed to open previous canvas"
                                        withContext(Dispatchers.Main) {
                                            makeToast(context, text)
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .width(250.dp)
                            .height(50.dp)
                    ) {
                        Text("Open Previous", fontSize = 20.sp)
                    }
    
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = joinSessionID,
                            onValueChange = { joinSessionID = it },
                            label = { Text("Session ID") },
                            modifier = Modifier
                                .width(130.dp)
                                .height(60.dp),
                            singleLine = true,
                            shape = RoundedCornerShape(30.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                cursorColor = MaterialTheme.colorScheme.primary,
                                textColor = MaterialTheme.colorScheme.onSurface,
                                placeholderColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
    
    
                        Button(
                            onClick = {
                                loading = true
                                GlobalScope.launch() {
                                    withContext(Dispatchers.IO) {
                                        if (client.join(joinSessionID)) {
                                            loading = false
                                            offline = false
                                            setPage(PageType.WhiteboardPage)
                                        } else {
                                            loading = false
                                            val text = "Invalid Session ID"
                                            withContext(Dispatchers.Main) {
                                                makeToast(context, text)
                                            }
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .width(100.dp)
                                .height(60.dp)
                        ) {
                            Text("Join", fontSize = 20.sp)
                        }
                    }
                }
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.width(64.dp).align(Alignment.Center)
                            .offset(y = (-30).dp),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            IconButton(onClick = {
                openPdfFromAssets(context, "instructions.pdf")
            }) {
                Icon(painterResource(id = R.drawable.help_24px), contentDescription = "Help", modifier = Modifier
                    .background(
                        color = Color.White,
                        shape = CircleShape,
                    )
                    .padding(5.dp)
                )
            }
        }
    }
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandscapeUI(setPage: (PageType) -> Unit) {
    var joinSessionID by remember { mutableStateOf("") }
    val context = LocalContext.current
    var loading by remember { mutableStateOf(false) }
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center) // Center the Column within the BoxWithConstraints
                .padding(top = 0.dp, start = 40.dp, end = 40.dp) // Apply padding to the Column
                //.width(250.dp) // Set the desired width for the Column
                .height(IntrinsicSize.Min), // Height is the minimum needed to wrap its content
            verticalArrangement = Arrangement.spacedBy(5.dp), // Arrange children starting from the top
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "YADA",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .offset(y=(-20).dp)
            )

            //Spacer(modifier = Modifier.height(5.dp)) // Manually set the padding between the title and the first button

            Button(
                onClick = {
                    setPage(PageType.WhiteboardPage)
                    offline = true
                },
                modifier = Modifier
                    .width(250.dp)
                    .height(40.dp)
            ) {
                Text("Offline Mode", fontSize = 20.sp)
            }

            OutlinedButton(
                onClick = {
                    GlobalScope.launch() {
                        loading = true
                        withContext(Dispatchers.IO) {
                            if (client.create()) {
                                loading = false
                                offline = false
                                setPage(PageType.WhiteboardPage)
                            } else {
                                loading = false
                                val text = "Failed to create"
                                withContext(Dispatchers.Main) {
                                    makeToast(context, text)
                                }
                            }
                        }
                    }

                },
                modifier = Modifier
                    .width(250.dp)
                    .height(40.dp)
            ) {
                Text("New Canvas", fontSize = 20.sp)
            }

            Button(
                onClick = {
                    loading = true
                    GlobalScope.launch() {
                        withContext(Dispatchers.IO) {
                            if (client.join(client.session_id.toString())) {
                                loading = false
                                offline = false
                                setPage(PageType.WhiteboardPage)
                            } else {
                                loading = false
                                val text = "Failed to open previous canvas"
                                withContext(Dispatchers.Main) {
                                    makeToast(context, text)
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .width(250.dp)
                    .height(40.dp)
            ) {
                Text("Open Previous", fontSize = 20.sp)
            }

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = joinSessionID,
                    onValueChange = { joinSessionID = it },
                    label = { Text("Session ID") },
                    modifier = Modifier
                        .width(130.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(30.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        textColor = MaterialTheme.colorScheme.onSurface,
                        placeholderColor = MaterialTheme.colorScheme.onSurface
                    )
                )

                Spacer(modifier = Modifier.width(25.dp))


                Button(
                    onClick = {
                        loading = true
                        GlobalScope.launch() {
                            withContext(Dispatchers.IO) {
                                if (client.join(joinSessionID)) {
                                    loading = false
                                    offline = false
                                    setPage(PageType.WhiteboardPage)
                                } else {
                                    loading = false
                                    val text = "Invalid Session ID"
                                    withContext(Dispatchers.Main) {
                                        makeToast(context, text)
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .width(100.dp)
                        .height(60.dp)
                ) {
                    Text("Join", fontSize = 20.sp)
                }
            }
        }
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.width(64.dp).align(Alignment.Center)
                    .offset(y = (-30).dp),
                color = MaterialTheme.colorScheme.secondary
            )
        }

        IconButton(onClick = {
            openPdfFromAssets(context, "instructions.pdf")
        }) {
            Icon(painterResource(id = R.drawable.help_24px), contentDescription = "Help", modifier = Modifier
                .background(
                    color = Color.White,
                    shape = CircleShape,
                )
                .padding(5.dp)
            )
        }

    }
}
