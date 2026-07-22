package org.dlof.tm.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.dlof.tm.data.TemplateRepository
import org.dlof.tm.model.DlofTemplateProject
import org.dlof.tm.ui.screens.DesignScreen
import org.dlof.tm.ui.screens.ExportScreen
import org.dlof.tm.ui.screens.HomeScreen
import org.dlof.tm.ui.screens.MediaScreen
import org.dlof.tm.ui.screens.MetadataScreen
import org.dlof.tm.ui.screens.PackageSettingsScreen
import org.dlof.tm.ui.screens.SettingsScreen
import java.util.UUID

@Composable
fun DlofTmNavGraph(repository: TemplateRepository) {
    val navController: NavHostController = rememberNavController()

    NavHost(navController = navController, startDestination = Dest.HOME) {
        composable(Dest.HOME) {
            HomeScreen(
                repository = repository,
                onOpenProject = { id -> navController.navigate(Dest.metadata(id)) },
                onCreateProject = {
                    val newProject = DlofTemplateProject(id = UUID.randomUUID().toString())
                    repository.save(newProject)
                    navController.navigate(Dest.metadata(newProject.id))
                },
                onOpenSettings = { navController.navigate(Dest.SETTINGS) }
            )
        }

        composable(Dest.METADATA) { backStackEntry ->
            val id = backStackEntry.arguments?.getString(Dest.EDITOR_ARG).orEmpty()
            MetadataScreen(
                repository = repository,
                projectId = id,
                onBack = { navController.popBackStack() },
                onNext = { navController.navigate(Dest.design(id)) }
            )
        }

        composable(Dest.DESIGN) { backStackEntry ->
            val id = backStackEntry.arguments?.getString(Dest.EDITOR_ARG).orEmpty()
            DesignScreen(
                repository = repository,
                projectId = id,
                onBack = { navController.popBackStack() },
                onNext = { navController.navigate(Dest.media(id)) }
            )
        }

        composable(Dest.MEDIA) { backStackEntry ->
            val id = backStackEntry.arguments?.getString(Dest.EDITOR_ARG).orEmpty()
            MediaScreen(
                repository = repository,
                projectId = id,
                onBack = { navController.popBackStack() },
                onNext = { navController.navigate(Dest.packageSettings(id)) }
            )
        }

        composable(Dest.PACKAGE_SETTINGS) { backStackEntry ->
            val id = backStackEntry.arguments?.getString(Dest.EDITOR_ARG).orEmpty()
            PackageSettingsScreen(
                repository = repository,
                projectId = id,
                onBack = { navController.popBackStack() },
                onNext = { navController.navigate(Dest.export(id)) }
            )
        }

        composable(Dest.EXPORT) { backStackEntry ->
            val id = backStackEntry.arguments?.getString(Dest.EDITOR_ARG).orEmpty()
            ExportScreen(
                repository = repository,
                projectId = id,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Dest.SETTINGS) {
            SettingsScreen(
                repository = repository,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
