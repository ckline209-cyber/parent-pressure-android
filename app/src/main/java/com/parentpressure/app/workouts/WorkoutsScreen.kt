package com.parentpressure.app.workouts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.parentpressure.app.network.ExerciseDto
import com.parentpressure.app.network.WorkoutDto

@Composable
fun WorkoutsScreen(viewModel: WorkoutsViewModel, onLogExercises: (userWorkoutId: String) -> Unit) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadWorkouts() }

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
            Column(modifier = Modifier.fillMaxSize()) {
                uiState.startError?.let {
                    Text(
                        it,
                        color = Color.Red,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                ) {
                    items(uiState.workouts) { workout ->
                        val userWorkoutId = uiState.startedWorkouts[workout.id]
                        WorkoutCard(
                            workout = workout,
                            isStarting = uiState.startingWorkoutId == workout.id,
                            userWorkoutId = userWorkoutId,
                            onStart = { viewModel.startWorkout(workout.id) },
                            onLogExercises = { userWorkoutId?.let(onLogExercises) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutCard(
    workout: WorkoutDto,
    isStarting: Boolean,
    userWorkoutId: String?,
    onStart: () -> Unit,
    onLogExercises: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(workout.name, style = MaterialTheme.typography.titleLarge)
            workout.description?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
            }
            Text(
                "${workout.difficultyLevel ?: "any level"} · ${workout.frequencyPerWeek ?: "?"}x/week",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(top = 4.dp),
            )

            if (userWorkoutId != null) {
                Button(onClick = onLogExercises, modifier = Modifier.padding(top = 12.dp)) {
                    Text("Log Exercises")
                }
            } else {
                Button(
                    onClick = onStart,
                    enabled = !isStarting,
                    modifier = Modifier.padding(top = 12.dp),
                ) {
                    if (isStarting) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        Text("Start Workout")
                    }
                }
            }

            workout.exercises.forEach { exercise ->
                ExerciseRow(exercise)
            }
        }
    }
}

@Composable
private fun ExerciseRow(exercise: ExerciseDto) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
        Text(exercise.name, style = MaterialTheme.typography.bodyLarge)
        Text(
            "${exercise.targetSets ?: "?"} sets x ${exercise.targetReps ?: "?"} reps" +
                (exercise.equipmentNeeded?.let { " · $it" } ?: ""),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
