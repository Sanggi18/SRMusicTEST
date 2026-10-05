package com.example.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import com.example.ui.common.theme.ExpressiveMotion
import com.example.ui.common.theme.LocalSpacing
import com.example.ui.navigation.components.TabItem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.sidebar.core.CollectionAction
import com.example.ui.sidebar.core.CollectionUiState
import com.example.ui.home.core.HomeAction
import com.example.ui.home.HomeScreen
import com.example.ui.home.core.HomeUiState
import com.example.ui.library.core.LibraryAction
import com.example.ui.library.core.LibraryUiState
import com.example.ui.library.core.SongAction
import com.example.ui.library.core.toCore
import com.example.ui.library.core.toUi
import com.example.ui.library.album.AlbumDetailScreen
import com.example.ui.library.album.AlbumScreen
import com.example.ui.library.artist.ArtistDetailScreen
import com.example.ui.library.artist.ArtistScreen
import com.example.ui.library.folder.FolderScreen
import com.example.ui.library.song.SongScreen
import com.example.ui.player.core.PlayerUiState
import com.example.core.model.Song

fun getTabIcon(tab: TabItem): ImageVector {
    return when (tab) {
        TabItem.HOME -> Icons.Rounded.Home
        TabItem.SONG -> Icons.Rounded.MusicNote
        TabItem.FOLDER -> Icons.Rounded.Folder
        TabItem.ARTIST -> Icons.Rounded.Person
        TabItem.ALBUM -> Icons.Rounded.Album
    }
}

@Composable
fun MainBottomNavigationBar(
    enabledTabs: List<TabItem>,
    selectedTab: TabItem,
    pagerState: PagerState? = null,
    expansionProgress: Float = 0f,
    isEqualizerOpen: Boolean,
    onTabSelected: (TabItem) -> Unit
) {
    if (isEqualizerOpen || expansionProgress >= 0.999f) return

    val spacing = LocalSpacing.current
    val navBarAlpha = (1f - expansionProgress * 2.0f).coerceIn(0f, 1f)
    val navBarBottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val navBarTotalHeight = spacing.navigationBarHeight + navBarBottomInset + 16.dp
    val navBarTotalHeightPx = with(LocalDensity.current) { navBarTotalHeight.toPx() }

    if (navBarAlpha > 0f) {
        val pillShape = RoundedCornerShape(28.dp)

        NavigationBar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .padding(bottom = navBarBottomInset + 8.dp)
                .graphicsLayer {
                    translationY = expansionProgress * navBarTotalHeightPx
                    alpha = navBarAlpha
                }
                .clip(pillShape)
                .border(
                    width = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                    shape = pillShape
                ),
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = 3.dp,
            windowInsets = WindowInsets(0, 0, 0, 0)
        ) {
            enabledTabs.forEach { tab ->
                val isSelected = selectedTab == tab
                val iconScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.15f else 1.0f,
                    animationSpec = ExpressiveMotion.BouncySpring,
                    label = "tab_icon_scale"
                )

                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onTabSelected(tab) },
                    icon = {
                        Icon(
                            imageVector = getTabIcon(tab),
                            contentDescription = tab.label,
                            modifier = Modifier.graphicsLayer {
                                scaleX = iconScale
                                scaleY = iconScale
                            }
                        )
                    },
                    label = {
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                            )
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                    )
                )
            }
        }
    }
}

@Composable
fun MainTabContent(
    pagerState: PagerState,
    enabledTabs: List<TabItem>,
    selectedTab: TabItem,
    hasDetailOpen: Boolean,
    isEqualizerOpen: Boolean,
    innerPadding: PaddingValues,
    homeUiState: HomeUiState,
    onHomeAction: (HomeAction) -> Unit,
    libraryUiState: LibraryUiState,
    onLibraryAction: (LibraryAction) -> Unit,
    collectionUiState: CollectionUiState,
    onCollectionAction: (CollectionAction) -> Unit,
    playerUiState: PlayerUiState,
    onSongAction: (SongAction, Song) -> Unit,
    onSongClick: (Song, List<Song>?) -> Unit,
    onArtistSelect: (String) -> Unit,
    onAlbumSelect: (String) -> Unit,
    onMenuClick: () -> Unit,
    onRequestPermission: () -> Unit,
    onRefresh: () -> Unit,
    onBackFromDetail: () -> Unit,
    artistListState: LazyListState,
    artistGridState: LazyGridState,
    albumListState: LazyListState,
    albumGridState: LazyGridState
) {
    val favoriteSongIds = remember(collectionUiState.favoriteSongs) {
        collectionUiState.favoriteSongs.map { it.id }.toSet()
    }
    val currentPlayingSongId = playerUiState.playbackInfo.currentSong?.id
    val isPlaying = playerUiState.playbackInfo.isPlaying
    val coroutineScope = rememberCoroutineScope()

    val bottomPadding = innerPadding.calculateBottomPadding()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (enabledTabs.contains(selectedTab)) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = !hasDetailOpen && !isEqualizerOpen,
                beyondViewportPageCount = 1,
                key = { page -> enabledTabs.getOrNull(page)?.id ?: page }
            ) { page ->
                val currentTab = enabledTabs.getOrNull(page)
                when (currentTab) {
                    TabItem.HOME -> {
                        HomeScreen(
                            uiState = homeUiState,
                            currentPlayingSongId = currentPlayingSongId,
                            isPlaying = isPlaying,
                            onArtistClick = onArtistSelect,
                            onAlbumClick = onAlbumSelect,
                            onMenuClick = onMenuClick,
                            onAction = onHomeAction
                        )
                    }
                    TabItem.SONG -> {
                        SongScreen(
                            uiState = libraryUiState,
                            currentPlayingSongId = currentPlayingSongId,
                            isPlaying = isPlaying,
                            onPermissionGranted = { onLibraryAction(LibraryAction.OnPermissionResult(it)) },
                            onRequestPermission = onRequestPermission,
                            onRefresh = onRefresh,
                            onSongClick = onSongClick,
                            onSongAction = onSongAction,
                            onMenuClick = onMenuClick,
                            searchQuery = libraryUiState.searchQuery,
                            onSearchQueryChange = { onLibraryAction(LibraryAction.Search(it)) },
                            sortCriteria = libraryUiState.songSortCriteria.toUi(),
                            onSortCriteriaChange = { onLibraryAction(LibraryAction.UpdateSongSort(it.toCore(), libraryUiState.songIsAscending)) },
                            isAscending = libraryUiState.songIsAscending,
                            onAscendingChange = { onLibraryAction(LibraryAction.UpdateSongSort(libraryUiState.songSortCriteria, it)) },
                            favoriteSongIds = favoriteSongIds,
                            onToggleFavorite = { song -> onCollectionAction(CollectionAction.ToggleFavorite(song.id)) }
                        )
                    }
                    TabItem.FOLDER -> {
                        FolderScreen(
                            uiState = libraryUiState,
                            currentPlayingSongId = currentPlayingSongId,
                            isPlaying = isPlaying,
                            onSongClick = onSongClick,
                            onMenuClick = onMenuClick,
                            searchQuery = libraryUiState.searchQuery,
                            onSearchQueryChange = { onLibraryAction(LibraryAction.Search(it)) },
                            rootFolderDisplay = libraryUiState.rootFolderDisplay,
                            currentRelativePath = libraryUiState.currentFolderRelativePath,
                            onRelativePathChange = { onLibraryAction(LibraryAction.SelectFolder(it)) },
                            onCurrentRelativePathChange = { onLibraryAction(LibraryAction.SelectFolder(it)) },
                            folderStyle = libraryUiState.folderStyle.toUi(),
                            onFolderStyleChange = { onLibraryAction(LibraryAction.ChangeFolderStyle(it.toCore())) },
                            onPlayNextBatch = { songs -> songs.forEach { onSongAction(SongAction.PLAY_NEXT, it) } },
                            onAddToQueueBatch = { songs -> songs.forEach { onSongAction(SongAction.ADD_TO_QUEUE, it) } },
                            onSongAction = onSongAction,
                            favoriteSongIds = favoriteSongIds,
                            onToggleFavorite = { song -> onCollectionAction(CollectionAction.ToggleFavorite(song.id)) }
                        )
                    }
                    TabItem.ARTIST -> {
                        ArtistScreen(
                            uiState = libraryUiState,
                            currentPlayingSongId = currentPlayingSongId,
                            isPlaying = isPlaying,
                            onSongClick = onSongClick,
                            onMenuClick = onMenuClick,
                            searchQuery = libraryUiState.searchQuery,
                            onSearchQueryChange = { onLibraryAction(LibraryAction.Search(it)) },
                            selectedArtistName = libraryUiState.selectedArtistName,
                            onSelectedArtistChange = { onLibraryAction(LibraryAction.SelectArtist(it)) },
                            sortCriteria = libraryUiState.artistSortCriteria.toUi(),
                            onSortCriteriaChange = { onLibraryAction(LibraryAction.UpdateArtistSort(it.toCore(), libraryUiState.artistIsAscending)) },
                            isAscending = libraryUiState.artistIsAscending,
                            onAscendingChange = { onLibraryAction(LibraryAction.UpdateArtistSort(libraryUiState.artistSortCriteria, it)) },
                            gridMode = libraryUiState.artistGridMode.toUi(),
                            onGridModeChange = { onLibraryAction(LibraryAction.UpdateArtistGridMode(it.toCore())) },
                            onSongAction = onSongAction,
                            favoriteSongIds = favoriteSongIds,
                            onToggleFavorite = { song -> onCollectionAction(CollectionAction.ToggleFavorite(song.id)) },
                            listState = artistListState,
                            gridState = artistGridState
                        )
                    }
                    TabItem.ALBUM -> {
                        AlbumScreen(
                            uiState = libraryUiState,
                            currentPlayingSongId = currentPlayingSongId,
                            isPlaying = isPlaying,
                            onSongClick = onSongClick,
                            onMenuClick = onMenuClick,
                            searchQuery = libraryUiState.searchQuery,
                            onSearchQueryChange = { onLibraryAction(LibraryAction.Search(it)) },
                            selectedAlbumName = libraryUiState.selectedAlbumName,
                            onSelectedAlbumChange = { onLibraryAction(LibraryAction.SelectAlbum(it)) },
                            sortCriteria = libraryUiState.albumSortCriteria.toUi(),
                            onSortCriteriaChange = { onLibraryAction(LibraryAction.UpdateAlbumSort(it.toCore(), libraryUiState.albumIsAscending)) },
                            isAscending = libraryUiState.albumIsAscending,
                            onAscendingChange = { onLibraryAction(LibraryAction.UpdateAlbumSort(libraryUiState.albumSortCriteria, it)) },
                            gridMode = libraryUiState.albumGridMode.toUi(),
                            onGridModeChange = { onLibraryAction(LibraryAction.UpdateAlbumGridMode(it.toCore())) },
                            onSongAction = onSongAction,
                            favoriteSongIds = favoriteSongIds,
                            onToggleFavorite = { song -> onCollectionAction(CollectionAction.ToggleFavorite(song.id)) },
                            listState = albumListState,
                            gridState = albumGridState
                        )
                    }
                    null -> {}
                }
            }
        }

        AnimatedVisibility(
            visible = libraryUiState.selectedArtistName != null,
            enter = slideInHorizontally(animationSpec = ExpressiveMotion.SlideSpringIntOffset) { fullWidth -> (fullWidth * 0.35f).toInt() } +
                    fadeIn(animationSpec = ExpressiveMotion.SnappySpring),
            exit = slideOutHorizontally(animationSpec = ExpressiveMotion.SlideSpringIntOffset) { fullWidth -> (fullWidth * 0.35f).toInt() } +
                    fadeOut(animationSpec = ExpressiveMotion.SnappySpring)
        ) {
            libraryUiState.selectedArtistName?.let { artistName ->
                ArtistDetailScreen(
                    artistName = artistName,
                    songs = libraryUiState.songs,
                    currentPlayingSongId = currentPlayingSongId,
                    isPlaying = isPlaying,
                    onSongClick = onSongClick,
                    onBackClick = onBackFromDetail,
                    onSongAction = onSongAction,
                    favoriteSongIds = favoriteSongIds,
                    onToggleFavorite = { song -> onCollectionAction(CollectionAction.ToggleFavorite(song.id)) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = innerPadding.calculateBottomPadding())
                        .background(MaterialTheme.colorScheme.background)
                )
            }
        }

        AnimatedVisibility(
            visible = libraryUiState.selectedAlbumName != null,
            enter = slideInHorizontally(animationSpec = ExpressiveMotion.SlideSpringIntOffset) { fullWidth -> (fullWidth * 0.35f).toInt() } +
                    fadeIn(animationSpec = ExpressiveMotion.SnappySpring),
            exit = slideOutHorizontally(animationSpec = ExpressiveMotion.SlideSpringIntOffset) { fullWidth -> (fullWidth * 0.35f).toInt() } +
                    fadeOut(animationSpec = ExpressiveMotion.SnappySpring)
        ) {
            libraryUiState.selectedAlbumName?.let { albumName ->
                AlbumDetailScreen(
                    albumName = albumName,
                    songs = libraryUiState.songs,
                    currentPlayingSongId = currentPlayingSongId,
                    isPlaying = isPlaying,
                    onSongClick = onSongClick,
                    onBackClick = onBackFromDetail,
                    onSongAction = onSongAction,
                    favoriteSongIds = favoriteSongIds,
                    onToggleFavorite = { song -> onCollectionAction(CollectionAction.ToggleFavorite(song.id)) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = innerPadding.calculateBottomPadding())
                        .background(MaterialTheme.colorScheme.background)
                )
            }
        }
    }
}
