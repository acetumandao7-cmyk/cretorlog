package com.example.creatorlog

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val clientId: Int,
    val projectName: String,
    val projectType: String,
    val eventDate: String,
    val location: String,
    val deadline: String,
    val status: String
)