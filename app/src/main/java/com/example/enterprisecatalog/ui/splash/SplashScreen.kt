package com.example.enterprisecatalog.ui.splash

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.enterprisecatalog.data.local.DataStoreManager
import kotlinx.coroutines.flow.first

@Composable
fun SplashScreen(
    dataStoreManager: DataStoreManager,
    onNavigateToLogin: () -> Unit,
    onNavigateToCatalog: () -> Unit,
    onNavigateToAdmin: () -> Unit
) {
    LaunchedEffect(Unit) {
        val token = dataStoreManager.getTokenFlow().first()
        val role = dataStoreManager.getRoleFlow().first()
        if (token.isNullOrBlank()) {
            onNavigateToLogin()
        } else {
            if (role == "ADMIN") onNavigateToAdmin()
            else onNavigateToCatalog()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Каталог предприятий",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    }
}
