package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.ui.theme.ExpressiveButtonShape
import com.example.ui.theme.ExpressiveCardShape
import com.example.ui.theme.ExpressiveChipShape
import com.example.ui.theme.ExpressiveDialogShape
import com.example.ui.theme.ExpressiveMenuShape
import com.example.ui.theme.ExpressivePillButtonShape
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Palette
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Upload
import com.example.receiver.SmsNotificationHelper
import com.example.receiver.ParsedSmsTransaction
import com.example.widget.WidgetUpdater
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.graphics.Brush
import com.example.ui.components.AtomicBorderStyle
import com.example.ui.components.AtomicWidget
import com.example.ui.components.AtomicWidgetSize
import com.example.ui.theme.SFMonoFontFamily
import kotlin.math.roundToInt
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.filled.BorderOuter
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.ColorUtils
import com.example.R
import com.example.ui.theme.ExpenseRed
import com.example.widget.WidgetWallpaperTintManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DriveScreen(
    driveAccount: String,
    lastDriveSyncTime: Long,
    isDriveSyncing: Boolean,
    driveAutoSync: Boolean,
    onSyncNow: () -> Unit,
    onToggleAutoSync: (Boolean) -> Unit,
    onExportBackup: ((String) -> Unit) -> Unit,
    onRestoreBackup: (String, (Boolean, Int) -> Unit) -> Unit,
    onClearAllData: (() -> Unit) -> Unit,
    onLoadSampleData: (() -> Unit) -> Unit,
    currentTheme: String = "Emerald",
    onSelectTheme: (String) -> Unit = {},
    wallpaperTintEnabled: Boolean? = null,
    applyWallpaperTintToApp: Boolean? = null,
    applyWallpaperTintToWidgets: Boolean? = null,
    onToggleWallpaperTint: ((Boolean) -> Unit)? = null,
    onToggleApplyWallpaperTintToApp: ((Boolean) -> Unit)? = null,
    onToggleApplyWallpaperTintToWidgets: ((Boolean) -> Unit)? = null,
    onRestoreFromGoogleDrive: ((Boolean, Int, String) -> Unit) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val syncDateFormat = remember { SimpleDateFormat("dd MMM yyyy, h:mm a", Locale.US) }

    var showExportDialog by remember { mutableStateOf(false) }
    var exportJsonContent by remember { mutableStateOf("") }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var restoreJsonInput by remember { mutableStateOf("") }
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var themesExpanded by rememberSaveable { mutableStateOf(false) }

    val saveFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(exportJsonContent.toByteArray(Charsets.UTF_8))
                    stream.flush()
                }
                Toast.makeText(context, "Backup successfully saved to file!", Toast.LENGTH_LONG).show()
                showExportDialog = false
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to save file: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val openJsonFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                val jsonString = context.contentResolver.openInputStream(uri)?.use { stream ->
                    stream.bufferedReader(Charsets.UTF_8).readText()
                } ?: ""
                if (jsonString.isNotBlank()) {
                    onRestoreBackup(jsonString.trim()) { success, count ->
                        if (success) {
                            Toast.makeText(context, "Successfully restored $count records from JSON file!", Toast.LENGTH_LONG).show()
                            showRestoreDialog = false
                        } else {
                            Toast.makeText(context, "Failed to restore backup: Invalid JSON", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "Selected backup file is empty", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error reading JSON backup file: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val prefs = remember { context.getSharedPreferences("money_manager_prefs", Context.MODE_PRIVATE) }
    var smsDetectionEnabled by remember { mutableStateOf(prefs.getBoolean("sms_auto_detect_enabled", true)) }
    var showWidgetNames by remember { mutableStateOf(prefs.getBoolean(WidgetUpdater.PREF_SHOW_WIDGET_NAMES, true)) }
    var localWallpaperTint by remember {
        mutableStateOf(WidgetWallpaperTintManager.isWallpaperTintEnabled(context))
    }
    var localApplyToApp by remember {
        mutableStateOf(WidgetWallpaperTintManager.isApplyToAppEnabled(context))
    }
    var localApplyToWidgets by remember {
        mutableStateOf(WidgetWallpaperTintManager.isApplyToWidgetsEnabled(context))
    }
    val effectiveWallpaperTintMaster = wallpaperTintEnabled ?: localWallpaperTint
    val effectiveApplyToApp = applyWallpaperTintToApp ?: localApplyToApp
    val effectiveApplyToWidgets = applyWallpaperTintToWidgets ?: localApplyToWidgets
    val widgetWallpaperTint = effectiveWallpaperTintMaster && effectiveApplyToWidgets

    var wallpaperPaletteTick by remember { mutableStateOf(0) }
    DisposableEffect(context) {
        val unregister = WidgetWallpaperTintManager.registerWallpaperListener(context) {
            wallpaperPaletteTick++
        }
        onDispose { unregister() }
    }
    val isSystemDark = isSystemInDarkTheme()
    val dynamicWidgetPalette = remember(wallpaperPaletteTick, isSystemDark) {
        WidgetWallpaperTintManager.getDynamicPalette(context, isDark = isSystemDark)
    }
    var widgetInnerPaddingDp by remember {
        mutableStateOf(
            prefs.getInt(WidgetUpdater.PREF_WIDGET_INNER_PADDING_DP, WidgetUpdater.DEFAULT_WIDGET_INNER_PADDING_DP)
        )
    }
    var widgetBorderEnabled by remember {
        mutableStateOf(prefs.getBoolean(WidgetUpdater.PREF_WIDGET_BORDER_ENABLED, true))
    }
    var widgetTransparentBg by remember {
        mutableStateOf(WidgetUpdater.isWidgetTransparentBg(context))
    }
    var widgetBorderStyle by remember {
        val saved = AtomicBorderStyle.fromKey(prefs.getString(WidgetUpdater.PREF_WIDGET_BORDER_STYLE, "hairline"))
        mutableStateOf(if (saved == AtomicBorderStyle.None) AtomicBorderStyle.Hairline else saved)
    }

    val smsPermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val smsGranted = permissions[android.Manifest.permission.RECEIVE_SMS] == true
        val notifGranted = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissions[android.Manifest.permission.POST_NOTIFICATIONS] == true
        } else true

        if (smsGranted && notifGranted) {
            Toast.makeText(context, "SMS Transaction Detection enabled!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Permissions required to detect SMS transactions", Toast.LENGTH_SHORT).show()
        }
    }

    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val safeDrawingTop = WindowInsets.safeDrawing.asPaddingValues().calculateTopPadding()
    val effectiveTopPadding = maxOf(statusBarTop, safeDrawingTop, 36.dp)

    Column(modifier = modifier.fillMaxSize().testTag("drive_screen")) {
        // Screen Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = effectiveTopPadding, bottom = 10.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Google Drive cloud sync, themes, SMS alerts & backup",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CloudDone,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 190.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Google Drive Cloud Sync Card with Liquid Glass
            item(key = "section_drive_card") {
                com.example.ui.components.LiquidGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("google_drive_sync_card"),
                    shape = RoundedCornerShape(22.dp),
                    tint = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    blurRadius = 20.dp,
                    lensCurvature = 0.42f,
                    specularHighlight = 0.80f,
                    chromaticDispersion = 0.28f,
                    elevation = 8.dp
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.CloudDone,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Google Drive Sync",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = driveAccount,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (isDriveSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.5.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Sync Status Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Last Synced Time",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = if (lastDriveSyncTime > 0) syncDateFormat.format(Date(lastDriveSyncTime)) else "Tap Sync below",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (lastDriveSyncTime > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Text(
                                    text = "Status: Online",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Auto-sync switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Auto-Sync on Open & Close",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Saves your changes automatically across sessions",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = driveAutoSync,
                                onCheckedChange = { onToggleAutoSync(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Sync and Restore from Google Drive Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    onSyncNow()
                                    Toast.makeText(context, "Google Drive backup updated successfully!", Toast.LENGTH_SHORT).show()
                                },
                                enabled = !isDriveSyncing,
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("btn_sync_now"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    if (isDriveSyncing) "Syncing..." else "Sync to Drive",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    onRestoreFromGoogleDrive { success, count, msg ->
                                        if (success) {
                                            Toast.makeText(context, "Successfully restored $count records from Google Drive!", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, msg.ifBlank { "No cloud backup found on Google Drive yet. Please tap 'Sync to Drive' on your primary device first." }, Toast.LENGTH_LONG).show()
                                        }
                                    }
                                },
                                enabled = !isDriveSyncing,
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("btn_restore_from_drive"),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Restore Drive",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // Themes Section Accordion
            item(key = "section_material_themes") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("material_themes_card"),
                    shape = ExpressiveCardShape,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(ExpressiveButtonShape)
                                .clickable { themesExpanded = !themesExpanded }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Palette,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Themes",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Active: $currentTheme • 5 Material 3 Styles",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            IconButton(
                                onClick = { themesExpanded = !themesExpanded },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (themesExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (themesExpanded) "Collapse Themes" else "Expand Themes",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        AnimatedVisibility(visible = themesExpanded) {
                            Column {
                                Spacer(modifier = Modifier.height(14.dp))

                                val themeOptions = listOf(
                                    Triple("Emerald", "Emerald Green", Color(0xFF10B981) to "Finance Classic • Vibrant Mint"),
                                    Triple("Ocean", "Sapphire Ocean", Color(0xFF3B82F6) to "Deep Blue & Cyan Accent"),
                                    Triple("Amethyst", "Royal Amethyst", Color(0xFF8B5CF6) to "Purple & Violet Elegance"),
                                    Triple("Sunset", "Sunset Amber", Color(0xFFF97316) to "Warm Gold & Sunset Dusk"),
                                    Triple("Rose", "Crimson Rose", Color(0xFFF43F5E) to "Bold Pink & Crimson Radiance")
                                )

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    themeOptions.forEach { (key, name, pair) ->
                                        val (color, desc) = pair
                                        val isSelected = currentTheme.equals(key, ignoreCase = true)
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(ExpressiveChipShape)
                                                .clickable {
                                                    onSelectTheme(key)
                                                    Toast.makeText(context, "Theme set to $name", Toast.LENGTH_SHORT).show()
                                                }
                                                .testTag("theme_option_$key"),
                                            color = if (isSelected) color.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surface,
                                            shape = ExpressiveChipShape,
                                            tonalElevation = if (isSelected) 3.dp else 0.dp
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(28.dp)
                                                            .clip(CircleShape)
                                                            .background(color),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        if (isSelected) {
                                                            Icon(
                                                                Icons.Default.Check,
                                                                contentDescription = null,
                                                                tint = Color.White,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Column {
                                                        Text(
                                                            text = name,
                                                            fontSize = 13.sp,
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                                            color = if (isSelected) color else MaterialTheme.colorScheme.onSurface
                                                        )
                                                        Text(
                                                            text = desc,
                                                            fontSize = 10.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }

                                                if (isSelected) {
                                                    Surface(
                                                        shape = ExpressiveChipShape,
                                                        color = color.copy(alpha = 0.25f)
                                                    ) {
                                                        Text(
                                                            text = "Active",
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = color,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // SMS Transaction Alerts Section
            item(key = "section_sms_transactions") {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("sms_detection_card"),
                    shape = ExpressiveCardShape,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.ReceiptLong,
                                            contentDescription = "SMS Detection",
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "SMS Transaction Alerts",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Detect bank debits & credits automatically",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = smsDetectionEnabled,
                                onCheckedChange = { isEnabled ->
                                    smsDetectionEnabled = isEnabled
                                    prefs.edit().putBoolean("sms_auto_detect_enabled", isEnabled).apply()
                                    if (isEnabled) {
                                        val perms = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                            arrayOf(
                                                android.Manifest.permission.RECEIVE_SMS,
                                                android.Manifest.permission.POST_NOTIFICATIONS
                                            )
                                        } else {
                                            arrayOf(android.Manifest.permission.RECEIVE_SMS)
                                        }
                                        smsPermissionsLauncher.launch(perms)
                                    }
                                },
                                modifier = Modifier.testTag("sms_detection_switch"),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "When an incoming SMS from your bank confirms a debit or credit, the app analyzes the amount, merchant, and payment mode locally and sends an instant push notification so you can record it in one tap. OTPs and non-financial messages are strictly ignored.",
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val perms = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                        arrayOf(
                                            android.Manifest.permission.RECEIVE_SMS,
                                            android.Manifest.permission.POST_NOTIFICATIONS
                                        )
                                    } else {
                                        arrayOf(android.Manifest.permission.RECEIVE_SMS)
                                    }
                                    smsPermissionsLauncher.launch(perms)
                                },
                                shape = ExpressivePillButtonShape,
                                modifier = Modifier.fillMaxWidth().testTag("btn_request_sms_perms")
                            ) {
                                Text("Manage SMS Permissions", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Home Screen Widget Preferences & OriginOS Atomic Preview
            item(key = "section_widget_preferences") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("widget_preferences_card"),
                    shape = ExpressiveCardShape,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Dashboard,
                                            contentDescription = "Widget Settings",
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Show Widget Names",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "OriginOS 24dp footer band (keeps exact card height when hidden)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = showWidgetNames,
                                onCheckedChange = { enabled ->
                                    showWidgetNames = enabled
                                    prefs.edit().putBoolean(WidgetUpdater.PREF_SHOW_WIDGET_NAMES, enabled).apply()
                                    WidgetUpdater.updateAllWidgets(context)
                                    Toast.makeText(
                                        context,
                                        if (enabled) "Widget names enabled" else "Widget names hidden",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                modifier = Modifier.testTag("widget_names_switch"),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                        Spacer(modifier = Modifier.height(14.dp))

                        // Dynamic Wallpaper Tinting Toggle (Master + Individual App & Widgets Toggles)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (effectiveWallpaperTintMaster) {
                                        Color(dynamicWidgetPalette.primaryPillBg).copy(alpha = 0.22f)
                                    } else {
                                        MaterialTheme.colorScheme.secondaryContainer
                                    },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Wallpaper,
                                            contentDescription = stringResource(R.string.widget_wallpaper_tint_title),
                                            tint = if (effectiveWallpaperTintMaster) {
                                                Color(dynamicWidgetPalette.accentText)
                                            } else {
                                                MaterialTheme.colorScheme.onSecondaryContainer
                                            },
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = stringResource(R.string.widget_wallpaper_tint_title),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = stringResource(R.string.widget_wallpaper_tint_subtitle),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = effectiveWallpaperTintMaster,
                                onCheckedChange = { enabled ->
                                    localWallpaperTint = enabled
                                    if (onToggleWallpaperTint != null) {
                                        onToggleWallpaperTint(enabled)
                                    } else {
                                        WidgetWallpaperTintManager.setWallpaperTintEnabled(context, enabled)
                                        WidgetUpdater.updateAllWidgets(context)
                                    }
                                    wallpaperPaletteTick++
                                    Toast.makeText(
                                        context,
                                        context.getString(
                                            if (enabled) R.string.widget_wallpaper_tint_enabled_toast
                                            else R.string.widget_wallpaper_tint_disabled_toast
                                        ),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                modifier = Modifier.testTag("widget_wallpaper_tint_switch"),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        AnimatedVisibility(visible = effectiveWallpaperTintMaster) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.55f))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Live extracted wallpaper swatches
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    dynamicWidgetPalette.swatchColors.take(4).forEach { argb ->
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(CircleShape)
                                                .background(Color(argb))
                                                .border(
                                                    width = 1.dp,
                                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                                    shape = CircleShape
                                                )
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = stringResource(R.string.widget_wallpaper_tint_live_badge),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

                                // Individual Toggle 1: Apply to App
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PhoneAndroid,
                                            contentDescription = stringResource(R.string.wallpaper_tint_apply_app_title),
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = stringResource(R.string.wallpaper_tint_apply_app_title),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = stringResource(R.string.wallpaper_tint_apply_app_subtitle),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Switch(
                                        checked = effectiveApplyToApp,
                                        onCheckedChange = { enabled ->
                                            localApplyToApp = enabled
                                            if (onToggleApplyWallpaperTintToApp != null) {
                                                onToggleApplyWallpaperTintToApp(enabled)
                                            } else {
                                                WidgetWallpaperTintManager.setApplyToAppEnabled(context, enabled)
                                            }
                                            wallpaperPaletteTick++
                                        },
                                        modifier = Modifier.testTag("wallpaper_tint_apply_app_switch"),
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                            checkedTrackColor = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }

                                // Individual Toggle 2: Apply to Widgets
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Widgets,
                                            contentDescription = stringResource(R.string.wallpaper_tint_apply_widgets_title),
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = stringResource(R.string.wallpaper_tint_apply_widgets_title),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = stringResource(R.string.wallpaper_tint_apply_widgets_subtitle),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Switch(
                                        checked = effectiveApplyToWidgets,
                                        onCheckedChange = { enabled ->
                                            localApplyToWidgets = enabled
                                            if (onToggleApplyWallpaperTintToWidgets != null) {
                                                onToggleApplyWallpaperTintToWidgets(enabled)
                                            } else {
                                                WidgetWallpaperTintManager.setApplyToWidgetsEnabled(context, enabled)
                                                WidgetUpdater.updateAllWidgets(context)
                                            }
                                            wallpaperPaletteTick++
                                        },
                                        modifier = Modifier.testTag("wallpaper_tint_apply_widgets_switch"),
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                            checkedTrackColor = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                        Spacer(modifier = Modifier.height(14.dp))

                        // Widget Background Toggle: Solid vs Transparent Glass
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.BlurOn,
                                            contentDescription = stringResource(R.string.widget_background_toggle_title),
                                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = stringResource(R.string.widget_background_toggle_title),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = stringResource(
                                            if (widgetTransparentBg) R.string.widget_background_toggle_subtitle_transparent
                                            else R.string.widget_background_toggle_subtitle_solid
                                        ),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = widgetTransparentBg,
                                onCheckedChange = { transparent ->
                                    widgetTransparentBg = transparent
                                    WidgetUpdater.setWidgetTransparentBg(context, transparent)
                                    WidgetUpdater.updateAllWidgets(context)
                                    Toast.makeText(
                                        context,
                                        context.getString(
                                            if (transparent) R.string.widget_background_transparent_toast
                                            else R.string.widget_background_solid_toast
                                        ),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                modifier = Modifier.testTag("widget_transparent_bg_switch"),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                        Spacer(modifier = Modifier.height(14.dp))

                        // Show Widget Border Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.tertiaryContainer,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.BorderOuter,
                                            contentDescription = stringResource(R.string.widget_border_toggle_title),
                                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = stringResource(R.string.widget_border_toggle_title),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = stringResource(R.string.widget_border_toggle_subtitle),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = widgetBorderEnabled,
                                onCheckedChange = { enabled ->
                                    widgetBorderEnabled = enabled
                                    prefs.edit().putBoolean(WidgetUpdater.PREF_WIDGET_BORDER_ENABLED, enabled).apply()
                                    WidgetUpdater.updateAllWidgets(context)
                                    Toast.makeText(
                                        context,
                                        context.getString(
                                            if (enabled) R.string.widget_border_enabled_toast
                                            else R.string.widget_border_disabled_toast
                                        ),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                modifier = Modifier.testTag("widget_border_switch"),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        // Border Style Selector: Hairline (1dp), Glow (2dp + 4dp halo), Bold (2.2dp)
                        AnimatedVisibility(visible = widgetBorderEnabled) {
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                Text(
                                    text = "Widget Border Style",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    AtomicBorderStyle.visibleBorderStyles.forEach { style ->
                                        FilterChip(
                                            selected = widgetBorderStyle == style,
                                            onClick = {
                                                widgetBorderStyle = style
                                                prefs.edit().putString(WidgetUpdater.PREF_WIDGET_BORDER_STYLE, style.key).apply()
                                                WidgetUpdater.updateAllWidgets(context)
                                            },
                                            label = {
                                                Text(
                                                    text = style.label,
                                                    fontSize = 12.sp,
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("widget_border_chip_${style.key}")
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Adjustable Inner Content Padding (default 14dp matching OriginOS cards)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Widget Inner Padding",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${widgetInnerPaddingDp}dp",
                                style = MaterialTheme.typography.labelLarge,
                                fontFamily = SFMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                        Slider(
                            value = widgetInnerPaddingDp.coerceIn(8, 20).toFloat(),
                            onValueChange = { raw ->
                                widgetInnerPaddingDp = raw.roundToInt().coerceIn(8, 20)
                            },
                            onValueChangeFinished = {
                                prefs.edit()
                                    .putInt(WidgetUpdater.PREF_WIDGET_INNER_PADDING_DP, widgetInnerPaddingDp)
                                    .apply()
                                WidgetUpdater.updateAllWidgets(context)
                            },
                            valueRange = 8f..20f,
                            steps = 11,
                            modifier = Modifier.testTag("widget_padding_slider")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Live OriginOS AtomicWidget Preview reflecting Solid vs Transparent toggle
                        val previewSolidCardBg = if (isSystemDark) Color(0xFF242426) else Color.White
                        val previewGlassCardBg = if (isSystemDark) Color(0x47000000) else Color(0x38000000)
                        val previewActiveCardBg = if (widgetTransparentBg) previewGlassCardBg else previewSolidCardBg
                        val previewIsLightCard = !widgetTransparentBg && !isSystemDark
                        val previewActivePrimaryText = when {
                            widgetTransparentBg && widgetWallpaperTint -> Color(dynamicWidgetPalette.primaryTextOnTransparent)
                            widgetTransparentBg -> Color.White
                            widgetWallpaperTint -> Color(dynamicWidgetPalette.accentText)
                            previewIsLightCard -> Color(0xFF0F172A)
                            else -> Color.White
                        }
                        val previewActiveSecondaryText = when {
                            widgetTransparentBg -> Color(0xFFE2E8F0)
                            previewIsLightCard -> Color(0xFF475569)
                            else -> Color(0xFF94A3B8)
                        }
                        val previewActiveBorderColor = when {
                            widgetTransparentBg && widgetWallpaperTint -> Color(dynamicWidgetPalette.transparentBorder)
                            widgetTransparentBg -> Color(0x4DFFFFFF)
                            widgetWallpaperTint -> Color(
                                if (widgetBorderStyle.key == "bold") {
                                    dynamicWidgetPalette.cardBorderBold
                                } else {
                                    dynamicWidgetPalette.cardBorder
                                }
                            )
                            previewIsLightCard -> Color(0xFFE2E8F0)
                            else -> Color(0xFF3E3F44)
                        }
                        val previewGlowInner = if (widgetWallpaperTint) Color(dynamicWidgetPalette.glowInner) else Color(0x99818CF8)
                        val previewGlowHalo = if (widgetWallpaperTint) Color(dynamicWidgetPalette.glowHalo) else Color(0x33818CF8)
                        val previewPrimaryPillBg = if (widgetWallpaperTint) Color(dynamicWidgetPalette.primaryPillBg) else Color(0xFF6366F1)
                        val previewPrimaryPillText = if (widgetWallpaperTint) Color(dynamicWidgetPalette.primaryPillText) else Color.White
                        val previewSecondaryPillBg = when {
                            widgetTransparentBg && widgetWallpaperTint -> Color(dynamicWidgetPalette.transparentPillBg)
                            widgetTransparentBg -> Color(0x45000000)
                            widgetWallpaperTint -> Color(dynamicWidgetPalette.secondaryPillBg)
                            previewIsLightCard -> Color(0xFFEEF2F6)
                            else -> Color(0xFF303136)
                        }
                        val previewSecondaryPillText = when {
                            widgetTransparentBg && widgetWallpaperTint -> Color(dynamicWidgetPalette.primaryTextOnTransparent)
                            widgetTransparentBg -> Color.White
                            widgetWallpaperTint -> Color(dynamicWidgetPalette.secondaryPillText)
                            previewIsLightCard -> Color(0xFF334155)
                            else -> Color(0xFFCBD5E1)
                        }
                        val previewBackdropColors = if (widgetWallpaperTint && dynamicWidgetPalette.swatchColors.isNotEmpty()) {
                            val s0 = ColorUtils.blendARGB(dynamicWidgetPalette.swatchColors[0], android.graphics.Color.BLACK, 0.55f)
                            val s1 = ColorUtils.blendARGB(
                                dynamicWidgetPalette.swatchColors.getOrElse(1) { dynamicWidgetPalette.primaryPillBg },
                                android.graphics.Color.BLACK,
                                0.50f
                            )
                            val s2 = ColorUtils.blendARGB(
                                dynamicWidgetPalette.swatchColors.getOrElse(2) { dynamicWidgetPalette.accentText },
                                android.graphics.Color.BLACK,
                                0.62f
                            )
                            listOf(Color(s0), Color(s1), Color(s2))
                        } else {
                            listOf(Color(0xFF15192B), Color(0xFF282040), Color(0xFF112A36))
                        }

                        val modeLabel = if (widgetTransparentBg) "Transparent" else "Solid"
                        Text(
                            text = if (widgetWallpaperTint) {
                                "OriginOS Grid Preview ($modeLabel • Wallpaper Tinted)"
                            } else {
                                "OriginOS Grid Preview ($modeLabel 2×2 & 4×2)"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(Brush.linearGradient(colors = previewBackdropColors))
                                .padding(vertical = 14.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .horizontalScroll(rememberScrollState())
                                    .padding(horizontal = 14.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                val effectivePreviewBorderStyle = if (widgetBorderEnabled) {
                                    widgetBorderStyle
                                } else {
                                    AtomicBorderStyle.None
                                }
                                AtomicWidget(
                                    label = "Velocity",
                                    size = AtomicWidgetSize.Square2x2,
                                    showLabel = showWidgetNames,
                                    cornerRadius = 26.dp,
                                    contentPadding = widgetInnerPaddingDp.dp,
                                    borderStyle = effectivePreviewBorderStyle,
                                    containerColor = previewActiveCardBg,
                                    borderColor = previewActiveBorderColor,
                                    glowInnerColor = previewGlowInner,
                                    glowHaloColor = previewGlowHalo,
                                    modifier = Modifier.testTag("preview_atomic_2x2")
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "VELOCITY",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = previewActiveSecondaryText,
                                            maxLines = 1
                                        )
                                        Column {
                                            Text(
                                                text = "Current Burn Rate",
                                                fontSize = 11.5.sp,
                                                color = previewActiveSecondaryText
                                            )
                                            Text(
                                                text = "₹1,250 / day",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = SFMonoFontFamily,
                                                color = previewActivePrimaryText,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(50),
                                                color = previewSecondaryPillBg
                                            ) {
                                                Text(
                                                    text = "10d Left",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = previewSecondaryPillText,
                                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                                )
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(50),
                                                color = previewPrimaryPillBg
                                            ) {
                                                Text(
                                                    text = "Stats →",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = previewPrimaryPillText,
                                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                AtomicWidget(
                                    label = "Monthly Budget",
                                    size = AtomicWidgetSize.Wide4x2,
                                    showLabel = showWidgetNames,
                                    cornerRadius = 26.dp,
                                    contentPadding = widgetInnerPaddingDp.dp,
                                    borderStyle = effectivePreviewBorderStyle,
                                    containerColor = previewActiveCardBg,
                                    borderColor = previewActiveBorderColor,
                                    glowInnerColor = previewGlowInner,
                                    glowHaloColor = previewGlowHalo,
                                    modifier = Modifier.testTag("preview_atomic_4x2")
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (widgetTransparentBg) "MONTHLY BUDGET • GLASS" else "MONTHLY BUDGET • SOLID",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = previewActivePrimaryText
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(50),
                                                color = previewPrimaryPillBg
                                            ) {
                                                Text(
                                                    text = "+ Expense",
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = previewPrimaryPillText,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text("Spent", fontSize = 11.5.sp, color = previewActiveSecondaryText)
                                                Text(
                                                    text = "₹18,450",
                                                    fontSize = 17.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = SFMonoFontFamily,
                                                    color = if (previewIsLightCard) Color(0xFFDC2626) else Color(0xFFF87171),
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text("Limit", fontSize = 11.5.sp, color = previewActiveSecondaryText)
                                                Text(
                                                    text = "₹30,000",
                                                    fontSize = 17.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = SFMonoFontFamily,
                                                    color = previewActivePrimaryText,
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                            }
                                        }
                                        Text(
                                            text = "61% Used • $modeLabel • ${widgetInnerPaddingDp}dp Padding",
                                            fontSize = 11.sp,
                                            color = previewActiveSecondaryText,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Cross-Device Backup & Restore Section
            item(key = "section_backup_restore") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Backup & Multi-Device Transfer",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Export your financial records to transfer or restore on another phone.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Export Button
                            Button(
                                onClick = {
                                    onExportBackup { json ->
                                        exportJsonContent = json
                                        showExportDialog = true
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Export Backup (.json)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            // Restore from File Button - full width, no overflow
                            Button(
                                onClick = {
                                    openJsonFileLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                                },
                                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Restore from JSON File",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Paste JSON Dialog button
                            OutlinedButton(
                                onClick = {
                                    restoreJsonInput = ""
                                    showRestoreDialog = true
                                },
                                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 44.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Paste JSON Backup String",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // Clean Slate & Device Reset Section
            item(key = "section_clean_slate") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Device Storage & Data Management",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Fresh installs now start completely clean without random data. If your second phone has dummy or random transactions, tap 'Clear Ledger' below to wipe it, then tap 'Restore Backup' to import your real transactions.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Clear Ledger Button
                            OutlinedButton(
                                onClick = { showClearConfirmDialog = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ExpenseRed)
                            ) {
                                Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Clear Ledger", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            // Optional Load Sample Data Button
                            OutlinedButton(
                                onClick = {
                                    onLoadSampleData {
                                        Toast.makeText(context, "Sample transactions loaded", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Load Sample", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // Export Dialog (Displays JSON with Copy to Clipboard button)
    if (showExportDialog) {
        Dialog(
            onDismissRequest = { showExportDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                shape = ExpressiveDialogShape,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth(0.92f)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Backup Export Ready", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showExportDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Copy this JSON backup or save it to your Google Drive to restore on another phone.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(12.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = exportJsonContent,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val timestamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
                                saveFileLauncher.launch("MoneyManager_Backup_$timestamp.json")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp),
                            shape = ExpressiveButtonShape,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Save Backup File (.json)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    try {
                                        val sendIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(android.content.Intent.EXTRA_SUBJECT, "Money Manager Backup")
                                            putExtra(android.content.Intent.EXTRA_TEXT, exportJsonContent)
                                            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        val chooser = android.content.Intent.createChooser(sendIntent, "Share Backup").apply {
                                            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        context.startActivity(chooser)
                                    } catch (e: Exception) {
                                        // Safe fallback: copy to clipboard so app never closes/crashes
                                        try {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("MoneyManager_Backup", exportJsonContent)
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, "Backup copied to clipboard", Toast.LENGTH_LONG).show()
                                        } catch (_: Exception) {
                                            Toast.makeText(context, "Could not open share menu", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp),
                                shape = ExpressiveButtonShape
                            ) {
                                Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Share",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("MoneyManager_Backup", exportJsonContent)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Backup copied to clipboard!", Toast.LENGTH_SHORT).show()
                                    showExportDialog = false
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp),
                                shape = ExpressiveButtonShape
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Copy JSON",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Restore Dialog (Paste JSON or load from clipboard)
    if (showRestoreDialog) {
        Dialog(
            onDismissRequest = { showRestoreDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                shape = ExpressiveDialogShape,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth(0.92f)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Restore from Backup", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showRestoreDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Button(
                        onClick = {
                            openJsonFileLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                        },
                        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp),
                        shape = ExpressiveButtonShape,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pick JSON File to Restore",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.HorizontalDivider(modifier = Modifier.weight(1f))
                        Text(
                            text = "  OR PASTE RAW JSON  ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        androidx.compose.material3.HorizontalDivider(modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = restoreJsonInput,
                        onValueChange = { restoreJsonInput = it },
                        placeholder = { Text("Paste JSON backup here...", fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp, max = 160.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clipText = clipboard.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
                            if (clipText.isNotBlank()) {
                                restoreJsonInput = clipText
                                Toast.makeText(context, "Pasted from clipboard", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Text("Paste from Clipboard", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (restoreJsonInput.isNotBlank()) {
                                onRestoreBackup(restoreJsonInput.trim()) { success, count ->
                                    if (success) {
                                        Toast.makeText(context, "Successfully restored $count records!", Toast.LENGTH_LONG).show()
                                        showRestoreDialog = false
                                    } else {
                                        Toast.makeText(context, "Failed to restore backup: Invalid JSON", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp),
                        shape = ExpressiveButtonShape,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = "Import & Restore All Records",
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }

    // Confirm Clear All Data Dialog
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            shape = ExpressiveDialogShape,
            title = { Text("Clear All Transactions?") },
            text = { Text("This will delete all current transactions, recurring items, and debts so you can start clean or restore your real backup.") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllData {
                            Toast.makeText(context, "Ledger cleared successfully", Toast.LENGTH_SHORT).show()
                            showClearConfirmDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
