package com.example.config

import android.content.Context
import com.example.BuildConfig

/**
 * ============================================================================
 * CINEBOX — TMDB API CONFIGURATION SECTION
 * ============================================================================
 *
 * HOW TO CONFIGURE YOUR TMDB API KEY:
 * 1. Recommended (AI Studio Secrets Panel):
 *    Open the "Secrets" panel in Google AI Studio and add a secret named:
 *    TMDB_API_KEY = <your_official_tmdb_v3_api_key>
 *
 * 2. Local .env File:
 *    Create or edit `.env` in the project root and set:
 *    TMDB_API_KEY=your_actual_tmdb_v3_api_key
 *
 * 3. In-App Configuration Box:
 *    Paste your TMDB v3 API key or v4 Read Access Token directly in the
 *    CineBox configuration card on the Home screen or About -> TMDB API Setup.
 *
 * Do NOT commit private server-side secrets to public repositories.
 * ============================================================================
 */
object TmdbConfig {

    // Official TMDB v3 API Base URL
    const val API_BASE_URL: String = "https://api.themoviedb.org/3/"

    // Official TMDB Image CDN Base URL
    const val IMAGE_BASE_URL: String = "https://image.tmdb.org/t/p/"

    // Standard TMDB Image Sizes
    const val POSTER_SIZE_MEDIUM: String = "w500"
    const val POSTER_SIZE_LARGE: String = "w780"
    const val BACKDROP_SIZE_HIGH: String = "w1280"
    const val BACKDROP_SIZE_ORIGINAL: String = "original"
    const val PROFILE_SIZE: String = "w185"
    const val LOGO_SIZE: String = "w154"

    private const val PREFS_NAME = "cinebox_tmdb_prefs"
    private const val KEY_SAVED_TMDB_API_KEY = "saved_tmdb_api_key"

    private val V3_HEX_KEY_REGEX = Regex("^[0-9a-fA-F]{32}$")

    @Volatile
    private var runtimeOverrideKey: String = ""

    fun init(context: Context) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_SAVED_TMDB_API_KEY, null)?.trim().orEmpty()
        if (saved.isNotEmpty()) {
            runtimeOverrideKey = sanitizeKey(saved)
        }
    }

    fun saveRuntimeApiKey(context: Context, rawKey: String) {
        val cleaned = sanitizeKey(rawKey)
        runtimeOverrideKey = cleaned
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SAVED_TMDB_API_KEY, cleaned).apply()
    }

    fun sanitizeKey(raw: String): String {
        var s = raw.trim()
        if ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'"))) {
            s = s.substring(1, s.length - 1).trim()
        }
        if (s.startsWith("TMDB_API_KEY=", ignoreCase = true)) {
            s = s.substringAfter("=").trim()
        }
        if (s.startsWith("api_key=", ignoreCase = true)) {
            s = s.substringAfter("=").trim()
        }
        if (s.startsWith("Bearer ", ignoreCase = true)) {
            s = s.substring(7).trim()
        }
        if ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'"))) {
            s = s.substring(1, s.length - 1).trim()
        }
        return s
    }

    /**
     * Reads the active TMDB_API_KEY from in-app override or BuildConfig (.env / AI Studio Secrets).
     */
    val apiKey: String
        get() {
            val override = runtimeOverrideKey
            if (override.isNotEmpty()) return override
            return sanitizeKey(BuildConfig.TMDB_API_KEY)
        }

    /**
     * Returns true only when a valid TMDB v3 32-char hex key or v4 JWT Bearer token is configured.
     */
    val isApiKeyConfigured: Boolean
        get() = isValidTmdbKeyFormat(apiKey)

    fun isValidTmdbKeyFormat(candidate: String): Boolean {
        val key = sanitizeKey(candidate)
        if (key.isEmpty()) return false
        val isV3Hex = V3_HEX_KEY_REGEX.matches(key)
        val isV4Jwt = key.startsWith("eyJ") && key.length > 40 && key.count { it == '.' } == 2
        return isV3Hex || isV4Jwt
    }

    /**
     * Detects whether the user supplied a TMDB v4 Bearer Read Access Token (JWT starting with eyJ)
     * or a standard TMDB v3 32-character hex API key.
     */
    val isBearerToken: Boolean
        get() = apiKey.startsWith("eyJ") && apiKey.length > 40

    fun posterUrl(path: String?, size: String = POSTER_SIZE_MEDIUM): String? {
        if (path.isNullOrBlank()) return null
        val cleanPath = if (path.startsWith("/")) path else "/$path"
        return "$IMAGE_BASE_URL$size$cleanPath"
    }

    fun backdropUrl(path: String?, size: String = BACKDROP_SIZE_HIGH): String? {
        if (path.isNullOrBlank()) return null
        val cleanPath = if (path.startsWith("/")) path else "/$path"
        return "$IMAGE_BASE_URL$size$cleanPath"
    }

    fun profileUrl(path: String?): String? {
        if (path.isNullOrBlank()) return null
        val cleanPath = if (path.startsWith("/")) path else "/$path"
        return "$IMAGE_BASE_URL$PROFILE_SIZE$cleanPath"
    }

    fun logoUrl(path: String?): String? {
        if (path.isNullOrBlank()) return null
        val cleanPath = if (path.startsWith("/")) path else "/$path"
        return "$IMAGE_BASE_URL$LOGO_SIZE$cleanPath"
    }
}
