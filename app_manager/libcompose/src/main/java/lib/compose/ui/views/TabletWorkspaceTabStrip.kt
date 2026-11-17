package lib.compose.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Brand colors aligned with Fluent 2 Office suite
val ColorWrite = FluentDesignTokens.BrandWord
val ColorSheet = FluentDesignTokens.BrandExcel
val ColorPresentation = FluentDesignTokens.BrandPowerPoint

data class TabletWorkspaceTab(
    val sessionId: String,
    val title: String,
    val editorKind: String, // WRITE, SHEET, PRESENTATION
    val isDirty: Boolean = false,
    val isActive: Boolean = false,
    val isCheckpointing: Boolean = false
)

/**
 * TabletWorkspaceTabStrip redesigned following Microsoft Fluent Design 2 specifications.
 * Features Mica layering, Fluent pill badges, subtle neutral strokes, and smooth tab elevations.
 */
@Composable
fun TabletWorkspaceTabStrip(
    tabs: List<TabletWorkspaceTab>,
    onTabSelected: (String) -> Unit,
    onTabClose: (String) -> Unit,
    onAddTab: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = false
) {
    val scrollState = rememberScrollState()

    val stripBg = if (isDarkMode) FluentDesignTokens.DarkNeutralBackground3 else FluentDesignTokens.LightNeutralBackground3
    val bottomBorderColor = if (isDarkMode) FluentDesignTokens.DarkNeutralStroke2 else FluentDesignTokens.LightNeutralStroke2

    Surface(
        color = stripBg,
        elevation = 0.dp,
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .border(width = 0.5.dp, color = bottomBorderColor)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            tabs.forEach { tab ->
                FluentWorkspaceTabItem(
                    tab = tab,
                    onSelect = { onTabSelected(tab.sessionId) },
                    onClose = { onTabClose(tab.sessionId) },
                    isDarkMode = isDarkMode
                )
            }

            if (tabs.size < 6) {
                // Fluent 2 subtle add button
                val addBg = if (isDarkMode) FluentDesignTokens.DarkNeutralBackground2 else FluentDesignTokens.LightNeutralBackground2
                val addStroke = if (isDarkMode) FluentDesignTokens.DarkNeutralStroke1 else FluentDesignTokens.LightNeutralStroke1
                val addFg = if (isDarkMode) FluentDesignTokens.DarkNeutralForeground2 else FluentDesignTokens.LightNeutralForeground2

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(start = 6.dp)
                        .size(32.dp)
                        .clip(RoundedCornerShape(FluentDesignTokens.CornerRadiusMedium))
                        .background(addBg)
                        .border(width = 1.dp, color = addStroke, shape = RoundedCornerShape(FluentDesignTokens.CornerRadiusMedium))
                        .clickable(onClick = onAddTab)
                ) {
                    Text(
                        text = "+",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = addFg,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun FluentWorkspaceTabItem(
    tab: TabletWorkspaceTab,
    onSelect: () -> Unit,
    onClose: () -> Unit,
    isDarkMode: Boolean
) {
    val (badgeText, badgeColor) = when (tab.editorKind.uppercase()) {
        "SHEET", "CELLS" -> "S" to ColorSheet
        "PRESENTATION", "SLIDES" -> "P" to ColorPresentation
        else -> "W" to ColorWrite
    }

    val activeBg = if (isDarkMode) FluentDesignTokens.DarkNeutralBackground1 else FluentDesignTokens.LightNeutralBackground1
    val inactiveBg = Color.Transparent

    val activeStroke = if (isDarkMode) FluentDesignTokens.DarkNeutralStroke1 else FluentDesignTokens.LightNeutralStroke2
    val inactiveStroke = Color.Transparent

    val primaryFg = if (isDarkMode) FluentDesignTokens.DarkNeutralForeground1 else FluentDesignTokens.LightNeutralForeground1
    val secondaryFg = if (isDarkMode) FluentDesignTokens.DarkNeutralForeground2 else FluentDesignTokens.LightNeutralForeground2

    val tabShape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp)

    Box(
        modifier = Modifier
            .padding(horizontal = 3.dp, vertical = 2.dp)
            .clip(tabShape)
            .background(if (tab.isActive) activeBg else inactiveBg)
            .border(
                width = if (tab.isActive) 1.dp else 0.dp,
                color = if (tab.isActive) activeStroke else inactiveStroke,
                shape = tabShape
            )
            .clickable(onClick = onSelect)
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .widthIn(min = 120.dp, max = 210.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                // Fluent 2 App Icon Pill (Rounded rectangle with brand color)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(badgeColor)
                ) {
                    Text(
                        text = badgeText,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                // Document Title
                Text(
                    text = tab.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 12.sp,
                    fontWeight = if (tab.isActive) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (tab.isActive) primaryFg else secondaryFg,
                    modifier = Modifier.weight(1f)
                )

                // Dirty / Unsaved indicator dot
                if (tab.isDirty) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE53935))
                    )
                }

                // Fluent Close Button (Subtle circle hover)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onClose)
                ) {
                    Text(
                        text = "✕",
                        fontSize = 10.sp,
                        color = secondaryFg,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Fluent Active Tab Bottom Indicator Line (Brand Accent)
            if (tab.isActive) {
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .fillMaxWidth()
                        .height(2.5.dp)
                        .clip(RoundedCornerShape(FluentDesignTokens.CornerRadiusPill))
                        .background(badgeColor)
                )
            }
        }
    }
}
