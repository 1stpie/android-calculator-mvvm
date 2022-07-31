package edu.nextstep.camp.calculator

import com.google.common.truth.Truth.assertThat
import edu.nextstep.camp.counter.getOrAwaitValue
import edu.nextstep.camp.domain.calculator.Expression
import edu.nextstep.camp.domain.calculator.ExpressionRecord
import edu.nextstep.camp.domain.calculator.ExpressionRecordsRepository
import edu.nextstep.camp.domain.calculator.Operator
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource

class CalculatorViewModelTest {

    @JvmField
    @RegisterExtension
    val instantTaskExecutorExtension = InstantTaskExecutorExtension()


    @ParameterizedTest(name = "입력 : {0}")
    @ValueSource(ints = [1, 2, 3, 4, 5, 6, 7, 8, 9])
    internal fun `숫자가 입력되면 수식에 추가되고 변경된 수식을 보여줘야 한다`(value: Int) {
        //given
        val viewModel = CalculatorViewModel(
            expressionRecordsRepository = mockk(),
        )
        val expected = Expression(listOf(value))

        //when
        viewModel.onEvent(CalculatorEvent.operand(value))
        val actual = viewModel.onState.getOrAwaitValue().consume() as CalculatorState.ShowExpression

        //then
        assertThat(actual.expression).isEqualTo(expected)
    }

    @ParameterizedTest
    @EnumSource(value = Operator::class)
    internal fun `숫자 뒤에 연산자가 입력되면 연산자가 추가되고 변경된 수식을 보여줘야 한다`(value: Operator) {
        //given
        val viewModel = CalculatorViewModel(
            expressionRecordsRepository = mockk(),
            expression = Expression(listOf(1))
        )
        val expected = Expression(listOf(1, value))

        //when
        viewModel.onEvent(CalculatorEvent.AddOperator(value))
        val actual = viewModel.onState.getOrAwaitValue().consume() as CalculatorState.ShowExpression

        //then
        assertThat(actual.expression).isEqualTo(expected)
    }

    @Test
    internal fun `숫자 뒤에 숫자가 입력되면 변경된 숫자를 보여줘야 한다`() {
        //given
        val viewModel = CalculatorViewModel(
            expressionRecordsRepository = mockk(), expression = Expression(listOf(123))
        )
        val expected = Expression(listOf(1239))

        //when
        viewModel.onEvent(CalculatorEvent.operand(9))
        val actual = viewModel.onState.getOrAwaitValue().consume() as CalculatorState.ShowExpression

        //then
        assertThat(actual.expression).isEqualTo(expected)
    }


    @Test
    internal fun `연산자가 입력된 상태에서 연산자를 추가 입력하면 마지막으로 입력된 연산자로 변경된다`() {
        //given
        val viewModel = CalculatorViewModel(
            expressionRecordsRepository = mockk(),
            expression = Expression(listOf(1, Operator.Plus))
        )
        val expected = Expression(listOf(1, Operator.Multiply))

        //when
        viewModel.onEvent(CalculatorEvent.AddOperator(Operator.Minus))
        viewModel.onEvent(CalculatorEvent.AddOperator(Operator.Plus))
        viewModel.onEvent(CalculatorEvent.AddOperator(Operator.Divide))
        viewModel.onEvent(CalculatorEvent.AddOperator(Operator.Multiply))
        val actual = viewModel.onState.getOrAwaitValue().consume() as CalculatorState.ShowExpression

        //then
        assertThat(actual.expression).isEqualTo(expected)
    }

    @Test
    internal fun `수식이 완성되지 않은 상태에서 계산을 하면 변화가 없다`() {
        //given
        val viewModel = CalculatorViewModel(
            expressionRecordsRepository = mockk(),
            expression = Expression(listOf(3, Operator.Multiply, 90, Operator.Minus))
        )

        val expected = CalculatorState.ShowIncompleteExpressionError

        //when
        viewModel.onEvent(CalculatorEvent.Calculate)
        val actual = viewModel.onState.getOrAwaitValue().consume() as CalculatorState.ShowIncompleteExpressionError

        //then
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    internal fun `수식이 완성된 상태에서 계산을 하면 수식의 결과를 보여줘야 한다`() {
        //given
        val expected = 270
        val viewModel = CalculatorViewModel(
            expressionRecordsRepository = mockk(),
            expression = Expression(listOf(3, Operator.Multiply, 90))
        )

        //when
        viewModel.onEvent(CalculatorEvent.Calculate)
        val actual = viewModel.onState.getOrAwaitValue().consume() as CalculatorState.ShowResult

        //then
        assertThat(actual.result).isEqualTo(expected)
    }

    @Test
    internal fun `수식이 입력된 상태에서 삭제 버튼을 누르면 마지막 입력된 숫자 또는 연산자가 삭제된다`() {
        //given
        val expected = Expression(listOf(1, Operator.Plus, 3))
        val viewModel = CalculatorViewModel(
            expressionRecordsRepository = mockk(),
            expression = Expression(listOf(1, Operator.Plus, 32))
        )

        //when
        viewModel.onEvent(CalculatorEvent.RemoveLast)
        val actual = viewModel.onState.getOrAwaitValue().consume() as CalculatorState.ShowExpression

        //then
        assertThat(actual.expression).isEqualTo(expected)
    }

    @Test
    internal fun `계산 기록이 표시되지 않은 상태에서 계산 기록을 활성화하면 계산 기록이 노출된다`() = runTest {
        //given
        val expressionRecord = ExpressionRecord(Expression(listOf(1, Operator.Plus, 32)), 33)
        val expected: List<ExpressionRecord> = listOf(expressionRecord)
        val repository = object : ExpressionRecordsRepository {
            override suspend fun saveExpressionRecord(vararg records: ExpressionRecord) {}
            override suspend fun loadExpressionRecords(): List<ExpressionRecord> = listOf(ExpressionRecord(Expression(listOf(1, Operator.Plus, 32)), 33))
        }

        val viewModel = CalculatorViewModel(
            coroutineScope = this,
            isDisplayingExpressionHistory = false,
            expressionRecordsRepository = repository
        )

        //when
        viewModel.onEvent(CalculatorEvent.ToggleCalculatorHistory)
        val actual = viewModel.onState.getOrAwaitValue().consume() as CalculatorState.LoadedCalculatorHistory

        //then
        assertThat(actual.records).isEqualTo(expected)
    }

    @Test
    internal fun `계산 기록이 표시된 상태에서 계산 기록을 비활성화하면 이전 계산 진행이 노출된다`() {
        //given
        val expected = Expression(listOf(1, Operator.Plus, 32))
        val repository = object : ExpressionRecordsRepository {
            override suspend fun saveExpressionRecord(vararg records: ExpressionRecord) {}
            override suspend fun loadExpressionRecords(): List<ExpressionRecord> = emptyList()
        }
        val viewModel = CalculatorViewModel(
            expression = expected,
            isDisplayingExpressionHistory = true,
            expressionRecordsRepository = repository
        )

        //when
        viewModel.onEvent(CalculatorEvent.ToggleCalculatorHistory)
        val actual = viewModel.onState.getOrAwaitValue().consume() as CalculatorState.ShowExpression

        //then
        assertThat(actual.expression).isEqualTo(expected)
    }

    @Test
    internal fun `연산을 할 때 마다 계산 기록이 저장되어야 한다`() {


        runBlocking {
            //given
            val expression = Expression(listOf(11, Operator.Multiply, 3))
            val expected = ExpressionRecord(expression, 33)
            val repository: ExpressionRecordsRepository = mockk(relaxed = true)
            val viewModel = CalculatorViewModel(
                expression = expression,
                isDisplayingExpressionHistory = true,
                expressionRecordsRepository = repository
            )

            //when
            viewModel.onEvent(CalculatorEvent.Calculate)

            //then
            repository.saveExpressionRecord(expected)
        }
    }
}
