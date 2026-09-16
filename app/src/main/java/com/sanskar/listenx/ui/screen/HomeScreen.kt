package com.sanskar.listenx.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
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
import com.sanskar.listenx.ui.util.neumorphicStyle

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.sanskar.listenx.data.model.FavoriteSong
import com.sanskar.listenx.util.MockDataProvider
import com.sanskar.listenx.viewmodel.SongRecognitionViewModel

// Helper function to get varied images based on song index
fun getImageUrlForSong(youtubeId: String, index: Int): String {
    val imageUrls = listOf(
        "https://images.unsplash.com/photo-1511379938547-c1f69b13d835?w=300&h=300&fit=crop",
        "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=300&h=300&fit=crop",
        "https://images.unsplash.com/photo-1459749411175-04bf5292ceea?w=300&h=300&fit=crop",
        "https://images.unsplash.com/photo-1487180144351-b8472da7d491?w=300&h=300&fit=crop",
        "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=300&h=300&fit=crop",
        "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=300&h=300&fit=crop",
        "https://images.unsplash.com/photo-1505628346881-b72b27e84530?w=300&h=300&fit=crop",
        "https://images.unsplash.com/photo-1484704849700-f032a568e944?w=300&h=300&fit=crop",
        "https://images.unsplash.com/photo-1500281335143-53f7c8ee4ee2?w=300&h=300&fit=crop",
        "https://images.unsplash.com/photo-1519412666065-38008118fc03?w=300&h=300&fit=crop",
        "https://images.unsplash.com/photo-1459749411175-04bf5292ceea?w=300&h=300&fit=crop",
        "https://images.unsplash.com/photo-1478225143143-5b500636000d?w=300&h=300&fit=crop",
    )
    return imageUrls[index % imageUrls.size]
}

@Composable
fun HomeScreen(
    viewModel: SongRecognitionViewModel
) {
    val favorites by viewModel.favoritesList.collectAsState()
    val mockSongs = MockDataProvider.getMockSongs()
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // 🎵 Header Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "ListenX",
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
                // Featured Card 1 - Top Charts
                FeaturedSongCard(
                    title = "Top Charts",
                    subtitle = "Popular Now",
                    imageUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=500&h=300&fit=crop",
                    gradientStart = GradientStart,
                    gradientEnd = GradientEnd
                )

                // Featured Card 2 - New Releases
                FeaturedSongCard(
                    title = "New Releases",
                    subtitle = "Fresh Tracks",
                    imageUrl = "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=500&h=300&fit=crop",
                    gradientStart = Color(0xFFFF6B6B),
                    gradientEnd = Color(0xFFFFE66D)
                )

                // Featured Card 3 - Trending
                FeaturedSongCard(
                    title = "Trending",
                    subtitle = "This Week",
                    imageUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500&h=300&fit=crop",
                    gradientStart = Color(0xFF4ECDC4),
                    gradientEnd = Color(0xFF44A08D)
                )

                // Featured Card 4 - Feel Good Hits
                FeaturedSongCard(
                    title = "Feel Good Hits",
                    subtitle = "Mood Booster",
                    imageUrl = "https://images.unsplash.com/photo-1459749411175-04bf5292ceea?w=500&h=300&fit=crop",
                    gradientStart = Color(0xFFFA6E5F),
                    gradientEnd = Color(0xFFFD7D92)
                )

                // Featured Card 5 - Chill Vibes
                FeaturedSongCard(
                    title = "Chill Vibes",
                    subtitle = "Relax & Unwind",
                    imageUrl = "https://images.unsplash.com/photo-1487180144351-b8472da7d491?w=500&h=300&fit=crop",
                    gradientStart = Color(0xFF667eea),
                    gradientEnd = Color(0xFF764ba2)
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
                 mockSongs.forEachIndexed { index, song ->
                     val isFav = viewModel.isFavorite(song.youtubeId)
                     SongCard(
                         title = song.title,
                         artist = song.artist,
                         imageUrl = getImageUrlForSong(song.youtubeId, index),
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

             Spacer(modifier = Modifier.height(24.dp))

             // 🎵 Recommended For You Section Header
             Text(
                 text = "Recommended For You",
                 fontSize = 18.sp,
                 fontWeight = FontWeight.Bold,
                 color = MaterialTheme.colorScheme.onBackground,
                 modifier = Modifier.padding(horizontal = 20.dp)
             )

             Spacer(modifier = Modifier.height(12.dp))

             // 🎵 Recommended Song List (Horizontal Scroll)
             Row(
                 modifier = Modifier
                     .fillMaxWidth()
                     .horizontalScroll(rememberScrollState())
                     .padding(horizontal = 20.dp),
                 horizontalArrangement = Arrangement.spacedBy(12.dp)
             ) {
                 mockSongs.reversed().forEachIndexed { index, song ->
                     val isFav = viewModel.isFavorite(song.youtubeId)
                     SongCard(
                         title = song.title,
                         artist = song.artist,
                         imageUrl = getImageUrlForSong(song.youtubeId, index + 6),
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
            .padding(8.dp)
            .neumorphicStyle(shape = RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Background Image
            AsyncImage(
                model = imageUrl,
                contentDescription = title,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp)),
                contentScale = ContentScale.Crop
            )

            // Dark Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                gradientStart.copy(alpha = 0.6f),
                                gradientEnd.copy(alpha = 0.8f)
                            )
                        )
                    )
            )

            // Content Overlay with text
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = subtitle,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = {},
                    modifier = Modifier
                        .height(40.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.25f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "Explore",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
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
            .size(width = 180.dp, height = 240.dp)
            .padding(8.dp)
            .neumorphicStyle(shape = RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Album Art with Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                // Main Image
                AsyncImage(
                    model = imageUrl,
                    contentDescription = title,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)),
                    contentScale = ContentScale.Crop
                )

                // Dark Gradient Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.1f),
                                    Color.Black.copy(alpha = 0.4f)
                                ),
                                startY = 0f,
                                endY = Float.POSITIVE_INFINITY
                            )
                        )
                )

                // Favorite Button with better visibility
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite)
                            Icons.Default.Favorite
                        else
                            Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color(0xFFFF4081) else Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Song Info
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = artist,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
                        maxLines = 1,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GradientStart
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        "Play",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
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

