package com.example.eventmanager

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "Events")
data class Event(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val title: String,
    val date: String,
    val time: String,
    val location: String
)
