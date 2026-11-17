package lib.compose.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Brand colors from ADR-12
val ColorWrite = Color(0xFF285E98)
val ColorSheet = Color(0xFF16744C)
val ColorPresentation = Color(0xFFB84B2B)

data class TabletWorkspaceTab(
    val sessionId: String,
    val title: String,
    val editorKind: String, // WRITE, SHEET, PRESENTATION
    val isDirty: Boolean = false,
    val isActive: Boolean = false,
    val isCheckpointing: Boolean = false
)

@Composable
fun TabletWorkspaceTabStrip(
    tabs: List<TabletWorkspaceTab>,
    onTabSelected: (String) -> Unit,
    onTabClose: (String) -> Unit,
    onAddTab: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Surface(
        color = MaterialTheme.colors.surface,
        elevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 8.dp)
        ) {
            tabs.forEach { tab ->
                WorkspaceTabItem(
                    tab = tab,
                    onSelect = { onTabSelected(tab.sessionId) },
                    onClose = { onTabClose(tab.sessionId) }
                )
            }

            if (tabs.size < 6) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(start = 6.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colors.onSurface.copy(alpha = 0.08f))
                        .clickable(onClick = onAddTab)
                ) {
                    Text(
                        text = "+",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkspaceTabItem(
    tab: TabletWorkspaceTab,
    onSelect: () -> Unit,
    onClose: () -> Unit
) {
    val (badgeText, badgeColor) = when (tab.editorKind.uppercase()) {
        "SHEET", "CELLS" -> "S" to ColorSheet
        "PRESENTATION", "SLIDES" -> "P" to ColorPresentation
        else -> "W" to ColorWrite
    }

    val backgroundColor = if (tab.isActive) {
        MaterialTheme.colors.background
    } else {
        MaterialTheme.colors.surface
    }

    val borderColor = if (tab.isActive) {
        badgeColor.copy(alpha = 0.8f)
    } else {
        Color.Transparent
    }

    Box(
        modifier = Modifier
            .padding(vertical = 4.dp, horizontal = 2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(8.dp))
            .clickable(onClick = onSelect)
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .widthIn(min = 120.dp, max = 220.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Document kind badge (W / S / P)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(badgeColor)
            ) {
                Text(
                    text = badgeText,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Document Title
            Text(
                text = tab.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 13.sp,
                fontWeight = if (tab.isActive) FontWeight.SemiBold else FontWeight.Normal,
                color = MaterialTheme.colors.onSurface,
                modifier = Modifier.weight(1f)
            )

            // Dirty / Unsaved indicator
            if (tab.isDirty) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE53935))
                )
            }

            // Close button (clean Unicode cross)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onClose)
            ) {
                Text(
                    text = "✕",
                    fontSize = 11.sp,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
