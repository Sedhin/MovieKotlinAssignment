@file:OptIn(ExperimentalMaterial3Api::class)
package com.exam.samplemoviekotlin
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.PaddingValues
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.draw.clip
import coil.request.CachePolicy
import coil.request.ImageRequest
import androidx.compose.ui.graphics.Brush
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import androidx.compose.foundation.shape.CircleShape
import coil.size.Precision
import coil.size.Scale

//data class Movie(
//    val id: Int,
//    val title: String,
//    val releaseDate: String,
//    val rating: Float,
//    val posterUrl: String,
//    val genre: String = "",
//    val synopsis: String = "",
//    val director: String = "",
//    val cast: List<String> = emptyList(),
//    val isFavorite: Boolean = false
//)

// Navigation destinations
object Destinations {
    const val MOVIE_LIST = "movie_list"
    const val MOVIE_DETAIL = "movie_detail/{movieId}"

    fun movieDetail(movieId: Int) = "movie_detail/$movieId"
}

@Composable
fun MovieApp(viewModel: MovieViewModel = viewModel()) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Destinations.MOVIE_LIST
    ) {
        composable(Destinations.MOVIE_LIST) {
            MovieListScreen(
                movies = viewModel.filteredMovies,
                isLoading = viewModel.isLoading,
                errorMessage = viewModel.errorMessage,
                onMovieClick = { movie ->
                    navController.navigate(Destinations.movieDetail(movie.id))
                },
                onFavoriteClick = { movie ->
                    viewModel.toggleFavorite(movie.id)
                },
                onSearchQueryChange = { query ->
                    viewModel.searchMovies(query)
                    // You can implement search filtering here if needed
                },
                onRetry = {
                    viewModel.loadMovies()
                }
            )
        }

        composable(Destinations.MOVIE_DETAIL) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId")?.toIntOrNull()
            val movie = movieId?.let { viewModel.getMovieById(it) }

            if (movie != null) {
                MovieDetailScreen(
                    movie = movie,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onFavoriteClick = {
                        viewModel.toggleFavorite(movie.id)
                    }
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieListScreen(
    movies: List<Movie> = emptyList(),
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onMovieClick: (Movie) -> Unit = {},
    onFavoriteClick: (Movie) -> Unit = {},
    onSearchQueryChange: (String) -> Unit = {},
    onRetry: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Search Bar (same as before)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 20.dp,start = 5.dp, end = 5.dp,),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )

                TextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        onSearchQueryChange(it) // This will call viewModel.searchMovies()
                    },
                    placeholder = {
                        Text(
                            "Search movies...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    singleLine = true, // ADD THIS - prevents text wrapping
                    maxLines = 1,      // ADD THIS - keeps it to one line
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = { /* Handle menu */ }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Content Area - Use movies directly (filtering handled in ViewModel)
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(50.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading movies...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            errorMessage != null -> {
                // Error state (same as before)
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text(
                            text = "⚠️",
                            fontSize = 48.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Oops! Something went wrong",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onRetry,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Try Again")
                        }
                    }
                }
            }

            movies.isEmpty() -> {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text(
                            text = "🎬",
                            fontSize = 48.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No movies found",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Check back later for new releases!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            else -> {
                // OPTIMIZED LAZY COLUMN FOR SMOOTH SCROLLING
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    // Key performance optimizations:
                    flingBehavior = ScrollableDefaults.flingBehavior(),
                    userScrollEnabled = true
                ) {
                    items(
                        items = movies,
                        key = { movie -> movie.id }, // Important for performance
                        contentType = { "MovieCard" } // Helps with recycling
                    ) { movie ->
                        MovieCard(
                            movie = movie,
                            onMovieClick = { onMovieClick(movie) },
                            onFavoriteClick = { onFavoriteClick(movie) }
                        )
                    }
                }
            }

//            else -> {
//                // Movie List - Use pre-filtered movies from ViewModel
//                LazyColumn
//                modifier = Modifier.fillMaxSize(),
//                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
//                verticalArrangement = Arrangement.spacedBy(12.dp)
//                ) {
//                    items(
//                        items = movies,
//                        key = { movie -> movie.id },
//                        contentType = { "MovieCard" }
//                    ) { movie ->
//                        MovieCard(
//                            movie = movie,
//                            onMovieClick = { onMovieClick(movie) },
//                            onFavoriteClick = { onFavoriteClick(movie) }
//                        )
//                    }
//                }
//            }
        }

        CheckAllImagesLoaded(movies = movies, viewModel = viewModel())
    }
}

@Composable
fun MovieCard(
    movie: Movie,
    onMovieClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    var imageLoading by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onMovieClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // OPTIMIZED Movie Poster with progress indicator
            Card(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // Background placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )

                    // Actual image
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(movie.posterUrl)
                            .size(60)
                            .scale(Scale.FIT)
                            .crossfade(150) // Smooth transition
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .allowHardware(false)
                            .precision(Precision.INEXACT)
                            .listener(
                                onStart = { imageLoading = true },
                                onSuccess = { _, _ -> imageLoading = false },
                                onError = { _, _ -> imageLoading = false }
                            )
                            .build(),
                        contentDescription = "Movie Poster",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Loading progress indicator for image
                    if (imageLoading) {
//                        Box(
//                            modifier = Modifier
//                                .fillMaxSize()
//                                .background(
//                                    Color.Black.copy(alpha = 0.3f)
//                                ),
//                            contentAlignment = Alignment.Center
//                        )
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        )
                        {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Movie Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = movie.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = movie.releaseDate,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (movie.genre.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = movie.genre,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = String.format("%.1f", movie.rating),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Favorite Button
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = if (movie.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (movie.isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (movie.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun LoadingScreen() {
    var progress by remember { mutableFloatStateOf(0f) }

    // Animate progress from 0 to 1 over 3 seconds
    LaunchedEffect(Unit) {
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 3000,
                easing = EaseInOutCubic
            )
        ) { value, _ ->
            progress = value
        }

    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            // Movie loading animation
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.sweepGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary,
                                MaterialTheme.colorScheme.tertiary,
                                MaterialTheme.colorScheme.primary
                            )
                        )
                    )
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🎬",
                        fontSize = 48.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Circular Progress Indicator
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(60.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 6.dp,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                // Percentage text in center
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Loading text
            Text(
                text = "Loading Movies...",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Dynamic loading messages
            val loadingMessages = listOf(
                "Fetching movie data...",
                "Loading amazing films...",
                "Preparing your cinema...",
                "Almost ready..."
            )

            val currentMessage = loadingMessages[
                (progress * (loadingMessages.size - 1)).toInt()
                    .coerceIn(0, loadingMessages.size - 1)
            ]

            Text(
                text = currentMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Linear progress bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    movie: Movie,
    onBackClick: () -> Unit = {},
    onFavoriteClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = movie.title,
                    maxLines = 1,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            actions = {
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = if (movie.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (movie.isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (movie.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Scrollable Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Movie Poster
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = movie.posterUrl,
                        contentDescription = "Movie Poster",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Movie Information
            Text(
                text = movie.title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = movie.releaseDate,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Rating
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Rating",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${movie.rating} Rating",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (movie.genre.isNotEmpty()) {
                Text(
                    text = movie.genre,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Synopsis Section
            if (movie.synopsis.isNotEmpty()) {
                Text(
                    text = "Synopsis/Description",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = movie.synopsis,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Justify
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Director Section
            if (movie.director.isNotEmpty()) {
                Text(
                    text = "Director",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = movie.director,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Cast Section
            if (movie.cast.isNotEmpty()) {
                Text(
                    text = "Cast",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                movie.cast.forEach { actor ->
                    Text(
                        text = actor,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// Enhanced sample data with detailed information
val sampleMoviesDetailed = listOf(
    Movie(
        id = 1,
        title = "The Dark Knight",
        releaseDate = "July 18, 2008",
        rating = 9.0f,
        posterUrl = "",
        genre = "Action/Crime/Drama",
        synopsis = "Batman raises the stakes in his war on crime. With the help of Lt. Jim Gordon and District Attorney Harvey Dent, Batman sets out to dismantle the remaining criminal organizations that plague the streets. The partnership proves to be effective, but they soon find themselves prey to a reign of chaos unleashed by a rising criminal mastermind known to the terrified citizens of Gotham as the Joker.",
        director = "Christopher Nolan",
        cast = listOf(
            "Christian Bale",
            "Heath Ledger",
            "Aaron Eckhart",
            "Michael Caine",
            "Maggie Gyllenhaal"
        ),
        isFavorite = true
    ),
    Movie(
        id = 2,
        title = "Inception",
        releaseDate = "July 16, 2010",
        rating = 8.8f,
        posterUrl = "",
        genre = "Action/Sci-Fi/Thriller",
        synopsis = "Dom Cobb is a skilled thief, the absolute best in the dangerous art of extraction, stealing valuable secrets from deep within the subconscious during the dream state, when the mind is at its most vulnerable. Cobb's rare ability has made him a coveted player in this treacherous new world of corporate espionage, but it has also made him an international fugitive and cost him everything he has ever loved.",
        director = "Christopher Nolan",
        cast = listOf(
            "Leonardo DiCaprio",
            "Marion Cotillard",
            "Tom Hardy",
            "Ellen Page",
            "Ken Watanabe"
        ),
        isFavorite = false
    ),
    Movie(
        id = 3,
        title = "Interstellar",
        releaseDate = "November 7, 2014",
        rating = 8.6f,
        posterUrl = "",
        genre = "Adventure/Drama/Sci-Fi",
        synopsis = "Earth's future has been riddled by disasters, famines, and droughts. There is only one way to ensure mankind's survival: Interstellar travel. A newly discovered wormhole in the far reaches of our solar system allows a team of astronauts to go where no man has gone before, a planet that may have the right environment to sustain human life.",
        director = "Christopher Nolan",
        cast = listOf(
            "Matthew McConaughey",
            "Anne Hathaway",
            "Jessica Chastain",
            "Bill Irwin",
            "Ellen Burstyn"
        ),
        isFavorite = true
    ),
    Movie(
        id = 4,
        title = "The Matrix",
        releaseDate = "March 31, 1999",
        rating = 8.7f,
        posterUrl = "",
        genre = "Action/Sci-Fi",
        synopsis = "Thomas A. Anderson is a man living two lives. By day he is an average computer programmer and by night a hacker known as Neo. Neo has always questioned his reality, but the truth is far beyond his imagination. Neo finds himself targeted by the police when he is contacted by Morpheus, a legendary computer hacker branded a terrorist by the government.",
        director = "The Wachowskis",
        cast = listOf(
            "Keanu Reeves",
            "Laurence Fishburne",
            "Carrie-Anne Moss",
            "Hugo Weaving",
            "Gloria Foster"
        ),
        isFavorite = false
    )
)

@Preview(showBackground = true)
@Composable
fun MovieAppPreview() {
    MaterialTheme {
        MovieApp()
    }
}

@Composable
fun CheckAllImagesLoaded(movies: List<Movie>, viewModel: MovieViewModel) {
    LaunchedEffect(movies.size) {
        if (movies.isNotEmpty()) {
            delay(10000) // Give images 2 seconds to load
            viewModel.markImagesAsLoaded()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MovieDetailScreenPreview() {
    MaterialTheme {
        MovieDetailScreen(movie = sampleMoviesDetailed[0])
    }
}