package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.Customer
import com.example.data.Measurement
import com.example.data.TailorRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("MeasureBook", appName)
  }

  @Test
  fun `verify salwar measurement labels and translations`() {
    val label = com.example.util.MeasurementFields.getFieldLabel("salwarLength", "en")
    assertEquals("Salwar Length (સલવાર લંબાઈ)", label)
  }

  @Test
  fun `deleting a customer automatically deletes all their measurements`() = runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    
    val repository = TailorRepository(db)
    
    // 1. Create a customer
    val customer = Customer(
      id = "test-customer-uuid",
      customerId = "CUS-TEST",
      name = "John Doe",
      mobile = "1234567890",
      alternateMobile = null,
      address = "123 Main St",
      notes = "Regular customer",
      createdAt = "2026-06-26T00:00:00Z",
      updatedAt = "2026-06-26T00:00:00Z"
    )
    repository.insertCustomer(customer)
    
    // 2. Create some measurements associated with this customer
    val measurement1 = Measurement(
      id = "m1",
      customerId = customer.id, // points to test-customer-uuid
      category = "blouse",
      subCategory = null,
      fields = mapOf("bust" to 36.0, "waist" to 28.0),
      notes = "Fit nicely",
      createdAt = "2026-06-26T00:00:00Z",
      updatedAt = "2026-06-26T00:00:00Z"
    )
    val measurement2 = Measurement(
      id = "m2",
      customerId = customer.id, // points to test-customer-uuid
      category = "punjabi-dress",
      subCategory = "top",
      fields = mapOf("length" to 40.0),
      notes = "A bit loose",
      createdAt = "2026-06-26T00:00:01Z",
      updatedAt = "2026-06-26T00:00:01Z"
    )
    repository.insertMeasurement(measurement1)
    repository.insertMeasurement(measurement2)
    
    // Verify insertion
    val initialCustomers = repository.allCustomers.first()
    assertEquals(1, initialCustomers.size)
    assertEquals("test-customer-uuid", initialCustomers[0].id)
    
    val initialMeasurements = repository.allMeasurements.first()
    assertEquals(2, initialMeasurements.size)
    
    // 3. Delete the customer
    repository.deleteCustomer(customer)
    
    // 4. Verify customer is deleted
    val finalCustomers = repository.allCustomers.first()
    assertEquals(0, finalCustomers.size)
    
    // 5. Verify ALL measurements for this customer are deleted too
    val finalMeasurements = repository.allMeasurements.first()
    assertEquals(0, finalMeasurements.size)
    
    db.close()
  }

  @Test
  fun `verify CustomerTrie search functionality`() {
    val trie = com.example.util.CustomerTrie()
    
    val customer1 = Customer(
      id = "c1",
      customerId = "CUS-JDOE",
      name = "John Doe",
      mobile = "9876543210",
      alternateMobile = null,
      address = null,
      notes = null,
      createdAt = "2026-06-26T00:00:00Z",
      updatedAt = "2026-06-26T00:00:00Z"
    )
    
    val customer2 = Customer(
      id = "c2",
      customerId = "CUS-JSMITH",
      name = "Jane Smith",
      mobile = "1234567890",
      alternateMobile = null,
      address = null,
      notes = null,
      createdAt = "2026-06-26T00:00:00Z",
      updatedAt = "2026-06-26T00:00:00Z"
    )

    trie.insert(customer1)
    trie.insert(customer2)

    // Test case insensitive name prefix search
    val searchResultJ = trie.search("j")
    assertEquals(2, searchResultJ.size) // Matches "John Doe" and "Jane Smith"
    assertEquals("Jane Smith", searchResultJ[0].name) // Alphabetical sort
    assertEquals("John Doe", searchResultJ[1].name)

    // Test individual name parts search ("doe")
    val searchResultDoe = trie.search("doe")
    assertEquals(1, searchResultDoe.size)
    assertEquals("John Doe", searchResultDoe[0].name)

    // Test case insensitive individual name parts search ("DOE")
    val searchResultDoeCaps = trie.search("DOE")
    assertEquals(1, searchResultDoeCaps.size)
    assertEquals("John Doe", searchResultDoeCaps[0].name)

    // Test phone number search prefix ("987")
    val searchResultPhone = trie.search("987")
    assertEquals(1, searchResultPhone.size)
    assertEquals("John Doe", searchResultPhone[0].name)

    // Test customer ID search prefix ("cus-js")
    val searchResultId = trie.search("cus-js")
    assertEquals(1, searchResultId.size)
    assertEquals("Jane Smith", searchResultId[0].name)
    
    // Test empty query returns empty list
    val searchResultEmpty = trie.search("")
    assertEquals(0, searchResultEmpty.size)

    // Test mismatch returns empty list
    val searchResultNone = trie.search("xyz")
    assertEquals(0, searchResultNone.size)
  }
}
