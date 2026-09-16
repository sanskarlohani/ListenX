package com.sanskar.listenx.ui.component

import com.sanskar.listenx.navigation.Screen
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanskar.listenx.ui.theme.GradientEnd
import com.sanskar.listenx.ui.theme.GradientStart
import com.sanskar.listenx.ui.util.neumorphicStyle
import com.sanskar.listenx.ui.util.performSoftHaptic
import com.sanskar.listenx.ui.util.softHapticClickable
import androidx.compose.ui.platform.LocalHapticFeedback

@Composable
fun BottomNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Total height of the component needs to be larger than the bar to accommodate the floating button
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(104.dp), // Increased height to prevent clipping
        contentAlignment = Alignment.BottomCenter
    ) {
        // Main Bar Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                .height(72.dp)
                .neumorphicStyle(shape = RoundedCornerShape(24.dp))
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(24.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home
                NavBarItem(
                    icon = Icons.Default.Home,
                    isSelected = currentRoute == "home",
                    onClick = { onNavigate("home") }
                )

                // Favorites
                NavBarItem(
                    icon = if (currentRoute == "favorites")
                        Icons.Default.Favorite
                    else
                        Icons.Default.FavoriteBorder,
                    isSelected = currentRoute == "favorites",
                    onClick = { onNavigate("favorites") }
                )

                // Reserve space for center floating button
                Spacer(modifier = Modifier.size(60.dp))

                // History
                NavBarItem(
                    icon = Icons.Default.History,
                    isSelected = currentRoute == "result",
                    onClick = { onNavigate("result") }
                )

                // Profile
                NavBarItem(
                    icon = Icons.Default.Person,
                    isSelected = currentRoute == "profile",
                    onClick = { onNavigate("profile") }
                )
            }
        }

        // 🎤 Enhanced Floating Center Button
        val haptic = LocalHapticFeedback.current
        val context = LocalContext.current
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp)
                .size(70.dp)
                .neumorphicStyle(shape = CircleShape)
                .clip(CircleShape)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(GradientStart, com.sanskar.listenx.ui.theme.GradientEnd)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = {
                haptic.performSoftHaptic(context)
                onNavigate("listening")
            }) {
                Icon(
                    Icons.Default.Mic,
                    contentDescription = "Listen",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun NavBarItem(
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val tint by animateColorAsState(
        targetValue = if (isSelected) GradientStart else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
        animationSpec = spring(), label = "tint"
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.2f else 1.0f,
        animationSpec = spring(), label = "scale"
    )

    Column(
        modifier = Modifier
            .size(56.dp)
            .softHapticClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier
                .size(26.dp)
                .graphicsLayer(scaleX = scale, scaleY = scale)
        )
        
        if (isSelected) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(4.dp)
                    .background(GradientStart, CircleShape)
            )
        }
    }
}
