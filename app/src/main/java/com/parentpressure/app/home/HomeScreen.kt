package com.parentpressure.app.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.parentpressure.app.network.UserDto

@Composable
fun HomeScreen(user: UserDto, onLogout: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Welcome, ${user.firstName ?: user.email}!", style = MaterialTheme.typography.headlineMedium)
        Text("Subscription: ${user.subscriptionTier}", modifier = Modifier.padding(top = 8.dp))

        Button(onClick = onLogout, modifier = Modifier.padding(top = 24.dp)) {
            Text("Log Out")
        }
    }
}
