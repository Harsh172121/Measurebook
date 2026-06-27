package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val customerId: String,
    val name: String,
    val mobile: String,
    val alternateMobile: String?,
    val address: String?,
    val notes: String?,
    val createdAt: String,
    val updatedAt: String
)
