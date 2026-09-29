package com.example.domain.model

enum class MediaType(val apiValue: String, val displayLabel: String) {
    MOVIE("movie", "MOVIE"),
    TV("tv", "TV SHOW");

    companion object {
        fun fromString(value: String?): MediaType {
            return if (value?.lowercase() == "tv") TV else MOVIE
        }
    }
}

enum class CineSection(val title: String) {
    HOME("Home"),
    MOVIES("Movies"),
    TV_SHOWS("TV Shows"),
    TRENDING("Trending"),
    POPULAR("Popular"),
    TOP_RATED("Top Rated"),
    UPCOMING("Upcoming"),
    GENRES("Genres"),
    SEARCH("Search"),
    WATCH_PROVIDERS("Where to Watch"),
    MY_LIST("My List"),
    ABOUT("About")
}

enum class SortOption(
    val label: String,
    val movieSortParam: String,
    val tvSortParam: String
) {
    POPULARITY("Most Popular", "popularity.desc", "popularity.desc"),
    RATING("Highest Rated", "vote_average.desc", "vote_average.desc"),
    RELEASE_DATE("Newest Releases", "primary_release_date.desc", "first_air_date.desc")
}

data class MediaItem(
    val id: Int,
    val title: String,
    val overview: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val releaseYear: String,
    val fullReleaseDate: String,
    val rating: Double,
    val voteCount: Int,
    val mediaType: MediaType,
    val genreIds: List<Int> = emptyList(),
    val popularity: Double = 0.0
)

data class GenreItem(
    val id: Int,
    val name: String,
    val mediaType: MediaType = MediaType.MOVIE
)

data class CastMember(
    val id: Int,
    val name: String,
    val character: String,
    val profileUrl: String?
)

data class VideoItem(
    val id: String,
    val key: String,
    val name: String,
    val site: String,
    val type: String,
    val isOfficial: Boolean
) {
    val youtubeWatchUrl: String
        get() = "https://www.youtube.com/watch?v=$key"

    val youtubeThumbnailUrl: String
        get() = "https://img.youtube.com/vi/$key/hqdefault.jpg"
}

data class WatchProviderOption(
    val providerId: Int,
    val providerName: String,
    val logoUrl: String?,
    val directSearchUrl: String
)

data class RegionalWatchProviders(
    val regionCode: String,
    val regionName: String,
    val tmdbWatchLink: String?,
    val streamFlatrate: List<WatchProviderOption>,
    val rent: List<WatchProviderOption>,
    val buy: List<WatchProviderOption>,
    val free: List<WatchProviderOption>
) {
    val hasAnyOptions: Boolean
        get() = streamFlatrate.isNotEmpty() || rent.isNotEmpty() || buy.isNotEmpty() || free.isNotEmpty()
}

data class MediaDetails(
    val id: Int,
    val mediaType: MediaType,
    val title: String,
    val tagline: String,
    val overview: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val releaseYear: String,
    val releaseDateFormatted: String,
    val rating: Double,
    val voteCount: Int,
    val runtimeFormatted: String,
    val status: String,
    val genres: List<GenreItem>,
    val directorOrCreator: String,
    val cast: List<CastMember>,
    val trailers: List<VideoItem>,
    val similar: List<MediaItem>,
    val recommendations: List<MediaItem>,
    val watchProvidersByRegion: Map<String, RegionalWatchProviders>,
    val officialHomepage: String?
) {
    val primaryTrailer: VideoItem?
        get() = trailers.firstOrNull { it.type.equals("Trailer", ignoreCase = true) && it.isOfficial }
            ?: trailers.firstOrNull { it.type.equals("Trailer", ignoreCase = true) }
            ?: trailers.firstOrNull()
}

data class WatchRegionOption(
    val code: String,
    val name: String,
    val flagEmoji: String
)

val SUPPORTED_WATCH_REGIONS = listOf(
    WatchRegionOption("US", "United States", "🇺🇸"),
    WatchRegionOption("GB", "United Kingdom", "🇬🇧"),
    WatchRegionOption("CA", "Canada", "🇨🇦"),
    WatchRegionOption("AU", "Australia", "🇦🇺"),
    WatchRegionOption("IN", "India", "🇮🇳"),
    WatchRegionOption("DE", "Germany", "🇩🇪"),
    WatchRegionOption("FR", "France", "🇫🇷"),
    WatchRegionOption("ES", "Spain", "🇪🇸"),
    WatchRegionOption("BR", "Brazil", "🇧🇷"),
    WatchRegionOption("JP", "Japan", "🇯🇵")
)
