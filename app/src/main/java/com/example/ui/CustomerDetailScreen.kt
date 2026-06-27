package com.example.ui

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.Customer
import com.example.data.Measurement
import com.example.util.AvatarUtil
import com.example.util.DateUtil
import com.example.util.Localization
import com.example.util.MeasurementFields
import com.example.util.PdfGenerator
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(
    viewModel: TailorViewModel,
    customerId: String,
    onNavigateToEditCustomer: (String) -> Unit,
    onNavigateToAddMeasurement: (String) -> Unit,
    onNavigateToEditMeasurement: (String, String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val lang by viewModel.language.collectAsState()
    val shopNameVal by viewModel.shopName.collectAsState()
    val shopPhoneVal by viewModel.shopPhone.collectAsState()
    val shopAddressVal by viewModel.shopAddress.collectAsState()

    val customerState = viewModel.getCustomerById(customerId).collectAsState(initial = null)
    val customer = customerState.value

    val measurementsListState = viewModel.getMeasurementsForCustomer(customerId).collectAsState(initial = emptyList())
    val measurementsList = measurementsListState.value

    var showDeleteCustomerDialog by remember { mutableStateOf(false) }
    var measurementToDelete by remember { mutableStateOf<Measurement?>(null) }

    if (customer == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
        return
    }

    // Dialog: Delete Customer Confirmation
    if (showDeleteCustomerDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteCustomerDialog = false },
            title = { Text(Localization.get("delete_customer_title", lang), color = MaterialTheme.colorScheme.onSurface) },
            text = { Text(Localization.get("delete_customer_message", lang), color = MaterialTheme.colorScheme.onSurfaceVariant) },
            containerColor = MaterialTheme.colorScheme.surface,
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteCustomerDialog = false
                        viewModel.deleteCustomer(customer) {
                            onNavigateBack()
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(Localization.get("delete", lang), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteCustomerDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Text(Localization.get("cancel", lang))
                }
            }
        )
    }

    // Dialog: Delete Measurement Confirmation
    if (measurementToDelete != null) {
        AlertDialog(
            onDismissRequest = { measurementToDelete = null },
            title = { Text(Localization.get("delete_measurement_title", lang), color = MaterialTheme.colorScheme.onSurface) },
            text = { Text(Localization.get("delete_measurement_message", lang), color = MaterialTheme.colorScheme.onSurfaceVariant) },
            containerColor = MaterialTheme.colorScheme.surface,
            confirmButton = {
                TextButton(
                    onClick = {
                        val toDelete = measurementToDelete!!
                        measurementToDelete = null
                        viewModel.deleteMeasurement(toDelete) {
                            Toast.makeText(context, "Measurement deleted", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(Localization.get("delete", lang), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { measurementToDelete = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Text(Localization.get("cancel", lang))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(customer.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onNavigateToEditCustomer(customer.id) },
                        modifier = Modifier.testTag("detail_edit_customer_btn")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = Localization.get("edit", lang), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(
                        onClick = { showDeleteCustomerDialog = true },
                        modifier = Modifier.testTag("detail_delete_customer_btn")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = Localization.get("delete", lang), tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToAddMeasurement(customer.id) },
                modifier = Modifier.testTag("detail_add_measurement_fab"),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Straighten, contentDescription = Localization.get("add_measurement", lang))
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Card Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val avatarColor = AvatarUtil.getColorForName(customer.name)
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(avatarColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = AvatarUtil.getInitials(customer.name),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = customer.name,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${Localization.get("customer_id", lang)}: ${customer.customerId}",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(12.dp))

                        if (customer.mobile.isNotBlank()) {
                            ContactDetailRowSleek(
                                icon = Icons.Default.Phone,
                                label = Localization.get("mobile", lang),
                                value = customer.mobile
                            )
                        }

                        if (!customer.alternateMobile.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            ContactDetailRowSleek(
                                icon = Icons.Default.PhoneAndroid,
                                label = Localization.get("alt_mobile", lang),
                                value = customer.alternateMobile
                            )
                        }

                        if (!customer.address.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            ContactDetailRowSleek(
                                icon = Icons.Default.Home,
                                label = Localization.get("address", lang),
                                value = customer.address
                            )
                        }

                        if (!customer.notes.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            ContactDetailRowSleek(
                                icon = Icons.Default.Note,
                                label = Localization.get("notes", lang),
                                value = customer.notes
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        ContactDetailRowSleek(
                            icon = Icons.Default.CalendarToday,
                            label = Localization.get("joined", lang),
                            value = DateUtil.formatIsoToDisplay(customer.createdAt)
                        )
                    }
                }
            }

            // Export Actions
            item {
                Button(
                    onClick = {
                        val pdfFile = PdfGenerator.generateMeasurementPdf(
                            context = context,
                            shopName = shopNameVal,
                            shopPhone = shopPhoneVal,
                            shopAddress = shopAddressVal,
                            customer = customer,
                            measurements = measurementsList,
                            language = lang
                        )
                        if (pdfFile != null) {
                            sharePdf(context, pdfFile)
                        } else {
                            Toast.makeText(context, "Failed to generate PDF.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("detail_export_pdf_btn"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = Localization.get("export_pdf", lang), 
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Measurement History Title
            item {
                Text(
                    text = Localization.get("measurement_history", lang),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (measurementsList.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Straighten,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.outline
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = Localization.get("no_measurements", lang),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            } else {
                items(measurementsList) { measurement ->
                    MeasurementHistoryCardSleek(
                        measurement = measurement,
                        lang = lang,
                        onEdit = { onNavigateToEditMeasurement(customer.id, measurement.id) },
                        onDelete = { measurementToDelete = measurement }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp)) // Cushion for FAB
            }
        }
    }
}

@Composable
fun ContactDetailRowSleek(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp).padding(top = 2.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            )
            Text(
                text = value,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MeasurementHistoryCardSleek(
    measurement: Measurement,
    lang: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("measurement_history_card_${measurement.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isBlouse = measurement.category == "blouse"
                val categoryText = if (isBlouse) {
                    Localization.get("category_blouse", lang)
                } else {
                    val sub = if (measurement.subCategory == "top") {
                        Localization.get("punjabi_top", lang)
                    } else {
                        Localization.get("punjabi_bottom", lang)
                    }
                    sub
                }
                
                Text(
                    text = categoryText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (isBlouse) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(8.dp))
                
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp).testTag("edit_measurement_${measurement.id}")) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp).testTag("delete_measurement_${measurement.id}")) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Text(
                text = DateUtil.formatIsoToDisplay(measurement.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Display grid of key-value pairs in a sleek chip design
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isBlouse = measurement.category == "blouse"
                val (chipBg, chipText) = if (isBlouse) {
                    Pair(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), MaterialTheme.colorScheme.primary)
                } else {
                    Pair(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f), MaterialTheme.colorScheme.secondary)
                }

                measurement.fields.forEach { (key, value) ->
                    val label = MeasurementFields.getFieldLabel(key, lang)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(chipBg)
                            .border(1.dp, chipText.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$label: $value\"",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = chipText
                        )
                    }
                }
            }

            if (!measurement.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Notes,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp).padding(top = 2.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = measurement.notes,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun sharePdf(context: Context, file: File) {
    val authority = "${context.packageName}.fileprovider"
    try {
        val uri = FileProvider.getUriForFile(context, authority, file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Measurement PDF"))
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error sharing PDF: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
