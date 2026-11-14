package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.HubTab
import com.example.ui.LauncherViewModel
import com.example.ui.components.AppearanceSettingsSheet
import com.example.ui.components.McpedlBrowserDialog
import com.example.ui.components.ProjectVersionsBottomSheet
import com.example.ui.components.VaultBottomSheet
import com.example.ui.screens.CatalogBrowseScreen
import com.example.ui.screens.SkinsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MichichModsTheme
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.TextSecondary
import com.example.util.SkinEngine

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: LauncherViewModel = viewModel()
            val colorTheme by viewModel.colorTheme.collectAsStateWithLifecycle()
            val shapeStyle by viewModel.shapeStyle.collectAsStateWithLifecycle()

            MichichModsTheme(
                colorTheme = colorTheme,
                shapeStyle = shapeStyle,
            ) {
                var showSplash by rememberSaveable { mutableStateOf(true) }
                if (showSplash) {
                    SplashScreen(onFinished = { showSplash = false })
                } else {
                    MichichModsHubApp(viewModel = viewModel)
                }
            }
        }
    }
}

private fun iconForTab(tab: HubTab): ImageVector = when (tab) {
    HubTab.MODS -> Icons.Filled.Extension
    HubTab.MODPACKS -> Icons.Filled.Inventory2
    HubTab.RESOURCE_PACKS -> Icons.Filled.Palette
    HubTab.SHADERS -> Icons.Filled.AutoAwesome
    HubTab.SKINS -> Icons.Filled.Face
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MichichModsHubApp(
    viewModel: LauncherViewModel = viewModel(),
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val colorTheme by viewModel.colorTheme.collectAsStateWithLifecycle()
    val shapeStyle by viewModel.shapeStyle.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val edition by viewModel.edition.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val sortIndex by viewModel.sortIndex.collectAsStateWithLifecycle()
    val catalogState by viewModel.catalogState.collectAsStateWithLifecycle()
    val selectedProject by viewModel.selectedProject.collectAsStateWithLifecycle()
    val versionsState by viewModel.versionsState.collectAsStateWithLifecycle()
    val downloadedAddons by viewModel.downloadedAddons.collectAsStateWithLifecycle()
    val savedSkins by viewModel.savedSkins.collectAsStateWithLifecycle()
    val isFetchingMojang by viewModel.isFetchingMojangSkin.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    var showVaultSheet by remember { mutableStateOf(false) }
    var showAppearanceSheet by remember { mutableStateOf(false) }
    var mcpedlInitialUrl by remember { mutableStateOf<String?>(null) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    LaunchedEffect(snackbarMessage) {
        val msg = snackbarMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.consumeSnackbar()
        }
    }

    BackHandler(enabled = selectedTab != HubTab.MODS) {
        viewModel.selectTab(HubTab.MODS)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            containerColor = ObsidianBg,
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = ObsidianSurface),
                    navigationIcon = {
                        IconButton(
                            onClick = { showAppearanceSheet = true },
                            modifier = Modifier.testTag("open_settings_button"),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = "Appearance Settings",
                                tint = primaryColor,
                            )
                        }
                    },
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.app_icon_michich),
                                contentDescription = "Michich Mods Hub",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, primaryColor, CircleShape),
                            )
                            Text(
                                text = "Michich Mods Hub",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { showVaultSheet = true },
                            modifier = Modifier.testTag("open_vault_button"),
                        ) {
                            BadgedBox(
                                badge = {
                                    if (downloadedAddons.isNotEmpty()) {
                                        Badge(containerColor = secondaryColor) {
                                            Text(downloadedAddons.size.toString())
                                        }
                                    }
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.FolderZip,
                                    contentDescription = "Open Downloads Vault",
                                    tint = primaryColor,
                                )
                            }
                        }
                    },
                )
            },
            bottomBar = {
                if (!useRail) {
                    NavigationBar(containerColor = ObsidianSurface) {
                        HubTab.entries.forEach { tab ->
                            val selected = selectedTab == tab
                            NavigationBarItem(
                                selected = selected,
                                onClick = { viewModel.selectTab(tab) },
                                icon = {
                                    Icon(
                                        imageVector = iconForTab(tab),
                                        contentDescription = tab.label,
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.label,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = primaryColor,
                                    selectedTextColor = primaryColor,
                                    indicatorColor = ObsidianElevated,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary,
                                ),
                                modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}"),
                            )
                        }
                    }
                }
            },
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                if (useRail) {
                    NavigationRail(containerColor = ObsidianSurface) {
                        HubTab.entries.forEach { tab ->
                            val selected = selectedTab == tab
                            NavigationRailItem(
                                selected = selected,
                                onClick = { viewModel.selectTab(tab) },
                                icon = { Icon(iconForTab(tab), contentDescription = tab.label) },
                                label = { Text(tab.label, maxLines = 1) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = primaryColor,
                                    selectedTextColor = primaryColor,
                                    indicatorColor = ObsidianElevated,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary,
                                ),
                                modifier = Modifier.testTag("rail_tab_${tab.name.lowercase()}"),
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                    if (selectedTab == HubTab.SKINS) {
                        SkinsScreen(
                            skins = savedSkins,
                            isFetchingMojang = isFetchingMojang,
                            onSelectSkin = { viewModel.selectSkin(it) },
                            onFetchMojangSkin = { viewModel.fetchMojangSkin(it) },
                            onImportSkinUri = { viewModel.importSkinFromUri(it) },
                            onSaveEditedSkin = { skin, bmp -> viewModel.saveEditedSkin(skin, bmp) },
                            onExportSkinMcpack = { skin ->
                                viewModel.exportSkinAsMcpack(skin) { file ->
                                    SkinEngine.openOrShareExportedFile(context, file, "application/octet-stream")
                                }
                            },
                            onDeleteSkin = { viewModel.deleteSkin(it) },
                        )
                    } else {
                        CatalogBrowseScreen(
                            tab = selectedTab,
                            edition = edition,
                            searchQuery = searchQuery,
                            sortIndex = sortIndex,
                            catalogState = catalogState,
                            onSelectEdition = { viewModel.selectEdition(it) },
                            onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                            onSearchSubmit = { viewModel.refreshCatalog() },
                            onSortChange = { viewModel.updateSortIndex(it) },
                            onProjectClick = { viewModel.openProjectDetails(it) },
                            onOpenMcpedlUrl = { url -> mcpedlInitialUrl = url },
                        )
                    }
                }
            }
        }
    }

    if (showAppearanceSheet) {
        AppearanceSettingsSheet(
            currentColor = colorTheme,
            currentShape = shapeStyle,
            onSelectColor = { viewModel.setColorTheme(it) },
            onSelectShape = { viewModel.setShapeStyle(it) },
            onDismiss = { showAppearanceSheet = false },
        )
    }

    selectedProject?.let { project ->
        ProjectVersionsBottomSheet(
            project = project,
            versionsState = versionsState,
            onDownloadVersion = { version -> viewModel.downloadModrinthVersion(project, version) },
            onDismiss = { viewModel.closeProjectDetails() },
        )
    }

    if (showVaultSheet) {
        VaultBottomSheet(
            addons = downloadedAddons,
            onImportLocalUri = { viewModel.importLocalAddon(it) },
            onDeleteAddon = { viewModel.deleteAddon(it) },
            onDismiss = { showVaultSheet = false },
        )
    }

    mcpedlInitialUrl?.let { url ->
        McpedlBrowserDialog(
            initialUrl = url,
            category = selectedTab.categoryKey,
            onAddonDownloadDetected = { downloadUrl, titleHint, cat ->
                viewModel.downloadBedrockUrl(downloadUrl, titleHint, cat)
                mcpedlInitialUrl = null
                showVaultSheet = true
            },
            onDismiss = { mcpedlInitialUrl = null },
        )
    }
}
