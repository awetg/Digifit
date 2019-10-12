package com.bwet.digifit.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Step(
    val timeStampMills: Long,
    @PrimaryKey(autoGenerate = true) var id: Int = 0
)

data class StepCount(val count: Int, val intervalFormat: String)
