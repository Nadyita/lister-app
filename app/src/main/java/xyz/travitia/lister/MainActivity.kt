package xyz.travitia.lister

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import xyz.travitia.lister.ui.navigation.Screen
import xyz.travitia.lister.ui.screens.CategoryManagementScreen
import xyz.travitia.lister.ui.screens.ListDetailScreen
import xyz.travitia.lister.ui.screens.ListOverviewScreen
import xyz.travitia.lister.ui.screens.SettingsScreen
import xyz.travitia.lister.ui.theme.ListerTheme
import xyz.travitia.lister.ui.viewmodel.CategoryManagementViewModel
import xyz.travitia.lister.ui.viewmodel.ListDetailViewModel
import xyz.travitia.lister.ui.viewmodel.ListOverviewViewModel
import xyz.travitia.lister.ui.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ListerTheme {
                ListerApp()
            }
        }
    }
}

@Composable
fun ListerApp() {
    val navController = rememberNavController()
    val application = androidx.compose.ui.platform.LocalContext.current.applicationContext as ListerApplication

    NavHost(
        navController = navController,
        startDestination = Screen.ListOverview.route
    ) {
        composable(Screen.ListOverview.route) {
            val viewModel: ListOverviewViewModel = viewModel(
                factory = ListOverviewViewModelFactory(application.repository, application.settingsPreferences)
            )
            ListOverviewScreen(
                viewModel = viewModel,
                onNavigateToList = { listId, listName ->
                    navController.navigate(Screen.ListDetail.createRoute(listId, listName))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(
            route = Screen.ListDetail.route,
            arguments = listOf(
                navArgument("listId") { type = NavType.IntType },
                navArgument("listName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val listId = backStackEntry.arguments?.getInt("listId") ?: return@composable
            val listName = backStackEntry.arguments?.getString("listName") ?: return@composable

            val viewModel: ListDetailViewModel = viewModel(
                factory = ListDetailViewModelFactory(application.repository, application.settingsPreferences)
            )

            ListDetailScreen(
                viewModel = viewModel,
                listId = listId,
                listName = listName,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            val viewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModelFactory(application.settingsPreferences)
            )
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCategories = {
                    navController.navigate(Screen.CategoryManagement.route)
                }
            )
        }

        composable(Screen.CategoryManagement.route) {
            val viewModel: CategoryManagementViewModel = viewModel(
                factory = CategoryManagementViewModelFactory(application.repository)
            )
            CategoryManagementScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

