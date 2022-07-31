package edu.nextstep.camp.calculator

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.nextstep.camp.domain.calculator.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class CalculatorViewModel(
    private val calculator: Calculator = Calculator(),
    private var expression: Expression = Expression.EMPTY,
    private var isDisplayingExpressionHistory: Boolean = false,
    private val expressionRecordsRepository: ExpressionRecordsRepository,
    coroutineScope: CoroutineScope? = null
) : ViewModel() {

    private val coroutineScope = coroutineScope ?: viewModelScope

    private val _onState = MutableLiveData<Event<CalculatorState>>()
    val onState: LiveData<Event<CalculatorState>> get() = _onState


    fun onEvent(event: CalculatorEvent) {
        when (event) {
            is CalculatorEvent.AddOperand -> eventAddOperand(event.operand)
            is CalculatorEvent.AddOperator -> eventAddOperator(event.operator)
            CalculatorEvent.Calculate -> eventCalculate()
            CalculatorEvent.RemoveLast -> eventRemoveLast()
            CalculatorEvent.ToggleCalculatorHistory -> eventToggleCalculatorHistory()
        }
    }

    private fun sendViewState(content: CalculatorState) {
        _onState.postValue(Event(content))
    }

    private fun sendShowExpressionState() {
        isDisplayingExpressionHistory = false
        sendViewState(CalculatorState.ShowExpression(expression))
    }

    private fun sendLoadedCalculatorRecordsState() {
        coroutineScope.launch {
            expressionRecordsRepository.loadExpressionRecords().let {
                CalculatorState.LoadedCalculatorHistory(it)
            }.run {
                sendViewState(this)
            }
        }
    }

    private fun eventToggleCalculatorHistory() {
        if (isDisplayingExpressionHistory) {
            sendShowExpressionState()
        } else {
            sendLoadedCalculatorRecordsState()
            isDisplayingExpressionHistory = true
        }
    }

    private fun eventAddOperand(operand: Int) {
        expression += operand
        sendShowExpressionState()
    }

    private fun eventAddOperator(operator: Operator) {
        expression += operator
        sendShowExpressionState()
    }

    private fun eventRemoveLast() {
        expression = expression.removeLast()
        sendShowExpressionState()
    }

    private fun eventCalculate() {
        val result = calculator.calculate(expression.toString())
        if (result == null) {
            sendViewState(CalculatorState.ShowIncompleteExpressionError)
        } else {
            saveCalculatorResult(result)
            expression = Expression(listOf(result))
            sendViewState(CalculatorState.ShowResult(result))
        }
    }

    private fun saveCalculatorResult(result: Int) {
        coroutineScope.launch {
            expressionRecordsRepository.saveExpressionRecord(
                ExpressionRecord(expression, result)
            )
        }
    }
}
