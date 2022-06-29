package com.skyyo.samples.features.nativeGoogleMap

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import com.google.maps.android.data.kml.KmlLayer
import com.skyyo.samples.theme.mapStyle
import com.skyyo.samples.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(MapsComposeExperimentalApi::class)
@Composable
fun NativeGoogleMapScreen(viewModel: NativeGoogleMapViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val points by viewModel.points.collectAsState()
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(viewModel.dolynaLatLng, 10f)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        properties = remember(mapStyle) {
            MapProperties(
                mapStyleOptions = mapStyle?.let { style ->
                    MapStyleOptions.loadRawResourceStyle(context, style)
                }
            )
        },
        cameraPositionState = cameraPositionState
    ) {
        MapEffect(Unit) { map ->
            val layer = withContext(Dispatchers.Default) {
                kotlin.runCatching { KmlLayer(map, R.raw.map_layer, context) }.getOrNull()
            }
            layer?.addLayerToMap()
        }
        points?.forEachIndexed { index, position ->
            Marker(
                state = MarkerState(position = position),
                title = "Test marker $index"
            )
        }
        Polyline(
            points = listOf(viewModel.dolynaLatLng, viewModel.lvivLatLng, viewModel.kiyvLatLng),
            color = Color.Red
        )
    }
}