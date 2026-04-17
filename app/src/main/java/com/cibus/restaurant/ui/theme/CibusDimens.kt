package com.cibus.restaurant.ui.theme

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Design tokens — aligned with iOS RestaurantDesignSystem. */
object CibusDimens {
    // Typography
    val displaySp = 28.sp
    val displayMediumSp = 24.sp
    val headingSp = 20.sp
    val titleSp = 20.sp
    val sectionTitleSp = 16.sp
    val bodySp = 14.sp
    val captionSp = 12.sp
    val labelSp = 12.sp
    val priceSp = 16.sp
    val priceLargeSp = 24.sp

    // Spacing (matches iOS RestaurantSpacing)
    val spacing4 = 4.dp
    val spacing8 = 8.dp
    val spacing12 = 12.dp
    val spacing16 = 16.dp
    val spacing20 = 20.dp
    val spacing24 = 24.dp
    val spacing28 = 28.dp
    val spacing32 = 32.dp
    val minTapTarget = 44.dp
    val screenHorizontal = 16.dp
    val cardPadding = 14.dp

    // Radii (matches iOS RestaurantRadius)
    val radiusXs = 4.dp
    val radiusSm = 8.dp
    val radiusMd = 12.dp
    val radiusButton = 14.dp
    val cardRadius = 14.dp
    val radiusLg = 16.dp
    val radiusXl = 20.dp

    val sectionGap = 24.dp
    val dividerThickness = 0.5.dp
    val dividerVerticalPadding = 10.dp

    // Buttons
    val btnMinHeight = 44.dp
    val btnHeight = 48.dp
    val btnHeightLarge = 56.dp
    val btnRadius = 14.dp

    // Cards
    val cardElevation = 3.dp

    val iconSize = 18.dp
    val iconSizeSmall = 14.dp

    const val motionPrimaryDuration = 180
    const val motionRowDuration = 160

    // Phase 300: Premium design tokens
    val radiusPill = 999.dp
    val sectionVertical = 16.dp
    val headerCurveRadius = 24.dp
    val emptyStateIconContainer = 80.dp
    val scrollBottomGap = 120.dp
    val iconSizeRow = 14.dp
    val iconSizeCard = 16.dp

    // Shadow elevation tiers
    val shadowSection = 1.dp
    val cardShadowElevation = 2.dp
    val floatingShadowElevation = 6.dp
    val heroShadowElevation = 8.dp

    // Press feedback
    const val pressScaleButton = 0.97f
    const val pressScaleRow = 0.975f
    const val pressOpacityButton = 0.92f

    // Motion tokens
    const val motionFast = 150
    const val motionSlow = 300
    const val motionStaggerDelay = 40L
}
