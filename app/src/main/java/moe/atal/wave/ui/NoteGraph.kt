package moe.atal.wave.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import moe.atal.wave.data.Note
import kotlin.math.sqrt

data class Pos(
    val x : Float,
    val y : Float
)

fun getPos(
    list: List<Note>
) : List <Pos>
{
    val res : MutableList<Pos> = mutableListOf()
    val w = list.first().time - list.last().time
    list.forEach { (_, x, y, _) ->
        res.add(Pos(
            x = (1f * (x - list.last().time) / w),
            y = (5f - y) / 10f
        ))
    }
    return res.toList()
}

@Composable
fun NoteGraph(
    flow : Flow<List<Note>>
) {
    val list by flow.collectAsState(listOf())
    Column {
        Text(
            modifier = Modifier.padding(5.dp),
            style = MaterialTheme.typography.headlineLarge,
            text = "折线图"
        )
        Card (
            modifier = Modifier
                .height(250.dp)
                .fillMaxWidth()
                .padding(10.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        ) {
            if (list.isEmpty())
            {
                NoNotes()
            } else {
                val pos : List<Pos> = remember(list) { getPos(list) }
                var showFullNote by remember { mutableStateOf(false) }
                var note by remember { mutableStateOf(Note(time = 0, mood = 0, note ="")) }
                if (showFullNote)
                {
                    PopupFullNote(note, onDismiss = { showFullNote = !showFullNote} )
                }

                Column {
                    Text(
                        modifier = Modifier.padding(5.dp),
                        text =  "从 ${convertMillisToDate(list.last().time)} " +
                                "至 ${convertMillisToDate(list.first().time)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Row {
                        DrawY()
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp, 15.dp)
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = { offset: Offset ->
                                            val h = size.height
                                            val d = size.width
                                            for ((i, p) in getPos(list).withIndex()) {
                                                if (distance(
                                                        d * p.x,
                                                        h * p.y,
                                                        offset.x,
                                                        offset.y
                                                    ) < 20f
                                                ) {
                                                    note = list[i]
                                                    showFullNote = true
                                                    break
                                                }
                                            }
                                        }
                                    )
                                }
                        ) {
                            val h = size.height
                            val d = size.width
                            val path = Path()
                            drawLine(
                                Color.Gray,
                                start = Offset(0f, 0f),
                                end =   Offset(0f, h),
                                strokeWidth = 5f
                            )
                            path.moveTo(d, h * pos.first().y)
                            pos.forEach { (x, y) ->
                                path.lineTo(d * x, h * y)
                                drawCircle(
                                    Color.Black,
                                    radius = 10f,
                                    center = Offset(x = d * x, y = h * y))
                            }
                            drawPath(path, Color.Black, style = Stroke(width = 5f))
                        }
                    }

                }
            }
        }
    }
}

@Composable
private fun DrawY() {
    Column(
        modifier = Modifier.padding(0.dp, 15.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.End
    ) {
        for (i in (-5..5)) {
            Text(
                text = "${-i}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun NoNotes() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            text = "暂无记录"
        )
    }
}

fun distance(
    x1 : Float,
    y1 : Float,
    x2 : Float,
    y2 : Float
) : Float {
    return sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2))
}