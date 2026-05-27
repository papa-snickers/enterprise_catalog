package com.example.enterprisecatalog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.enterprisecatalog.data.api.ApiClient
import com.example.enterprisecatalog.data.local.DataStoreManager
import com.example.enterprisecatalog.navigation.NavGraph
import com.example.enterprisecatalog.ui.theme.EnterpriseCatalogTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var dataStoreManager: DataStoreManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        dataStoreManager = DataStoreManager(applicationContext)
        val apiService = ApiClient.create(dataStoreManager)

        setContent {
            val darkTheme by dataStoreManager.getDarkThemeFlow()
                .collectAsStateWithLifecycle(initialValue = false)

            val scope = rememberCoroutineScope()

            EnterpriseCatalogTheme(darkTheme = darkTheme) {
                val navController = rememberNavController()
                NavGraph(
                    navController = navController,
                    apiService = apiService,
                    dataStoreManager = dataStoreManager,
                    onToggleTheme = {
                        scope.launch { dataStoreManager.setDarkTheme(!darkTheme) }
                    }
                )
            }
        }
    }
}
