package com.example.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.DownloadedAddonEntity
import com.example.ui.theme.BedrockCyan
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.NetherGold
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VolcanicRed
import com.example.util.AddonArchiveParser
import com.example.util.ParsedArchiveInfo
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultBottomSheet(
    addons: List<DownloadedAddonEntity>,
    onImportLocalUri: (android.net.Uri) -> Unit,
    onDeleteAddon: (DownloadedAddonEntity) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    var inspectedInfo by remember { mutableStateOf<ParsedArchiveInfo?>(null) }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            onImportLocalUri(uri)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Mods & Packs Vault", style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = "${addons.size} saved packages",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Button(
                    onClick = { filePicker.launch(arrayOf("*/*")) },
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = ObsidianBg,
                    ),
                    modifier = Modifier.testTag("import_local_addon_button"),
                ) {
                    Icon(Icons.Filled.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Import Pack")
                }
            }

            if (addons.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .background(ObsidianCard)
                        .border(1.dp, ObsidianBorder, MaterialTheme.shapes.medium)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Download mods, modpacks, shaders or resource packs from Modrinth & MCPEDL, or tap Import Pack to inspect local .mcpack/.mrpack/.jar files.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.height(340.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(addons, key = { it.id }) { addon ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, ObsidianBorder, MaterialTheme.shapes.medium),
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1f),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(MaterialTheme.shapes.small)
                                                .background(ObsidianElevated),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.FolderZip,
                                                contentDescription = null,
                                                tint = if (addon.edition == "BEDROCK") BedrockCyan else MaterialTheme.colorScheme.primary,
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = addon.title,
                                                style = MaterialTheme.typography.titleSmall,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                            Text(
                                                text = "${addon.edition} • ${addon.loaderOrFormat} • ${AddonArchiveParser.formatBytes(addon.sizeBytes)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextSecondary,
                                            )
                                        }
                                    }

                                    Row {
                                        if (addon.status == "READY") {
                                            IconButton(onClick = {
                                                inspectedInfo = AddonArchiveParser.inspectArchiveFile(
                                                    file = File(addon.filePath),
                                                    fallbackTitle = addon.title,
                                                    preferredCategory = addon.category,
                                                    preferredEdition = addon.edition,
                                                )
                                            }) {
                                                Icon(Icons.Filled.Info, contentDescription = "Inspect archive", tint = MaterialTheme.colorScheme.primary)
                                            }
                                            IconButton(onClick = {
                                                AddonArchiveParser.openAddonWithMinecraftOrChooser(context, addon.filePath)
                                            }) {
                                                Icon(Icons.Filled.FileOpen, contentDescription = "Open or import pack", tint = EmeraldAccent)
                                            }
                                        }
                                        IconButton(onClick = { onDeleteAddon(addon) }) {
                                            Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete package", tint = VolcanicRed)
                                        }
                                    }
                                }

                                if (addon.status == "DOWNLOADING") {
                                    LinearProgressIndicator(
                                        progress = { addon.progress / 100f },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(4.dp)
                                            .clip(MaterialTheme.shapes.small),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = ObsidianElevated,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    inspectedInfo?.let { info ->
        AlertDialog(
            onDismissRequest = { inspectedInfo = null },
            containerColor = ObsidianSurface,
            title = { Text(info.title) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "${info.edition} • ${info.category} • ${info.loaderOrFormat}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    if (info.description.isNotBlank()) {
                        Text(info.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    Text("Archive Entries (${info.entries.size}):", style = MaterialTheme.typography.labelLarge)
                    LazyColumn(modifier = Modifier.height(180.dp)) {
                        items(info.entries) { entry ->
                            Text(
                                text = "• ${entry.path} (${AddonArchiveParser.formatBytes(entry.sizeBytes)})",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { inspectedInfo = null }) {
                    Text("Close", color = MaterialTheme.colorScheme.primary)
                }
            },
        )
    }
}
