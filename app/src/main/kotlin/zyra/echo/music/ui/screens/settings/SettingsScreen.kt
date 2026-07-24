

package zyra.echo.music.ui.screens.settings

import zyra.echo.music.R
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import android.net.Uri
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment

import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import zyra.echo.music.utils.AppUpdater

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import zyra.echo.music.BuildConfig
import zyra.echo.music.LocalPlayerAwareWindowInsets
import zyra.echo.music.ui.component.IconButton
import zyra.echo.music.ui.component.Material3SettingsGroup
import zyra.echo.music.ui.component.Material3SettingsItem
import zyra.echo.music.ui.screens.Screens
import zyra.echo.music.ui.utils.backToMain


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
highlightKey: String? = null) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isAndroid12OrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    var searchQuery by rememberSaveable { mutableStateOf("") }
    val searchLower = searchQuery.lowercase()

    val accountText = stringResource(R.string.account)
    val appearanceText = stringResource(R.string.appearance)
    val playerText = stringResource(R.string.player_and_audio)
    val listenTogetherText = stringResource(R.string.listen_together)
    val historyText = stringResource(R.string.history)
    val statsText = stringResource(R.string.stats)
    val contentText = stringResource(R.string.content)
    val privacyText = stringResource(R.string.privacy)
    val storageText = stringResource(R.string.storage)
    val backupText = stringResource(R.string.backup_restore)
    val aboutText = stringResource(R.string.about)

    val scrollState = rememberScrollState()
    Column(
        Modifier
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Horizontal))
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(
            Modifier.windowInsetsPadding(
                LocalPlayerAwareWindowInsets.current.only(
                    WindowInsetsSides.Top
                )
            )
        )
        Text(
            text = stringResource(R.string.settings),
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 8.dp, top = 24.dp, bottom = 16.dp)
        )

        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(stringResource(R.string.search), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = stringResource(R.string.search),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Rounded.Clear,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.10f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.06f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, bottom = 16.dp)
                .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(24.dp))
        )

        val itemsList = buildList {
            if ("zyra brain".contains(searchLower)) {
                add(
                    Material3SettingsItem(
    isHighlighted = (highlightKey == "Zyra Brain"),
                        icon = rememberVectorPainter(Icons.Outlined.AutoAwesome),
                        title = { Text("Zyra Brain") },
                        onClick = { navController.navigate("settings/echo_brain") }
                    )
                )
            }
            if (historyText.lowercase().contains(searchLower) || "watch history".contains(searchLower)) {
                add(
                    Material3SettingsItem(
                        isHighlighted = (highlightKey == historyText),
                        icon = painterResource(R.drawable.music_history),
                        title = { Text(historyText) },
                        onClick = { navController.navigate("history") }
                    )
                )
            }
            if (statsText.lowercase().contains(searchLower)) {
                add(
                    Material3SettingsItem(
                        isHighlighted = (highlightKey == statsText),
                        icon = painterResource(R.drawable.stats),
                        title = { Text(statsText) },
                        onClick = { navController.navigate("stats") }
                    )
                )
            }
            if (appearanceText.lowercase().contains(searchLower)) {
                add(
                    Material3SettingsItem(
    isHighlighted = (highlightKey == appearanceText),
                        icon = painterResource(R.drawable.palette),
                        title = { Text(appearanceText) },
                        onClick = { navController.navigate("settings/appearance") }
                    )
                )
            }
            if (playerText.lowercase().contains(searchLower)) {
                add(
                    Material3SettingsItem(
    isHighlighted = (highlightKey == playerText),
                        icon = painterResource(R.drawable.play),
                        title = { Text(playerText) },
                        onClick = { navController.navigate("settings/player") }
                    )
                )
            }
            if (listenTogetherText.lowercase().contains(searchLower)) {
                add(
                    Material3SettingsItem(
    isHighlighted = (highlightKey == listenTogetherText),
                        icon = painterResource(R.drawable.group),
                        title = { Text(listenTogetherText) },
                        onClick = { navController.navigate(Screens.ListenTogether.route) }
                    )
                )
            }
            if (contentText.lowercase().contains(searchLower)) {
                add(
                    Material3SettingsItem(
    isHighlighted = (highlightKey == contentText),
                        icon = painterResource(R.drawable.language),
                        title = { Text(contentText) },
                        onClick = { navController.navigate("settings/content") }
                    )
                )
            }

            if (privacyText.lowercase().contains(searchLower)) {
                add(
                    Material3SettingsItem(
    isHighlighted = (highlightKey == privacyText),
                        icon = painterResource(R.drawable.security),
                        title = { Text(privacyText) },
                        onClick = { navController.navigate("settings/privacy") }
                    )
                )
            }
            if (storageText.lowercase().contains(searchLower)) {
                add(
                    Material3SettingsItem(
    isHighlighted = (highlightKey == storageText),
                        icon = painterResource(R.drawable.storage),
                        title = { Text(storageText) },
                        onClick = { navController.navigate("settings/storage") }
                    )
                )
            }
            if (backupText.lowercase().contains(searchLower)) {
                add(
                    Material3SettingsItem(
    isHighlighted = (highlightKey == backupText),
                        icon = painterResource(R.drawable.restore),
                        title = { Text(backupText) },
                        onClick = { navController.navigate("settings/backup_restore") }
                    )
                )
            }
            if ("check for updates".contains(searchLower) || "update".contains(searchLower)) {
                add(
                    Material3SettingsItem(
                        icon = painterResource(R.drawable.restore),
                        title = { Text(stringResource(R.string.checking_for_updates)) },
                        description = { Text("App version: v${BuildConfig.VERSION_NAME}") },
                        onClick = {
                            coroutineScope.launch {
                                Toast.makeText(context, R.string.checking_for_updates, Toast.LENGTH_SHORT).show()
                                AppUpdater.checkForUpdate().onSuccess { release ->
                                    if (release == null) {
                                        Toast.makeText(context, R.string.latest_version_installed, Toast.LENGTH_SHORT).show()
                                        AppUpdater.openGitHubReleases(context)
                                    } else {
                                        Toast.makeText(context, "Redirecting to GitHub for update ${release.versionName}...", Toast.LENGTH_LONG).show()
                                        AppUpdater.openGitHubReleases(context, release.downloadUrl.ifEmpty { AppUpdater.GITHUB_RELEASES_PAGE_URL })
                                    }
                                }.onFailure {
                                    AppUpdater.openGitHubReleases(context)
                                }
                            }
                        }
                    )
                )
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if ("supported links".contains(searchLower)) {
                    add(
                        Material3SettingsItem(
                            isHighlighted = (highlightKey == "supported links"),
                            icon = painterResource(R.drawable.link),
                            title = { Text("Supported Links") },
                            onClick = {
                                try {
                                    val intent = Intent(
                                        Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS,
                                        Uri.parse("package:${context.packageName}")
                                    )
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    when (e) {
                                        is ActivityNotFoundException, is SecurityException -> {
                                            Toast.makeText(context, "Cannot open settings", Toast.LENGTH_SHORT).show()
                                        }
                                        else -> {
                                            Toast.makeText(context, "An error occurred", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                        )
                    )
                }
            }
        }

        val finalItemsList = if (searchQuery.isNotEmpty()) {
            val subSettings = getAllSearchableSettings()

            val matchedSubSettings = subSettings
                .filter { it.first.lowercase().contains(searchLower) }
                .groupBy { it.second }
                .map { (parentTitle, settingsInPage) ->
                    val route = settingsInPage.first().third
                    val title = settingsInPage.first().first
                    Material3SettingsItem(
                        icon = painterResource(R.drawable.search),
                        title = { Text(parentTitle) },
                        description = { Text("Contains ${settingsInPage.size} matching setting(s)") },
                        onClick = { 
                            val encodedTitle = java.net.URLEncoder.encode(title, "UTF-8")
                            val finalRoute = if (route.contains("?")) "$route&highlightKey=$encodedTitle" else "$route?highlightKey=$encodedTitle"
                            navController.navigate(finalRoute)
                        }
                    )
                }
            
            itemsList + matchedSubSettings
        } else {
            itemsList
        }

        if (finalItemsList.isEmpty() && searchQuery.isNotEmpty()) {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "No settings found for \"$searchQuery\"",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )
        } else {
            Material3SettingsGroup(scrollState = scrollState, items = finalItemsList)
        }
        
        Spacer(modifier = Modifier.height(50.dp))
        Spacer(
            Modifier.windowInsetsPadding(
                LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Bottom)
            )
        )
    }

    TopAppBar(
        title = {
            androidx.compose.animation.AnimatedVisibility(
                visible = scrollState.value > 100,
                enter = androidx.compose.animation.fadeIn(),
                exit = androidx.compose.animation.fadeOut()
            ) {
                Text(
                    text = stringResource(R.string.settings),
                    style = MaterialTheme.typography.titleLarge
                )
            }
        },
        navigationIcon = {
            IconButton(
                onClick = navController::navigateUp,
                onLongClick = navController::backToMain
            ) {
                Icon(
                    painterResource(R.drawable.arrow_back),
                    contentDescription = null
                )
            }
        }
    )
}
