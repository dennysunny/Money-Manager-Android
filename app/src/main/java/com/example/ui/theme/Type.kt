package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

// SF Pro, SF Body, and SF Mono Font Families
val SFProFontFamily = FontFamily(
    Font(R.font.sf_pro, FontWeight.Normal),
    Font(R.font.sf_pro, FontWeight.Medium),
    Font(R.font.sf_pro, FontWeight.SemiBold),
    Font(R.font.sf_pro, FontWeight.Bold),
    Font(R.font.sf_pro, FontWeight.ExtraBold)
)

val SFBodyFontFamily = FontFamily(
    Font(R.font.sf_body, FontWeight.Normal),
    Font(R.font.sf_body, FontWeight.Medium),
    Font(R.font.sf_body, FontWeight.SemiBold),
    Font(R.font.sf_body, FontWeight.Bold)
)

val SFMonoFontFamily = FontFamily(
    Font(R.font.sf_mono, FontWeight.Normal),
    Font(R.font.sf_mono, FontWeight.Medium),
    Font(R.font.sf_mono, FontWeight.SemiBold),
    Font(R.font.sf_mono, FontWeight.Bold),
    Font(R.font.sf_mono, FontWeight.ExtraBold)
)

// Set of Material typography styles powered by SF Pro and SF Body
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = SFProFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 44.sp,
        lineHeight = 52.sp,
        letterSpacing = (-1.2).sp
    ),
    displayMedium = TextStyle(
        fontFamily = SFProFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = (-1.0).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = SFProFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = SFProFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.25).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = SFProFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.15).sp
    ),
    titleLarge = TextStyle(
        fontFamily = SFProFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = SFProFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = SFProFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = SFBodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.25.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = SFBodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = SFBodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.3.sp
    ),
    labelLarge = TextStyle(
        fontFamily = SFProFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = SFBodyFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.3.sp
    ),
    labelSmall = TextStyle(
        fontFamily = SFBodyFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.4.sp
    )
)
