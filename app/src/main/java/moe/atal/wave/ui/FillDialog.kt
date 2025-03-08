package moe.atal.wave.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.wear.compose.material.ContentAlpha
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import moe.atal.wave.data.Note
import moe.atal.wave.data.NoteDatabase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import kotlin.math.roundToLong

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FillDialog(
    onConfirm : () -> Unit,
    onDismiss : () -> Unit,
)
{
    var text by remember { mutableStateOf("") }
    val currentTime = Calendar.getInstance()
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = currentTime.timeInMillis)
    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = currentTime.get(Calendar.MINUTE),
        is24Hour = true,
    )
    var sliderPosition by remember { mutableFloatStateOf(0f) }
    val context = LocalContext.current
    val dao = NoteDatabase.getDatabase(context).noteDao()

    Dialog(onDismissRequest = onDismiss)
    {
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
        )
        {
            Column()
            {
                    PickerTextFiled(
                        datePickerState.selectedDateMillis?.let { convertMillisToDate(it) } ?: "",
                        "日期",
                        "选择日期",
                        Icons.Default.DateRange,
                        onClick = { showDatePicker = !showDatePicker }
                    )
                    PickerTextFiled(
                        "${timePickerState.hour} 时 ${timePickerState.minute} 分",
                        "时间",
                        "选择时间",
                        Icons.Default.CheckCircle,
                        onClick = { showTimePicker = !showTimePicker }
                    )


                Row (
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier.padding(15.dp)
                ){
                    Text(modifier = Modifier
                        .weight(4f)
                        .wrapContentHeight(Alignment.CenterVertically),
                        text = "心情指数：${sliderPosition.roundToLong()}")
                    Slider(
                        modifier = Modifier.weight(6f),
                        value = sliderPosition,
                        onValueChange = { sliderPosition = it },
                        steps = 9,
                        valueRange = -5f..5f
                    )
                }

                if (showDatePicker)
                {
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                showDatePicker = false
                            }) {
                                Text("确定")
                            }
                        }
                    ) {
                        DatePicker(
                            state = datePickerState,
                            showModeToggle = false
                        )
                    }
                }
                if (showTimePicker)
                {
                    Dialog (
                        onDismissRequest = { showTimePicker = false },
                    ) {
                        Card(modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface)
                        ) {
                            TimePicker(
                                state = timePickerState
                            )
                        }
                    }
                }

                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("备注") }
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Absolute.Right,
                ) {
                    ConfirmBottom(onDismiss, "取消")
                    ConfirmBottom({
                        CoroutineScope(Dispatchers.IO).launch {
                            dao.insert(
                                Note (
                                    time = convertToMillis(
                                        datePickerState.selectedDateMillis!!.toLong(),
                                        timePickerState.hour.toLong(),
                                        timePickerState.minute.toLong()
                                    ),
                                    mood = sliderPosition.roundToLong(),
                                    note = text
                                )
                            )
                        }
                        onConfirm()
                    }, "确定")
                }
            }
        }
    }
}

@Composable
private fun PickerTextFiled(
    value : String,
    label : String,
    description : String,
    icon : ImageVector,
    onClick : () -> Unit
){

    OutlinedTextField(
        enabled = false,
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        colors = TextFieldDefaults.colors(
            disabledLeadingIconColor = MaterialTheme.colorScheme.primary.copy(alpha = ContentAlpha.high),
        ),
        value = value,
        onValueChange = {},
        label = { Text(label) },
        leadingIcon = {
            IconButton(onClick = onClick) {
                Icon(
                    imageVector = icon,
                    contentDescription = description
                )
            }
        }
    )
}

@Composable
fun ConfirmBottom(
    onDismiss: () -> Unit,
    text : String
) {
    TextButton(
        onClick = { onDismiss() },
        modifier = Modifier.padding(8.dp),
    ) {
        Text(text)
    }
}

@SuppressLint("SimpleDateFormat")
fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("yyyy 年 MM 月 dd 日").apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    return formatter.format(Date(millis))
}

fun convertToMillis(date: Long, hour: Long, minute: Long): Long {
    val tmp : Long = 24 * 60 * 60 * 1000
    return (date / tmp) * tmp + 60 * 60 * 1000 * hour + 60 * 1000 * minute
}

