package org.dlof.tm.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.dlof.tm.R
import org.dlof.tm.builder.DlofPkgBuilder
import org.dlof.tm.data.TemplateRepository
import org.dlof.tm.model.DlofTemplateProject
import org.dlof.tm.ui.components.InfoCard
import org.dlof.tm.util.FileUtils
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    repository: TemplateRepository,
    projectId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val project = remember { repository.get(projectId) ?: DlofTemplateProject(id = projectId) }

    var isBuilding by remember { mutableStateOf(false) }
    var resultFile by remember { mutableStateOf<File?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val pageCount = project.imageChapters.sumOf { it.pages.size }
    val videoCount = project.videoEpisodes.count { it.videoUri != null }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_export)) },
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(stringResource(R.string.export_summary), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

            InfoCard(
                title = project.title.ifBlank { project.packageId },
                subtitle = "${project.packageId} · ${project.domain} · ${project.language} · v${project.version}"
            )
            InfoCard(
                title = stringResource(R.string.media_chapters),
                subtitle = "${project.imageChapters.size} " + stringResource(R.string.media_chapters) + " · $pageCount " + stringResource(R.string.media_pick_images)
            )
            InfoCard(
                title = stringResource(R.string.media_episodes),
                subtitle = "${project.videoEpisodes.size} " + stringResource(R.string.media_episodes) + " · $videoCount " + stringResource(R.string.media_pick_video)
            )
            InfoCard(
                title = stringResource(R.string.media_fonts),
                subtitle = "${project.fonts.size} " + stringResource(R.string.media_fonts)
            )
            InfoCard(
                title = stringResource(R.string.pkg_crypto_enable),
                subtitle = if (project.cryptoEnabled) project.cryptoProfile else "—"
            )

            Button(
                onClick = {
                    isBuilding = true
                    errorMessage = null
                    resultFile = null
                    scope.launch {
                        val outDir = File(context.cacheDir, "exported").apply { mkdirs() }
                        val outFile = File(outDir, DlofPkgBuilder.suggestedFileName(project))
                        val result = withContext(Dispatchers.IO) {
                            val builder = DlofPkgBuilder(context.contentResolver)
                            FileOutputStream(outFile).use { fos ->
                                builder.build(project, fos)
                            }
                        }
                        isBuilding = false
                        when (result) {
                            is DlofPkgBuilder.Result.Success -> resultFile = outFile
                            is DlofPkgBuilder.Result.Failure -> errorMessage = result.message
                        }
                    }
                },
                enabled = !isBuilding,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isBuilding) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                } else {
                    Icon(Icons.Filled.Build, contentDescription = null)
                    Text("  " + stringResource(R.string.export_build))
                }
            }

            resultFile?.let { file ->
                InfoCard(
                    title = stringResource(R.string.export_success),
                    subtitle = "${file.name} · ${FileUtils.humanFileSize(file.length())}"
                )
                Button(
                    onClick = {
                        val uri = FileUtils.shareUriFor(context, file)
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/octet-stream"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, file.name))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null)
                    Text("  " + stringResource(R.string.export_share))
                }
            }

            errorMessage?.let { msg ->
                InfoCard(title = stringResource(R.string.export_failure), subtitle = msg)
            }
        }
    }
}
