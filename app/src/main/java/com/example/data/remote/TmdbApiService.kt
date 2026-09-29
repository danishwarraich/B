package com.example.data.remote

import com.example.config.TmdbConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit service interface for the official TMDB v3 API (https://api.themoviedb.org/3/).
 * Configured with Moshi for JSON serialization/deserialization.
 */
interface TmdbApiService {

    // =========================================================================
    // 1. TRENDING MOVIES & MEDIA
    // =========================================================================

    @GET("trending/{media_type}/{time_window}")
    suspend fun getTrending(
        @Path("media_type") mediaType: String = "movie",
        @Path("time_window") timeWindow: String = "week",
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US"
    ): TmdbPagedResponse<TmdbMediaDto>

    @GET("trending/movie/{time_window}")
    suspend fun getTrendingMovies(
        @Path("time_window") timeWindow: String = "week",
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US"
    ): TmdbPagedResponse<TmdbMediaDto>

    // =========================================================================
    // 2. POPULAR, TOP RATED & UPCOMING MOVIES
    // =========================================================================

    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US",
        @Query("region") region: String? = null
    ): TmdbPagedResponse<TmdbMediaDto>

    @GET("movie/top_rated")
    suspend fun getTopRatedMovies(
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US",
        @Query("region") region: String? = null
    ): TmdbPagedResponse<TmdbMediaDto>

    @GET("movie/upcoming")
    suspend fun getUpcomingMovies(
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US",
        @Query("region") region: String? = null
    ): TmdbPagedResponse<TmdbMediaDto>

    // =========================================================================
    // 3. SEARCH (MOVIES, TV & MULTI-SEARCH)
    // =========================================================================

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("include_adult") includeAdult: Boolean = false,
        @Query("language") language: String = "en-US",
        @Query("primary_release_year") primaryReleaseYear: String? = null
    ): TmdbPagedResponse<TmdbMediaDto>

    @GET("search/multi")
    suspend fun searchMulti(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("include_adult") includeAdult: Boolean = false,
        @Query("language") language: String = "en-US"
    ): TmdbPagedResponse<TmdbMediaDto>

    @GET("search/tv")
    suspend fun searchTvShows(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("include_adult") includeAdult: Boolean = false,
        @Query("language") language: String = "en-US"
    ): TmdbPagedResponse<TmdbMediaDto>

    // =========================================================================
    // 4. MOVIE DETAILS (WITH CREDITS, TRAILERS, SIMILAR, RECOMMENDATIONS & PROVIDERS)
    // =========================================================================

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Query("append_to_response") appendToResponse: String = "credits,videos,similar,recommendations,watch/providers",
        @Query("language") language: String = "en-US"
    ): TmdbMovieDetailsDto

    @GET("movie/{movie_id}/credits")
    suspend fun getMovieCredits(
        @Path("movie_id") movieId: Int,
        @Query("language") language: String = "en-US"
    ): TmdbCreditsResponse

    @GET("movie/{movie_id}/videos")
    suspend fun getMovieVideos(
        @Path("movie_id") movieId: Int,
        @Query("language") language: String = "en-US"
    ): TmdbVideosResponse

    @GET("movie/{movie_id}/similar")
    suspend fun getSimilarMovies(
        @Path("movie_id") movieId: Int,
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US"
    ): TmdbPagedResponse<TmdbMediaDto>

    @GET("movie/{movie_id}/recommendations")
    suspend fun getMovieRecommendations(
        @Path("movie_id") movieId: Int,
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US"
    ): TmdbPagedResponse<TmdbMediaDto>

    @GET("movie/{movie_id}/watch/providers")
    suspend fun getMovieWatchProviders(
        @Path("movie_id") movieId: Int
    ): TmdbWatchProvidersResponse

    // =========================================================================
    // 5. TV SHOWS, GENRES, DISCOVER & REGIONAL WATCH PROVIDERS
    // =========================================================================

    @GET("tv/popular")
    suspend fun getPopularTvShows(
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US"
    ): TmdbPagedResponse<TmdbMediaDto>

    @GET("tv/top_rated")
    suspend fun getTopRatedTvShows(
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US"
    ): TmdbPagedResponse<TmdbMediaDto>

    @GET("tv/{tv_id}")
    suspend fun getTvDetails(
        @Path("tv_id") tvId: Int,
        @Query("append_to_response") appendToResponse: String = "credits,videos,similar,recommendations,watch/providers",
        @Query("language") language: String = "en-US"
    ): TmdbTvDetailsDto

    @GET("genre/movie/list")
    suspend fun getMovieGenres(
        @Query("language") language: String = "en-US"
    ): TmdbGenreListResponse

    @GET("genre/tv/list")
    suspend fun getTvGenres(
        @Query("language") language: String = "en-US"
    ): TmdbGenreListResponse

    @GET("discover/movie")
    suspend fun discoverMovies(
        @Query("page") page: Int = 1,
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("with_genres") withGenres: String? = null,
        @Query("with_watch_providers") withWatchProviders: String? = null,
        @Query("watch_region") watchRegion: String? = null,
        @Query("vote_count.gte") minVoteCount: Int = 50,
        @Query("language") language: String = "en-US"
    ): TmdbPagedResponse<TmdbMediaDto>

    @GET("discover/tv")
    suspend fun discoverTvShows(
        @Query("page") page: Int = 1,
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("with_genres") withGenres: String? = null,
        @Query("with_watch_providers") withWatchProviders: String? = null,
        @Query("watch_region") watchRegion: String? = null,
        @Query("vote_count.gte") minVoteCount: Int = 30,
        @Query("language") language: String = "en-US"
    ): TmdbPagedResponse<TmdbMediaDto>

    @GET("watch/providers/movie")
    suspend fun getMovieWatchProvidersCatalog(
        @Query("watch_region") watchRegion: String = "US",
        @Query("language") language: String = "en-US"
    ): TmdbProviderCatalogResponse

    @GET("watch/providers/tv")
    suspend fun getTvWatchProvidersCatalog(
        @Query("watch_region") watchRegion: String = "US",
        @Query("language") language: String = "en-US"
    ): TmdbProviderCatalogResponse

    companion object {
        const val BASE_URL: String = TmdbConfig.API_BASE_URL

        fun createMoshi(): Moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        fun create(
            okHttpClient: OkHttpClient = TmdbNetworkClient.httpClient,
            moshi: Moshi = createMoshi(),
            baseUrl: String = BASE_URL
        ): TmdbApiService {
            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(TmdbApiService::class.java)
        }
    }
}
