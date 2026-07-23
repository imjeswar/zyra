

package zyra.echo.music.ui.screens.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.border
import androidx.core.content.edit
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import zyra.echo.music.LocalPlayerAwareWindowInsets
import zyra.echo.music.R
import zyra.echo.music.constants.CanvasThumbnailAnimationKey
import zyra.echo.music.constants.ChipSortTypeKey
import zyra.echo.music.constants.CropAlbumArtKey
import zyra.echo.music.constants.DefaultOpenTabKey
import zyra.echo.music.constants.DensityScale
import zyra.echo.music.constants.DensityScaleKey
import zyra.echo.music.constants.DynamicThemeKey
import zyra.echo.music.constants.EnableDynamicIconKey
import zyra.echo.music.constants.EnableHighRefreshRateKey
import zyra.echo.music.constants.EnableLyricsThumbnailPlayPauseKey
import zyra.echo.music.constants.GridItemSize
import zyra.echo.music.constants.GridItemsSizeKey
import zyra.echo.music.constants.HidePlayerThumbnailKey
import zyra.echo.music.constants.LibraryFilter
import zyra.echo.music.constants.ListenTogetherInTopBarKey
import zyra.echo.music.constants.LyricsAnimationStyle
import zyra.echo.music.constants.LyricsAnimationStyleKey
import zyra.echo.music.constants.LyricsStandardBlurKey
import zyra.echo.music.constants.LyricsTextPositionKey
import zyra.echo.music.constants.LyricsTextSizeKey
import zyra.echo.music.constants.PlayerBackgroundStyle
import zyra.echo.music.constants.PlayerBackgroundStyleKey
import zyra.echo.music.constants.PlayerButtonsStyle
import zyra.echo.music.constants.PlayerButtonsStyleKey

import zyra.echo.music.constants.RotatingThumbnailKey
import zyra.echo.music.constants.SelectedThemeColorKey
import zyra.echo.music.constants.ShowCachedPlaylistKey
import zyra.echo.music.constants.ShowExportedPlaylistKey
import zyra.echo.music.constants.ShowDownloadedPlaylistKey
import zyra.echo.music.constants.ShowLikedPlaylistKey
import zyra.echo.music.constants.ShowTopPlaylistKey
import zyra.echo.music.constants.ShowUploadedPlaylistKey
import zyra.echo.music.constants.SliderStyle
import zyra.echo.music.constants.SliderStyleKey
import zyra.echo.music.constants.SquigglySliderKey
import zyra.echo.music.constants.SwipeSensitivityKey
import zyra.echo.music.constants.SwipeThumbnailKey
import zyra.echo.music.constants.SwipeLyricsKey
import zyra.echo.music.constants.SwipeToRemoveSongKey
import zyra.echo.music.constants.SwipeToSongKey
import zyra.echo.music.constants.ThumbnailCornerRadiusKey

import zyra.echo.music.constants.UseNewPlayerDesignKey
import zyra.echo.music.ui.component.ThumbnailCornerRadiusModal
import zyra.echo.music.ui.component.DefaultDialog
import zyra.echo.music.ui.component.EnumDialog
import zyra.echo.music.ui.component.IconButton
import zyra.echo.music.ui.component.Material3SettingsGroup
import zyra.echo.music.ui.component.Material3SettingsItem
import zyra.echo.music.ui.component.PlayerSliderTrack
import zyra.echo.music.ui.component.SquigglySlider
import zyra.echo.music.ui.component.WavySlider
import zyra.echo.music.ui.theme.DefaultThemeColor
import zyra.echo.music.ui.theme.PlayerSliderColors
import zyra.echo.music.ui.utils.backToMain
import zyra.echo.music.utils.IconUtils
import zyra.echo.music.utils.rememberEnumPreference
import zyra.echo.music.utils.rememberPreference
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import zyra.echo.music.constants.LyricsClickKey
import zyra.echo.music.constants.AppleMusicLyricsBlurKey
import zyra.echo.music.constants.LyricsGlowEffectKey
import zyra.echo.music.constants.LyricsLineSpacingKey
import zyra.echo.music.constants.LyricsScrollKey
import zyra.echo.music.constants.HideStatusBarOnFullscreenKey
import zyra.echo.music.constants.MiniPlayerBackgroundStyleKey
import zyra.echo.music.constants.ShowCommentButtonKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
    activity: Activity,
    snackbarHostState: SnackbarHostState,
highlightKey: String? = null) {
    val scrollState = androidx.compose.foundation.rememberScrollState()

    val (dynamicTheme, onDynamicThemeChange) = rememberPreference(
        DynamicThemeKey,
        defaultValue = true
    )
    val (enableLegacyIcon, onEnableLegacyIconChange) = rememberPreference(
        zyra.echo.music.constants.EnableLegacyIconKey,
        defaultValue = false
    )
    val (enableHighRefreshRate, onEnableHighRefreshRateChange) = rememberPreference(
        zyra.echo.music.constants.EnableHighRefreshRateKey,
        defaultValue = true
    )
    val (selectedThemeColorInt) = rememberPreference(
        SelectedThemeColorKey,
        defaultValue = DefaultThemeColor.toArgb()
    )
    
    val isUsingCustomColor = selectedThemeColorInt != DefaultThemeColor.toArgb()
    val coroutineScope = rememberCoroutineScope()

    fun handleIconChange(legacyEnabled: Boolean) {
        onEnableLegacyIconChange(legacyEnabled)
        IconUtils.setIcon(activity, false, legacyEnabled)
        coroutineScope.launch {
            val result = snackbarHostState.showSnackbar(
                message = "Icon updated, restart to apply",
                actionLabel = "Restart"
            )
            if (result == SnackbarResult.ActionPerformed) {
                val packageManager = activity.packageManager
                val intent = packageManager.getLaunchIntentForPackage(activity.packageName)
                val componentName = intent?.component
                val mainIntent = Intent.makeRestartActivityTask(componentName)
                activity.startActivity(mainIntent)
                Runtime.getRuntime().exit(0)
            }
        }
    }


    val (useNewPlayerDesign, onUseNewPlayerDesignChange) = rememberPreference(
        UseNewPlayerDesignKey,
        defaultValue = false
    )
    val (showCodecOnPlayer, onShowCodecOnPlayerChange) = rememberPreference(
        zyra.echo.music.constants.ShowCodecOnPlayerKey,
        defaultValue = false
    )
    val (hidePlayerSlider, onHidePlayerSliderChange) = rememberPreference(
        zyra.echo.music.constants.HidePlayerSliderKey,
        defaultValue = false
    )
    val (hidePlayerThumbnail, onHidePlayerThumbnailChange) = rememberPreference(
        HidePlayerThumbnailKey,
        defaultValue = false
    )
    val (cropAlbumArt, onCropAlbumArtChange) = rememberPreference(
        CropAlbumArtKey,
        defaultValue = false
    )
    val (playerStyle, onPlayerStyleChange) = rememberEnumPreference(
        zyra.echo.music.constants.PlayerStyleKey,
        defaultValue = zyra.echo.music.constants.PlayerStyle.DEFAULT
    )
    val (playerBackground, onPlayerBackgroundChange) =
        rememberEnumPreference(
            PlayerBackgroundStyleKey,
            defaultValue = PlayerBackgroundStyle.APPLE_MUSIC,
        )
    val (miniPlayerBackground, onMiniPlayerBackgroundChange) =
        rememberEnumPreference(
            MiniPlayerBackgroundStyleKey,
            defaultValue = PlayerBackgroundStyle.DEFAULT,
        )

    val (defaultOpenTab, onDefaultOpenTabChange) = rememberEnumPreference(
        DefaultOpenTabKey,
        defaultValue = NavigationTab.HOME
    )
    val (playerButtonsStyle, onPlayerButtonsStyleChange) = rememberEnumPreference(
        PlayerButtonsStyleKey,
        defaultValue = PlayerButtonsStyle.DEFAULT
    )
    val (lyricsPosition, onLyricsPositionChange) = rememberEnumPreference(
        LyricsTextPositionKey,
        defaultValue = LyricsPosition.LEFT
    )
    val (lyricsClick, onLyricsClickChange) = rememberPreference(LyricsClickKey, defaultValue = true)
    val (lyricsScroll, onLyricsScrollChange) = rememberPreference(
        LyricsScrollKey,
        defaultValue = true
    )
    val (lyricsAnimationStyle, onLyricsAnimationStyleChange) = rememberEnumPreference(
        LyricsAnimationStyleKey,
        defaultValue = LyricsAnimationStyle.echomusic_1
    )
    val (lyricsTextSize, onLyricsTextSizeChange) = rememberPreference(LyricsTextSizeKey, defaultValue = 24f)
    val (lyricsLineSpacing, onLyricsLineSpacingChange) = rememberPreference(LyricsLineSpacingKey, defaultValue = 1.3f)
    val (lyricsGlowEffect, onLyricsGlowEffectChange) = rememberPreference(LyricsGlowEffectKey, defaultValue = false)
    val (appleMusicLyricsBlur, onAppleMusicLyricsBlurChange) = rememberPreference(AppleMusicLyricsBlurKey, defaultValue = true)
    val (lyricsStandardBlur, onLyricsStandardBlurChange) = rememberPreference(LyricsStandardBlurKey, defaultValue = false)
    val (swipeLyrics, onSwipeLyricsChange) = rememberPreference(SwipeLyricsKey, defaultValue = false)
    val (enableLyricsThumbnailPlayPause, onEnableLyricsThumbnailPlayPauseChange) = rememberPreference(EnableLyricsThumbnailPlayPauseKey, defaultValue = false)
    val (hideStatusBarOnFullscreen, onHideStatusBarOnFullscreenChange) = rememberPreference(HideStatusBarOnFullscreenKey, defaultValue = false)

    val (sliderStyle, onSliderStyleChange) = rememberEnumPreference(
        SliderStyleKey,
        defaultValue = SliderStyle.WAVY
    )
    val (squigglySlider, onSquigglySliderChange) = rememberPreference(
        SquigglySliderKey,
        defaultValue = false
    )
    val (swipeThumbnail, onSwipeThumbnailChange) = rememberPreference(
        SwipeThumbnailKey,
        defaultValue = true
    )
    val (swipeSensitivity, onSwipeSensitivityChange) = rememberPreference(
        SwipeSensitivityKey,
        defaultValue = 0.73f
    )
    val (canvasThumbnailAnimation, onCanvasThumbnailAnimationChange) = rememberPreference(
        CanvasThumbnailAnimationKey,
        defaultValue = false
    )
    val (rotatingThumbnail, onRotatingThumbnailChange) = rememberPreference(
        RotatingThumbnailKey,
        defaultValue = false
    )
    val (gridItemSize, onGridItemSizeChange) = rememberEnumPreference(
        GridItemsSizeKey,
        defaultValue = GridItemSize.SMALL
    )

    
    val context = activity as Context
    val sharedPreferences = remember { context.getSharedPreferences("echomusic_settings", Context.MODE_PRIVATE) }
    val prefDensityScale = remember(sharedPreferences) {
        sharedPreferences.getFloat("density_scale_factor", 1.0f)
    }
    val (densityScale, setDensityScale) = rememberPreference(DensityScaleKey, defaultValue = prefDensityScale)
    var showRestartDialog by rememberSaveable { mutableStateOf(false) }
    var showDensityScaleDialog by rememberSaveable { mutableStateOf(false) }

    val onDensityScaleChange: (Float) -> Unit = { newScale ->
        setDensityScale(newScale)
        
        sharedPreferences.edit {
            putFloat("density_scale_factor", newScale)
        }
        showRestartDialog = true
    }

    val (listenTogetherInTopBar, onListenTogetherInTopBarChange) = rememberPreference(
        ListenTogetherInTopBarKey,
        defaultValue = true
    )

    val (swipeToSong, onSwipeToSongChange) = rememberPreference(
        SwipeToSongKey,
        defaultValue = false
    )

    val (swipeToRemoveSong, onSwipeToRemoveSongChange) = rememberPreference(
        SwipeToRemoveSongKey,
        defaultValue = false
    )

    val (showLikedPlaylist, onShowLikedPlaylistChange) = rememberPreference(
        ShowLikedPlaylistKey,
        defaultValue = true
    )
    val (showDownloadedPlaylist, onShowDownloadedPlaylistChange) = rememberPreference(
        ShowDownloadedPlaylistKey,
        defaultValue = true
    )
    val (showExportedPlaylist, onShowExportedPlaylistChange) = rememberPreference(
        ShowExportedPlaylistKey,
        defaultValue = true
    )
    val (showTopPlaylist, onShowTopPlaylistChange) = rememberPreference(
        ShowTopPlaylistKey,
        defaultValue = true
    )
    val (showCachedPlaylist, onShowCachedPlaylistChange) = rememberPreference(
        ShowCachedPlaylistKey,
        defaultValue = true
    )
    val (showCommentButton, onShowCommentButtonChange) = rememberPreference(
        ShowCommentButtonKey,
        defaultValue = false
    )

    val availableBackgroundStyles = PlayerBackgroundStyle.entries.filter {
        it != PlayerBackgroundStyle.BLUR || Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    }

    val availableMiniPlayerBackgroundStyles = availableBackgroundStyles.filter { 
        it != PlayerBackgroundStyle.APPLE_MUSIC && it != PlayerBackgroundStyle.GRADIENT
    }



    val (defaultChip, onDefaultChipChange) = rememberEnumPreference(
        key = ChipSortTypeKey,
        defaultValue = LibraryFilter.LIBRARY
    )

    var showSliderOptionDialog by rememberSaveable {
        mutableStateOf(false)
    }



    var showPlayerBackgroundDialog by rememberSaveable {
        mutableStateOf(false)
    }



    var showMiniPlayerBackgroundDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var showPlayerButtonsStyleDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var showLyricsPositionDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var showLyricsAnimationStyleDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var showLyricsTextSizeDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var showLyricsLineSpacingDialog by rememberSaveable {
        mutableStateOf(false)
    }

    if (showLyricsPositionDialog) {
        EnumDialog(
            onDismiss = { showLyricsPositionDialog = false },
            onSelect = {
                onLyricsPositionChange(it)
                showLyricsPositionDialog = false
            },
            title = stringResource(R.string.lyrics_text_position),
            current = lyricsPosition,
            values = LyricsPosition.values().toList(),
            valueText = {
                when (it) {
                    LyricsPosition.LEFT -> stringResource(R.string.left)
                    LyricsPosition.CENTER -> stringResource(R.string.center)
                    LyricsPosition.RIGHT -> stringResource(R.string.right)
                }
            }
        )
    }

    if (showLyricsAnimationStyleDialog) {
        EnumDialog(
            onDismiss = { showLyricsAnimationStyleDialog = false },
            onSelect = {
                onLyricsAnimationStyleChange(it)
                showLyricsAnimationStyleDialog = false
            },
            title = stringResource(R.string.lyrics_animation_style),
            current = lyricsAnimationStyle,
            values = LyricsAnimationStyle.values().toList(),
            valueText = {
                when (it) {
                    LyricsAnimationStyle.NONE -> stringResource(R.string.none)
                    LyricsAnimationStyle.FADE -> stringResource(R.string.fade)
                    LyricsAnimationStyle.GLOW -> stringResource(R.string.glow)
                    LyricsAnimationStyle.SLIDE -> stringResource(R.string.slide)
                    LyricsAnimationStyle.KARAOKE -> stringResource(R.string.karaoke)
                    LyricsAnimationStyle.APPLE -> stringResource(R.string.apple_music_style)
                    LyricsAnimationStyle.APPLE_V2 -> stringResource(R.string.apple_music_style_letter)
                    LyricsAnimationStyle.echomusic_1 -> stringResource(R.string.echomusic_1)
                    LyricsAnimationStyle.LYRICS_V2 -> stringResource(R.string.lyrics_v2_fluid)
                    LyricsAnimationStyle.METRO_LYRICS -> stringResource(R.string.lyrics_animation_metro)
                }
            }
        )
    }

    if (showLyricsTextSizeDialog) {
        var tempTextSize by remember { mutableFloatStateOf(lyricsTextSize) }
        
        DefaultDialog(
            onDismiss = { 
                tempTextSize = lyricsTextSize
                showLyricsTextSizeDialog = false 
            },
            buttons = {
                TextButton(
                    onClick = { 
                        tempTextSize = 24f
                    }
                ) {
                    Text(stringResource(R.string.reset))
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                TextButton(
                    onClick = { 
                        tempTextSize = lyricsTextSize
                        showLyricsTextSizeDialog = false 
                    }
                ) {
                    Text(stringResource(android.R.string.cancel))
                }
                TextButton(
                    onClick = { 
                        onLyricsTextSizeChange(tempTextSize)
                        showLyricsTextSizeDialog = false 
                    }
                ) {
                    Text(stringResource(android.R.string.ok))
                }
            }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.lyrics_text_size),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "${tempTextSize.roundToInt()} sp",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Slider(
                    value = tempTextSize,
                    onValueChange = { tempTextSize = it },
                    valueRange = 16f..36f,
                    steps = 19,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (showLyricsLineSpacingDialog) {
        var tempLineSpacing by remember { mutableFloatStateOf(lyricsLineSpacing) }
        
        DefaultDialog(
            onDismiss = { 
                tempLineSpacing = lyricsLineSpacing
                showLyricsLineSpacingDialog = false 
            },
            buttons = {
                TextButton(
                    onClick = { 
                        tempLineSpacing = 1.3f
                    }
                ) {
                    Text(stringResource(R.string.reset))
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                TextButton(
                    onClick = { 
                        tempLineSpacing = lyricsLineSpacing
                        showLyricsLineSpacingDialog = false 
                    }
                ) {
                    Text(stringResource(android.R.string.cancel))
                }
                TextButton(
                    onClick = { 
                        onLyricsLineSpacingChange(tempLineSpacing)
                        showLyricsLineSpacingDialog = false 
                    }
                ) {
                    Text(stringResource(android.R.string.ok))
                }
            }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.lyrics_line_spacing),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "${String.format("%.1f", tempLineSpacing)}x",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Slider(
                    value = tempLineSpacing,
                    onValueChange = { tempLineSpacing = it },
                    valueRange = 1.0f..4.0f,
                    steps = 59,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (showPlayerButtonsStyleDialog) {
        EnumDialog(
            onDismiss = { showPlayerButtonsStyleDialog = false },
            onSelect = {
                onPlayerButtonsStyleChange(it)
                showPlayerButtonsStyleDialog = false
            },
            title = stringResource(R.string.player_buttons_style),
            current = playerButtonsStyle,
            values = PlayerButtonsStyle.values().toList(),
            valueText = {
                when (it) {
                    PlayerButtonsStyle.DEFAULT -> stringResource(R.string.default_style)
                    PlayerButtonsStyle.PRIMARY -> stringResource(R.string.primary_color_style)
                    PlayerButtonsStyle.TERTIARY -> stringResource(R.string.tertiary_color_style)
                }
            }
        )
    }

    if (showPlayerBackgroundDialog) {
        EnumDialog(
            onDismiss = { showPlayerBackgroundDialog = false },
            onSelect = {
                onPlayerBackgroundChange(it)
                showPlayerBackgroundDialog = false
            },
            title = stringResource(R.string.player_background_style),
            current = playerBackground,
            values = availableBackgroundStyles,
            valueText = {
                when (it) {
                    PlayerBackgroundStyle.DEFAULT -> stringResource(R.string.follow_theme)
                    PlayerBackgroundStyle.GRADIENT -> stringResource(R.string.gradient)
                    PlayerBackgroundStyle.BLUR -> stringResource(R.string.player_background_blur)
                    PlayerBackgroundStyle.GLOW_ANIMATED -> stringResource(R.string.glow_animated)
                    PlayerBackgroundStyle.APPLE_MUSIC -> stringResource(R.string.apple_music)
                    PlayerBackgroundStyle.LIVE_MESH -> stringResource(R.string.live_mesh)
                }
            }
        )
    }

    if (showMiniPlayerBackgroundDialog) {
        EnumDialog(
            onDismiss = { showMiniPlayerBackgroundDialog = false },
            onSelect = {
                onMiniPlayerBackgroundChange(it)
                showMiniPlayerBackgroundDialog = false
            },
            title = stringResource(R.string.miniplayer_background_style),
            current = miniPlayerBackground,
            values = availableMiniPlayerBackgroundStyles,
            valueText = {
                when (it) {
                    PlayerBackgroundStyle.DEFAULT -> stringResource(R.string.follow_theme)
                    PlayerBackgroundStyle.GRADIENT -> stringResource(R.string.gradient)
                    PlayerBackgroundStyle.BLUR -> stringResource(R.string.player_background_blur)
                    PlayerBackgroundStyle.GLOW_ANIMATED -> stringResource(R.string.glow_animated)
                    PlayerBackgroundStyle.LIVE_MESH -> stringResource(R.string.live_mesh)
                    else -> "Unknown"
                }
            }
        )
    }


    var showDefaultOpenTabDialog by rememberSaveable {
        mutableStateOf(false)
    }

    if (showDefaultOpenTabDialog) {
        EnumDialog(
            onDismiss = { showDefaultOpenTabDialog = false },
            onSelect = {
                onDefaultOpenTabChange(it)
                showDefaultOpenTabDialog = false
            },
            title = stringResource(R.string.default_open_tab),
            current = defaultOpenTab,
            values = NavigationTab.values().toList(),
            valueText = {
                when (it) {
                    NavigationTab.HOME -> stringResource(R.string.home)
                    NavigationTab.SEARCH -> stringResource(R.string.search)
                    NavigationTab.LIBRARY -> stringResource(R.string.filter_library)
                }
            }
        )
    }

    var showDefaultChipDialog by rememberSaveable {
        mutableStateOf(false)
    }

    if (showDefaultChipDialog) {
        EnumDialog(
            onDismiss = { showDefaultChipDialog = false },
            onSelect = {
                onDefaultChipChange(it)
                showDefaultChipDialog = false
            },
            title = stringResource(R.string.default_lib_chips),
            current = defaultChip,
            values = LibraryFilter.values().toList(),
            valueText = {
                when (it) {
                    LibraryFilter.SONGS -> stringResource(R.string.songs)
                    LibraryFilter.ARTISTS -> stringResource(R.string.artists)
                    LibraryFilter.ALBUMS -> stringResource(R.string.albums)
                    LibraryFilter.PLAYLISTS -> stringResource(R.string.playlists)
                    LibraryFilter.LIBRARY -> stringResource(R.string.filter_library)
                    LibraryFilter.LOCAL -> stringResource(R.string.filter_local)
                }
            }
        )
    }

    var showGridSizeDialog by rememberSaveable {
        mutableStateOf(false)
    }

    if (showGridSizeDialog) {
        EnumDialog(
            onDismiss = { showGridSizeDialog = false },
            onSelect = {
                onGridItemSizeChange(it)
                showGridSizeDialog = false
            },
            title = stringResource(R.string.grid_cell_size),
            current = gridItemSize,
            values = GridItemSize.values().toList(),
            valueText = {
                when (it) {
                    GridItemSize.BIG -> stringResource(R.string.big)
                    GridItemSize.SMALL -> stringResource(R.string.small)
                }
            }
        )
    }

    if (showRestartDialog) {
        DefaultDialog(
            onDismiss = { showRestartDialog = false },
            buttons = {
                TextButton(
                    onClick = { showRestartDialog = false }
                ) {
                    Text(text = stringResource(android.R.string.cancel))
                }
                TextButton(
                    onClick = {
                        showRestartDialog = false
                        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        }
                        context.startActivity(intent)
                        Runtime.getRuntime().exit(0)
                    }
                ) {
                    Text(text = stringResource(R.string.restart))
                }
            }
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.restart_required),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = stringResource(R.string.density_restart_message),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    if (showDensityScaleDialog) {
        DefaultDialog(
            onDismiss = { showDensityScaleDialog = false },
            buttons = {
                TextButton(
                    onClick = { showDensityScaleDialog = false }
                ) {
                    Text(text = stringResource(android.R.string.cancel))
                }
            }
        ) {
            Column {
                DensityScale.entries.forEach { scale ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onDensityScaleChange(scale.value)
                                showDensityScaleDialog = false
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = scale.label,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (densityScale == scale.value) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }
        }
    }

    if (showSliderOptionDialog) {
        DefaultDialog(
            buttons = {
                TextButton(
                    onClick = { showSliderOptionDialog = false }
                ) {
                    Text(text = stringResource(android.R.string.cancel))
                }
            },
            onDismiss = {
                showSliderOptionDialog = false
            }
        ) {
            val sliderPreviewColors = PlayerSliderColors.getSliderColors(
                MaterialTheme.colorScheme.primary,
                PlayerBackgroundStyle.DEFAULT,
                isSystemInDarkTheme()
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .aspectRatio(1f)
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                1.dp,
                                if (sliderStyle == SliderStyle.DEFAULT && !squigglySlider) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                onSliderStyleChange(SliderStyle.DEFAULT)
                                onSquigglySliderChange(false)
                                showSliderOptionDialog = false
                            }
                            .padding(12.dp)
                    ) {
                        val sliderValue = 0.35f
                        Slider(
                            value = sliderValue,
                            valueRange = 0f..1f,
                                    
                            onValueChange = {  },
                            colors = sliderPreviewColors,
                            enabled = false,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = stringResource(R.string.default_),
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .aspectRatio(1f)
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                1.dp,
                                if (sliderStyle == SliderStyle.WAVY && !squigglySlider) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                onSliderStyleChange(SliderStyle.WAVY)
                                onSquigglySliderChange(false)
                                showSliderOptionDialog = false
                            }
                            .padding(12.dp)
                    ) {
                        val sliderValue = 0.5f
                        WavySlider(
                            value = sliderValue,
                            valueRange = 0f..1f,
                                    
                            onValueChange = {  },
                            colors = sliderPreviewColors,
                            modifier = Modifier.weight(1f),
                            isPlaying = true,
                            enabled = false
                        )
                        Text(
                            text = stringResource(R.string.wavy),
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .aspectRatio(1f)
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                1.dp,
                                if (sliderStyle == SliderStyle.SLIM) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                onSliderStyleChange(SliderStyle.SLIM)
                                onSquigglySliderChange(false)
                                showSliderOptionDialog = false
                            }
                            .padding(12.dp)
                    ) {
                        val sliderValue = 0.65f
                        Slider(
                            value = sliderValue,
                            valueRange = 0f..1f,
                                    
                            onValueChange = {  },
                            thumb = { Spacer(modifier = Modifier.size(0.dp)) },
                            track = { sliderState ->
                                PlayerSliderTrack(
                                    sliderState = sliderState,
                                    colors = sliderPreviewColors
                                )
                            },
                            colors = sliderPreviewColors,
                            enabled = false,
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = stringResource(R.string.slim),
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .aspectRatio(1f)
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                1.dp,
                                if (sliderStyle == SliderStyle.WAVY && squigglySlider) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                onSliderStyleChange(SliderStyle.WAVY)
                                onSquigglySliderChange(true)
                                showSliderOptionDialog = false
                            }
                            .padding(12.dp)
                    ) {
                        val sliderValue = 0.5f
                        SquigglySlider(
                            value = sliderValue,
                            valueRange = 0f..1f,
                                    
                            onValueChange = {  },
                            modifier = Modifier.weight(1f),
                            enabled = false,
                            colors = sliderPreviewColors,
                            isPlaying = true,
                        )
                        Text(
                            text = stringResource(R.string.squiggly),
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }

    Column(
        Modifier
            .windowInsetsPadding(
                LocalPlayerAwareWindowInsets.current.only(
                    WindowInsetsSides.Horizontal
                )
            )
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp),
    ) {
        Spacer(
            Modifier.windowInsetsPadding(
                LocalPlayerAwareWindowInsets.current.only(
                    WindowInsetsSides.Top
                )
            )
        )
        Material3SettingsGroup(scrollState = scrollState, 
            title = stringResource(R.string.theme),
            items = buildList {






















                add(
                    Material3SettingsItem(
    isHighlighted = (highlightKey == stringResource(R.string.theme)),
                        icon = painterResource(R.drawable.palette),
                        title = { Text(stringResource(R.string.theme)) },
                        description = { Text(stringResource(R.string.theme_desc)) },
                        onClick = { navController.navigate("settings/appearance/theme") }
                    )
                )
            }
        )

        Spacer(modifier = Modifier.height(27.dp))

        Material3SettingsGroup(scrollState = scrollState, 
            title = stringResource(id = R.string.mini_player),
            items = buildList {
                add(
                    Material3SettingsItem(
    isHighlighted = (highlightKey == stringResource(R.string.miniplayer_background_style)),
                        icon = painterResource(R.drawable.palette),
                        title = { Text(stringResource(R.string.miniplayer_background_style)) },
                        description = {
                            Text(
                                when (miniPlayerBackground) {
                                    PlayerBackgroundStyle.DEFAULT -> stringResource(R.string.follow_theme)
                                    PlayerBackgroundStyle.GRADIENT -> stringResource(R.string.gradient)
                                    PlayerBackgroundStyle.BLUR -> stringResource(R.string.player_background_blur)
                                    PlayerBackgroundStyle.GLOW_ANIMATED -> stringResource(R.string.glow_animated)
                                    PlayerBackgroundStyle.LIVE_MESH -> stringResource(R.string.live_mesh)
                                    else -> stringResource(R.string.follow_theme)
                                }
                            )
                        },
                        onClick = { showMiniPlayerBackgroundDialog = true }
                    )
                )
            }
        )

        Spacer(modifier = Modifier.height(27.dp))

        val (thumbnailCornerRadius, onThumbnailCornerRadiusChange) = rememberPreference(
            ThumbnailCornerRadiusKey,
            defaultValue = 3f
        )
        
        var showSensitivityDialog by rememberSaveable { mutableStateOf(false) }
        var showThumbnailCornerRadiusDialog by rememberSaveable { mutableStateOf(false) }

        Material3SettingsGroup(scrollState = scrollState, 
            title = stringResource(R.string.player),
            items = listOfNotNull(
                Material3SettingsItem(
    isHighlighted = (highlightKey == stringResource(R.string.player_buttons_style)),
                    icon = painterResource(R.drawable.palette),
                    title = { Text(stringResource(R.string.player_buttons_style)) },
                    description = {
                        Text(
                            when (playerButtonsStyle) {
                                PlayerButtonsStyle.DEFAULT -> stringResource(R.string.default_style)
                                PlayerButtonsStyle.PRIMARY -> stringResource(R.string.primary_color_style)
                                PlayerButtonsStyle.TERTIARY -> stringResource(R.string.tertiary_color_style)
                            }
                        )
                    },
                    onClick = { showPlayerButtonsStyleDialog = true }
                ),
                Material3SettingsItem(
    isHighlighted = (highlightKey == stringResource(R.string.enable_swipe_thumbnail)),
                    icon = painterResource(R.drawable.swipe),
                    title = { Text(stringResource(R.string.enable_swipe_thumbnail)) },
                    trailingContent = {
                        Switch(
                            checked = swipeThumbnail,
                            onCheckedChange = onSwipeThumbnailChange,
                            thumbContent = {
                                Icon(
                                    painter = painterResource(
                                        id = if (swipeThumbnail) R.drawable.check else R.drawable.close
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        )
                    },
                    onClick = { onSwipeThumbnailChange(!swipeThumbnail) }
                ),
                if (swipeThumbnail) Material3SettingsItem(
    isHighlighted = (highlightKey == stringResource(R.string.swipe_sensitivity)),
                    icon = painterResource(R.drawable.tune),
                    title = { Text(stringResource(R.string.swipe_sensitivity)) },
                    description = {
                        Text(
                            stringResource(
                                R.string.sensitivity_percentage,
                                (swipeSensitivity * 100).roundToInt()
                            )
                        )
                    },
                    onClick = { showSensitivityDialog = true }
                ) else null
            )
        )

        if (showSensitivityDialog) {
            var tempSensitivity by remember { mutableFloatStateOf(swipeSensitivity) }

            DefaultDialog(
                onDismiss = {
                    tempSensitivity = swipeSensitivity
                    showSensitivityDialog = false
                },
                buttons = {
                    TextButton(
                        onClick = {
                            tempSensitivity = 0.73f
                        }
                    ) {
                        Text(stringResource(R.string.reset))
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    TextButton(
                        onClick = {
                            tempSensitivity = swipeSensitivity
                            showSensitivityDialog = false
                        }
                    ) {
                        Text(stringResource(android.R.string.cancel))
                    }
                    TextButton(
                        onClick = {
                            onSwipeSensitivityChange(tempSensitivity)
                            showSensitivityDialog = false
                        }
                    ) {
                        Text(stringResource(android.R.string.ok))
                    }
                }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.swipe_sensitivity),
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        text = stringResource(
                            R.string.sensitivity_percentage,
                            (tempSensitivity * 100).roundToInt()
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Slider(
                        value = tempSensitivity,
                        onValueChange = { tempSensitivity = it },
                        valueRange = 0f..1f,
                                    
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        if (showThumbnailCornerRadiusDialog) {
            ThumbnailCornerRadiusModal(
                initialRadius = thumbnailCornerRadius,
                onDismiss = { showThumbnailCornerRadiusDialog = false },
                onRadiusSelected = { radius ->
                    onThumbnailCornerRadiusChange(radius)
                    showThumbnailCornerRadiusDialog = false
                }
            )
        }

        Spacer(modifier = Modifier.height(27.dp))

        Material3SettingsGroup(scrollState = scrollState, 
            title = stringResource(R.string.lyrics),
            items = listOfNotNull(
                Material3SettingsItem(
    isHighlighted = (highlightKey == stringResource(R.string.lyrics_glow_effect)),
                    icon = painterResource(R.drawable.lyrics),
                    title = { Text(stringResource(R.string.lyrics_glow_effect)) },
                    description = { Text(stringResource(R.string.lyrics_glow_effect_desc)) },
                    trailingContent = {
                        Switch(
                            checked = lyricsGlowEffect,
                            onCheckedChange = onLyricsGlowEffectChange,
                            thumbContent = {
                                Icon(
                                    painter = painterResource(
                                        id = if (lyricsGlowEffect) R.drawable.check else R.drawable.close
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        )
                    },
                    onClick = { onLyricsGlowEffectChange(!lyricsGlowEffect) }
                ),
                if (lyricsAnimationStyle == LyricsAnimationStyle.echomusic_1) {
                    Material3SettingsItem(
    isHighlighted = (highlightKey == stringResource(R.string.apple_music_lyrics_blur)),
                        icon = painterResource(R.drawable.lyrics),
                        title = { Text(stringResource(R.string.apple_music_lyrics_blur)) },
                        description = { Text(stringResource(R.string.apple_music_lyrics_blur_desc)) },
                        trailingContent = {
                            Switch(
                                checked = appleMusicLyricsBlur,
                                onCheckedChange = onAppleMusicLyricsBlurChange,
                                thumbContent = {
                                    Icon(
                                        painter = painterResource(
                                            id = if (appleMusicLyricsBlur) R.drawable.check else R.drawable.close
                                        ),
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize)
                                    )
                                }
                            )
                        },
                        onClick = { onAppleMusicLyricsBlurChange(!appleMusicLyricsBlur) }
                    )
                } else null,
                Material3SettingsItem(
    isHighlighted = (highlightKey == stringResource(R.string.standard_lyrics_blur)),
                    icon = painterResource(R.drawable.lyrics),
                    title = { Text(stringResource(R.string.standard_lyrics_blur)) },
                    description = { Text(stringResource(R.string.apple_music_lyrics_blur_desc)) },
                    trailingContent = {
                        Switch(
                            checked = lyricsStandardBlur,
                            onCheckedChange = onLyricsStandardBlurChange,
                            thumbContent = {
                                Icon(
                                    painter = painterResource(
                                        id = if (lyricsStandardBlur) R.drawable.check else R.drawable.close
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        )
                    },
                    onClick = { onLyricsStandardBlurChange(!lyricsStandardBlur) }
                ),
                Material3SettingsItem(
    isHighlighted = (highlightKey == stringResource(R.string.lyrics_click_change)),
                    icon = painterResource(R.drawable.lyrics),
                    title = { Text(stringResource(R.string.lyrics_click_change)) },
                    trailingContent = {
                        Switch(
                            checked = lyricsClick,
                            onCheckedChange = onLyricsClickChange,
                            thumbContent = {
                                Icon(
                                    painter = painterResource(
                                        id = if (lyricsClick) R.drawable.check else R.drawable.close
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        )
                    },
                    onClick = { onLyricsClickChange(!lyricsClick) }
                ),
                Material3SettingsItem(
    isHighlighted = (highlightKey == stringResource(R.string.lyrics_auto_scroll)),
                    icon = painterResource(R.drawable.lyrics),
                    title = { Text(stringResource(R.string.lyrics_auto_scroll)) },
                    trailingContent = {
                        Switch(
                            checked = lyricsScroll,
                            onCheckedChange = onLyricsScrollChange,
                            thumbContent = {
                                Icon(
                                    painter = painterResource(
                                        id = if (lyricsScroll) R.drawable.check else R.drawable.close
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        )
                    },
                    onClick = { onLyricsScrollChange(!lyricsScroll) }
                ),
                Material3SettingsItem(
    isHighlighted = (highlightKey == stringResource(R.string.lyrics_swipe_to_change_song)),
                    icon = painterResource(R.drawable.swipe),
                    title = { Text(stringResource(R.string.lyrics_swipe_to_change_song)) },
                    description = { Text(stringResource(R.string.lyrics_swipe_to_change_song_desc)) },
                    trailingContent = {
                        Switch(
                            checked = swipeLyrics,
                            onCheckedChange = onSwipeLyricsChange,
                            thumbContent = {
                                Icon(
                                    painter = painterResource(
                                        id = if (swipeLyrics) R.drawable.check else R.drawable.close
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        )
                    },
                    onClick = { onSwipeLyricsChange(!swipeLyrics) }
                ),
                Material3SettingsItem(
    isHighlighted = (highlightKey == stringResource(R.string.lyrics_thumbnail_play_pause)),
                    icon = painterResource(R.drawable.play),
                    title = { Text(stringResource(R.string.lyrics_thumbnail_play_pause)) },
                    description = { Text(stringResource(R.string.lyrics_thumbnail_play_pause_desc)) },
                    trailingContent = {
                        Switch(
                            checked = enableLyricsThumbnailPlayPause,
                            onCheckedChange = onEnableLyricsThumbnailPlayPauseChange,
                            thumbContent = {
                                Icon(
                                    painter = painterResource(
                                        id = if (enableLyricsThumbnailPlayPause) R.drawable.check else R.drawable.close
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        )
                    },
                    onClick = { onEnableLyricsThumbnailPlayPauseChange(!enableLyricsThumbnailPlayPause) }
                ),
                Material3SettingsItem(
    isHighlighted = (highlightKey == stringResource(R.string.hide_status_bar_on_fullscreen)),
                    icon = painterResource(R.drawable.fullscreen),
                    title = { Text(stringResource(R.string.hide_status_bar_on_fullscreen)) },
                    description = { Text(stringResource(R.string.hide_status_bar_on_fullscreen_desc)) },
                    trailingContent = {
                        Switch(
                            checked = hideStatusBarOnFullscreen,
                            onCheckedChange = onHideStatusBarOnFullscreenChange,
                            thumbContent = {
                                Icon(
                                    painter = painterResource(
                                        id = if (hideStatusBarOnFullscreen) R.drawable.check else R.drawable.close
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        )
                    },
                    onClick = { onHideStatusBarOnFullscreenChange(!hideStatusBarOnFullscreen) }
                )
            )
        )

        Spacer(modifier = Modifier.height(27.dp))

        Material3SettingsGroup(scrollState = scrollState, 
            title = stringResource(R.string.misc),
            items = listOf(
                Material3SettingsItem(
    isHighlighted = (highlightKey == stringResource(R.string.default_open_tab)),
                    icon = painterResource(R.drawable.nav_bar),
                    title = { Text(stringResource(R.string.default_open_tab)) },
                    description = {
                        Text(
                            when (defaultOpenTab) {
                                NavigationTab.HOME -> stringResource(R.string.home)
                                NavigationTab.SEARCH -> stringResource(R.string.search)
                                NavigationTab.LIBRARY -> stringResource(R.string.filter_library)
                            }
                        )
                    },
                    onClick = { showDefaultOpenTabDialog = true }
                ),
                Material3SettingsItem(
    isHighlighted = (highlightKey == stringResource(R.string.default_lib_chips)),
                    icon = painterResource(R.drawable.tab),
                    title = { Text(stringResource(R.string.default_lib_chips)) },
                    description = {
                        Text(
                            when (defaultChip) {
                                LibraryFilter.SONGS -> stringResource(R.string.songs)
                                LibraryFilter.ARTISTS -> stringResource(R.string.artists)
                                LibraryFilter.ALBUMS -> stringResource(R.string.albums)
                                LibraryFilter.PLAYLISTS -> stringResource(R.string.playlists)
                                LibraryFilter.LIBRARY -> stringResource(R.string.filter_library)
                                LibraryFilter.LOCAL -> stringResource(R.string.filter_local)
                            }
                        )
                    },
                    onClick = { showDefaultChipDialog = true }
                ),
                Material3SettingsItem(
    isHighlighted = (highlightKey == stringResource(R.string.swipe_song_to_add)),
                    icon = painterResource(R.drawable.swipe),
                    title = { Text(stringResource(R.string.swipe_song_to_add)) },
                    trailingContent = {
                        Switch(
                            checked = swipeToSong,
                            onCheckedChange = onSwipeToSongChange,
                            thumbContent = {
                                Icon(
                                    painter = painterResource(
                                        id = if (swipeToSong) R.drawable.check else R.drawable.close
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        )
                    },
                    onClick = { onSwipeToSongChange(!swipeToSong) }
                ),
                Material3SettingsItem(
    isHighlighted = (highlightKey == stringResource(R.string.swipe_song_to_remove)),
                    icon = painterResource(R.drawable.swipe),
                    title = { Text(stringResource(R.string.swipe_song_to_remove)) },
                    trailingContent = {
                        Switch(
                            checked = swipeToRemoveSong,
                            onCheckedChange = onSwipeToRemoveSongChange,
                            thumbContent = {
                                Icon(
                                    painter = painterResource(
                                        id = if (swipeToRemoveSong) R.drawable.check else R.drawable.close
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        )
                    },
                    onClick = { onSwipeToRemoveSongChange(!swipeToRemoveSong) }
                ),
                Material3SettingsItem(
    isHighlighted = (highlightKey == stringResource(R.string.listen_together_in_top_bar)),
                    icon = painterResource(R.drawable.group_outlined),
                    title = { Text(stringResource(R.string.listen_together_in_top_bar)) },
                    description = { Text(stringResource(R.string.listen_together_in_top_bar_desc)) },
                    trailingContent = {
                        Switch(
                            checked = listenTogetherInTopBar,
                            onCheckedChange = onListenTogetherInTopBarChange,
                            thumbContent = {
                                Icon(
                                    painter = painterResource(
                                        id = if (listenTogetherInTopBar) R.drawable.check else R.drawable.close
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        )
                    },
                    onClick = { onListenTogetherInTopBarChange(!listenTogetherInTopBar) }
                ),
                Material3SettingsItem(
    isHighlighted = (highlightKey == stringResource(R.string.grid_cell_size)),
                    icon = painterResource(R.drawable.grid_view),
                    title = { Text(stringResource(R.string.grid_cell_size)) },
                    description = {
                        Text(
                            when (gridItemSize) {
                                GridItemSize.BIG -> stringResource(R.string.big)
                                GridItemSize.SMALL -> stringResource(R.string.small)
                            }
                        )
                    },
                    onClick = { showGridSizeDialog = true }
                ),
                Material3SettingsItem(
    isHighlighted = (highlightKey == stringResource(R.string.display_density)),
                    icon = painterResource(R.drawable.grid_view),
                    title = { Text(stringResource(R.string.display_density)) },
                    description = {
                        Text(DensityScale.fromValue(densityScale).label)
                    },
                    onClick = { showDensityScaleDialog = true }
                )
            )
        )

        Spacer(modifier = Modifier.height(27.dp))

        Material3SettingsGroup(scrollState = scrollState, 
            title = stringResource(R.string.auto_playlists),
            items = listOf(
                Material3SettingsItem(
    isHighlighted = (highlightKey == stringResource(R.string.show_liked_playlist)),
                    icon = painterResource(R.drawable.favorite),
                    title = { Text(stringResource(R.string.show_liked_playlist)) },
                    trailingContent = {
                        Switch(
                            checked = showLikedPlaylist,
                            onCheckedChange = onShowLikedPlaylistChange,
                            thumbContent = {
                                Icon(
                                    painter = painterResource(
                                        id = if (showLikedPlaylist) R.drawable.check else R.drawable.close
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        )
                    },
                    onClick = { onShowLikedPlaylistChange(!showLikedPlaylist) }
                ),
                Material3SettingsItem(
    isHighlighted = (highlightKey == stringResource(R.string.show_downloaded_playlist)),
                    icon = painterResource(R.drawable.offline),
                    title = { Text(stringResource(R.string.show_downloaded_playlist)) },
                    trailingContent = {
                        Switch(
                            checked = showDownloadedPlaylist,
                            onCheckedChange = onShowDownloadedPlaylistChange,
                            thumbContent = {
                                Icon(
                                    painter = painterResource(
                                        id = if (showDownloadedPlaylist) R.drawable.check else R.drawable.close
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        )
                    },
                    onClick = { onShowDownloadedPlaylistChange(!showDownloadedPlaylist) }
                ),
                Material3SettingsItem(
    isHighlighted = (highlightKey == stringResource(R.string.action_exported)),
                    icon = painterResource(R.drawable.download),
                    title = { Text(stringResource(R.string.action_exported)) },
                    trailingContent = {
                        Switch(
                            checked = showExportedPlaylist,
                            onCheckedChange = onShowExportedPlaylistChange,
                            thumbContent = {
                                Icon(
                                    painter = painterResource(
                                        id = if (showExportedPlaylist) R.drawable.check else R.drawable.close
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        )
                    },
                    onClick = { onShowExportedPlaylistChange(!showExportedPlaylist) }
                ),
                Material3SettingsItem(
    isHighlighted = (highlightKey == stringResource(R.string.show_top_playlist)),
                    icon = painterResource(R.drawable.trending_up),
                    title = { Text(stringResource(R.string.show_top_playlist)) },
                    trailingContent = {
                        Switch(
                            checked = showTopPlaylist,
                            onCheckedChange = onShowTopPlaylistChange,
                            thumbContent = {
                                Icon(
                                    painter = painterResource(
                                        id = if (showTopPlaylist) R.drawable.check else R.drawable.close
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        )
                    },
                    onClick = { onShowTopPlaylistChange(!showTopPlaylist) }
                ),
                Material3SettingsItem(
    isHighlighted = (highlightKey == stringResource(R.string.show_cached_playlist)),
                    icon = painterResource(R.drawable.cached),
                    title = { Text(stringResource(R.string.show_cached_playlist)) },
                    trailingContent = {
                        Switch(
                            checked = showCachedPlaylist,
                            onCheckedChange = onShowCachedPlaylistChange,
                            thumbContent = {
                                Icon(
                                    painter = painterResource(
                                        id = if (showCachedPlaylist) R.drawable.check else R.drawable.close
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        )
                    },
                    onClick = { onShowCachedPlaylistChange(!showCachedPlaylist) }
                )
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
    
        Spacer(Modifier.windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Bottom)))
    }

    TopAppBar(
        title = { Text(stringResource(R.string.appearance)) },
        navigationIcon = {
            IconButton(
                onClick = navController::navigateUp,
                onLongClick = navController::backToMain,
            ) {
                Icon(
                    painterResource(R.drawable.arrow_back),
                    contentDescription = null,
                )
            }
        }
    )
}

enum class DarkMode {
    ON,
    OFF,
    AUTO,
}

enum class NavigationTab {
    HOME,
    SEARCH,
    LIBRARY,
}

enum class LyricsPosition {
    LEFT,
    CENTER,
    RIGHT,
}

enum class PlayerTextAlignment {
    SIDED,
    CENTER,
}
