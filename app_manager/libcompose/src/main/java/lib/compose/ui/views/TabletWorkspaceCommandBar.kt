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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Microsoft Fluent Design 2 Simplified Ribbon / Command Bar for Android tablet workspace.
 * Provides touch-friendly command targets (36dp-40dp), Fluent typography, and category tabs.
 */
@Composable
fun TabletWorkspaceCommandBar(
    editorKind: String, // WRITE, SHEET, PRESENTATION
    onCommandClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = false
) {
    var selectedCategory by remember { mutableStateOf("Home") }
    val categories = listOf("Home", "Insert", "Layout", "Review", "View")

    val brandAccent = when (editorKind.uppercase()) {
        "SHEET", "CELLS" -> FluentDesignTokens.BrandExcel
        "PRESENTATION", "SLIDES" -> FluentDesignTokens.BrandPowerPoint
        else -> FluentDesignTokens.BrandWord
    }

    val barBg = if (isDarkMode) FluentDesignTokens.DarkNeutralBackground2 else FluentDesignTokens.LightNeutralBackground2
    val strokeColor = if (isDarkMode) FluentDesignTokens.DarkNeutralStroke2 else FluentDesignTokens.LightNeutralStroke2
    val primaryFg = if (isDarkMode) FluentDesignTokens.DarkNeutralForeground1 else FluentDesignTokens.LightNeutralForeground1
    val secondaryFg = if (isDarkMode) FluentDesignTokens.DarkNeutralForeground2 else FluentDesignTokens.LightNeutralForeground2

    Surface(
        color = barBg,
        elevation = 0.dp,
        modifier = modifier
            .fillMaxWidth()
            .border(width = 0.5.dp, color = strokeColor)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Category Tab Strip (Home, Insert, Layout, etc.)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp)
            ) {
                categories.forEach { category ->
                    val isSelected = category == selectedCategory
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { selectedCategory = category }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = category,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) brandAccent else secondaryFg
                        )
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .width(20.dp)
                                    .height(2.dp)
                                    .clip(RoundedCornerShape(FluentDesignTokens.CornerRadiusPill))
                                    .background(brandAccent)
                            )
                        } else {
                            Box(modifier = Modifier.height(2.dp))
                        }
                    }
                }

                // Copilot Sparkle Action Pill (Fluent 2 AI integration)
                Box(modifier = Modifier.weight(1f))
                FluentCopilotPill(onClick = { onCommandClick("COPILOT") })
            }

            // Command Button Bar (Simplified Ribbon)
            val scrollState = rememberScrollState()
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .horizontalScroll(scrollState)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                // Undo / Redo
                FluentCommandButton(label = "↶", tooltip = "Undo", isDarkMode = isDarkMode) { onCommandClick("UNDO") }
                FluentCommandButton(label = "↷", tooltip = "Redo", isDarkMode = isDarkMode) { onCommandClick("REDO") }

                FluentDivider(isDarkMode = isDarkMode)

                // Font & Style
                FluentCommandButton(label = "B", fontWeight = FontWeight.Bold, tooltip = "Bold", isDarkMode = isDarkMode) { onCommandClick("BOLD") }
                FluentCommandButton(label = "I", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, tooltip = "Italic", isDarkMode = isDarkMode) { onCommandClick("ITALIC") }
                FluentCommandButton(label = "U", tooltip = "Underline", isDarkMode = isDarkMode) { onCommandClick("UNDERLINE") }
                FluentCommandButton(label = "S̶", tooltip = "Strikethrough", isDarkMode = isDarkMode) { onCommandClick("STRIKE") }

                FluentDivider(isDarkMode = isDarkMode)

                // Color & Highlight
                FluentCommandButton(label = "A", colorBar = brandAccent, tooltip = "Font Color", isDarkMode = isDarkMode) { onCommandClick("FONT_COLOR") }
                FluentCommandButton(label = "✎", colorBar = Color(0xFFFFEB3B), tooltip = "Highlight", isDarkMode = isDarkMode) { onCommandClick("HIGHLIGHT") }

                FluentDivider(isDarkMode = isDarkMode)

                // Alignment
                FluentCommandButton(label = "≡", tooltip = "Align Left", isDarkMode = isDarkMode) { onCommandClick("ALIGN_LEFT") }
                FluentCommandButton(label = "≢", tooltip = "Align Center", isDarkMode = isDarkMode) { onCommandClick("ALIGN_CENTER") }
                FluentCommandButton(label = "≣", tooltip = "Align Right", isDarkMode = isDarkMode) { onCommandClick("ALIGN_RIGHT") }

                FluentDivider(isDarkMode = isDarkMode)

                // Lists
                FluentCommandButton(label = "•—", tooltip = "Bullets", isDarkMode = isDarkMode) { onCommandClick("BULLETS") }
                FluentCommandButton(label = "1.—", tooltip = "Numbering", isDarkMode = isDarkMode) { onCommandClick("NUMBERING") }
            }
        }
    }
}

@Composable
private fun FluentCommandButton(
    label: String,
    tooltip: String,
    modifier: Modifier = Modifier,
    fontWeight: FontWeight = FontWeight.Normal,
    fontStyle: androidx.compose.ui.text.font.FontStyle? = null,
    colorBar: Color? = null,
    isDarkMode: Boolean = false,
    onClick: () -> Unit
) {
    val fg = if (isDarkMode) FluentDesignTokens.DarkNeutralForeground1 else FluentDesignTokens.LightNeutralForeground1
    val bg = if (isDarkMode) FluentDesignTokens.DarkNeutralBackground1 else FluentDesignTokens.LightNeutralBackground1
    val borderStroke = if (isDarkMode) FluentDesignTokens.DarkNeutralStroke1 else FluentDesignTokens.LightNeutralStroke2

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(34.dp)
            .clip(RoundedCornerShape(FluentDesignTokens.CornerRadiusMedium))
            .background(bg)
            .border(width = 0.5.dp, color = borderStroke, shape = RoundedCornerShape(FluentDesignTokens.CornerRadiusMedium))
            .clickable(onClick = onClick)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = fontWeight,
                fontStyle = fontStyle,
                color = fg,
                textAlign = TextAlign.Center
            )
            if (colorBar != null) {
                Box(
                    modifier = Modifier
                        .padding(top = 1.dp)
                        .size(width = 14.dp, height = 2.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(colorBar)
                )
            }
        }
    }
}

@Composable
private fun FluentDivider(isDarkMode: Boolean) {
    val stroke = if (isDarkMode) FluentDesignTokens.DarkNeutralStroke1 else FluentDesignTokens.LightNeutralStroke2
    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .width(1.dp)
            .height(20.dp)
            .background(stroke)
    )
}

@Composable
private fun FluentCopilotPill(onClick: () -> Unit) {
    val gradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF0F6CBD), // Fluent Blue
            Color(0xFF744DA9), // Fluent Purple
            Color(0xFFDF4870)  // Fluent Magenta
        )
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(end = 4.dp)
            .height(28.dp)
            .clip(RoundedCornerShape(FluentDesignTokens.CornerRadiusPill))
            .background(gradient)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "✦",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Copilot",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
