package edu.nextstep.camp.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import edu.nextstep.camp.domain.calculator.Expression

@Entity(
    tableName = "expression_records",
)
internal data class ExpressionRecordEntity(
    @ColumnInfo(name = "expression") val expression: Expression,
    @ColumnInfo(name = "result") val result: Int,
    @PrimaryKey(autoGenerate = true) val id: Int = 0
)