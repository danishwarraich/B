package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.config.TmdbConfig
import com.example.data.local.CineBoxDatabase
import com.example.data.remote.TmdbNetworkClient
import com.example.data.repository.CineBoxRepository
import com.example.domain.model.CineSection
import com.example.domain.model.GenreItem
import com.example.domain.model.MediaDetails
import com.example.domain.model.MediaItem
import com.example.domain.model.MediaType
import com.example.domain.model.SortOption
import com.example.domain.model.VideoItem
import com.example.domain.model.WatchProviderOption
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import retrofit2.HttpException

data class HomeFeedState(
    val isLoading: Boolean = false,
    val isApiKeyMissing: Boolean = false,
    val errorMessage: String? = null,
    val featuredHero: MediaItem? = null,
    val featuredHeroDetails: MediaDetails? = null,
    val trendingMovies: List<MediaItem> = emptyList(),
    val popularMovies: List<MediaItem> = emptyList(),
    val topRatedMovies: List<MediaItem> = emptyList(),
    val upcomingMovies: List<MediaItem> = emptyList(),
    val popularTvShows: List<MediaItem> = emptyList()
)

data class CatalogGridState(
    val isLoading: Boolean = false,
    val isAppending: Boolean = false,
    val isApiKeyMissing: Boolean = false,
    val errorMessage: String? = null,
    val items: List<MediaItem> = emptyList(),
    val currentPage: Int = 1,
    val canLoadMore: Boolean = true,
    val selectedMediaType: MediaType = MediaType.MOVIE,
    val selectedSort: SortOption = SortOption.POPULARITY,
    val selectedGenreId: Int? = null,
    val selectedWatchProviderId: Int? = null,
    val movieGenres: List<GenreItem> = emptyList(),
    val tvGenres: List<GenreItem> = emptyList(),
    val regionalProviders: List<WatchProviderOption> = emptyList()
)

data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val isAppending: Boolean = false,
    val isApiKeyMissing: Boolean = false,
    val errorMessage: String? = null,
    val results: List<MediaItem> = emptyList(),
    val currentPage: Int = 1,
    val canLoadMore: Boolean = false,
    val hasSearched: Boolean = false
)

sealed class DetailsUiState {
    data object Idle : DetailsUiState()
    data class Loading(val previewItem: MediaItem) : DetailsUiState()
    data class Success(val details: MediaDetails) : DetailsUiState()
    data class Error(val previewItem: MediaItem, val message: String) : DetailsUiState()
}

enum class AboutSubSection(val title: String) {
    OVERVIEW("About CineBox"),
    API_CONFIG("TMDB API Setup"),
    CONTACT("Contact"),
    PRIVACY("Privacy Policy"),
    TERMS("Terms of Use")
}

class CineBoxViewModel(
    private val repository: CineBoxRepository
) : ViewModel() {

    private val _currentSection = MutableStateFlow(CineSection.HOME)
    val currentSection: StateFlow<CineSection> = _currentSection.asStateFlow()

    private val _navigationHistory = ArrayDeque<CineSection>()

    private val _selectedWatchRegion = MutableStateFlow("US")
    val selectedWatchRegion: StateFlow<String> = _selectedWatchRegion.asStateFlow()

    private val _homeState = MutableStateFlow(HomeFeedState())
    val homeState: StateFlow<HomeFeedState> = _homeState.asStateFlow()

    private val _catalogState = MutableStateFlow(CatalogGridState())
    val catalogState: StateFlow<CatalogGridState> = _catalogState.asStateFlow()

    private val _searchState = MutableStateFlow(SearchUiState())
    val searchState: StateFlow<SearchUiState> = _searchState.asStateFlow()

    private val _detailsState = MutableStateFlow<DetailsUiState>(DetailsUiState.Idle)
    val detailsState: StateFlow<DetailsUiState> = _detailsState.asStateFlow()

    private val _activeTrailer = MutableStateFlow<VideoItem?>(null)
    val activeTrailer: StateFlow<VideoItem?> = _activeTrailer.asStateFlow()

    private val _aboutSubSection = MutableStateFlow(AboutSubSection.OVERVIEW)
    val aboutSubSection: StateFlow<AboutSubSection> = _aboutSubSection.asStateFlow()

    val favoriteItems: StateFlow<List<MediaItem>> = repository.favoriteItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _myListFilterType = MutableStateFlow<MediaType?>(null)
    val myListFilterType: StateFlow<MediaType?> = _myListFilterType.asStateFlow()

    private val _myListSortOption = MutableStateFlow(SortOption.POPULARITY)
    val myListSortOption: StateFlow<SortOption> = _myListSortOption.asStateFlow()

    private var searchJob: Job? = null

    private val safeExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        val isUnauthorized = isHttpUnauthorized(throwable)
        val message = formatError(throwable)
        _homeState.update { state ->
            if (state.isLoading || state.errorMessage == null) {
                state.copy(
                    isLoading = false,
                    isApiKeyMissing = isUnauthorized || !TmdbConfig.isApiKeyConfigured,
                    errorMessage = message
                )
            } else {
                state
            }
        }
        _catalogState.update { state ->
            if (state.isLoading || state.isAppending) {
                state.copy(
                    isLoading = false,
                    isAppending = false,
                    isApiKeyMissing = isUnauthorized || !TmdbConfig.isApiKeyConfigured,
                    errorMessage = message
                )
            } else {
                state
            }
        }
    }

    init {
        refreshAllInitialData()
    }

    fun refreshAllInitialData() {
        loadGenresAndProviders()
        loadHomeFeed()
        if (_currentSection.value !in listOf(CineSection.HOME, CineSection.MY_LIST, CineSection.ABOUT, CineSection.SEARCH)) {
            loadCatalogForSection(_currentSection.value, resetPage = true)
        } else if (_currentSection.value == CineSection.SEARCH && _searchState.value.query.isNotBlank()) {
            executeSearch(_searchState.value.query.trim(), page = 1, append = false)
        }
    }

    fun navigateToSection(
        section: CineSection,
        presetGenreId: Int? = null,
        presetMediaType: MediaType? = null,
        presetAboutTab: AboutSubSection? = null
    ) {
        if (_detailsState.value !is DetailsUiState.Idle) {
            _detailsState.value = DetailsUiState.Idle
        }
        val current = _currentSection.value
        if (current != section) {
            _navigationHistory.addLast(current)
            if (_navigationHistory.size > 15) {
                _navigationHistory.removeFirst()
            }
            _currentSection.value = section
        }

        if (presetAboutTab != null && section == CineSection.ABOUT) {
            _aboutSubSection.value = presetAboutTab
        }

        when (section) {
            CineSection.HOME -> {
                if (_homeState.value.trendingMovies.isEmpty() && !_homeState.value.isLoading) {
                    loadHomeFeed()
                }
            }
            CineSection.MOVIES -> {
                _catalogState.update {
                    it.copy(
                        selectedMediaType = MediaType.MOVIE,
                        selectedGenreId = presetGenreId,
                        selectedWatchProviderId = null
                    )
                }
                loadCatalogForSection(section, resetPage = true)
            }
            CineSection.TV_SHOWS -> {
                _catalogState.update {
                    it.copy(
                        selectedMediaType = MediaType.TV,
                        selectedGenreId = presetGenreId,
                        selectedWatchProviderId = null
                    )
                }
                loadCatalogForSection(section, resetPage = true)
            }
            CineSection.TRENDING,
            CineSection.POPULAR,
            CineSection.TOP_RATED,
            CineSection.UPCOMING -> {
                _catalogState.update {
                    it.copy(
                        selectedMediaType = presetMediaType ?: MediaType.MOVIE,
                        selectedGenreId = null,
                        selectedWatchProviderId = null
                    )
                }
                loadCatalogForSection(section, resetPage = true)
            }
            CineSection.GENRES -> {
                _catalogState.update {
                    val targetType = presetMediaType ?: it.selectedMediaType
                    val defaultGenre = presetGenreId
                        ?: it.selectedGenreId
                        ?: if (targetType == MediaType.MOVIE) {
                            it.movieGenres.firstOrNull()?.id
                        } else {
                            it.tvGenres.firstOrNull()?.id
                        }
                    it.copy(
                        selectedMediaType = targetType,
                        selectedGenreId = defaultGenre,
                        selectedWatchProviderId = null
                    )
                }
                loadCatalogForSection(section, resetPage = true)
            }
            CineSection.WATCH_PROVIDERS -> {
                _catalogState.update {
                    val defaultProvider = it.selectedWatchProviderId
                        ?: it.regionalProviders.firstOrNull()?.providerId
                    it.copy(
                        selectedMediaType = presetMediaType ?: it.selectedMediaType,
                        selectedWatchProviderId = defaultProvider
                    )
                }
                loadCatalogForSection(section, resetPage = true)
            }
            CineSection.SEARCH -> {
                if (_searchState.value.query.isNotBlank() && !_searchState.value.hasSearched) {
                    executeSearch(_searchState.value.query, page = 1, append = false)
                }
            }
            CineSection.MY_LIST,
            CineSection.ABOUT -> Unit
        }
    }

    fun selectAboutSubSection(subSection: AboutSubSection) {
        _aboutSubSection.value = subSection
    }

    fun navigateBack(): Boolean {
        if (_activeTrailer.value != null) {
            _activeTrailer.value = null
            return true
        }
        if (_detailsState.value !is DetailsUiState.Idle) {
            _detailsState.value = DetailsUiState.Idle
            return true
        }
        if (_navigationHistory.isNotEmpty()) {
            val previous = _navigationHistory.removeLast()
            _currentSection.value = previous
            return true
        }
        if (_currentSection.value != CineSection.HOME) {
            _currentSection.value = CineSection.HOME
            return true
        }
        return false
    }

    fun loadHomeFeed() {
        if (!TmdbConfig.isApiKeyConfigured) {
            _homeState.update {
                it.copy(
                    isLoading = false,
                    isApiKeyMissing = true,
                    errorMessage = "TMDB_API_KEY is not configured yet. Paste your TMDB API key below or add TMDB_API_KEY in the AI Studio Secrets panel."
                )
            }
            return
        }

        viewModelScope.launch(safeExceptionHandler) {
            _homeState.update {
                it.copy(isLoading = true, isApiKeyMissing = false, errorMessage = null)
            }

            supervisorScope {
                val trendingDeferred = async {
                    runCatching { repository.getTrendingMovies(page = 1) }
                }
                val popularMoviesDeferred = async {
                    runCatching { repository.getPopularMovies(page = 1) }
                }
                val topRatedDeferred = async {
                    runCatching { repository.getTopRatedMovies(page = 1) }
                }
                val upcomingDeferred = async {
                    runCatching { repository.getUpcomingMovies(page = 1) }
                }
                val popularTvDeferred = async {
                    runCatching { repository.getPopularTvShows(page = 1) }
                }

                val trendingResult = trendingDeferred.await()
                val popularMoviesResult = popularMoviesDeferred.await()
                val topRatedResult = topRatedDeferred.await()
                val upcomingResult = upcomingDeferred.await()
                val popularTvResult = popularTvDeferred.await()

                val firstError = listOfNotNull(
                    trendingResult.exceptionOrNull(),
                    popularMoviesResult.exceptionOrNull(),
                    topRatedResult.exceptionOrNull(),
                    upcomingResult.exceptionOrNull(),
                    popularTvResult.exceptionOrNull()
                ).firstOrNull()

                val trending = trendingResult.getOrNull().orEmpty()
                val popularMovies = popularMoviesResult.getOrNull().orEmpty()
                val topRated = topRatedResult.getOrNull().orEmpty()
                val upcoming = upcomingResult.getOrNull().orEmpty()
                val popularTv = popularTvResult.getOrNull().orEmpty()

                if (trending.isEmpty() && popularMovies.isEmpty() && firstError != null) {
                    val isUnauthorized = isHttpUnauthorized(firstError)
                    _homeState.update {
                        it.copy(
                            isLoading = false,
                            isApiKeyMissing = isUnauthorized || !TmdbConfig.isApiKeyConfigured,
                            errorMessage = formatError(firstError)
                        )
                    }
                    return@supervisorScope
                }

                val heroCandidate = trending.firstOrNull { !it.backdropUrl.isNullOrBlank() }
                    ?: popularMovies.firstOrNull { !it.backdropUrl.isNullOrBlank() }
                    ?: trending.firstOrNull()

                _homeState.update {
                    it.copy(
                        isLoading = false,
                        isApiKeyMissing = false,
                        errorMessage = null,
                        featuredHero = heroCandidate,
                        trendingMovies = trending,
                        popularMovies = popularMovies,
                        topRatedMovies = topRated,
                        upcomingMovies = upcoming,
                        popularTvShows = popularTv
                    )
                }

                if (heroCandidate != null) {
                    val heroDetails = runCatching {
                        repository.getMediaDetails(heroCandidate.mediaType, heroCandidate.id)
                    }.getOrNull()
                    if (heroDetails != null) {
                        _homeState.update { it.copy(featuredHeroDetails = heroDetails) }
                    }
                }
            }
        }
    }

    private fun loadGenresAndProviders() {
        if (!TmdbConfig.isApiKeyConfigured) return

        viewModelScope.launch(safeExceptionHandler) {
            supervisorScope {
                val movieGenresDeferred = async {
                    runCatching { repository.getGenres(MediaType.MOVIE) }.getOrDefault(emptyList())
                }
                val tvGenresDeferred = async {
                    runCatching { repository.getGenres(MediaType.TV) }.getOrDefault(emptyList())
                }
                val providersDeferred = async {
                    runCatching {
                        repository.getWatchProviderCatalog(
                            region = _selectedWatchRegion.value,
                            mediaType = _catalogState.value.selectedMediaType
                        )
                    }.getOrDefault(emptyList())
                }

                val movieGenres = movieGenresDeferred.await()
                val tvGenres = tvGenresDeferred.await()
                val providers = providersDeferred.await()

                _catalogState.update { state ->
                    state.copy(
                        movieGenres = movieGenres.ifEmpty { state.movieGenres },
                        tvGenres = tvGenres.ifEmpty { state.tvGenres },
                        regionalProviders = providers.ifEmpty { state.regionalProviders },
                        selectedWatchProviderId = state.selectedWatchProviderId
                            ?: providers.firstOrNull()?.providerId
                    )
                }
            }
        }
    }

    fun loadCatalogForSection(section: CineSection = _currentSection.value, resetPage: Boolean = true) {
        if (!TmdbConfig.isApiKeyConfigured) {
            _catalogState.update {
                it.copy(
                    isLoading = false,
                    isAppending = false,
                    isApiKeyMissing = true,
                    errorMessage = "TMDB_API_KEY is not configured yet. Paste your TMDB API key below or configure it in the AI Studio Secrets panel."
                )
            }
            return
        }

        val currentState = _catalogState.value
        val targetPage = if (resetPage) 1 else currentState.currentPage + 1
        if (!resetPage && (currentState.isAppending || !currentState.canLoadMore)) {
            return
        }

        viewModelScope.launch(safeExceptionHandler) {
            _catalogState.update {
                if (resetPage) {
                    it.copy(isLoading = true, isApiKeyMissing = false, errorMessage = null)
                } else {
                    it.copy(isAppending = true, errorMessage = null)
                }
            }

            val result = runCatching {
                when (section) {
                    CineSection.TRENDING -> {
                        repository.getTrendingAll(page = targetPage)
                    }
                    CineSection.POPULAR -> {
                        if (currentState.selectedMediaType == MediaType.TV) {
                            repository.getPopularTvShows(page = targetPage)
                        } else {
                            repository.getPopularMovies(page = targetPage)
                        }
                    }
                    CineSection.TOP_RATED -> {
                        if (currentState.selectedMediaType == MediaType.TV) {
                            repository.getTopRatedTvShows(page = targetPage)
                        } else {
                            repository.getTopRatedMovies(page = targetPage)
                        }
                    }
                    CineSection.UPCOMING -> {
                        repository.getUpcomingMovies(page = targetPage)
                    }
                    CineSection.MOVIES -> {
                        repository.discoverMedia(
                            mediaType = MediaType.MOVIE,
                            page = targetPage,
                            sortOption = currentState.selectedSort,
                            genreId = currentState.selectedGenreId
                        )
                    }
                    CineSection.TV_SHOWS -> {
                        repository.discoverMedia(
                            mediaType = MediaType.TV,
                            page = targetPage,
                            sortOption = currentState.selectedSort,
                            genreId = currentState.selectedGenreId
                        )
                    }
                    CineSection.GENRES -> {
                        repository.discoverMedia(
                            mediaType = currentState.selectedMediaType,
                            page = targetPage,
                            sortOption = currentState.selectedSort,
                            genreId = currentState.selectedGenreId
                        )
                    }
                    CineSection.WATCH_PROVIDERS -> {
                        repository.discoverMedia(
                            mediaType = currentState.selectedMediaType,
                            page = targetPage,
                            sortOption = currentState.selectedSort,
                            watchProviderId = currentState.selectedWatchProviderId,
                            watchRegion = _selectedWatchRegion.value
                        )
                    }
                    else -> {
                        repository.getPopularMovies(page = targetPage)
                    }
                }
            }

            result.onSuccess { fetched ->
                _catalogState.update { state ->
                    val combined = if (resetPage) {
                        fetched
                    } else {
                        (state.items + fetched).distinctBy { "${it.mediaType}_${it.id}" }
                    }
                    state.copy(
                        isLoading = false,
                        isAppending = false,
                        isApiKeyMissing = false,
                        errorMessage = null,
                        items = combined,
                        currentPage = targetPage,
                        canLoadMore = fetched.isNotEmpty()
                    )
                }
            }.onFailure { e ->
                val isUnauthorized = isHttpUnauthorized(e)
                _catalogState.update {
                    it.copy(
                        isLoading = false,
                        isAppending = false,
                        isApiKeyMissing = isUnauthorized || !TmdbConfig.isApiKeyConfigured,
                        errorMessage = formatError(e)
                    )
                }
            }
        }
    }

    fun loadMoreCatalogItems() {
        loadCatalogForSection(_currentSection.value, resetPage = false)
    }

    fun selectMediaTypeInCatalog(mediaType: MediaType) {
        if (_catalogState.value.selectedMediaType == mediaType) return
        _catalogState.update { state ->
            val updatedGenre = if (_currentSection.value == CineSection.GENRES) {
                if (mediaType == MediaType.MOVIE) state.movieGenres.firstOrNull()?.id else state.tvGenres.firstOrNull()?.id
            } else {
                null
            }
            state.copy(
                selectedMediaType = mediaType,
                selectedGenreId = updatedGenre
            )
        }
        if (_currentSection.value == CineSection.WATCH_PROVIDERS) {
            refreshWatchProvidersForCurrentSelection()
        } else {
            loadCatalogForSection(_currentSection.value, resetPage = true)
        }
    }

    fun selectSortOption(sortOption: SortOption) {
        if (_catalogState.value.selectedSort == sortOption) return
        _catalogState.update { it.copy(selectedSort = sortOption) }
        loadCatalogForSection(_currentSection.value, resetPage = true)
    }

    fun selectGenreFilter(genreId: Int?) {
        _catalogState.update { it.copy(selectedGenreId = genreId) }
        loadCatalogForSection(_currentSection.value, resetPage = true)
    }

    fun selectWatchProviderFilter(providerId: Int?) {
        _catalogState.update { it.copy(selectedWatchProviderId = providerId) }
        loadCatalogForSection(_currentSection.value, resetPage = true)
    }

    fun selectWatchRegion(regionCode: String) {
        if (_selectedWatchRegion.value == regionCode) return
        _selectedWatchRegion.value = regionCode
        if (_currentSection.value == CineSection.WATCH_PROVIDERS) {
            refreshWatchProvidersForCurrentSelection()
        }
    }

    private fun refreshWatchProvidersForCurrentSelection() {
        if (!TmdbConfig.isApiKeyConfigured) return
        viewModelScope.launch(safeExceptionHandler) {
            val providers = runCatching {
                repository.getWatchProviderCatalog(
                    region = _selectedWatchRegion.value,
                    mediaType = _catalogState.value.selectedMediaType
                )
            }.getOrDefault(emptyList())
            _catalogState.update { state ->
                val activeProviderId = state.selectedWatchProviderId
                    ?.takeIf { id -> providers.any { it.providerId == id } }
                    ?: providers.firstOrNull()?.providerId
                state.copy(
                    regionalProviders = providers,
                    selectedWatchProviderId = activeProviderId
                )
            }
            loadCatalogForSection(CineSection.WATCH_PROVIDERS, resetPage = true)
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchState.update { it.copy(query = newQuery) }
        if (newQuery.isNotBlank() && _currentSection.value != CineSection.SEARCH) {
            _currentSection.value = CineSection.SEARCH
        }
        searchJob?.cancel()
        if (newQuery.trim().isEmpty()) {
            _searchState.update {
                it.copy(
                    isLoading = false,
                    isAppending = false,
                    results = emptyList(),
                    hasSearched = false,
                    errorMessage = null
                )
            }
            return
        }
        searchJob = viewModelScope.launch(safeExceptionHandler) {
            delay(350)
            executeSearch(newQuery.trim(), page = 1, append = false)
        }
    }

    fun submitSearchNow() {
        val q = _searchState.value.query.trim()
        if (q.isEmpty()) return
        if (_currentSection.value != CineSection.SEARCH) {
            _currentSection.value = CineSection.SEARCH
        }
        searchJob?.cancel()
        executeSearch(q, page = 1, append = false)
    }

    fun clearSearch() {
        searchJob?.cancel()
        _searchState.value = SearchUiState()
    }

    fun loadMoreSearchResults() {
        val state = _searchState.value
        if (state.isLoading || state.isAppending || !state.canLoadMore || state.query.isBlank()) {
            return
        }
        executeSearch(state.query.trim(), page = state.currentPage + 1, append = true)
    }

    private fun executeSearch(query: String, page: Int, append: Boolean) {
        if (!TmdbConfig.isApiKeyConfigured) {
            _searchState.update {
                it.copy(
                    isLoading = false,
                    isAppending = false,
                    isApiKeyMissing = true,
                    hasSearched = true,
                    errorMessage = "TMDB_API_KEY is not configured yet. Paste your TMDB API key below or configure it in the AI Studio Secrets panel."
                )
            }
            return
        }

        viewModelScope.launch(safeExceptionHandler) {
            _searchState.update {
                if (append) {
                    it.copy(isAppending = true, errorMessage = null)
                } else {
                    it.copy(isLoading = true, isApiKeyMissing = false, errorMessage = null, hasSearched = true)
                }
            }

            runCatching {
                repository.searchCatalog(query = query, page = page)
            }.onSuccess { fetched ->
                _searchState.update { state ->
                    val merged = if (append) {
                        (state.results + fetched).distinctBy { "${it.mediaType}_${it.id}" }
                    } else {
                        fetched
                    }
                    state.copy(
                        isLoading = false,
                        isAppending = false,
                        isApiKeyMissing = false,
                        errorMessage = null,
                        results = merged,
                        currentPage = page,
                        canLoadMore = fetched.size >= 18,
                        hasSearched = true
                    )
                }
            }.onFailure { e ->
                val isUnauthorized = isHttpUnauthorized(e)
                _searchState.update {
                    it.copy(
                        isLoading = false,
                        isAppending = false,
                        isApiKeyMissing = isUnauthorized || !TmdbConfig.isApiKeyConfigured,
                        errorMessage = formatError(e)
                    )
                }
            }
        }
    }

    fun openMediaDetails(item: MediaItem) {
        if (!TmdbConfig.isApiKeyConfigured) {
            _detailsState.value = DetailsUiState.Error(
                previewItem = item,
                message = "TMDB_API_KEY is required to load full details, trailers, cast, and streaming providers."
            )
            return
        }

        _detailsState.value = DetailsUiState.Loading(item)
        viewModelScope.launch(safeExceptionHandler) {
            runCatching {
                repository.getMediaDetails(item.mediaType, item.id)
            }.onSuccess { details ->
                _detailsState.value = DetailsUiState.Success(details)
            }.onFailure { e ->
                _detailsState.value = DetailsUiState.Error(
                    previewItem = item,
                    message = formatError(e)
                )
            }
        }
    }

    fun closeMediaDetails() {
        _detailsState.value = DetailsUiState.Idle
    }

    fun openTrailerPopup(video: VideoItem?) {
        _activeTrailer.value = video
    }

    fun openHeroTrailer() {
        val detailsTrailer = _homeState.value.featuredHeroDetails?.primaryTrailer
        if (detailsTrailer != null) {
            _activeTrailer.value = detailsTrailer
            return
        }
        val hero = _homeState.value.featuredHero ?: return
        viewModelScope.launch(safeExceptionHandler) {
            runCatching {
                val details = repository.getMediaDetails(hero.mediaType, hero.id)
                _homeState.update { it.copy(featuredHeroDetails = details) }
                val trailer = details.primaryTrailer
                if (trailer != null) {
                    _activeTrailer.value = trailer
                } else {
                    _detailsState.value = DetailsUiState.Success(details)
                }
            }.onFailure {
                openMediaDetails(hero)
            }
        }
    }

    fun closeTrailerPopup() {
        _activeTrailer.value = null
    }

    fun toggleFavorite(item: MediaItem) {
        viewModelScope.launch(safeExceptionHandler) {
            repository.toggleFavorite(item)
        }
    }

    fun toggleFavoriteFromDetails(details: MediaDetails) {
        val item = MediaItem(
            id = details.id,
            title = details.title,
            overview = details.overview,
            posterUrl = details.posterUrl,
            backdropUrl = details.backdropUrl,
            releaseYear = details.releaseYear,
            fullReleaseDate = details.releaseDateFormatted,
            rating = details.rating,
            voteCount = details.voteCount,
            mediaType = details.mediaType,
            genreIds = details.genres.map { it.id }
        )
        toggleFavorite(item)
    }

    fun setMyListFilterType(mediaType: MediaType?) {
        _myListFilterType.value = mediaType
    }

    fun setMyListSortOption(sortOption: SortOption) {
        _myListSortOption.value = sortOption
    }

    private fun isHttpUnauthorized(e: Throwable): Boolean {
        return (e is HttpException && e.code() == 401) ||
            e.message?.contains("HTTP 401", ignoreCase = true) == true
    }

    private fun formatError(e: Throwable): String {
        if (isHttpUnauthorized(e)) {
            return "TMDB API returned HTTP 401 (Unauthorized). Your TMDB_API_KEY is missing, invalid, or inactive. Paste a valid TMDB v3 API key or v4 Read Access Token below to connect."
        }
        if (e is HttpException) {
            return "TMDB API error (HTTP ${e.code()}): ${e.message()}. Please verify your TMDB_API_KEY and network connection."
        }
        val msg = e.localizedMessage ?: "Unable to reach TMDB API"
        return "TMDB API request failed: $msg"
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory {
            val appContext = context.applicationContext
            TmdbConfig.init(appContext)
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val database = CineBoxDatabase.getInstance(appContext)
                    val repository = CineBoxRepository(
                        apiService = TmdbNetworkClient.apiService,
                        favoriteDao = database.favoriteMediaDao()
                    )
                    return CineBoxViewModel(repository) as T
                }
            }
        }
    }
}
