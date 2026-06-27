package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.UUID

@Entity(tableName = "measurements")
data class Measurement(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val customerId: String, // foreign key pointing to Customer.id
    val category: String, // "blouse" or "punjabi-dress"
    val subCategory: String?, // nullable — "top" or "salwar"
    val fields: Map<String, Double>, // stored as JSON via TypeConverter
    val notes: String?,
    val createdAt: String,
    val updatedAt: String
)

class Converters {
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val mapType = Types.newParameterizedType(Map::class.java, String::class.java, java.lang.Double::class.java)
    private val adapter = moshi.adapter<Map<String, Double>>(mapType)

    @TypeConverter
    fun fromString(value: String?): Map<String, Double>? {
        if (value == null) return null
        return try {
            adapter.fromJson(value)
        } catch (e: Exception) {
            emptyMap()
        }
    }

    @TypeConverter
    fun fromMap(map: Map<String, Double>?): String? {
        if (map == null) return null
        return adapter.toJson(map)
    }
}
