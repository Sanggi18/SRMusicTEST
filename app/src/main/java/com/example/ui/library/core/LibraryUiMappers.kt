package com.example.ui.library.core
import com.example.ui.library.components.*
import com.example.ui.common.components.*

import com.example.core.model.AppFolderStyle
import com.example.core.model.AppGridMode
import com.example.core.model.AppSortCriteria
import com.example.core.model.LibraryScanProgress
import com.example.ui.library.folder.FolderStyle


fun AppSortCriteria.toUi(): SortCriteria = when (this) {
    AppSortCriteria.TITLE -> SortCriteria.TITLE
    AppSortCriteria.ARTIST -> SortCriteria.ARTIST
    AppSortCriteria.ALBUM -> SortCriteria.ALBUM
    AppSortCriteria.DURATION -> SortCriteria.DURATION
    AppSortCriteria.DATE_ADDED -> SortCriteria.DATE_ADDED
    AppSortCriteria.DATE_MODIFIED -> SortCriteria.DATE_MODIFIED
    AppSortCriteria.COUNT -> SortCriteria.COUNT
    else -> SortCriteria.TITLE
}

fun SortCriteria.toCore(): AppSortCriteria = when (this) {
    SortCriteria.TITLE -> AppSortCriteria.TITLE
    SortCriteria.ARTIST -> AppSortCriteria.ARTIST
    SortCriteria.ALBUM -> AppSortCriteria.ALBUM
    SortCriteria.DURATION -> AppSortCriteria.DURATION
    SortCriteria.DATE_ADDED -> AppSortCriteria.DATE_ADDED
    SortCriteria.DATE_MODIFIED -> AppSortCriteria.DATE_MODIFIED
    SortCriteria.COUNT -> AppSortCriteria.COUNT
}

fun AppGridMode.toUi(): GridMode = GridMode.valueOf(name)
fun GridMode.toCore(): AppGridMode = AppGridMode.valueOf(name)

fun AppFolderStyle.toUi(): FolderStyle = FolderStyle.valueOf(name)
fun FolderStyle.toCore(): AppFolderStyle = AppFolderStyle.valueOf(name)

fun LibraryScanProgress.toUi(): ScanProgressState = ScanProgressState(
    isScanning = isScanning,
    currentCount = currentCount,
    isFinished = isFinished,
    title = title,
    description = description
)
