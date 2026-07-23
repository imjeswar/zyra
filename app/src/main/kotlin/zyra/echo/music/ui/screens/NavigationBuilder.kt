

package zyra.echo.music.ui.screens

import android.app.Activity
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.navArgument
import zyra.echo.music.constants.DarkModeKey
import zyra.echo.music.constants.PureBlackKey
import zyra.echo.music.ui.screens.artist.ArtistAlbumsScreen
import zyra.echo.music.ui.screens.artist.ArtistItemsScreen
import zyra.echo.music.ui.screens.artist.ArtistScreen
import zyra.echo.music.ui.screens.artist.ArtistSongsScreen
import zyra.echo.music.ui.screens.equalizer.EqScreen
import zyra.echo.music.ui.screens.library.LibraryScreen
import zyra.echo.music.ui.screens.library.LocalSongScreen
import zyra.echo.music.ui.screens.playlist.AutoPlaylistScreen
import zyra.echo.music.ui.screens.playlist.CachePlaylistScreen
import zyra.echo.music.ui.screens.playlist.LocalPlaylistScreen
import zyra.echo.music.ui.screens.playlist.OnlinePlaylistScreen
import zyra.echo.music.ui.screens.playlist.TopPlaylistScreen
import zyra.echo.music.ui.screens.search.OnlineSearchResult
import zyra.echo.music.ui.screens.search.SearchScreen
import zyra.echo.music.ui.screens.settings.AppearanceSettings
import zyra.echo.music.ui.screens.settings.BackupAndRestore
import zyra.echo.music.ui.screens.settings.ContentSettings
import zyra.echo.music.ui.screens.settings.UptimeScreen
import zyra.echo.music.ui.screens.settings.DarkMode
import zyra.echo.music.ui.screens.settings.PlayerSettings
import zyra.echo.music.ui.screens.settings.PrivacySettings
import zyra.echo.music.ui.screens.settings.RomanizationSettings
import zyra.echo.music.ui.screens.settings.SettingsScreen
import zyra.echo.music.ui.screens.settings.StorageSettings
import zyra.echo.music.ui.screens.settings.ThemeScreen
import zyra.echo.music.ui.screens.settings.integrations.IntegrationScreen
import zyra.echo.music.ui.screens.settings.integrations.ListenTogetherSettings
import zyra.echo.music.ui.screens.recognition.RecognitionScreen
import zyra.echo.music.ui.screens.recognition.RecognitionHistoryScreen
import zyra.echo.music.utils.rememberEnumPreference
import zyra.echo.music.utils.rememberPreference
import zyra.echo.music.ui.screens.equalizer.axion.AxionEqScreen
import zyra.echo.music.ui.screens.ambient.AmbientModeScreen

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.navigationBuilder(
    navController: NavHostController,
    scrollBehavior: TopAppBarScrollBehavior,
    activity: Activity,
    snackbarHostState: SnackbarHostState
) {
    composable(Screens.Home.route) {
        HomeScreen(navController = navController, snackbarHostState = snackbarHostState)
    }

    composable(Screens.Search.route) {
        val pureBlackEnabled by rememberPreference(PureBlackKey, defaultValue = true)
        val darkTheme by rememberEnumPreference(DarkModeKey, defaultValue = DarkMode.ON)
        val isSystemInDarkTheme = isSystemInDarkTheme()
        val useDarkTheme = remember(darkTheme, isSystemInDarkTheme) {
            if (darkTheme == DarkMode.AUTO) isSystemInDarkTheme else darkTheme == DarkMode.ON
        }
        val pureBlack = remember(pureBlackEnabled, useDarkTheme) {
            pureBlackEnabled && useDarkTheme
        }
        SearchScreen(
            navController = navController,
            pureBlack = pureBlack
        )
    }

    composable(Screens.Library.route) {
        LibraryScreen(navController)
    }

    composable(Screens.ListenTogether.route) {
        ListenTogetherScreen(navController, showTopBar = false)
    }

    composable(
        route = "listen_together_from_topbar",
    ) {
        ListenTogetherScreen(navController, showTopBar = true)
    }

    composable("listen_together/chat") {
        CommentTogetherScreen(navController)
    }

    composable("history") {
        HistoryScreen(navController)
    }

    composable("ambient_mode") {
        AmbientModeScreen(navController)
    }

    composable("local_songs") {
        LocalSongScreen(navController)
    }

    composable("stats") {
        StatsScreen(navController)
    }

    composable("mood_and_genres") {
        MoodAndGenresScreen(navController, scrollBehavior)
    }



    composable("new_release") {
        NewReleaseScreen(navController, scrollBehavior)
    }

    composable("charts_screen") {
        ChartsScreen(navController)
    }

    composable(
        route = "browse/{browseId}",
        arguments = listOf(
            navArgument("browseId") {
                type = NavType.StringType
            }
        )
    ) {
        BrowseScreen(
            navController,
            scrollBehavior,
            it.arguments?.getString("browseId")
        )
    }

    composable(
        route = "search/{query}",
        arguments = listOf(
            navArgument("query") {
                type = NavType.StringType
            },
        ),
        enterTransition = {
            fadeIn(tween(250))
        },
        exitTransition = {
            if (targetState.destination.route?.startsWith("search/") == true) {
                fadeOut(tween(200))
            } else {
                fadeOut(tween(200)) + slideOutHorizontally { -it / 2 }
            }
        },
        popEnterTransition = {
            if (initialState.destination.route?.startsWith("search/") == true) {
                fadeIn(tween(250))
            } else {
                fadeIn(tween(250)) + slideInHorizontally { -it / 2 }
            }
        },
        popExitTransition = {
            fadeOut(tween(200))
        },
    ) {
        OnlineSearchResult(navController)
    }

    composable(
        route = "album/{albumId}",
        arguments = listOf(
            navArgument("albumId") {
                type = NavType.StringType
            },
        ),
    ) {
        AlbumScreen(navController, scrollBehavior)
    }

    composable(
        route = "artist/{artistId}",
        arguments = listOf(
            navArgument("artistId") {
                type = NavType.StringType
            },
        ),
    ) {
        ArtistScreen(navController, scrollBehavior)
    }

    composable(
        route = "artist/{artistId}/songs",
        arguments = listOf(
            navArgument("artistId") {
                type = NavType.StringType
            },
        ),
    ) {
        ArtistSongsScreen(navController, scrollBehavior)
    }

    composable(
        route = "artist/{artistId}/albums",
        arguments = listOf(
            navArgument("artistId") {
                type = NavType.StringType
            }
        )
    ) {
        ArtistAlbumsScreen(navController, scrollBehavior)
    }

    composable(
        route = "artist/{artistId}/items?browseId={browseId}?params={params}",
        arguments = listOf(
            navArgument("artistId") {
                type = NavType.StringType
            },
            navArgument("browseId") {
                type = NavType.StringType
                nullable = true
            },
            navArgument("params") {
                type = NavType.StringType
                nullable = true
            },
        ),
    ) {
        ArtistItemsScreen(navController, scrollBehavior)
    }

    composable(
        route = "online_playlist/{playlistId}",
        arguments = listOf(
            navArgument("playlistId") {
                type = NavType.StringType
            },
        ),
    ) {
        OnlinePlaylistScreen(navController, scrollBehavior)
    }

    composable(
        route = "local_playlist/{playlistId}",
        arguments = listOf(
            navArgument("playlistId") {
                type = NavType.StringType
            },
        ),
    ) {
        LocalPlaylistScreen(navController, scrollBehavior)
    }

    composable(
        route = "auto_playlist/{playlist}",
        arguments = listOf(
            navArgument("playlist") {
                type = NavType.StringType
            },
        ),
    ) {
        AutoPlaylistScreen(navController, scrollBehavior)
    }

    composable(
        route = "cache_playlist/{playlist}",
        arguments = listOf(
            navArgument("playlist") {
                type = NavType.StringType
            },
        ),
    ) {
        CachePlaylistScreen(navController, scrollBehavior)
    }

    composable(
        route = "top_playlist/{top}",
        arguments = listOf(
            navArgument("top") {
                type = NavType.StringType
            },
        ),
    ) {
        TopPlaylistScreen(navController, scrollBehavior)
    }

    composable(
        route = "youtube_browse/{browseId}?params={params}",
        arguments = listOf(
            navArgument("browseId") {
                type = NavType.StringType
                nullable = true
            },
            navArgument("params") {
                type = NavType.StringType
                nullable = true
            },
        ),
    ) {
        YouTubeBrowseScreen(navController)
    }

    composable("settings") {
        SettingsScreen(navController, scrollBehavior)
    }

    composable(
        route = "settings/echo_brain?highlightKey={highlightKey}",
        arguments = listOf(navArgument("highlightKey") { type = NavType.StringType; nullable = true })
    ) { backStackEntry ->
        zyra.echo.music.ui.screens.settings.EchoBrainScreen(
            navController, 
            (activity as zyra.echo.music.MainActivity).echoBrainEngine, 
            (activity as zyra.echo.music.MainActivity).echoBrainRepository,
            highlightKey = backStackEntry.arguments?.getString("highlightKey")
        )
    }


    composable(
        route = "settings/appearance?highlightKey={highlightKey}",
        arguments = listOf(navArgument("highlightKey") { type = NavType.StringType; nullable = true })
    ) { backStackEntry ->
        AppearanceSettings(navController, scrollBehavior, activity, snackbarHostState, highlightKey = backStackEntry.arguments?.getString("highlightKey"))
    }

    composable("settings/appearance/theme") {
        ThemeScreen(navController)
    }

    composable(
        route = "settings/content?highlightKey={highlightKey}",
        arguments = listOf(navArgument("highlightKey") { type = NavType.StringType; nullable = true })
    ) { backStackEntry ->
        ContentSettings(navController, scrollBehavior, highlightKey = backStackEntry.arguments?.getString("highlightKey"))
    }

    composable("uptime") {
        UptimeScreen(navController, scrollBehavior)
    }

    composable("settings/content/romanization") {
        RomanizationSettings(navController, scrollBehavior)
    }


    
    composable(
        route = "settings/player?highlightKey={highlightKey}",
        arguments = listOf(navArgument("highlightKey") { type = NavType.StringType; nullable = true })
    ) { backStackEntry ->
        PlayerSettings(navController, scrollBehavior, highlightKey = backStackEntry.arguments?.getString("highlightKey"))
    }

    composable(
        route = "settings/storage?autoOpenExportPicker={autoOpenExportPicker}&highlightKey={highlightKey}",
        arguments = listOf(
            navArgument("autoOpenExportPicker") {
                type = NavType.BoolType
                defaultValue = false
            },
            navArgument("highlightKey") { type = NavType.StringType; nullable = true }
        )
    ) { backStackEntry ->
        val autoOpenExportPicker =
            backStackEntry.arguments?.getBoolean("autoOpenExportPicker") ?: false
        StorageSettings(
            navController = navController,
            scrollBehavior = scrollBehavior,
            autoOpenExportPicker = autoOpenExportPicker,
            highlightKey = backStackEntry.arguments?.getString("highlightKey")
        )
    }

    composable("settings/equalizer") {
        AxionEqScreen(onBackClick = { navController.navigateUp() })
    }

    composable(
        route = "settings/privacy?highlightKey={highlightKey}",
        arguments = listOf(navArgument("highlightKey") { type = NavType.StringType; nullable = true })
    ) { backStackEntry ->
        PrivacySettings(navController, scrollBehavior, highlightKey = backStackEntry.arguments?.getString("highlightKey"))
    }

    composable(
        route = "settings/backup_restore?highlightKey={highlightKey}",
        arguments = listOf(navArgument("highlightKey") { type = NavType.StringType; nullable = true })
    ) { backStackEntry ->
        BackupAndRestore(navController, scrollBehavior, highlightKey = backStackEntry.arguments?.getString("highlightKey"))
    }

    composable("settings/integrations") {
        IntegrationScreen(navController, scrollBehavior)
    }

    composable("settings/discord") {
        zyra.echo.music.ui.screens.settings.DiscordSettings(navController, scrollBehavior)
    }

    composable("settings/discord/experimental") {
        com.music.echo.ui.screens.settings.DiscordExperimental(navController)
    }

    composable(route = "settings/integrations/listen_together") {
        ListenTogetherSettings(navController, scrollBehavior)
    }


    dialog("equalizer") {
        EqScreen(navController = navController)
    }

    composable("recognition") {
        RecognitionScreen(navController)
    }

    composable("recognition_history") {
        RecognitionHistoryScreen(navController)
    }
}
