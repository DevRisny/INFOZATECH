package com.infozatech.allinone.viewmodel

import androidx.lifecycle.ViewModel
import com.infozatech.allinone.calculator.CalcState
import com.infozatech.allinone.calculator.CalculatorLogic
import com.infozatech.allinone.calculator.HistoryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CalculatorViewModel : ViewModel() {
    private val _state = MutableStateFlow(CalcState())
    val state: StateFlow<CalcState> = _state.asStateFlow()

    fun press(key: String) {
        _state.update { CalculatorLogic.press(it, key) }
    }

    fun recall(item: HistoryItem) {
        _state.update { CalculatorLogic.recall(it, item) }
    }

    fun clearHistory() {
        _state.update { it.copy(history = emptyList()) }
    }
}
