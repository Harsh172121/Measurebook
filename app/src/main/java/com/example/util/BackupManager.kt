package com.example.util

import com.example.data.AppDatabase
import com.example.data.Customer
import com.example.data.Measurement
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class BackupManager(private val database: AppDatabase) {
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    
    data class BackupWrapper(
        val customers: List<Customer>,
        val measurements: List<Measurement>
    )

    private val adapter = moshi.adapter(BackupWrapper::class.java)

    suspend fun exportBackup(): String = withContext(Dispatchers.IO) {
        val customers = database.customerDao().getAllCustomers().first()
        val measurements = database.measurementDao().getAllMeasurements().first()
        val wrapper = BackupWrapper(customers, measurements)
        adapter.toJson(wrapper)
    }

    suspend fun importBackup(jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val wrapper = adapter.fromJson(jsonString) ?: return@withContext false
            val customerDao = database.customerDao()
            val measurementDao = database.measurementDao()
            
            for (customer in wrapper.customers) {
                customerDao.insertCustomer(customer)
            }
            for (measurement in wrapper.measurements) {
                measurementDao.insertMeasurement(measurement)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
