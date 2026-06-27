package com.example.data

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow

class TailorRepository(private val database: AppDatabase) {
    private val customerDao = database.customerDao()
    private val measurementDao = database.measurementDao()

    val allCustomers: Flow<List<Customer>> = customerDao.getAllCustomers()
    val allMeasurements: Flow<List<Measurement>> = measurementDao.getAllMeasurements()

    val customerCount: Flow<Int> = customerDao.getCustomerCount()
    val measurementCount: Flow<Int> = measurementDao.getMeasurementCount()

    fun getCustomersCreatedSince(dateTime: String): Flow<Int> =
        customerDao.getCustomerCountCreatedSince(dateTime)

    fun getCustomerById(id: String): Flow<Customer?> = customerDao.getCustomerById(id)
    
    suspend fun getCustomerByIdDirect(id: String): Customer? = customerDao.getCustomerByIdDirect(id)

    fun searchCustomers(query: String): Flow<List<Customer>> {
        return if (query.isBlank()) {
            customerDao.getAllCustomers()
        } else {
            customerDao.searchCustomers("%$query%")
        }
    }

    fun getMeasurementsForCustomer(customerId: String): Flow<List<Measurement>> =
        measurementDao.getMeasurementsForCustomer(customerId)

    fun getMeasurementById(id: String): Flow<Measurement?> =
        measurementDao.getMeasurementById(id)

    suspend fun insertCustomer(customer: Customer) {
        customerDao.insertCustomer(customer)
    }

    suspend fun deleteCustomer(customer: Customer) {
        database.withTransaction {
            measurementDao.deleteMeasurementsForCustomer(customer.id)
            customerDao.deleteCustomer(customer)
        }
    }

    suspend fun insertMeasurement(measurement: Measurement) {
        measurementDao.insertMeasurement(measurement)
    }

    suspend fun deleteMeasurement(measurement: Measurement) {
        measurementDao.deleteMeasurement(measurement)
    }
}
