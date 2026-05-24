package com.whatshappening.novisad.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whatshappening.novisad.ui.theme.LocalCatppuccin
import com.whatshappening.novisad.ui.theme.WhatsHappeningTheme

// ── BottomNavDestination ──────────────────────────────────────────────────────

enum class BottomNavDestination { Home, Map, Saved }

// ── AppBottomNav ──────────────────────────────────────────────────────────────

/**
 * Floating bottom navigation bar — NOT a full-bleed M3 NavigationBar.
 * Sits inset 12dp from edges, 14dp above the system nav bar, with 24dp
 * rounded corners and a semi-transparent base-colour surface.
 */
@Composable
fun AppBottomNav(
    current: BottomNavDestination,
    onHomeClick: () -> Unit,
    onMapClick: () -> Unit,
    onSavedClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalCatppuccin.current

    Surface(
        modifier      = modifier
            .padding(start = 12.dp, end = 12.dp, bottom = 14.dp)
            .fillMaxWidth(),
        shape         = RoundedCornerShape(24.dp),
        color         = palette.base.copy(alpha = 0.92f),
        tonalElevation = 16.dp,
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier              = Modifier.padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            NavItem(
                label         = "Home",
                selectedIcon  = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home,
                selected      = current == BottomNavDestination.Home,
                onClick       = onHomeClick,
                modifier      = Modifier.weight(1f),
            )
            NavItem(
                label         = "Map",
                selectedIcon  = Icons.Filled.Map,
                unselectedIcon = Icons.Outlined.Map,
                selected      = current == BottomNavDestination.Map,
                onClick       = onMapClick,
                modifier      = Modifier.weight(1f),
            )
            NavItem(
                label         = "Saved",
                selectedIcon  = Icons.Filled.Bookmark,
                unselectedIcon = Icons.Outlined.BookmarkBorder,
                selected      = current == BottomNavDestination.Saved,
                onClick       = onSavedClick,
                modifier      = Modifier.weight(1f),
            )
        }
    }
}

// ── NavItem ───────────────────────────────────────────────────────────────────

@Composable
private fun NavItem(
    label: String,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent   = MaterialTheme.colorScheme.primary
    val onAccent = MaterialTheme.colorScheme.onPrimary
    val palette  = LocalCatppuccin.current

    val bgColor by animateColorAsState(
        targetValue   = if (selected) accent else Color.Transparent,
        animationSpec = tween(200),
        label         = "navBg_$label",
    )
    val contentColor by animateColorAsState(
        targetValue   = if (selected) onAccent else palette.subtext1,
        animationSpec = tween(200),
        label         = "navContent_$label",
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Icon(
            imageVector        = if (selected) selectedIcon else unselectedIcon,
            contentDescription = label,
            modifier           = Modifier.size(22.dp),
            tint               = contentColor,
        )
        Text(
            text       = label,
            fontSize   = 11.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color      = contentColor,
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(name = "AppBottomNav · Home · Light")
@Preview(name = "AppBottomNav · Home · Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AppBottomNavHomePreview() {
    WhatsHappeningTheme {
        Box(Modifier.background(LocalCatppuccin.current.base).padding(bottom = 8.dp)) {
            AppBottomNav(
                current      = BottomNavDestination.Home,
                onHomeClick  = {},
                onMapClick   = {},
                onSavedClick = {},
            )
        }
    }
}

@Preview(name = "AppBottomNav · Saved · Light")
@Preview(name = "AppBottomNav · Saved · Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AppBottomNavSavedPreview() {
    WhatsHappeningTheme {
        Box(Modifier.background(LocalCatppuccin.current.base).padding(bottom = 8.dp)) {
            AppBottomNav(
                current      = BottomNavDestination.Saved,
                onHomeClick  = {},
                onMapClick   = {},
                onSavedClick = {},
            )
        }
    }
}
