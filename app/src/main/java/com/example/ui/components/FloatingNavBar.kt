package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DonutSmall
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.DonutSmall
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppTab
import com.example.ui.theme.SFProFontFamily

@Composable
fun FloatingNavBar(
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        // 100% Solid, opaque Material 3 Expressive pill navbar (compact width, no empty margins)
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 10.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
            modifier = Modifier.wrapContentWidth().testTag("floating_nav_bar")
        ) {
            // Combined 5-tab solid navigation: Ledger, Insights & Stats, Budgets, Recurring, Settings
            val allTabs = listOf(
                AppTab.LEDGER,
                AppTab.INSIGHTS,
                AppTab.BUDGETS,
                AppTab.EMI,
                AppTab.DRIVE
            )

            Row(
                modifier = Modifier
                    .wrapContentWidth()
                    .padding(horizontal = 6.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                allTabs.forEach { tab ->
                    // Map both INSIGHTS and legacy STATISTICS to the unified Insights tab
                    val isSelected = (tab == selectedTab) ||
                            (tab == AppTab.INSIGHTS && selectedTab == AppTab.STATISTICS)

                    val iconTint by animateColorAsState(
                        targetValue = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f),
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label = "iconTint"
                    )

                    val pillBgColor by animateColorAsState(
                        targetValue = if (isSelected) primaryColor.copy(alpha = 0.15f) else Color.Transparent,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label = "pillBgColor"
                    )

                    val tabTitle = if (tab == AppTab.INSIGHTS) "Insights" else tab.title

                    val icon = when (tab) {
                        AppTab.LEDGER -> if (isSelected) Icons.Filled.AccountBalanceWallet else Icons.Outlined.AccountBalanceWallet
                        AppTab.INSIGHTS, AppTab.STATISTICS -> if (isSelected) Icons.Filled.Insights else Icons.Outlined.Insights
                        AppTab.BUDGETS -> if (isSelected) Icons.Filled.DonutSmall else Icons.Outlined.DonutSmall
                        AppTab.EMI -> if (isSelected) Icons.Filled.CreditCard else Icons.Outlined.CreditCard
                        AppTab.DRIVE -> if (isSelected) Icons.Filled.Settings else Icons.Outlined.Settings
                    }

                    // Material 3 Expressive pill item:
                    // Selected: [Icon]  [Label]
                    // Unselected: [Icon]
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .height(44.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(pillBgColor)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onTabSelected(tab) }
                            .padding(horizontal = if (isSelected) 12.dp else 8.dp)
                            .testTag("nav_tab_${tab.name.lowercase()}")
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = tabTitle,
                            tint = iconTint,
                            modifier = Modifier.size(22.dp)
                        )

                        AnimatedVisibility(
                            visible = isSelected,
                            enter = fadeIn(spring(stiffness = Spring.StiffnessMedium)) + expandHorizontally(spring(stiffness = Spring.StiffnessMedium)),
                            exit = fadeOut(spring(stiffness = Spring.StiffnessMedium)) + shrinkHorizontally(spring(stiffness = Spring.StiffnessMedium))
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = tabTitle,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = SFProFontFamily,
                                    color = primaryColor,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
