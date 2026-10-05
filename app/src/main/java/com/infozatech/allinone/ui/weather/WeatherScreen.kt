package com.infozatech.allinone.ui.weather

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infozatech.allinone.ui.components.ScreenHeader
import com.infozatech.allinone.viewmodel.WeatherUiState
import com.infozatech.allinone.viewmodel.WeatherViewModel
import com.infozatech.allinone.weather.WeatherCodes
import com.infozatech.allinone.weather.WeatherData
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(vm: WeatherViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val metric by vm.metric.collectAsStateWithLifecycle()
    val recent by vm.recent.collectAsStateWithLifecycle()

    var query by remember { mutableStateOf("") }
    val focus = LocalFocusManager.current

    fun submit(city: String) {
        focus.clearFocus()
        vm.search(city)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        ScreenHeader(title = "Weather", subtitle = "Live data from Open-Meteo")

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search a city, e.g. Kandy") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(28.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { submit(query) }),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        )

        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilterChip(selected = metric, onClick = { vm.setMetric(true) }, label = { Text("°C · km/h") })
            FilterChip(selected = !metric, onClick = { vm.setMetric(false) }, label = { Text("°F · mph") })
        }

        if (recent.isNotEmpty()) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                recent.take(3).forEach { city ->
                    FilterChip(
                        selected = false,
                        onClick = {
                            query = city
                            submit(city)
                        },
                        label = { Text(city) },
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        when (val s = state) {
            is WeatherUiState.Loading -> LoadingView()
            is WeatherUiState.Error -> ErrorView(message = s.message, onRetry = vm::retry)
            is WeatherUiState.Success -> WeatherContent(data = s.data, metric = metric)
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun LoadingView() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Text(
                text = "Loading weather…",
                modifier = Modifier.padding(top = 16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ErrorView(message: String, onRetry: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                Icons.Default.CloudOff,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onErrorContainer,
            )
            Text(
                text = message,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(vertical = 12.dp),
            )
            Button(onClick = onRetry) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Text("  Try again")
            }
        }
    }
}

@Composable
private fun WeatherContent(data: WeatherData, metric: Boolean) {
    val current = data.forecast.current
    val isDay = current.isDay == 1
    val gradient = weatherGradient(current.weatherCode, isDay)

    // Main card
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(brush = Brush.verticalGradient(gradient), shape = RoundedCornerShape(28.dp))
            .padding(24.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = data.city,
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
            )
            if (data.region.isNotBlank()) {
                Text(text = data.region, color = Color.White.copy(alpha = 0.85f))
            }
            Text(
                text = WeatherCodes.emoji(current.weatherCode, isDay),
                fontSize = 72.sp,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                text = formatTemp(current.temperature, metric),
                color = Color.White,
                fontSize = 64.sp,
                fontWeight = FontWeight.Light,
            )
            Text(
                text = WeatherCodes.describe(current.weatherCode),
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }

    Spacer(Modifier.height(16.dp))

    // Stat tiles: temperature feel, humidity, wind, pressure
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(
                icon = Icons.Default.WaterDrop,
                label = "Humidity",
                value = "${current.humidity}%",
                modifier = Modifier.weight(1f),
            )
            StatTile(
                icon = Icons.Default.Air,
                label = "Wind",
                value = formatWind(current.windSpeedKmh, metric),
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(
                icon = Icons.Default.Thermostat,
                label = "Feels like",
                value = formatTemp(current.feelsLike, metric),
                modifier = Modifier.weight(1f),
            )
            StatTile(
                icon = Icons.Default.Speed,
                label = "Pressure",
                value = "${current.pressure.roundToInt()} hPa",
                modifier = Modifier.weight(1f),
            )
        }
    }

    Spacer(Modifier.height(20.dp))

    // 5-day forecast (index 0 is today, so we start from 1)
    val daily = data.forecast.daily
    val days = (1 until daily.time.size).take(5)
    if (days.isNotEmpty()) {
        Text(
            text = "Next ${days.size} days",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
        )
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                days.forEach { i ->
                    val date = runCatching { LocalDate.parse(daily.time[i]) }.getOrNull()
                    val name = date?.dayOfWeek?.getDisplayName(TextStyle.SHORT, Locale.getDefault()) ?: "—"
                    val rain = daily.rainChance.getOrNull(i)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = name,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = WeatherCodes.emoji(daily.weatherCode[i]),
                            fontSize = 24.sp,
                            modifier = Modifier.padding(end = 8.dp),
                        )
                        Text(
                            text = if (rain != null) "💧$rain%" else "",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = "${formatTemp(daily.maxTemp[i], metric)}  ${formatTemp(daily.minTemp[i], metric)}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1.4f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatTile(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun weatherGradient(code: Int, isDay: Boolean): List<Color> = when {
    !isDay -> listOf(Color(0xFF1B2440), Color(0xFF3B4A7A))
    code in listOf(95, 96, 99) -> listOf(Color(0xFF3A3F5C), Color(0xFF6B6F8F))
    code in listOf(51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82) ->
        listOf(Color(0xFF4A6FA5), Color(0xFF7FA0C9))
    code in listOf(71, 73, 75, 77, 85, 86) -> listOf(Color(0xFF7FA6C9), Color(0xFFB9D3E8))
    code == 3 || code == 45 || code == 48 -> listOf(Color(0xFF6B7A90), Color(0xFF9AA8BA))
    else -> listOf(Color(0xFF0B57F5), Color(0xFF37B6F6))
}

private fun toFahrenheit(celsius: Double): Double = celsius * 9.0 / 5.0 + 32.0

private fun formatTemp(celsius: Double, metric: Boolean): String {
    val value = if (metric) celsius else toFahrenheit(celsius)
    return "${value.roundToInt()}°"
}

private fun formatWind(kmh: Double, metric: Boolean): String =
    if (metric) "${kmh.roundToInt()} km/h" else "${(kmh * 0.621371).roundToInt()} mph"
