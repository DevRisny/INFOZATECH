package com.infozatech.allinone

import com.infozatech.allinone.calculator.CalcState
import com.infozatech.allinone.calculator.CalculatorLogic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculatorLogicTest {

    private fun type(vararg keys: String, start: CalcState = CalcState()): CalcState =
        keys.fold(start) { state, key -> CalculatorLogic.press(state, key) }

    @Test fun typingBuildsExpression() {
        assertEquals("12+3", type("1", "2", "+", "3").expression)
    }

    @Test fun livePreviewShowsWhileTyping() {
        assertEquals("14", type("2", "+", "3", "×", "4").result)
    }

    @Test fun equalsShowsAnswerAndSavesHistory() {
        val s = type("2", "+", "3", "=")
        assertEquals("5", s.result)
        assertEquals(1, s.history.size)
        assertEquals("2+3", s.history.first().expression)
    }

    @Test fun divideByZeroShowsError() {
        val s = type("8", "÷", "0", "=")
        assertEquals("Cannot divide by zero", s.error)
    }

    @Test fun keyAfterErrorStartsFresh() {
        val s = type("8", "÷", "0", "=", "5")
        assertNull(s.error)
        assertEquals("5", s.expression)
    }

    @Test fun clearResetsButKeepsHistory() {
        val s = type("2", "+", "3", "=", "C")
        assertEquals("", s.expression)
        assertEquals(1, s.history.size)
    }

    @Test fun operatorAfterEqualsContinuesFromAnswer() {
        val s = type("2", "+", "3", "=", "×", "2", "=")
        assertEquals("10", s.result)
    }

    @Test fun digitAfterEqualsStartsNewCalculation() {
        val s = type("2", "+", "3", "=", "7")
        assertEquals("7", s.expression)
    }

    @Test fun decimalAllowedOncePerNumber() {
        assertEquals("1.5", type("1", ".", "5", ".").expression)
    }

    @Test fun decimalAtStartAddsLeadingZero() {
        assertEquals("0.", type(".").expression)
    }

    @Test fun leadingZerosAreNotStacked() {
        assertEquals("5", type("0", "0", "5").expression)
    }

    @Test fun operatorReplacesPreviousOperator() {
        assertEquals("5×", type("5", "+", "×").expression)
    }

    @Test fun backspaceRemovesLastCharacter() {
        assertEquals("1", type("1", "2", "⌫").expression)
    }

    @Test fun bracketsMultiplyImplicitly() {
        assertEquals("2×(", type("2", "(").expression)
        assertEquals("(2+3)×4", type("(", "2", "+", "3", ")", "4").expression)
    }

    @Test fun closeBracketOnlyWhenOpen() {
        assertEquals("5", type("5", ")").expression)
    }

    @Test fun negateTogglesLastNumber() {
        assertEquals("-5", type("5", "±").expression)
        assertEquals("5", type("5", "±", "±").expression)
        assertEquals("2×-3", type("2", "×", "3", "±").expression)
        assertEquals("2-3", type("2", "+", "3", "±").expression)
    }

    @Test fun percentAfterNumber() {
        assertEquals("0.5", type("5", "0", "%", "=").result)
    }

    @Test fun squareRootKey() {
        assertEquals("6", type("√", "3", "6", "=").result)
    }

    @Test fun historyIsLimited() {
        var s = CalcState()
        repeat(60) { s = type("1", "+", "1", "=", "C", start = s) }
        assertTrue(s.history.size <= 50)
    }
}
