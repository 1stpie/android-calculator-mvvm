package edu.nextstep.camp.data

import edu.nextstep.camp.domain.calculator.ExpressionRecord
import edu.nextstep.camp.domain.calculator.ExpressionRecordsRepository

internal class ExpressionRecordsRepositoryImpl(
    private val expressionRecordsDao: ExpressionRecordsDao
) : ExpressionRecordsRepository {

    override suspend fun saveExpressionRecord(vararg records: ExpressionRecord) {
        expressionRecordsDao.insertExpressionRecords(*records.map(::mapTo).toTypedArray())
    }

    override suspend fun loadExpressionRecords(): List<ExpressionRecord> {
        return expressionRecordsDao.loadExpressionRecords().map(::mapTo).toList()
    }

    private fun mapTo(expressionRecord: ExpressionRecordEntity): ExpressionRecord = ExpressionRecord(expressionRecord.expression, expressionRecord.result)
    private fun mapTo(expressionRecord: ExpressionRecord): ExpressionRecordEntity = ExpressionRecordEntity(expressionRecord.expression, expressionRecord.result)
}