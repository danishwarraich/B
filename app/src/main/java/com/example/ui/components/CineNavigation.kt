package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.model.CineSection
import com.example.ui.theme.BebasNeueFontFamily
import com.example.ui.theme.CineBlack
import com.example.ui.theme.CineBorder
import com.example.ui.theme.CineCharcoal
import com.example.ui.theme.CineEmerald
import com.example.ui.theme.CineRed
import com.example.ui.theme.CineSurface
import com.example.ui.theme.CineSurfaceElevated
import com.example.ui.theme.CineTextMuted
import com.example.ui.theme.CineTextPrimary
import com.example.ui.theme.CineTextSecondary
import com.example.ui.viewmodel.AboutSubSection

@Composable
fun CineStickyTopBar(
    currentSection: CineSection,
    searchQuery: String,
    favoritesCount: Int,
    onSelectSection: (CineSection) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSearchSubmit: () -> Unit,
    onClearSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSearchExpanded by rememberSaveable { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Surface(
        color = CineBlack.copy(alpha = 0.96f),
        tonalElevation = 6.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(top = 8.dp, bottom = 8.dp)
        ) {
            // Top Brand & Search Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // CineBox Logo
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSelectSection(CineSection.HOME) }
                        .padding(vertical = 4.dp, horizontal = 4.dp)
                        .testTag("cinebox_logo_button")
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(CineRed, Color(0xFF99060D))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = stringResource(R.string.app_name),
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "CINEBOX",
                        fontFamily = BebasNeueFontFamily,
                        fontSize = 30.sp,
                        letterSpacing = 2.sp,
                        color = CineRed
                    )
                }

                // Quick Right Actions: Search Toggle + My List + Where to Watch
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = {
                            isSearchExpanded = !isSearchExpanded
                            if (isSearchExpanded && currentSection != CineSection.SEARCH) {
                                onSelectSection(CineSection.SEARCH)
                            }
                        },
                        modifier = Modifier.testTag("top_search_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isSearchExpanded) Icons.Filled.Close else Icons.Filled.Search,
                            contentDescription = stringResource(R.string.nav_search),
                            tint = if (currentSection == CineSection.SEARCH || isSearchExpanded) CineRed else CineTextPrimary
                        )
                    }

                    // My List Pill Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (currentSection == CineSection.MY_LIST) CineRed
                                else CineSurfaceElevated
                            )
                            .clickable { onSelectSection(CineSection.MY_LIST) }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                            .testTag("top_my_list_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Bookmark,
                                contentDescription = stringResource(R.string.nav_my_list),
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = stringResource(R.string.nav_my_list),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White
                            )
                            if (favoritesCount > 0) {
                                Badge(
                                    containerColor = if (currentSection == CineSection.MY_LIST) CineBlack else CineRed,
                                    contentColor = Color.White
                                ) {
                                    Text(text = favoritesCount.toString())
                                }
                            }
                        }
                    }
                }
            }

            // Expandable Search Bar when Search is active or toggled
            if (isSearchExpanded || currentSection == CineSection.SEARCH) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = {
                        Text(
                            text = stringResource(R.string.search_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = CineTextMuted
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            tint = CineRed
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = onClearSearch,
                                modifier = Modifier.testTag("clear_search_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = stringResource(R.string.action_clear_search),
                                    tint = CineTextSecondary
                                )
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            onSearchSubmit()
                            focusManager.clearFocus()
                        }
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CineSurface,
                        unfocusedContainerColor = CineSurface,
                        focusedBorderColor = CineRed,
                        unfocusedBorderColor = CineBorder,
                        focusedTextColor = CineTextPrimary,
                        unfocusedTextColor = CineTextPrimary,
                        cursorColor = CineRed
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("search_input_field")
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Horizontal Scrollable Navigation Pills for all Sections
            val navItems = listOf(
                CineSection.HOME to Icons.Filled.Home,
                CineSection.MOVIES to Icons.Filled.Movie,
                CineSection.TV_SHOWS to Icons.Filled.LiveTv,
                CineSection.TRENDING to Icons.Filled.LocalFireDepartment,
                CineSection.POPULAR to Icons.AutoMirrored.Filled.TrendingUp,
                CineSection.TOP_RATED to Icons.Filled.Star,
                CineSection.UPCOMING to Icons.Filled.NewReleases,
                CineSection.GENRES to Icons.Filled.Category,
                CineSection.WATCH_PROVIDERS to Icons.Filled.PlayCircleFilled,
                CineSection.MY_LIST to Icons.Filled.Bookmark,
                CineSection.SEARCH to Icons.Filled.Search,
                CineSection.ABOUT to Icons.Filled.Info
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                navItems.forEach { (section, icon) ->
                    val isSelected = currentSection == section
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) CineRed else CineSurface
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) CineRed else CineBorder,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { onSelectSection(section) }
                            .padding(horizontal = 13.dp, vertical = 7.dp)
                            .testTag("nav_pill_${section.name.lowercase()}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = section.title,
                                tint = if (isSelected) Color.White else CineTextSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = section.title,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isSelected) Color.White else CineTextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CineBottomNavigationBar(
    currentSection: CineSection,
    onSelectSection: (CineSection) -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryTabs = listOf(
        Triple(CineSection.HOME, "Home", Icons.Filled.Home),
        Triple(CineSection.MOVIES, "Movies", Icons.Filled.Movie),
        Triple(CineSection.TV_SHOWS, "TV Shows", Icons.Filled.LiveTv),
        Triple(CineSection.TRENDING, "Trending", Icons.Filled.LocalFireDepartment),
        Triple(CineSection.GENRES, "Genres", Icons.Filled.Category)
    )

    NavigationBar(
        containerColor = CineCharcoal,
        contentColor = CineTextPrimary,
        tonalElevation = 8.dp,
        windowInsets = WindowInsets.navigationBars,
        modifier = modifier.fillMaxWidth()
    ) {
        primaryTabs.forEach { (section, label, icon) ->
            val selected = currentSection == section
            NavigationBarItem(
                selected = selected,
                onClick = { onSelectSection(section) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label
                    )
                },
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = CineRed,
                    indicatorColor = CineRed,
                    unselectedIconColor = CineTextSecondary,
                    unselectedTextColor = CineTextSecondary
                ),
                modifier = Modifier.testTag("bottom_nav_${section.name.lowercase()}")
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CineFooter(
    onSelectSection: (CineSection) -> Unit,
    onOpenAboutTab: (AboutSubSection) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(CineCharcoal)
            .padding(horizontal = 20.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        HorizontalDivider(color = CineBorder)

        // Brand Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(CineRed),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = "CINEBOX",
                fontFamily = BebasNeueFontFamily,
                fontSize = 26.sp,
                letterSpacing = 1.5.sp,
                color = CineRed
            )
            Text(
                text = "• Stream Discovery & Cinema Guide",
                style = MaterialTheme.typography.bodySmall,
                color = CineTextSecondary
            )
        }

        // Browse Section Links
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CineSection.entries.forEach { section ->
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.labelMedium,
                    color = CineTextSecondary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onSelectSection(section) }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .testTag("footer_nav_${section.name.lowercase()}")
                )
            }
        }

        // Legal & Info Links (About, Contact, Privacy Policy, Terms, API Setup)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AboutSubSection.entries.forEach { tab ->
                Text(
                    text = tab.title,
                    style = MaterialTheme.typography.labelLarge,
                    color = CineTextPrimary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(CineSurfaceElevated)
                        .clickable { onOpenAboutTab(tab) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("footer_legal_${tab.name.lowercase()}")
                )
            }
        }

        // Official TMDB Attribution Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CineSurface)
                .border(1.dp, CineBorder, RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF90CEA1), Color(0xFF01B4E4))
                            )
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "TMDB",
                        style = MaterialTheme.typography.labelLarge,
                        color = CineBlack,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = stringResource(R.string.tmdb_attribution),
                        style = MaterialTheme.typography.bodySmall,
                        color = CineTextSecondary
                    )
                    Text(
                        text = stringResource(R.string.providers_powered_by),
                        style = MaterialTheme.typography.labelSmall,
                        color = CineEmerald
                    )
                }
            }
        }

        Text(
            text = stringResource(R.string.footer_copyright),
            style = MaterialTheme.typography.labelSmall,
            color = CineTextMuted
        )
    }
}
