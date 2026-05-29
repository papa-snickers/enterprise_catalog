package com.example.enterprisecatalog.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Catalog : Screen("catalog")
    object Admin : Screen("admin")
    object Profile : Screen("profile")
    object Favorites : Screen("favorites")
    object EnterpriseEdit : Screen("enterprise_edit?id={id}") {
        fun createRoute(id: String? = null): String =
            if (id != null) "enterprise_edit?id=$id" else "enterprise_edit"
    }
}
