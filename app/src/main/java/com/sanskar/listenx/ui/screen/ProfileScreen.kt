package com.sanskar.listenx.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.sanskar.listenx.ui.theme.GradientEnd
import com.sanskar.listenx.ui.theme.GradientStart
import com.sanskar.listenx.ui.util.HapticPreference
import com.sanskar.listenx.ui.util.ThemePreference
import com.sanskar.listenx.ui.util.neumorphic
import com.sanskar.listenx.ui.util.neumorphicStyle
import com.sanskar.listenx.viewmodel.SongRecognitionViewModel

@Composable
fun ProfileScreen(
    viewModel: SongRecognitionViewModel,
    onNavigateBack: () -> Unit,
    onSettingsClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val context = LocalContext.current
    var notificationsEnabled by remember { mutableStateOf(true) }
    val isDarkMode = ThemePreference.isDarkTheme()
    var hapticsEnabled by remember { mutableStateOf(HapticPreference.isHapticEnabled(context)) }
    
    val totalSongs by viewModel.totalSongsInDb.collectAsState()
    val favorites by viewModel.favoritesList.collectAsState()
    val searchCount by viewModel.searchHistoryCount.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadInitialData()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // 🔥 IMPROVED HEADER
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp) // Increased height for breathing room
            ) {
                // Background Gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp) // Gradient background doesn't cover full Box height
                        .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(GradientStart, GradientEnd)
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                        }

                        Text(
                            "My Account",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )

                        IconButton(
                            onClick = onSettingsClick,
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(Icons.Default.Settings, null, tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Profile Image
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .neumorphicStyle(shape = CircleShape)
                            .background(Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "U",
                            fontSize = 42.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = GradientStart
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "User Account",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                    Text(
                        "Music Enthusiast 🎵",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // 🔥 STATS (Centered and Theme-Aware)
        item {
            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .offset(y = (-40).dp) // Better overlap position
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp) // More spacing
            ) {
                StatCard(totalSongs.toString(), "Found", Icons.Default.MusicNote, Modifier.weight(1f))
                StatCard(favorites.size.toString(), "Liked", Icons.Default.Favorite, Modifier.weight(1f))
                StatCard(searchCount.toString(), "Searches", Icons.Default.History, Modifier.weight(1f))
            }
        }

        // 🔥 SETTINGS SECTION
        item {
            SectionHeader("Preferences")
            Card(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .neumorphicStyle(shape = RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column {
                    SwitchItem(
                        "Notifications",
                        "App alerts & updates",
                        Icons.Default.Notifications,
                        notificationsEnabled
                    ) { notificationsEnabled = it }
                    
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.2f))
                    
                    SwitchItem(
                        "Dark Mode",
                        "Toggle dark theme",
                        Icons.Default.DarkMode,
                        isDarkMode
                    ) { enabled ->
                        ThemePreference.setDarkMode(context, enabled)
                    }

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.2f))

                    SwitchItem(
                        "Haptic Feedback",
                        "Touch vibration on tap",
                        Icons.Default.Vibration,
                        hapticsEnabled
                    ) { enabled ->
                        hapticsEnabled = enabled
                        HapticPreference.setHapticEnabled(context, enabled)
                    }
                }
            }
        }

        item {
            SectionHeader("Account Settings")
            Card(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .neumorphicStyle(shape = RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column {
                    SettingItem("My History", "Manage past searches", Icons.Default.History)
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.2f))
                    SettingItem("Privacy & Security", "Data usage & safety", Icons.Default.Security)
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.2f))
                    SettingItem("Logout", "Sign out of your account", Icons.AutoMirrored.Filled.Logout, color = Color.Red, onClick = onLogoutClick)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "ListenX v1.0.0",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(start = 24.dp, top = 4.dp, bottom = 6.dp),
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = GradientStart
    )
}

@Composable
fun StatCard(value: String, label: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .height(110.dp)
            .neumorphicStyle(shape = RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = GradientStart, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun SettingItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color = GradientStart,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(color.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }

        Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray.copy(alpha = 0.6f))
    }
}

@Composable
fun SwitchItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onChecked: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(GradientStart.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = GradientStart, modifier = Modifier.size(22.dp))
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }

        Switch(
            checked = checked,
            onCheckedChange = onChecked,
            colors = SwitchDefaults.colors(
                checkedTrackColor = GradientStart,
                checkedThumbColor = Color.White,
                uncheckedTrackColor = Color.LightGray.copy(alpha = 0.3f)
            )
        )
    }
}
