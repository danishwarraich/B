package com.example.data.repository

import android.net.Uri
import com.example.config.TmdbConfig
import com.example.data.local.FavoriteMediaDao
import com.example.data.local.FavoriteMediaEntity
import com.example.data.remote.TmdbApiService
import com.example.data.remote.TmdbMediaDto
import com.example.data.remote.TmdbMovieDetailsDto
import com.example.data.remote.TmdbRegionWatchProvidersDto
import com.example.data.remote.TmdbTvDetailsDto
import com.example.data.remote.TmdbWatchProviderItemDto
import com.example.domain.model.CastMember
import com.example.domain.model.GenreItem
import com.example.domain.model.MediaDetails
import com.example.domain.model.MediaItem
import com.example.domain.model.MediaType
import com.example.domain.model.RegionalWatchProviders
import com.example.domain.model.SUPPORTED_WATCH_REGIONS
import com.example.domain.model.SortOption
import com.example.domain.model.VideoItem
import com.example.domain.model.WatchProviderOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CineBoxRepository(
    private val apiService: TmdbApiService,
    private val favoriteDao: FavoriteMediaDao
) {

    val favoriteItems: Flow<List<MediaItem>> = favoriteDao.getAllFavorites().map { entities ->
        entities.map { it.toDomainModel() }
    }

    suspend fun toggleFavorite(item: MediaItem) {
        val key = FavoriteMediaEntity.buildKey(item.mediaType, item.id)
        if (favoriteDao.isFavorite(key)) {
            favoriteDao.deleteFavoriteByKey(key)
        } else {
            favoriteDao.insertFavorite(FavoriteMediaEntity.fromDomainModel(item))
        }
    }

    suspend fun getTrendingMovies(page: Int = 1): List<MediaItem> {
        val response = apiService.getTrending(mediaType = "movie", timeWindow = "week", page = page)
        return response.results.mapNotNull { it.toDomainModel(defaultType = MediaType.MOVIE) }
    }

    suspend fun getTrendingAll(page: Int = 1): List<MediaItem> {
        val response = apiService.getTrending(mediaType = "all", timeWindow = "week", page = page)
        return response.results.mapNotNull { dto ->
            if (dto.mediaType == "person") null
            else dto.toDomainModel(defaultType = MediaType.fromString(dto.mediaType))
        }
    }

    suspend fun getPopularMovies(page: Int = 1): List<MediaItem> {
        val response = apiService.getPopularMovies(page = page)
        return response.results.mapNotNull { it.toDomainModel(defaultType = MediaType.MOVIE) }
    }

    suspend fun getTopRatedMovies(page: Int = 1): List<MediaItem> {
        val response = apiService.getTopRatedMovies(page = page)
        return response.results.mapNotNull { it.toDomainModel(defaultType = MediaType.MOVIE) }
    }

    suspend fun getUpcomingMovies(page: Int = 1): List<MediaItem> {
        val response = apiService.getUpcomingMovies(page = page)
        return response.results.mapNotNull { it.toDomainModel(defaultType = MediaType.MOVIE) }
    }

    suspend fun getPopularTvShows(page: Int = 1): List<MediaItem> {
        val response = apiService.getPopularTvShows(page = page)
        return response.results.mapNotNull { it.toDomainModel(defaultType = MediaType.TV) }
    }

    suspend fun getTopRatedTvShows(page: Int = 1): List<MediaItem> {
        val response = apiService.getTopRatedTvShows(page = page)
        return response.results.mapNotNull { it.toDomainModel(defaultType = MediaType.TV) }
    }

    suspend fun getGenres(mediaType: MediaType): List<GenreItem> {
        val response = if (mediaType == MediaType.MOVIE) {
            apiService.getMovieGenres()
        } else {
            apiService.getTvGenres()
        }
        return response.genres.map { GenreItem(id = it.id, name = it.name, mediaType = mediaType) }
    }

    suspend fun discoverMedia(
        mediaType: MediaType,
        page: Int = 1,
        sortOption: SortOption = SortOption.POPULARITY,
        genreId: Int? = null,
        watchProviderId: Int? = null,
        watchRegion: String? = null
    ): List<MediaItem> {
        val genreParam = genreId?.toString()
        val providerParam = watchProviderId?.toString()
        val minVotes = if (sortOption == SortOption.RATING) 150 else 20

        val response = if (mediaType == MediaType.MOVIE) {
            apiService.discoverMovies(
                page = page,
                sortBy = sortOption.movieSortParam,
                withGenres = genreParam,
                withWatchProviders = providerParam,
                watchRegion = if (providerParam != null) (watchRegion ?: "US") else null,
                minVoteCount = minVotes
            )
        } else {
            apiService.discoverTvShows(
                page = page,
                sortBy = sortOption.tvSortParam,
                withGenres = genreParam,
                withWatchProviders = providerParam,
                watchRegion = if (providerParam != null) (watchRegion ?: "US") else null,
                minVoteCount = minVotes
            )
        }
        return response.results.mapNotNull { it.toDomainModel(defaultType = mediaType) }
    }

    suspend fun searchCatalog(query: String, page: Int = 1): List<MediaItem> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return emptyList()
        val response = apiService.searchMulti(query = trimmed, page = page)
        return response.results.mapNotNull { dto ->
            when (dto.mediaType) {
                "movie" -> dto.toDomainModel(defaultType = MediaType.MOVIE)
                "tv" -> dto.toDomainModel(defaultType = MediaType.TV)
                "person" -> null
                else -> dto.toDomainModel(
                    defaultType = if (dto.firstAirDate != null && dto.releaseDate == null) {
                        MediaType.TV
                    } else {
                        MediaType.MOVIE
                    }
                )
            }
        }
    }

    suspend fun getWatchProviderCatalog(
        region: String = "US",
        mediaType: MediaType = MediaType.MOVIE
    ): List<WatchProviderOption> {
        val response = if (mediaType == MediaType.MOVIE) {
            apiService.getMovieWatchProvidersCatalog(watchRegion = region)
        } else {
            apiService.getTvWatchProvidersCatalog(watchRegion = region)
        }
        return response.results
            .sortedBy { it.displayPriority ?: 999 }
            .take(36)
            .map { dto ->
                dto.toWatchProviderOption(titleForSearch = "", fallbackLink = null)
            }
    }

    suspend fun getMediaDetails(mediaType: MediaType, id: Int): MediaDetails {
        return if (mediaType == MediaType.MOVIE) {
            val dto = apiService.getMovieDetails(movieId = id)
            dto.toDomainDetails()
        } else {
            val dto = apiService.getTvDetails(tvId = id)
            dto.toDomainDetails()
        }
    }

    private fun TmdbMediaDto.toDomainModel(defaultType: MediaType): MediaItem? {
        val resolvedTitle = (title ?: name ?: originalTitle ?: originalName)?.trim()
        if (resolvedTitle.isNullOrEmpty()) return null
        val rawDate = (releaseDate ?: firstAirDate)?.trim().orEmpty()
        val year = if (rawDate.length >= 4) rawDate.substring(0, 4) else "TBA"
        val resolvedType = when (mediaType?.lowercase()) {
            "tv" -> MediaType.TV
            "movie" -> MediaType.MOVIE
            else -> defaultType
        }
        return MediaItem(
            id = id,
            title = resolvedTitle,
            overview = overview?.takeIf { it.isNotBlank() } ?: "No synopsis available on TMDB.",
            posterUrl = TmdbConfig.posterUrl(posterPath),
            backdropUrl = TmdbConfig.backdropUrl(backdropPath),
            releaseYear = year,
            fullReleaseDate = rawDate.ifEmpty { "TBA" },
            rating = voteAverage ?: 0.0,
            voteCount = voteCount ?: 0,
            mediaType = resolvedType,
            genreIds = genreIds.orEmpty(),
            popularity = popularity ?: 0.0
        )
    }

    private fun TmdbMovieDetailsDto.toDomainDetails(): MediaDetails {
        val resolvedTitle = title?.takeIf { it.isNotBlank() } ?: "Untitled Movie"
        val rawDate = releaseDate?.trim().orEmpty()
        val year = if (rawDate.length >= 4) rawDate.substring(0, 4) else "TBA"
        val runtimeText = runtime?.takeIf { it > 0 }?.let { mins ->
            val hours = mins / 60
            val rem = mins % 60
            if (hours > 0) "${hours}h ${rem}m" else "${rem}m"
        } ?: "Runtime N/A"

        val director = credits?.crew
            ?.firstOrNull { it.job.equals("Director", ignoreCase = true) }
            ?.name ?: "Unknown Director"

        val castList = credits?.cast.orEmpty()
            .sortedBy { it.order ?: 999 }
            .take(18)
            .map {
                CastMember(
                    id = it.id,
                    name = it.name,
                    character = it.character?.takeIf { c -> c.isNotBlank() } ?: "Cast",
                    profileUrl = TmdbConfig.profileUrl(it.profilePath)
                )
            }

        val videosList = videos?.results.orEmpty()
            .filter { it.site.equals("YouTube", ignoreCase = true) && it.key.isNotBlank() }
            .sortedWith(
                compareByDescending<com.example.data.remote.TmdbVideoDto> {
                    it.type.equals("Trailer", ignoreCase = true)
                }.thenByDescending { it.official == true }
            )
            .map {
                VideoItem(
                    id = it.id,
                    key = it.key,
                    name = it.name,
                    site = it.site,
                    type = it.type,
                    isOfficial = it.official == true
                )
            }

        val similarItems = similar?.results.orEmpty()
            .mapNotNull { it.toDomainModel(MediaType.MOVIE) }
            .take(14)

        val recommendedItems = recommendations?.results.orEmpty()
            .mapNotNull { it.toDomainModel(MediaType.MOVIE) }
            .take(14)

        val providersMap = mapWatchProviders(
            rawResults = watchProviders?.results.orEmpty(),
            title = resolvedTitle
        )

        return MediaDetails(
            id = id,
            mediaType = MediaType.MOVIE,
            title = resolvedTitle,
            tagline = tagline.orEmpty(),
            overview = overview?.takeIf { it.isNotBlank() } ?: "No overview available.",
            posterUrl = TmdbConfig.posterUrl(posterPath, TmdbConfig.POSTER_SIZE_LARGE),
            backdropUrl = TmdbConfig.backdropUrl(backdropPath, TmdbConfig.BACKDROP_SIZE_HIGH),
            releaseYear = year,
            releaseDateFormatted = rawDate.ifEmpty { "TBA" },
            rating = voteAverage ?: 0.0,
            voteCount = voteCount ?: 0,
            runtimeFormatted = runtimeText,
            status = status ?: "Released",
            genres = genres.map { GenreItem(it.id, it.name, MediaType.MOVIE) },
            directorOrCreator = director,
            cast = castList,
            trailers = videosList,
            similar = similarItems,
            recommendations = recommendedItems,
            watchProvidersByRegion = providersMap,
            officialHomepage = homepage?.takeIf { it.isNotBlank() }
        )
    }

    private fun TmdbTvDetailsDto.toDomainDetails(): MediaDetails {
        val resolvedTitle = name?.takeIf { it.isNotBlank() } ?: "Untitled Series"
        val rawDate = firstAirDate?.trim().orEmpty()
        val year = if (rawDate.length >= 4) rawDate.substring(0, 4) else "TBA"

        val seasonsText = numberOfSeasons?.takeIf { it > 0 }?.let {
            "$it Season${if (it > 1) "s" else ""}"
        }
        val epMins = episodeRunTime.firstOrNull()?.takeIf { it > 0 }?.let { "${it}m / ep" }
        val runtimeText = listOfNotNull(seasonsText, epMins).joinToString(" • ").ifEmpty { "TV Series" }

        val creator = createdBy.firstOrNull()?.name
            ?: credits?.crew?.firstOrNull {
                it.job.equals("Executive Producer", ignoreCase = true) ||
                    it.job.equals("Director", ignoreCase = true)
            }?.name
            ?: "Series Creator"

        val castList = credits?.cast.orEmpty()
            .sortedBy { it.order ?: 999 }
            .take(18)
            .map {
                CastMember(
                    id = it.id,
                    name = it.name,
                    character = it.character?.takeIf { c -> c.isNotBlank() } ?: "Cast",
                    profileUrl = TmdbConfig.profileUrl(it.profilePath)
                )
            }

        val videosList = videos?.results.orEmpty()
            .filter { it.site.equals("YouTube", ignoreCase = true) && it.key.isNotBlank() }
            .sortedWith(
                compareByDescending<com.example.data.remote.TmdbVideoDto> {
                    it.type.equals("Trailer", ignoreCase = true)
                }.thenByDescending { it.official == true }
            )
            .map {
                VideoItem(
                    id = it.id,
                    key = it.key,
                    name = it.name,
                    site = it.site,
                    type = it.type,
                    isOfficial = it.official == true
                )
            }

        val similarItems = similar?.results.orEmpty()
            .mapNotNull { it.toDomainModel(MediaType.TV) }
            .take(14)

        val recommendedItems = recommendations?.results.orEmpty()
            .mapNotNull { it.toDomainModel(MediaType.TV) }
            .take(14)

        val providersMap = mapWatchProviders(
            rawResults = watchProviders?.results.orEmpty(),
            title = resolvedTitle
        )

        return MediaDetails(
            id = id,
            mediaType = MediaType.TV,
            title = resolvedTitle,
            tagline = tagline.orEmpty(),
            overview = overview?.takeIf { it.isNotBlank() } ?: "No overview available.",
            posterUrl = TmdbConfig.posterUrl(posterPath, TmdbConfig.POSTER_SIZE_LARGE),
            backdropUrl = TmdbConfig.backdropUrl(backdropPath, TmdbConfig.BACKDROP_SIZE_HIGH),
            releaseYear = year,
            releaseDateFormatted = rawDate.ifEmpty { "TBA" },
            rating = voteAverage ?: 0.0,
            voteCount = voteCount ?: 0,
            runtimeFormatted = runtimeText,
            status = status ?: "Returning Series",
            genres = genres.map { GenreItem(it.id, it.name, MediaType.TV) },
            directorOrCreator = creator,
            cast = castList,
            trailers = videosList,
            similar = similarItems,
            recommendations = recommendedItems,
            watchProvidersByRegion = providersMap,
            officialHomepage = homepage?.takeIf { it.isNotBlank() }
        )
    }

    private fun mapWatchProviders(
        rawResults: Map<String, TmdbRegionWatchProvidersDto>,
        title: String
    ): Map<String, RegionalWatchProviders> {
        val result = mutableMapOf<String, RegionalWatchProviders>()
        for ((regionCode, dto) in rawResults) {
            val regionName = SUPPORTED_WATCH_REGIONS.firstOrNull { it.code == regionCode }?.name ?: regionCode
            val tmdbLink = dto.link
            result[regionCode] = RegionalWatchProviders(
                regionCode = regionCode,
                regionName = regionName,
                tmdbWatchLink = tmdbLink,
                streamFlatrate = dto.flatrate.orEmpty().map { it.toWatchProviderOption(title, tmdbLink) },
                rent = dto.rent.orEmpty().map { it.toWatchProviderOption(title, tmdbLink) },
                buy = dto.buy.orEmpty().map { it.toWatchProviderOption(title, tmdbLink) },
                free = (dto.free.orEmpty() + dto.ads.orEmpty()).distinctBy { it.providerId }
                    .map { it.toWatchProviderOption(title, tmdbLink) }
            )
        }
        return result
    }

    private fun TmdbWatchProviderItemDto.toWatchProviderOption(
        titleForSearch: String,
        fallbackLink: String?
    ): WatchProviderOption {
        val encodedQuery = Uri.encode(titleForSearch.ifBlank { providerName })
        val nameLower = providerName.lowercase()
        val legitimateServiceUrl = when {
            nameLower.contains("netflix") -> "https://www.netflix.com/search?q=$encodedQuery"
            nameLower.contains("disney") -> "https://www.disneyplus.com"
            nameLower.contains("amazon") || nameLower.contains("prime video") ->
                "https://www.amazon.com/s?k=$encodedQuery&i=instant-video"
            nameLower.contains("apple tv") -> "https://tv.apple.com/search?term=$encodedQuery"
            nameLower.contains("max") || nameLower.contains("hbo") -> "https://www.max.com"
            nameLower.contains("hulu") -> "https://www.hulu.com"
            nameLower.contains("paramount") -> "https://www.paramountplus.com"
            nameLower.contains("peacock") -> "https://www.peacocktv.com"
            nameLower.contains("crunchyroll") -> "https://www.crunchyroll.com/search?q=$encodedQuery"
            nameLower.contains("youtube") -> "https://www.youtube.com/results?search_query=$encodedQuery"
            nameLower.contains("google play") -> "https://play.google.com/store/search?q=$encodedQuery&c=movies"
            nameLower.contains("fandango") || nameLower.contains("vudu") -> "https://www.vudu.com"
            nameLower.contains("mubi") -> "https://mubi.com"
            nameLower.contains("tubi") -> "https://tubitv.com/search/$encodedQuery"
            nameLower.contains("pluto") -> "https://pluto.tv"
            !fallbackLink.isNullOrBlank() -> fallbackLink
            else -> "https://www.themoviedb.org/movie"
        }

        return WatchProviderOption(
            providerId = providerId,
            providerName = providerName,
            logoUrl = TmdbConfig.logoUrl(logoPath),
            directSearchUrl = legitimateServiceUrl
        )
    }
}
