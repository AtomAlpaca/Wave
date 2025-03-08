package moe.atal.wave.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import moe.atal.wave.data.Note
import moe.atal.wave.data.NoteDatabase
import java.util.Date
import java.util.TimeZone


@Composable
fun NoteCard(
    note : Note,
    onClick : () -> Unit
)
{
    Card (
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
            .clickable(
                onClick = onClick
            ).height(
                if (note.note.isBlank()) { 60.dp }
                else { 75.dp }
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    ) {
        Column (
            modifier = Modifier.padding(10.dp)
        ) {
            Text(
                style = MaterialTheme.typography.bodyLarge,
                text = "心情 ${note.mood}"
            )
            Text(
                text = convertToTime(note.time),
                style = MaterialTheme.typography.bodySmall
            )
            Text(text = note.note,
                maxLines = 1,
                style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun PopupFullNote(
    note: Note,
    onDismiss : () -> Unit
)
{
    val context = LocalContext.current
    val dao = NoteDatabase.getDatabase(context).noteDao()
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.padding(16.dp).wrapContentSize(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column (
                modifier = Modifier.padding(16.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        text = "记录详情"
                    )
                }
                Text(
                    style = MaterialTheme.typography.titleMedium,
                    text = "心情 ${note.mood}"
                )
                Text(
                    text = convertToTime(note.time),
                    style = MaterialTheme.typography.bodyMedium
                )

                LazyColumn(modifier = Modifier.fillMaxWidth().height(150.dp).padding(5.dp)) {
                    item {
                        Text(
                            text = note.note,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                )
                {
                    Row(
                        horizontalArrangement = Arrangement.Absolute.Left,
                    ) {
                        TextButton(
                            onClick =
                            {
                                onDismiss()
                                CoroutineScope(Dispatchers.IO).launch {
                                    dao.delete(note)
                                }
                            },
                        ) { Text("删除") }
                    }
                    Row(
                        horizontalArrangement = Arrangement.Absolute.Right,
                    ) {
                        TextButton(
                            onClick = onDismiss,
                        ) { Text("关闭") }
                    }
                }
            }
        }
    }
}


@Composable
fun NoteList(
    flow : Flow<List<Note>>
)
{
    val list = flow.collectAsState(listOf())
    Text(
        text = "记录",
        style = MaterialTheme.typography.headlineLarge,
        modifier = Modifier.padding(5.dp)
    )
    LazyColumn (
        modifier = Modifier.fillMaxSize().padding(5.dp)
    ) {
        items(list.value) { x ->
            var showFullNote by remember { mutableStateOf(false) }
            NoteCard(x, onClick = { showFullNote = !showFullNote })
            if (showFullNote)
            {
                PopupFullNote( x, onDismiss = { showFullNote = !showFullNote} )
            }
        }
    }

}

@SuppressLint("SimpleDateFormat")
fun convertToTime(millis: Long): String {
    val formatter = java.text.SimpleDateFormat("yyyy 年 MM 月 dd 日 HH 时 mm 分").apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    return formatter.format(Date(millis))
}
