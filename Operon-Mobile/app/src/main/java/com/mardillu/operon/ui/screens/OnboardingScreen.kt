package com.mardillu.operon.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onEnableAccessibility: () -> Unit,
    onRequestScreenCapture: () -> Unit,
    onProceed: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Welcome to Operon") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Let's set up your Autopilot",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Operon requires a few system permissions to be able to see and interact with your screen securely.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(48.dp))

            PermissionCard(
                title = "Accessibility Service",
                description = "Required to interact with UI elements on your behalf.",
                onClick = onEnableAccessibility
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            PermissionCard(
                title = "Screen Capture",
                description = "Required to allow the AI to 'see' the screen.",
                onClick = onRequestScreenCapture
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onProceed,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Continue to Home", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun PermissionCard(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = description, style = MaterialTheme.typography.bodyMedium)
            }
            Icon(
                imageVector = Icons.Rounded.CheckCircle, 
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
