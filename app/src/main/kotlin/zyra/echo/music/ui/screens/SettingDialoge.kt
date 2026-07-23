package zyra.echo.music.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.music.innertube.utils.parseCookieString
import zyra.echo.music.BuildConfig
import zyra.echo.music.R
import zyra.echo.music.constants.AccountEmailKey
import zyra.echo.music.constants.InnerTubeCookieKey
import zyra.echo.music.constants.UseLoginForBrowse
import zyra.echo.music.constants.YtmSyncKey
import zyra.echo.music.ui.component.Material3SettingsGroup
import zyra.echo.music.ui.component.Material3SettingsItem
import zyra.echo.music.utils.rememberPreference
import zyra.echo.music.viewmodels.HomeViewModel
import androidx.compose.ui.layout.ContentScale

import androidx.compose.foundation.Image
import androidx.compose.ui.res.stringResource

@Composable
fun SettingDialoge(
    onDismissRequest: () -> Unit,
    onNavigate: (String) -> Unit,
    homeViewModel: HomeViewModel
) {
    val uriHandler = LocalUriHandler.current


    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val primaryColor = MaterialTheme.colorScheme.onSurface
        val onSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant

        Card(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
                .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF161420).copy(alpha = 0.92f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 16.dp, horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                ) {
                    Spacer(modifier = Modifier.size(24.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AsyncImage(
                            model = R.mipmap.ic_launcher_round,
                            contentDescription = null,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                        )
                        Text(
                            text = "Zyra",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = primaryColor,
                            textAlign = TextAlign.Center
                        )
                    }

                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.close),
                            contentDescription = "Close",
                            tint = primaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }



                Material3SettingsGroup(
                    title = "App",
                    compact = true,
                    items = listOf(
                        Material3SettingsItem(
                            title = { Text(stringResource(R.string.history)) },
                            icon = painterResource(R.drawable.music_history),
                            onClick = {
                                onDismissRequest()
                                onNavigate("history")
                            }
                        ),
                        Material3SettingsItem(
                            title = { Text(stringResource(R.string.stats)) },
                            icon = painterResource(R.drawable.stats),
                            onClick = {
                                onDismissRequest()
                                onNavigate("stats")
                            }
                        ),
                        Material3SettingsItem(
                            title = { Text(stringResource(R.string.settings)) },
                            icon = painterResource(R.drawable.settings),
                            onClick = {
                                onDismissRequest()
                                onNavigate("settings")
                            }
                        )
                    )
                )
            }
        }
    }
}
