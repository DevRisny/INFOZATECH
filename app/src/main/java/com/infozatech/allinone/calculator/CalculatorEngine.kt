package com.infozatech.allinone.calculator

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

/** The result of evaluating an expression. */
sealed interface CalcResult {
    data class Success(val value: Double) : CalcResult
    data class Failure(val error: CalcError) : CalcResult
}

enum class CalcError(val message: String) {
    DIVIDE_BY_ZERO("Cannot divide by zero"),
    INVALID("Invalid expression"),
    OVERFLOW("Result is too large"),
}

private class CalcException(val error: CalcError) : Exception()

/**
 * A small expression evaluator written as a recursive-descent parser.
 *
 * Supported: + - × ÷ ^ (power), % (divide by 100), √, parentheses, decimals
 * and a leading minus sign. Operator precedence follows normal maths rules.
 *
 * Grammar:
 *   expression := term (('+' | '-') term)*
 *   term       := unary (('×' | '÷') unary)*
 *   unary      := ('-' | '+') unary | power
 *   power      := postfix ('^' unary)?
 *   postfix    := primary '%'*
 *   primary    := number | '(' expression ')' | '√' primary
 */
object CalculatorEngine {

    fun evaluate(raw: String): CalcResult {
        val expression = prepare(raw)
        if (expression.isEmpty()) return CalcResult.Failure(CalcError.INVALID)
        return try {
            val value = Parser(expression).parse()
            if (value.isNaN() || value.isInfinite()) {
                CalcResult.Failure(CalcError.OVERFLOW)
            } else {
                CalcResult.Success(value)
            }
        } catch (e: CalcException) {
            CalcResult.Failure(e.error)
        }
    }

    /** Turns a double into clean display text: 0.1 + 0.2 shows as 0.3, 4.0 shows as 4. */
    fun format(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "Error"
        if (value == 0.0) return "0"
        val rounded = BigDecimal(value)
            .round(MathContext(12, RoundingMode.HALF_UP))
            .stripTrailingZeros()
        val magnitude = Math.abs(value)
        return if (magnitude >= 1e15 || magnitude < 1e-9) {
            rounded.toString()
        } else {
            rounded.toPlainString()
        }
    }

    /** Removes dangling operators and closes any open brackets so live previews work. */
    private fun prepare(raw: String): String {
        var text = raw.trim()
        while (text.isNotEmpty() && text.last() in "+-×÷^(") {
            text = text.dropLast(1)
        }
        val open = text.count { it == '(' }
        val close = text.count { it == ')' }
        if (open > close) text += ")".repeat(open - close)
        return text
    }

    private class Parser(private val s: String) {
        private var pos = 0

        fun parse(): Double {
            val value = expression()
            if (pos != s.length) throw CalcException(CalcError.INVALID)
            return value
        }

        private fun peek(): Char? = if (pos < s.length) s[pos] else null

        private fun expression(): Double {
            var value = term()
            while (true) {
                when (peek()) {
                    '+' -> { pos++; value += term() }
                    '-' -> { pos++; value -= term() }
                    else -> return value
                }
            }
        }

        private fun term(): Double {
            var value = unary()
            while (true) {
                when (peek()) {
                    '×' -> { pos++; value *= unary() }
                    '÷' -> {
                        pos++
                        val divisor = unary()
                        if (divisor == 0.0) throw CalcException(CalcError.DIVIDE_BY_ZERO)
                        value /= divisor
                    }
                    else -> return value
                }
            }
        }

        private fun unary(): Double = when (peek()) {
            '-' -> { pos++; -unary() }
            '+' -> { pos++; unary() }
            else -> power()
        }

        private fun power(): Double {
            val base = postfix()
            if (peek() == '^') {
                pos++
                return Math.pow(base, unary())
            }
            return base
        }

        private fun postfix(): Double {
            var value = primary()
            while (peek() == '%') {
                pos++
                value /= 100.0
            }
            return value
        }

        private fun primary(): Double {
            val c = peek() ?: throw CalcException(CalcError.INVALID)
            return when {
                c == '(' -> {
                    pos++
                    val inner = expression()
                    if (peek() != ')') throw CalcException(CalcError.INVALID)
                    pos++
                    inner
                }
                c == '√' -> {
                    pos++
                    val inner = primary()
                    if (inner < 0) throw CalcException(CalcError.INVALID)
                    Math.sqrt(inner)
                }
                c.isDigit() || c == '.' -> number()
                else -> throw CalcException(CalcError.INVALID)
            }
        }

        private fun number(): Double {
            val start = pos
            var dots = 0
            while (pos < s.length && (s[pos].isDigit() || s[pos] == '.')) {
                if (s[pos] == '.') dots++
                pos++
            }
            val text = s.substring(start, pos)
            if (dots > 1 || text == ".") throw CalcException(CalcError.INVALID)
            return text.toDouble()
        }
    }
}
