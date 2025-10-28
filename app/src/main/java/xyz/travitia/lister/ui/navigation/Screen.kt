package xyz.travitia.lister.ui.navigation

sealed class Screen(val route: String) {
    object ListOverview : Screen("list_overview")
    object ListDetail : Screen("list_detail/{listId}/{listName}") {
        fun createRoute(listId: Int, listName: String) = "list_detail/$listId/$listName"
    }
    object Settings : Screen("settings")
    object CategoryManagement : Screen("category_management")
}

