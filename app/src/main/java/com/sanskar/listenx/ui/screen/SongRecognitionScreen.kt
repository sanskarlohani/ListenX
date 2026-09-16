package com.sanskar.listenx.ui.screen

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.sanskar.listenx.R
import com.sanskar.listenx.data.model.FavoriteSong
import com.sanskar.listenx.data.model.RecognitionState
import com.sanskar.listenx.viewmodel.SongRecognitionViewModel
import androidx.compose.foundation.Image
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import com.sanskar.listenx.ui.util.performSoftHaptic
import java.util.Locale

/**
 * Normalize raw fingerprint match scores to 0-100 percentage scale
 * Backend returns raw matching confidence values that can be very large (28k-43k+)
 * We normalize to a standard 0-100 scale using a threshold approach
 */
fun normalizeMatchScoreInRecognition(rawScore: Float): Float {
    /**
     * Empirically determined thresholds based on real fingerprint matching:
     * - Top match (highest score): ~43,231 → should be close to 100%
     * - Low match (lowest useful score): ~28,484 → should be significantly lower
     *
     * We use 50,000 as the reference point:
     * - Scores >= 50,000: clamped to 100%
     * - Scores <= 0: 0%
     * - Scores in between: (score / 50,000) * 100
     *
     * This gives us reasonable percentage distribution:
     * - 43,231 → ~86.5%
     * - 41,885 → ~83.8%
     * - 28,484 → ~57.0%
     */
    val MAX_REFERENCE_SCORE = 50000f
    val percentageScore = (rawScore / MAX_REFERENCE_SCORE) * 100f
    return percentageScore.coerceIn(0f, 100f)
}

@Composable
fun SongRecognitionScreen(
    viewModel: SongRecognitionViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val state by viewModel.recognitionState.collectAsState()
    val recordingProgress by viewModel.recordingProgress.collectAsState()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // 🔹 Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                haptic.performSoftHaptic(context)
                onNavigateBack()
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }

            Text(
                text = "Identify Song",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.size(48.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))

        when (state) {

            // 🔵 Idle State
            is RecognitionState.Idle -> {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.audiolisten),
                        contentDescription = "Audio Listen",
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(250.dp),
                        contentScale = ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Ready to Identify?",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Tap the button below to start recording",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            haptic.performSoftHaptic(context)
                            viewModel.startRecognition()
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(56.dp),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Icon(
                            Icons.Default.Mic,
                            contentDescription = "Start",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Recording", fontSize = 16.sp)
                    }
                }
            }

            // 🔵 Listening State
            is RecognitionState.Listening -> {

                val scale by animateFloatAsState(
                    targetValue = if (recordingProgress % 20 < 10) 1f else 0.95f,
                    animationSpec = tween(600, easing = LinearEasing),
                    label = "Pulse"
                )

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(250.dp)
                            .scale(scale),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = androidx.compose.ui.res.painterResource(id = R.drawable.audiolisten),
                            contentDescription = "Listening",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "🎵 Listening...",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(0.7f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LinearProgressIndicator(
                            { recordingProgress / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "${recordingProgress}% • ${10 - (recordingProgress / 10)} sec remaining",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // 🔵 Processing State
            is RecognitionState.Processing -> {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.searching),
                        contentDescription = "Searching",
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(250.dp),
                        contentScale = ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "🔍 Analyzing...",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Finding your song in the database",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // 🔵 Success State
            is RecognitionState.Success -> {

                val songs = (state as RecognitionState.Success).songs

                Column(modifier = Modifier.fillMaxSize()) {

                    // 🎵 Result Header Image
                    Image(
                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.result),
                        contentDescription = "Results",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "✅ Found ${songs.size} Match${if (songs.size != 1) "es" else ""}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(songs) { song ->

                            val isFav = viewModel.isFavorite(song.youtubeId)

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium,
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    AsyncImage(
                                        model = "https://img.youtube.com/vi/${song.youtubeId}/mqdefault.jpg",
                                        contentDescription = "Thumbnail",
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(MaterialTheme.shapes.small),
                                        contentScale = ContentScale.Crop
                                    )

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = song.title,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )

                                        Text(
                                            text = song.artist,
                                            fontSize = 14.sp,
                                            maxLines = 1,
                                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                                        )

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            LinearProgressIndicator(
                                                { normalizeMatchScoreInRecognition(song.score) / 100f },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(4.dp),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            val percentageScore = normalizeMatchScoreInRecognition(song.score)
                                            Text(
                                                text = String.format(Locale.US, "%.1f%%", percentageScore),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = {
                                            haptic.performSoftHaptic(context)
                                            val fav = FavoriteSong(
                                                id = song.songId.toString(),
                                                title = song.title,
                                                artist = song.artist,
                                                youtubeId = song.youtubeId,
                                                score = song.score,
                                                album = null
                                            )

                                            if (isFav)
                                                viewModel.removeFromFavorites(song.youtubeId)
                                            else
                                                viewModel.addToFavorites(fav)
                                        },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isFav)
                                                Icons.Default.Favorite
                                            else
                                                Icons.Default.FavoriteBorder,
                                            contentDescription = "Favorite",
                                            tint = if (isFav)
                                                MaterialTheme.colorScheme.error
                                            else
                                                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            haptic.performSoftHaptic(context)
                            onNavigateToHome()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .padding(horizontal = 16.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Search Again", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // 🔴 Error State
            is RecognitionState.Error -> {

                val msg = (state as RecognitionState.Error).message

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Error",
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.error
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "Error",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = msg,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            haptic.performSoftHaptic(context)
                            viewModel.startRecognition()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Try Again")
                    }

                    OutlinedButton(
                        onClick = {
                            haptic.performSoftHaptic(context)
                            onNavigateBack()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}