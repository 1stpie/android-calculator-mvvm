package edu.nextstep.camp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface ExpressionRecordsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpressionRecords(vararg expressionRecords: ExpressionRecordEntity)

    @Query("SELECT * FROM expression_records")
    suspend fun loadExpressionRecords(): List<ExpressionRecordEntity>
}

