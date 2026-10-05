package com.infozatech.allinone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infozatech.allinone.ui.alarm.AlarmScreen
import com.infozatech.allinone.ui.calculator.CalculatorScreen
import com.infozatech.allinone.ui.notes.NotesScreen
import com.infozatech.allinone.ui.theme.InfozaTheme
import com.infozatech.allinone.ui.todo.TodoScreen
import com.infozatech.allinone.ui.weather.WeatherScreen
import com.infozatech.allinone.viewmodel.NotesViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InfozaTheme {
                InfozaHubApp()
            }
        }
    }
}

private enum class Tab(val label: String, val icon: ImageVector) {
    Calculator("Calc", Icons.Default.Calculate),
    Tasks("Tasks", Icons.Default.Checklist),
    Alarms("Alarms", Icons.Default.Alarm),
    Weather("Weather", Icons.Default.WbSunny),
    Notes("Notes", Icons.Default.Description),
}

@Composable
private fun InfozaHubApp() {
    var selected by rememberSaveable { mutableIntStateOf(0) }
    val tabs = Tab.entries
    val current = tabs[selected]

    // Hide the bottom bar while a note is open full-screen.
    val notesVm: NotesViewModel = viewModel()
    val editingNote by notesVm.editingId.collectAsStateWithLifecycle()
    val showBottomBar = !(current == Tab.Notes && editingNote != null)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    tabs.forEachIndexed { index, tab ->
                        NavigationBarItem(
                            selected = index == selected,
                            onClick = { selected = index },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (current) {
                Tab.Calculator -> CalculatorScreen()
                Tab.Tasks -> TodoScreen()
                Tab.Alarms -> AlarmScreen()
                Tab.Weather -> WeatherScreen()
                Tab.Notes -> NotesScreen()
            }
        }
    }
}
