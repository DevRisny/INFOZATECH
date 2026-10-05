package com.infozatech.allinone.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.infozatech.allinone.weather.WeatherData
import com.infozatech.allinone.weather.WeatherService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

sealed interface WeatherUiState {
    data object Loading : WeatherUiState
    data class Success(val data: WeatherData) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}

private class CityNotFoundException : Exception()

class WeatherViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)

    private val _state = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val state: StateFlow<WeatherUiState> = _state.asStateFlow()

    private val _metric = MutableStateFlow(prefs.getBoolean(KEY_METRIC, true))
    val metric: StateFlow<Boolean> = _metric.asStateFlow()

    private val _recent = MutableStateFlow(loadRecent())
    val recent: StateFlow<List<String>> = _recent.asStateFlow()

    private var job: Job? = null
    private var lastQuery: String = prefs.getString(KEY_LAST_CITY, DEFAULT_CITY) ?: DEFAULT_CITY

    init {
        search(lastQuery)
    }

    fun retry() = search(lastQuery)

    fun setMetric(value: Boolean) {
        _metric.value = value
        prefs.edit().putBoolean(KEY_METRIC, value).apply()
    }

    fun search(rawCity: String) {
        val city = rawCity.trim()
        if (city.isEmpty()) {
            _state.value = WeatherUiState.Error("Please type a city name first.")
            return
        }
        lastQuery = city
        job?.cancel()
        _state.value = WeatherUiState.Loading
        job = viewModelScope.launch {
            _state.value = try {
                val place = WeatherService.geocoding.search(city).results?.firstOrNull()
                    ?: throw CityNotFoundException()
                val forecast = WeatherService.forecast.forecast(place.latitude, place.longitude)
                rememberCity(place.name)
                WeatherUiState.Success(
                    WeatherData(
                        city = place.name,
                        region = listOfNotNull(place.admin1, place.country).joinToString(", "),
                        forecast = forecast,
                    ),
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: CityNotFoundException) {
                WeatherUiState.Error("We couldn't find \"$city\". Check the spelling and try again.")
            } catch (e: IOException) {
                WeatherUiState.Error("No internet connection. Please check your network and try again.")
            } catch (e: HttpException) {
                WeatherUiState.Error("The weather service had a problem (code ${e.code()}). Please try again later.")
            } catch (e: Exception) {
                WeatherUiState.Error("Something went wrong while loading the weather. Please try again.")
            }
        }
    }

    // ---- small helpers for saving the last and recent cities -----------------------------

    private fun rememberCity(name: String) {
        val updated = (listOf(name) + _recent.value.filter { !it.equals(name, ignoreCase = true) })
            .take(MAX_RECENT)
        _recent.value = updated
        prefs.edit()
            .putString(KEY_LAST_CITY, name)
            .putString(KEY_RECENT, updated.joinToString(SEPARATOR))
            .apply()
        lastQuery = name
    }

    private fun loadRecent(): List<String> =
        prefs.getString(KEY_RECENT, "").orEmpty().split(SEPARATOR).filter { it.isNotBlank() }

    private companion object {
        const val DEFAULT_CITY = "Colombo"
        const val KEY_LAST_CITY = "last_city"
        const val KEY_RECENT = "recent_cities"
        const val KEY_METRIC = "metric"
        const val SEPARATOR = "|"
        const val MAX_RECENT = 5
    }
}
