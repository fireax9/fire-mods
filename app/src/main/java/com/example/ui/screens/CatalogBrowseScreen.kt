package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.remote.McpedlService
import com.example.data.remote.ModrinthHit
import com.example.ui.CatalogState
import com.example.ui.EditionMode
import com.example.ui.HubTab
import com.example.ui.components.EditionToggleBar
import com.example.ui.components.ModrinthProjectCard
import com.example.ui.theme.BedrockCyan
import com.example.ui.theme.NetherGold
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VolcanicRed

@Composable
fun CatalogBrowseScreen(
    tab: HubTab,
    edition: EditionMode,
    searchQuery: String,
    sortIndex: String,
    catalogState: CatalogState,
    onSelectEdition: (EditionMode) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSearchSubmit: () -> Unit,
    onSortChange: (String) -> Unit,
    onProjectClick: (ModrinthHit) -> Unit,
    onOpenMcpedlUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val heroImage = if (tab == HubTab.MODPACKS) R.drawable.img_modpack_forge else R.drawable.img_launcher_hero
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 820.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Hero Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(126.dp)
                        .clip(MaterialTheme.shapes.large)
                        .border(1.dp, ObsidianBorder, MaterialTheme.shapes.large),
                ) {
                    Image(
                        painter = painterResource(id = heroImage),
                        contentDescription = "${tab.label} banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        ObsidianBg.copy(alpha = 0.92f),
                                        secondaryColor.copy(alpha = 0.30f),
                                        ObsidianBg.copy(alpha = 0.55f),
                                    )
                                )
                            ),
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = if (edition == EditionMode.JAVA) "MODRINTH LIVE CATALOG" else "MCPEDL BEDROCK HUB",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (edition == EditionMode.JAVA) primaryColor else BedrockCyan,
                        )
                        Column {
                            Text(
                                text = when (tab) {
                                    HubTab.MODS -> "Minecraft Mods & Addons"
                                    HubTab.MODPACKS -> "Curated Modpacks & Worlds"
                                    HubTab.RESOURCE_PACKS -> "Resource & Texture Packs"
                                    HubTab.SHADERS -> "Shaders & Visual Overhauls"
                                    HubTab.SKINS -> "Skin Studio"
                                },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                            )
                            Text(
                                text = if (edition == EditionMode.JAVA) {
                                    "Fabric • Forge • NeoForge • Iris • OptiFine"
                                } else {
                                    "Direct .mcpack, .mcaddon & .mcworld capture"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                            )
                        }
                    }
                }
            }

            // Edition Switch (Java vs Bedrock)
            item {
                EditionToggleBar(
                    edition = edition,
                    onSelectEdition = onSelectEdition,
                )
            }

            // Search Field
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = {
                        Text(
                            if (edition == EditionMode.JAVA) {
                                "Search Modrinth ${tab.label.lowercase()}…"
                            } else {
                                "Search MCPEDL ${tab.label.lowercase()}…"
                            }
                        )
                    },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = primaryColor) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                onSearchQueryChange("")
                                if (edition == EditionMode.JAVA) onSearchSubmit()
                            }) {
                                Icon(Icons.Filled.Close, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            focusManager.clearFocus()
                            if (edition == EditionMode.JAVA) {
                                onSearchSubmit()
                            } else {
                                onOpenMcpedlUrl(McpedlService.buildSearchUrl(searchQuery, tab.categoryKey))
                            }
                        }
                    ),
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ObsidianCard,
                        unfocusedContainerColor = ObsidianCard,
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = ObsidianBorder,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("catalog_search_input"),
                )
            }

            if (edition == EditionMode.JAVA) {
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        listOf(
                            "downloads" to "Popular",
                            "newest" to "Newest",
                            "follows" to "Most Followed",
                        ).forEach { (key, label) ->
                            FilterChip(
                                selected = sortIndex == key,
                                onClick = { onSortChange(key) },
                                label = { Text(label) },
                            )
                        }
                    }
                }

                when (catalogState) {
                    is CatalogState.Loading -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(color = primaryColor)
                            }
                        }
                    }
                    is CatalogState.Error -> {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, VolcanicRed.copy(alpha = 0.6f), MaterialTheme.shapes.medium),
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Text(
                                        text = catalogState.message,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = VolcanicRed,
                                    )
                                    Button(
                                        onClick = onSearchSubmit,
                                        shape = MaterialTheme.shapes.small,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = primaryColor,
                                            contentColor = ObsidianBg,
                                        ),
                                    ) {
                                        Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Retry")
                                    }
                                }
                            }
                        }
                    }
                    is CatalogState.Success -> {
                        if (catalogState.hits.isEmpty()) {
                            item {
                                Text(
                                    text = "No Modrinth results matched your query.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(vertical = 24.dp),
                                )
                            }
                        } else {
                            items(catalogState.hits, key = { it.projectId }) { hit ->
                                ModrinthProjectCard(
                                    hit = hit,
                                    onClick = { onProjectClick(hit) },
                                )
                            }
                        }
                    }
                }
            } else {
                // Bedrock MCPEDL Portal Section
                item {
                    Button(
                        onClick = {
                            onOpenMcpedlUrl(McpedlService.buildSearchUrl(searchQuery, tab.categoryKey))
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = secondaryColor,
                            contentColor = ObsidianBg,
                        ),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("open_mcpedl_browser_button"),
                    ) {
                        Icon(Icons.Filled.OpenInBrowser, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (searchQuery.isBlank()) "Launch MCPEDL ${tab.label} Browser" else "Search \"$searchQuery\" on MCPEDL",
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                val portals = McpedlService.getPortalsForCategory(tab.categoryKey)
                items(portals, key = { it.id }) { portal ->
                    Card(
                        onClick = { onOpenMcpedlUrl(portal.url) },
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, ObsidianBorder, MaterialTheme.shapes.medium),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(MaterialTheme.shapes.small)
                                    .background(ObsidianElevated),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Filled.Public, contentDescription = null, tint = BedrockCyan)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(portal.title, style = MaterialTheme.typography.titleMedium)
                                Text(portal.subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}
