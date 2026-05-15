package com.sanskar.listenx.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.sanskar.listenx.ui.theme.GradientEnd
import com.sanskar.listenx.ui.theme.GradientStart

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.sanskar.listenx.data.model.FavoriteSong
import com.sanskar.listenx.util.MockDataProvider
import com.sanskar.listenx.viewmodel.SongRecognitionViewModel

@Composable
fun HomeScreen(
    viewModel: SongRecognitionViewModel
) {
    val favorites by viewModel.favoritesList.collectAsState()
    val mockSongs = MockDataProvider.getMockSongs()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)
        ) {
            // 🎵 Header Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "FOURIO",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GradientStart
                )
                Text(
                    text = "Discover What's Playing",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 🎨 Featured Card Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Featured Card 1
                FeaturedSongCard(
                    title = "Top Charts",
                    subtitle = "Popular Now",
                    imageUrl = "https://images.unsplash.com/photo-1511379938547-c1f69b13d835?w=400",
                    gradientStart = GradientStart,
                    gradientEnd = GradientEnd
                )

                // Featured Card 2
                FeaturedSongCard(
                    title = "New Releases",
                    subtitle = "Fresh Tracks",
                    imageUrl = "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=400",
                    gradientStart = Color(0xFFFF6B6B),
                    gradientEnd = Color(0xFFFFE66D)
                )

                // Featured Card 3
                FeaturedSongCard(
                    title = "Trending",
                    subtitle = "This Week",
                    imageUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=400",
                    gradientStart = Color(0xFF4ECDC4),
                    gradientEnd = Color(0xFF44A08D)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 🎵 Recently Played Section Header
            Text(
                text = "Recently Played",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 🎵 Song List (Horizontal Scroll)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                mockSongs.forEach { song ->
                    val isFav = viewModel.isFavorite(song.youtubeId)
                    SongCard(
                        title = song.title,
                        artist = song.artist,
                        imageUrl = "https://img.youtube.com/vi/${song.youtubeId}/mqdefault.jpg",
                        isFavorite = isFav,
                        onFavoriteClick = {
                            if (isFav) {
                                viewModel.removeFromFavorites(song.youtubeId)
                            } else {
                                viewModel.addToFavorites(
                                    FavoriteSong(
                                        id = song.songId.toString(),
                                        title = song.title,
                                        artist = song.artist,
                                        youtubeId = song.youtubeId,
                                        score = song.score,
                                        album = null
                                    )
                                )
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun FeaturedSongCard(
    title: String,
    subtitle: String,
    imageUrl: String,
    gradientStart: Color,
    gradientEnd: Color
) {
    Card(
        modifier = Modifier
            .size(width = 280.dp, height = 160.dp)
            .clip(RoundedCornerShape(20.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(gradientStart, gradientEnd)
                    )
                )
        ) {
            // Background Image (lower opacity)
            AsyncImage(
                model = imageUrl,
                contentDescription = title,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp)),
                contentScale = ContentScale.Crop,
                alpha = 0.3f
            )

            // Overlay with text
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = subtitle,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }

                Button(
                    onClick = {},
                    modifier = Modifier
                        .height(36.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "Explore",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun SongCard(
    title: String,
    artist: String,
    imageUrl: String,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .size(width = 160.dp, height = 200.dp)
            .clip(RoundedCornerShape(16.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Album Art
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = title,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )

                // Favorite Button
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (isFavorite)
                            Icons.Default.Favorite
                        else
                            Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color.Red else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Song Info
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = artist,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        maxLines = 1
                    )
                }

                Button(
                    onClick = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GradientStart
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Play",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
@Suppress("ViewModelConstructorInComposable")
fun HomeScreenPreview() {
    HomeScreen(viewModel = SongRecognitionViewModel())
}

