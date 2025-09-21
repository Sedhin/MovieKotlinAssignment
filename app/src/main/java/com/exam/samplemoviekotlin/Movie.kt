package com.exam.samplemoviekotlin

data class Movie(
    val id: Int,
    val title: String,
    val releaseDate: String,
    val rating: Float,
    val posterUrl: String,
    val genre: String = "",
    val synopsis: String = "",
    val director: String = "",
    val cast: List<String> = emptyList(),
    val isFavorite: Boolean = false
)