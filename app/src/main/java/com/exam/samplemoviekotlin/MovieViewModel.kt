package com.exam.samplemoviekotlin

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MovieViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MovieRepository(application.applicationContext)

    var movies by mutableStateOf<List<Movie>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    // Add filtered movies state for better search performance
    var filteredMovies by mutableStateOf<List<Movie>>(emptyList())
        private set

    var allImagesLoaded by mutableStateOf(false)
        private set

    fun markImagesAsLoaded() {
        allImagesLoaded = true
    }

    private var searchQuery by mutableStateOf("")

    init {
        loadMovies()
    }

    fun loadMovies() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            repository.getMovies()
                .onSuccess { movieList ->
                    movies = movieList
                    filteredMovies = movieList
                    isLoading = false
                }
                .onFailure { exception ->
                    errorMessage = exception.message
                    isLoading = false
                }
        }
    }

    fun searchMovies(query: String) {
        searchQuery = query
        filteredMovies = if (query.isEmpty()) {
            movies
        } else {
            movies.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.genre.contains(query, ignoreCase = true)
            }
        }
    }

    fun toggleFavorite(movieId: Int) {
        movies = movies.map { movie ->
            if (movie.id == movieId) {
                movie.copy(isFavorite = !movie.isFavorite)
            } else {
                movie
            }
        }
        // Update filtered movies as well
        searchMovies(searchQuery)
    }

    fun getMovieById(id: Int): Movie? {
        return movies.find { it.id == id }
    }
}