package com.exam.samplemoviekotlin

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.*

data class ApiMovieResponse(
    val id: String,
    val createdAt: Long? = null,
    val title: String,
    val genre: List<String>? = null,
    val rating: Rating,
    @SerializedName("release_date")
    val releaseDate: Long,
    @SerializedName("poster_url")
    val posterUrl: String,
    @SerializedName("duration_minutes")
    val durationMinutes: Int? = null,
    val director: String,
    val cast: List<String>? = null,
    @SerializedName("box_office_usd")
    val boxOfficeUsd: Long? = null,
    val description: String
)

data class Rating(
    val imdb: Double
)

fun ApiMovieResponse.toMovie(isFavorite: Boolean = false): Movie {
    val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
    val releaseDate = try {
        dateFormat.format(Date(this.releaseDate * 1000))
    } catch (e: Exception) {
        "Unknown"
    }

    return Movie(
        id = this.id.toIntOrNull() ?: 0,
        title = this.title,
        releaseDate = releaseDate,
        rating = this.rating.imdb.toFloat(),
        posterUrl = this.posterUrl,
        genre = this.genre?.joinToString(", ") ?: "",
        synopsis = this.description,
        director = this.director,
        cast = this.cast ?: emptyList(),
        isFavorite = isFavorite
    )
}