package com.example.enterprisecatalog.ui.catalog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.enterprisecatalog.data.model.Enterprise

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnterpriseBottomSheet(
    enterprise: Enterprise,
    isAdmin: Boolean,
    onDismiss: () -> Unit,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = enterprise.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            AssistChip(
                onClick = {},
                label = { Text(enterprise.specialization) }
            )

            HorizontalDivider()

            Text(
                text = "О предприятии",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = enterprise.description.ifBlank { "—" },
                fontSize = 14.sp
            )

            HorizontalDivider()

            Text(
                text = "Контакты",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            if (enterprise.address.isNotBlank()) {
                ContactRow(icon = Icons.Outlined.LocationOn, label = enterprise.address)
            }
            if (enterprise.phone.isNotBlank()) {
                ContactRow(icon = Icons.Outlined.Phone, label = enterprise.phone)
            }
            if (enterprise.email.isNotBlank()) {
                ContactRow(icon = Icons.Outlined.Email, label = enterprise.email)
            }
            if (enterprise.website.isNotBlank()) {
                ContactRow(icon = Icons.Outlined.Language, label = enterprise.website)
            }

            if (isAdmin) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { onDismiss(); onEdit() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Outlined.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Редактировать")
                    }
                    Button(
                        onClick = { onDismiss(); onDelete() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Удалить")
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactRow(icon: ImageVector, label: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .size(16.dp)
                .padding(top = 2.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
