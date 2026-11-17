package lib.compose.ui.views

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Microsoft Fluent Design 2 design tokens for Euro-Office Android tablet workspace.
 * Follows Fluent 2 specifications: https://fluent2.microsoft.design
 */
object FluentDesignTokens {
    // Fluent 2 Brand Colors (Office suite)
    val BrandWord = Color(0xFF185ABD)           // Microsoft Word / Euro-Office Write
    val BrandWordSubtle = Color(0xFFEBF3FC)
    val BrandExcel = Color(0xFF107C41)          // Microsoft Excel / Euro-Office Sheet
    val BrandExcelSubtle = Color(0xFFECF7ED)
    val BrandPowerPoint = Color(0xFFC43E1C)     // Microsoft PowerPoint / Euro-Office Presentation
    val BrandPowerPointSubtle = Color(0xFFFDF0ED)
    val BrandCopilot = Color(0xFF6B45A8)        // Fluent Copilot Accent

    // Fluent 2 Light Neutral Ramp
    val LightNeutralBackground1 = Color(0xFFFFFFFF)     // Canvas / Active surface
    val LightNeutralBackground2 = Color(0xFFF5F5F5)     // Ribbon / Card container
    val LightNeutralBackground3 = Color(0xFFEBEBEB)     // Window Mica background
    val LightNeutralForeground1 = Color(0xFF242424)     // Primary text
    val LightNeutralForeground2 = Color(0xFF616161)     // Secondary text
    val LightNeutralForeground3 = Color(0xFF757575)     // Tertiary text
    val LightNeutralStroke1 = Color(0xFFD1D1D1)         // Strong border
    val LightNeutralStroke2 = Color(0xFFE5E5E5)         // Subtle border
    val LightSubtleHover = Color(0x0A000000)            // 4% black
    val LightSubtlePressed = Color(0x14000000)          // 8% black

    // Fluent 2 Dark Neutral Ramp
    val DarkNeutralBackground1 = Color(0xFF202020)      // Canvas / Active surface
    val DarkNeutralBackground2 = Color(0xFF292929)      // Ribbon / Card container
    val DarkNeutralBackground3 = Color(0xFF1C1C1C)      // Window Mica background
    val DarkNeutralForeground1 = Color(0xFFFFFFFF)      // Primary text
    val DarkNeutralForeground2 = Color(0xFFD1D1D1)      // Secondary text
    val DarkNeutralForeground3 = Color(0xFFA19F9D)      // Tertiary text
    val DarkNeutralStroke1 = Color(0xFF3D3D3D)          // Strong border
    val DarkNeutralStroke2 = Color(0xFF2E2E2E)          // Subtle border
    val DarkSubtleHover = Color(0x14FFFFFF)             // 8% white
    val DarkSubtlePressed = Color(0x24FFFFFF)           // 14% white

    // Corner Radii (Fluent 2 Corner Radius Tokens)
    val CornerRadiusNone = 0.dp
    val CornerRadiusSmall = 2.dp
    val CornerRadiusMedium = 4.dp                       // Control / Button
    val CornerRadiusLarge = 8.dp                        // Card / Tab
    val CornerRadiusXLarge = 12.dp                      // Dialog / Sheet
    val CornerRadiusPill = 999.dp                       // Pill / Badge

    // Typography Sizes
    val FontCaption = 11.sp
    val FontBody2 = 12.sp
    val FontBody1 = 14.sp
    val FontSubtitle2 = 16.sp
}
