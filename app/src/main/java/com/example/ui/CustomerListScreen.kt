package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Customer
import com.example.util.AvatarUtil
import com.example.util.DateUtil
import com.example.util.Localization

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerListScreen(
    viewModel: TailorViewModel,
    onNavigateToCustomerDetail: (String) -> Unit,
    onNavigateToCustomerAdd: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val lang by viewModel.language.collectAsState()
    val allCustomersList by viewModel.allCustomers.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredCustomers = remember(allCustomersList, searchQuery) {
        if (searchQuery.isBlank()) {
            allCustomersList
        } else {
            allCustomersList.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.mobile.contains(searchQuery, ignoreCase = true) ||
                it.customerId.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    val columnsCount = if (isTablet) 2 else 1

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Localization.get("customers", lang), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) },
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
                onClick = onNavigateToCustomerAdd,
                modifier = Modifier.testTag("customer_list_fab"),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = Localization.get("add_customer", lang))
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .testTag("customer_list_search"),
                placeholder = { 
                    Text(
                        Localization.get("search_hint", lang),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        fontSize = 14.sp
                    ) 
                },
                leadingIcon = { 
                    Text(
                        text = "🔍", 
                        fontSize = 16.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    ) 
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )

            if (filteredCustomers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PeopleOutline,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No customers found.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columnsCount),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredCustomers) { customer ->
                        CustomerCardInListSleek(
                            viewModel = viewModel,
                            customer = customer,
                            lang = lang,
                            onClick = { onNavigateToCustomerDetail(customer.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerCardInListSleek(
    viewModel: TailorViewModel,
    customer: Customer,
    lang: String,
    onClick: () -> Unit
) {
    // Reactively load categories based on customer's measurements
    val measurementsState = viewModel.getMeasurementsForCustomer(customer.id).collectAsState(initial = emptyList())
    val measurements = measurementsState.value
    
    val categories = remember(measurements) {
        measurements.map { it.category }.distinct()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("customer_card_list_${customer.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val avatarColor = AvatarUtil.getColorForName(customer.name)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(avatarColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = AvatarUtil.getInitials(customer.name),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = customer.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (!customer.notes.isNullOrBlank()) {
                        Icon(
                            imageVector = Icons.Default.Note,
                            contentDescription = "Has notes",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${Localization.get("customer_id", lang)}: ${customer.customerId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                if (customer.mobile.isNotBlank()) {
                    Text(
                        text = "${Localization.get("mobile", lang)}: ${customer.mobile}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!customer.address.isNullOrBlank()) {
                    Text(
                        text = customer.address,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                
                // Categories Badges
                if (categories.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        categories.forEach { category ->
                            val (badgeBg, badgeText, label) = when (category) {
                                "blouse" -> Triple(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), 
                                    MaterialTheme.colorScheme.primary,
                                    Localization.get("category_blouse", lang)
                                )
                                else -> Triple(
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f), 
                                    MaterialTheme.colorScheme.secondary,
                                    Localization.get("category_punjabi", lang)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(badgeBg)
                                    .border(1.dp, badgeText.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = badgeText
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${Localization.get("last_updated", lang)}: ${DateUtil.formatIsoToDisplay(customer.updatedAt).split(",").firstOrNull() ?: ""}",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "›",
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
    }
}
