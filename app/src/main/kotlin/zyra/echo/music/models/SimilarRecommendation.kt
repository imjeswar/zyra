

package zyra.echo.music.models

import com.music.innertube.models.YTItem
import zyra.echo.music.db.entities.LocalItem

data class SimilarRecommendation(
    val title: LocalItem,
    val items: List<YTItem>,
)
