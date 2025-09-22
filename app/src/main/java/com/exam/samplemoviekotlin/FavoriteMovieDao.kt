package com.exam.samplemoviekotlin


import androidx.room.*
import kotlinx.coroutines.flow.Flow
@Dao
interface FavoriteMovieDao {

    @Query("SELECT * FROM favorite_movies ORDER BY title ASC")
    fun getAllFavorites(): Flow<List<FavoriteMovie>>

    @Query("SELECT * FROM favorite_movies WHERE movieId = :movieId")
    suspend fun getFavoriteById(movieId: Int): FavoriteMovie?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(movie: FavoriteMovie)

    @Delete
    suspend fun deleteFavorite(movie: FavoriteMovie)

    @Query("DELETE FROM favorite_movies WHERE movieId = :movieId")
    suspend fun deleteFavoriteById(movieId: Int)

    @Query("SELECT movieId FROM favorite_movies")
    suspend fun getFavoriteMovieIds(): List<Int>
}