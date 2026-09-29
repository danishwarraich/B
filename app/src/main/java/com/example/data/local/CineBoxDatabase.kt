package com.example.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.domain.model.MediaItem
import com.example.domain.model.MediaType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class FavoriteMediaEntity(
    val compositeKey: String, // e.g., "MOVIE_12345" or "TV_67890"
    val mediaId: Int,
    val mediaType: String,
    val title: String,
    val overview: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val releaseYear: String,
    val fullReleaseDate: String,
    val rating: Double,
    val voteCount: Int,
    val addedTimestamp: Long = System.currentTimeMillis()
) {
    fun toDomainModel(): MediaItem = MediaItem(
        id = mediaId,
        title = title,
        overview = overview,
        posterUrl = posterUrl,
        backdropUrl = backdropUrl,
        releaseYear = releaseYear,
        fullReleaseDate = fullReleaseDate,
        rating = rating,
        voteCount = voteCount,
        mediaType = MediaType.fromString(mediaType)
    )

    companion object {
        fun buildKey(mediaType: MediaType, id: Int): String = "${mediaType.name}_$id"

        fun fromDomainModel(item: MediaItem): FavoriteMediaEntity = FavoriteMediaEntity(
            compositeKey = buildKey(item.mediaType, item.id),
            mediaId = item.id,
            mediaType = item.mediaType.name,
            title = item.title,
            overview = item.overview,
            posterUrl = item.posterUrl,
            backdropUrl = item.backdropUrl,
            releaseYear = item.releaseYear,
            fullReleaseDate = item.fullReleaseDate,
            rating = item.rating,
            voteCount = item.voteCount
        )
    }
}

interface FavoriteMediaDao {
    fun getAllFavorites(): Flow<List<FavoriteMediaEntity>>
    suspend fun insertFavorite(item: FavoriteMediaEntity)
    suspend fun deleteFavoriteByKey(compositeKey: String)
    suspend fun isFavorite(compositeKey: String): Boolean
}

class CineBoxDatabase private constructor(context: Context) :
    SQLiteOpenHelper(context.applicationContext, DATABASE_NAME, null, DATABASE_VERSION),
    FavoriteMediaDao {

    private val dbScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val favoritesState = MutableStateFlow<List<FavoriteMediaEntity>>(emptyList())

    init {
        dbScope.launch {
            favoritesState.value = queryAllInternal()
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_FAVORITES (
                compositeKey TEXT PRIMARY KEY NOT NULL,
                mediaId INTEGER NOT NULL,
                mediaType TEXT NOT NULL,
                title TEXT NOT NULL,
                overview TEXT NOT NULL,
                posterUrl TEXT,
                backdropUrl TEXT,
                releaseYear TEXT NOT NULL,
                fullReleaseDate TEXT NOT NULL,
                rating REAL NOT NULL,
                voteCount INTEGER NOT NULL,
                addedTimestamp INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FAVORITES")
        onCreate(db)
    }

    fun favoriteMediaDao(): FavoriteMediaDao = this

    override fun getAllFavorites(): Flow<List<FavoriteMediaEntity>> = favoritesState.asStateFlow()

    override suspend fun insertFavorite(item: FavoriteMediaEntity) {
        withContext(Dispatchers.IO) {
            val values = ContentValues().apply {
                put("compositeKey", item.compositeKey)
                put("mediaId", item.mediaId)
                put("mediaType", item.mediaType)
                put("title", item.title)
                put("overview", item.overview)
                put("posterUrl", item.posterUrl)
                put("backdropUrl", item.backdropUrl)
                put("releaseYear", item.releaseYear)
                put("fullReleaseDate", item.fullReleaseDate)
                put("rating", item.rating)
                put("voteCount", item.voteCount)
                put("addedTimestamp", item.addedTimestamp)
            }
            writableDatabase.insertWithOnConflict(
                TABLE_FAVORITES,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
            )
            favoritesState.value = queryAllInternal()
        }
    }

    override suspend fun deleteFavoriteByKey(compositeKey: String) {
        withContext(Dispatchers.IO) {
            writableDatabase.delete(
                TABLE_FAVORITES,
                "compositeKey = ?",
                arrayOf(compositeKey)
            )
            favoritesState.value = queryAllInternal()
        }
    }

    override suspend fun isFavorite(compositeKey: String): Boolean {
        return withContext(Dispatchers.IO) {
            readableDatabase.rawQuery(
                "SELECT 1 FROM $TABLE_FAVORITES WHERE compositeKey = ? LIMIT 1",
                arrayOf(compositeKey)
            ).use { cursor ->
                cursor.moveToFirst()
            }
        }
    }

    private fun queryAllInternal(): List<FavoriteMediaEntity> {
        val result = mutableListOf<FavoriteMediaEntity>()
        readableDatabase.rawQuery(
            "SELECT compositeKey, mediaId, mediaType, title, overview, posterUrl, backdropUrl, releaseYear, fullReleaseDate, rating, voteCount, addedTimestamp FROM $TABLE_FAVORITES ORDER BY addedTimestamp DESC",
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                result.add(
                    FavoriteMediaEntity(
                        compositeKey = cursor.getString(0),
                        mediaId = cursor.getInt(1),
                        mediaType = cursor.getString(2),
                        title = cursor.getString(3),
                        overview = cursor.getString(4),
                        posterUrl = if (cursor.isNull(5)) null else cursor.getString(5),
                        backdropUrl = if (cursor.isNull(6)) null else cursor.getString(6),
                        releaseYear = cursor.getString(7),
                        fullReleaseDate = cursor.getString(8),
                        rating = cursor.getDouble(9),
                        voteCount = cursor.getInt(10),
                        addedTimestamp = cursor.getLong(11)
                    )
                )
            }
        }
        return result
    }

    companion object {
        private const val DATABASE_NAME = "cinebox_database.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_FAVORITES = "favorite_media"

        @Volatile
        private var INSTANCE: CineBoxDatabase? = null

        fun getInstance(context: Context): CineBoxDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CineBoxDatabase(context).also { INSTANCE = it }
            }
        }
    }
}
