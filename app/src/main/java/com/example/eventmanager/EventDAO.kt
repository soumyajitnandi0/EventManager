package com.example.eventmanager

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update


@Dao
interface EventDAO {

    @Insert
    suspend fun insertEvent(events: Events)

    @Update
    suspend fun updateEvent(events: Events)

    @Delete
    suspend fun deleteEvent(events: Events)

    @Query("SELECT * FROM Events")
    fun getEvent(): LiveData<List<Event>>

}