// Entry.kt
package com.nexustech.bitfitpart1

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "entry_table")
data class Entry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,
    val sleepHours: Float,
    val moodRating: Int,
    val notes: String?,
    val photoPath: String?
)
