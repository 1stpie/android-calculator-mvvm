package edu.nextstep.camp.calculator

import edu.nextstep.camp.domain.calculator.Expression
import edu.nextstep.camp.domain.calculator.ExpressionRecord

sealed class CalculatorState {
    data class ShowExpression(val expression: Expression) : CalculatorState()
    data class ShowResult(val result: Int) : CalculatorState()
    data class LoadedCalculatorHistory(val records: List<ExpressionRecord>) : CalculatorState()
    object ShowIncompleteExpressionError : CalculatorState()
}
