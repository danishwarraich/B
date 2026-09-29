package com.example.data.remote

import com.squareup.moshi.Json

data class TmdbPagedResponse<T>(
    @param:Json(name = "page") val page: Int = 1,
    @param:Json(name = "results") val results: List<T> = emptyList(),
    @param:Json(name = "total_pages") val totalPages: Int = 1,
    @param:Json(name = "total_results") val totalResults: Int = 0
)

data class TmdbMediaDto(
    @param:Json(name = "id") val id: Int,
    @param:Json(name = "title") val title: String? = null,
    @param:Json(name = "name") val name: String? = null,
    @param:Json(name = "original_title") val originalTitle: String? = null,
    @param:Json(name = "original_name") val originalName: String? = null,
    @param:Json(name = "overview") val overview: String? = null,
    @param:Json(name = "poster_path") val posterPath: String? = null,
    @param:Json(name = "backdrop_path") val backdropPath: String? = null,
    @param:Json(name = "media_type") val mediaType: String? = null,
    @param:Json(name = "release_date") val releaseDate: String? = null,
    @param:Json(name = "first_air_date") val firstAirDate: String? = null,
    @param:Json(name = "vote_average") val voteAverage: Double? = null,
    @param:Json(name = "vote_count") val voteCount: Int? = null,
    @param:Json(name = "popularity") val popularity: Double? = null,
    @param:Json(name = "genre_ids") val genreIds: List<Int>? = null,
    @param:Json(name = "original_language") val originalLanguage: String? = null
)

data class TmdbGenreListResponse(
    @param:Json(name = "genres") val genres: List<TmdbGenreDto> = emptyList()
)

data class TmdbGenreDto(
    @param:Json(name = "id") val id: Int,
    @param:Json(name = "name") val name: String
)

data class TmdbMovieDetailsDto(
    @param:Json(name = "id") val id: Int,
    @param:Json(name = "title") val title: String? = null,
    @param:Json(name = "tagline") val tagline: String? = null,
    @param:Json(name = "overview") val overview: String? = null,
    @param:Json(name = "poster_path") val posterPath: String? = null,
    @param:Json(name = "backdrop_path") val backdropPath: String? = null,
    @param:Json(name = "release_date") val releaseDate: String? = null,
    @param:Json(name = "runtime") val runtime: Int? = null,
    @param:Json(name = "vote_average") val voteAverage: Double? = null,
    @param:Json(name = "vote_count") val voteCount: Int? = null,
    @param:Json(name = "status") val status: String? = null,
    @param:Json(name = "homepage") val homepage: String? = null,
    @param:Json(name = "budget") val budget: Long? = null,
    @param:Json(name = "revenue") val revenue: Long? = null,
    @param:Json(name = "genres") val genres: List<TmdbGenreDto> = emptyList(),
    @param:Json(name = "credits") val credits: TmdbCreditsResponse? = null,
    @param:Json(name = "videos") val videos: TmdbVideosResponse? = null,
    @param:Json(name = "similar") val similar: TmdbPagedResponse<TmdbMediaDto>? = null,
    @param:Json(name = "recommendations") val recommendations: TmdbPagedResponse<TmdbMediaDto>? = null,
    @param:Json(name = "watch/providers") val watchProviders: TmdbWatchProvidersResponse? = null
)

data class TmdbTvDetailsDto(
    @param:Json(name = "id") val id: Int,
    @param:Json(name = "name") val name: String? = null,
    @param:Json(name = "tagline") val tagline: String? = null,
    @param:Json(name = "overview") val overview: String? = null,
    @param:Json(name = "poster_path") val posterPath: String? = null,
    @param:Json(name = "backdrop_path") val backdropPath: String? = null,
    @param:Json(name = "first_air_date") val firstAirDate: String? = null,
    @param:Json(name = "last_air_date") val lastAirDate: String? = null,
    @param:Json(name = "episode_run_time") val episodeRunTime: List<Int> = emptyList(),
    @param:Json(name = "number_of_seasons") val numberOfSeasons: Int? = null,
    @param:Json(name = "number_of_episodes") val numberOfEpisodes: Int? = null,
    @param:Json(name = "vote_average") val voteAverage: Double? = null,
    @param:Json(name = "vote_count") val voteCount: Int? = null,
    @param:Json(name = "status") val status: String? = null,
    @param:Json(name = "homepage") val homepage: String? = null,
    @param:Json(name = "created_by") val createdBy: List<TmdbCreatorDto> = emptyList(),
    @param:Json(name = "genres") val genres: List<TmdbGenreDto> = emptyList(),
    @param:Json(name = "credits") val credits: TmdbCreditsResponse? = null,
    @param:Json(name = "videos") val videos: TmdbVideosResponse? = null,
    @param:Json(name = "similar") val similar: TmdbPagedResponse<TmdbMediaDto>? = null,
    @param:Json(name = "recommendations") val recommendations: TmdbPagedResponse<TmdbMediaDto>? = null,
    @param:Json(name = "watch/providers") val watchProviders: TmdbWatchProvidersResponse? = null
)

data class TmdbCreatorDto(
    @param:Json(name = "id") val id: Int,
    @param:Json(name = "name") val name: String,
    @param:Json(name = "profile_path") val profilePath: String? = null
)

data class TmdbCreditsResponse(
    @param:Json(name = "cast") val cast: List<TmdbCastDto> = emptyList(),
    @param:Json(name = "crew") val crew: List<TmdbCrewDto> = emptyList()
)

data class TmdbCastDto(
    @param:Json(name = "id") val id: Int,
    @param:Json(name = "name") val name: String,
    @param:Json(name = "character") val character: String? = null,
    @param:Json(name = "profile_path") val profilePath: String? = null,
    @param:Json(name = "order") val order: Int? = null
)

data class TmdbCrewDto(
    @param:Json(name = "id") val id: Int,
    @param:Json(name = "name") val name: String,
    @param:Json(name = "job") val job: String? = null,
    @param:Json(name = "department") val department: String? = null,
    @param:Json(name = "profile_path") val profilePath: String? = null
)

data class TmdbVideosResponse(
    @param:Json(name = "results") val results: List<TmdbVideoDto> = emptyList()
)

data class TmdbVideoDto(
    @param:Json(name = "id") val id: String,
    @param:Json(name = "key") val key: String,
    @param:Json(name = "name") val name: String,
    @param:Json(name = "site") val site: String,
    @param:Json(name = "type") val type: String,
    @param:Json(name = "official") val official: Boolean? = null,
    @param:Json(name = "published_at") val publishedAt: String? = null
)

data class TmdbWatchProvidersResponse(
    @param:Json(name = "id") val id: Int? = null,
    @param:Json(name = "results") val results: Map<String, TmdbRegionWatchProvidersDto> = emptyMap()
)

data class TmdbRegionWatchProvidersDto(
    @param:Json(name = "link") val link: String? = null,
    @param:Json(name = "flatrate") val flatrate: List<TmdbWatchProviderItemDto>? = null,
    @param:Json(name = "rent") val rent: List<TmdbWatchProviderItemDto>? = null,
    @param:Json(name = "buy") val buy: List<TmdbWatchProviderItemDto>? = null,
    @param:Json(name = "free") val free: List<TmdbWatchProviderItemDto>? = null,
    @param:Json(name = "ads") val ads: List<TmdbWatchProviderItemDto>? = null
)

data class TmdbWatchProviderItemDto(
    @param:Json(name = "provider_id") val providerId: Int,
    @param:Json(name = "provider_name") val providerName: String,
    @param:Json(name = "logo_path") val logoPath: String? = null,
    @param:Json(name = "display_priority") val displayPriority: Int? = null
)

data class TmdbProviderCatalogResponse(
    @param:Json(name = "results") val results: List<TmdbWatchProviderItemDto> = emptyList()
)
