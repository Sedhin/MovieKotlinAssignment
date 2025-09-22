package com.exam.samplemoviekotlin

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_movies")
data class FavoriteMovie (
    @PrimaryKey
    val movieId: Int,
    val title: String,
    val posterUrl: String,
    val releaseDate: String,
    val rating: Float,
    val genre: String,
    val director: String,
    val synopsis: String,
    val cast: String

)

fun Movie.toFavoriteMovie(): FavoriteMovie {
    return FavoriteMovie(
        movieId = this.id,
        title = this.title,
        posterUrl = this.posterUrl,
        releaseDate = this.releaseDate,
        rating = this.rating,
        genre = this.genre,
        director = this.director,
        synopsis = this.synopsis,
        cast = this.cast.joinToString(",")
    )
}

fun FavoriteMovie.toMovie(): Movie {
    return Movie(
        id = this.movieId,
        title = this.title,
        posterUrl = this.posterUrl,
        releaseDate = this.releaseDate,
        rating = this.rating,
        genre = this.genre,
        director = this.director,
        synopsis = this.synopsis,
        cast = this.cast.split(",").filter { it.isNotBlank() },
        isFavorite = true
    )
}

