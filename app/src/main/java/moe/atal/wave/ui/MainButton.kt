package moe.atal.wave.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun MainButton() {
    var showFillDialog by remember { mutableStateOf(false) }
    ShowMainButton(onClick = {showFillDialog = true} )
    if (showFillDialog) {
        FillDialog(
            onConfirm = { showFillDialog = false },
            onDismiss = { showFillDialog = false },
        )
    }
}

@Composable
fun ShowMainButton(
    onClick: () -> Unit
)
{
    FloatingActionButton(
        onClick = onClick
    ) {
        Icon(Icons.Filled.Add, contentDescription = "添加记录")
    }
}