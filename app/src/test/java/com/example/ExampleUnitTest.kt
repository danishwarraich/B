package com.example

import com.example.data.remote.TmdbApiService
import com.example.data.remote.TmdbMediaDto
import com.example.data.remote.TmdbMovieDetailsDto
import com.example.data.remote.TmdbPagedResponse
import com.squareup.moshi.Types
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `moshi parses popular and trending movie paged response`() {
        val moshi = TmdbApiService.createMoshi()
        val type = Types.newParameterizedType(
            TmdbPagedResponse::class.java,
            TmdbMediaDto::class.java
        )
        val adapter = moshi.adapter<TmdbPagedResponse<TmdbMediaDto>>(type)

        val sampleJson = """
            {
              "page": 1,
              "total_pages": 10,
              "total_results": 200,
              "results": [
                {
                  "id": 693134,
                  "title": "Dune: Part Two",
                  "overview": "Follow the mythic journey of Paul Atreides.",
                  "poster_path": "/1pdfLvkbY9ohJlCjQH2CZjjYVvJ.jpg",
                  "backdrop_path": "/xOMo8BRK7PfcJv9JCnx7s5hj0PX.jpg",
                  "release_date": "2024-02-27",
                  "vote_average": 8.3,
                  "vote_count": 4500,
                  "media_type": "movie"
                }
              ]
            }
        """.trimIndent()

        val parsed = adapter.fromJson(sampleJson)
        assertNotNull(parsed)
        assertEquals(1, parsed?.page)
        assertEquals(1, parsed?.results?.size)
        assertEquals("Dune: Part Two", parsed?.results?.first()?.title)
        assertEquals(8.3, parsed?.results?.first()?.voteAverage ?: 0.0, 0.01)
    }

    @Test
    fun `moshi parses movie details with credits and videos`() {
        val moshi = TmdbApiService.createMoshi()
        val adapter = moshi.adapter(TmdbMovieDetailsDto::class.java)

        val sampleDetailsJson = """
            {
              "id": 693134,
              "title": "Dune: Part Two",
              "runtime": 167,
              "release_date": "2024-02-27",
              "vote_average": 8.3,
              "genres": [{"id": 878, "name": "Science Fiction"}],
              "credits": {
                "cast": [{"id": 1190668, "name": "Timothée Chalamet", "character": "Paul Atreides"}],
                "crew": [{"id": 137427, "name": "Denis Villeneuve", "job": "Director"}]
              },
              "videos": {
                "results": [{"id": "v1", "key": "Way9Dexny3w", "name": "Official Trailer", "site": "YouTube", "type": "Trailer", "official": true}]
              }
            }
        """.trimIndent()

        val details = adapter.fromJson(sampleDetailsJson)
        assertNotNull(details)
        assertEquals(167, details?.runtime)
        assertEquals("Science Fiction", details?.genres?.firstOrNull()?.name)
        assertEquals("Denis Villeneuve", details?.credits?.crew?.firstOrNull()?.name)
        assertEquals("Way9Dexny3w", details?.videos?.results?.firstOrNull()?.key)
    }
}
