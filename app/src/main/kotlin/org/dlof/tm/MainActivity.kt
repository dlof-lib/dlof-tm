package org.dlof.tm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.dlof.tm.data.TemplateRepository
import org.dlof.tm.ui.navigation.DlofTmNavGraph
import org.dlof.tm.ui.theme.DlofTmTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = TemplateRepository.getInstance(applicationContext)

        setContent {
            DlofTmRoot(repository)
        }
    }
}

@Composable
private fun DlofTmRoot(repository: TemplateRepository) {
    DlofTmTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            DlofTmNavGraph(repository = repository)
        }
    }
}
