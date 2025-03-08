package moe.atal.wave.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import moe.atal.wave.data.NoteDatabase
import java.util.Calendar
import kotlin.math.max
import kotlin.math.min

@ExperimentalMaterial3Api
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainUI()
{
    val currentTime = Calendar.getInstance()
    var mode by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    val dao = NoteDatabase.getDatabase(context).noteDao()
    val list = dao.getAll().collectAsState(listOf())

    var showDropDownMenu     by remember { mutableStateOf(false) }
    var showSelectRange      by remember { mutableStateOf(false) }
    var showDatePicker       by remember { mutableStateOf(false) }
    var showDateRangePicker  by remember { mutableStateOf(false) }
    var showAbout            by remember { mutableStateOf(false) }

    var recentK   by rememberSaveable { mutableLongStateOf(0L) }
    val datePickerState      = rememberDatePickerState(
        initialSelectedDateMillis = currentTime.timeInMillis
    )

    val dateRangePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = currentTime.timeInMillis,
        initialSelectedEndDateMillis   = currentTime.timeInMillis
    )

    val flow =
        when (mode) {
            0 -> { dao.getAll() }
            1 -> { dao.getRecentK(recentK) }
            2 -> { dao.getAfter(datePickerState.selectedDateMillis!!) }
            else -> {
                if (dateRangePickerState.selectedStartDateMillis != null &&
                    dateRangePickerState.selectedEndDateMillis   != null) {
                    dao.getBetween(
                        dateRangePickerState.selectedStartDateMillis!!,
                        dateRangePickerState.selectedEndDateMillis!!
                    )
                }
                else
                {
                    dao.getAll()
                }
            }
        }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
                title = { Text("Wave") },
                actions = {
                    IconButton(
                        onClick = { showDropDownMenu = !showDropDownMenu }
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                    DropdownMenu(
                        expanded = showDropDownMenu,
                        onDismissRequest = { showDropDownMenu = !showDropDownMenu }
                    ) {
                        DropdownMenuItem(
                            text = { Text(text = "选择显示范围") },
                            onClick = { showSelectRange = !showSelectRange }
                        )
                        DropdownMenuItem(
                            text = { Text("关于") },
                            onClick = { showAbout = !showAbout }
                        )
                    }
                }
            )
        },

        floatingActionButton = { MainButton() },
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) { x ->
        Column (
            modifier = Modifier.padding(x)
        ) {
            NoteGraph(flow)
            NoteList(flow)
        }
    }

    if (showSelectRange)
    {
        Dialog(
            onDismissRequest = { showSelectRange = false }
        ) {
            Card (
                modifier = Modifier.padding(0.dp, 20.dp)
            ) {
                Column(Modifier.selectableGroup()) {
                    for (i in (0 .. 3)) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (mode == i),
                                    onClick = { mode = i },
                                    role = Role.RadioButton
                                )
                                .padding(horizontal = 16.dp)
                                .height(56.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (mode == i),
                                onClick = null,
                                modifier = Modifier.padding(5.dp)
                            )
                            when (i) {
                                0 -> { Text("全部 ${list.value.size} 条记录") }
                                1 -> {
                                    Text("最近")
                                    TextField(
                                        value = if (recentK == 0L) { "" } else { recentK.toString() },
                                        onValueChange =
                                        {
                                            val tmp = Regex("[^0-9]").replace(it, "")
                                            var num = if (tmp.isBlank()) { 0 } else { tmp.toLong() }
                                            num = max(num, 0)
                                            num = min(num, list.value.size.toLong())
                                            recentK = num
                                            mode = 1
                                        },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier
                                            .width(80.dp)
                                            .clickable(onClick = { mode = 1 }),
                                    )
                                    Text("条记录")
                                }
                                2 -> {
                                    IconButton(
                                        onClick = {
                                            mode = 2
                                            showDatePicker = !showDatePicker
                                        }
                                    ) {
                                        Icon(Icons.Default.DateRange, "选择日期")
                                    }
                                    Text("${convertMillisToDate(datePickerState.selectedDateMillis!!)}之后")
                                }
                                3 -> {
                                    IconButton(
                                        onClick = {
                                            mode = 3
                                            showDateRangePicker = !showDateRangePicker
                                        }
                                    ) {
                                        Icon(Icons.Default.DateRange, "选择日期")
                                    }
                                    Text(
                                        convertMillisToDate(
                                            if (dateRangePickerState.selectedStartDateMillis != null)
                                            {
                                                dateRangePickerState.selectedStartDateMillis!!
                                            }
                                            else
                                            {
                                                currentTime.timeInMillis
                                            }
                                        ) +
                                            "至" +
                                            convertMillisToDate(
                                                if (dateRangePickerState.selectedEndDateMillis != null)
                                                {
                                                    dateRangePickerState.selectedEndDateMillis!!
                                                }
                                                else
                                                {
                                                    currentTime.timeInMillis
                                                }
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Absolute.Right,
                ) {
                    ConfirmBottom( onDismiss = { showSelectRange = false }, text = "确定")
                }
            }
        }
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
    if (showDateRangePicker)
    {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDateRangePicker = false
                }) {
                    Text("确定")
                }
            }
        ) {
            DateRangePicker(
                state = dateRangePickerState,
                showModeToggle = false
            )
        }
    }

    if (showAbout)
    {
        Dialog(
            onDismissRequest = { showAbout = false }
        ) {
            Card (
                modifier = Modifier.height(250.dp)
            ) {
                Column (
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        text = "关于"
                    )
                    Text(
                        textAlign = TextAlign.Center,
                        text = "Version 1.0"
                    )
                    Text(
                        textAlign = TextAlign.Center,
                        text = "由 AtomAlpaca 开发"
                    )
                }
            }
        }
    }
}