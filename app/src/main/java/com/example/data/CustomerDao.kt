package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id")
    fun getCustomerById(id: String): Flow<Customer?>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerByIdDirect(id: String): Customer?

    @Query("SELECT * FROM customers WHERE name LIKE :query OR mobile LIKE :query OR customerId LIKE :query ORDER BY name ASC")
    fun searchCustomers(query: String): Flow<List<Customer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer)

    @Delete
    suspend fun deleteCustomer(customer: Customer)

    @Query("SELECT COUNT(*) FROM customers")
    fun getCustomerCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM customers WHERE createdAt >= :dateTime")
    fun getCustomerCountCreatedSince(dateTime: String): Flow<Int>
}
