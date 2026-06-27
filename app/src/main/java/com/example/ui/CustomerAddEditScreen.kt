package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.Localization

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerAddEditScreen(
    viewModel: TailorViewModel,
    customerId: String?, // Passed if editing
    onNavigateBack: () -> Unit,
    onSuccess: (String) -> Unit // Navigates to detail with customerId
) {
    val lang by viewModel.language.collectAsState()
    
    val isEditMode = !customerId.isNullOrBlank()
    
    // Form fields
    var name by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var alternateMobile by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    
    // Form validation errors
    var nameError by remember { mutableStateOf<String?>(null) }
    var mobileError by remember { mutableStateOf<String?>(null) }
    
    var isInitialized by remember { mutableStateOf(false) }
    
    // Load customer if editing
    val customerFlow = remember(customerId) {
        if (isEditMode) viewModel.getCustomerById(customerId!!) else null
    }
    val customer by (customerFlow?.collectAsState(initial = null) ?: remember { mutableStateOf(null) })
    
    LaunchedEffect(customer) {
        if (isEditMode && customer != null && !isInitialized) {
            name = customer!!.name
            mobile = customer!!.mobile
            alternateMobile = customer!!.alternateMobile ?: ""
            address = customer!!.address ?: ""
            notes = customer!!.notes ?: ""
            isInitialized = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditMode) Localization.get("edit_customer", lang) else Localization.get("add_customer", lang),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Validations
                    nameError = if (name.isBlank()) Localization.get("validation_name_empty", lang) else null
                    
                    val digitsOnly = mobile.filter { it.isDigit() }
                    mobileError = if (mobile.isNotBlank() && (digitsOnly.length < 10 || digitsOnly.length > 15)) {
                        Localization.get("validation_mobile_invalid", lang)
                    } else null
                    
                    if (nameError == null && mobileError == null) {
                        if (isEditMode && customer != null) {
                            viewModel.updateCustomer(
                                id = customer!!.id,
                                originalCustomerId = customer!!.customerId,
                                name = name,
                                mobile = mobile,
                                alternateMobile = alternateMobile,
                                address = address,
                                notes = notes,
                                createdAt = customer!!.createdAt,
                                onSuccess = {
                                    onSuccess(customer!!.id)
                                }
                            )
                        } else {
                            viewModel.addCustomer(
                                name = name,
                                mobile = mobile,
                                alternateMobile = alternateMobile,
                                address = address,
                                notes = notes,
                                onSuccess = { newId ->
                                    onSuccess(newId)
                                }
                            )
                        }
                    }
                },
                modifier = Modifier.testTag("customer_save_fab"),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Check, contentDescription = "Save")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val fieldColors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                errorBorderColor = MaterialTheme.colorScheme.error,
                errorLabelColor = MaterialTheme.colorScheme.error,
                errorTextColor = MaterialTheme.colorScheme.onSurface
            )

            // Customer Name
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    if (nameError != null && it.isNotBlank()) nameError = null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("customer_input_name"),
                label = { Text(Localization.get("name", lang) + " *") },
                isError = nameError != null,
                supportingText = nameError?.let { { Text(it) } },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = fieldColors
            )

            // Mobile Number
            OutlinedTextField(
                value = mobile,
                onValueChange = {
                    mobile = it.filter { char -> char.isDigit() || char == '+' }
                    if (mobileError != null) mobileError = null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("customer_input_mobile"),
                label = { Text(Localization.get("mobile", lang)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = mobileError != null,
                supportingText = mobileError?.let { { Text(it) } },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = fieldColors
            )

            // Alternate Mobile Number
            OutlinedTextField(
                value = alternateMobile,
                onValueChange = {
                    alternateMobile = it.filter { char -> char.isDigit() || char == '+' }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("customer_input_alt_mobile"),
                label = { Text(Localization.get("alt_mobile", lang)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = fieldColors
            )

            // Address
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("customer_input_address"),
                label = { Text(Localization.get("address", lang)) },
                minLines = 2,
                shape = RoundedCornerShape(16.dp),
                colors = fieldColors
            )

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("customer_input_notes"),
                label = { Text(Localization.get("notes", lang)) },
                minLines = 3,
                shape = RoundedCornerShape(16.dp),
                colors = fieldColors
            )
            
            Spacer(modifier = Modifier.height(80.dp)) // Cushion for FAB
        }
    }
}
