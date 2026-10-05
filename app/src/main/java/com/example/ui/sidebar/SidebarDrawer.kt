package com.example.ui.sidebar

import android.net.Uri
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.SRMusicApp
import com.example.core.data.preferences.AppPreferences
import com.example.ui.common.components.InteractiveImageCropperDialog
import com.example.ui.common.theme.springPress
import java.io.File

@Composable
fun SidebarDrawer(
    totalLibrarySizeFormatted: String,
    totalSongCount: Int,
    onScanMediaClick: () -> Unit,
    onNavigateHistory: () -> Unit,
    onNavigateFavorites: () -> Unit,
    onNavigatePlaylist: () -> Unit,
    onNavigateSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Sidebar(
        totalLibrarySizeFormatted = totalLibrarySizeFormatted,
        totalSongCount = totalSongCount,
        onScanMediaClick = onScanMediaClick,
        onNavigateHistory = onNavigateHistory,
        onNavigateFavorites = onNavigateFavorites,
        onNavigatePlaylist = onNavigatePlaylist,
        onNavigateSettings = onNavigateSettings,
        modifier = modifier
    )
}

@Composable
fun Sidebar(
    totalLibrarySizeFormatted: String,
    totalSongCount: Int,
    onScanMediaClick: () -> Unit,
    onNavigateHistory: () -> Unit,
    onNavigateFavorites: () -> Unit,
    onNavigatePlaylist: () -> Unit,
    onNavigateSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    SidebarDrawerContent(
        totalLibrarySizeFormatted = totalLibrarySizeFormatted,
        totalSongCount = totalSongCount,
        onScanMediaClick = onScanMediaClick,
        onNavigateHistory = onNavigateHistory,
        onNavigateFavorites = onNavigateFavorites,
        onNavigatePlaylist = onNavigatePlaylist,
        onNavigateSettings = onNavigateSettings,
        modifier = modifier
    )
}

@Composable
fun SidebarDrawerContent(
    totalLibrarySizeFormatted: String,
    totalSongCount: Int,
    onScanMediaClick: () -> Unit,
    onNavigateHistory: () -> Unit,
    onNavigateFavorites: () -> Unit,
    onNavigatePlaylist: () -> Unit,
    onNavigateSettings: () -> Unit,
    modifier: Modifier = Modifier,
    appPreferences: AppPreferences = (LocalContext.current.applicationContext as SRMusicApp).appPreferences
) {
    val context = LocalContext.current
    val headerConfig by appPreferences.sidebarHeaderFlow.collectAsStateWithLifecycle()

    var showEditNameDialog by remember { mutableStateOf(false) }
    var editedNameInput by remember { mutableStateOf("") }

    var showAvatarSourceDialog by remember { mutableStateOf(false) }
    var showBannerSourceDialog by remember { mutableStateOf(false) }

    var showCropDialog by remember { mutableStateOf(false) }
    var pendingCropUri by remember { mutableStateOf<Uri?>(null) }
    var isCropAvatar by remember { mutableStateOf(true) }

    val avatarPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            pendingCropUri = uri
            isCropAvatar = true
            showCropDialog = true
        }
    }

    val bannerPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            pendingCropUri = uri
            isCropAvatar = false
            showCropDialog = true
        }
    }

    if (showAvatarSourceDialog) {
        AlertDialog(
            onDismissRequest = { showAvatarSourceDialog = false },
            title = { Text("Foto Profil Avatar") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Pilih opsi untuk mengganti atau mengatur foto profil avatar:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                showAvatarSourceDialog = false
                                avatarPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Folder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Pilih dari Galeri & Sesuaikan",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    if (headerConfig.customAvatarUri != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    showAvatarSourceDialog = false
                                    try {
                                        File(context.filesDir, "sidebar_avatar.jpg").delete()
                                    } catch (_: Exception) {}
                                    appPreferences.setSidebarAvatarUri(null)
                                    Toast.makeText(context, "Foto avatar dikembalikan ke default", Toast.LENGTH_SHORT).show()
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Folder,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = "Hapus Foto Avatar",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAvatarSourceDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    if (showBannerSourceDialog) {
        AlertDialog(
            onDismissRequest = { showBannerSourceDialog = false },
            title = { Text("Header Banner") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Pilih opsi untuk mengganti gambar latar belakang banner header:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                showBannerSourceDialog = false
                                bannerPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Folder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Pilih dari Galeri & Sesuaikan",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    if (headerConfig.customBannerUri != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    showBannerSourceDialog = false
                                    try {
                                        File(context.filesDir, "sidebar_banner.jpg").delete()
                                    } catch (_: Exception) {}
                                    appPreferences.setSidebarBannerUri(null)
                                    Toast.makeText(context, "Header banner dikembalikan ke default", Toast.LENGTH_SHORT).show()
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Folder,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = "Hapus Banner",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showBannerSourceDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    if (showCropDialog && pendingCropUri != null) {
        val uri = pendingCropUri!!
        val targetFile = if (isCropAvatar) {
            File(context.filesDir, "sidebar_avatar.jpg")
        } else {
            File(context.filesDir, "sidebar_banner.jpg")
        }
        InteractiveImageCropperDialog(
            uri = uri,
            isAvatar = isCropAvatar,
            targetFile = targetFile,
            onDismiss = {
                showCropDialog = false
                pendingCropUri = null
            },
            onCropSuccess = { savedFile ->
                if (isCropAvatar) {
                    appPreferences.setSidebarAvatarUri(savedFile.absolutePath)
                    Toast.makeText(context, "Avatar berhasil diperbarui", Toast.LENGTH_SHORT).show()
                } else {
                    appPreferences.setSidebarBannerUri(savedFile.absolutePath)
                    Toast.makeText(context, "Header banner berhasil diperbarui", Toast.LENGTH_SHORT).show()
                }
                showCropDialog = false
                pendingCropUri = null
            }
        )
    }

    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("Ganti Nama Aplikasi") },
            text = {
                OutlinedTextField(
                    value = editedNameInput,
                    onValueChange = { editedNameInput = it },
                    label = { Text("Nama Aplikasi") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val trimmed = editedNameInput.trim()
                        if (trimmed.isNotEmpty()) {
                            appPreferences.setSidebarAppName(trimmed)
                            Toast.makeText(context, "Nama aplikasi disimpan", Toast.LENGTH_SHORT).show()
                        }
                        showEditNameDialog = false
                    }
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    val drawerBgColor = MaterialTheme.colorScheme.surface
    val clickableCardColor = MaterialTheme.colorScheme.surfaceContainer
    val customBannerUri = headerConfig.customBannerUri
    val customAvatarUri = headerConfig.customAvatarUri
    val customAppName = headerConfig.customAppName

    ModalDrawerSheet(
        modifier = modifier.width(300.dp),
        windowInsets = WindowInsets(0, 0, 0, 0),
        drawerContainerColor = drawerBgColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
        ) {
            // Profile & Banner Header (Isolated modular component)
            SidebarProfileHeader(
                customBannerUri = customBannerUri,
                customAvatarUri = customAvatarUri,
                customAppName = customAppName,
                totalSongCount = totalSongCount,
                totalLibrarySizeFormatted = totalLibrarySizeFormatted,
                drawerBgColor = drawerBgColor,
                onBannerClick = { showBannerSourceDialog = true },
                onAvatarClick = { showAvatarSourceDialog = true },
                onAppNameClick = {
                    editedNameInput = customAppName
                    showEditNameDialog = true
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                // 1. Scan Media
                SidebarNavigationItem(
                    icon = Icons.Rounded.Refresh,
                    label = "Scan Media",
                    testTag = "drawer_scan_media",
                    onClick = onScanMediaClick
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 2. History
                SidebarNavigationItem(
                    icon = Icons.Rounded.History,
                    label = "History",
                    testTag = "drawer_nav_history",
                    onClick = onNavigateHistory
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 3. Favorites
                SidebarNavigationItem(
                    icon = Icons.Rounded.Favorite,
                    label = "Favorites",
                    testTag = "drawer_nav_favorites",
                    onClick = onNavigateFavorites
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 4. Playlist
                SidebarNavigationItem(
                    icon = Icons.Rounded.QueueMusic,
                    label = "Playlist",
                    testTag = "drawer_nav_playlist",
                    onClick = onNavigatePlaylist
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 5. Settings
                SidebarNavigationItem(
                    icon = Icons.Rounded.Settings,
                    label = "Settings",
                    testTag = "drawer_nav_settings",
                    onClick = onNavigateSettings
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SidebarNavigationItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple()
            ) { onClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(19.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.40f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun SidebarProfileHeader(
    customBannerUri: String?,
    customAvatarUri: String?,
    customAppName: String,
    totalSongCount: Int,
    totalLibrarySizeFormatted: String,
    drawerBgColor: Color,
    onBannerClick: () -> Unit,
    onAvatarClick: () -> Unit,
    onAppNameClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(if (customBannerUri != null) Color.Transparent else drawerBgColor)
            .clickable { onBannerClick() }
    ) {
        if (customBannerUri != null) {
            AndroidView<ImageView>(
                modifier = Modifier.matchParentSize(),
                factory = { ctx ->
                    ImageView(ctx).apply {
                        scaleType = ImageView.ScaleType.CENTER_CROP
                    }
                },
                update = { view ->
                    try {
                        if (view.tag != customBannerUri) {
                            view.tag = customBannerUri
                            val uriStr = customBannerUri ?: ""
                            val model: Any = if (uriStr.startsWith("/")) File(uriStr) else Uri.parse(uriStr)
                            Glide.with(view.context)
                                .load(model)
                                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                .into(view)
                        }
                    } catch (_: Exception) {}
                }
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            0.0f to Color.Black.copy(alpha = 0.50f),
                            0.30f to Color.Black.copy(alpha = 0.20f),
                            0.72f to drawerBgColor.copy(alpha = 0.70f),
                            1.0f to drawerBgColor
                        )
                    )
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 22.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), CircleShape)
                    .clickable { onAvatarClick() },
                contentAlignment = Alignment.Center
            ) {
                if (customAvatarUri != null) {
                    AndroidView<ImageView>(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            ImageView(ctx).apply {
                                scaleType = ImageView.ScaleType.CENTER_CROP
                            }
                        },
                        update = { view ->
                            try {
                                if (view.tag != customAvatarUri) {
                                    view.tag = customAvatarUri
                                    val uriStr = customAvatarUri ?: ""
                                    val model: Any = if (uriStr.startsWith("/")) File(uriStr) else Uri.parse(uriStr)
                                    Glide.with(view.context)
                                        .load(model)
                                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                        .into(view)
                                }
                            } catch (_: Exception) {}
                        }
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.MusicNote,
                        contentDescription = "Avatar",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = customAppName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp
                        ),
                        color = if (customBannerUri != null) Color.White else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .clickable { onAppNameClick() }
                            .testTag("drawer_title")
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                val badgeBg = if (customBannerUri != null) Color.Black.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                val badgeTextColor = if (customBannerUri != null) Color.White else MaterialTheme.colorScheme.onSurface

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = badgeBg,
                        modifier = Modifier.testTag("drawer_total_songs")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MusicNote,
                                contentDescription = null,
                                tint = badgeTextColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$totalSongCount Songs",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = badgeTextColor
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = badgeBg,
                        modifier = Modifier.testTag("drawer_total_size")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Folder,
                                contentDescription = null,
                                tint = badgeTextColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = totalLibrarySizeFormatted,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = badgeTextColor
                            )
                        }
                    }
                }
            }
        }
    }
}

