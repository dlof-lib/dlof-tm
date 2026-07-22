package org.dlof.tm.ui.screens

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
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.dlof.tm.R
import org.dlof.tm.data.TemplateRepository
import org.dlof.tm.model.Base64Mode
import org.dlof.tm.model.DlofTemplateProject
import org.dlof.tm.ui.components.LabeledDropdown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackageSettingsScreen(
    repository: TemplateRepository,
    projectId: String,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val project = remember { repository.get(projectId) ?: DlofTemplateProject(id = projectId) }

    var base64Mode by remember { mutableStateOf(project.base64Mode) }
    var cryptoEnabled by remember { mutableStateOf(project.cryptoEnabled) }
    var cryptoProfile by remember { mutableStateOf(project.cryptoProfile) }
    var compressBeforeEncrypt by remember { mutableStateOf(project.compressBeforeEncrypt) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_package)) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LabeledDropdown(
                label = stringResource(R.string.pkg_base64_mode),
                options = Base64Mode.entries,
                selected = base64Mode,
                optionText = { it.value },
                onSelected = { base64Mode = it },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.pkg_crypto_enable),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Switch(checked = cryptoEnabled, onCheckedChange = { cryptoEnabled = it })
            }

            if (cryptoEnabled) {
                OutlinedTextField(
                    value = cryptoProfile,
                    onValueChange = { cryptoProfile = it },
                    label = { Text(stringResource(R.string.pkg_crypto_profile)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.pkg_compress_before_encrypt),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(checked = compressBeforeEncrypt, onCheckedChange = { compressBeforeEncrypt = it })
                }
            }

            Button(
                onClick = {
                    project.base64Mode = base64Mode
                    project.cryptoEnabled = cryptoEnabled
                    project.cryptoProfile = cryptoProfile.ifBlank { "Best64" }
                    project.compressBeforeEncrypt = compressBeforeEncrypt
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
