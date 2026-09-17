package com.parentpressure.app.subscription

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.parentpressure.app.network.SubscriptionStatusDto

@Composable
fun SubscriptionScreen(viewModel: SubscriptionViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadStatus() }

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
            val status = uiState.status
            Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                StatusCard(status)

                uiState.submitError?.let {
                    Text(it, color = Color.Red, modifier = Modifier.padding(top = 16.dp))
                }

                if (status?.subscriptionTier == "premium" && status.subscriptionActive) {
                    Button(
                        onClick = { viewModel.cancel() },
                        enabled = !uiState.isSubmitting,
                        modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                    ) {
                        if (uiState.isSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        } else {
                            Text("Cancel Subscription")
                        }
                    }
                } else {
                    Button(
                        onClick = { viewModel.upgrade("monthly") },
                        enabled = !uiState.isSubmitting,
                        modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                    ) {
                        Text("Upgrade - $9.99/month")
                    }

                    Button(
                        onClick = { viewModel.upgrade("yearly") },
                        enabled = !uiState.isSubmitting,
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    ) {
                        if (uiState.isSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        } else {
                            Text("Upgrade - $79.99/year")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusCard(status: SubscriptionStatusDto?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            val tierLabel = if (status?.subscriptionTier == "premium") "Premium" else "Free"
            Text(tierLabel, style = MaterialTheme.typography.headlineSmall)

            if (status?.subscriptionTier == "premium") {
                Text(
                    if (status.subscriptionActive) "Active" else "Inactive",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp),
                )
                status.subscriptionEndDate?.let {
                    Text(
                        "Renews/expires: ${it.take(10)}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            } else {
                Text(
                    "Upgrade for AI-customized workouts, meal plans, and advanced analytics.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}
