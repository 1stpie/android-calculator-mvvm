package edu.nextstep.camp.domain.calculator

interface ExpressionRecordsRepository {
    suspend fun saveExpressionRecord(vararg records: ExpressionRecord)
    suspend fun loadExpressionRecords(): List<ExpressionRecord>
}