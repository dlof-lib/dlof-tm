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
import org.dlof.tm.model.TemplateLayout
import org.dlof.tm.ui.components.HexColorField
import org.dlof.tm.ui.components.LabeledDropdown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesignScreen(
    repository: TemplateRepository,
    projectId: String,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val project = remember { repository.get(projectId) ?: DlofTemplateProject(id = projectId) }

    var primary by remember { mutableStateOf(project.primaryColor) }
    var secondary by remember { mutableStateOf(project.secondaryColor) }
    var background by remember { mutableStateOf(project.backgroundColor) }
    var textColor by remember { mutableStateOf(project.textColor) }
    var fontFamily by remember { mutableStateOf(project.fontFamily) }
    var layout by remember { mutableStateOf(project.layout) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_design)) },
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
            HexColorField(stringResource(R.string.design_primary), primary) { primary = it }
            HexColorField(stringResource(R.string.design_secondary), secondary) { secondary = it }
            HexColorField(stringResource(R.string.design_background), background) { background = it }
            HexColorField(stringResource(R.string.design_text), textColor) { textColor = it }

            OutlinedTextField(
                value = fontFamily,
                onValueChange = { fontFamily = it },
                label = { Text(stringResource(R.string.design_font)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            LabeledDropdown(
                label = stringResource(R.string.design_layout),
                options = TemplateLayout.entries,
                selected = layout,
                optionText = { it.value },
                onSelected = { layout = it },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    project.primaryColor = primary
                    project.secondaryColor = secondary
                    project.backgroundColor = background
                    project.textColor = textColor
                    project.fontFamily = fontFamily
                    project.layout = layout
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
