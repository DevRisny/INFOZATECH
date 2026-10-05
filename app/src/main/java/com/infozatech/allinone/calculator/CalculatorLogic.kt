package com.infozatech.allinone.calculator

data class HistoryItem(val expression: String, val result: String)

data class CalcState(
    /** What the user has typed so far, e.g. "2+3×4". */
    val expression: String = "",
    /** Live preview while typing, or the final answer after "=". */
    val result: String = "",
    /** Error text such as "Cannot divide by zero". Null when everything is fine. */
    val error: String? = null,
    /** True right after "=", so the next key either starts fresh or continues from the answer. */
    val justEvaluated: Boolean = false,
    val history: List<HistoryItem> = emptyList(),
)

/**
 * Pure key-handling logic for the calculator. Each call takes the old state and
 * returns the new state, which makes it easy to unit test without Android.
 */
object CalculatorLogic {

    private const val OPERATORS = "+-×÷^"
    private const val MAX_LENGTH = 60
    private const val MAX_HISTORY = 50

    fun press(state: CalcState, key: String): CalcState {
        // After an error, any key starts a fresh calculation (history is kept).
        val base = if (state.error != null) CalcState(history = state.history) else state

        val prepared = if (base.justEvaluated) {
            val isDigitKey = key.length == 1 && key[0].isDigit()
            when {
                key == "=" -> return base
                isDigitKey || key == "." || key == "(" || key == "√" ->
                    CalcState(history = base.history)
                key.length == 1 && key[0] in "+-×÷^%)" -> {
                    val carry = base.result.takeIf { !it.contains('E') } ?: ""
                    base.copy(expression = carry, result = "", justEvaluated = false)
                }
                else -> base.copy(justEvaluated = false)
            }
        } else {
            base
        }

        return when (key) {
            "C" -> CalcState(history = prepared.history)
            "⌫" -> backspace(prepared)
            "=" -> equals(prepared)
            "±" -> negate(prepared)
            "." -> decimal(prepared)
            "(" -> openBracket(prepared)
            ")" -> closeBracket(prepared)
            "%" -> percent(prepared)
            "√" -> squareRoot(prepared)
            "+", "-", "×", "÷", "^" -> operator(prepared, key[0])
            else -> if (key.length == 1 && key[0].isDigit()) digit(prepared, key[0]) else prepared
        }
    }

    /** Puts a past calculation's answer back on the display. */
    fun recall(state: CalcState, item: HistoryItem): CalcState {
        if (item.result.contains('E')) return state
        return update(CalcState(history = state.history), item.result)
    }

    // ---- key handlers -------------------------------------------------------------------

    private fun digit(s: CalcState, d: Char): CalcState {
        if (s.expression.length >= MAX_LENGTH) return s
        val prefix = withImplicitMultiply(s.expression)
        val current = prefix.substring(numberStart(prefix))
        val next = if (current == "0") prefix.dropLast(1) + d else prefix + d
        return update(s, next)
    }

    private fun decimal(s: CalcState): CalcState {
        if (s.expression.length >= MAX_LENGTH) return s
        val prefix = withImplicitMultiply(s.expression)
        val current = prefix.substring(numberStart(prefix))
        if (current.contains('.')) return s
        return update(s, if (current.isEmpty()) "${prefix}0." else "$prefix.")
    }

    private fun operator(s: CalcState, op: Char): CalcState {
        val e = s.expression
        if (e.isEmpty()) return if (op == '-') update(s, "-") else s
        if (e.length >= MAX_LENGTH) return s
        val last = e.last()
        if (last == '(') return if (op == '-') update(s, "$e-") else s
        if (last in OPERATORS) {
            val trimmed = e.dropLast(1)
            // Keep a leading minus sign instead of replacing it.
            if (trimmed.isEmpty() || trimmed.last() == '(') return s
            return update(s, trimmed + op)
        }
        return update(s, e + op)
    }

    private fun openBracket(s: CalcState): CalcState {
        if (s.expression.length >= MAX_LENGTH) return s
        return update(s, withImplicitMultiply(s.expression, includeDot = true) + "(")
    }

    private fun closeBracket(s: CalcState): CalcState {
        val e = s.expression
        if (e.isEmpty()) return s
        val open = e.count { it == '(' }
        val close = e.count { it == ')' }
        if (open <= close) return s
        val last = e.last()
        if (last in OPERATORS || last == '(') return s
        return update(s, "$e)")
    }

    private fun percent(s: CalcState): CalcState {
        val e = s.expression
        if (e.isEmpty() || e.length >= MAX_LENGTH) return s
        val last = e.last()
        return if (last.isDigit() || last == ')' || last == '%') update(s, "$e%") else s
    }

    private fun squareRoot(s: CalcState): CalcState {
        if (s.expression.length >= MAX_LENGTH) return s
        return update(s, withImplicitMultiply(s.expression, includeDot = true) + "√")
    }

    private fun backspace(s: CalcState): CalcState {
        if (s.expression.isEmpty()) return s
        return update(s, s.expression.dropLast(1))
    }

    private fun negate(s: CalcState): CalcState {
        val e = s.expression
        if (e.isEmpty()) return s
        val start = numberStart(e)
        if (start == e.length) return s // no number at the end to flip
        val before = if (start > 0) e[start - 1] else null
        val next = when {
            before == null -> "-$e"
            before == '√' -> return s
            before == '-' && (start - 1 == 0 || e[start - 2] in "(×÷^") ->
                e.removeRange(start - 1, start) // remove the unary minus
            before == '-' -> e.replaceRange(start - 1, start, "+")
            before == '+' -> e.replaceRange(start - 1, start, "-")
            else -> e.substring(0, start) + "-" + e.substring(start)
        }
        return update(s, next)
    }

    private fun equals(s: CalcState): CalcState {
        if (s.expression.isEmpty()) return s
        return when (val r = CalculatorEngine.evaluate(s.expression)) {
            is CalcResult.Success -> {
                val text = CalculatorEngine.format(r.value)
                s.copy(
                    result = text,
                    error = null,
                    justEvaluated = true,
                    history = (listOf(HistoryItem(s.expression, text)) + s.history).take(MAX_HISTORY),
                )
            }
            is CalcResult.Failure -> s.copy(result = "", error = r.error.message, justEvaluated = false)
        }
    }

    // ---- helpers ------------------------------------------------------------------------

    /** Index where the number at the end of [e] begins (equals e.length if there is none). */
    private fun numberStart(e: String): Int {
        var i = e.length
        while (i > 0 && (e[i - 1].isDigit() || e[i - 1] == '.')) i--
        return i
    }

    /** Typing a number straight after ")" or "%" means multiply, e.g. (2)3 becomes (2)×3. */
    private fun withImplicitMultiply(e: String, includeDot: Boolean = false): String {
        if (e.isEmpty()) return e
        val last = e.last()
        val needs = last == ')' || last == '%' ||
            (includeDot && (last.isDigit() || last == '.'))
        return if (needs) "$e×" else e
    }

    /** Stores the new expression and refreshes the live preview. */
    private fun update(s: CalcState, expression: String): CalcState {
        val preview = if (expression.toDoubleOrNull() != null) {
            ""
        } else {
            when (val r = CalculatorEngine.evaluate(expression)) {
                is CalcResult.Success -> CalculatorEngine.format(r.value)
                is CalcResult.Failure -> ""
            }
        }
        return s.copy(expression = expression, result = preview, error = null, justEvaluated = false)
    }
}
