package org.dlof.tm.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.dlof.tm.R
import org.dlof.tm.data.TemplateRepository
import org.dlof.tm.model.DlofTemplateProject
import org.dlof.tm.model.FontFile
import org.dlof.tm.model.ImageChapter
import org.dlof.tm.model.ImagePage
import org.dlof.tm.model.VideoEpisode
import org.dlof.tm.ui.components.SectionHeader
import org.dlof.tm.util.FileUtils
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaScreen(
    repository: TemplateRepository,
    projectId: String,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val context = LocalContext.current
    val project = remember { repository.get(projectId) ?: DlofTemplateProject(id = projectId) }

    val chapters = remember { mutableStateListOf(*project.imageChapters.toTypedArray()) }
    val episodes = remember { mutableStateListOf(*project.videoEpisodes.toTypedArray()) }
    val fonts = remember { mutableStateListOf(*project.fonts.toTypedArray()) }

    var newChapterName by remember { mutableStateOf("chapter${chapters.size + 1}") }
    var newEpisodeName by remember { mutableStateOf("episode${episodes.size + 1}") }
    var pendingChapterIndexForImages by remember { mutableStateOf(-1) }
    var pendingEpisodeIndexForVideo by remember { mutableStateOf(-1) }
    var pendingEpisodeIndexForSubtitle by remember { mutableStateOf(-1) }

    val pickImagesLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        val idx = pendingChapterIndexForImages
        if (idx in chapters.indices && uris.isNotEmpty()) {
            val pages = uris.map { uri ->
                ImagePage(uri.toString(), FileUtils.sanitizeFileName(FileUtils.displayName(context, uri)))
            }
            val existing = chapters[idx]
            chapters[idx] = existing.copy(pages = existing.pages + pages)
        }
    }

    val pickVideoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        val idx = pendingEpisodeIndexForVideo
        if (idx in episodes.indices && uri != null) {
            val existing = episodes[idx]
            episodes[idx] = existing.copy(
                videoUri = uri.toString(),
                videoFileName = FileUtils.sanitizeFileName(FileUtils.displayName(context, uri))
            )
        }
    }

    val pickSubtitleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        val idx = pendingEpisodeIndexForSubtitle
        if (idx in episodes.indices && uri != null) {
            val existing = episodes[idx]
            episodes[idx] = existing.copy(
                subtitleUri = uri.toString(),
                subtitleFileName = FileUtils.sanitizeFileName(FileUtils.displayName(context, uri))
            )
        }
    }

    val pickFontsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uris.forEach { uri ->
            fonts.add(FontFile(uri.toString(), FileUtils.sanitizeFileName(FileUtils.displayName(context, uri))))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_media)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ── image chapters ──
            SectionHeader(stringResource(R.string.media_chapters))
            chapters.forEachIndexed { index, chapter ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(chapter.name, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { chapters.removeAt(index) }) {
                                Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.action_delete))
                            }
                        }
                        Text(
                            "${chapter.pages.size} " + stringResource(R.string.media_pick_images),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        OutlinedButton(
                            onClick = {
                                pendingChapterIndexForImages = index
                                pickImagesLauncher.launch("image/*")
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Image, contentDescription = null)
                            Text("  " + stringResource(R.string.media_pick_images))
                        }
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = newChapterName,
                    onValueChange = { newChapterName = it },
                    label = { Text(stringResource(R.string.field_title)) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = {
                    val name = newChapterName.ifBlank { "chapter${chapters.size + 1}" }
                    chapters.add(ImageChapter(UUID.randomUUID().toString(), name))
                    newChapterName = "chapter${chapters.size + 1}"
                }) {
                    Text(stringResource(R.string.media_add_chapter))
                }
            }

            // ── video episodes ──
            SectionHeader(stringResource(R.string.media_episodes))
            episodes.forEachIndexed { index, episode ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(episode.name, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { episodes.removeAt(index) }) {
                                Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.action_delete))
                            }
                        }
                        if (episode.videoFileName != null) {
                            AssistChip(onClick = {}, label = { Text(episode.videoFileName) })
                        }
                        OutlinedButton(
                            onClick = {
                                pendingEpisodeIndexForVideo = index
                                pickVideoLauncher.launch("video/*")
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.VideoFile, contentDescription = null)
                            Text("  " + stringResource(R.string.media_pick_video))
                        }
                        if (episode.subtitleFileName != null) {
                            AssistChip(onClick = {}, label = { Text(episode.subtitleFileName) })
                        }
                        OutlinedButton(
                            onClick = {
                                pendingEpisodeIndexForSubtitle = index
                                pickSubtitleLauncher.launch("*/*")
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.TextFields, contentDescription = null)
                            Text("  " + stringResource(R.string.media_pick_subtitle))
                        }
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = newEpisodeName,
                    onValueChange = { newEpisodeName = it },
                    label = { Text(stringResource(R.string.field_title)) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = {
                    val name = newEpisodeName.ifBlank { "episode${episodes.size + 1}" }
                    episodes.add(
                        VideoEpisode(
                            id = UUID.randomUUID().toString(),
                            name = name,
                            videoUri = null,
                            videoFileName = null,
                            subtitleUri = null,
                            subtitleFileName = null
                        )
                    )
                    newEpisodeName = "episode${episodes.size + 1}"
                }) {
                    Text(stringResource(R.string.media_add_episode))
                }
            }

            // ── fonts ──
            SectionHeader(stringResource(R.string.media_fonts))
            fonts.forEachIndexed { index, font ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(font.fileName, modifier = Modifier.weight(1f))
                    IconButton(onClick = { fonts.removeAt(index) }) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.action_delete))
                    }
                }
            }
            OutlinedButton(
                onClick = { pickFontsLauncher.launch("*/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.media_add_fonts))
            }

            Button(
                onClick = {
                    project.imageChapters = chapters.toList()
                    project.videoEpisodes = episodes.toList()
                    project.fonts = fonts.toList()
                    repository.save(project)
                    onNext()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.action_next))
                Icon(Icons.Filled.NavigateNext, contentDescription = null)
            }
        }
    }
}
