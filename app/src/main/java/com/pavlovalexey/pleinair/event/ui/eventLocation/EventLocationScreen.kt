package com.pavlovalexey.pleinair.event.ui.eventLocation

import androidx.compose.runtime.*
import com.google.maps.android.compose.*
import com.google.android.gms.maps.model.LatLng
import android.location.Geocoder
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.BottomAppBar
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.pavlovalexey.pleinair.R
import com.pavlovalexey.pleinair.profile.ui.myLocation.MyLocationViewModel
import com.pavlovalexey.pleinair.utils.uiComponents.CustomButtonOne
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

@Composable
fun EventLocationScreen(
    navController: NavController,
    viewModel: MyLocationViewModel = hiltViewModel(),
    city: String,
    onLocationSelected: (Double, Double) -> Unit
) {
    val context = LocalContext.current
    var mapProperties by remember { mutableStateOf(MapProperties()) }
    val cameraPositionState = rememberCameraPositionState()
    var markerPosition by remember { mutableStateOf<LatLng?>(null) }

    LaunchedEffect(city) {
        withContext(Dispatchers.IO) {
            val geocoder = Geocoder(context)
            try {
                val addresses = geocoder.getFromLocationName(city, 1)
                if (addresses != null && addresses.isNotEmpty()) {
                    val address = addresses[0]
                    val latLng = LatLng(address.latitude, address.longitude)
                    withContext(Dispatchers.Main) {
                        cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(latLng, 12f))
                    }
                } else {// Если геокодирование не удалось, установка начальной позиции в родном городе
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Город не найден, используем координаты Москвы.", Toast.LENGTH_SHORT).show()
                        cameraPositionState.move(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    55.75,
                                    37.61
                                ), 12f
                            )
                        )
                    }
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Ошибка геокодирования: $e", Toast.LENGTH_SHORT).show()
                    cameraPositionState.move(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(55.75, 37.61),
                            12f
                        )
                    )
                }
            } catch (e: IllegalArgumentException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Ошибка ввода города: $e", Toast.LENGTH_SHORT).show()
                    cameraPositionState.move(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(55.75, 37.61),
                            12f
                        )
                    )
                }
            }
        }
    }

    Scaffold(
        backgroundColor = Color.White,
        bottomBar = {
            BottomAppBar(
                backgroundColor = Color.White,
                modifier = Modifier.height(100.dp)
            ) {
                CustomButtonOne(
                    text = stringResource(R.string.geo_mark),
                    iconResId = R.drawable.palette_30dp,
                    textColor = MaterialTheme.colors.primary,
                    iconColor = MaterialTheme.colors.primary,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        markerPosition?.let {
                            onLocationSelected(it.latitude, it.longitude)
                        } ?: run {
                            Toast.makeText(context, "Выберите местоположение", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        GoogleMap(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            cameraPositionState = cameraPositionState,
            onMapClick = { latLng ->
                markerPosition = latLng
            },
            properties = mapProperties
        ) {
            markerPosition?.let {
                Marker(state = MarkerState(position = it))
            }
        }
    }
}
