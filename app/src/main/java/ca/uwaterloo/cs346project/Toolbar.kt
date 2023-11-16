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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.LineWeight
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Rectangle
import androidx.compose.material.icons.outlined.ShapeLine
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

val colors = listOf(Color.Black, Color.Gray, Color.Red,
    Color.Yellow, Color.Green, Color.Blue, Color.Cyan, Color.Magenta)

@Composable
fun ToolbarIconButton(icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(icon, contentDescription = "Localized description", modifier = Modifier
            .background(
                color = if (selected) Color.White else MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape,
            )
            .padding(5.dp)
        )
    }
}

@Composable
fun ToolbarColorButton(color: Color, selected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(color),
        modifier = Modifier
            .size(45.dp)
            .background(
                color = if (selected) Color.White else MaterialTheme.colorScheme.secondaryContainer,
                shape = CircleShape
            )
            .padding(10.dp)
    ) {}
}

@Composable
fun Toolbar(drawInfo: DrawInfo, setDrawInfo: (DrawInfo) -> Unit) {
    Column(modifier = Modifier
        .background(MaterialTheme.colorScheme.secondaryContainer)
        .zIndex(1f)
    ) {
        var toolbarExtensionSetting by remember { mutableStateOf(ToolbarExtensionSetting.Hidden) }

        if (toolbarExtensionSetting != ToolbarExtensionSetting.Hidden) {
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Line Weight Setting
                if (toolbarExtensionSetting == ToolbarExtensionSetting.StrokeWidthAdjustment) {
                    Slider(
                        value = drawInfo.strokeWidth,
                        onValueChange = {
                            setDrawInfo(drawInfo.copy(strokeWidth = it))
                        },
                        valueRange = 1f..MAX_STROKE_WIDTH
                    )
                }

                // Color Picker Setting
                if (toolbarExtensionSetting == ToolbarExtensionSetting.ColorSelection) {
                    colors.forEach { color ->
                        ToolbarColorButton(color = color, selected = drawInfo.color == color) {
                            setDrawInfo(drawInfo.copy(color = color))
                        }
                    }
                }

                // Shape Setting
                if (toolbarExtensionSetting == ToolbarExtensionSetting.ShapeSelection) {
                    ToolbarIconButton(Icons.Outlined.Rectangle, selected = (drawInfo.shape == Shape.Rectangle)) {
                        setDrawInfo(drawInfo.copy(drawMode = DrawMode.Shape, shape = Shape.Rectangle))
                    }

                    ToolbarIconButton(Icons.Outlined.Circle, selected = (drawInfo.shape == Shape.Oval)) {
                        setDrawInfo(drawInfo.copy(drawMode = DrawMode.Shape, shape = Shape.Oval))
                    }

                    ToolbarIconButton(ImageVector.vectorResource(id = R.drawable.pen_size_3_24px), selected = (drawInfo.shape == Shape.StraightLine)) {
                        setDrawInfo(drawInfo.copy(drawMode = DrawMode.Shape, shape = Shape.StraightLine))
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
            ToolbarIconButton(Icons.Filled.Create, selected = (drawInfo.drawMode == DrawMode.Pen)) {
                if (drawInfo.drawMode == DrawMode.Pen) {
                    setDrawInfo(drawInfo.copy(drawMode = DrawMode.CanvasDrag))
                } else {
                    setDrawInfo(drawInfo.copy(drawMode = DrawMode.Pen, shape = Shape.Line))
                }
                toolbarExtensionSetting = ToolbarExtensionSetting.Hidden
            }

            // Eraser Button
            ToolbarIconButton(ImageVector.vectorResource(id = R.drawable.ink_eraser_24px), selected = (drawInfo.drawMode == DrawMode.Eraser)) {
                // ORIGINAL
                if (drawInfo.drawMode == DrawMode.Eraser) {
                    setDrawInfo(drawInfo.copy(drawMode = DrawMode.CanvasDrag, shape = Shape.Line))
                } else {
                    setDrawInfo(drawInfo.copy(drawMode = DrawMode.Eraser, shape = Shape.Line))
                    toolbarExtensionSetting = ToolbarExtensionSetting.Hidden
                }
            }

            // Selector Tool Button
            ToolbarIconButton(ImageVector.vectorResource(id = R.drawable.lasso_select_24px), selected = (toolbarExtensionSetting == ToolbarExtensionSetting.SelectorTool)) {
                if (toolbarExtensionSetting == ToolbarExtensionSetting.SelectorTool) {
                    toolbarExtensionSetting = ToolbarExtensionSetting.Hidden
                    setDrawInfo(drawInfo.copy(drawMode = DrawMode.CanvasDrag))
                } else {
                    toolbarExtensionSetting = ToolbarExtensionSetting.SelectorTool
                    setDrawInfo(drawInfo.copy(drawMode = DrawMode.Selection))
                }
            }

            // Shapes Selection Button
            ToolbarIconButton(icon = Icons.Outlined.ShapeLine, selected = (drawInfo.drawMode == DrawMode.Shape && toolbarExtensionSetting == ToolbarExtensionSetting.ShapeSelection)) {
                if (toolbarExtensionSetting == ToolbarExtensionSetting.ShapeSelection) {
                    toolbarExtensionSetting = ToolbarExtensionSetting.Hidden
                    setDrawInfo(drawInfo.copy(drawMode = DrawMode.CanvasDrag))
                } else {
                    toolbarExtensionSetting = ToolbarExtensionSetting.ShapeSelection
                    setDrawInfo(drawInfo.copy(drawMode = DrawMode.Shape, shape = Shape.Rectangle)) // Default shape
                }
            }

            // Line Weight Button
            ToolbarIconButton(icon = Icons.Outlined.LineWeight, selected = (toolbarExtensionSetting == ToolbarExtensionSetting.StrokeWidthAdjustment)) {
                if (toolbarExtensionSetting == ToolbarExtensionSetting.StrokeWidthAdjustment) {
                    toolbarExtensionSetting = ToolbarExtensionSetting.Hidden
                } else {
                    toolbarExtensionSetting = ToolbarExtensionSetting.StrokeWidthAdjustment
                }
            }

            // Color Picker Button
            ToolbarIconButton(icon = Icons.Outlined.Palette, selected = (toolbarExtensionSetting == ToolbarExtensionSetting.ColorSelection)) {
                if (toolbarExtensionSetting == ToolbarExtensionSetting.ColorSelection) {
                    toolbarExtensionSetting = ToolbarExtensionSetting.Hidden
                } else {
                    toolbarExtensionSetting = ToolbarExtensionSetting.ColorSelection
                }
            }
        }
    }
}
