

package zyra.echo.music.ui.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import zyra.echo.music.R
import zyra.echo.music.viewmodels.ConvertedSongLog
import zyra.echo.music.viewmodels.CsvImportState

@Composable
fun CsvColumnMappingDialog(
    isVisible: Boolean,
    csvState: CsvImportState,
    onDismiss: () -> Unit,
    onConfirm: (CsvImportState) -> Unit,
) {
    if (!isVisible) return

    var artistColumnIndex by remember { mutableIntStateOf(csvState.artistColumnIndex) }
    var titleColumnIndex by remember { mutableIntStateOf(csvState.titleColumnIndex) }
    var urlColumnIndex by remember { mutableIntStateOf(csvState.urlColumnIndex) }
    var hasHeader by remember { mutableStateOf(csvState.hasHeader) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
            ) {
                Text(
                    text = stringResource(R.string.map_csv_columns),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    if (csvState.previewRows.isNotEmpty()) {
                        val totalCols = csvState.previewRows.firstOrNull()?.size ?: 0
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "Preview",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = "$totalCols columns detected",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 160.dp)
                                    .horizontalScroll(rememberScrollState()),
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    repeat(totalCols) { colIndex ->
                                        val isArtist = colIndex == artistColumnIndex
                                        val isTitle = colIndex == titleColumnIndex
                                        val isUrl = colIndex == urlColumnIndex && urlColumnIndex >= 0

                                        val columnHeaderLabel = if (hasHeader && csvState.previewRows.isNotEmpty()) {
                                            csvState.previewRows[0].getOrNull(colIndex) ?: "Col ${colIndex + 1}"
                                        } else {
                                            "Col ${colIndex + 1}"
                                        }

                                        Column(
                                            modifier = Modifier
                                                .width(130.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    when {
                                                        isTitle -> MaterialTheme.colorScheme.primaryContainer
                                                        isArtist -> MaterialTheme.colorScheme.secondaryContainer
                                                        isUrl -> MaterialTheme.colorScheme.tertiaryContainer
                                                        else -> MaterialTheme.colorScheme.surface
                                                    }
                                                )
                                                .padding(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp),
                                        ) {
                                            Text(
                                                text = "Col ${colIndex + 1}: $columnHeaderLabel",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = when {
                                                    isTitle -> MaterialTheme.colorScheme.onPrimaryContainer
                                                    isArtist -> MaterialTheme.colorScheme.onSecondaryContainer
                                                    isUrl -> MaterialTheme.colorScheme.onTertiaryContainer
                                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                                },
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                fontFamily = FontFamily.Monospace,
                                            )

                                            val sampleData = if (hasHeader) csvState.previewRows.drop(1).take(3) else csvState.previewRows.take(3)
                                            sampleData.forEach { row ->
                                                val cellValue = row.getOrNull(colIndex) ?: ""
                                                if (cellValue.isNotBlank()) {
                                                    Text(
                                                        text = cellValue,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Checkbox(
                            checked = hasHeader,
                            onCheckedChange = { hasHeader = it },
                        )
                        Text(
                            text = stringResource(R.string.first_row_is_header),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }

                    ColumnSelector(
                        label = stringResource(R.string.artist_name_column),
                        selectedIndex = artistColumnIndex,
                        maxColumns = csvState.previewRows.firstOrNull()?.size ?: 0,
                        headerRow = if (hasHeader) csvState.previewRows.firstOrNull() else null,
                        onSelected = { artistColumnIndex = it },
                    )

                    ColumnSelector(
                        label = stringResource(R.string.song_title_column),
                        selectedIndex = titleColumnIndex,
                        maxColumns = csvState.previewRows.firstOrNull()?.size ?: 0,
                        headerRow = if (hasHeader) csvState.previewRows.firstOrNull() else null,
                        onSelected = { titleColumnIndex = it },
                    )

                    ColumnSelector(
                        label = stringResource(R.string.youtube_url_column),
                        selectedIndex = urlColumnIndex,
                        maxColumns = csvState.previewRows.firstOrNull()?.size ?: 0,
                        headerRow = if (hasHeader) csvState.previewRows.firstOrNull() else null,
                        allowNone = true,
                        onSelected = { urlColumnIndex = it },
                    )
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                    Button(
                        onClick = {
                            onConfirm(
                                CsvImportState(
                                    previewRows = csvState.previewRows,
                                    artistColumnIndex = artistColumnIndex,
                                    titleColumnIndex = titleColumnIndex,
                                    urlColumnIndex = urlColumnIndex,
                                    hasHeader = hasHeader,
                                )
                            )
                        },
                    ) {
                        Text(stringResource(R.string.continue_action))
                    }
                }
            }
        }
    }
}

@Composable
private fun ColumnSelector(
    label: String,
    selectedIndex: Int,
    maxColumns: Int,
    headerRow: List<String>? = null,
    allowNone: Boolean = false,
    onSelected: (Int) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (allowNone) {
                if (selectedIndex == -1) {
                    Button(
                        onClick = { onSelected(-1) },
                        modifier = Modifier.height(36.dp),
                    ) {
                        Text(stringResource(R.string.none), style = MaterialTheme.typography.labelSmall)
                    }
                } else {
                    OutlinedButton(
                        onClick = { onSelected(-1) },
                        modifier = Modifier.height(36.dp),
                    ) {
                        Text(stringResource(R.string.none), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            repeat(maxColumns) { index ->
                val headerName = headerRow?.getOrNull(index)?.take(14)
                val btnText = if (!headerName.isNullOrBlank()) {
                    "Col ${index + 1} ($headerName)"
                } else {
                    stringResource(R.string.column_label, index + 1)
                }

                if (selectedIndex == index) {
                    Button(
                        onClick = { onSelected(index) },
                        modifier = Modifier.height(36.dp),
                    ) {
                        Text(
                            btnText,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = { onSelected(index) },
                        modifier = Modifier.height(36.dp),
                    ) {
                        Text(
                            btnText,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CsvImportProgressDialog(
    isVisible: Boolean,
    progress: Int,
    recentLogs: List<ConvertedSongLog>,
    onDismiss: () -> Unit,
) {
    if (!isVisible) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = false, dismissOnClickOutside = false),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.importing_csv),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )

            LinearProgressIndicator(
                progress = { progress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
            )

            Text(
                text = stringResource(R.string.percentage_format, progress),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (recentLogs.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.recently_converted),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    recentLogs.forEach { log ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.background)
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = log.title,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = log.artists,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}
