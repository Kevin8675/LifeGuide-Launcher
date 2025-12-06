package com.example.lifeguide

import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.StateFlow
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.app.AppOpsManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.example.lifeguide.ui.theme.LifeGuideTheme
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.delay

class LauncherActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LifeGuideTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    LauncherHomeScreen()
                }
            }
        }
    }
}

val seniorReminders = listOf(
    "Take your morning medication",
    "Drink a full glass of water",
    "Do some light stretching",
    "Eat a healthy breakfast",
    "Check your blood pressure",
    "Call a family member or friend",
    "Go for a short walk",
    "Read a chapter of a book",
    "Listen to your favorite music",
    "Take your afternoon medication",
    "Have a healthy lunch",
    "Take a short nap if you need one",
    "Do a puzzle or brain game",
    "Stay hydrated, drink water",
    "Check your email or social media",
    "Spend some time in the garden",
    "Prepare a healthy dinner",
    "Take your evening medication",
    "Watch a favorite movie or TV show",
    "Practice deep breathing or meditation",
    "Write in a journal",
    "Plan your meals for tomorrow",
    "Tidy up one room in the house",
    "Water your plants",
    "Check your calendar for appointments",
    "Do some gentle yoga",
    "Listen to a podcast or audiobook",
    "Work on a hobby you enjoy",
    "Look at old photos",
    "Make a to-do list for the next day",
    "Take your vitamins",
    "Do some balance exercises",
    "Have a warm cup of tea",
    "Check the weather for tomorrow",
    "Lay out your clothes for the next day",
    "Make sure all doors and windows are locked",
    "Reflect on something you're grateful for",
    "Do a crossword puzzle",
    "Learn a new word",
    "Watch the sunset or sunrise",
    "Feed the birds",
    "Organize your medication for the week",
    "Pay any outstanding bills",
    "Check in on a neighbor",
    "Try a new healthy recipe",
    "Do some light hand-weight exercises",
    "Schedule a doctor's appointment",
    "Clean out your wallet or purse",
    "Smile at yourself in the mirror",
    "Tell someone you love them"
)

val drugList = listOf(
    "Aspirin", "Ibuprofen", "Paracetamol", "Lisinopril", "Metformin",
    "Simvastatin", "Levothyroxine", "Amoxicillin", "Hydrochlorothiazide", "Gabapentin"
)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun LauncherHomeScreen() {
    val context = LocalContext.current
    val packageManager = context.packageManager
    var searchQuery by remember { mutableStateOf("") }
    var showMedicationReminder by remember { mutableStateOf(false) }
    var currentDrug by remember { mutableStateOf("") }
    val sharedPrefs = context.getSharedPreferences("lifeguide_prefs", Context.MODE_PRIVATE)
    var customWallpaperUri by remember { mutableStateOf(sharedPrefs.getString("wallpaper_uri", null)?.let { Uri.parse(it) }) }

    // Re-read custom wallpaper URI on resume
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                customWallpaperUri = sharedPrefs.getString("wallpaper_uri", null)?.let { Uri.parse(it) }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Medication Reminder Trigger
    LaunchedEffect(Unit) {
        while (true) {
            delay(20000) // Every 20 seconds
            currentDrug = drugList.random()
            showMedicationReminder = true
            delay(5000) // Display for 5 seconds
            showMedicationReminder = false
        }
    }

    // Check for usage stats permission
    val hasUsageStatsPermission = remember {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        mode == AppOpsManager.MODE_ALLOWED
    }

    LaunchedEffect(hasUsageStatsPermission) {
        if (!hasUsageStatsPermission) {
            context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
    }

    // Initialize UsageStatsManager once
    LaunchedEffect(Unit) {
        UsageStatsManager.initialize(context)
    }
    val usageMap by UsageStatsManager.usageMapFlow.collectAsState()

    val wallpaperPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            customWallpaperUri = uri
            // Save the URI to SharedPreferences
            with(sharedPrefs.edit()) {
                putString("wallpaper_uri", uri?.toString())
                apply()
            }
        }
    )

    // Get all installed apps that can be launched
    val allApps = remember(usageMap) {
        val mainIntent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
        val installedApps = packageManager.queryIntentActivities(mainIntent, 0)
            .mapNotNull { resolveInfo ->
                try {
                    val appInfo = resolveInfo.activityInfo.applicationInfo
                    val appName = packageManager.getApplicationLabel(appInfo).toString()
                    val icon = packageManager.getApplicationIcon(appInfo.packageName)
                    AppInfo(appName, appInfo.packageName, icon)
                } catch (e: Exception) {
                    null
                }
            }
            .sortedWith(
                compareByDescending<AppInfo> { usageMap[it.packageName] ?: 0 }
                    .thenBy { it.name.lowercase() }
            )
            .toMutableList()

        // Add settings app
        val settingsApp = AppInfo(
            name = "LifeGuide Settings",
            packageName = "com.example.lifeguide.settings",
            icon = ContextCompat.getDrawable(context, R.drawable.ic_settings)!!
        )
        installedApps.add(0, settingsApp) // Add at the top

        installedApps
    }

    // Filter apps based on search query
    val filteredApps = remember(searchQuery, allApps) {
        if (searchQuery.isBlank()) {
            allApps
        } else {
            val query = searchQuery.lowercase()
            allApps.filter {
                it.name.lowercase().contains(query) ||
                        it.packageName.lowercase().contains(query)
            }
        }
    }

    val systemWallpaperDrawable = remember {
        try {
            WallpaperManager.getInstance(context).peekDrawable()
        } catch (e: Exception) {
            null
        }
    }

    var wallpaperBitmap by remember {
        mutableStateOf(systemWallpaperDrawable?.toBitmap()?.asImageBitmap())
    }

    LaunchedEffect(customWallpaperUri) {
        if (customWallpaperUri != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, customWallpaperUri!!)
                wallpaperBitmap = ImageDecoder.decodeBitmap(source).asImageBitmap()
            }
        } else {
            wallpaperBitmap = systemWallpaperDrawable?.toBitmap()?.asImageBitmap()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Wallpaper background
        if (wallpaperBitmap != null) {
            Image(
                bitmap = wallpaperBitmap!!,
                contentDescription = "Device Wallpaper",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Gray)
            )
        }

        // Content overlay with semi-transparent background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(top = 32.dp),  // Add top padding to move everything down
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Search Bar
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    filteredApps = filteredApps
                )

                if (searchQuery.isBlank()) {
                    // Reminders Widget
                    RemindersWidget()

                    // App Grid (show all filtered apps when not searching)
                    AppGrid(apps = filteredApps, modifier = Modifier.weight(1f))
                } else {
                    // Show search results
                    AppList(apps = filteredApps)
                }
            }

            // Medication Reminder overlay
            if (showMedicationReminder) {
                MedicationReminder(drugName = currentDrug) {
                    showMedicationReminder = false
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    filteredApps: List<AppInfo>
) {
    val context = LocalContext.current
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Search apps...", style = MaterialTheme.typography.headlineSmall) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.Gray
            )
        },
        textStyle = MaterialTheme.typography.headlineSmall, // Apply to input text
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White.copy(alpha = 0.9f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.9f),
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = {
                if (filteredApps.isNotEmpty()) {
                    if (filteredApps[0].packageName == "com.example.lifeguide.settings") {
                        context.startActivity(Intent(context, SettingsActivity::class.java))
                    } else {
                        val launchIntent =
                            context.packageManager.getLaunchIntentForPackage(filteredApps[0].packageName)
                        launchIntent?.let {
                            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(it)
                        }
                    }
                }
            }
        )
    )
}

@Composable
fun RemindersWidget() {
    val displayedReminders = remember { mutableStateListOf<String>() }

    LaunchedEffect(Unit) {
        // Initialize with random reminders
        displayedReminders.addAll(seniorReminders.shuffled().take(3))

        while (true) {
            delay(30000) // 30 seconds
            displayedReminders.clear()
            displayedReminders.addAll(seniorReminders.shuffled().take(3))
        }
    }

    Surface(
        color = Color.White.copy(alpha = 0.8f),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Reminders",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            displayedReminders.forEach { reminder ->
                Text(
                    text = "• $reminder",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun MedicationReminder(drugName: String, onDismiss: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(5000) // Display for 5 seconds
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)) // Semi-transparent black background
            .clickable(onClick = onDismiss), // Dismiss on click
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Time to take your $drugName!",
                color = Color.White,
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
            Text(
                text = "(Tap to dismiss early)",
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AppList(apps: List<AppInfo>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(apps) { app ->
            AppRow(app = app)
        }
    }
}

@Composable
fun AppRow(app: AppInfo) {
    val context = LocalContext.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clickable {
                if (app.packageName == "com.example.lifeguide.settings") {
                    context.startActivity(Intent(context, SettingsActivity::class.java))
                } else {
                    try {
                        val launchIntent =
                            context.packageManager.getLaunchIntentForPackage(app.packageName)
                        launchIntent?.let {
                            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(it)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.size(90.dp)
        ) {
            Image(
                bitmap = app.icon.toBitmap().asImageBitmap(),
                contentDescription = app.name,
                modifier = Modifier.size(80.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = app.name,
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun AppGrid(apps: List<AppInfo>, modifier: Modifier = Modifier) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(apps) { app ->
            AppIcon(app = app, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun AppIcon(app: AppInfo, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .padding(4.dp)
    ) {
        Surface(
            color = Color.White.copy(alpha = 0.2f),
            shape = MaterialTheme.shapes.medium,
            onClick = {
                if (app.packageName == "com.example.lifeguide.settings") {
                    context.startActivity(Intent(context, SettingsActivity::class.java))
                } else {
                    try {
                        val launchIntent =
                            context.packageManager.getLaunchIntentForPackage(app.packageName)
                        launchIntent?.let {
                            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(it)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            },
            modifier = Modifier.size(110.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Image(
                    bitmap = app.icon.toBitmap().asImageBitmap(),
                    contentDescription = app.name,
                    modifier = Modifier.size(96.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = app.name,
            style = MaterialTheme.typography.headlineMedium.copy(
                shadow = Shadow(color = Color.Black, offset = Offset(2f, 2f), blurRadius = 4f)
            ),
            color = Color.White,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
