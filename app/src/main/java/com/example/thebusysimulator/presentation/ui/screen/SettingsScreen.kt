package com.example.thebusysimulator.presentation.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.thebusysimulator.data.datasource.FakeCallSettingsDataSource
import com.example.thebusysimulator.presentation.ui.statusBarPadding
import com.example.thebusysimulator.presentation.ui.theme.ThemeMode
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(navController: NavController) {
    MainContainer(navController = navController) {
        SettingsScreenContent(navController = navController)
    }
}

@Composable
fun SettingsScreenContent(navController: NavController) {
    val context = LocalContext.current
    val settingsDataSource = remember { FakeCallSettingsDataSource(context) }
    val scope = rememberCoroutineScope()
    val systemIsDark = isSystemInDarkTheme()
    
    var themeMode by remember { mutableStateOf("system") }
    
    LaunchedEffect(Unit) {
        settingsDataSource.themeMode.collect { mode ->
            themeMode = mode
        }
    }
    
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarPadding()
            .padding(16.dp)
    ) {
        // Top bar with title
        Text(
            text = "Cài đặt",
            style = MaterialTheme.typography.headlineMedium,
            color = colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Theme Selection Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Giao diện",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // Theme Options
                ThemeOption(
                    icon = Icons.Filled.Settings,
                    title = ThemeMode.SYSTEM.displayName,
                    selected = themeMode == ThemeMode.SYSTEM.value,
                    onClick = {
                        if (themeMode != ThemeMode.SYSTEM.value) {
                            scope.launch {
                                settingsDataSource.setThemeMode(ThemeMode.SYSTEM.value)
                            }
                        }
                    }
                )
                
                Divider()
                
                ThemeOption(
                    icon = Icons.Filled.Menu,
                    title = ThemeMode.LIGHT.displayName,
                    selected = themeMode == ThemeMode.LIGHT.value,
                    onClick = {
                        if (themeMode != ThemeMode.LIGHT.value) {
                            scope.launch {
                                settingsDataSource.setThemeMode(ThemeMode.LIGHT.value)
                            }
                        }
                    }
                )
                
                Divider()
                
                ThemeOption(
                    icon = Icons.Filled.Person,
                    title = ThemeMode.DARK.displayName,
                    selected = themeMode == ThemeMode.DARK.value,
                    onClick = {
                        if (themeMode != ThemeMode.DARK.value) {
                            scope.launch {
                                settingsDataSource.setThemeMode(ThemeMode.DARK.value)
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ThemeOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (selected) {
            RadioButton(
                selected = true,
                onClick = null
            )
        }
    }
}

