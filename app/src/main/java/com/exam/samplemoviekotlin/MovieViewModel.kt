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

    var favoriteMoviesFromDb by mutableStateOf<List<Movie>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var filteredMovies by mutableStateOf<List<Movie>>(emptyList())
        private set

    var allImagesLoaded by mutableStateOf(false)
        private set

    fun markImagesAsLoaded() {
        allImagesLoaded = true
    }

    enum class SortOption {
        TITLE_ASC, TITLE_DESC,
        RELEASE_DATE_ASC, RELEASE_DATE_DESC,
        RATING_ASC, RATING_DESC
    }
    var showFavoritesOnly by mutableStateOf(false)
        private set

    var currentSortOption by mutableStateOf(SortOption.TITLE_ASC)
        private set

    fun filterFavorites(favoritesOnly: Boolean) {
        showFavoritesOnly = favoritesOnly
        applyFilters()
    }

    private var searchQuery by mutableStateOf("")

    init {
        loadMovies()
        loadFavoriteMovies()
    }

    private fun loadFavoriteMovies() {
        viewModelScope.launch {
            repository.getAllFavorites().collect { favoriteMovies ->
                favoriteMoviesFromDb = favoriteMovies.map { it.toMovie() }
                // Update existing movies with favorite status
                updateMoviesWithFavoriteStatus()
            }
        }
    }
    private suspend fun updateMoviesWithFavoriteStatus() {
        val favoriteIds = repository.getFavoriteMovieIds()
        movies = movies.map { movie ->
            movie.copy(isFavorite = favoriteIds.contains(movie.id))
        }
        applyFilters()
    }



    fun sortMovies(sortOption: SortOption) {
        currentSortOption = sortOption

        val sortedMovies = when (sortOption) {
            SortOption.TITLE_ASC -> movies.sortedBy { it.title }
            SortOption.TITLE_DESC -> movies.sortedByDescending { it.title }
            SortOption.RELEASE_DATE_ASC -> movies.sortedBy { it.releaseDate }
            SortOption.RELEASE_DATE_DESC -> movies.sortedByDescending { it.releaseDate }
            SortOption.RATING_ASC -> movies.sortedBy { it.rating }
            SortOption.RATING_DESC -> movies.sortedByDescending { it.rating }
        }

        movies = sortedMovies
        applyFilters()
    }

    private fun applyFilters() {
        var result = movies

        if (showFavoritesOnly) {
            result = result.filter { it.isFavorite }
        }

        if (searchQuery.isNotEmpty()) {
            result = result.filter { movie ->
                movie.title.contains(searchQuery, ignoreCase = true) ||
                        movie.genre.contains(searchQuery, ignoreCase = true)
            }
        }

        filteredMovies = result
    }



    fun loadMovies() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            repository.getMovies()
                .onSuccess { movieList ->
                    movies = movieList
                    updateMoviesWithFavoriteStatus()
                    filteredMovies = movies
//                    filteredMovies = movieList
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
        applyFilters()
    }



    fun toggleFavorite(movieId: Int) {
        viewModelScope.launch {
            val movie = movies.find { it.id == movieId } ?: return@launch

            if (movie.isFavorite) {
                repository.removeFromFavorites(movieId)
            } else {
                repository.addToFavorites(movie)
            }

            movies = movies.map {
                if (it.id == movieId) {
                    it.copy(isFavorite = !it.isFavorite)
                } else it
            }
            applyFilters()
        }
    }

    fun getMovieById(id: Int): Movie? {
        return movies.find { it.id == id }
    }
}