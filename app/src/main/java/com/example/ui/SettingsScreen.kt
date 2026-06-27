package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.Localization

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: TailorViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val lang by viewModel.language.collectAsState()
    val theme by viewModel.theme.collectAsState()
    
    val shopNameVal by viewModel.shopName.collectAsState()
    val ownerNameVal by viewModel.ownerName.collectAsState()
    val shopPhoneVal by viewModel.shopPhone.collectAsState()
    val shopAddressVal by viewModel.shopAddress.collectAsState()

    var shopNameInput by remember { mutableStateOf(shopNameVal) }
    var ownerNameInput by remember { mutableStateOf(ownerNameVal) }
    var shopPhoneInput by remember { mutableStateOf(shopPhoneVal) }
    var shopAddressInput by remember { mutableStateOf(shopAddressVal) }

    var importJsonInput by remember { mutableStateOf("") }
    var showExportDialog by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(shopNameVal, ownerNameVal, shopPhoneVal, shopAddressVal) {
        shopNameInput = shopNameVal
        ownerNameInput = ownerNameVal
        shopPhoneInput = shopPhoneVal
        shopAddressInput = shopAddressVal
    }

    if (showExportDialog != null) {
        AlertDialog(
            onDismissRequest = { showExportDialog = null },
            title = { Text(Localization.get("export_backup", lang), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Your backup JSON is ready. Copy it to your clipboard or share it below.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = showExportDialog!!,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        textStyle = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("MeasureBook Backup", showExportDialog!!)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Copied to Clipboard", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                    TextButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, showExportDialog!!)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share JSON Backup"))
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = null }) {
                    Text("Dismiss", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Localization.get("settings", lang), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) },
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
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
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

            // 0. Brand Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = com.example.R.drawable.img_app_logo),
                        contentDescription = "Kalanidhan Ladies Tailor Logo",
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape)
                            .background(Color.White)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Kalanidhan".uppercase(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "LADIES TAILOR",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 2.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 1. Language Toggle
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = Localization.get("language", lang),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.saveLanguage("en") },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (lang == "en") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background,
                                contentColor = if (lang == "en") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            border = if (lang != "en") androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null,
                            modifier = Modifier.weight(1f).testTag("lang_en_chip")
                        ) {
                            Text("English", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Button(
                            onClick = { viewModel.saveLanguage("gu") },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (lang == "gu") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background,
                                contentColor = if (lang == "gu") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            border = if (lang != "gu") androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null,
                            modifier = Modifier.weight(1f).testTag("lang_gu_chip")
                        ) {
                            Text("ગુજરાતી", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            // 1.5 Theme Selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = Localization.get("app_theme", lang),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val themeOptions = listOf("system", "light", "dark")
                        themeOptions.forEach { option ->
                            val isSelected = theme == option
                            val label = when (option) {
                                "system" -> Localization.get("theme_system", lang)
                                "light" -> Localization.get("theme_light", lang)
                                "dark" -> Localization.get("theme_dark", lang)
                                else -> option
                            }
                            Button(
                                onClick = { viewModel.saveTheme(option) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background,
                                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null,
                                modifier = Modifier.weight(1f).testTag("theme_${option}_chip"),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Text(label, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
                            }
                        }
                    }
                }
            }

            // 2. Shop Information
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = Localization.get("shop_info", lang),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = shopNameInput,
                        onValueChange = { shopNameInput = it },
                        modifier = Modifier.fillMaxWidth().testTag("settings_shop_name"),
                        label = { Text(Localization.get("shop_name", lang)) },
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = fieldColors
                    )

                    OutlinedTextField(
                        value = ownerNameInput,
                        onValueChange = { ownerNameInput = it },
                        modifier = Modifier.fillMaxWidth().testTag("settings_owner_name"),
                        label = { Text(Localization.get("owner_name", lang)) },
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = fieldColors
                    )

                    OutlinedTextField(
                        value = shopPhoneInput,
                        onValueChange = { shopPhoneInput = it },
                        modifier = Modifier.fillMaxWidth().testTag("settings_phone"),
                        label = { Text(Localization.get("mobile", lang)) },
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = fieldColors
                    )

                    OutlinedTextField(
                        value = shopAddressInput,
                        onValueChange = { shopAddressInput = it },
                        modifier = Modifier.fillMaxWidth().testTag("settings_address"),
                        label = { Text(Localization.get("address", lang)) },
                        minLines = 2,
                        shape = RoundedCornerShape(16.dp),
                        colors = fieldColors
                    )

                    Button(
                        onClick = {
                            viewModel.saveShopInfo(
                                name = shopNameInput,
                                owner = ownerNameInput,
                                phone = shopPhoneInput,
                                address = shopAddressInput
                            )
                            Toast.makeText(context, Localization.get("save_shop_success", lang), Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("settings_save_shop_btn"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(Localization.get("save", lang), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }

            // 3. Backup and Restore
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = Localization.get("backup_restore", lang),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = Localization.get("backup_desc", lang),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = {
                            viewModel.exportBackup { json ->
                                if (json != null) {
                                    showExportDialog = json
                                } else {
                                    Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("settings_export_backup_btn"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text("Export Backup JSON", fontWeight = FontWeight.Bold)
                    }

                    Divider(color = MaterialTheme.colorScheme.outline)

                    Text(
                        text = "Paste Backup JSON to Import",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = importJsonInput,
                        onValueChange = { importJsonInput = it },
                        modifier = Modifier.fillMaxWidth().height(100.dp).testTag("settings_import_text"),
                        placeholder = { Text("Paste JSON here...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                        shape = RoundedCornerShape(16.dp),
                        textStyle = MaterialTheme.typography.bodySmall,
                        colors = fieldColors
                    )

                    Button(
                        onClick = {
                            if (importJsonInput.isBlank()) {
                                Toast.makeText(context, "Please paste valid JSON backup text first.", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.importBackup(importJsonInput) { success ->
                                    if (success) {
                                        importJsonInput = ""
                                        Toast.makeText(context, "Backup restored successfully!", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "Invalid JSON backup data.", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("settings_import_backup_btn"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(Localization.get("import_backup", lang), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
