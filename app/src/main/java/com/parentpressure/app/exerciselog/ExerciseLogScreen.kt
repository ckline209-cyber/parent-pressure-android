package com.parentpressure.app.exerciselog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.parentpressure.app.network.ExerciseDto
import com.parentpressure.app.network.ExerciseLogDto
import com.parentpressure.app.network.ProgressionSuggestionDto

@Composable
fun ExerciseLogScreen(userWorkoutId: String, viewModel: ExerciseLogViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(userWorkoutId) { viewModel.load(userWorkoutId) }

    when {
        uiState.isLoading -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator()
            }
        }

        uiState.errorMessage != null -> {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(uiState.errorMessage.orEmpty(), color = Color.Red)
            }
        }

        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
            ) {
                item {
                    Text(uiState.workoutName, style = MaterialTheme.typography.headlineSmall)
                    if (uiState.isCompleted) {
                        Text(
                            "Completed",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
                        )
                    }
                    uiState.logError?.let {
                        Text(it, color = Color.Red, modifier = Modifier.padding(top = 8.dp, bottom = 8.dp))
                    }
                }

                items(uiState.exercises) { exercise ->
                    ExerciseLogCard(
                        exercise = exercise,
                        logs = uiState.logsByExercise[exercise.id] ?: emptyList(),
                        progression = uiState.progressionByExercise[exercise.id],
                        isLogging = uiState.loggingExerciseId == exercise.id,
                        isCompleted = uiState.isCompleted,
                        onLogSet = { repsPerSet, weightKg, rpe ->
                            viewModel.logSet(exercise.id, repsPerSet, weightKg, rpe)
                        },
                    )
                }

                if (!uiState.isCompleted) {
                    item {
                        Button(
                            onClick = { viewModel.completeWorkout() },
                            enabled = !uiState.isCompleting,
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        ) {
                            if (uiState.isCompleting) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            } else {
                                Text("Complete Workout")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseLogCard(
    exercise: ExerciseDto,
    logs: List<ExerciseLogDto>,
    progression: ProgressionSuggestionDto?,
    isLogging: Boolean,
    isCompleted: Boolean,
    onLogSet: (repsPerSet: List<Int>, weightKg: Double?, rpe: Int?) -> Unit,
) {
    var reps by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var rpe by remember { mutableStateOf("") }

    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(exercise.name, style = MaterialTheme.typography.titleMedium)
            Text(
                "Target: ${exercise.targetSets ?: "?"} sets x ${exercise.targetReps ?: "?"} reps",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 2.dp),
            )

            progression?.let {
                val suggestion = when {
                    it.suggestedWeightKg != null -> "Suggested: ${it.suggestedWeightKg}kg x ${it.suggestedReps}"
                    it.suggestedReps != null -> "Suggested: ${it.suggestedReps} reps"
                    else -> null
                }
                Text(
                    listOfNotNull(suggestion, it.rationale).joinToString(" — "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }

            logs.forEach { log ->
                Text(
                    "Logged: ${log.repsPerSet?.joinToString(", ") ?: "?"} reps" +
                        (log.weightUsedKg?.let { " @ ${it}kg" } ?: "") +
                        (log.rpe?.let { " (RPE $it)" } ?: ""),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }

            if (!isCompleted) {
                Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                    OutlinedTextField(
                        value = reps,
                        onValueChange = { reps = it },
                        label = { Text("Reps (e.g. 8,8,7)") },
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        label = { Text("Weight (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = rpe,
                        onValueChange = { rpe = it },
                        label = { Text("RPE") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).padding(start = 8.dp),
                    )
                }

                val repsPerSet = reps.split(",").mapNotNull { it.trim().toIntOrNull() }
                Button(
                    onClick = {
                        onLogSet(repsPerSet, weight.toDoubleOrNull(), rpe.toIntOrNull())
                        reps = ""
                        weight = ""
                        rpe = ""
                    },
                    enabled = !isLogging && repsPerSet.isNotEmpty(),
                    modifier = Modifier.padding(top = 8.dp),
                ) {
                    if (isLogging) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        Text("Log Set")
                    }
                }
            }
        }
    }
}
