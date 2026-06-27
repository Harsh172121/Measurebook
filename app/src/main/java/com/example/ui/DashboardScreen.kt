package com.example.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.data.Customer
import com.example.util.AvatarUtil
import com.example.util.DateUtil
import com.example.util.Localization

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: TailorViewModel,
    onNavigateToCustomerList: () -> Unit,
    onNavigateToCustomerAdd: () -> Unit,
    onNavigateToCustomerDetail: (String) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val lang by viewModel.language.collectAsState()
    val shopNameVal by viewModel.shopName.collectAsState()

    val totalCustomers by viewModel.totalCustomersCount.collectAsState()
    val totalMeasurements by viewModel.totalMeasurementsCount.collectAsState()
    val recentCustomersCount by viewModel.recentCustomersCount.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val recentCustomersList by viewModel.recentCustomers.collectAsState()

    var showSplash by remember { mutableStateOf(!viewModel.hasShownSplash) }

    LaunchedEffect(Unit) {
        if (!viewModel.hasShownSplash) {
            kotlinx.coroutines.delay(1200)
            showSplash = false
            viewModel.hasShownSplash = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Sleek Brand Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = com.example.R.drawable.img_app_logo),
                            contentDescription = "Kalanidhan Logo",
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape)
                                .background(Color.White)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Kalanidhan Ladies Tailor",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    // Settings Icon Button
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            .testTag("dashboard_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // 2. Search Box
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        viewModel.updateSearchQuery(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dashboard_search_bar"),
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
                            IconButton(onClick = {
                                viewModel.updateSearchQuery("")
                            }) {
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
            }

            // 3. Search Results Overlay
            if (searchQuery.isNotBlank()) {
                if (searchResults.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            border = BoxBorder(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Text(
                                text = "No matching customers found.",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(searchResults) { customer ->
                        CustomerSearchItemSleek(
                            customer = customer,
                            onClick = {
                                viewModel.updateSearchQuery("")
                                onNavigateToCustomerDetail(customer.id)
                            }
                        )
                    }
                }
            }

            // Show normal dashboard only if search is blank
            if (searchQuery.isBlank()) {
                // 4. Custom Sleek Stats Grid
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatsCardSleek(
                            upperText = "Total",
                            value = totalCustomers.toString(),
                            bottomText = "Customers",
                            modifier = Modifier.weight(1f)
                        )
                        StatsCardSleek(
                            upperText = "Measures",
                            value = totalMeasurements.toString(),
                            bottomText = "Records",
                            modifier = Modifier.weight(1f)
                        )
                        StatsCardSleek(
                            upperText = "Recent",
                            value = recentCustomersCount.toString(),
                            bottomText = "Last 30 Days",
                            isGradient = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 5. Actions Row
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onNavigateToCustomerAdd,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("dashboard_add_customer_btn"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = Localization.get("add_customer", lang),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Button(
                            onClick = onNavigateToCustomerList,
                            modifier = Modifier
                                .height(52.dp)
                                .testTag("dashboard_view_all_btn"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = BoxBorder(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Icon(Icons.Default.FormatListBulleted, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // 6. Recent Customers List Title
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Localization.get("recent_customers", lang),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        TextButton(onClick = onNavigateToCustomerList) {
                            Text(
                                text = "View All",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                if (recentCustomersList.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            border = BoxBorder(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.PersonOutline,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.outline
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = Localization.get("no_recent_customers", lang),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    items(recentCustomersList) { customer ->
                        CustomerCardSleek(
                            viewModel = viewModel,
                            customer = customer,
                            lang = lang,
                            onClick = { onNavigateToCustomerDetail(customer.id) }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Animated Splash Overlay
    AnimatedVisibility(
        visible = showSplash,
        enter = androidx.compose.animation.fadeIn(),
        exit = androidx.compose.animation.fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = com.example.R.drawable.img_app_logo),
                    contentDescription = "Splash Logo",
                    modifier = Modifier
                        .size(180.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape)
                        .background(Color.White)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Kalanidhan".uppercase(),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "LADIES TAILOR",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 2.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
}

@Composable
fun BoxBorder(width: androidx.compose.ui.unit.Dp, color: Color) = 
    androidx.compose.foundation.BorderStroke(width, color)

@Composable
fun StatsCardSleek(
    upperText: String,
    value: String,
    bottomText: String,
    isGradient: Boolean = false,
    modifier: Modifier = Modifier
) {
    val bgModifier = if (isGradient) {
        Modifier.background(
            Brush.linearGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.surface, 
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                )
            )
        )
    } else {
        Modifier.background(MaterialTheme.colorScheme.surface)
    }

    val borderStroke = if (isGradient) {
        BoxBorder(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
    } else {
        BoxBorder(1.dp, MaterialTheme.colorScheme.outline)
    }

    Card(
        modifier = modifier.height(96.dp),
        shape = RoundedCornerShape(16.dp),
        border = borderStroke
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(bgModifier)
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = upperText.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = if (isGradient) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (isGradient) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = bottomText,
                fontSize = 10.sp,
                color = if (isGradient) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun CustomerCardSleek(
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
            .testTag("customer_card_${customer.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BoxBorder(1.dp, MaterialTheme.colorScheme.outline)
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
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = customer.customerId,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                if (customer.mobile.isNotBlank()) {
                    Text(
                        text = customer.mobile,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Show smart category badges with Gujarati or English labels based on app locale!
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

@Composable
fun CustomerSearchItemSleek(
    customer: Customer,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = BoxBorder(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val avatarColor = AvatarUtil.getColorForName(customer.name)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(avatarColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = AvatarUtil.getInitials(customer.name),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = customer.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (customer.mobile.isNotBlank()) "${customer.customerId} • ${customer.mobile}" else customer.customerId,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "›",
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
