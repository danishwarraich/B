package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.R
import com.example.config.TmdbConfig
import com.example.domain.model.CastMember
import com.example.domain.model.CineSection
import com.example.domain.model.MediaDetails
import com.example.domain.model.MediaItem
import com.example.domain.model.MediaType
import com.example.domain.model.RegionalWatchProviders
import com.example.domain.model.SUPPORTED_WATCH_REGIONS
import com.example.domain.model.VideoItem
import com.example.domain.model.WatchProviderOption
import com.example.ui.components.CineFooter
import com.example.ui.components.MediaPosterCard
import com.example.ui.theme.BebasNeueFontFamily
import com.example.ui.theme.CineBlack
import com.example.ui.theme.CineBorder
import com.example.ui.theme.CineCharcoal
import com.example.ui.theme.CineCyan
import com.example.ui.theme.CineEmerald
import com.example.ui.theme.CineGold
import com.example.ui.theme.CineRed
import com.example.ui.theme.CineSurface
import com.example.ui.theme.CineSurfaceElevated
import com.example.ui.theme.CineTextMuted
import com.example.ui.theme.CineTextPrimary
import com.example.ui.theme.CineTextSecondary
import com.example.ui.viewmodel.AboutSubSection
import com.example.ui.viewmodel.DetailsUiState
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MediaDetailsOverlayPage(
    detailsState: DetailsUiState,
    selectedWatchRegion: String,
    favoriteIds: Set<String>,
    onClose: () -> Unit,
    onSelectWatchRegion: (String) -> Unit,
    onPlayTrailer: (VideoItem) -> Unit,
    onToggleFavoriteDetails: (MediaDetails) -> Unit,
    onToggleFavoriteItem: (MediaItem) -> Unit,
    onSelectMediaItem: (MediaItem) -> Unit,
    onSelectGenre: (Int, MediaType) -> Unit,
    onOpenAboutTab: (AboutSubSection) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onClose)
    val context = LocalContext.current

    Surface(
        color = CineBlack,
        modifier = modifier
            .fillMaxSize()
            .testTag("media_details_overlay")
    ) {
        when (detailsState) {
            is DetailsUiState.Idle -> Unit
            is DetailsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(color = CineRed)
                        Text(
                            text = "Loading ${detailsState.previewItem?.title ?: "Details"} from TMDB…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CineTextSecondary
                        )
                    }
                }
            }
            is DetailsUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = detailsState.previewItem?.title ?: "Unable to Load Details",
                        style = MaterialTheme.typography.headlineMedium,
                        color = CineTextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = detailsState.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = CineTextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = onClose,
                            colors = ButtonDefaults.buttonColors(containerColor = CineSurfaceElevated)
                        ) {
                            Text(text = stringResource(R.string.action_close))
                        }
                        Button(
                            onClick = {
                                onClose()
                                onOpenAboutTab(AboutSubSection.API_CONFIG)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CineRed)
                        ) {
                            Text(text = "TMDB API Setup Guide")
                        }
                    }
                }
            }
            is DetailsUiState.Success -> {
                val details = detailsState.details
                val isFav = favoriteIds.contains("${details.mediaType.name}_${details.id}")
                val regionalProviders: RegionalWatchProviders? =
                    details.watchProvidersByRegion[selectedWatchRegion]
                        ?: details.watchProvidersByRegion["US"]
                        ?: details.watchProvidersByRegion.values.firstOrNull()

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // 1. Hero Backdrop + Poster + Primary Metadata Header
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CineCharcoal)
                        ) {
                            // Large Backdrop
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(340.dp)
                            ) {
                                val backdrop = details.backdropUrl ?: details.posterUrl
                                if (backdrop != null) {
                                    AsyncImage(
                                        model = backdrop,
                                        contentDescription = details.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Image(
                                        painter = painterResource(id = R.drawable.img_hero_backdrop_1790404514620),
                                        contentDescription = details.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    CineBlack.copy(alpha = 0.45f),
                                                    CineBlack.copy(alpha = 0.7f),
                                                    CineBlack
                                                )
                                            )
                                        )
                                )

                                // Top Back Button Bar
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = onClose,
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(CineBlack.copy(alpha = 0.75f))
                                            .testTag("details_back_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = stringResource(R.string.action_close),
                                            tint = Color.White
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (details.mediaType == MediaType.MOVIE) CineRed else CineCyan
                                            )
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = if (details.mediaType == MediaType.MOVIE) {
                                                "MOVIE DETAILS"
                                            } else {
                                                "TV SHOW DETAILS"
                                            },
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (details.mediaType == MediaType.MOVIE) Color.White else CineBlack,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // Poster + Title + Metadata Row overlapping backdrop bottom
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 190.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // Poster Card
                                    Card(
                                        shape = RoundedCornerShape(14.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                                        modifier = Modifier
                                            .width(126.dp)
                                            .aspectRatio(2f / 3f)
                                            .border(1.5.dp, CineBorder, RoundedCornerShape(14.dp))
                                    ) {
                                        if (details.posterUrl != null) {
                                            AsyncImage(
                                                model = details.posterUrl,
                                                contentDescription = details.title,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(CineSurfaceElevated),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Movie,
                                                    contentDescription = null,
                                                    tint = CineTextMuted,
                                                    modifier = Modifier.size(42.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Title, Tagline, Rating, Year, Runtime
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = details.title.uppercase(),
                                            fontFamily = BebasNeueFontFamily,
                                            fontSize = 34.sp,
                                            lineHeight = 36.sp,
                                            color = CineTextPrimary
                                        )

                                        if (details.tagline.isNotBlank()) {
                                            Text(
                                                text = "\"${details.tagline}\"",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = CineGold
                                            )
                                        }

                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(CineSurfaceElevated)
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Star,
                                                    contentDescription = null,
                                                    tint = CineGold,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                                Text(
                                                    text = String.format(Locale.US, "%.1f", details.rating),
                                                    style = MaterialTheme.typography.labelLarge,
                                                    color = CineTextPrimary
                                                )
                                                Text(
                                                    text = "(${details.voteCount})",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = CineTextSecondary
                                                )
                                            }

                                            MetadataBadgePill(text = details.releaseYear)
                                            MetadataBadgePill(text = details.runtimeFormatted)
                                            MetadataBadgePill(text = details.status)
                                        }
                                    }
                                }

                                // Genre Chips
                                if (details.genres.isNotEmpty()) {
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        details.genres.forEach { genre ->
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(20.dp))
                                                    .background(CineSurfaceElevated)
                                                    .border(1.dp, CineRed.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                                                    .clickable {
                                                        onSelectGenre(genre.id, details.mediaType)
                                                    }
                                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = genre.name,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = CineTextPrimary
                                                )
                                            }
                                        }
                                    }
                                }

                                // Primary Action Buttons: Watch Trailer + Add/Remove My List
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    val primaryTrailer = details.primaryTrailer
                                    Button(
                                        onClick = {
                                            if (primaryTrailer != null) {
                                                onPlayTrailer(primaryTrailer)
                                            } else {
                                                val ytQuery = Uri.encode("${details.title} ${details.releaseYear} official trailer")
                                                runCatching {
                                                    context.startActivity(
                                                        Intent(
                                                            Intent.ACTION_VIEW,
                                                            Uri.parse("https://www.youtube.com/results?search_query=$ytQuery")
                                                        )
                                                    )
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = CineRed),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("details_watch_trailer_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.PlayArrow,
                                            contentDescription = null
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = stringResource(R.string.action_watch_trailer),
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = { onToggleFavoriteDetails(details) },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("details_favorite_button")
                                    ) {
                                        Icon(
                                            imageVector = if (isFav) Icons.Filled.Check else Icons.Filled.Add,
                                            contentDescription = null,
                                            tint = if (isFav) CineGold else CineTextPrimary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = stringResource(
                                                if (isFav) R.string.action_in_my_list else R.string.action_add_my_list
                                            ),
                                            color = CineTextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2. Storyline / Overview & Director / Creator
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.details_overview).uppercase(),
                                fontFamily = BebasNeueFontFamily,
                                fontSize = 24.sp,
                                color = CineTextPrimary
                            )
                            Text(
                                text = details.overview,
                                style = MaterialTheme.typography.bodyLarge,
                                color = CineTextSecondary
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CineSurface)
                                    .border(1.dp, CineBorder, RoundedCornerShape(12.dp))
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = stringResource(
                                            if (details.mediaType == MediaType.MOVIE) R.string.details_director
                                            else R.string.details_creator
                                        ),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CineTextMuted
                                    )
                                    Text(
                                        text = details.directorOrCreator,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = CineTextPrimary
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Release Date",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CineTextMuted
                                    )
                                    Text(
                                        text = details.releaseDateFormatted,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = CineTextSecondary
                                    )
                                }
                            }
                        }
                    }

                    // 3. WHERE TO WATCH / WATCH PROVIDERS SECTION
                    item {
                        WhereToWatchSection(
                            selectedWatchRegion = selectedWatchRegion,
                            regionalProviders = regionalProviders,
                            availableRegionCodes = details.watchProvidersByRegion.keys,
                            onSelectRegion = onSelectWatchRegion
                        )
                    }

                    // 4. TOP CAST SECTION
                    if (details.cast.isNotEmpty()) {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = stringResource(R.string.details_cast).uppercase(),
                                    fontFamily = BebasNeueFontFamily,
                                    fontSize = 24.sp,
                                    color = CineTextPrimary,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(details.cast, key = { "${it.id}_${it.character}" }) { member ->
                                        CastMemberCard(member = member)
                                    }
                                }
                            }
                        }
                    }

                    // 5. OFFICIAL TRAILERS & VIDEOS SECTION
                    if (details.trailers.isNotEmpty()) {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = stringResource(R.string.details_trailers).uppercase(),
                                    fontFamily = BebasNeueFontFamily,
                                    fontSize = 24.sp,
                                    color = CineTextPrimary,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(details.trailers, key = { it.id }) { video ->
                                        TrailerThumbnailCard(
                                            video = video,
                                            onClick = { onPlayTrailer(video) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 6. SIMILAR MOVIES / TV SHOWS
                    if (details.similar.isNotEmpty()) {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = stringResource(R.string.details_similar).uppercase(),
                                    fontFamily = BebasNeueFontFamily,
                                    fontSize = 24.sp,
                                    color = CineTextPrimary,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(details.similar, key = { "sim_${it.id}" }) { media ->
                                        MediaPosterCard(
                                            item = media,
                                            isFavorite = favoriteIds.contains("${media.mediaType.name}_${media.id}"),
                                            onClick = { onSelectMediaItem(media) },
                                            onToggleFavorite = { onToggleFavoriteItem(media) },
                                            modifier = Modifier.width(146.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 7. RECOMMENDATIONS
                    if (details.recommendations.isNotEmpty()) {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = stringResource(R.string.details_recommendations).uppercase(),
                                    fontFamily = BebasNeueFontFamily,
                                    fontSize = 24.sp,
                                    color = CineTextPrimary,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(details.recommendations, key = { "rec_${it.id}" }) { media ->
                                        MediaPosterCard(
                                            item = media,
                                            isFavorite = favoriteIds.contains("${media.mediaType.name}_${media.id}"),
                                            onClick = { onSelectMediaItem(media) },
                                            onToggleFavorite = { onToggleFavoriteItem(media) },
                                            modifier = Modifier.width(146.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(28.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun MetadataBadgePill(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(CineSurfaceElevated)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = CineTextSecondary
        )
    }
}

@Composable
private fun CastMemberCard(member: CastMember) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CineSurface),
        modifier = Modifier
            .width(120.dp)
            .border(1.dp, CineBorder, RoundedCornerShape(12.dp))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(CineSurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                if (member.profileUrl != null) {
                    AsyncImage(
                        model = member.profileUrl,
                        contentDescription = member.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = CineTextMuted,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = member.name,
                    style = MaterialTheme.typography.labelLarge,
                    color = CineTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = member.character,
                    style = MaterialTheme.typography.bodySmall,
                    color = CineTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun TrailerThumbnailCard(
    video: VideoItem,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CineSurface),
        modifier = Modifier
            .width(250.dp)
            .border(1.dp, CineBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("trailer_card_${video.key}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(CineSurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = video.youtubeThumbnailUrl,
                    contentDescription = video.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CineBlack.copy(alpha = 0.35f))
                )
                Icon(
                    imageVector = Icons.Filled.PlayCircleFilled,
                    contentDescription = stringResource(R.string.action_watch_trailer),
                    tint = CineRed,
                    modifier = Modifier.size(48.dp)
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(CineBlack.copy(alpha = 0.8f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = video.type.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = CineGold
                    )
                }
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = video.name,
                    style = MaterialTheme.typography.labelLarge,
                    color = CineTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Official YouTube Video",
                    style = MaterialTheme.typography.bodySmall,
                    color = CineTextSecondary
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WhereToWatchSection(
    selectedWatchRegion: String,
    regionalProviders: RegionalWatchProviders?,
    availableRegionCodes: Set<String>,
    onSelectRegion: (String) -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(CineSurface)
            .border(1.dp, CineBorder, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(R.string.details_where_to_watch).uppercase(),
                    fontFamily = BebasNeueFontFamily,
                    fontSize = 24.sp,
                    color = CineTextPrimary
                )
                Text(
                    text = stringResource(R.string.providers_powered_by),
                    style = MaterialTheme.typography.labelSmall,
                    color = CineEmerald
                )
            }
        }

        // Country / Region Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SUPPORTED_WATCH_REGIONS.forEach { region ->
                val isSelected = selectedWatchRegion == region.code
                val hasData = availableRegionCodes.contains(region.code)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) CineRed else CineSurfaceElevated)
                        .border(
                            1.dp,
                            if (isSelected) CineRed else if (hasData) CineEmerald.copy(alpha = 0.5f) else CineBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onSelectRegion(region.code) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${region.flagEmoji} ${region.code}",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) Color.White else CineTextPrimary
                    )
                }
            }
        }

        if (regionalProviders == null || !regionalProviders.hasAnyOptions) {
            Text(
                text = stringResource(R.string.providers_none),
                style = MaterialTheme.typography.bodyMedium,
                color = CineTextSecondary
            )
        } else {
            if (regionalProviders.streamFlatrate.isNotEmpty()) {
                ProviderTierGroup(
                    label = stringResource(R.string.providers_stream),
                    providers = regionalProviders.streamFlatrate
                )
            }
            if (regionalProviders.free.isNotEmpty()) {
                ProviderTierGroup(
                    label = "Free with Ads",
                    providers = regionalProviders.free
                )
            }
            if (regionalProviders.rent.isNotEmpty()) {
                ProviderTierGroup(
                    label = stringResource(R.string.providers_rent),
                    providers = regionalProviders.rent
                )
            }
            if (regionalProviders.buy.isNotEmpty()) {
                ProviderTierGroup(
                    label = stringResource(R.string.providers_buy),
                    providers = regionalProviders.buy
                )
            }

            if (!regionalProviders.tmdbWatchLink.isNullOrBlank()) {
                OutlinedButton(
                    onClick = {
                        runCatching {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse(regionalProviders.tmdbWatchLink))
                            )
                        }
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "View All Regional Links on TMDB",
                        color = CineTextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        tint = CineRed,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProviderTierGroup(
    label: String,
    providers: List<WatchProviderOption>
) {
    val context = LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = CineGold
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            providers.forEach { provider ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(CineSurfaceElevated)
                        .border(1.dp, CineBorder, RoundedCornerShape(10.dp))
                        .clickable {
                            runCatching {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(provider.directSearchUrl))
                                )
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                ) {
                    if (provider.logoUrl != null) {
                        AsyncImage(
                            model = provider.logoUrl,
                            contentDescription = provider.providerName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                        )
                    }
                    Text(
                        text = provider.providerName,
                        style = MaterialTheme.typography.labelMedium,
                        color = CineTextPrimary
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        tint = CineRed,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TrailerPopupDialog(
    video: VideoItem,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CineSurface),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .border(1.dp, CineRed.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                .testTag("trailer_popup_dialog")
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "OFFICIAL TRAILER",
                            style = MaterialTheme.typography.labelSmall,
                            color = CineRed,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = video.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = CineTextPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.action_close),
                            tint = CineTextSecondary
                        )
                    }
                }

                // High-res YouTube Thumbnail Preview with Play Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CineBlack)
                        .clickable {
                            runCatching {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(video.youtubeWatchUrl))
                                )
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = video.youtubeThumbnailUrl,
                        contentDescription = video.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(CineBlack.copy(alpha = 0.35f))
                    )
                    Icon(
                        imageVector = Icons.Filled.PlayCircleFilled,
                        contentDescription = null,
                        tint = CineRed,
                        modifier = Modifier.size(64.dp)
                    )
                }

                Text(
                    text = "Trailers are opened via official YouTube video links returned by TMDB (${video.youtubeWatchUrl}). CineBox does not host or reproduce copyrighted video files.",
                    style = MaterialTheme.typography.bodySmall,
                    color = CineTextSecondary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = stringResource(R.string.action_close), color = CineTextPrimary)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            runCatching {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(video.youtubeWatchUrl))
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CineRed),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("open_youtube_trailer_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = stringResource(R.string.action_open_youtube))
                    }
                }
            }
        }
    }
}

@Composable
fun AboutAndSetupScreen(
    selectedTab: AboutSubSection,
    onSelectTab: (AboutSubSection) -> Unit,
    onSelectSection: (CineSection) -> Unit,
    onApiKeySaved: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var apiKeyInput by remember { mutableStateOf("") }
    var saveFeedback by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CineBlack)
            .testTag("about_screen_list"),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CineSurface)
                    .padding(horizontal = 20.dp, vertical = 22.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "ABOUT CINEBOX & TMDB CONFIGURATION",
                    fontFamily = BebasNeueFontFamily,
                    fontSize = 34.sp,
                    letterSpacing = 1.2.sp,
                    color = CineTextPrimary
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AboutSubSection.entries.forEach { sub ->
                        val selected = selectedTab == sub
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selected) CineRed else CineSurfaceElevated)
                                .clickable { onSelectTab(sub) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .testTag("about_tab_${sub.name.lowercase()}")
                        ) {
                            Text(
                                text = sub.title,
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CineSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .border(1.dp, CineBorder, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    when (selectedTab) {
                        AboutSubSection.OVERVIEW -> {
                            Text(
                                text = "CineBox — Modern Movie & TV Streaming Discovery",
                                style = MaterialTheme.typography.headlineSmall,
                                color = CineTextPrimary
                            )
                            Text(
                                text = "CineBox is a legal streaming-discovery platform and cinema guide powered by the official TMDB API (https://api.themoviedb.org/3). Discover trending movies, popular TV shows, top rated classics, upcoming releases, cast and crew credits, official YouTube trailers, and regional Where-to-Watch streaming availability.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = CineTextSecondary
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.VerifiedUser,
                                    contentDescription = null,
                                    tint = CineEmerald
                                )
                                Text(
                                    text = "100% Legal Streaming Discovery: Links direct users to legitimate streaming services and official YouTube trailers.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = CineTextPrimary
                                )
                            }
                        }

                        AboutSubSection.API_CONFIG -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Key,
                                    contentDescription = null,
                                    tint = CineRed
                                )
                                Text(
                                    text = "How to Configure Your TMDB_API_KEY",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = CineTextPrimary
                                )
                            }

                            val statusText = if (TmdbConfig.isApiKeyConfigured) {
                                "STATUS: TMDB_API_KEY is configured and active."
                            } else {
                                "STATUS: Waiting for your TMDB_API_KEY (paste below, or add in AI Studio Secrets / .env)."
                            }
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (TmdbConfig.isApiKeyConfigured) CineEmerald else CineGold
                            )

                            OutlinedTextField(
                                value = apiKeyInput,
                                onValueChange = {
                                    apiKeyInput = it
                                    saveFeedback = null
                                },
                                label = { Text("Paste TMDB_API_KEY Here") },
                                placeholder = { Text("32-character TMDB v3 hex key or v4 JWT Read Access Token") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CineRed,
                                    unfocusedBorderColor = CineBorder,
                                    focusedTextColor = CineTextPrimary,
                                    unfocusedTextColor = CineTextPrimary,
                                    focusedLabelColor = CineGold,
                                    unfocusedLabelColor = CineTextSecondary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("about_tmdb_api_key_input")
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = {
                                        val trimmed = apiKeyInput.trim()
                                        if (TmdbConfig.isValidTmdbKeyFormat(trimmed)) {
                                            TmdbConfig.saveRuntimeApiKey(context, trimmed)
                                            saveFeedback = "TMDB_API_KEY saved! Loading live TMDB catalog..."
                                            onApiKeySaved()
                                        } else {
                                            saveFeedback = "Invalid format. Please paste a 32-character hex TMDB v3 API key or a TMDB v4 JWT Read Access Token."
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CineRed),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("save_tmdb_api_key_button")
                                ) {
                                    Text(text = "Save & Connect TMDB API")
                                }

                                if (TmdbConfig.isApiKeyConfigured) {
                                    OutlinedButton(
                                        onClick = {
                                            TmdbConfig.saveRuntimeApiKey(context, "")
                                            apiKeyInput = ""
                                            saveFeedback = "Cleared saved in-app override key."
                                        },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(text = "Clear Saved Key", color = CineTextSecondary)
                                    }
                                }
                            }

                            saveFeedback?.let { msg ->
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (msg.startsWith("TMDB_API_KEY saved")) CineEmerald else CineGold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(CineBlack)
                                    .border(1.dp, CineBorder, RoundedCornerShape(10.dp))
                                    .padding(14.dp)
                            ) {
                                Text(
                                    text = "# 1. In AI Studio Secrets Panel (Recommended):\n" +
                                        "Secret Name: TMDB_API_KEY\n" +
                                        "Secret Value: <your_official_tmdb_v3_api_key>\n\n" +
                                        "# 2. Or in /.env (copied from /.env.example):\n" +
                                        "TMDB_API_KEY=PASTE_YOUR_TMDB_API_KEY_HERE\n\n" +
                                        "# 3. Code Configuration Reference:\n" +
                                        "app/src/main/java/com/example/config/TmdbConfig.kt",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp,
                                    color = CineTextPrimary
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Security,
                                    contentDescription = null,
                                    tint = CineGold
                                )
                                Text(
                                    text = "Security Note: Client-side API keys packaged into mobile/web builds are public/client-side keys. For production deployments, route TMDB requests through a backend proxy (such as Cloud Functions or Cloud Run) so your API key remains server-side.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CineTextSecondary
                                )
                            }
                        }

                        AboutSubSection.PRIVACY -> {
                            Text(
                                text = "Privacy Policy",
                                style = MaterialTheme.typography.headlineSmall,
                                color = CineTextPrimary
                            )
                            Text(
                                text = "CineBox stores your 'My List' favorites locally on your device using Android Room SQLite storage. No personal account data, viewing history, or tracking cookies are sold or transmitted to third-party ad networks. Search queries and catalog requests are sent directly to the official TMDB API (api.themoviedb.org).",
                                style = MaterialTheme.typography.bodyLarge,
                                color = CineTextSecondary
                            )
                        }

                        AboutSubSection.TERMS -> {
                            Text(
                                text = "Terms of Use",
                                style = MaterialTheme.typography.headlineSmall,
                                color = CineTextPrimary
                            )
                            Text(
                                text = "CineBox is a movie and TV discovery guide. All movie metadata, posters, backdrops, ratings, and watch-provider availability are provided via the official TMDB API. CineBox does not host, stream, pirate, or distribute copyrighted movie or television video files.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = CineTextSecondary
                            )
                        }

                        AboutSubSection.CONTACT -> {
                            Text(
                                text = "Contact & Official TMDB Attribution",
                                style = MaterialTheme.typography.headlineSmall,
                                color = CineTextPrimary
                            )
                            Text(
                                text = "This product uses the TMDB API but is not endorsed or certified by TMDB. Regional streaming provider availability is powered by JustWatch through TMDB.\n\nSupport & Inquiries: support@cinebox-discovery.example",
                                style = MaterialTheme.typography.bodyLarge,
                                color = CineTextSecondary
                            )
                        }
                    }
                }
            }
        }

        item {
            CineFooter(
                onSelectSection = onSelectSection,
                onOpenAboutTab = onSelectTab
            )
        }
    }
}
