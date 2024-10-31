package com.nexustech.bitfitpart1

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface EntryDao {
    @Insert
    suspend fun insert(entry: Entry): Long

    @Query("SELECT * FROM entry_table ORDER BY date DESC")
    fun getAllEntries(): LiveData<List<Entry>>

    @Query("SELECT AVG(sleepHours) FROM entry_table")
    fun getAverageSleep(): LiveData<Float>

    @Query("SELECT AVG(moodRating) FROM entry_table")
    fun getAverageMood(): LiveData<Float>
}
