

package zyra.echo.music.ui.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

import zyra.echo.music.ui.utils.scrollToOnHighlight
import androidx.compose.foundation.ScrollState
import androidx.compose.material3.HorizontalDivider

@Composable
fun Material3SettingsGroup(
    title: String? = null,
    compact: Boolean = false,
    scrollState: ScrollState? = null,
    items: List<Material3SettingsItem>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        title?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = if (compact) 4.dp else 8.dp, top = if (compact) 4.dp else 8.dp)
            )
        }

        val containerShape = RoundedCornerShape(24.dp)
        val groupGlassBg = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.12f),
                Color.White.copy(alpha = 0.04f)
            )
        )
        val groupBorderColor = Color.White.copy(alpha = 0.22f)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(containerShape)
                .background(groupGlassBg)
                .border(1.dp, groupBorderColor, containerShape)
                .padding(vertical = 4.dp, horizontal = 4.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                items.forEachIndexed { index, item ->
                    if (index > 0) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = Color.White.copy(alpha = 0.12f),
                            thickness = 0.8.dp
                        )
                    }

                    val itemShape = RoundedCornerShape(16.dp)

                    val itemModifier = if (item.isHighlighted) {
                        val glassBrush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.25f),
                                Color.White.copy(alpha = 0.12f)
                            )
                        )
                        Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                            .clip(itemShape)
                            .background(glassBrush)
                            .border(1.dp, Color.White.copy(alpha = 0.45f), itemShape)
                    } else {
                        Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                            .clip(itemShape)
                    }

                    Box(
                        modifier = itemModifier
                    ) {
                        Material3SettingsItemRow(item = item, compact = compact, scrollState = scrollState)
                    }
                }
            }
        }
    }
}


@Composable
private fun Material3SettingsItemRow(
    item: Material3SettingsItem,
    compact: Boolean = false,
    scrollState: ScrollState? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = item.enabled && item.onClick != null,
                onClick = { item.onClick?.invoke() }
            )
            .then(if (scrollState != null) Modifier.scrollToOnHighlight(scrollState, item.isHighlighted) else Modifier)
            .padding(
                horizontal = if (compact) 14.dp else 20.dp, 
                vertical = if (compact) 10.dp else 16.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        
        item.icon?.let { icon ->
            Box(
                modifier = Modifier
                    .size(if (compact) 34.dp else 40.dp)
                    .clip(item.iconShape ?: RoundedCornerShape(12.dp))
                    .background(
                        if (item.tintIcon) {
                            MaterialTheme.colorScheme.primary.copy(
                                alpha = if (item.isHighlighted) 0.15f else 0.1f
                            )
                        } else {
                            androidx.compose.ui.graphics.Color.Transparent
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (item.showBadge) {
                    BadgedBox(
                        badge = {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        }
                    ) {
                        if (item.tintIcon) {
                            Icon(
                                painter = icon,
                                contentDescription = null,
                                tint = if (!item.enabled)
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                else if (item.isHighlighted)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                                modifier = Modifier.size(if (compact) 20.dp else 24.dp)
                            )
                        } else {
                            Image(
                                painter = icon,
                                contentDescription = null,
                                modifier = Modifier.size(if (compact) 34.dp else 40.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                } else {
                    if (item.tintIcon) {
                        Icon(
                            painter = icon,
                            contentDescription = null,
                            tint = if (!item.enabled)
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            else if (item.isHighlighted)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                            modifier = Modifier.size(if (compact) 20.dp else 24.dp)
                        )
                    } else {
                        Image(
                            painter = icon,
                            contentDescription = null,
                            modifier = Modifier.size(if (compact) 34.dp else 40.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(if (compact) 12.dp else 16.dp))
        }

        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            
            ProvideTextStyle(
                MaterialTheme.typography.titleMedium.copy(
                    color = if (!item.enabled) 
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    else
                        MaterialTheme.colorScheme.onSurface
                )
            ) {
                item.title()
            }

            
            item.description?.let { desc ->
                Spacer(modifier = Modifier.height(2.dp))
                ProvideTextStyle(
                    MaterialTheme.typography.bodyMedium.copy(
                        color = if (!item.enabled)
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    desc()
                }
            }
        }

        
        item.trailingContent?.let { trailing ->
            Spacer(modifier = Modifier.width(8.dp))
            trailing()
        }
    }
}


data class Material3SettingsItem(
    val icon: Painter? = null,
    val title: @Composable () -> Unit,
    val description: (@Composable () -> Unit)? = null,
    val trailingContent: (@Composable () -> Unit)? = null,
    val showBadge: Boolean = false,
    val isHighlighted: Boolean = false,
    val tintIcon: Boolean = true,
    val iconShape: Shape? = null,
    val enabled: Boolean = true,
    val onClick: (() -> Unit)? = null
)
