package com.infozatech.allinone

import com.infozatech.allinone.calculator.CalcError
import com.infozatech.allinone.calculator.CalcResult
import com.infozatech.allinone.calculator.CalculatorEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculatorEngineTest {

    private fun calc(expression: String): String =
        when (val r = CalculatorEngine.evaluate(expression)) {
            is CalcResult.Success -> CalculatorEngine.format(r.value)
            is CalcResult.Failure -> "ERR:" + r.error.name
        }

    @Test fun addition() = assertEquals("5", calc("2+3"))

    @Test fun precedence() = assertEquals("14", calc("2+3×4"))

    @Test fun brackets() = assertEquals("20", calc("(2+3)×4"))

    @Test fun decimals() = assertEquals("3.75", calc("1.5×2.5"))

    @Test fun floatingPointNoise() = assertEquals("0.3", calc("0.1+0.2"))

    @Test fun division() = assertEquals("2.5", calc("5÷2"))

    @Test fun subtractionToNegative() = assertEquals("-3", calc("2-5"))

    @Test fun leadingMinus() = assertEquals("-8", calc("-3-5"))

    @Test fun unaryAfterOperator() = assertEquals("15", calc("5×-3×-1"))

    @Test fun percent() = assertEquals("0.5", calc("50%"))

    @Test fun power() = assertEquals("1024", calc("2^10"))

    @Test fun powerIsRightAssociative() = assertEquals("512", calc("2^3^2"))

    @Test fun squareRoot() = assertEquals("12", calc("√144"))

    @Test fun squareRootInExpression() = assertEquals("7", calc("√9+4"))

    @Test fun divideByZero() = assertEquals("ERR:DIVIDE_BY_ZERO", calc("10÷0"))

    @Test fun zeroDividedByZero() = assertEquals("ERR:DIVIDE_BY_ZERO", calc("0÷0"))

    @Test fun divideByZeroInsideBrackets() = assertEquals("ERR:DIVIDE_BY_ZERO", calc("1÷(2-2)"))

    @Test fun negativeSquareRootIsInvalid() = assertEquals("ERR:INVALID", calc("√(0-4)"))

    @Test fun trailingOperatorIsIgnored() = assertEquals("5", calc("2+3+"))

    @Test fun unclosedBracketIsClosed() = assertEquals("9", calc("(1+2)×(1+2"))

    @Test fun emptyIsInvalid() = assertEquals("ERR:INVALID", calc(""))

    @Test fun twoDotsInvalid() = assertEquals("ERR:INVALID", calc("1.2.3+1"))

    @Test fun hugeResultIsOverflow() = assertEquals("ERR:OVERFLOW", calc("10^400"))

    @Test fun largeNumbersUseScientific() {
        assertTrue(CalculatorEngine.format(1e20).contains("E"))
    }

    @Test fun formatDropsTrailingZero() = assertEquals("4", CalculatorEngine.format(4.0))

    @Test fun errorMessagesAreFriendly() =
        assertEquals("Cannot divide by zero", CalcError.DIVIDE_BY_ZERO.message)
}
