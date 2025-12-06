package com.example.lifeguide

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.lifeguide.ui.theme.LifeGuideTheme

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LifeGuideTheme {
                SettingsScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("lifeguide_prefs", Context.MODE_PRIVATE)

    val wallpaperPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            with(sharedPrefs.edit()) {
                putString("wallpaper_uri", uri?.toString())
                apply()
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("LifeGuide Settings") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Wallpaper Settings
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Wallpaper",
                    style = MaterialTheme.typography.titleLarge
                )
                Row {
                    Button(onClick = { wallpaperPickerLauncher.launch("image/*") }) {
                        Text("Change")
                    }
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Button(onClick = {
                        with(sharedPrefs.edit()) {
                            remove("wallpaper_uri")
                            apply()
                        }
                    }) {
                        Text("Clear")
                    }
                }
            }
        }
    }
}