package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.SavedSkinEntity
import com.example.ui.components.MinecraftSkinCanvas
import com.example.ui.components.SkinPixelEditorDialog
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.NetherGold
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VolcanicRed
import com.example.util.SkinEngine

@Composable
fun SkinsScreen(
    skins: List<SavedSkinEntity>,
    isFetchingMojang: Boolean,
    onSelectSkin: (String) -> Unit,
    onFetchMojangSkin: (String) -> Unit,
    onImportSkinUri: (Uri) -> Unit,
    onSaveEditedSkin: (SavedSkinEntity, android.graphics.Bitmap) -> Unit,
    onExportSkinMcpack: (SavedSkinEntity) -> Unit,
    onDeleteSkin: (SavedSkinEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    var usernameQuery by remember { mutableStateOf("") }
    var showBackView by remember { mutableStateOf(false) }
    var editingSkin by remember { mutableStateOf<SavedSkinEntity?>(null) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    val selectedSkin = remember(skins) {
        skins.firstOrNull { it.isSelected } ?: skins.firstOrNull()
    }
    val selectedBitmap = remember(selectedSkin?.filePath, selectedSkin?.updatedAt) {
        selectedSkin?.let { SkinEngine.loadSkinBitmap(it.filePath) }
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            onImportSkinUri(uri)
        }
    }

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
            // Active Skin Studio Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.large)
                        .border(1.dp, ObsidianBorder, MaterialTheme.shapes.large),
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_skin_studio_bg),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize(),
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(ObsidianBg.copy(alpha = 0.8f), ObsidianBg.copy(alpha = 0.94f))
                                )
                            ),
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("MICHICH SKIN STUDIO", style = MaterialTheme.typography.labelMedium, color = primaryColor)
                                Text(
                                    text = selectedSkin?.name ?: "No Skin Selected",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                )
                                if (selectedSkin != null) {
                                    Text(
                                        text = "${selectedSkin.modelType} • ${selectedSkin.source}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary,
                                    )
                                }
                            }
                            IconButton(onClick = { showBackView = !showBackView }) {
                                Icon(Icons.Filled.FlipCameraAndroid, contentDescription = "Rotate skin view", tint = primaryColor)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            MinecraftSkinCanvas(
                                bitmap = selectedBitmap,
                                showBack = showBackView,
                                isSlim = selectedSkin?.modelType == "SLIM",
                            )
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Button(
                                    onClick = {
                                        if (selectedSkin != null) editingSkin = selectedSkin
                                    },
                                    enabled = selectedSkin != null && selectedBitmap != null,
                                    shape = MaterialTheme.shapes.small,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = primaryColor,
                                        contentColor = ObsidianBg,
                                    ),
                                    modifier = Modifier.fillMaxWidth().testTag("edit_skin_button"),
                                ) {
                                    Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Edit Pixels")
                                }

                                Button(
                                    onClick = {
                                        if (selectedSkin != null) onExportSkinMcpack(selectedSkin)
                                    },
                                    enabled = selectedSkin != null,
                                    shape = MaterialTheme.shapes.small,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = EmeraldAccent,
                                        contentColor = ObsidianBg,
                                    ),
                                    modifier = Modifier.fillMaxWidth().testTag("export_skin_mcpack_button"),
                                ) {
                                    Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Export .mcpack")
                                }

                                OutlinedButton(
                                    onClick = {
                                        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                    },
                                    shape = MaterialTheme.shapes.small,
                                    modifier = Modifier.fillMaxWidth().testTag("import_skin_png_button"),
                                ) {
                                    Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Import PNG")
                                }
                            }
                        }
                    }
                }
            }

            // Mojang Username Lookup
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = usernameQuery,
                        onValueChange = { usernameQuery = it },
                        placeholder = { Text("Fetch Java skin by username (e.g. Notch)…") },
                        leadingIcon = { Icon(Icons.Filled.PersonSearch, contentDescription = null, tint = primaryColor) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                focusManager.clearFocus()
                                onFetchMojangSkin(usernameQuery)
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
                            .weight(1f)
                            .testTag("mojang_username_input"),
                    )
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            onFetchMojangSkin(usernameQuery)
                        },
                        enabled = !isFetchingMojang && usernameQuery.isNotBlank(),
                        shape = MaterialTheme.shapes.small,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = secondaryColor,
                            contentColor = ObsidianBg,
                        ),
                        modifier = Modifier.height(54.dp).testTag("fetch_mojang_skin_button"),
                    ) {
                        if (isFetchingMojang) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = ObsidianBg, strokeWidth = 2.dp)
                        } else {
                            Text("Fetch")
                        }
                    }
                }
            }

            item {
                Text("Saved Skins (${skins.size})", style = MaterialTheme.typography.titleMedium)
            }

            items(skins, key = { it.id }) { skin ->
                val active = skin.id == selectedSkin?.id
                Card(
                    onClick = { onSelectSkin(skin.id) },
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = if (active) ObsidianElevated else ObsidianCard,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = if (active) 1.5.dp else 1.dp,
                            color = if (active) primaryColor else ObsidianBorder,
                            shape = MaterialTheme.shapes.medium,
                        ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(MaterialTheme.shapes.small)
                                    .background(ObsidianBg),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = skin.name.firstOrNull()?.uppercase() ?: "S",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (active) primaryColor else TextSecondary,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            Column {
                                Text(skin.name, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    text = "${skin.modelType} • ${skin.source}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (active) primaryColor else TextSecondary,
                                )
                            }
                        }

                        if (skin.source != "BUILTIN") {
                            IconButton(onClick = { onDeleteSkin(skin) }) {
                                Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete skin", tint = VolcanicRed)
                            }
                        }
                    }
                }
            }
        }
    }

    val skinForEditor = editingSkin
    if (skinForEditor != null) {
        val bmp = remember(skinForEditor.filePath) { SkinEngine.loadSkinBitmap(skinForEditor.filePath) }
        if (bmp != null) {
            SkinPixelEditorDialog(
                skin = skinForEditor,
                initialBitmap = bmp,
                onSave = { updatedBmp ->
                    onSaveEditedSkin(skinForEditor, updatedBmp)
                    editingSkin = null
                },
                onDismiss = { editingSkin = null },
            )
        }
    }
}
