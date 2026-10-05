package com.infozatech.allinone.ui.calculator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infozatech.allinone.calculator.CalcState
import com.infozatech.allinone.ui.components.ScreenHeader
import com.infozatech.allinone.viewmodel.CalculatorViewModel

private val KEY_ROWS = listOf(
    listOf("C", "(", ")", "⌫"),
    listOf("√", "^", "%", "÷"),
    listOf("7", "8", "9", "×"),
    listOf("4", "5", "6", "-"),
    listOf("1", "2", "3", "+"),
    listOf("±", "0", ".", "="),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(vm: CalculatorViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    var showHistory by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Calculator",
            subtitle = "Decimals, brackets, %, √ and powers",
            actions = {
                IconButton(onClick = { showHistory = true }) {
                    Icon(Icons.Default.History, contentDescription = "History")
                }
            },
        )

        Display(
            state = state,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
        )

        Keypad(
            onKey = vm::press,
            modifier = Modifier
                .weight(2.1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }

    if (showHistory) {
        ModalBottomSheet(onDismissRequest = { showHistory = false }) {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "History",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                    )
                    if (state.history.isNotEmpty()) {
                        TextButton(onClick = { vm.clearHistory() }) { Text("Clear all") }
                    }
                }
                if (state.history.isEmpty()) {
                    Text(
                        text = "Your calculations will appear here.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 24.dp),
                    )
                } else {
                    LazyColumn(modifier = Modifier.padding(bottom = 16.dp)) {
                        items(state.history) { item ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        vm.recall(item)
                                        showHistory = false
                                    }
                                    .padding(vertical = 10.dp),
                                horizontalAlignment = Alignment.End,
                            ) {
                                Text(
                                    text = item.expression,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                                Text(
                                    text = "= ${item.result}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/** The area above the keys that shows what was typed and the result. */
@Composable
private fun Display(state: CalcState, modifier: Modifier = Modifier) {
    val mainText = state.error ?: state.result
    val resultSize = when {
        state.error != null -> 24.sp
        mainText.length <= 8 -> 56.sp
        mainText.length <= 12 -> 44.sp
        mainText.length <= 16 -> 34.sp
        else -> 26.sp
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.End,
    ) {
        Text(
            text = state.expression.ifEmpty { "0" },
            fontSize = if (state.expression.length > 18) 22.sp else 30.sp,
            color = if (state.expression.isEmpty() && mainText.isEmpty()) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = mainText.ifEmpty { " " },
            fontSize = resultSize,
            fontWeight = FontWeight.Bold,
            color = if (state.error != null) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            maxLines = 1,
            softWrap = false,
            textAlign = TextAlign.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 12.dp),
        )
    }
}

@Composable
private fun Keypad(onKey: (String) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        KEY_ROWS.forEach { row ->
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                row.forEach { key ->
                    CalcKey(
                        key = key,
                        onClick = onKey,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalcKey(key: String, onClick: (String) -> Unit, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val (background, foreground) = when (key) {
        "C" -> scheme.errorContainer to scheme.onErrorContainer
        "=" -> scheme.primary to scheme.onPrimary
        "÷", "×", "-", "+" -> scheme.primaryContainer to scheme.onPrimaryContainer
        "(", ")", "√", "^", "%", "±", "⌫" -> scheme.secondaryContainer to scheme.onSecondaryContainer
        else -> scheme.surfaceVariant to scheme.onSurface
    }

    Surface(
        onClick = { onClick(key) },
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = background,
        contentColor = foreground,
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (key == "⌫") {
                Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "Backspace")
            } else {
                Text(
                    text = if (key == "-") "−" else key,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}
