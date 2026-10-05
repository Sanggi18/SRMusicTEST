package com.example.ui.navigation

import com.example.ui.navigation.components.AddToPlaylistOverlay
import com.example.ui.navigation.components.DrawerHost
import com.example.ui.navigation.components.DrawerScreenOverlay
import com.example.ui.navigation.components.SettingsOverlay
import com.example.ui.navigation.components.TabItem
import com.example.ui.navigation.core.BackNavigationHandler
import com.example.ui.navigation.components.smoothClose
import com.example.ui.navigation.components.smoothOpen
import com.example.ui.navigation.core.NavigationAction
import com.example.ui.navigation.core.NavigationState
import com.example.ui.navigation.core.rememberNavigationState

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.ui.library.components.LocalMiniPlayerVisible
import com.example.ui.sidebar.core.CollectionAction
import com.example.ui.sidebar.core.CollectionUiState
import com.example.ui.equalizer.core.EqualizerAction
import com.example.ui.equalizer.EqualizerScreen
import com.example.ui.equalizer.core.EqualizerUiState
import com.example.ui.home.core.HomeAction
import com.example.ui.home.core.HomeUiState
import com.example.ui.library.core.LibraryAction
import com.example.ui.library.core.LibraryUiState
import com.example.ui.library.core.SongAction
import com.example.ui.player.core.PlayerAction
import com.example.ui.player.PlayerHost
import com.example.ui.player.core.PlayerUiState
import com.example.ui.settings.core.SettingsAction
import com.example.ui.settings.core.SettingsUiState
import com.example.ui.settings.core.toTabItems
import com.example.core.model.Song
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    settingsUiState: SettingsUiState,
    onSettingsAction: (SettingsAction) -> Unit,
    equalizerUiState: EqualizerUiState,
    onEqualizerAction: (EqualizerAction) -> Unit,
    homeUiState: HomeUiState,
    onHomeAction: (HomeAction) -> Unit,
    libraryUiState: LibraryUiState,
    onLibraryAction: (LibraryAction) -> Unit,
    collectionUiState: CollectionUiState,
    onCollectionAction: (CollectionAction) -> Unit,
    playerUiState: PlayerUiState,
    onPlayerAction: (PlayerAction) -> Unit,
    playbackProgressFlow: kotlinx.coroutines.flow.StateFlow<com.example.core.model.PlaybackProgress>? = null,
    onRequestPermission: () -> Unit,
    onRefreshMedia: () -> Unit,
    navigationState: NavigationState = rememberNavigationState(),
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val enabledTabs = settingsUiState.enabledTabIds.toTabItems()
    val firstTab = enabledTabs.firstOrNull() ?: TabItem.HOME

    val artistListState = rememberLazyListState()
    val artistGridState = rememberLazyGridState()
    val albumListState = rememberLazyListState()
    val albumGridState = rememberLazyGridState()

    val pagerState = rememberPagerState(initialPage = 0) { enabledTabs.size }

    // Synchronize pager settled state to navigationState
    LaunchedEffect(pagerState, enabledTabs) {
        snapshotFlow { pagerState.settledPage }
            .collect { settledPage ->
                val targetTab = enabledTabs.getOrNull(settledPage)
                if (targetTab != null && targetTab != navigationState.selectedTab) {
                    navigationState.onAction(NavigationAction.SelectTab(targetTab))
                    onLibraryAction(LibraryAction.ClearDetail)
                    navigationState.onAction(NavigationAction.ClearDetailSourceTab)
                }
                navigationState.isBackNavigating = false
            }
    }

    // Keep tabs valid when enabledTabs changes in settings
    LaunchedEffect(enabledTabs) {
        navigationState.tabBackStack.retainAll(enabledTabs.toSet())
        val currentIndex = enabledTabs.indexOf(navigationState.selectedTab)
        if (currentIndex != -1) {
            if (pagerState.currentPage != currentIndex) {
                pagerState.scrollToPage(currentIndex)
            }
        } else if (enabledTabs.isNotEmpty()) {
            navigationState.onAction(NavigationAction.SelectTab(enabledTabs[0]))
            pagerState.scrollToPage(0)
        }
    }

    fun selectTab(tab: TabItem, animate: Boolean = true) {
        val targetIndex = enabledTabs.indexOf(tab)
        if (targetIndex == -1) return
        navigationState.onAction(NavigationAction.SelectTab(tab, animate))
        coroutineScope.launch {
            if (targetIndex in 0 until pagerState.pageCount) {
                if (animate) {
                    pagerState.animateScrollToPage(targetIndex)
                } else {
                    pagerState.scrollToPage(targetIndex)
                }
            }
        }
    }

    fun navigateToTab(tab: TabItem, animate: Boolean = true) {
        onLibraryAction(LibraryAction.ClearDetail)
        navigationState.onAction(NavigationAction.ClearDetailSourceTab)
        selectTab(tab, animate)
    }

    val onSongActionDispatcher: (SongAction, Song) -> Unit = { action, song ->
        when (action) {
            SongAction.PLAY -> onPlayerAction(PlayerAction.PlaySong(song, emptyList()))
            SongAction.PLAY_NEXT -> onPlayerAction(PlayerAction.PlayNext(song))
            SongAction.ADD_TO_QUEUE -> onPlayerAction(PlayerAction.AddToQueue(song))
            SongAction.ADD_TO_PLAYLIST -> {
                navigationState.onAction(NavigationAction.ShowAddToPlaylist(song))
            }
            SongAction.TOGGLE_FAVORITE, SongAction.REMOVE_FROM_FAVORITES -> {
                onCollectionAction(CollectionAction.ToggleFavorite(song.id))
            }
            SongAction.GO_TO_ARTIST -> {
                val artistName = song.artist.trim()
                if (artistName.isNotBlank() && !artistName.equals("<unknown>", ignoreCase = true) && !artistName.equals("unknown artist", ignoreCase = true)) {
                    navigationState.onAction(NavigationAction.OpenArtist(artistName, navigationState.selectedTab))
                    onLibraryAction(LibraryAction.SelectAlbum(null))
                    onLibraryAction(LibraryAction.SelectArtist(artistName))
                    val targetIndex = enabledTabs.indexOf(TabItem.ARTIST)
                    if (targetIndex != -1) {
                        coroutineScope.launch { pagerState.scrollToPage(targetIndex) }
                    }
                }
            }
            SongAction.GO_TO_ALBUM -> {
                val albumName = song.album.trim()
                if (albumName.isNotBlank() && !albumName.equals("<unknown>", ignoreCase = true) && !albumName.equals("unknown album", ignoreCase = true)) {
                    navigationState.onAction(NavigationAction.OpenAlbum(albumName, navigationState.selectedTab))
                    onLibraryAction(LibraryAction.SelectArtist(null))
                    onLibraryAction(LibraryAction.SelectAlbum(albumName))
                    val targetIndex = enabledTabs.indexOf(TabItem.ALBUM)
                    if (targetIndex != -1) {
                        coroutineScope.launch { pagerState.scrollToPage(targetIndex) }
                    }
                }
            }
            SongAction.GO_TO_FOLDER -> {
                navigationState.onAction(NavigationAction.OpenFolder(song.relativePath))
                onLibraryAction(LibraryAction.ClearDetail)
                onLibraryAction(LibraryAction.HandleSongAction(SongAction.GO_TO_FOLDER, song))
                val targetIndex = enabledTabs.indexOf(TabItem.FOLDER)
                if (targetIndex != -1) {
                    coroutineScope.launch { pagerState.scrollToPage(targetIndex) }
                }
            }
            SongAction.SONG_INFO -> {}
            SongAction.DELETE -> {}
        }
    }

    val hasDetailOpen = libraryUiState.selectedArtistName != null || libraryUiState.selectedAlbumName != null
    val canGoBackTab = navigationState.tabBackStack.isNotEmpty() || (navigationState.selectedTab != TabItem.HOME && navigationState.selectedTab != firstTab)

    // Canonical Back Handling
    BackNavigationHandler(
        isSettingsOpen = navigationState.isSettingsOpen,
        onCloseSettings = { navigationState.onAction(NavigationAction.SetSettingsOpen(false)) },
        activeDrawerScreen = navigationState.activeDrawerScreen,
        onCloseDrawerScreen = { navigationState.onAction(NavigationAction.OpenDrawerScreen(null)) },
        isEqualizerOpen = navigationState.isEqualizerOpen,
        onCloseEqualizer = { navigationState.onAction(NavigationAction.SetEqualizerOpen(false)) },
        isLyricsOpen = navigationState.isLyricsOpen,
        onCloseLyrics = { navigationState.onAction(NavigationAction.SetLyricsOpen(false)) },
        isDrawerOpen = drawerState.isOpen,
        onCloseDrawer = { coroutineScope.launch { drawerState.smoothClose() } },
        hasDetailOpen = hasDetailOpen,
        onBackFromDetail = {
            onLibraryAction(LibraryAction.ClearDetail)
            navigationState.detailSourceTab?.let { origin ->
                navigationState.onAction(NavigationAction.ClearDetailSourceTab)
                selectTab(origin, animate = false)
            }
        },
        canGoBackTab = canGoBackTab,
        onBackTab = {
            val targetTab = navigationState.popTabBackStack(enabledTabs, firstTab)
            if (targetTab != null) {
                onLibraryAction(LibraryAction.ClearDetail)
                navigationState.onAction(NavigationAction.ClearDetailSourceTab)
                val targetIndex = enabledTabs.indexOf(targetTab)
                if (targetIndex != -1) {
                    coroutineScope.launch { pagerState.animateScrollToPage(targetIndex) }
                }
            }
        }
    )

    // Modal Drawer & Main Content
    DrawerHost(
        drawerState = drawerState,
        gesturesEnabled = !hasDetailOpen && !navigationState.isLyricsOpen && !navigationState.isEqualizerOpen && !navigationState.isSettingsOpen && navigationState.activeDrawerScreen == null,
        totalLibrarySizeFormatted = libraryUiState.totalLibrarySizeFormatted,
        totalSongCount = libraryUiState.totalSongCount,
        onScanMediaRequest = onRefreshMedia,
        onOpenHistory = {
            navigationState.onAction(NavigationAction.OpenDrawerScreen("history"))
        },
        onOpenFavorites = {
            navigationState.onAction(NavigationAction.OpenDrawerScreen("favorites"))
        },
        onOpenPlaylist = {
            navigationState.onAction(NavigationAction.OpenDrawerScreen("playlist"))
        },
        onOpenSettings = {
            navigationState.onAction(NavigationAction.SetSettingsOpen(true))
        }
    ) {
        val isMiniPlayerVisible = playerUiState.currentSong != null &&
            !playerUiState.isDismissed &&
            !navigationState.isEqualizerOpen &&
            !navigationState.isSettingsOpen &&
            (navigationState.activeDrawerScreen == null)

        CompositionLocalProvider(
            LocalMiniPlayerVisible provides isMiniPlayerVisible
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Scaffold(
                modifier = modifier.fillMaxSize(),
                containerColor = Color.Transparent,
                contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
                bottomBar = {
                    MainBottomNavigationBar(
                        enabledTabs = enabledTabs,
                        selectedTab = navigationState.selectedTab,
                        pagerState = pagerState,
                        expansionProgress = 0f,
                        isEqualizerOpen = navigationState.isEqualizerOpen,
                        onTabSelected = { navigateToTab(it) }
                    )
                }
            ) { innerPadding ->
                Box(modifier = Modifier.fillMaxSize()) {
                    MainTabContent(
                        pagerState = pagerState,
                        enabledTabs = enabledTabs,
                        selectedTab = navigationState.selectedTab,
                        hasDetailOpen = hasDetailOpen,
                        isEqualizerOpen = navigationState.isEqualizerOpen,
                        innerPadding = innerPadding,
                        homeUiState = homeUiState,
                        onHomeAction = onHomeAction,
                        libraryUiState = libraryUiState,
                        onLibraryAction = onLibraryAction,
                        collectionUiState = collectionUiState,
                        onCollectionAction = onCollectionAction,
                        playerUiState = playerUiState,
                        onSongAction = onSongActionDispatcher,
                        onSongClick = { song, queue ->
                            onPlayerAction(PlayerAction.PlaySong(song, queue ?: emptyList()))
                        },
                        onArtistSelect = { artistName ->
                            navigationState.onAction(NavigationAction.OpenArtist(artistName, navigationState.selectedTab))
                            onLibraryAction(LibraryAction.SelectAlbum(null))
                            onLibraryAction(LibraryAction.SelectArtist(artistName))
                            val targetIndex = enabledTabs.indexOf(TabItem.ARTIST)
                            if (targetIndex != -1) {
                                coroutineScope.launch { pagerState.scrollToPage(targetIndex) }
                            }
                        },
                        onAlbumSelect = { albumName ->
                            navigationState.onAction(NavigationAction.OpenAlbum(albumName, navigationState.selectedTab))
                            onLibraryAction(LibraryAction.SelectArtist(null))
                            onLibraryAction(LibraryAction.SelectAlbum(albumName))
                            val targetIndex = enabledTabs.indexOf(TabItem.ALBUM)
                            if (targetIndex != -1) {
                                coroutineScope.launch { pagerState.scrollToPage(targetIndex) }
                            }
                        },
                        onMenuClick = { coroutineScope.launch { drawerState.smoothOpen() } },
                        onRequestPermission = onRequestPermission,
                        onRefresh = onRefreshMedia,
                        onBackFromDetail = {
                            onLibraryAction(LibraryAction.ClearDetail)
                            navigationState.detailSourceTab?.let { origin ->
                                navigationState.onAction(NavigationAction.ClearDetailSourceTab)
                                selectTab(origin, animate = false)
                            }
                        },
                        artistListState = artistListState,
                        artistGridState = artistGridState,
                        albumListState = albumListState,
                        albumGridState = albumGridState
                    )

                    androidx.compose.animation.AnimatedVisibility(
                        visible = navigationState.isEqualizerOpen,
                        enter = androidx.compose.animation.slideInHorizontally(
                            animationSpec = com.example.ui.common.theme.ExpressiveMotion.SlideSpringIntOffset
                        ) { fullWidth -> (fullWidth * 0.35f).toInt() } + androidx.compose.animation.fadeIn(
                            animationSpec = com.example.ui.common.theme.ExpressiveMotion.SnappySpring
                        ),
                        exit = androidx.compose.animation.slideOutHorizontally(
                            animationSpec = com.example.ui.common.theme.ExpressiveMotion.SlideSpringIntOffset
                        ) { fullWidth -> (fullWidth * 0.35f).toInt() } + androidx.compose.animation.fadeOut(
                            animationSpec = com.example.ui.common.theme.ExpressiveMotion.SnappySpring
                        )
                    ) {
                        EqualizerScreen(
                            uiState = equalizerUiState,
                            onAction = onEqualizerAction,
                            onBackClick = { navigationState.onAction(NavigationAction.SetEqualizerOpen(false)) },
                            onMenuClick = { coroutineScope.launch { drawerState.smoothOpen() } },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .background(MaterialTheme.colorScheme.background)
                        )
                    }
                }
            }

            // Drawer Screen Overlay (History, Favorites, Playlist) with smooth spring animation
            DrawerScreenOverlay(
                activeDrawerScreen = navigationState.activeDrawerScreen,
                onClose = { navigationState.onAction(NavigationAction.OpenDrawerScreen(null)) },
                collectionUiState = collectionUiState,
                playbackInfo = playerUiState.playbackInfo,
                onCollectionAction = onCollectionAction,
                onSongActionExternal = onSongActionDispatcher
            )

            // Settings Overlay with smooth spring animation
            SettingsOverlay(
                isOpen = navigationState.isSettingsOpen,
                onClose = { navigationState.onAction(NavigationAction.SetSettingsOpen(false)) },
                uiState = settingsUiState,
                onAction = onSettingsAction,
                onNavigateToEqualizer = {
                    coroutineScope.launch { drawerState.snapTo(DrawerValue.Closed) }
                    navigationState.onAction(NavigationAction.SetSettingsOpen(false))
                    navigationState.onAction(NavigationAction.SetEqualizerOpen(true))
                },
                onRefreshLibrary = onRefreshMedia
            )

            // Interactive Expanding Player Surface
            PlayerHost(
                uiState = playerUiState,
                onAction = onPlayerAction,
                playbackProgressFlow = playbackProgressFlow,
                isEqualizerOpen = navigationState.isEqualizerOpen,
                isLyricsOpen = navigationState.isLyricsOpen,
                isSettingsOpen = navigationState.isSettingsOpen,
                hasDrawerScreenOpen = navigationState.activeDrawerScreen != null,
                onNavigateToEqualizer = {
                    coroutineScope.launch { drawerState.snapTo(DrawerValue.Closed) }
                    navigationState.onAction(NavigationAction.SetEqualizerOpen(true))
                },
                onNavigateToLyrics = {
                    navigationState.onAction(NavigationAction.SetLyricsOpen(true))
                }
            )

            // Full-screen Lyrics Child Screen
            androidx.compose.animation.AnimatedVisibility(
                visible = navigationState.isLyricsOpen,
                enter = androidx.compose.animation.slideInHorizontally(
                    animationSpec = com.example.ui.common.theme.ExpressiveMotion.SlideSpringIntOffset
                ) { fullWidth -> (fullWidth * 0.35f).toInt() } + androidx.compose.animation.fadeIn(
                    animationSpec = com.example.ui.common.theme.ExpressiveMotion.SnappySpring
                ),
                exit = androidx.compose.animation.slideOutHorizontally(
                    animationSpec = com.example.ui.common.theme.ExpressiveMotion.SlideSpringIntOffset
                ) { fullWidth -> (fullWidth * 0.35f).toInt() } + androidx.compose.animation.fadeOut(
                    animationSpec = com.example.ui.common.theme.ExpressiveMotion.SnappySpring
                )
            ) {
                com.example.ui.lyrics.LyricsChildScreen(
                    song = playerUiState.currentSong,
                    playbackProgressFlow = playbackProgressFlow,
                    isPlaying = playerUiState.playbackInfo.isPlaying,
                    onBackClick = { navigationState.onAction(NavigationAction.SetLyricsOpen(false)) },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Add to Playlist Overlay Dialog
            AddToPlaylistOverlay(
                song = navigationState.songForAddToPlaylist,
                playlists = collectionUiState.playlists,
                onDismiss = { navigationState.onAction(NavigationAction.DismissAddToPlaylist) },
                onAddSongToPlaylist = { playlistId, songId ->
                    onCollectionAction(CollectionAction.AddSongToPlaylist(playlistId, songId))
                },
                onCreatePlaylist = { name, callback ->
                    onCollectionAction(CollectionAction.CreatePlaylist(name, callback))
                }
            )
        }
    }
}
}
