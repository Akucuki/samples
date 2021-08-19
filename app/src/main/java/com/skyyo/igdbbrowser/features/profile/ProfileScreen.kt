package com.skyyo.igdbbrowser.features.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.insets.systemBarsPadding

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    Column {
        Text(text = "Profile", modifier = Modifier.systemBarsPadding())
        Button(onClick = viewModel::onBtnClick) {
            Text("go edit profile")
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = viewModel::onSignOut) {
            Text("sign out")
        }
    }
}