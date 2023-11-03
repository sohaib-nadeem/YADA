package ca.uwaterloo.cs346project

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp

@Composable
fun UpperBarIconButton(icon: ImageVector, color: Color, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(icon, contentDescription = "Localized description", modifier = Modifier
            .background(
                color = color,
                shape = CircleShape,
            )
            .padding(5.dp)
        )
    }
}

@Composable
fun UpperBar() {
    Row(
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.Top,
        modifier = Modifier.padding(8.dp)
            .fillMaxWidth()
    ) {
        // Undo button
        UpperBarIconButton(ImageVector.vectorResource(id = R.drawable.undo_24px), color = Color.LightGray) {
            /* Handle undo */
        }

        // Redo button
        UpperBarIconButton(ImageVector.vectorResource(id = R.drawable.redo_24px), color = Color.LightGray) {
            /* Handle undo */
        }

        UpperBarIconButton(ImageVector.vectorResource(id = R.drawable.delete_24px), color = Color.LightGray) {
            drawnItems.clear()
        }

        /*
        UpperBarIconButton(ImageVector.vectorResource(id = R.drawable.settings_24px), color = Color.LightGray) {
            /* Handle setting */
        }
        */
    }
}