package com.sanskar.listenx.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.sanskar.listenx.data.model.FavoriteSong
import com.sanskar.listenx.data.model.RecognitionState
import com.sanskar.listenx.ui.theme.GradientStart
import com.sanskar.listenx.viewmodel.SongRecognitionViewModel
import java.util.Locale
import androidx.compose.ui.platform.LocalContext

/**
 * Normalize raw fingerprint match scores to 0-100 percentage scale
 * Backend returns raw matching confidence values that can be very large (28k-43k+)
 * We normalize to a standard 0-100 scale using a threshold approach
 */
fun normalizeMatchScore(rawScore: Float): Float {
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
fun ResultScreen(
    viewModel: SongRecognitionViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val state by viewModel.recognitionState.collectAsState()
    val context = LocalContext.current

    fun openYouTube(videoId: String) {
        val url = "https://www.youtube.com/watch?v=$videoId"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }

    fun shareSong(title: String, artist: String, videoId: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "Check out this song: $title by $artist - https://www.youtube.com/watch?v=$videoId")
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, null))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (state) {
            is RecognitionState.Success -> {
                val songs = (state as RecognitionState.Success).songs

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    // 🔹 Top Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }

                        Text(
                            text = "✅ Found ${songs.size} Match${if (songs.size != 1) "es" else ""}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = GradientStart
                        )

                        Spacer(modifier = Modifier.size(48.dp))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 🎵 Results List
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(songs) { song ->
                            val isFav = viewModel.isFavorite(song.youtubeId)

                            ResultSongCard(
                                song = song,
                                isFavorite = isFav,
                                onFavoriteClick = {
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
                                onPlayClick = { openYouTube(song.youtubeId) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 🎤 Action Buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.resetState()
                                onNavigateToHome()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GradientStart
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Search Again", fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = { /* Navigate to profile */ },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = GradientStart
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Share", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            is RecognitionState.Error -> {
                val message = (state as RecognitionState.Error).message
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "❌ Error",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = message,
                        fontSize = 16.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = onNavigateToHome,
                        colors = ButtonDefaults.buttonColors(containerColor = GradientStart)
                    ) {
                        Text("Try Again")
                    }
                }
            }
            is RecognitionState.Processing -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator(color = GradientStart)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Finalizing results...")
                }
            }
            else -> {
                // For demo purposes: If idle, load mock songs automatically
                val mockSongs = com.sanskar.listenx.util.MockDataProvider.getMockSongs()
                
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 🔹 Top Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }

                        Text(
                            text = "📜 Recognition History",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = GradientStart
                        )

                        Spacer(modifier = Modifier.size(48.dp))
                    }

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(mockSongs) { song ->
                            val isFav = viewModel.isFavorite(song.youtubeId)
                            ResultSongCard(
                                song = song,
                                isFavorite = isFav,
                                onFavoriteClick = {
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
                                onPlayClick = { openYouTube(song.youtubeId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResultSongCard(
    song: com.sanskar.listenx.data.model.SongRecognitionResponse,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onPlayClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Album Art + Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Album Image
                AsyncImage(
                    model = "https://img.youtube.com/vi/${song.youtubeId}/mqdefault.jpg",
                    contentDescription = song.title,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )

                // Song Info
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = song.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2
                    )

                    Text(
                        text = song.artist,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1
                    )

                    // Match Score
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Match Confidence",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )

                            // Normalize raw fingerprint score to 0-100 percentage
                            val percentageScore = normalizeMatchScore(song.score)
                            Text(
                                text = String.format(Locale.US, "%.1f%%", percentageScore),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GradientStart
                            )
                        }

                        LinearProgressIndicator(
                            progress = { normalizeMatchScore(song.score) / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = GradientStart,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }

                    // Favorite Button
                    IconButton(
                        onClick = onFavoriteClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite)
                                Icons.Default.Favorite
                            else
                                Icons.Default.FavoriteBorder,
                            contentDescription = "Add to Favorites",
                            tint = if (isFavorite) Color.Red else GradientStart,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onPlayClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GradientStart
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Text("Play on YouTube", fontSize = 12.sp)
                }

                Button(
                    onClick = { /* Share */ },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = GradientStart
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Share", fontSize = 12.sp)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
@Suppress("ViewModelConstructorInComposable")
fun ResultScreenPreview() {
    ResultScreen(
        viewModel = SongRecognitionViewModel(),
        onNavigateBack = {},
        onNavigateToHome = {}
    )
}
