package com.example.ui.library.folder
import com.example.ui.library.core.*
import com.example.ui.library.components.*
import com.example.ui.common.components.*

data class FolderUiModel(
    val path: String,
    val name: String,
    val songCount: Int,
    val subfolderCount: Int = 0,
    val thumbnailArtworkUri: Any? = null,
    val thumbnailDataPath: String = "",
    val thumbnailAlbumId: Long = -1L,
    val thumbnailDateModified: Long = 0L
)

enum class FolderStyle {
    ICON,
    THUMBNAIL
}

enum class FolderAction {
    PLAY,
    SHUFFLE,
    PLAY_NEXT,
    ADD_TO_QUEUE
}
