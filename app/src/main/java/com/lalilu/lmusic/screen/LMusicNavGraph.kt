package com.lalilu.lmusic.screen

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.lalilu.R
import com.lalilu.lmusic.datasource.ALBUM_ID
import com.lalilu.lmusic.datasource.ALBUM_PREFIX
import com.lalilu.lmusic.datasource.ALL_ID
import com.lalilu.lmusic.datasource.ITEM_PREFIX
import com.lalilu.lmusic.screen.detail.*
import com.lalilu.lmusic.utils.WindowSize
import com.lalilu.lmusic.viewmodel.MainViewModel

fun NavHostController.clearBackStack() {
    if (popBackStack()) clearBackStack()
}

fun NavController.navigateTo(destination: String) = navigate(destination) {
    popUpTo(MainScreenData.Library.name) {
        inclusive = destination == MainScreenData.Library.name
    }
}

fun NavController.navigateSingle(destination: String) = navigate(destination) {
    launchSingleTop = true
}

@ExperimentalAnimationApi
@Composable
@ExperimentalMaterialApi
fun LMusicNavGraph(
    modifier: Modifier = Modifier,
    currentWindowSize: WindowSize,
    navController: NavHostController,
    contentPaddingForFooter: Dp = 0.dp,
    mainViewModel: MainViewModel = hiltViewModel(),
    onExpendModal: () -> Unit = {},
) {
    val mediaSource = mainViewModel.mediaSource

    NavHost(
        navController = navController,
        startDestination = MainScreenData.Library.name,
        modifier = modifier
    ) {
        composable(
            route = MainScreenData.Library.name
        ) {
            LibraryScreen(
                currentWindowSize = currentWindowSize,
                navigateTo = navController::navigate,
                contentPaddingForFooter = contentPaddingForFooter
            )
        }

        composable(
            route = MainScreenData.Songs.name
        ) {
            val songs = mediaSource.getChildren(ALL_ID) ?: emptyList()
            SongsScreen(
                songs = songs,
                currentWindowSize = currentWindowSize,
                navigateTo = navController::navigate,
                contentPaddingForFooter = contentPaddingForFooter
            )
        }

        composable(
            route = MainScreenData.Artists.name
        ) {
            ArtistScreen(
                navigateTo = navController::navigate,
                contentPaddingForFooter = contentPaddingForFooter
            )
        }

        composable(
            route = MainScreenData.Albums.name
        ) {
            val albums = mediaSource.getChildren(ALBUM_ID) ?: emptyList()
            AlbumsScreen(
                albums = albums,
                currentWindowSize = currentWindowSize,
                navigateTo = navController::navigate,
                contentPaddingForFooter = contentPaddingForFooter
            )
        }
        composable(
            route = MainScreenData.Playlists.name
        ) {
            PlaylistsScreen(
                navigateTo = navController::navigate,
                contentPaddingForFooter = contentPaddingForFooter
            )
        }
        composable(
            route = "${MainScreenData.PlaylistsDetail.name}/{playlistId}",
            arguments = listOf(navArgument("playlistId") { type = NavType.StringType })
        ) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getString("playlistId")?.toLong()

            playlistId?.let {
                PlaylistDetailScreen(
                    playlistId = it,
                    currentWindowSize = currentWindowSize,
                    navigateTo = navController::navigate,
                    contentPaddingForFooter = contentPaddingForFooter
                )
            }
        }
        composable(
            route = "${MainScreenData.SongsDetail.name}/{mediaId}",
            arguments = listOf(navArgument("mediaId") { type = NavType.StringType })
        ) { backStackEntry ->
            val mediaId = backStackEntry.arguments?.getString("mediaId")

            mediaId?.let { id ->
                mediaSource.getItemById(ITEM_PREFIX + id)?.also {
                    SongDetailScreen(
                        mediaItem = it,
                        navigateTo = navController::navigate
                    )
                }
            } ?: EmptySongDetailScreen()
        }

        composable(
            route = "${MainScreenData.ArtistsDetail.name}/{artistName}",
            arguments = listOf(navArgument("artistName") { type = NavType.StringType })
        ) { backStackEntry ->
            val artistName = backStackEntry.arguments?.getString("artistName")

            artistName?.let { name ->
                ArtistDetailScreen(
                    artistName = name,
                    currentWindowSize = currentWindowSize,
                    navigateTo = navController::navigate,
                    contentPaddingForFooter = contentPaddingForFooter
                )
            } ?: EmptyArtistDetailScreen()
        }

        composable(
            route = "${MainScreenData.AlbumsDetail.name}/{albumId}",
            arguments = listOf(navArgument("albumId") { type = NavType.StringType })
        ) { backStackEntry ->
            val albumId = backStackEntry.arguments?.getString("albumId")

            albumId?.let { id ->
                mediaSource.getItemById(ALBUM_PREFIX + id)?.let { album ->
                    mediaSource.getChildren(ALBUM_PREFIX + id)?.also { songs ->
                        AlbumDetailScreen(
                            album = album,
                            songs = songs,
                            currentWindowSize = currentWindowSize,
                            navigateTo = navController::navigate,
                            contentPaddingForFooter = contentPaddingForFooter
                        )
                    }
                }
            } ?: EmptyAlbumDetailScreen()
        }
        composable(
            route = MainScreenData.SongsAddToPlaylist.name,
            arguments = listOf(navArgument("mediaIds") { type = NavType.StringArrayType })
        ) { backStackEntry ->
            val mediaIds = backStackEntry.arguments?.getStringArrayList("mediaIds")

            mediaIds?.takeIf { it.isNotEmpty() }?.let {
                PlaylistsScreen(
                    navigateUp = navController::navigateUp,
                    contentPaddingForFooter = contentPaddingForFooter,
                    isAddingSongToPlaylist = true,
                    mediaIds = it
                )
            }
        }
        composable(
            route = "${MainScreenData.SongsAddToPlaylist.name}/{mediaId}",
            arguments = listOf(navArgument("mediaId") { type = NavType.StringType })
        ) { backStackEntry ->
            val mediaId = backStackEntry.arguments?.getString("mediaId")

            mediaId?.let {
                PlaylistsScreen(
                    navigateUp = navController::navigateUp,
                    contentPaddingForFooter = contentPaddingForFooter,
                    isAddingSongToPlaylist = true,
                    mediaIds = listOf(it)
                )
            }
        }
        composable(
            route = "${MainScreenData.SongsSearchForLyric.name}/{mediaId}",
            arguments = listOf(navArgument("mediaId") { type = NavType.StringType })
        ) { backStackEntry ->
            val mediaId = backStackEntry.arguments?.getString("mediaId")

            mediaId?.let { id ->
                mediaSource.getItemById(ITEM_PREFIX + id)?.also {
                    SearchForLyricScreen(
                        mediaItem = it,
                        navigateUp = navController::navigateUp,
                        expendScaffold = onExpendModal,
                        contentPaddingForFooter = contentPaddingForFooter
                    )
                }
            } ?: EmptySearchForLyricScreen()
        }
        composable(
            route = MainScreenData.Settings.name
        ) {
            SettingsScreen(
                currentWindowSize = currentWindowSize,
                contentPaddingForFooter = contentPaddingForFooter
            )
        }
    }
}

enum class MainScreenData(
    @DrawableRes val icon: Int,
    @StringRes val title: Int,
    @StringRes val subTitle: Int,
    val showNavigateButton: Boolean = false
) {
    Library(
        icon = R.drawable.ic_loader_line,
        title = R.string.destination_label_library,
        subTitle = R.string.destination_subtitle_library
    ),
    Songs(
        icon = R.drawable.ic_music_2_line,
        title = R.string.destination_label_all_song,
        subTitle = R.string.destination_subtitle_all_song,
        showNavigateButton = true
    ),
    Playlists(
        icon = R.drawable.ic_play_list_line,
        title = R.string.destination_label_playlists,
        subTitle = R.string.destination_subtitle_playlists,
        showNavigateButton = true
    ),
    Artists(
        icon = R.drawable.ic_user_line,
        title = R.string.destination_label_artist,
        subTitle = R.string.destination_subtitle_artist,
        showNavigateButton = true
    ),
    Albums(
        icon = R.drawable.ic_album_fill,
        title = R.string.destination_label_albums,
        subTitle = R.string.destination_subtitle_albums,
        showNavigateButton = true
    ),
    Settings(
        icon = R.drawable.ic_settings_4_line,
        title = R.string.destination_label_settings,
        subTitle = R.string.destination_subtitle_settings,
        showNavigateButton = true
    ),
    PlaylistsDetail(
        icon = R.drawable.ic_play_list_line,
        title = R.string.destination_label_playlist_detail,
        subTitle = R.string.destination_subtitle_playlist_detail
    ),
    ArtistsDetail(
        icon = R.drawable.ic_user_line,
        title = R.string.destination_label_artist,
        subTitle = R.string.destination_subtitle_artist
    ),
    AlbumsDetail(
        icon = R.drawable.ic_album_fill,
        title = R.string.destination_label_album_detail,
        subTitle = R.string.destination_subtitle_album_detail
    ),
    SongsDetail(
        icon = R.drawable.ic_music_2_line,
        title = R.string.destination_label_song_detail,
        subTitle = R.string.destination_subtitle_song_detail
    ),
    SongsAddToPlaylist(
        icon = R.drawable.ic_play_list_line,
        title = R.string.destination_label_add_song_to_playlist,
        subTitle = R.string.destination_label_add_song_to_playlist
    ),
    SongsSearchForLyric(
        icon = R.drawable.ic_lrc_fill,
        title = R.string.destination_label_search_for_lyric,
        subTitle = R.string.destination_label_search_for_lyric
    );

    companion object {
        fun fromRoute(route: String?): MainScreenData? {
            val target = route?.substringBefore("/")
            return values().find { it.name == target }
        }
    }
}