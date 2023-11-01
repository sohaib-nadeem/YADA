package ca.uwaterloo.cs346project

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.LineWeight
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Rectangle
import androidx.compose.material.icons.outlined.ShapeLine
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
@Composable
fun Toolbar(drawInfo: DrawInfo, setDrawInfo: (DrawInfo) -> Unit, setting: Settings, setSetting: (Settings) -> Unit) {
    Column(modifier = Modifier
        .background(MaterialTheme.colorScheme.secondaryContainer)
        .zIndex(1f)
    ) {
        if (setting != Settings.NULL) {
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Line Weight Setting
                if (setting == Settings.LineWeight) {
                    Slider(
                        value = drawInfo.strokeWidth,
                        onValueChange = {
                            setDrawInfo(drawInfo.copy(strokeWidth = it))
                        },
                        valueRange = 1f..MAX_STROKE_WIDTH
                    )
                }

                // Color Picker Setting
                if (setting == Settings.ColorPicker) {
                    val colors = listOf(Color.Black, Color.Gray, Color.Red,
                        Color.Yellow, Color.Green, Color.Blue, Color.Cyan, Color.Magenta)
                    colors.forEach { color ->
                        Button(
                            onClick = { setDrawInfo(drawInfo.copy(color = color)) },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(color),
                            modifier = Modifier
                                .size(45.dp)
                                .background(
                                    color = if (drawInfo.color == color) Color.White else MaterialTheme.colorScheme.secondaryContainer,
                                    shape = CircleShape
                                )
                                .padding(10.dp)
                        ) {}
                    }
                }

                // Shape Setting
                if (setting == Settings.Shape) {
                    IconButton(onClick = { setDrawInfo(drawInfo.copy(drawMode = DrawMode.Shape, shape = Shape.Rectangle)) }) {
                        Icon(Icons.Outlined.Rectangle, contentDescription = "Localized description", modifier = Modifier
                            .background(
                                color = if (drawInfo.shape == Shape.Rectangle) Color.White else MaterialTheme.colorScheme.secondaryContainer,
                                shape = CircleShape,
                            )
                            .padding(5.dp)
                        )
                    }

                    IconButton(onClick = { setDrawInfo(drawInfo.copy(drawMode = DrawMode.Shape, shape = Shape.Oval)) }) {
                        Icon(Icons.Outlined.Circle, contentDescription = "Localized description", modifier = Modifier
                            .background(
                                color = if (drawInfo.shape == Shape.Oval) Color.White else MaterialTheme.colorScheme.secondaryContainer,
                                shape = CircleShape,
                            )
                            .padding(5.dp)
                        )
                    }

                    IconButton(onClick = { setDrawInfo(drawInfo.copy(drawMode = DrawMode.Shape, shape = Shape.StraightLine)) }) {
                        Icon(painterResource(id = R.drawable.pen_size_3_24px), contentDescription = "Localized description", modifier = Modifier
                            .background(
                                color = if (drawInfo.shape == Shape.StraightLine) Color.White else MaterialTheme.colorScheme.secondaryContainer,
                                shape = CircleShape,
                            )
                            .padding(5.dp)
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pen Button
            IconButton(
                onClick = {
                    if (drawInfo.drawMode == DrawMode.Pen) {
                        setDrawInfo(drawInfo.copy(drawMode = DrawMode.NULL))
                    } else {
                        setDrawInfo(drawInfo.copy(drawMode = DrawMode.Pen, shape = Shape.Line))
                    }
                }
            ) {
                Icon(Icons.Filled.Create, contentDescription = "Localized description", modifier = Modifier
                    .background(
                        color = if (drawInfo.drawMode == DrawMode.Pen) Color.White else MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape,
                    )
                    .padding(5.dp)
                )
            }

            // Eraser Button
            IconButton(onClick = {
                if (drawInfo.drawMode == DrawMode.Eraser) {
                    setDrawInfo(drawInfo.copy(drawMode = DrawMode.NULL))
                } else {
                    setDrawInfo(drawInfo.copy(drawMode = DrawMode.Eraser, shape = Shape.Line))
                }
            }) {
                Icon(painterResource(id = R.drawable.ink_eraser_24px), contentDescription = "Localized description", modifier = Modifier
                    .background(
                        color = if (drawInfo.drawMode == DrawMode.Eraser) Color.White else MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape,
                    )
                    .padding(5.dp)
                )
            }

            // Line Weight Button
            IconButton(onClick = {
                if (setting == Settings.LineWeight) {
                    setSetting(Settings.NULL)
                } else {
                    setSetting(Settings.LineWeight)
                }
            }) {
                Icon(Icons.Outlined.LineWeight, contentDescription = "Localized description", modifier = Modifier
                    .background(
                        color = if (setting == Settings.LineWeight) Color.White else MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape,
                    )
                    .padding(5.dp)
                )
            }

            // Shapes Selection Button
            IconButton(onClick = {
                if (setting == Settings.Shape) {
                    setSetting(Settings.NULL)
                    setDrawInfo(drawInfo.copy(drawMode = DrawMode.NULL))
                } else {
                    setSetting(Settings.Shape)
                    setDrawInfo(drawInfo.copy(drawMode = DrawMode.Shape, shape = Shape.Rectangle)) // Default shape
                }
            }) {
                Icon(Icons.Outlined.ShapeLine, contentDescription = "Localized description", modifier = Modifier
                    .background(
                        color = if (setting == Settings.Shape) Color.White else MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape,
                    )
                    .padding(5.dp)
                )
            }

            // Color Picker Button
            IconButton(onClick = {
                if (setting == Settings.ColorPicker) {
                    setSetting(Settings.NULL)
                } else {
                    setSetting(Settings.ColorPicker)
                }
            }) {
                Icon(Icons.Outlined.Palette, contentDescription = "Localized description", modifier = Modifier
                    .background(
                        color = if (setting == Settings.ColorPicker) Color.White else MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape,
                    )
                    .padding(5.dp)
                )
            }
        }
    }
}