package com.example.ui

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DownloadedAddonEntity
import com.example.data.local.MichichDatabase
import com.example.data.local.SavedSkinEntity
import com.example.data.remote.ModrinthHit
import com.example.data.remote.ModrinthVersion
import com.example.data.repository.LauncherRepository
import com.example.ui.theme.AppColorTheme
import com.example.ui.theme.AppShapeStyle
import com.example.util.SkinEngine
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class HubTab(val categoryKey: String, val label: String) {
    MODS("MOD", "Mods"),
    MODPACKS("MODPACK", "Modpacks"),
    RESOURCE_PACKS("RESOURCE_PACK", "Resources"),
    SHADERS("SHADER", "Shaders"),
    SKINS("SKINS", "Skins"),
}

enum class EditionMode(val key: String, val label: String) {
    JAVA("JAVA", "Java Edition"),
    BEDROCK("BEDROCK", "Bedrock"),
}

sealed interface CatalogState {
    data object Loading : CatalogState
    data class Success(val hits: List<ModrinthHit>) : CatalogState
    data class Error(val message: String) : CatalogState
}

sealed interface VersionsState {
    data object Idle : VersionsState
    data object Loading : VersionsState
    data class Ready(val versions: List<ModrinthVersion>) : VersionsState
    data class Error(val message: String) : VersionsState
}

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("michich_appearance_prefs", Context.MODE_PRIVATE)

    private val repository = LauncherRepository(
        context = application.applicationContext,
        dao = MichichDatabase.getInstance(application).launcherDao(),
    )

    private val _colorTheme = MutableStateFlow(
        AppColorTheme.fromKey(prefs.getString("color_theme", AppColorTheme.ORANGE.key))
    )
    val colorTheme: StateFlow<AppColorTheme> = _colorTheme.asStateFlow()

    private val _shapeStyle = MutableStateFlow(
        AppShapeStyle.fromKey(prefs.getString("shape_style", AppShapeStyle.ROUNDED.key))
    )
    val shapeStyle: StateFlow<AppShapeStyle> = _shapeStyle.asStateFlow()

    val downloadedAddons: StateFlow<List<DownloadedAddonEntity>> = repository.addons
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val savedSkins: StateFlow<List<SavedSkinEntity>> = repository.skins
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedTab = MutableStateFlow(HubTab.MODS)
    val selectedTab: StateFlow<HubTab> = _selectedTab.asStateFlow()

    private val _edition = MutableStateFlow(EditionMode.JAVA)
    val edition: StateFlow<EditionMode> = _edition.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortIndex = MutableStateFlow("downloads")
    val sortIndex: StateFlow<String> = _sortIndex.asStateFlow()

    private val _catalogState = MutableStateFlow<CatalogState>(CatalogState.Loading)
    val catalogState: StateFlow<CatalogState> = _catalogState.asStateFlow()

    private val _selectedProject = MutableStateFlow<ModrinthHit?>(null)
    val selectedProject: StateFlow<ModrinthHit?> = _selectedProject.asStateFlow()

    private val _versionsState = MutableStateFlow<VersionsState>(VersionsState.Idle)
    val versionsState: StateFlow<VersionsState> = _versionsState.asStateFlow()

    private val _isFetchingMojangSkin = MutableStateFlow(false)
    val isFetchingMojangSkin: StateFlow<Boolean> = _isFetchingMojangSkin.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureStarterSkins()
        }
        refreshCatalog()
    }

    fun selectTab(tab: HubTab) {
        if (_selectedTab.value == tab) return
        _selectedTab.value = tab
        if (tab != HubTab.SKINS && _edition.value == EditionMode.JAVA) {
            refreshCatalog()
        }
    }

    fun selectEdition(mode: EditionMode) {
        if (_edition.value == mode) return
        _edition.value = mode
        if (mode == EditionMode.JAVA && _selectedTab.value != HubTab.SKINS) {
            refreshCatalog()
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSortIndex(sort: String) {
        _sortIndex.value = sort
        refreshCatalog()
    }

    fun refreshCatalog() {
        val tab = _selectedTab.value
        if (tab == HubTab.SKINS) return
        viewModelScope.launch {
            _catalogState.value = CatalogState.Loading
            val res = repository.searchModrinth(
                query = _searchQuery.value,
                category = tab.categoryKey,
                sortIndex = _sortIndex.value,
            )
            _catalogState.value = res.fold(
                onSuccess = { CatalogState.Success(it) },
                onFailure = { CatalogState.Error(it.localizedMessage ?: "Failed to load Modrinth catalog") },
            )
        }
    }

    fun openProjectDetails(hit: ModrinthHit) {
        _selectedProject.value = hit
        _versionsState.value = VersionsState.Loading
        viewModelScope.launch {
            val res = repository.getModrinthVersions(hit.projectId)
            _versionsState.value = res.fold(
                onSuccess = { VersionsState.Ready(it) },
                onFailure = { VersionsState.Error(it.localizedMessage ?: "Could not load versions") },
            )
        }
    }

    fun closeProjectDetails() {
        _selectedProject.value = null
        _versionsState.value = VersionsState.Idle
    }

    fun downloadModrinthVersion(project: ModrinthHit, version: ModrinthVersion) {
        val file = version.files.firstOrNull { it.primary } ?: version.files.firstOrNull()
        if (file == null) {
            _snackbarMessage.value = "No downloadable file found for ${version.versionNumber}"
            return
        }
        val category = _selectedTab.value.categoryKey
        val loader = version.loaders.firstOrNull()?.replaceFirstChar { it.uppercase() } ?: "Java"
        _snackbarMessage.value = "Downloading ${project.title} (${version.versionNumber})..."
        viewModelScope.launch {
            val res = repository.downloadAddon(
                projectId = project.projectId,
                title = project.title,
                category = category,
                edition = "JAVA",
                source = "MODRINTH",
                versionName = version.versionNumber,
                fileName = file.filename,
                downloadUrl = file.url,
                iconUrl = project.iconUrl.orEmpty(),
                loaderOrFormat = loader,
            )
            _snackbarMessage.value = res.fold(
                onSuccess = { "Saved ${it.title} to Vault" },
                onFailure = { "Download failed: ${it.localizedMessage}" },
            )
        }
    }

    fun downloadBedrockUrl(url: String, titleHint: String, category: String) {
        val fileName = url.substringAfterLast('/').substringBefore('?').ifBlank { "mcpedl_pack.mcpack" }
        _snackbarMessage.value = "Downloading $fileName from MCPEDL..."
        viewModelScope.launch {
            val res = repository.downloadAddon(
                projectId = "mcpedl_${System.currentTimeMillis()}",
                title = titleHint.ifBlank { fileName.substringBeforeLast('.') },
                category = category,
                edition = "BEDROCK",
                source = "MCPEDL",
                versionName = "Bedrock",
                fileName = fileName,
                downloadUrl = url,
                iconUrl = "",
                loaderOrFormat = ".${fileName.substringAfterLast('.', "mcpack")}",
            )
            _snackbarMessage.value = res.fold(
                onSuccess = { "Saved ${it.title} to Vault" },
                onFailure = { "MCPEDL download failed: ${it.localizedMessage}" },
            )
        }
    }

    fun importLocalAddon(uri: Uri) {
        viewModelScope.launch {
            val res = repository.importLocalAddon(uri)
            _snackbarMessage.value = res.fold(
                onSuccess = { "Imported ${it.title} (${it.edition})" },
                onFailure = { "Import failed: ${it.localizedMessage}" },
            )
        }
    }

    fun deleteAddon(addon: DownloadedAddonEntity) {
        viewModelScope.launch {
            repository.deleteAddon(addon)
            _snackbarMessage.value = "Removed ${addon.title}"
        }
    }

    fun selectSkin(id: String) {
        viewModelScope.launch {
            repository.selectSkin(id)
        }
    }

    fun fetchMojangSkin(username: String) {
        viewModelScope.launch {
            _isFetchingMojangSkin.value = true
            val res = repository.fetchMojangSkin(username)
            _isFetchingMojangSkin.value = false
            _snackbarMessage.value = res.fold(
                onSuccess = { "Loaded skin for ${it.name}" },
                onFailure = { "Lookup failed: ${it.localizedMessage}" },
            )
        }
    }

    fun importSkinFromUri(uri: Uri) {
        viewModelScope.launch {
            val res = repository.importSkinFromUri(uri)
            _snackbarMessage.value = res.fold(
                onSuccess = { "Imported skin (${it.modelType})" },
                onFailure = { "Skin import failed: ${it.localizedMessage}" },
            )
        }
    }

    fun saveEditedSkin(skin: SavedSkinEntity, editedBitmap: Bitmap) {
        viewModelScope.launch {
            repository.saveEditedSkin(skin, editedBitmap)
            _snackbarMessage.value = "Saved edits to ${skin.name}"
        }
    }

    fun deleteSkin(skin: SavedSkinEntity) {
        viewModelScope.launch {
            repository.deleteSkin(skin)
            _snackbarMessage.value = "Deleted ${skin.name}"
        }
    }

    fun exportSkinAsMcpack(skin: SavedSkinEntity, onReady: (File) -> Unit) {
        viewModelScope.launch {
            val res = SkinEngine.exportSkinAsBedrockMcpack(getApplication(), skin)
            res.fold(
                onSuccess = { file -> onReady(file) },
                onFailure = { _snackbarMessage.value = "Export failed: ${it.localizedMessage}" },
            )
        }
    }

    fun setColorTheme(theme: AppColorTheme) {
        _colorTheme.value = theme
        prefs.edit().putString("color_theme", theme.key).apply()
    }

    fun setShapeStyle(style: AppShapeStyle) {
        _shapeStyle.value = style
        prefs.edit().putString("shape_style", style.key).apply()
    }

    fun consumeSnackbar() {
        _snackbarMessage.value = null
    }
}
