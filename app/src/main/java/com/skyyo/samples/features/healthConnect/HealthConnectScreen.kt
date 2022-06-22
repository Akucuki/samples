package com.skyyo.samples.features.healthConnect

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthDataRequestPermissions
import androidx.health.connect.client.permission.Permission
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsPadding
import com.skyyo.samples.utils.OnClick
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@Composable
fun HealthConnectScreen(viewModel: HealthConnectViewModel = hiltViewModel()) {
    val context = LocalContext.current
    var isHealthConnectAvailable by remember { mutableStateOf(false) }
    val permissions = viewModel.permissions
    val stepsWritten by viewModel.stepsWritten.collectAsState()
    val stepsRead by viewModel.stepsRead.collectAsState()
    val localStepsCanBeRead by viewModel.localStepsCanBeRead.collectAsState()
    val localLifecycle = LocalLifecycleOwner.current.lifecycle
    val events = remember(viewModel.events) {
        viewModel.events.receiveAsFlow().flowWithLifecycle(localLifecycle, Lifecycle.State.RESUMED)
    }

    val healthConnectPermissionState = rememberHealthConnectPermissionState(permissions, viewModel::checkPermissions)
    var areAllPermissionsGranted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        events.collect { healthConnectEvent ->
            when(healthConnectEvent) {
                is HealthConnectEvent.PermissionsStatus -> {
                    areAllPermissionsGranted = healthConnectEvent.areAllPermissionsGranted
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        callbackFlow {
            localLifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                send(HealthConnectClient.isAvailable(context))
            }
            close()
        }.collect { isHealthConnectAvailableNew ->
            isHealthConnectAvailable = isHealthConnectAvailableNew
            if (isHealthConnectAvailableNew) viewModel.checkPermissions()
        }
    }

    Box(modifier = Modifier
        .statusBarsPadding()
        .navigationBarsPadding()
        .padding(vertical = 20.dp)) {
        when {
            !isHealthConnectAvailable -> InstallHealthConnect()
            else -> {
                if (areAllPermissionsGranted) {
                    StepsTracker(
                        stepsWritten = stepsWritten,
                        stepsRead = stepsRead,
                        localStepsCanBeRead = localStepsCanBeRead,
                        onWriteClick = viewModel::writeSteps,
                        onReadClick = viewModel::readSteps,
                        onReadThirdPartyClick = viewModel::read3rdPartySteps
                    )
                } else {
                    PermissionRequired(
                        permissionState = healthConnectPermissionState,
                        permissionNotGrantedContent = {
                            Button(onClick = { healthConnectPermissionState.launchPermissionRequest() }) {
                                Text(text = "grant health connect permissions")
                            }
                        }
                    ) {
                        StepsTracker(
                            stepsWritten = stepsWritten,
                            stepsRead = stepsRead,
                            localStepsCanBeRead = localStepsCanBeRead,
                            onWriteClick = viewModel::writeSteps,
                            onReadClick = viewModel::readSteps,
                            onReadThirdPartyClick = viewModel::read3rdPartySteps
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InstallHealthConnect() {
    val context = LocalContext.current
    Button(onClick = {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.apps.healthdata")))
    }) {
        Text(text = "Install health connect app")
    }
}

@Composable
fun StepsTracker(
    stepsWritten: Long,
    localStepsCanBeRead: Boolean,
    stepsRead: Long?,
    onWriteClick: OnClick,
    onReadClick: OnClick,
    onReadThirdPartyClick: OnClick
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Steps to write: $stepsWritten")
            Button(modifier = Modifier.padding(start = 10.dp), onClick = onWriteClick) {
                Text(text = "Write")
            }
        }
        Row(modifier = Modifier.padding(top = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Read steps: $stepsRead")
            if (localStepsCanBeRead) {
                Button(modifier = Modifier.padding(start = 10.dp), onClick = onReadClick) {
                    Text(text = "Read local steps")
                }
            }
            Button(modifier = Modifier.padding(start = 10.dp), onClick = onReadThirdPartyClick) {
                Text(text = "Read 3rd party steps")
            }
        }
    }
}

@Stable
class HealthConnectPermissionState(
    private val requestedPermissions: Set<Permission>
) {

    val permission: String
        get() = "HealthConnectPermission"

    var hasPermission by mutableStateOf(false)

    fun launchPermissionRequest() {
        launcher?.launch(
            requestedPermissions
        ) ?: throw IllegalStateException("ActivityResultLauncher cannot be null")
    }

    internal var launcher: ActivityResultLauncher<Set<Permission>>? = null
}

@Composable
private fun rememberHealthConnectPermissionState(
    permissions: Set<Permission>,
    checkPermissions: suspend () -> Unit
): HealthConnectPermissionState {
    val mutablePermission: HealthConnectPermissionState = remember(permissions) {
        HealthConnectPermissionState(permissions)
    }
    val coroutineScope = rememberCoroutineScope()

    key(mutablePermission.permission) {
        //TODO it always return empty list
        val launcher = rememberLauncherForActivityResult(
            HealthDataRequestPermissions()
        ) {
            coroutineScope.launch { checkPermissions() }
        }

        DisposableEffect(launcher) {
            mutablePermission.launcher = launcher
            onDispose {
                mutablePermission.launcher = null
            }
        }
    }

    return mutablePermission
}

@Composable
fun PermissionRequired(
    permissionState: HealthConnectPermissionState,
    permissionNotGrantedContent: @Composable (() -> Unit),
    content: @Composable (() -> Unit),
) {
    when {
        permissionState.hasPermission -> {
            content()
        }
        else -> {
            permissionNotGrantedContent()
        }
    }
}