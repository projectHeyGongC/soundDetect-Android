package com.example.heygongcsounddetect

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.heygongcsounddetect.record.SoundRecorder
import com.example.heygongcsounddetect.ui.theme.HeygongcSoundDetectTheme
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.map

class MainActivity : ComponentActivity() {
    private lateinit var recorder: SoundRecorder
    private var soundDetectModeJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        recorder = SoundRecorder(this, CoroutineScope(Dispatchers.IO))

        setContent {
            MaterialTheme {
                // LocalContext is used to provide a Context object to the RecordingScreen
                val context = LocalContext.current

                // Invoke the RecordingScreen composable function
                RecordingScreen(context = context, recorder = recorder)
            }

        }
    }


    @Composable
    fun RecordingScreen(context: Context, recorder: SoundRecorder) {
        val coroutineScope = rememberCoroutineScope()
        var isDetectingSound by remember { mutableStateOf(false) }
        // Collect the sound data as state for displaying
        val soundData by recorder.soundData.collectAsState(initial = emptyList())

        // Decide the background color based on sound data threshold
        val backgroundColor = if (soundData.any { it > 400 }) Color.Blue else MaterialTheme.colorScheme.background

        Column(modifier = Modifier
            .fillMaxSize()
            .background(color = backgroundColor)) {

            // Display the sound data as text
            Text(
                text = "Sound Data: ${soundData.joinToString(", ")}",
                modifier = Modifier.padding(16.dp)
            )
            Box(modifier = Modifier.fillMaxSize()) {
                RecordButton(
                    isRecording = isDetectingSound,
                    onStartDetecting = {
                        // Toggle the detection mode on
                        isDetectingSound = true
                        // Launch the repeating sound detection mode
                        soundDetectModeJob = coroutineScope.launch {
                            startSoundDetectMode()
                        }
                    },
                    onStopDetecting = {
                        // Toggle the detection mode off
                        isDetectingSound = false
                        // Cancel the coroutine job to stop the cycle
                        soundDetectModeJob?.cancel()
                    },
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }

    private suspend fun startSoundDetectMode() = withContext(Dispatchers.IO) {
        while (isActive) { // Continues until the job is cancelled
            try {
                recorder.startRecording()
                delay(5000) // Record for 5 seconds
                recorder.stopRecording()
                delay(1000) // Pause for 1 second
            } catch (e: Exception) {
                Log.e("SoundDetectMode", "Error in sound detection mode", e)
            }
        }
    }

    @Composable
    fun RecordButton(
        isRecording: Boolean,
        onStartDetecting: () -> Unit,
        onStopDetecting: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        val backgroundColor by animateColorAsState(if (isRecording) Color.Green else Color.Gray)
        val icon = if (isRecording) Icons.Default.Check else Icons.Default.PlayArrow

        IconButton(
            onClick = {
                if (isRecording) {
                    onStopDetecting()
                } else {
                    onStartDetecting()
                }
            },
            modifier = modifier
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = backgroundColor
            ) {
                Icon(
                    icon,
                    contentDescription = if (isRecording) "Stop Recording" else "Start Recording",
                    tint = Color.White
                )
            }
        }
    }

}

