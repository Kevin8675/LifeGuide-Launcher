@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.lifeguide

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.lifeguide.ui.theme.LifeGuideTheme

class AppListActivity : ComponentActivity() {
    private lateinit var packageManager: PackageManager
    private var allApps = listOf<AppInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        packageManager = getPackageManager()
        allApps = loadApps()

        setContent {
            LifeGuideTheme {
                MainScreen(
                    apps = allApps,
                    onBackClick = { finish() },
                    onAppClick = { app ->
                        try {
                            val intent = packageManager.getLaunchIntentForPackage(app.packageName)
                            startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(this, "Error launching app: ${'$'}{e.message}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onSearchInApp = { app ->
                        try {
                            val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                                `package` = app.packageName
                                putExtra("query", "")
                            }
                            startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(this, "Search not supported in this app", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }

    private fun loadApps(): List<AppInfo> {
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)

        return packageManager.queryIntentActivities(mainIntent, 0)
            .map {
                AppInfo(
                    name = it.loadLabel(packageManager).toString(),
                    packageName = it.activityInfo.packageName,
                    icon = it.loadIcon(packageManager)
                )
            }
            .sortedBy { it.name.lowercase() }
    }
}

@Composable
fun MainScreen(
    apps: List<AppInfo>,
    onBackClick: () -> Unit,
    onAppClick: (AppInfo) -> Unit,
    onSearchInApp: (AppInfo) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredApps = remember(searchQuery, apps) {
        if (searchQuery.isBlank()) {
            apps
        } else {
            apps.filter { app ->
                app.name.contains(searchQuery, ignoreCase = true) ||
                app.packageName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TopAppBar(
                title = { Text("Installed Apps") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search apps") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            // App List
            if (filteredApps.isEmpty()) {
                NoAppsFound()
            } else {
                AppList(
                    apps = filteredApps,
                    onAppClick = onAppClick,
                    onSearchInApp = onSearchInApp
                )
            }
        }
    }
}

@Composable
fun NoAppsFound() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("No apps found")
    }
}

@Composable
fun AppList(
    apps: List<AppInfo>,
    onAppClick: (AppInfo) -> Unit,
    onSearchInApp: (AppInfo) -> Unit
) {
    LazyColumn {
        items(apps) { app ->
            AppListItem(
                app = app,
                onAppClick = { onAppClick(app) },
                onSearchInApp = { onSearchInApp(app) }
            )
            Divider()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppListItem(
    app: AppInfo,
    onAppClick: () -> Unit,
    onSearchInApp: () -> Unit
) {
    Card(
        onClick = onAppClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Icon
            val bitmap = remember(app.packageName) {
                Bitmap.createBitmap(
                    app.icon.intrinsicWidth,
                    app.icon.intrinsicHeight,
                    Bitmap.Config.ARGB_8888
                ).apply {
                    val canvas = Canvas(this)
                    app.icon.setBounds(0, 0, canvas.width, canvas.height)
                    app.icon.draw(canvas)
                }
            }
            
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = app.name,
                modifier = Modifier.size(48.dp)
            )

            // App Name and Package
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = app.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = app.packageName,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Search in App Button
            TextButton(
                onClick = { onSearchInApp() },
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search in ${'$'}{app.name}",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
