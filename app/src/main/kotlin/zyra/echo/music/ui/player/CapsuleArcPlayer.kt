package zyra.echo.music.ui.player

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import kotlinx.coroutines.delay
import zyra.echo.music.LocalDatabase
import zyra.echo.music.LocalSyncUtils
import zyra.echo.music.R
import zyra.echo.music.playback.PlayerConnection
import zyra.echo.music.utils.makeTimeString
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CapsuleArcPlayerView(
    playerConnection: PlayerConnection,
    onCollapse: () -> Unit,
    onOpenQueue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val database = LocalDatabase.current
    val syncUtils = LocalSyncUtils.current

    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val isPlaying by playerConnection.isPlaying.collectAsState()

    var positionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(1L) }

    LaunchedEffect(isPlaying, mediaMetadata?.id) {
        while (true) {
            try {
                positionMs = playerConnection.player.currentPosition.coerceAtLeast(0L)
                durationMs = playerConnection.player.duration.coerceAtLeast(1L)
            } catch (e: Exception) {
                // Ignore player transient errors
            }
            delay(200L)
        }
    }

    val shuffleEnabled = playerConnection.player.shuffleModeEnabled

    val currentSongEntity by database.song(mediaMetadata?.id ?: "").collectAsState(initial = null)
    val isLiked = currentSongEntity?.song?.liked == true

    val progressFraction = (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)

    var isSeeking by remember { mutableStateOf(false) }
    var seekProgress by remember { mutableFloatStateOf(0f) }

    val activeFraction = if (isSeeking) seekProgress else progressFraction

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF6B5E8A),
                        Color(0xFF453B66),
                        Color(0xFF282344),
                        Color(0xFF1B1731)
                    )
                )
            )
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar: Back & Brand & Queue
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCollapse) {
                    Icon(
                        painter = painterResource(R.drawable.arrow_back),
                        contentDescription = "Collapse Player",
                        tint = Color.White
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AsyncImage(
                        model = R.mipmap.ic_launcher_round,
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                    )
                    Text(
                        text = "Zyra",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = Color.White
                    )
                }

                IconButton(onClick = onOpenQueue) {
                    Icon(
                        painter = painterResource(R.drawable.queue_music),
                        contentDescription = "Open Queue",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3D Glass Carousel Artwork Layout
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentAlignment = Alignment.Center
            ) {
                // Side Left Artwork Card Preview
                Box(
                    modifier = Modifier
                        .size(200.dp, 250.dp)
                        .padding(end = 120.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(mediaMetadata?.thumbnailUrl)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(24.dp))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f))
                    )
                }

                // Side Right Artwork Card Preview
                Box(
                    modifier = Modifier
                        .size(200.dp, 250.dp)
                        .padding(start = 120.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(mediaMetadata?.thumbnailUrl)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(24.dp))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f))
                    )
                }

                // Center Glassmorphic Main Album Cover Card
                Box(
                    modifier = Modifier
                        .size(230.dp, 275.dp)
                        .shadow(24.dp, shape = RoundedCornerShape(28.dp), clip = false)
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .border(1.5.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(28.dp)),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(mediaMetadata?.thumbnailUrl)
                            .build(),
                        contentDescription = mediaMetadata?.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Bottom Gradient & Details Inside Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.82f)
                                    )
                                )
                            )
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = mediaMetadata?.artists?.joinToString { it.name } ?: "Zyra Music",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = mediaMetadata?.title ?: "Playing",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.75f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Subtitle Caption
            Text(
                text = mediaMetadata?.title ?: "Enjoy your favorite music with Zyra",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Arc Progress Slider
            ArcProgressSlider(
                progressFraction = activeFraction,
                onSeekStarted = {
                    isSeeking = true
                    seekProgress = progressFraction
                },
                onSeekProgress = { fraction ->
                    seekProgress = fraction
                },
                onSeekFinished = { finalFraction ->
                    isSeeking = false
                    val targetMs = (finalFraction * durationMs).toLong()
                    try {
                        playerConnection.player.seekTo(targetMs)
                    } catch (e: Exception) { }
                },
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .height(70.dp)
            )

            // Digital Time Display
            val displayMs = if (isSeeking) (seekProgress * durationMs).toLong() else positionMs
            Text(
                text = "${makeTimeString(displayMs)} / ${makeTimeString(durationMs)}",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                ),
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1f))

            // Floating Glassmorphic Pill Player Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
                    .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left Controls: Previous, Play/Pause, Next
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        IconButton(
                            onClick = { playerConnection.player.seekToPrevious() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.skip_previous),
                                contentDescription = "Previous",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = { if (isPlaying) playerConnection.player.pause() else playerConnection.player.play() },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                painter = painterResource(if (isPlaying) R.drawable.pause else R.drawable.play),
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        IconButton(
                            onClick = { playerConnection.player.seekToNext() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.skip_next),
                                contentDescription = "Next",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Center Glass Mini Song Pill
                    Row(
                        modifier = Modifier
                            .height(44.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color.Black.copy(alpha = 0.35f))
                            .border(0.8.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(22.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(mediaMetadata?.thumbnailUrl)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )

                        Column(
                            modifier = Modifier.widthIn(max = 85.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = mediaMetadata?.title ?: "Zyra",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = mediaMetadata?.artists?.firstOrNull()?.name ?: "Playing",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = Color.White.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Right Actions: Shuffle & Like
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        IconButton(
                            onClick = { playerConnection.player.shuffleModeEnabled = !shuffleEnabled },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.shuffle),
                                contentDescription = "Shuffle",
                                tint = if (shuffleEnabled) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                mediaMetadata?.let {
                                    database.query {
                                        currentSongEntity?.song?.let { song ->
                                            val updated = song.toggleLike()
                                            update(updated)
                                            syncUtils.likeSong(updated)
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                painter = painterResource(if (isLiked) R.drawable.favorite else R.drawable.favorite_border),
                                contentDescription = "Favorite",
                                tint = if (isLiked) Color(0xFFFF5252) else Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

/**
 * Custom Arc Progress Slider Composable that draws a curved arc surrounding the capsule artwork
 */
@Composable
fun ArcProgressSlider(
    progressFraction: Float,
    onSeekStarted: () -> Unit,
    onSeekProgress: (Float) -> Unit,
    onSeekFinished: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    val progressColor = MaterialTheme.colorScheme.onSurface
    val thumbColor = MaterialTheme.colorScheme.onSurface

    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        onSeekStarted()
                        val fraction = calculateArcFraction(offset, size.width.toFloat(), size.height.toFloat())
                        onSeekProgress(fraction)
                        tryAwaitRelease()
                        onSeekFinished(fraction)
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        onSeekStarted()
                        val fraction = calculateArcFraction(offset, size.width.toFloat(), size.height.toFloat())
                        onSeekProgress(fraction)
                    },
                    onDrag = { change, _ ->
                        val fraction = calculateArcFraction(change.position, size.width.toFloat(), size.height.toFloat())
                        onSeekProgress(fraction)
                    },
                    onDragEnd = {
                        onSeekFinished(progressFraction)
                    },
                    onDragCancel = {
                        onSeekFinished(progressFraction)
                    }
                )
            }
    ) {
        val width = size.width
        val height = size.height

        val strokeWidth = 6.dp.toPx()
        val thumbRadius = 8.dp.toPx()

        // Define arc bounds centered at bottom curve
        val arcRadius = (width * 0.45f)
        val centerX = width / 2f
        val centerY = -height * 0.35f

        val startAngle = 30f // deg
        val sweepAngle = 120f // deg

        val topLeft = Offset(centerX - arcRadius, centerY - arcRadius)
        val arcSize = Size(arcRadius * 2f, arcRadius * 2f)

        // Draw Background Track Arc
        drawArc(
            color = trackColor,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Draw Active Progress Arc
        val currentSweep = sweepAngle * progressFraction.coerceIn(0f, 1f)
        drawArc(
            color = progressColor,
            startAngle = startAngle,
            sweepAngle = currentSweep,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Calculate and Draw Thumb Knob position
        val currentAngleRad = Math.toRadians((startAngle + currentSweep).toDouble())
        val thumbX = centerX + arcRadius * cos(currentAngleRad).toFloat()
        val thumbY = centerY + arcRadius * sin(currentAngleRad).toFloat()

        drawCircle(
            color = thumbColor,
            radius = thumbRadius,
            center = Offset(thumbX, thumbY)
        )

        drawCircle(
            color = Color.Black,
            radius = thumbRadius * 0.4f,
            center = Offset(thumbX, thumbY)
        )
    }
}

private fun calculateArcFraction(offset: Offset, width: Float, height: Float): Float {
    val centerX = width / 2f
    val centerY = -height * 0.35f
    val dx = offset.x - centerX
    val dy = offset.y - centerY
    val angleRad = atan2(dy.toDouble(), dx.toDouble())
    var angleDeg = Math.toDegrees(angleRad).toFloat()
    if (angleDeg < 0) angleDeg += 360f

    val startAngle = 30f
    val sweepAngle = 120f

    val relativeAngle = (angleDeg - startAngle).coerceIn(0f, sweepAngle)
    return (relativeAngle / sweepAngle).coerceIn(0f, 1f)
}
