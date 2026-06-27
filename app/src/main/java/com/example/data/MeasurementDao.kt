package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasurementDao {
    @Query("SELECT * FROM measurements ORDER BY updatedAt DESC")
    fun getAllMeasurements(): Flow<List<Measurement>>

    @Query("SELECT * FROM measurements WHERE customerId = :customerId ORDER BY updatedAt DESC")
    fun getMeasurementsForCustomer(customerId: String): Flow<List<Measurement>>

    @Query("SELECT * FROM measurements WHERE id = :id")
    fun getMeasurementById(id: String): Flow<Measurement?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurement(measurement: Measurement)

    @Delete
    suspend fun deleteMeasurement(measurement: Measurement)

    @Query("DELETE FROM measurements WHERE customerId = :customerId")
    suspend fun deleteMeasurementsForCustomer(customerId: String)

    @Query("SELECT COUNT(*) FROM measurements")
    fun getMeasurementCount(): Flow<Int>
}
