package com.example.ui.sidebar.playlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.data.database.PlaylistEntity
import com.example.core.data.playlist.PlaylistFileManager.PlaylistFormat
import com.example.core.model.Song
import com.example.ui.sidebar.core.PlaylistSortCriteria

@Composable
fun NewPlaylistDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Playlist") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Playlist Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onCreate(name.trim())
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun RenamePlaylistDialog(
    playlist: PlaylistEntity,
    onDismiss: () -> Unit,
    onRename: (Long, String) -> Unit
) {
    var name by remember { mutableStateOf(playlist.name) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename Playlist") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("New Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onRename(playlist.id, name.trim())
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Rename")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DeletePlaylistDialog(
    playlist: PlaylistEntity,
    onDismiss: () -> Unit,
    onDelete: (Long) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Playlist") },
        text = { Text("Are you sure you want to delete '${playlist.name}'?") },
        confirmButton = {
            Button(
                onClick = { onDelete(playlist.id) }
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DeleteMultiplePlaylistsDialog(
    playlists: List<PlaylistEntity>,
    onDismiss: () -> Unit,
    onDeletePlaylists: (List<Long>) -> Unit
) {
    val selectedIds = remember { mutableStateMapOf<Long, Boolean>() }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Playlists") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Select playlists to delete:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyColumn(modifier = Modifier.heightIn(max = 280.dp)) {
                    items(playlists, key = { it.id }) { p ->
                        val isChecked = selectedIds[p.id] == true
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedIds[p.id] = !isChecked }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { selectedIds[p.id] = it }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = p.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        },
        confirmButton = {
            val count = selectedIds.filterValues { it }.size
            Button(
                onClick = {
                    val toDelete = selectedIds.filterValues { it }.keys.toList()
                    if (toDelete.isNotEmpty()) {
                        onDeletePlaylists(toDelete)
                    }
                },
                enabled = count > 0
            ) {
                Text("Delete ($count)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun PlaylistSortDialog(
    currentSort: PlaylistSortCriteria,
    isAscending: Boolean,
    onDismiss: () -> Unit,
    onApplySort: (PlaylistSortCriteria, Boolean) -> Unit
) {
    var tempCriteria by remember { mutableStateOf(currentSort) }
    var tempAsc by remember { mutableStateOf(isAscending) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sort Playlists") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                PlaylistSortCriteria.entries.forEach { criteria ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { tempCriteria = criteria }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = tempCriteria == criteria,
                            onClick = { tempCriteria = criteria }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(criteria.displayName)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { tempAsc = !tempAsc }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(if (tempAsc) "Ascending Order" else "Descending Order")
                    IconButton(onClick = { tempAsc = !tempAsc }) {
                        Icon(
                            imageVector = if (tempAsc) Icons.Rounded.ArrowUpward else Icons.Rounded.ArrowDownward,
                            contentDescription = "Toggle sort direction"
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onApplySort(tempCriteria, tempAsc) }) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ExportFormatDialog(
    playlist: PlaylistEntity,
    currentFormat: PlaylistFormat,
    onDismiss: () -> Unit,
    onFormatSelected: (PlaylistFormat) -> Unit
) {
    var selected by remember { mutableStateOf(currentFormat) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Export '${playlist.name}'") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Select export format:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                PlaylistFormat.entries.forEach { format ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selected = format }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selected == format,
                            onClick = { selected = format }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(format.displayName)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onFormatSelected(selected) }) {
                Text("Export")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddSongsToPlaylistDialog(
    playlist: PlaylistEntity,
    allSongs: List<Song>,
    existingSongs: List<Song>,
    onDismiss: () -> Unit,
    onAddSongs: (List<Long>) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val selectedIds = remember { mutableStateMapOf<Long, Boolean>() }
    val existingIdSet = remember(existingSongs) { existingSongs.map { it.id }.toSet() }

    val filteredSongs = remember(allSongs, searchQuery, existingIdSet) {
        allSongs.filter { !existingIdSet.contains(it.id) && (it.title.contains(searchQuery, ignoreCase = true) || it.artist.contains(searchQuery, ignoreCase = true)) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Songs to '${playlist.name}'") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search songs") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (filteredSongs.isEmpty()) {
                    Text(
                        text = if (searchQuery.isBlank()) "All library tracks are already in this playlist." else "No matching tracks found.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(filteredSongs, key = { it.id }) { song ->
                            val isChecked = selectedIds[song.id] == true
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedIds[song.id] = !isChecked }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { selectedIds[song.id] = it }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = song.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = song.artist,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            val count = selectedIds.filterValues { it }.size
            Button(
                onClick = {
                    val ids = selectedIds.filterValues { it }.keys.toList()
                    if (ids.isNotEmpty()) {
                        onAddSongs(ids)
                    }
                },
                enabled = count > 0
            ) {
                Text("Add ($count)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
