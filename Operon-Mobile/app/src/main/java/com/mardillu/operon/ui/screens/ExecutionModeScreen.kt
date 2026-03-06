package com.mardillu.operon.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mardillu.operon.data.ExecutionMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExecutionModeScreen(
    currentMode: ExecutionMode,
    onModeSelected: (ExecutionMode) -> Unit,
    onProceed: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agent Autonomy") },
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
                .fillMaxSize()
        ) {
            Text(
                text = "How should the agent execute actions?",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Choose the level of autonomy the Copilot has when interacting with your screen.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(48.dp))

            ModeOptionCard(
                title = "Always Execute",
                description = "Agent performs all actions immediately without asking for approval.",
                selected = currentMode == ExecutionMode.ALWAYS_EXECUTE,
                onClick = { onModeSelected(ExecutionMode.ALWAYS_EXECUTE) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ModeOptionCard(
                title = "Request Approval for Some (Recommended)",
                description = "Agent performs safe actions like scrolling immediately, but asks permission for sensitive actions like clicking forms or injecting text.",
                selected = currentMode == ExecutionMode.ASK_SOME_RECOMMENDED,
                onClick = { onModeSelected(ExecutionMode.ASK_SOME_RECOMMENDED) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ModeOptionCard(
                title = "Always Request Approval",
                description = "Agent halts and asks for permission before executing any action whatsoever.",
                selected = currentMode == ExecutionMode.ALWAYS_ASK,
                onClick = { onModeSelected(ExecutionMode.ALWAYS_ASK) }
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onProceed,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Finish Setup", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun ModeOptionCard(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = onClick
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title, 
                    style = MaterialTheme.typography.titleMedium,
                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description, 
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
