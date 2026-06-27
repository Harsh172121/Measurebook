package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.util.BackupManager
import com.example.util.CustomerTrie
import com.example.util.DateUtil
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TailorViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = TailorRepository(database)
    private val preferencesManager = PreferencesManager(application)
    private val backupManager = BackupManager(database)

    // Shop Settings
    val shopName = preferencesManager.shopName
    val ownerName = preferencesManager.ownerName
    val shopPhone = preferencesManager.shopPhone
    val shopAddress = preferencesManager.shopAddress
    val language = preferencesManager.language
    val theme = preferencesManager.theme

    // Keep track of whether the splash screen was shown during this app launch
    var hasShownSplash = false

    // Dashboard search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // In-memory Customer Trie constructed reactively from repository.allCustomers
    private val customerTrieFlow: Flow<CustomerTrie> = repository.allCustomers
        .map { customers ->
            val trie = CustomerTrie()
            for (customer in customers) {
                trie.insert(customer)
            }
            trie
        }

    // Reactive search results using Trie algorithm for instant prefix matching
    val searchResults: StateFlow<List<Customer>> = combine(
        _searchQuery,
        repository.allCustomers,
        customerTrieFlow
    ) { query, customers, trie ->
        if (query.isBlank()) {
            customers
        } else {
            trie.search(query)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All customers
    val allCustomers = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Recent 5 customers sorted by updatedAt desc
    val recentCustomers = repository.allCustomers
        .map { list ->
            list.sortedByDescending { it.updatedAt }.take(5)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Stats
    val totalCustomersCount = repository.customerCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalMeasurementsCount = repository.measurementCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val recentCustomersCount = repository.getCustomersCreatedSince(DateUtil.getIsoString30DaysAgo())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Helper to generate customer ID
    fun generateCustomerId(name: String): String {
        val cleanName = name.filter { it.isLetter() }.uppercase()
        val prefix = if (cleanName.length >= 3) {
            cleanName.substring(0, 3)
        } else if (cleanName.isNotEmpty()) {
            cleanName + "X".repeat(3 - cleanName.length)
        } else {
            "CUS"
        }
        val base36Timestamp = java.lang.Long.toString(System.currentTimeMillis(), 36).uppercase()
        return "$prefix-$base36Timestamp"
    }

    // Customer CRUD
    fun addCustomer(
        name: String,
        mobile: String,
        alternateMobile: String?,
        address: String?,
        notes: String?,
        onSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {
            val generatedId = generateCustomerId(name)
            val customer = Customer(
                customerId = generatedId,
                name = name.trim(),
                mobile = mobile.trim(),
                alternateMobile = alternateMobile?.trim()?.takeIf { it.isNotEmpty() },
                address = address?.trim()?.takeIf { it.isNotEmpty() },
                notes = notes?.trim()?.takeIf { it.isNotEmpty() },
                createdAt = DateUtil.getCurrentIsoString(),
                updatedAt = DateUtil.getCurrentIsoString()
            )
            repository.insertCustomer(customer)
            onSuccess(customer.id)
        }
    }

    fun updateCustomer(
        id: String,
        originalCustomerId: String,
        name: String,
        mobile: String,
        alternateMobile: String?,
        address: String?,
        notes: String?,
        createdAt: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val customer = Customer(
                id = id,
                customerId = originalCustomerId,
                name = name.trim(),
                mobile = mobile.trim(),
                alternateMobile = alternateMobile?.trim()?.takeIf { it.isNotEmpty() },
                address = address?.trim()?.takeIf { it.isNotEmpty() },
                notes = notes?.trim()?.takeIf { it.isNotEmpty() },
                createdAt = createdAt,
                updatedAt = DateUtil.getCurrentIsoString()
            )
            repository.insertCustomer(customer)
            onSuccess()
        }
    }

    fun deleteCustomer(customer: Customer, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
            onSuccess()
        }
    }

    fun getCustomerById(id: String): Flow<Customer?> = repository.getCustomerById(id)

    // Measurement CRUD
    fun getMeasurementsForCustomer(customerId: String): Flow<List<Measurement>> =
        repository.getMeasurementsForCustomer(customerId)

    fun getMeasurementById(id: String): Flow<Measurement?> =
        repository.getMeasurementById(id)

    fun addMeasurement(
        customerId: String,
        category: String,
        subCategory: String?,
        fields: Map<String, Double>,
        notes: String?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val measurement = Measurement(
                customerId = customerId,
                category = category,
                subCategory = subCategory,
                fields = fields,
                notes = notes?.trim()?.takeIf { it.isNotEmpty() },
                createdAt = DateUtil.getCurrentIsoString(),
                updatedAt = DateUtil.getCurrentIsoString()
            )
            repository.insertMeasurement(measurement)
            
            // Touch customer updatedAt
            repository.getCustomerByIdDirect(customerId)?.let { customer ->
                repository.insertCustomer(customer.copy(updatedAt = DateUtil.getCurrentIsoString()))
            }
            onSuccess()
        }
    }

    fun updateMeasurement(
        id: String,
        customerId: String,
        category: String,
        subCategory: String?,
        fields: Map<String, Double>,
        notes: String?,
        createdAt: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val measurement = Measurement(
                id = id,
                customerId = customerId,
                category = category,
                subCategory = subCategory,
                fields = fields,
                notes = notes?.trim()?.takeIf { it.isNotEmpty() },
                createdAt = createdAt,
                updatedAt = DateUtil.getCurrentIsoString()
            )
            repository.insertMeasurement(measurement)
            
            // Touch customer updatedAt
            repository.getCustomerByIdDirect(customerId)?.let { customer ->
                repository.insertCustomer(customer.copy(updatedAt = DateUtil.getCurrentIsoString()))
            }
            onSuccess()
        }
    }

    fun deleteMeasurement(measurement: Measurement, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.deleteMeasurement(measurement)
            onSuccess()
        }
    }

    // Settings
    fun saveShopInfo(name: String, owner: String, phone: String, address: String) {
        preferencesManager.saveShopInfo(name, owner, phone, address)
    }

    fun saveLanguage(lang: String) {
        preferencesManager.saveLanguage(lang)
    }

    fun saveTheme(theme: String) {
        preferencesManager.saveTheme(theme)
    }

    // Backup & Import
    fun exportBackup(onResult: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val json = backupManager.exportBackup()
                onResult(json)
            } catch (e: Exception) {
                onResult(null)
            }
        }
    }

    fun importBackup(jsonString: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = backupManager.importBackup(jsonString)
            onResult(success)
        }
    }
}
