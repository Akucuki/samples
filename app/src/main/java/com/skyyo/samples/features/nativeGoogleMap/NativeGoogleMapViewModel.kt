package com.skyyo.samples.features.nativeGoogleMap

import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class NativeGoogleMapViewModel @Inject constructor() : ViewModel() {

    val points = MutableStateFlow<List<LatLng>?>(null)
    val dolynaLatLng = LatLng(48.980833, 24.0025)
    val lvivLatLng = LatLng(49.8357, 24.0322)
    val kiyvLatLng = LatLng(50.4432, 30.5235)

    init {
        generateRandomPositions()
    }

    private fun generateRandomPositions() {
        val positionsList = mutableListOf<LatLng>()
        repeat(100) {
            positionsList.add(
                LatLng(
                    it * 0.1,
                    it * 0.1
                )
            )
        }
        points.value = positionsList
    }

}