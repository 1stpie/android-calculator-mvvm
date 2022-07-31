package edu.nextstep.camp.data

import android.content.Context
import edu.nextstep.camp.domain.calculator.ExpressionRecordsRepository

object Injector {
    fun provideRecordsRepository(context: Context): ExpressionRecordsRepository {
        return AppDatabase.getInstance(context).expressionRecordDao()
            .let {
                ExpressionRecordsRepositoryImpl(it)
            }
    }
}