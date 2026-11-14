package com.example.ui.components

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.remote.McpedlService
import com.example.data.remote.ModrinthHit
import com.example.data.remote.ModrinthVersion
import com.example.ui.EditionMode
import com.example.ui.VersionsState
import com.example.ui.theme.BedrockCyan
import com.example.ui.theme.ModrinthGreen
import com.example.ui.theme.NetherGold
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VolcanicRed
import com.example.util.AddonArchiveParser

@Composable
fun EditionToggleBar(
    edition: EditionMode,
    onSelectEdition: (EditionMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        EditionMode.entries.forEachIndexed { index, mode ->
            val selected = edition == mode
            SegmentedButton(
                selected = selected,
                onClick = { onSelectEdition(mode) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = EditionMode.entries.size),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = if (mode == EditionMode.JAVA) primaryColor.copy(alpha = 0.2f) else secondaryColor.copy(alpha = 0.22f),
                    activeContentColor = if (mode == EditionMode.JAVA) primaryColor else secondaryColor,
                    inactiveContainerColor = ObsidianCard,
                    inactiveContentColor = TextSecondary,
                ),
                modifier = Modifier.testTag("edition_toggle_${mode.key.lowercase()}"),
            ) {
                Text(
                    text = if (mode == EditionMode.JAVA) "Java • Modrinth" else "Bedrock • MCPEDL",
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
fun ModrinthProjectCard(
    hit: ModrinthHit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = ObsidianCard),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, ObsidianBorder, MaterialTheme.shapes.medium),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(ObsidianElevated)
                    .border(1.dp, ObsidianBorder, MaterialTheme.shapes.small),
                contentAlignment = Alignment.Center,
            ) {
                if (!hit.iconUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = hit.iconUrl,
                        contentDescription = hit.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Extension,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = hit.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = AddonArchiveParser.formatDownloads(hit.downloads),
                        style = MaterialTheme.typography.labelMedium,
                        color = primaryColor,
                    )
                }

                Text(
                    text = "by ${hit.author.ifBlank { "Creator" }}",
                    style = MaterialTheme.typography.labelSmall,
                    color = ModrinthGreen,
                )

                Text(
                    text = hit.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                if (hit.categories.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        hit.categories.take(3).forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ObsidianElevated)
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                            ) {
                                Text(
                                    text = tag,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProjectVersionsBottomSheet(
    project: ModrinthHit,
    versionsState: VersionsState,
    onDownloadVersion: (ModrinthVersion) -> Unit,
    onDismiss: () -> Unit,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedLoader by remember { mutableStateOf("All") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ObsidianSurface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (!project.iconUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = project.iconUrl,
                        contentDescription = project.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(MaterialTheme.shapes.small),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(project.title, style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = "${project.author} • ${AddonArchiveParser.formatDownloads(project.downloads)} downloads",
                        style = MaterialTheme.typography.labelMedium,
                        color = primaryColor,
                    )
                }
            }

            Text(
                text = project.description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )

            when (versionsState) {
                is VersionsState.Loading, VersionsState.Idle -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = primaryColor)
                    }
                }
                is VersionsState.Error -> {
                    Text(
                        text = versionsState.message,
                        color = VolcanicRed,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                is VersionsState.Ready -> {
                    val loaders = remember(versionsState.versions) {
                        listOf("All") + versionsState.versions.flatMap { it.loaders }.distinct().take(5)
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        loaders.forEach { loader ->
                            FilterChip(
                                selected = selectedLoader == loader,
                                onClick = { selectedLoader = loader },
                                label = { Text(loader.replaceFirstChar { it.uppercase() }) },
                            )
                        }
                    }

                    val filtered = remember(versionsState.versions, selectedLoader) {
                        if (selectedLoader == "All") {
                            versionsState.versions
                        } else {
                            versionsState.versions.filter { v ->
                                v.loaders.any { it.equals(selectedLoader, ignoreCase = true) }
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.height(300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(filtered.take(20), key = { it.id }) { ver ->
                            val primaryFile = ver.files.firstOrNull { it.primary } ?: ver.files.firstOrNull()
                            Card(
                                colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, ObsidianBorder, MaterialTheme.shapes.medium),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = ver.name.ifBlank { ver.versionNumber },
                                            style = MaterialTheme.typography.titleSmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Text(
                                            text = "${ver.loaders.joinToString("/")} • MC ${ver.gameVersions.take(3).joinToString(", ")}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        if (primaryFile != null) {
                                            Text(
                                                text = "${primaryFile.filename} (${AddonArchiveParser.formatBytes(primaryFile.size)})",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = primaryColor,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                        }
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            onDownloadVersion(ver)
                                            onDismiss()
                                        },
                                        shape = MaterialTheme.shapes.small,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = primaryColor,
                                            contentColor = ObsidianBg,
                                        ),
                                        modifier = Modifier.testTag("download_version_${ver.id}"),
                                    ) {
                                        Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Get")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun McpedlBrowserDialog(
    initialUrl: String,
    category: String,
    onAddonDownloadDetected: (url: String, titleHint: String, category: String) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    var currentTitle by remember { mutableStateOf("MCPEDL Bedrock Hub") }
    var progress by remember { mutableIntStateOf(0) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            webViewRef?.destroy()
            webViewRef = null
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.9f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, ObsidianBorder, RoundedCornerShape(20.dp)),
            color = ObsidianSurface,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ObsidianCard)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f),
                    ) {
                        IconButton(onClick = {
                            val wv = webViewRef
                            if (wv?.canGoBack() == true) wv.goBack() else onDismiss()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        Icon(Icons.Filled.Public, contentDescription = null, tint = BedrockCyan, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = currentTitle,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Row {
                        IconButton(onClick = { webViewRef?.reload() }) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Reload MCPEDL")
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Filled.Close, contentDescription = "Close MCPEDL")
                        }
                    }
                }

                if (progress in 1..99) {
                    LinearProgressIndicator(
                        progress = { progress / 100f },
                        modifier = Modifier.fillMaxWidth().height(3.dp),
                        color = BedrockCyan,
                        trackColor = ObsidianCard,
                    )
                }

                AndroidView(
                    factory = {
                        WebView(context).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT,
                            )
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.useWideViewPort = true
                            settings.loadWithOverviewMode = true
                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                    val url = request?.url?.toString() ?: return false
                                    if (McpedlService.isDirectAddonDownload(url)) {
                                        onAddonDownloadDetected(url, currentTitle, category)
                                        return true
                                    }
                                    return false
                                }
                            }
                            webChromeClient = object : WebChromeClient() {
                                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                    progress = newProgress
                                }

                                override fun onReceivedTitle(view: WebView?, title: String?) {
                                    if (!title.isNullOrBlank()) currentTitle = title
                                }
                            }
                            setDownloadListener { url, _, _, _, _ ->
                                if (url != null) {
                                    onAddonDownloadDetected(url, currentTitle, category)
                                }
                            }
                            loadUrl(initialUrl)
                            webViewRef = this
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
