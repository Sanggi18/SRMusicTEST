package com.example.ui.navigation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.example.core.data.database.PlaylistEntity
import com.example.core.model.PlaybackInfo
import com.example.core.model.Song
import com.example.ui.common.components.AddToPlaylistDialog
import com.example.ui.common.theme.ExpressiveMotion
import com.example.ui.library.core.SongAction
import com.example.ui.settings.core.SettingsAction
import com.example.ui.settings.SettingsScreen
import com.example.ui.settings.core.SettingsUiState
import com.example.ui.sidebar.core.CollectionAction
import com.example.ui.sidebar.core.CollectionUiState
import com.example.ui.sidebar.SidebarDrawerContent
import com.example.ui.sidebar.favorites.FavoritesScreen
import com.example.ui.sidebar.history.HistoryScreen
import com.example.ui.sidebar.playlist.PlaylistScreen
import kotlinx.coroutines.launch

import androidx.compose.animation.core.spring
import androidx.compose.material3.DrawerValue

suspend fun DrawerState.smoothOpen() = animateTo(
    targetValue = DrawerValue.Open,
    anim = spring(dampingRatio = 0.88f, stiffness = 480f)
)

suspend fun DrawerState.smoothClose() = animateTo(
    targetValue = DrawerValue.Closed,
    anim = spring(dampingRatio = 0.90f, stiffness = 520f)
)

@Composable
fun DrawerHost(
    drawerState: DrawerState,
    gesturesEnabled: Boolean,
    totalLibrarySizeFormatted: String,
    totalSongCount: Int,
    onScanMediaRequest: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenPlaylist: () -> Unit,
    onOpenSettings: () -> Unit,
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = gesturesEnabled,
        drawerContent = {
            SidebarDrawerContent(
                totalLibrarySizeFormatted = totalLibrarySizeFormatted,
                totalSongCount = totalSongCount,
                onScanMediaClick = {
                    coroutineScope.launch {
                        drawerState.smoothClose()
                        onScanMediaRequest()
                    }
                },
                onNavigateHistory = {
                    coroutineScope.launch {
                        drawerState.smoothClose()
                        onOpenHistory()
                    }
                },
                onNavigateFavorites = {
                    coroutineScope.launch {
                        drawerState.smoothClose()
                        onOpenFavorites()
                    }
                },
                onNavigatePlaylist = {
                    coroutineScope.launch {
                        drawerState.smoothClose()
                        onOpenPlaylist()
                    }
                },
                onNavigateSettings = {
                    coroutineScope.launch {
                        drawerState.smoothClose()
                        onOpenSettings()
                    }
                }
            )
        },
        content = content
    )
}

@Composable
fun DrawerScreenOverlay(
    activeDrawerScreen: String?,
    onClose: () -> Unit,
    collectionUiState: CollectionUiState,
    playbackInfo: PlaybackInfo,
    onCollectionAction: (CollectionAction) -> Unit,
    onSongActionExternal: ((SongAction, Song) -> Unit)? = null
) {
    AnimatedVisibility(
        visible = activeDrawerScreen != null,
        enter = slideInHorizontally(animationSpec = ExpressiveMotion.SlideSpringIntOffset) { fullWidth -> (fullWidth * 0.35f).toInt() } +
                fadeIn(animationSpec = ExpressiveMotion.SnappySpring),
        exit = slideOutHorizontally(animationSpec = ExpressiveMotion.SlideSpringIntOffset) { fullWidth -> (fullWidth * 0.35f).toInt() } +
                fadeOut(animationSpec = ExpressiveMotion.SnappySpring)
    ) {
        when (activeDrawerScreen) {
            "history" -> {
                HistoryScreen(
                    uiState = collectionUiState,
                    playbackInfo = playbackInfo,
                    onAction = onCollectionAction,
                    onBackClick = onClose,
                    onSongActionExternal = onSongActionExternal
                )
            }
            "favorites" -> {
                FavoritesScreen(
                    uiState = collectionUiState,
                    playbackInfo = playbackInfo,
                    onAction = onCollectionAction,
                    onBackClick = onClose,
                    onSongActionExternal = onSongActionExternal
                )
            }
            "playlist" -> {
                PlaylistScreen(
                    uiState = collectionUiState,
                    playbackInfo = playbackInfo,
                    onAction = onCollectionAction,
                    onBackClick = onClose,
                    onSongActionExternal = onSongActionExternal
                )
            }
        }
    }
}

@Composable
fun SettingsOverlay(
    isOpen: Boolean,
    onClose: () -> Unit,
    uiState: SettingsUiState,
    onAction: (SettingsAction) -> Unit,
    onNavigateToEqualizer: () -> Unit,
    onRefreshLibrary: () -> Unit
) {
    AnimatedVisibility(
        visible = isOpen,
        enter = slideInHorizontally(animationSpec = ExpressiveMotion.SlideSpringIntOffset) { fullWidth -> (fullWidth * 0.35f).toInt() } +
                fadeIn(animationSpec = ExpressiveMotion.SnappySpring),
        exit = slideOutHorizontally(animationSpec = ExpressiveMotion.SlideSpringIntOffset) { fullWidth -> (fullWidth * 0.35f).toInt() } +
                fadeOut(animationSpec = ExpressiveMotion.SnappySpring)
    ) {
        SettingsScreen(
            uiState = uiState,
            onAction = onAction,
            onClose = onClose,
            onNavigateToEqualizer = onNavigateToEqualizer,
            onRefreshLibrary = onRefreshLibrary
        )
    }
}

@Composable
fun AddToPlaylistOverlay(
    song: Song?,
    playlists: List<PlaylistEntity>,
    onDismiss: () -> Unit,
    onAddSongToPlaylist: (Long, Long) -> Unit,
    onCreatePlaylist: (String, ((Long) -> Unit)?) -> Unit
) {
    if (song != null) {
        AddToPlaylistDialog(
            song = song,
            playlists = playlists,
            onDismiss = onDismiss,
            onAddToPlaylist = { playlistId, songId ->
                onAddSongToPlaylist(playlistId, songId)
                onDismiss()
            },
            onCreateAndAdd = { playlistName, songId ->
                onCreatePlaylist(playlistName) { newPlaylistId ->
                    onAddSongToPlaylist(newPlaylistId, songId)
                }
                onDismiss()
            }
        )
    }
}
