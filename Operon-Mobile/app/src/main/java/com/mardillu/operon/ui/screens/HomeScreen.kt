package com.mardillu.operon.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.Info
import com.mardillu.operon.data.ExecutionMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mardillu.operon.ui.theme.PulseColor
import com.mardillu.operon.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    currentMode: ExecutionMode,
    onModeSelected: (ExecutionMode) -> Unit
) {
    val logs by viewModel.logs.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()
    
    val context = LocalContext.current
    var isListening by remember { mutableStateOf(false) }
    var spokenText by remember { mutableStateOf("Tap the microphone and say your goal...") }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startListening(
                recognizer = speechRecognizer, 
                context = context, 
                onPartialResult = { text ->
                    spokenText = text
                },
                onFinalResult = { text ->
                    spokenText = text
                    if (text.isNotBlank()) {
                        viewModel.toggleAgent(text, currentMode)
                    }
                }, 
                onStateChanged = { listening -> isListening = listening }
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            speechRecognizer.destroy()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Operon Autopilot") },
                actions = {
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logs Area
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.large
            ) {
                LazyColumn(
                    modifier = Modifier.padding(16.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(logs) { log ->
                        Text(
                            text = log,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
                    }
                }
            }

            // Prompt Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = spokenText,
                    style = MaterialTheme.typography.titleLarge,
                    color = if (spokenText == "Tap the microphone and say your goal...") MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                if (isRunning) {
                    Button(
                        onClick = { viewModel.toggleAgent("", currentMode) }, // Stop
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier
                            .height(64.dp)
                            .fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Stop, contentDescription = "Stop")
                        Spacer(Modifier.width(8.dp))
                        Text("Stop Agent", style = MaterialTheme.typography.titleMedium)
                    }
                } else {
                    PulsingMicButton(
                        isListening = isListening,
                        onClick = {
                            if (!isListening) {
                                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            } else {
                                speechRecognizer.stopListening()
                                isListening = false
                            }
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Info, 
                        contentDescription = "AI Disclaimer",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "AI agents may produce inaccurate results. Verify actions.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (showSettingsDialog) {
        SettingsDialog(
            currentMode = currentMode,
            onModeSelected = onModeSelected,
            onDismiss = { showSettingsDialog = false }
        )
    }
}

@Composable
private fun SettingsDialog(
    currentMode: ExecutionMode,
    onModeSelected: (ExecutionMode) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agent Autonomy") },
        text = {
            Column {
                ExecutionMode.values().forEach { mode ->
                    val isSelected = currentMode == mode
                    val title = when (mode) {
                        ExecutionMode.ALWAYS_EXECUTE -> "Always Execute"
                        ExecutionMode.ASK_SOME_RECOMMENDED -> "Request for Some (Recommended)"
                        ExecutionMode.ALWAYS_ASK -> "Always Ask"
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onModeSelected(mode)
                                onDismiss()
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = isSelected, onClick = null)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(text = title, style = MaterialTheme.typography.bodyLarge)
                            if (mode == ExecutionMode.ALWAYS_EXECUTE) {
                                Text(
                                    text = "Not recommended. Agent will execute committal actions blindly without prompting you.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else if (mode == ExecutionMode.ASK_SOME_RECOMMENDED) {
                                Text(
                                    text = "Agent infers risky actions and prompts you before sending/submitting limits.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun PulsingMicButton(isListening: Boolean, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(120.dp)
    ) {
        if (isListening) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(PulseColor.copy(alpha = 0.3f))
            )
        }
        
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(if (isListening) PulseColor else MaterialTheme.colorScheme.primary)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Mic,
                contentDescription = "Microphone",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

private fun startListening(
    recognizer: SpeechRecognizer,
    context: android.content.Context,
    onPartialResult: (String) -> Unit,
    onFinalResult: (String) -> Unit,
    onStateChanged: (Boolean) -> Unit
) {
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
    }

    recognizer.setRecognitionListener(object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) { onStateChanged(true) }
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() { onStateChanged(false) }
        override fun onError(error: Int) { 
            onStateChanged(false)
            onPartialResult("Error listening. Please try again.") 
        }
        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                onFinalResult(matches[0])
            }
            onStateChanged(false)
        }
        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                onPartialResult(matches[0] + "...") // Show partial text
            }
        }
        override fun onEvent(eventType: Int, params: Bundle?) {}
    })

    recognizer.startListening(intent)
}
