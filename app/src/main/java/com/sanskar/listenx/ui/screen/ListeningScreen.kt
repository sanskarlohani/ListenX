package com.sanskar.listenx.ui.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanskar.listenx.ui.theme.GradientEnd
import com.sanskar.listenx.ui.theme.GradientStart
import com.sanskar.listenx.viewmodel.SongRecognitionViewModel

@Composable
fun ListeningScreen(
    viewModel: SongRecognitionViewModel,
    onNavigateBack: () -> Unit
) {
    val recordingProgress by viewModel.recordingProgress.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        GradientStart,
                        GradientEnd,
                        Color(0xFF3D2B6F)
                    ),
                    radius = 1500f
                )
            )
    ) {
        // 🔹 Cancel Button (Top)
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Cancel",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }

        // 🎵 Center Content
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Alignment.Center
        ) {
            // 🎤 Animated Waveform Circle
            AnimatedWaveform(progress = recordingProgress)

            Spacer(modifier = Modifier.height(40.dp))

            // "Listening..." Text
            Text(
                text = "🎵 Listening...",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Subtitle
            Text(
                text = "Hold your device near the sound source",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Progress Text
            Text(
                text = "${recordingProgress}% • ${10 - (recordingProgress / 10)} sec remaining",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Cancel Button (Bottom)
            Button(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(width = 200.dp, height = 56.dp)
                    .clip(CircleShape),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.2f),
                    contentColor = Color.White
                ),
                shape = CircleShape
            ) {
                Text(
                    "Cancel",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun AnimatedWaveform(progress: Int) {
    val scale1 by animateFloatAsState(
        targetValue = 0.8f + (progress / 200f),
        animationSpec = tween(500),
        label = "Scale1"
    )

    val scale2 by animateFloatAsState(
        targetValue = 0.6f + (progress / 300f),
        animationSpec = tween(700),
        label = "Scale2"
    )

    val scale3 by animateFloatAsState(
        targetValue = 0.4f + (progress / 400f),
        animationSpec = tween(900),
        label = "Scale3"
    )

    Box(
        modifier = Modifier
            .size(320.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer circle (3rd ring)
        Box(
            modifier = Modifier
                .size(320.dp)
                .scale(scale3)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            GradientEnd.copy(alpha = 0.2f),
                            GradientEnd.copy(alpha = 0f)
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Middle circle (2nd ring)
        Box(
            modifier = Modifier
                .size(240.dp)
                .scale(scale2)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            GradientEnd.copy(alpha = 0.4f),
                            GradientEnd.copy(alpha = 0.1f)
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Inner circle (1st ring)
        Box(
            modifier = Modifier
                .size(160.dp)
                .scale(scale1)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.6f),
                            GradientEnd.copy(alpha = 0.3f)
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Center circle with Icon
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(
                    color = Color.White,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = androidx.compose.ui.res.painterResource(
                    id = android.os.Build.VERSION.SDK_INT.let {
                        android.R.drawable.ic_btn_speak_now
                    }
                ),
                contentDescription = "Microphone",
                modifier = Modifier.size(50.dp),
                tint = GradientStart
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ListeningScreenPreview() {
    // Mock ViewModel for preview
    ListeningScreen(
        viewModel = SongRecognitionViewModel(),
        onNavigateBack = {}
    )
}

