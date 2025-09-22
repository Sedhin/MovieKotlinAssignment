package com.exam.samplemoviekotlin

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import android.content.Context
import kotlinx.coroutines.withContext

class MovieRepository(private val context: Context) {
    private val apiService = ApiService.create(context.cacheDir)
    private val database = MovieDatabase.getDatabase(context)
    private val favoriteDao = database.favoriteMovieDao()

    suspend fun getMovies(): Result<List<Movie>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getMovies()
                if (response.isSuccessful) {
                    val movies = response.body()?.map { it.toMovie() } ?: emptyList()
                    Result.success(movies)
                } else {
                    Result.failure(Exception("Failed to fetch movies: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    suspend fun addToFavorites(movie: Movie) {
        favoriteDao.insertFavorite(movie.toFavoriteMovie())
    }

    suspend fun removeFromFavorites(movieId: Int) {
        favoriteDao.deleteFavoriteById(movieId)
    }

    suspend fun getFavoriteMovieIds(): List<Int> {
        return favoriteDao.getFavoriteMovieIds()
    }

    fun getAllFavorites() = favoriteDao.getAllFavorites()
}