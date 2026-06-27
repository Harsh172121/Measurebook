package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Measurement
import com.example.util.Localization
import com.example.util.MeasurementFields

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeasurementAddEditScreen(
    viewModel: TailorViewModel,
    customerId: String,
    measurementId: String?, // Passed if editing
    onNavigateBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val lang by viewModel.language.collectAsState()
    
    val isEditMode = !measurementId.isNullOrBlank()
    
    // Category tabs
    var selectedCategory by remember { mutableStateOf("blouse") } // "blouse" or "punjabi-dress"
    var selectedSubCategory by remember { mutableStateOf("top") } // "top" or "salwar"
    
    // Form fields dictionary: stores key to typed string
    var fieldsState by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var notes by remember { mutableStateOf("") }
    
    var isInitialized by remember { mutableStateOf(false) }
    
    // Load customer name for header
    val customerState = viewModel.getCustomerById(customerId).collectAsState(initial = null)
    val customer = customerState.value
    
    // Load measurement if editing
    val measurementFlow = remember(measurementId) {
        if (isEditMode) viewModel.getMeasurementById(measurementId!!) else null
    }
    val measurement by (measurementFlow?.collectAsState(initial = null) ?: remember { mutableStateOf(null) })
    
    LaunchedEffect(measurement) {
        if (isEditMode && measurement != null && !isInitialized) {
            selectedCategory = measurement!!.category
            selectedSubCategory = measurement!!.subCategory ?: "top"
            notes = measurement!!.notes ?: ""
            
            val initialFields = mutableMapOf<String, String>()
            measurement!!.fields.forEach { (k, v) ->
                initialFields[k] = v.toString().removeSuffix(".0")
            }
            fieldsState = initialFields
            isInitialized = true
        }
    }

    val currentFields = remember(selectedCategory, selectedSubCategory) {
        when (selectedCategory) {
            "blouse" -> MeasurementFields.BLOUSE_FIELDS
            "punjabi-dress" -> {
                if (selectedSubCategory == "top") MeasurementFields.PUNJABI_TOP_FIELDS
                else MeasurementFields.SALWAR_PANT_FIELDS
            }
            else -> emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isEditMode) Localization.get("save_measurement", lang) else Localization.get("add_measurement", lang),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        customer?.let {
                            Text(
                                text = it.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
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
                    // Parse entered string values to Doubles
                    val parsedFields = mutableMapOf<String, Double>()
                    currentFields.forEach { field ->
                        val textValue = fieldsState[field.key]
                        if (!textValue.isNullOrBlank()) {
                            textValue.toDoubleOrNull()?.let { dValue ->
                                parsedFields[field.key] = dValue
                            }
                        }
                    }
                    
                    if (parsedFields.isEmpty()) {
                        Toast.makeText(context, Localization.get("validation_measurement_empty", lang), Toast.LENGTH_LONG).show()
                    } else {
                        if (isEditMode && measurement != null) {
                            viewModel.updateMeasurement(
                                id = measurement!!.id,
                                customerId = customerId,
                                category = selectedCategory,
                                subCategory = if (selectedCategory == "punjabi-dress") selectedSubCategory else null,
                                fields = parsedFields,
                                notes = notes,
                                createdAt = measurement!!.createdAt,
                                onSuccess = onSuccess
                            )
                        } else {
                            viewModel.addMeasurement(
                                customerId = customerId,
                                category = selectedCategory,
                                subCategory = if (selectedCategory == "punjabi-dress") selectedSubCategory else null,
                                fields = parsedFields,
                                notes = notes,
                                onSuccess = onSuccess
                            )
                        }
                    }
                },
                modifier = Modifier.testTag("measurement_save_fab"),
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
        ) {
            val fieldColors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            // Category Tabs (Show selection in both Add and Edit modes)
            TabRow(
                selectedTabIndex = if (selectedCategory == "blouse") 0 else 1,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[if (selectedCategory == "blouse") 0 else 1]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                Tab(
                    selected = selectedCategory == "blouse",
                    onClick = {
                        selectedCategory = "blouse"
                        fieldsState = emptyMap()
                    },
                    text = { Text(Localization.get("category_blouse", lang), fontWeight = FontWeight.Bold) },
                    selectedContentColor = MaterialTheme.colorScheme.onSurface,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Tab(
                    selected = selectedCategory == "punjabi-dress",
                    onClick = {
                        selectedCategory = "punjabi-dress"
                        fieldsState = emptyMap()
                    },
                    text = { Text(Localization.get("category_punjabi", lang), fontWeight = FontWeight.Bold) },
                    selectedContentColor = MaterialTheme.colorScheme.onSurface,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Sub Category Toggle (Only for Punjabi Dress, available in both Add and Edit modes)
            if (selectedCategory == "punjabi-dress") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { selectedSubCategory = "top" },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedSubCategory == "top") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            contentColor = if (selectedSubCategory == "top") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = if (selectedSubCategory != "top") androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(Localization.get("punjabi_top", lang), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }

                    Button(
                        onClick = { selectedSubCategory = "salwar" },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedSubCategory == "salwar") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            contentColor = if (selectedSubCategory == "salwar") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = if (selectedSubCategory != "salwar") androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(Localization.get("punjabi_bottom", lang), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            }

            // Measurement Fields Grid
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = Localization.get("all_fields_in_inches", lang),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                // Render grid of active fields (2 columns)
                currentFields.chunked(2).forEach { rowFields ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowFields.forEach { field ->
                            val labelValue = MeasurementFields.getFieldLabel(field.key, lang)
                            OutlinedTextField(
                                value = fieldsState[field.key] ?: "",
                                onValueChange = { input ->
                                    // Accept numbers and decimals only
                                    val filtered = input.filter { it.isDigit() || it == '.' }
                                    fieldsState = fieldsState.toMutableMap().apply {
                                        put(field.key, filtered)
                                    }
                                },
                                label = { Text(labelValue) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("measurement_input_${field.key}"),
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true,
                                suffix = { Text("\"", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
                                colors = fieldColors
                            )
                        }
                        if (rowFields.size < 2) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                // Measurement notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(Localization.get("measurement_notes", lang)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .testTag("measurement_input_notes"),
                    minLines = 3,
                    shape = RoundedCornerShape(16.dp),
                    colors = fieldColors
                )

                Spacer(modifier = Modifier.height(100.dp)) // Cushion for FAB
            }
        }
    }
}
