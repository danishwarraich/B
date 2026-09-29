package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.domain.model.CineSection
import com.example.ui.components.CineBottomNavigationBar
import com.example.ui.components.CineStickyTopBar
import com.example.ui.screens.AboutAndSetupScreen
import com.example.ui.screens.CatalogBrowseScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MediaDetailsOverlayPage
import com.example.ui.screens.MyListScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.TrailerPopupDialog
import com.example.ui.theme.CineBlack
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.CineBoxViewModel
import com.example.ui.viewmodel.DetailsUiState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CineBoxApp()
            }
        }
    }
}

@Composable
fun CineBoxApp() {
    val context = LocalContext.current
    val viewModel: CineBoxViewModel = viewModel(
        factory = CineBoxViewModel.provideFactory(context)
    )

    val currentSection by viewModel.currentSection.collectAsStateWithLifecycle()
    val selectedWatchRegion by viewModel.selectedWatchRegion.collectAsStateWithLifecycle()
    val homeState by viewModel.homeState.collectAsStateWithLifecycle()
    val catalogState by viewModel.catalogState.collectAsStateWithLifecycle()
    val searchState by viewModel.searchState.collectAsStateWithLifecycle()
    val detailsState by viewModel.detailsState.collectAsStateWithLifecycle()
    val activeTrailer by viewModel.activeTrailer.collectAsStateWithLifecycle()
    val aboutSubSection by viewModel.aboutSubSection.collectAsStateWithLifecycle()
    val favorites by viewModel.favoriteItems.collectAsStateWithLifecycle()
    val myListFilterType by viewModel.myListFilterType.collectAsStateWithLifecycle()
    val myListSortOption by viewModel.myListSortOption.collectAsStateWithLifecycle()

    val favoriteIds = remember(favorites) {
        favorites.map { "${it.mediaType.name}_${it.id}" }.toSet()
    }

    val canInterceptBack = currentSection != CineSection.HOME ||
        detailsState !is DetailsUiState.Idle ||
        activeTrailer != null

    BackHandler(enabled = canInterceptBack) {
        viewModel.navigateBack()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = CineBlack,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                CineStickyTopBar(
                    currentSection = currentSection,
                    searchQuery = searchState.query,
                    favoritesCount = favorites.size,
                    onSelectSection = { section -> viewModel.navigateToSection(section) },
                    onSearchQueryChange = { q -> viewModel.onSearchQueryChanged(q) },
                    onSearchSubmit = { viewModel.submitSearchNow() },
                    onClearSearch = { viewModel.clearSearch() }
                )
            },
            bottomBar = {
                CineBottomNavigationBar(
                    currentSection = currentSection,
                    onSelectSection = { section -> viewModel.navigateToSection(section) }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentSection) {
                    CineSection.HOME -> {
                        HomeScreen(
                            homeState = homeState,
                            favoriteIds = favoriteIds,
                            movieGenres = catalogState.movieGenres,
                            onMediaClick = { item -> viewModel.openMediaDetails(item) },
                            onWatchHeroTrailer = { viewModel.openHeroTrailer() },
                            onToggleFavorite = { item -> viewModel.toggleFavorite(item) },
                            onSelectSection = { section -> viewModel.navigateToSection(section) },
                            onSelectGenre = { genreId ->
                                viewModel.navigateToSection(
                                    section = CineSection.GENRES,
                                    presetGenreId = genreId
                                )
                            },
                            onOpenAboutTab = { tab ->
                                viewModel.navigateToSection(
                                    section = CineSection.ABOUT,
                                    presetAboutTab = tab
                                )
                            },
                            onRetry = { viewModel.refreshAllInitialData() }
                        )
                    }

                    CineSection.MOVIES,
                    CineSection.TV_SHOWS,
                    CineSection.TRENDING,
                    CineSection.POPULAR,
                    CineSection.TOP_RATED,
                    CineSection.UPCOMING,
                    CineSection.GENRES,
                    CineSection.WATCH_PROVIDERS -> {
                        CatalogBrowseScreen(
                            section = currentSection,
                            catalogState = catalogState,
                            selectedWatchRegion = selectedWatchRegion,
                            favoriteIds = favoriteIds,
                            onMediaClick = { item -> viewModel.openMediaDetails(item) },
                            onToggleFavorite = { item -> viewModel.toggleFavorite(item) },
                            onSelectMediaType = { type -> viewModel.selectMediaTypeInCatalog(type) },
                            onSelectSort = { sort -> viewModel.selectSortOption(sort) },
                            onSelectGenre = { genreId -> viewModel.selectGenreFilter(genreId) },
                            onSelectWatchProvider = { providerId ->
                                viewModel.selectWatchProviderFilter(providerId)
                            },
                            onSelectWatchRegion = { regionCode ->
                                viewModel.selectWatchRegion(regionCode)
                            },
                            onLoadMore = { viewModel.loadMoreCatalogItems() },
                            onSelectSection = { section -> viewModel.navigateToSection(section) },
                            onOpenAboutTab = { tab ->
                                viewModel.navigateToSection(
                                    section = CineSection.ABOUT,
                                    presetAboutTab = tab
                                )
                            },
                            onRetry = {
                                viewModel.loadCatalogForSection(currentSection, resetPage = true)
                            }
                        )
                    }

                    CineSection.SEARCH -> {
                        SearchScreen(
                            searchState = searchState,
                            favoriteIds = favoriteIds,
                            onMediaClick = { item -> viewModel.openMediaDetails(item) },
                            onToggleFavorite = { item -> viewModel.toggleFavorite(item) },
                            onLoadMore = { viewModel.loadMoreSearchResults() },
                            onClearSearch = { viewModel.clearSearch() },
                            onSelectSection = { section -> viewModel.navigateToSection(section) },
                            onOpenAboutTab = { tab ->
                                viewModel.navigateToSection(
                                    section = CineSection.ABOUT,
                                    presetAboutTab = tab
                                )
                            },
                            onRetry = { viewModel.submitSearchNow() }
                        )
                    }

                    CineSection.MY_LIST -> {
                        MyListScreen(
                            favorites = favorites,
                            selectedFilterType = myListFilterType,
                            selectedSortOption = myListSortOption,
                            onSelectFilterType = { viewModel.setMyListFilterType(it) },
                            onSelectSortOption = { viewModel.setMyListSortOption(it) },
                            onMediaClick = { item -> viewModel.openMediaDetails(item) },
                            onToggleFavorite = { item -> viewModel.toggleFavorite(item) },
                            onSelectSection = { section -> viewModel.navigateToSection(section) },
                            onOpenAboutTab = { tab ->
                                viewModel.navigateToSection(
                                    section = CineSection.ABOUT,
                                    presetAboutTab = tab
                                )
                            }
                        )
                    }

                    CineSection.ABOUT -> {
                        AboutAndSetupScreen(
                            selectedTab = aboutSubSection,
                            onSelectTab = { viewModel.selectAboutSubSection(it) },
                            onSelectSection = { section -> viewModel.navigateToSection(section) },
                            onApiKeySaved = {
                                viewModel.refreshAllInitialData()
                                viewModel.navigateToSection(CineSection.HOME)
                            }
                        )
                    }
                }
            }
        }

        // Full-Screen Movie / TV Show Details Modal Overlay
        if (detailsState !is DetailsUiState.Idle) {
            MediaDetailsOverlayPage(
                detailsState = detailsState,
                selectedWatchRegion = selectedWatchRegion,
                favoriteIds = favoriteIds,
                onClose = { viewModel.closeMediaDetails() },
                onSelectWatchRegion = { region -> viewModel.selectWatchRegion(region) },
                onPlayTrailer = { video -> viewModel.openTrailerPopup(video) },
                onToggleFavoriteDetails = { details -> viewModel.toggleFavoriteFromDetails(details) },
                onToggleFavoriteItem = { item -> viewModel.toggleFavorite(item) },
                onSelectMediaItem = { item -> viewModel.openMediaDetails(item) },
                onSelectGenre = { genreId, mediaType ->
                    viewModel.closeMediaDetails()
                    viewModel.navigateToSection(
                        section = CineSection.GENRES,
                        presetGenreId = genreId,
                        presetMediaType = mediaType
                    )
                },
                onOpenAboutTab = { tab ->
                    viewModel.closeMediaDetails()
                    viewModel.navigateToSection(
                        section = CineSection.ABOUT,
                        presetAboutTab = tab
                    )
                }
            )
        }

        // Official Trailer Popup Dialog
        activeTrailer?.let { video ->
            TrailerPopupDialog(
                video = video,
                onDismiss = { viewModel.closeTrailerPopup() }
            )
        }
    }
}
