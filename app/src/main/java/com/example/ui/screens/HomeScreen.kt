package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.domain.model.CineSection
import com.example.domain.model.GenreItem
import com.example.domain.model.MediaDetails
import com.example.domain.model.MediaItem
import com.example.domain.model.MediaType
import com.example.ui.components.ApiNoticeAndErrorCard
import com.example.ui.components.CineFooter
import com.example.ui.components.MediaPosterCard
import com.example.ui.components.ShimmerPosterPlaceholder
import com.example.ui.theme.BebasNeueFontFamily
import com.example.ui.theme.CineBlack
import com.example.ui.theme.CineBorder
import com.example.ui.theme.CineGold
import com.example.ui.theme.CineRed
import com.example.ui.theme.CineSurface
import com.example.ui.theme.CineSurfaceElevated
import com.example.ui.theme.CineTextPrimary
import com.example.ui.theme.CineTextSecondary
import com.example.ui.viewmodel.AboutSubSection
import com.example.ui.viewmodel.HomeFeedState
import java.util.Locale

@Composable
fun HomeScreen(
    homeState: HomeFeedState,
    favoriteIds: Set<String>,
    movieGenres: List<GenreItem>,
    onMediaClick: (MediaItem) -> Unit,
    onWatchHeroTrailer: () -> Unit,
    onToggleFavorite: (MediaItem) -> Unit,
    onSelectSection: (CineSection) -> Unit,
    onSelectGenre: (Int) -> Unit,
    onOpenAboutTab: (AboutSubSection) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CineBlack)
            .testTag("home_screen_list"),
        verticalArrangement = Arrangement.spacedBy(26.dp)
    ) {
        // 1. Large Featured Movie Hero Section
        item {
            HeroBannerSection(
                heroItem = homeState.featuredHero,
                heroDetails = homeState.featuredHeroDetails,
                isFavorite = homeState.featuredHero?.let {
                    favoriteIds.contains("${it.mediaType.name}_${it.id}")
                } == true,
                onWatchTrailer = onWatchHeroTrailer,
                onMoreInfo = {
                    homeState.featuredHero?.let(onMediaClick)
                        ?: onOpenAboutTab(AboutSubSection.API_CONFIG)
                },
                onToggleFavorite = {
                    homeState.featuredHero?.let(onToggleFavorite)
                }
            )
        }

        // 2. API Notice / Error State if TMDB_API_KEY is not yet configured or network failed
        if (homeState.errorMessage != null) {
            item {
                ApiNoticeAndErrorCard(
                    message = homeState.errorMessage,
                    onRetry = onRetry,
                    onOpenSetupGuide = { onOpenAboutTab(AboutSubSection.API_CONFIG) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        // 3. Quick Genre Discovery Strip
        if (movieGenres.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionHeaderRow(
                        title = stringResource(R.string.nav_genres),
                        subtitle = "Browse by category",
                        onSeeAll = { onSelectSection(CineSection.GENRES) }
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(movieGenres, key = { it.id }) { genre ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(CineSurfaceElevated)
                                    .border(1.dp, CineBorder, RoundedCornerShape(20.dp))
                                    .clickable { onSelectGenre(genre.id) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                                    .testTag("home_genre_chip_${genre.id}")
                            ) {
                                Text(
                                    text = genre.name,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = CineTextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Trending Movies Horizontal Carousel
        item {
            HorizontalMediaCarouselSection(
                title = stringResource(R.string.section_trending_movies),
                subtitle = "This week's biggest box office & streaming hits",
                items = homeState.trendingMovies,
                isLoading = homeState.isLoading,
                favoriteIds = favoriteIds,
                onMediaClick = onMediaClick,
                onToggleFavorite = onToggleFavorite,
                onSeeAll = { onSelectSection(CineSection.TRENDING) }
            )
        }

        // 5. Popular Movies Responsive Grid Section
        item {
            PopularMoviesGridSection(
                title = stringResource(R.string.section_popular_movies),
                subtitle = "Most watched movies across global streaming platforms",
                items = homeState.popularMovies.take(12),
                isLoading = homeState.isLoading,
                favoriteIds = favoriteIds,
                onMediaClick = onMediaClick,
                onToggleFavorite = onToggleFavorite,
                onSeeAll = { onSelectSection(CineSection.POPULAR) }
            )
        }

        // 6. Top Rated Movies Carousel Section
        item {
            HorizontalMediaCarouselSection(
                title = stringResource(R.string.section_top_rated),
                subtitle = "Critically acclaimed cinema masterpieces on TMDB",
                items = homeState.topRatedMovies,
                isLoading = homeState.isLoading,
                favoriteIds = favoriteIds,
                onMediaClick = onMediaClick,
                onToggleFavorite = onToggleFavorite,
                onSeeAll = { onSelectSection(CineSection.TOP_RATED) }
            )
        }

        // 7. Upcoming Movies Section
        item {
            HorizontalMediaCarouselSection(
                title = stringResource(R.string.section_upcoming),
                subtitle = "Coming soon to theaters & digital release",
                items = homeState.upcomingMovies,
                isLoading = homeState.isLoading,
                favoriteIds = favoriteIds,
                onMediaClick = onMediaClick,
                onToggleFavorite = onToggleFavorite,
                onSeeAll = { onSelectSection(CineSection.UPCOMING) }
            )
        }

        // 8. Popular TV Shows Section
        item {
            HorizontalMediaCarouselSection(
                title = stringResource(R.string.section_popular_tv),
                subtitle = "Binge-worthy series & trending television",
                items = homeState.popularTvShows,
                isLoading = homeState.isLoading,
                favoriteIds = favoriteIds,
                onMediaClick = onMediaClick,
                onToggleFavorite = onToggleFavorite,
                onSeeAll = { onSelectSection(CineSection.TV_SHOWS) }
            )
        }

        // 9. Footer with TMDB Attribution & Legal Links
        item {
            CineFooter(
                onSelectSection = onSelectSection,
                onOpenAboutTab = onOpenAboutTab
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HeroBannerSection(
    heroItem: MediaItem?,
    heroDetails: MediaDetails?,
    isFavorite: Boolean,
    onWatchTrailer: () -> Unit,
    onMoreInfo: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(440.dp)
            .background(CineSurface)
            .testTag("hero_banner_section")
    ) {
        // Backdrop image (TMDB backdrop when loaded, or our custom cinema fallback art)
        val backdropUrl = heroDetails?.backdropUrl ?: heroItem?.backdropUrl ?: heroItem?.posterUrl
        if (backdropUrl != null) {
            AsyncImage(
                model = backdropUrl,
                contentDescription = heroItem?.title ?: "Featured Movie",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.img_hero_backdrop_1790404514620),
                contentDescription = stringResource(R.string.app_name),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Multi-stop cinematic vignette overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            CineBlack.copy(alpha = 0.35f),
                            CineBlack.copy(alpha = 0.55f),
                            CineBlack.copy(alpha = 0.92f),
                            CineBlack
                        )
                    )
                )
        )

        // Hero Content Overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Featured Spotlight Badge + Rating + Year
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(CineRed)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocalFireDepartment,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (heroItem != null) "FEATURED SPOTLIGHT" else "CINEBOX STREAMING GUIDE",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (heroItem != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CineSurfaceElevated.copy(alpha = 0.85f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = CineGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = String.format(Locale.US, "%.1f", heroItem.rating),
                            style = MaterialTheme.typography.labelMedium,
                            color = CineTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = heroItem.releaseYear,
                        style = MaterialTheme.typography.labelLarge,
                        color = CineTextSecondary
                    )

                    if (heroDetails != null && heroDetails.runtimeFormatted.isNotBlank()) {
                        Text(
                            text = "• ${heroDetails.runtimeFormatted}",
                            style = MaterialTheme.typography.labelLarge,
                            color = CineTextSecondary
                        )
                    }
                }
            }

            // Large Cinematic Hero Title
            Text(
                text = (heroItem?.title ?: "DISCOVER CINEMA WITHOUT LIMITS").uppercase(),
                fontFamily = BebasNeueFontFamily,
                fontSize = 42.sp,
                lineHeight = 44.sp,
                letterSpacing = 1.2.sp,
                color = CineTextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Genre Badges if available
            if (heroDetails != null && heroDetails.genres.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    heroDetails.genres.take(4).forEach { genre ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(CineSurfaceElevated.copy(alpha = 0.8f))
                                .border(1.dp, CineBorder, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = genre.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = CineTextSecondary
                            )
                        }
                    }
                }
            }

            // Overview Synopsis
            Text(
                text = heroItem?.overview
                    ?: "Explore trending movies, binge-worthy TV series, official trailers, cast credits, and legal streaming availability across regions powered by the official TMDB API.",
                style = MaterialTheme.typography.bodyMedium,
                color = CineTextSecondary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Primary Hero CTA Buttons: Watch Trailer + More Info + My List
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onWatchTrailer,
                    enabled = heroItem != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CineRed,
                        contentColor = Color.White,
                        disabledContainerColor = CineRed.copy(alpha = 0.45f),
                        disabledContentColor = Color.White.copy(alpha = 0.7f)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
                    modifier = Modifier.testTag("hero_watch_trailer_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.action_watch_trailer),
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                OutlinedButton(
                    onClick = onMoreInfo,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    modifier = Modifier
                        .background(CineSurfaceElevated.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                        .testTag("hero_more_info_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = CineTextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.action_more_info),
                        style = MaterialTheme.typography.labelLarge,
                        color = CineTextPrimary
                    )
                }

                if (heroItem != null) {
                    OutlinedButton(
                        onClick = onToggleFavorite,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                        modifier = Modifier
                            .background(CineSurfaceElevated.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                            .testTag("hero_my_list_button")
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Check else Icons.Filled.Add,
                            contentDescription = null,
                            tint = if (isFavorite) CineGold else CineTextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(
                                if (isFavorite) R.string.action_in_my_list else R.string.action_add_my_list
                            ),
                            style = MaterialTheme.typography.labelLarge,
                            color = CineTextPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeaderRow(
    title: String,
    subtitle: String,
    onSeeAll: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(22.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(CineRed)
                )
                Text(
                    text = title.uppercase(),
                    fontFamily = BebasNeueFontFamily,
                    fontSize = 25.sp,
                    letterSpacing = 1.sp,
                    color = CineTextPrimary
                )
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = CineTextSecondary
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onSeeAll)
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Text(
                text = "Explore All",
                style = MaterialTheme.typography.labelMedium,
                color = CineRed,
                fontWeight = FontWeight.Bold
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = CineRed,
                modifier = Modifier.size(15.dp)
            )
        }
    }
}

@Composable
private fun HorizontalMediaCarouselSection(
    title: String,
    subtitle: String,
    items: List<MediaItem>,
    isLoading: Boolean,
    favoriteIds: Set<String>,
    onMediaClick: (MediaItem) -> Unit,
    onToggleFavorite: (MediaItem) -> Unit,
    onSeeAll: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeaderRow(
            title = title,
            subtitle = subtitle,
            onSeeAll = onSeeAll
        )

        if (isLoading && items.isEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(6) {
                    ShimmerPosterPlaceholder(modifier = Modifier.width(152.dp))
                }
            }
        } else if (items.isNotEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items, key = { "${it.mediaType}_${it.id}" }) { media ->
                    val isFav = favoriteIds.contains("${media.mediaType.name}_${media.id}")
                    MediaPosterCard(
                        item = media,
                        isFavorite = isFav,
                        onClick = { onMediaClick(media) },
                        onToggleFavorite = { onToggleFavorite(media) },
                        modifier = Modifier.width(152.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PopularMoviesGridSection(
    title: String,
    subtitle: String,
    items: List<MediaItem>,
    isLoading: Boolean,
    favoriteIds: Set<String>,
    onMediaClick: (MediaItem) -> Unit,
    onToggleFavorite: (MediaItem) -> Unit,
    onSeeAll: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeaderRow(
            title = title,
            subtitle = subtitle,
            onSeeAll = onSeeAll
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            val columns = when {
                maxWidth >= 900.dp -> 6
                maxWidth >= 680.dp -> 4
                maxWidth >= 420.dp -> 3
                else -> 2
            }

            if (isLoading && items.isEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    repeat(2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            repeat(columns) {
                                ShimmerPosterPlaceholder(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            } else if (items.isNotEmpty()) {
                val chunkedRows = items.chunked(columns)
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    chunkedRows.forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowItems.forEach { media ->
                                val isFav = favoriteIds.contains("${media.mediaType.name}_${media.id}")
                                MediaPosterCard(
                                    item = media,
                                    isFavorite = isFav,
                                    onClick = { onMediaClick(media) },
                                    onToggleFavorite = { onToggleFavorite(media) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            repeat(columns - rowItems.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}
