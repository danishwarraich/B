package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.domain.model.CineSection
import com.example.domain.model.MediaItem
import com.example.domain.model.MediaType
import com.example.domain.model.SUPPORTED_WATCH_REGIONS
import com.example.domain.model.SortOption
import com.example.ui.components.ApiNoticeAndErrorCard
import com.example.ui.components.CineFooter
import com.example.ui.components.MediaPosterCard
import com.example.ui.components.ShimmerPosterPlaceholder
import com.example.ui.theme.BebasNeueFontFamily
import com.example.ui.theme.CineBlack
import com.example.ui.theme.CineBorder
import com.example.ui.theme.CineRed
import com.example.ui.theme.CineSurface
import com.example.ui.theme.CineSurfaceElevated
import com.example.ui.theme.CineTextMuted
import com.example.ui.theme.CineTextPrimary
import com.example.ui.theme.CineTextSecondary
import com.example.ui.viewmodel.AboutSubSection
import com.example.ui.viewmodel.CatalogGridState
import com.example.ui.viewmodel.SearchUiState

@Composable
fun CatalogBrowseScreen(
    section: CineSection,
    catalogState: CatalogGridState,
    selectedWatchRegion: String,
    favoriteIds: Set<String>,
    onMediaClick: (MediaItem) -> Unit,
    onToggleFavorite: (MediaItem) -> Unit,
    onSelectMediaType: (MediaType) -> Unit,
    onSelectSort: (SortOption) -> Unit,
    onSelectGenre: (Int?) -> Unit,
    onSelectWatchProvider: (Int?) -> Unit,
    onSelectWatchRegion: (String) -> Unit,
    onLoadMore: () -> Unit,
    onSelectSection: (CineSection) -> Unit,
    onOpenAboutTab: (AboutSubSection) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val showMediaTypeSwitcher = section in listOf(
        CineSection.POPULAR,
        CineSection.TOP_RATED,
        CineSection.GENRES,
        CineSection.WATCH_PROVIDERS
    )
    val showSortBar = section in listOf(
        CineSection.MOVIES,
        CineSection.TV_SHOWS,
        CineSection.GENRES,
        CineSection.WATCH_PROVIDERS
    )
    val showGenreFilterRow = section in listOf(
        CineSection.MOVIES,
        CineSection.TV_SHOWS,
        CineSection.GENRES
    )

    val activeGenres = if (catalogState.selectedMediaType == MediaType.TV && section != CineSection.MOVIES) {
        catalogState.tvGenres
    } else {
        catalogState.movieGenres
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CineBlack)
            .testTag("catalog_screen_${section.name.lowercase()}"),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Section Banner Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CineSurface)
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(5.dp)
                            .height(28.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(CineRed)
                    )
                    Text(
                        text = section.title.uppercase(),
                        fontFamily = BebasNeueFontFamily,
                        fontSize = 34.sp,
                        letterSpacing = 1.2.sp,
                        color = CineTextPrimary
                    )
                }

                val subtitle = when (section) {
                    CineSection.MOVIES -> "Explore theatrical releases, blockbusters, and indie films with genre & rating filters."
                    CineSection.TV_SHOWS -> "Discover critically acclaimed television series, miniseries, and global shows."
                    CineSection.TRENDING -> "Real-time trending movies and TV shows across the globe on TMDB."
                    CineSection.POPULAR -> "What audiences are watching right now across theaters and streaming."
                    CineSection.TOP_RATED -> "All-time highest rated movies and television series voted by fans."
                    CineSection.UPCOMING -> "Upcoming theatrical releases and anticipated premieres."
                    CineSection.GENRES -> "Filter the TMDB catalog by genre, media type, rating, and release date."
                    CineSection.WATCH_PROVIDERS -> "Browse official streaming availability by country/region and streaming service."
                    else -> "Explore the official TMDB catalog."
                }

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CineTextSecondary
                )

                // Movie / TV Show Switcher when applicable
                if (showMediaTypeSwitcher) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MediaType.entries.forEach { type ->
                            val selected = catalogState.selectedMediaType == type
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (selected) CineRed else CineSurfaceElevated)
                                    .border(
                                        1.dp,
                                        if (selected) CineRed else CineBorder,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { onSelectMediaType(type) }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .testTag("catalog_type_${type.name.lowercase()}")
                            ) {
                                Text(
                                    text = if (type == MediaType.MOVIE) "Movies" else "TV Shows",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (selected) Color.White else CineTextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Watch Providers Region & Service Selector (when on WHERE TO WATCH section)
        if (section == CineSection.WATCH_PROVIDERS) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "1. Select Country / Streaming Region",
                        style = MaterialTheme.typography.titleSmall,
                        color = CineTextPrimary
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SUPPORTED_WATCH_REGIONS.forEach { region ->
                            val selected = selectedWatchRegion == region.code
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (selected) CineRed else CineSurfaceElevated)
                                    .border(
                                        1.dp,
                                        if (selected) CineRed else CineBorder,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { onSelectWatchRegion(region.code) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .testTag("region_chip_${region.code}")
                            ) {
                                Text(
                                    text = "${region.flagEmoji} ${region.name} (${region.code})",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (selected) Color.White else CineTextSecondary
                                )
                            }
                        }
                    }

                    if (catalogState.regionalProviders.isNotEmpty()) {
                        Text(
                            text = "2. Filter by Streaming Service (or tap Open Service to visit official platform)",
                            style = MaterialTheme.typography.titleSmall,
                            color = CineTextPrimary
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item {
                                val allSelected = catalogState.selectedWatchProviderId == null
                                Box(
                                    modifier = Modifier
                                        .height(64.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (allSelected) CineRed else CineSurfaceElevated)
                                        .border(
                                            1.dp,
                                            if (allSelected) CineRed else CineBorder,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable { onSelectWatchProvider(null) }
                                        .padding(horizontal = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "All Services",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = Color.White
                                    )
                                }
                            }

                            items(catalogState.regionalProviders, key = { it.providerId }) { provider ->
                                val isSelected = catalogState.selectedWatchProviderId == provider.providerId
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier
                                        .height(64.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) CineRed.copy(alpha = 0.22f) else CineSurfaceElevated)
                                        .border(
                                            1.5.dp,
                                            if (isSelected) CineRed else CineBorder,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable { onSelectWatchProvider(provider.providerId) }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                        .testTag("provider_card_${provider.providerId}")
                                ) {
                                    if (provider.logoUrl != null) {
                                        AsyncImage(
                                            model = provider.logoUrl,
                                            contentDescription = provider.providerName,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Filled.PlayCircleFilled,
                                            contentDescription = null,
                                            tint = CineRed,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = provider.providerName,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = CineTextPrimary,
                                            maxLines = 1
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                                            modifier = Modifier.clickable {
                                                runCatching {
                                                    context.startActivity(
                                                        Intent(Intent.ACTION_VIEW, Uri.parse(provider.directSearchUrl))
                                                    )
                                                }
                                            }
                                        ) {
                                            Text(
                                                text = "Visit Site",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = CineRed
                                            )
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                                contentDescription = null,
                                                tint = CineRed,
                                                modifier = Modifier.size(11.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Genre Filter Chips Row
        if (showGenreFilterRow && activeGenres.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FilterList,
                            contentDescription = null,
                            tint = CineRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Filter by Genre",
                            style = MaterialTheme.typography.labelLarge,
                            color = CineTextSecondary
                        )
                    }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            val allSelected = catalogState.selectedGenreId == null
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (allSelected) CineRed else CineSurfaceElevated)
                                    .border(
                                        1.dp,
                                        if (allSelected) CineRed else CineBorder,
                                        RoundedCornerShape(20.dp)
                                    )
                                    .clickable { onSelectGenre(null) }
                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                                    .testTag("genre_filter_all")
                            ) {
                                Text(
                                    text = stringResource(R.string.filter_all_genres),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (allSelected) Color.White else CineTextSecondary
                                )
                            }
                        }

                        items(activeGenres, key = { it.id }) { genre ->
                            val isSelected = catalogState.selectedGenreId == genre.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) CineRed else CineSurfaceElevated)
                                    .border(
                                        1.dp,
                                        if (isSelected) CineRed else CineBorder,
                                        RoundedCornerShape(20.dp)
                                    )
                                    .clickable { onSelectGenre(genre.id) }
                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                                    .testTag("genre_filter_${genre.id}")
                            ) {
                                Text(
                                    text = genre.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSelected) Color.White else CineTextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Sort Controls Row (Sort by Popularity / Rating / Release Date)
        if (showSortBar) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Sort,
                        contentDescription = null,
                        tint = CineRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Sort by:",
                        style = MaterialTheme.typography.labelLarge,
                        color = CineTextSecondary
                    )
                    SortOption.entries.forEach { option ->
                        val isSelected = catalogState.selectedSort == option
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) CineRed else CineSurface)
                                .border(
                                    1.dp,
                                    if (isSelected) CineRed else CineBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { onSelectSort(option) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("sort_option_${option.name.lowercase()}")
                        ) {
                            Text(
                                text = option.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) Color.White else CineTextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Error or API Key Notice
        if (catalogState.errorMessage != null) {
            item {
                ApiNoticeAndErrorCard(
                    message = catalogState.errorMessage,
                    onRetry = onRetry,
                    onOpenSetupGuide = { onOpenAboutTab(AboutSubSection.API_CONFIG) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        // Responsive Poster Grid + Load More Pagination
        item {
            ResponsivePosterGridWithLoadMore(
                items = catalogState.items,
                isLoading = catalogState.isLoading,
                isAppending = catalogState.isAppending,
                canLoadMore = catalogState.canLoadMore,
                favoriteIds = favoriteIds,
                onMediaClick = onMediaClick,
                onToggleFavorite = onToggleFavorite,
                onLoadMore = onLoadMore
            )
        }

        // Footer
        item {
            CineFooter(
                onSelectSection = onSelectSection,
                onOpenAboutTab = onOpenAboutTab
            )
        }
    }
}

@Composable
fun SearchScreen(
    searchState: SearchUiState,
    favoriteIds: Set<String>,
    onMediaClick: (MediaItem) -> Unit,
    onToggleFavorite: (MediaItem) -> Unit,
    onLoadMore: () -> Unit,
    onClearSearch: () -> Unit,
    onSelectSection: (CineSection) -> Unit,
    onOpenAboutTab: (AboutSubSection) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CineBlack)
            .testTag("search_screen_list"),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CineSurface)
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = if (searchState.query.isBlank()) {
                        "SEARCH MOVIES & TV SHOWS"
                    } else {
                        "SEARCH RESULTS FOR \"${searchState.query.uppercase()}\""
                    },
                    fontFamily = BebasNeueFontFamily,
                    fontSize = 30.sp,
                    letterSpacing = 1.sp,
                    color = CineTextPrimary
                )
                Text(
                    text = "Real-time multi-search powered by the official TMDB API (Posters, Release Year, Ratings, Movie/TV type).",
                    style = MaterialTheme.typography.bodySmall,
                    color = CineTextSecondary
                )
            }
        }

        if (searchState.errorMessage != null) {
            item {
                ApiNoticeAndErrorCard(
                    message = searchState.errorMessage,
                    onRetry = onRetry,
                    onOpenSetupGuide = { onOpenAboutTab(AboutSubSection.API_CONFIG) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        // Empty or Initial State
        if (!searchState.isLoading && searchState.results.isEmpty() && searchState.errorMessage == null) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp, vertical = 56.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(CineSurfaceElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (searchState.hasSearched) Icons.Filled.SearchOff else Icons.Filled.Search,
                            contentDescription = null,
                            tint = CineRed,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Text(
                        text = stringResource(
                            if (searchState.hasSearched) R.string.search_empty_title
                            else R.string.search_prompt_title
                        ),
                        style = MaterialTheme.typography.headlineSmall,
                        color = CineTextPrimary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = stringResource(
                            if (searchState.hasSearched) R.string.search_empty_subtitle
                            else R.string.search_prompt_subtitle
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = CineTextSecondary,
                        textAlign = TextAlign.Center
                    )
                    if (searchState.hasSearched && searchState.query.isNotEmpty()) {
                        OutlinedButton(
                            onClick = onClearSearch,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("empty_search_clear_button")
                        ) {
                            Text(
                                text = stringResource(R.string.action_clear_search),
                                color = CineTextPrimary
                            )
                        }
                    }
                }
            }
        } else {
            item {
                ResponsivePosterGridWithLoadMore(
                    items = searchState.results,
                    isLoading = searchState.isLoading,
                    isAppending = searchState.isAppending,
                    canLoadMore = searchState.canLoadMore,
                    favoriteIds = favoriteIds,
                    onMediaClick = onMediaClick,
                    onToggleFavorite = onToggleFavorite,
                    onLoadMore = onLoadMore
                )
            }
        }

        item {
            CineFooter(
                onSelectSection = onSelectSection,
                onOpenAboutTab = onOpenAboutTab
            )
        }
    }
}

@Composable
fun MyListScreen(
    favorites: List<MediaItem>,
    selectedFilterType: MediaType?,
    selectedSortOption: SortOption,
    onSelectFilterType: (MediaType?) -> Unit,
    onSelectSortOption: (SortOption) -> Unit,
    onMediaClick: (MediaItem) -> Unit,
    onToggleFavorite: (MediaItem) -> Unit,
    onSelectSection: (CineSection) -> Unit,
    onOpenAboutTab: (AboutSubSection) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredFavorites = favorites
        .filter { selectedFilterType == null || it.mediaType == selectedFilterType }
        .let { list ->
            when (selectedSortOption) {
                SortOption.POPULARITY -> list
                SortOption.RATING -> list.sortedByDescending { it.rating }
                SortOption.RELEASE_DATE -> list.sortedByDescending { it.fullReleaseDate }
            }
        }

    val favoriteIds = favorites.map { "${it.mediaType.name}_${it.id}" }.toSet()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CineBlack)
            .testTag("my_list_screen"),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CineSurface)
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "MY LIST & FAVORITES (${favorites.size})",
                    fontFamily = BebasNeueFontFamily,
                    fontSize = 34.sp,
                    letterSpacing = 1.2.sp,
                    color = CineTextPrimary
                )
                Text(
                    text = "Saved locally on your device for instant access.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CineTextSecondary
                )

                // Filter & Sort Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val filterOptions: List<Pair<MediaType?, String>> = listOf(
                        null to "All (${favorites.size})",
                        MediaType.MOVIE to "Movies",
                        MediaType.TV to "TV Shows"
                    )
                    filterOptions.forEach { (type, label) ->
                        val selected = selectedFilterType == type
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) CineRed else CineSurfaceElevated)
                                .clickable { onSelectFilterType(type) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    SortOption.entries.forEach { sort ->
                        val selected = selectedSortOption == sort
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) CineSurfaceElevated else CineBlack)
                                .border(1.dp, if (selected) CineRed else CineBorder, RoundedCornerShape(8.dp))
                                .clickable { onSelectSortOption(sort) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = sort.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected) CineRed else CineTextSecondary
                            )
                        }
                    }
                }
            }
        }

        if (filteredFavorites.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp, vertical = 56.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(CineSurfaceElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.BookmarkBorder,
                            contentDescription = null,
                            tint = CineRed,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Text(
                        text = stringResource(R.string.my_list_empty_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = CineTextPrimary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = stringResource(R.string.my_list_empty_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = CineTextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = { onSelectSection(CineSection.TRENDING) },
                        colors = ButtonDefaults.buttonColors(containerColor = CineRed),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("explore_trending_from_empty_list")
                    ) {
                        Text(text = "Browse Trending Titles")
                    }
                }
            }
        } else {
            item {
                ResponsivePosterGridWithLoadMore(
                    items = filteredFavorites,
                    isLoading = false,
                    isAppending = false,
                    canLoadMore = false,
                    favoriteIds = favoriteIds,
                    onMediaClick = onMediaClick,
                    onToggleFavorite = onToggleFavorite,
                    onLoadMore = {}
                )
            }
        }

        item {
            CineFooter(
                onSelectSection = onSelectSection,
                onOpenAboutTab = onOpenAboutTab
            )
        }
    }
}

@Composable
private fun ResponsivePosterGridWithLoadMore(
    items: List<MediaItem>,
    isLoading: Boolean,
    isAppending: Boolean,
    canLoadMore: Boolean,
    favoriteIds: Set<String>,
    onMediaClick: (MediaItem) -> Unit,
    onToggleFavorite: (MediaItem) -> Unit,
    onLoadMore: () -> Unit
) {
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

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading && items.isEmpty()) {
                repeat(3) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        repeat(columns) {
                            ShimmerPosterPlaceholder(modifier = Modifier.weight(1f))
                        }
                    }
                }
            } else if (items.isNotEmpty()) {
                val rows = items.chunked(columns)
                rows.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowItems.forEach { item ->
                            val isFav = favoriteIds.contains("${item.mediaType.name}_${item.id}")
                            MediaPosterCard(
                                item = item,
                                isFavorite = isFav,
                                onClick = { onMediaClick(item) },
                                onToggleFavorite = { onToggleFavorite(item) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        repeat(columns - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                if (canLoadMore) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onLoadMore,
                        enabled = !isAppending,
                        colors = ButtonDefaults.buttonColors(containerColor = CineSurfaceElevated),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 28.dp, vertical = 14.dp),
                        modifier = Modifier
                            .border(1.dp, CineRed, RoundedCornerShape(12.dp))
                            .testTag("load_more_button")
                    ) {
                        if (isAppending) {
                            CircularProgressIndicator(
                                color = CineRed,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                        }
                        Text(
                            text = stringResource(R.string.action_load_more),
                            style = MaterialTheme.typography.labelLarge,
                            color = CineTextPrimary
                        )
                    }
                }
            }
        }
    }
}
