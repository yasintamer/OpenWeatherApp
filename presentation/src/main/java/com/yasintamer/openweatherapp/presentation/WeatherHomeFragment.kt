package com.yasintamer.openweatherapp.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import coil.compose.AsyncImage
import com.yasintamer.openweatherapp.api.entity.CurrentWeather
import com.yasintamer.openweatherapp.api.entity.WeatherLocation
import com.yasintamer.openweatherapp.theme.OpenWeatherAppTheme
import com.yasintamer.openweatherapp.presentation.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WeatherHomeFragment : Fragment() {

    private val weatherHomeViewModel: WeatherHomeViewModel by viewModels()

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                // Precise location access granted.
                onPermissionGranted()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // Only approximate location access granted.
                onPermissionGranted()
            } else -> {
                // No location access granted.
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setContent {
            OpenWeatherAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background,
                ) {
                    val isLookupVisible by weatherHomeViewModel.isLookupVisible.observeAsState(true)
                    val currentLocation by weatherHomeViewModel.lastLocation.collectAsState(null)
                    if (isLookupVisible) {
                        LocationsLookupPage()
                    } else {
                        CurrentWeatherPage(currentLocation)
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkForLocationPermission()
    }

    private fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkForLocationPermission() {
        if (isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION) ||
            isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            onPermissionGranted()
        } else {
            locationPermissionRequest.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    private fun onPermissionGranted() {
        val lm = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (LocationManagerCompat.isLocationEnabled(lm)) {
            getGpsLocation()?.let { weatherHomeViewModel.findAndSaveCurrentLocation(it) }
        } else {
            // prompt user to enable location or launch location settings check
        }
    }

    @SuppressLint("MissingPermission")
    private fun getGpsLocation() = (requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager).run {
        getLastKnownLocation(LocationManager.GPS_PROVIDER)
    }

    @Composable
    fun CurrentWeatherPage(currentLocation: WeatherLocation?) {
        val viewState by weatherHomeViewModel.currentWeatherViewState.observeAsState()
        when (viewState) {
            CurrentWeatherViewState.Loading -> {
                Loading()
            }
            is CurrentWeatherViewState.Success -> {
                CurrentWeatherSuccessPage(currentLocation, (viewState as CurrentWeatherViewState.Success).data)
            }
            is CurrentWeatherViewState.Error -> {
                Text((viewState as CurrentWeatherViewState.Error).message ?: getString(R.string.generic_error_message))
            }
            else -> {}
        }
    }

    @Composable
    private fun CurrentWeatherSuccessPage(currentLocation: WeatherLocation?, data: CurrentWeather) {
        Column {
            TopBarView(currentLocation)
            Card(
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    AsyncImage(
                        model = "https://openweathermap.org/img/wn/${data.weatherData?.icon}@2x.png",
                        placeholder = painterResource(id = R.drawable.image_placeholder),
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .height(50.dp)
                            .width(50.dp)
                            .clip(shape = RoundedCornerShape(percent = 50)),
                    )
                    Text(text = (data.mainData?.temp?.kTof() ?: "--"))
                    Text(text = (data.weatherData?.main ?: "--") + "/" + (data.weatherData?.description ?: "--"))
                }
            }
            Card(
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    data.mainData?.let { mainData ->
                        LabelValueRow(label = "Temp", value = mainData.temp.kTof())
                        LabelValueRow(label = "Feels Like", value = mainData.feelsLike.kTof())
                        LabelValueRow(label = "Temp Min", value = mainData.tempMin.kTof())
                        LabelValueRow(label = "Temp Max", value = mainData.tempMax.kTof())
                    }
                }
            }
        }
    }

    @Composable
    private fun Loading() {
        val strokeWidth = 5.dp

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(52.dp),
                strokeWidth = strokeWidth,
            )
        }
    }

    @Composable
    private fun TopBarView(currentLocation: WeatherLocation?) {
        Card(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth()
                .clickable {
                    weatherHomeViewModel.openLookupPage()
                },
        ) {
            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = getLocationTitle(currentLocation),
                    textAlign = TextAlign.Center,
                )
                Icon(painter = painterResource(id = R.drawable.expand_more), null)
            }
        }
    }

    private fun getLocationTitle(currentLocation: WeatherLocation?): String {
        val title = buildString {
            currentLocation?.name?.let { append(it) }
            currentLocation?.state?.let {
                append(",")
                append(it)
            }
        }
        return if (title.isEmpty()) {
            "Click to Set a Location"
        } else {
            title
        }
    }

    @Composable
    private fun LabelValueRow(label: String, value: String) {
        Row(modifier = Modifier.height(24.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = label, textAlign = TextAlign.Start, maxLines = 1)
            Text(text = value, modifier = Modifier.weight(1F), textAlign = TextAlign.End, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }

    @Composable
    fun LocationsLookupPage() {
        Column(modifier = Modifier.padding(20.dp)) {
            val textState = remember { mutableStateOf(TextFieldValue()) }
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = textState.value,
                onValueChange = {
                    onLookupTextChanged(textState, it)
                },
                placeholder = { Text("find location") },
            )
            LocationsResultView()
        }
    }

    private fun onLookupTextChanged(
        textState: MutableState<TextFieldValue>,
        it: TextFieldValue,
    ) {
        textState.value = it
        weatherHomeViewModel.findLocations(it.text)
    }

    @Composable
    fun LocationsResultView() {
        val viewState by weatherHomeViewModel.locationsViewState.observeAsState()
        when (viewState) {
            LocationsViewState.Loading -> {
                Loading()
            }
            is LocationsViewState.Success -> LocationsSuccessView((viewState as LocationsViewState.Success).data)
            is LocationsViewState.Error -> Text((viewState as LocationsViewState.Error).message ?: getString(R.string.generic_error_message))
            else -> {}
        }
    }

    @Composable
    fun LocationsSuccessView(data: List<WeatherLocation>) {
        val lazyColumnState: LazyListState = rememberLazyListState()

        LazyColumn(state = lazyColumnState) {
            items(
                count = data.size,
                key = {
                    "${data[it].lat}-${data[it].lon}"
                },
                itemContent = { index ->
                    Text(
                        modifier = Modifier
                            .padding(vertical = 10.dp)
                            .clickable {
                                weatherHomeViewModel.setLastLocation(data[index])
                            },
                        text = AnnotatedString("${data[index].name}, ${data[index].state}"),
                    )
                    Divider(color = MaterialTheme.colors.secondary, thickness = 1.dp)
                },
            )
        }
    }
}
