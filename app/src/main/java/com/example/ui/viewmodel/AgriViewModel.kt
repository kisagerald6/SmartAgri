package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.os.Vibrator
import android.os.VibrationEffect
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AgriDatabase
import com.example.data.model.ActiveBanner
import com.example.data.model.AgriAlert
import com.example.data.model.AgriHistoryPoint
import com.example.data.model.AgriStatus
import com.example.data.model.AlertLevel
import com.example.data.model.CropGrowthStage
import com.example.data.model.PestDiseaseDiagnosis
import com.example.data.model.SampleCropImage
import com.example.data.model.SoilType
import com.example.data.model.YieldForecastInput
import com.example.data.model.YieldForecastResult
import com.example.data.model.ZoneEnvironmentData
import com.example.data.repository.AgriRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface PestDetectionUiState {
  data object Idle : PestDetectionUiState
  data object Analyzing : PestDetectionUiState
  data class Success(val diagnosis: PestDiseaseDiagnosis) : PestDetectionUiState
  data class Error(val message: String) : PestDetectionUiState
}

data class SmartAgriUiState(
  val status: AgriStatus = AgriStatus(),
  val isConnected: Boolean = true,
  val isDemo: Boolean = true,
  val siteName: String = "SmartAgri Farm",
  val host: String = "192.168.1.50",
  val selectedZoneId: String = "all",
  val zones: List<ZoneEnvironmentData> = emptyList(),
  val activeZone: ZoneEnvironmentData? = null,
  val banner: ActiveBanner = ActiveBanner(AlertLevel.INFO, "bi-check-circle", "All systems normal", "SmartAgri telemetry live."),
  val activeTrendKey: String = "soil", // "soil", "food", "tank", "flow"
  val activeRange: String = "5m", // "5m", "1h", "24h"
  val selectedDetailSheet: String? = null,
  val runDurationSec: Int = 60,
  val themeMode: String = "auto", // "auto", "light", "dark"
  val soundVibrationEnabled: Boolean = true
)

class AgriViewModel(application: Application) : AndroidViewModel(application) {

  private val repository: AgriRepository
  private val vibrator = application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

  private val _uiState = MutableStateFlow(SmartAgriUiState())
  val uiState: StateFlow<SmartAgriUiState> = _uiState.asStateFlow()

  private val _pestState = MutableStateFlow<PestDetectionUiState>(PestDetectionUiState.Idle)
  val pestState: StateFlow<PestDetectionUiState> = _pestState.asStateFlow()

  private val _yieldInput = MutableStateFlow(YieldForecastInput())
  val yieldInput: StateFlow<YieldForecastInput> = _yieldInput.asStateFlow()

  private val _yieldResult = MutableStateFlow<YieldForecastResult?>(null)
  val yieldResult: StateFlow<YieldForecastResult?> = _yieldResult.asStateFlow()

  val pastPestScans: StateFlow<List<PestDiseaseDiagnosis>>
  val alerts: StateFlow<List<AgriAlert>>
  val history5m: StateFlow<List<AgriHistoryPoint>>
  val history1h: StateFlow<List<AgriHistoryPoint>>
  val history24h: StateFlow<List<AgriHistoryPoint>>

  val sampleCrops: List<SampleCropImage>
    get() = repository.sampleCropLibrary

  init {
    val database = AgriDatabase.getDatabase(application)
    repository = AgriRepository(application, database.agriDao())

    pastPestScans = repository.pastPestScans
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    alerts = repository.alertsFlow
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    history5m = repository.history5m
    history1h = repository.history1h
    history24h = repository.history24h

    // Calculate initial yield forecast
    _yieldResult.value = repository.generateYieldForecast(_yieldInput.value)

    // Observe Repository Telemetry
    viewModelScope.launch {
      combine(
        repository.status,
        repository.isConnected,
        repository.isDemo,
        repository.selectedZoneId,
        repository.zonesData
      ) { status, connected, demo, zoneId, zones ->
        val site = repository.siteName.value
        val host = repository.host.value
        val activeZone = if (zoneId == "all") {
          // Whole-farm aggregate
          if (zones.isNotEmpty()) {
            ZoneEnvironmentData(
              zoneId = "all",
              name = "All Farm Greenhouses & Fields",
              cropType = "Multi-Crop Commercial Aggregate",
              areaSqMeters = zones.sumOf { it.areaSqMeters },
              temperatureC = zones.map { it.temperatureC }.average(),
              humidityPct = zones.map { it.humidityPct }.average(),
              lightLux = zones.map { it.lightLux }.average(),
              lightDliMol = zones.map { it.lightDliMol }.average(),
              soilMoisturePct = zones.map { it.soilMoisturePct }.average(),
              nutrientsPpm = zones.map { it.nutrientsPpm }.average(),
              tankLevelPct = zones.map { it.tankLevelPct }.average(),
              flowRateLMin = zones.sumOf { it.flowRateLMin },
              isIrrigating = zones.any { it.isIrrigating }
            )
          } else null
        } else {
          zones.find { it.zoneId == zoneId }
        }

        val banner = computeStatusBanner(status, connected, demo)

        _uiState.value.copy(
          status = status,
          isConnected = connected,
          isDemo = demo,
          siteName = site,
          host = host,
          selectedZoneId = zoneId,
          zones = zones,
          activeZone = activeZone,
          banner = banner
        )
      }.collect { updated ->
        _uiState.value = updated
      }
    }

    // Listen for alert notification triggers to trigger haptic feedback
    viewModelScope.launch {
      repository.alertNotificationEvent.collect { alert ->
        if (_uiState.value.soundVibrationEnabled && alert.level != AlertLevel.INFO) {
          triggerHapticAlert()
        }
      }
    }
  }

  private fun computeStatusBanner(s: AgriStatus, isConnected: Boolean, isDemo: Boolean): ActiveBanner {
    if (!isConnected && !isDemo) {
      return ActiveBanner(
        level = AlertLevel.CRITICAL,
        iconName = "bi-wifi-off",
        title = "Controller offline",
        description = "Cannot reach controller at ${_uiState.value.host}. Retrying automatically.",
        primaryAction = "go:connect",
        primaryActionLabel = "Change connection"
      )
    }

    if (s.fault == "NO FLOW") {
      return ActiveBanner(
        level = AlertLevel.CRITICAL,
        iconName = "bi-exclamation-octagon",
        title = "No flow detected. Pump stopped",
        description = "Check the water tank, pipe lines, and flow sensor, then clear the fault.",
        primaryAction = "clear",
        primaryActionLabel = "Clear fault"
      )
    }

    if (s.tank < s.cfg.minTank) {
      return ActiveBanner(
        level = AlertLevel.CRITICAL,
        iconName = "bi-exclamation-octagon",
        title = "Tank level critical (${s.tank.toInt()}%)",
        description = "Tank is below safe cut-off (${s.cfg.minTank.toInt()}%). Refill before irrigating.",
        primaryAction = "go:irrigation",
        primaryActionLabel = "Tank settings"
      )
    }

    if (s.pump) {
      val timeLeft = if (s.waterLeft > 0) "${s.waterLeft}s remaining" else "Running for ${s.runSec}s"
      return ActiveBanner(
        level = AlertLevel.INFO,
        iconName = "bi-droplet-fill",
        title = "Irrigation running ($timeLeft)",
        description = "Watering crops. Automatically stops at ${s.cfg.offAbove.toInt()}% soil moisture.",
        primaryAction = "stop",
        primaryActionLabel = "Stop irrigation"
      )
    }

    val n = s.nutr
    if (n != null && n.en && n.low) {
      return ActiveBanner(
        level = AlertLevel.WARNING,
        iconName = "bi-exclamation-triangle",
        title = "Nutrient level low (${n.ppm} ppm)",
        description = "Below ${s.cfg.nutrLowPpm.toInt()} ppm alert limit. Apply balanced NPK fertiliser.",
        primaryAction = "go:nutrients",
        primaryActionLabel = "View details"
      )
    }

    if (s.soil < s.cfg.onBelow) {
      return ActiveBanner(
        level = AlertLevel.WARNING,
        iconName = "bi-exclamation-triangle",
        title = "Soil moisture below range (${s.soil.toInt()}%)",
        description = if (s.mode == "auto") {
          if (s.hold > 0) "Auto mode on hold (${s.hold / 60}m). Start manual or resume."
          else "Below ${s.cfg.onBelow.toInt()}% trigger. Starting automatically."
        } else "Below ${s.cfg.onBelow.toInt()}% trigger. Automatic irrigation is OFF.",
        primaryAction = "run",
        primaryActionLabel = "Start irrigation"
      )
    }

    return ActiveBanner(
      level = AlertLevel.INFO,
      iconName = "bi-check-circle",
      title = "All systems normal",
      description = "Soil ${s.soil.toInt()}%, Tank ${s.tank.toInt()}%${if (n?.en == true) ", Nutrients ${n.ppm} ppm" else ""}. Auto is ${if (s.mode == "auto") "ON" else "OFF"}."
    )
  }

  private fun triggerHapticAlert() {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 180, 90, 180), -1))
      } else {
        @Suppress("DEPRECATION")
        vibrator?.vibrate(longArrayOf(0, 180, 90, 180), -1)
      }
    } catch (_: Exception) {}
  }

  // User Actions
  fun setTrendKey(key: String) {
    _uiState.value = _uiState.value.copy(activeTrendKey = key)
  }

  fun setRange(range: String) {
    _uiState.value = _uiState.value.copy(activeRange = range)
  }

  fun setRunDurationSec(seconds: Int) {
    _uiState.value = _uiState.value.copy(runDurationSec = seconds)
  }

  fun openDetailSheet(kind: String) {
    _uiState.value = _uiState.value.copy(selectedDetailSheet = kind)
  }

  fun closeDetailSheet() {
    _uiState.value = _uiState.value.copy(selectedDetailSheet = null)
  }

  fun selectZone(zoneId: String) {
    repository.selectZone(zoneId)
  }

  fun setSiteName(name: String) {
    repository.setSiteName(name)
  }

  fun setThemeMode(mode: String) {
    _uiState.value = _uiState.value.copy(themeMode = mode)
  }

  fun setSoundVibration(enabled: Boolean) {
    _uiState.value = _uiState.value.copy(soundVibrationEnabled = enabled)
  }

  fun connectToHost(ipAddress: String, isDemo: Boolean) {
    repository.connectToHost(ipAddress, isDemo)
  }

  fun triggerDemoScenario(scenario: String) {
    repository.triggerDemoScenario(scenario)
  }

  fun startTimedIrrigation(seconds: Int = _uiState.value.runDurationSec) {
    viewModelScope.launch {
      repository.startTimedIrrigation(seconds)
    }
  }

  fun stopIrrigation() {
    viewModelScope.launch {
      repository.stopIrrigation()
    }
  }

  fun resumeAuto() {
    viewModelScope.launch {
      repository.resumeAuto()
    }
  }

  fun setAutoMode(enabled: Boolean) {
    viewModelScope.launch {
      repository.setAutoMode(enabled)
    }
  }

  fun setNutrientMonitoring(enabled: Boolean) {
    viewModelScope.launch {
      repository.setNutrientMonitoring(enabled)
    }
  }

  fun updateConfig(key: String, value: Double) {
    viewModelScope.launch {
      repository.updateConfigKey(key, value)
    }
  }

  fun clearFault() {
    viewModelScope.launch {
      repository.clearFault()
    }
  }

  fun resetFlow() {
    viewModelScope.launch {
      repository.resetFlowTotal()
    }
  }

  fun calibrate(point: String) {
    viewModelScope.launch {
      repository.calibrateSoil(point)
    }
  }

  fun markAllAlertsRead() {
    viewModelScope.launch {
      repository.markAllAlertsRead()
    }
  }

  fun clearAlerts() {
    viewModelScope.launch {
      repository.clearAlerts()
    }
  }

  // Pest & Disease AI
  fun analyzeCropImage(bitmap: Bitmap, imageUri: String?) {
    viewModelScope.launch {
      _pestState.value = PestDetectionUiState.Analyzing
      val result = repository.analyzeCropPestImage(bitmap, imageUri)
      result.fold(
        onSuccess = { diagnosis ->
          _pestState.value = PestDetectionUiState.Success(diagnosis)
        },
        onFailure = { error ->
          _pestState.value = PestDetectionUiState.Error(error.localizedMessage ?: "Crop diagnosis failed")
        }
      )
    }
  }

  fun loadSampleDiagnosis(sample: SampleCropImage) {
    viewModelScope.launch {
      repository.importSampleCropScan(sample)
      _pestState.value = PestDetectionUiState.Success(sample.sampleDiagnosis)
    }
  }

  fun resetPestState() {
    _pestState.value = PestDetectionUiState.Idle
  }

  // Yield Forecasting
  fun updateYieldInput(
    cropName: String = _yieldInput.value.cropName,
    areaSqMeters: Double = _yieldInput.value.areaSqMeters,
    growthStage: CropGrowthStage = _yieldInput.value.growthStage,
    soilType: SoilType = _yieldInput.value.soilType,
    avgDayTempC: Double = _yieldInput.value.avgDayTempC,
    avgNightTempC: Double = _yieldInput.value.avgNightTempC,
    avgHumidityPct: Double = _yieldInput.value.avgRelativeHumidityPct,
    lightDli: Double = _yieldInput.value.dailyLightIntegral,
    soilMoisturePct: Double = _yieldInput.value.avgSoilMoisturePct,
    nutrientsPpm: Double = _yieldInput.value.avgNutrientsPpm
  ) {
    val updated = _yieldInput.value.copy(
      cropName = cropName,
      areaSqMeters = areaSqMeters,
      growthStage = growthStage,
      soilType = soilType,
      avgDayTempC = avgDayTempC,
      avgNightTempC = avgNightTempC,
      avgRelativeHumidityPct = avgHumidityPct,
      dailyLightIntegral = lightDli,
      avgSoilMoisturePct = soilMoisturePct,
      avgNutrientsPpm = nutrientsPpm
    )
    _yieldInput.value = updated
    _yieldResult.value = repository.generateYieldForecast(updated)
  }
}
