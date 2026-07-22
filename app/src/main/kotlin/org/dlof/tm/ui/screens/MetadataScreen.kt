package org.dlof.tm.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.dlof.tm.R
import org.dlof.tm.data.TemplateRepository
import org.dlof.tm.model.DlofTemplateProject
import org.dlof.tm.ui.components.LabeledDropdown

private val DOMAINS = listOf("series", "book", "comic", "document", "qa")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetadataScreen(
    repository: TemplateRepository,
    projectId: String,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val project = remember { repository.get(projectId) ?: DlofTemplateProject(id = projectId) }

    var packageId by remember { mutableStateOf(project.packageId) }
    var title by remember { mutableStateOf(project.title) }
    var domain by remember { mutableStateOf(project.domain) }
    var author by remember { mutableStateOf(project.author) }
    var language by remember { mutableStateOf(project.language) }
    var version by remember { mutableStateOf(project.version) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_new)) },
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
            OutlinedTextField(
                value = packageId,
                onValueChange = { packageId = it },
                label = { Text(stringResource(R.string.field_id)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.field_title)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            LabeledDropdown(
                label = stringResource(R.string.field_domain),
                options = DOMAINS,
                selected = domain,
                optionText = { it },
                onSelected = { domain = it },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = author,
                onValueChange = { author = it },
                label = { Text(stringResource(R.string.field_author)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = language,
                onValueChange = { language = it },
                label = { Text(stringResource(R.string.field_language)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = version,
                onValueChange = { version = it },
                label = { Text(stringResource(R.string.field_version)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    project.packageId = packageId.ifBlank { "my-package" }
                    project.title = title
                    project.domain = domain
                    project.author = author
                    project.language = language.ifBlank { "ar" }
                    project.version = version.ifBlank { "2.0" }
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
