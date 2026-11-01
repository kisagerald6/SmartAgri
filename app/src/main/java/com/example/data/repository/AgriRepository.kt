package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.example.data.ai.PestDetectionAiService
import com.example.data.ai.YieldPredictorEngine
import com.example.data.local.AgriAlertEntity
import com.example.data.local.AgriDao
import com.example.data.local.FarmZoneEntity
import com.example.data.local.PestScanEntity
import com.example.data.local.YieldForecastEntity
import com.example.data.model.ActiveBanner
import com.example.data.model.AgriAlert
import com.example.data.model.AgriConfig
import com.example.data.model.AgriHistoryPoint
import com.example.data.model.AgriNutrientStatus
import com.example.data.model.AgriStatus
import com.example.data.model.AlertLevel
import com.example.data.model.DiseaseSeverity
import com.example.data.model.PestDiseaseDiagnosis
import com.example.data.model.SampleCropImage
import com.example.data.model.YieldForecastInput
import com.example.data.model.YieldForecastResult
import com.example.data.model.ZoneEnvironmentData
import com.example.data.network.AgriApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.util.UUID
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

private const val TAG = "AgriRepository"

class AgriRepository(
  private val context: Context,
  private val dao: AgriDao
) {
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
  private val aiService = PestDetectionAiService()

  // Connection and Live Status
  private val _host = MutableStateFlow("192.168.1.50")
  val host: StateFlow<String> = _host.asStateFlow()

  private val _isDemo = MutableStateFlow(true)
  val isDemo: StateFlow<Boolean> = _isDemo.asStateFlow()

  private val _siteName = MutableStateFlow("Sunrise Commercial Farm & Greenhouses")
  val siteName: StateFlow<String> = _siteName.asStateFlow()

  private val _isConnected = MutableStateFlow(true)
  val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

  private val _status = MutableStateFlow(AgriStatus())
  val status: StateFlow<AgriStatus> = _status.asStateFlow()

  // Selected Farm Zone (or "all" for whole farm)
  private val _selectedZoneId = MutableStateFlow("all")
  val selectedZoneId: StateFlow<String> = _selectedZoneId.asStateFlow()

  // Active Alert Notification Event (for sound & vibration triggers)
  private val _alertNotificationEvent = MutableSharedFlow<AgriAlert>()
  val alertNotificationEvent: SharedFlow<AgriAlert> = _alertNotificationEvent.asSharedFlow()

  // Historical Telemetry for Interactive Canvas Charts
  private val _history5m = MutableStateFlow<List<AgriHistoryPoint>>(emptyList())
  val history5m: StateFlow<List<AgriHistoryPoint>> = _history5m.asStateFlow()

  private val _history1h = MutableStateFlow<List<AgriHistoryPoint>>(emptyList())
  val history1h: StateFlow<List<AgriHistoryPoint>> = _history1h.asStateFlow()

  private val _history24h = MutableStateFlow<List<AgriHistoryPoint>>(emptyList())
  val history24h: StateFlow<List<AgriHistoryPoint>> = _history24h.asStateFlow()

  // Multi-Zone Farm Environmental Data
  private val _zonesData = MutableStateFlow<List<ZoneEnvironmentData>>(emptyList())
  val zonesData: StateFlow<List<ZoneEnvironmentData>> = _zonesData.asStateFlow()

  // Pest Diagnosis Scans
  val pastPestScans: Flow<List<PestDiseaseDiagnosis>> = dao.getAllPestScans().map { entities ->
    entities.map { entity ->
      val symList = mutableListOf<String>()
      try {
        val arr = JSONArray(entity.symptomsJson)
        for (i in 0 until arr.length()) symList.add(arr.getString(i))
      } catch (_: Exception) {}

      val orgList = mutableListOf<String>()
      try {
        val arr = JSONArray(entity.organicTreatmentsJson)
        for (i in 0 until arr.length()) orgList.add(arr.getString(i))
      } catch (_: Exception) {}

      val chemList = mutableListOf<String>()
      try {
        val arr = JSONArray(entity.chemicalTreatmentsJson)
        for (i in 0 until arr.length()) chemList.add(arr.getString(i))
      } catch (_: Exception) {}

      val prevList = mutableListOf<String>()
      try {
        val arr = JSONArray(entity.preventativeJson)
        for (i in 0 until arr.length()) prevList.add(arr.getString(i))
      } catch (_: Exception) {}

      PestDiseaseDiagnosis(
        cropName = entity.cropName,
        diagnosis = entity.diagnosis,
        confidenceScore = entity.confidenceScore,
        severity = try { DiseaseSeverity.valueOf(entity.severity) } catch (e: Exception) { DiseaseSeverity.MODERATE },
        affectedPart = entity.affectedPart,
        symptomsObserved = symList,
        organicTreatments = orgList,
        chemicalTreatments = chemList,
        preventativeMeasures = prevList,
        alertTriggered = entity.severity == "HIGH" || entity.severity == "CRITICAL",
        timestamp = entity.timestamp,
        imageUri = entity.imageUri
      )
    }
  }

  // Active Alerts Flow
  val alertsFlow: Flow<List<AgriAlert>> = dao.getAllAlerts().map { list ->
    list.map {
      AgriAlert(
        id = it.id,
        timestamp = it.timestamp,
        level = try { AlertLevel.valueOf(it.level) } catch (e: Exception) { AlertLevel.INFO },
        title = it.title,
        message = it.message,
        isRead = it.isRead
      )
    }
  }

  // Pre-loaded Sample Crops for Instant Pest/Disease Test Scans
  val sampleCropLibrary: List<SampleCropImage> = listOf(
    SampleCropImage(
      id = "tomato_early_blight",
      title = "Tomato Leaf - Early Blight",
      cropType = "Indeterminate Greenhouse Tomato",
      description = "Concentric dark brown 'bullseye' target rings with chlorotic yellow halo.",
      sampleDiagnosis = PestDiseaseDiagnosis(
        cropName = "Greenhouse Tomato",
        diagnosis = "Early Blight (Alternaria solani)",
        confidenceScore = 0.96,
        severity = DiseaseSeverity.HIGH,
        affectedPart = "Lower canopy foliage & petioles",
        symptomsObserved = listOf(
          "Brown necrotic lesions with concentric rings (target spots)",
          "Chlorotic yellowing around leaf margins",
          "Lower leaf senescence and premature defoliation"
        ),
        organicTreatments = listOf(
          "Spray Copper Octanoate (copper soap) fungicide every 7 days",
          "Apply Bacillus amyloliquefaciens / subtilis bio-fungicide to root and leaf zone",
          "Prune affected bottom 30cm of leaves to stop splash-up"
        ),
        chemicalTreatments = listOf(
          "Azoxystrobin (Heritage / Quadris) or Chlorothalonil 720 SC foliar spray",
          "Observe strict 7-day pre-harvest interval (PHI) and rotate modes of action"
        ),
        preventativeMeasures = listOf(
          "Switch from overhead watering to drip irrigation exclusively",
          "Maintain relative humidity below 80% with greenhouse exhaust fans",
          "Sanitize pruning clippers between rows with 70% isopropyl alcohol"
        ),
        alertTriggered = true
      )
    ),
    SampleCropImage(
      id = "pepper_spider_mites",
      title = "Bell Pepper - Two-Spotted Spider Mites",
      cropType = "Sweet Bell Pepper",
      description = "Fine stippling on leaf upper surface, bronzing, and delicate silk webbing.",
      sampleDiagnosis = PestDiseaseDiagnosis(
        cropName = "Sweet Bell Pepper",
        diagnosis = "Two-Spotted Spider Mite (Tetranychus urticae)",
        confidenceScore = 0.93,
        severity = DiseaseSeverity.CRITICAL,
        affectedPart = "Underside of young leaves and flower buds",
        symptomsObserved = listOf(
          "Chlorotic micro-speckling/stippling on upper leaf blade",
          "Fine silk webbing on leaf axils and growing tips",
          "Leaves turning bronze, brittle, and curling upward"
        ),
        organicTreatments = listOf(
          "Release biological predatory mites (Phytoseiulus persimilis or Neoseiulus californicus)",
          "Foliar spray with Cold-Pressed Neem Oil (1% emulsion) in the evening",
          "Apply insecticidal soap targeting the underside of leaves"
        ),
        chemicalTreatments = listOf(
          "Bifenazate (Floramite) or Spiromesifen selective miticide",
          "Avoid broad-spectrum pyrethroids which decimate beneficial predators"
        ),
        preventativeMeasures = listOf(
          "Elevate greenhouse relative humidity above 60% (spider mites thrive in dry heat)",
          "Inspect incoming plant plugs with a 10x hand lens during intake quarantine"
        ),
        alertTriggered = true
      )
    ),
    SampleCropImage(
      id = "cucumber_powdery_mildew",
      title = "Cucumber - Powdery Mildew",
      cropType = "English Long Cucumber",
      description = "White talcum-powder patches across upper leaf surfaces.",
      sampleDiagnosis = PestDiseaseDiagnosis(
        cropName = "Cucumber",
        diagnosis = "Powdery Mildew (Podosphaera xanthii)",
        confidenceScore = 0.95,
        severity = DiseaseSeverity.MODERATE,
        affectedPart = "Upper foliage and stems",
        symptomsObserved = listOf(
          "Circular talcum-like white powdery fungal colonies",
          "Leaves becoming dull green and losing photosynthetic capacity",
          "Early blossom drop under heavy colonization"
        ),
        organicTreatments = listOf(
          "Potassium bicarbonate (MilStop) foliar spray (raises pH to kill mycelium)",
          "Diluted milk whey spray (1:9 ratio with water) under morning sunlight",
          "Sulfur vaporization during greenhouse night cycle"
        ),
        chemicalTreatments = listOf(
          "Myclobutanil or Triflumizole systemics for rapid eradication"
        ),
        preventativeMeasures = listOf(
          "Increase air circulation using horizontal greenhouse fans (HAF)",
          "Select powdery-mildew resistant F1 commercial hybrids"
        ),
        alertTriggered = false
      )
    ),
    SampleCropImage(
      id = "lettuce_healthy",
      title = "Hydroponic Butterhead Lettuce - Prime Health",
      cropType = "Butterhead Lettuce",
      description = "Crisp emerald foliage, vigorous root crown, no lesions or pest evidence.",
      sampleDiagnosis = PestDiseaseDiagnosis(
        cropName = "Butterhead Lettuce",
        diagnosis = "Optimal Physiological Health - No Pathogens Detected",
        confidenceScore = 0.99,
        severity = DiseaseSeverity.LOW,
        affectedPart = "Entire plant canopy",
        symptomsObserved = listOf(
          "Turgid, deep green leaves with balanced venation",
          "No necrosis, chlorosis, or insect damage",
          "Active photosynthetic coloration"
        ),
        organicTreatments = listOf(
          "Maintain current balanced nutrient fertigation (EC 1.4 - 1.8 mS/cm)",
          "Continue routine beneficial root inoculant (Trichoderma harzianum)"
        ),
        chemicalTreatments = listOf(
          "No chemical intervention required."
        ),
        preventativeMeasures = listOf(
          "Keep dissolved oxygen (DO) in NFT water channels above 7 mg/L",
          "Inspect yellow sticky cards weekly for early winged thrip or aphid alerts"
        ),
        alertTriggered = false
      )
    )
  )

  // Simulation State for Demo Mode
  private var simSoil = 52.0
  private var simTank = 84.0
  private var simFlow = 0.0
  private var simTotal = 418.5
  private var simPump = false
  private var simMode = "auto"
  private var simFault = ""
  private var simRunSec = 0
  private var simWaterLeft = 0
  private var simHold = 0
  private var simNutrEn = true
  private var simNutrPpm = 760.0
  private var simNutrLow = false
  private var simNutrSeq = 0
  private var simBelowCount = 0
  private var simCfg = AgriConfig()

  private var apiService: AgriApiService = AgriApiService.create("192.168.1.50")
  private var failCount = 0

  init {
    initializeSeedData()
    startTelemetryLoop()
  }

  private fun initializeSeedData() {
    scope.launch {
      // Seed default zones if empty
      val initialZones = listOf(
        ZoneEnvironmentData(
          zoneId = "zone_1",
          name = "Greenhouse 1: Solanaceous",
          cropType = "Beefsteak Tomatoes & Bell Peppers",
          areaSqMeters = 1200.0,
          temperatureC = 24.8,
          humidityPct = 68.0,
          lightLux = 42500.0,
          lightDliMol = 24.2,
          soilMoisturePct = 52.0,
          nutrientsPpm = 820.0,
          tankLevelPct = 84.0,
          flowRateLMin = 0.0,
          isIrrigating = false
        ),
        ZoneEnvironmentData(
          zoneId = "zone_2",
          name = "Greenhouse 2: Hydroponic Greens",
          cropType = "Butterhead & Romaine Lettuce",
          areaSqMeters = 850.0,
          temperatureC = 21.4,
          humidityPct = 62.0,
          lightLux = 36000.0,
          lightDliMol = 19.5,
          soilMoisturePct = 78.0,
          nutrientsPpm = 640.0,
          tankLevelPct = 92.0,
          flowRateLMin = 1.4,
          isIrrigating = true
        ),
        ZoneEnvironmentData(
          zoneId = "zone_3",
          name = "Nursery Block 3: Seedlings",
          cropType = "High-Density Seedling Trays",
          areaSqMeters = 500.0,
          temperatureC = 26.2,
          humidityPct = 76.0,
          lightLux = 28000.0,
          lightDliMol = 16.0,
          soilMoisturePct = 64.0,
          nutrientsPpm = 450.0,
          tankLevelPct = 70.0,
          flowRateLMin = 0.0,
          isIrrigating = false
        ),
        ZoneEnvironmentData(
          zoneId = "zone_4",
          name = "Open Field Zone 4: Sweet Corn",
          cropType = "Sweet Corn & Brassicas",
          areaSqMeters = 5000.0,
          temperatureC = 28.5,
          humidityPct = 54.0,
          lightLux = 68000.0,
          lightDliMol = 32.0,
          soilMoisturePct = 38.0,
          nutrientsPpm = 950.0,
          tankLevelPct = 65.0,
          flowRateLMin = 0.0,
          isIrrigating = false
        )
      )
      _zonesData.value = initialZones

      // Seed initial history points
      val now = System.currentTimeMillis()
      val hist24 = mutableListOf<AgriHistoryPoint>()
      val hist1 = mutableListOf<AgriHistoryPoint>()
      val hist5 = mutableListOf<AgriHistoryPoint>()

      for (i in 0 until 120) {
        val t = now - (120 - i) * 720000L
        val x = (i % 24) / 24.0
        val soil = (54.0 - 15 * x + Random.nextDouble(-1.5, 1.5)).coerceIn(20.0, 95.0)
        val food = (780.0 + Random.nextDouble(-20.0, 20.0)).coerceIn(300.0, 1500.0)
        val tank = (85.0 - (i * 0.15)).coerceIn(10.0, 100.0)
        val flow = if (x > 0.85) 2.2 else 0.0
        val pt = AgriHistoryPoint(t, soil, food, tank, flow)
        hist24.add(pt)
        if (i >= 90) hist1.add(pt)
        if (i >= 110) hist5.add(pt)
      }
      _history24h.value = hist24
      _history1h.value = hist1
      _history5m.value = hist5

      // Seed initial sample alert logs if empty
      dao.insertAlert(
        AgriAlertEntity(
          timestamp = now - 3600000L * 2,
          level = "INFO",
          title = "Irrigation cycle finished",
          message = "Zone 1 soil moisture replenished to 60%. Total 48.2 L delivered.",
          isRead = true
        )
      )
      dao.insertAlert(
        AgriAlertEntity(
          timestamp = now - 3600000L * 5,
          level = "WARNING",
          title = "Nutrient level low in Zone 3",
          message = "Reading fell to 380 ppm (under 400 ppm threshold). Stock fertigation replenished.",
          isRead = true
        )
      )
    }
  }

  private fun startTelemetryLoop() {
    scope.launch {
      while (isActive) {
        try {
          if (_isDemo.value) {
            tickDemoSimulation()
            val simulatedStatus = getSimulatedStatus()
            _status.value = simulatedStatus
            _isConnected.value = true
            failCount = 0
            evaluateAlerts(simulatedStatus)
            recordHistory(simulatedStatus)
            updateZonesFromTelemetry(simulatedStatus)
          } else {
            val liveStatus = apiService.getStatus()
            _status.value = liveStatus
            _isConnected.value = true
            failCount = 0
            evaluateAlerts(liveStatus)
            recordHistory(liveStatus)
            updateZonesFromTelemetry(liveStatus)
          }
        } catch (e: Exception) {
          Log.w(TAG, "Controller polling tick failed: ${e.message}")
          failCount++
          if (failCount >= 2 && !_isDemo.value) {
            _isConnected.value = false
          }
        }
        delay(2000L)
      }
    }
  }

  private fun tickDemoSimulation() {
    // Water timing countdown
    if (simWaterLeft > 0) {
      simWaterLeft = max(0, simWaterLeft - 2)
      if (simWaterLeft == 0) simPump = false
    }

    // Auto pause countdown
    if (simHold > 0) {
      simHold = max(0, simHold - 2)
    }

    // Pump physics
    if (simPump) {
      simSoil = min(100.0, simSoil + 1.2)
      simTank = max(0.0, simTank - 0.35)
      simFlow = 2.1 + Random.nextDouble(-0.15, 0.25)
      simTotal += simFlow * (2.0 / 60.0)
      simRunSec += 2

      if (simTank < simCfg.minTank) {
        simPump = false
        simWaterLeft = 0
        simFlow = 0.0
        simFault = "TANK LOW"
      }
    } else {
      simFlow = 0.0
      simSoil = max(0.0, simSoil - 0.25)
      simRunSec = 0
    }

    // Automatic rules
    if (simMode == "auto" && simWaterLeft <= 0 && simHold <= 0) {
      if (!simPump && simSoil < simCfg.onBelow && simTank >= simCfg.minTank) {
        simPump = true
      } else if (simPump && simSoil >= simCfg.offAbove) {
        simPump = false
      }
    }

    // Nutrients dynamics
    if (simNutrEn) {
      if (simSoil >= 30.0) {
        simNutrPpm = max(0.0, simNutrPpm - (if (simPump) 4.0 else 1.2))
        if (simNutrPpm < simCfg.nutrLowPpm) {
          simBelowCount++
          if (simBelowCount >= 3 && !simNutrLow) {
            simNutrLow = true
            simNutrSeq++
          }
        } else {
          simBelowCount = 0
          if (simNutrPpm > simCfg.nutrLowPpm * 1.1) {
            simNutrLow = false
          }
        }
      }
    } else {
      simNutrLow = false
    }
  }

  private fun getSimulatedStatus(): AgriStatus {
    val ppmWithNoise = (simNutrPpm + Random.nextDouble(-2.5, 2.5)).toInt().coerceAtLeast(0)
    return AgriStatus(
      soil = simSoil,
      soilRaw = (2800 - simSoil * 16).toInt(),
      tank = simTank,
      tankRaw = (simTank * 22).toInt(),
      flow = simFlow,
      total = simTotal,
      pump = simPump,
      mode = simMode,
      fault = simFault,
      runSec = simRunSec,
      cooldownSec = 0,
      waterLeft = simWaterLeft,
      hold = simHold,
      uptime = 142800L,
      rssi = -54,
      cfg = simCfg,
      nutr = AgriNutrientStatus(
        en = simNutrEn,
        ppm = ppmWithNoise,
        pct = (ppmWithNoise / simCfg.nutrTarget * 100).coerceIn(0.0, 100.0),
        raw = (ppmWithNoise * 3.2).toInt(),
        low = simNutrLow,
        reliable = simSoil >= 30.0,
        seq = simNutrSeq,
        push = false
      )
    )
  }

  private fun recordHistory(s: AgriStatus) {
    val now = System.currentTimeMillis()
    val pt = AgriHistoryPoint(
      timestamp = now,
      soil = s.soil,
      food = if (s.nutr?.en == true && s.nutr.reliable) s.nutr.ppm.toDouble() else null,
      tank = s.tank,
      flow = s.flow
    )

    // 5-min buffer
    val cur5 = _history5m.value.toMutableList()
    cur5.add(pt)
    while (cur5.isNotEmpty() && cur5.first().timestamp < now - 300000L) cur5.removeAt(0)
    _history5m.value = cur5

    // 1-hour buffer
    val cur1 = _history1h.value.toMutableList()
    if (cur1.isEmpty() || now - cur1.last().timestamp >= 15000L) {
      cur1.add(pt)
      while (cur1.isNotEmpty() && cur1.first().timestamp < now - 3600000L) cur1.removeAt(0)
      _history1h.value = cur1
    }

    // 24-hour buffer
    val cur24 = _history24h.value.toMutableList()
    if (cur24.isEmpty() || now - cur24.last().timestamp >= 60000L) {
      cur24.add(pt)
      while (cur24.isNotEmpty() && cur24.first().timestamp < now - 86400000L) cur24.removeAt(0)
      _history24h.value = cur24
    }
  }

  private var prevStatus: AgriStatus? = null
  private suspend fun evaluateAlerts(s: AgriStatus) {
    val prev = prevStatus
    prevStatus = s
    val n = s.nutr

    // Detect Low Nutrients
    if (n != null && n.en && n.low && (prev == null || prev.nutr?.low != true)) {
      postAlert(
        AlertLevel.WARNING,
        "Nutrient level low",
        "Reading dropped to ${n.ppm} ppm (below ${s.cfg.nutrLowPpm.toInt()} ppm). Apply fertiliser."
      )
    }

    // Detect Nutrients Recovered
    if (n != null && n.en && !n.low && prev != null && prev.nutr?.low == true) {
      postAlert(
        AlertLevel.INFO,
        "Nutrient level back to normal",
        "Nutrients restored to ${n.ppm} ppm."
      )
    }

    // Detect Faults
    if (s.fault.isNotEmpty() && (prev == null || prev.fault != s.fault)) {
      when (s.fault) {
        "TANK LOW" -> postAlert(
          AlertLevel.CRITICAL,
          "Tank level critical",
          "Water tank dropped below ${s.cfg.minTank.toInt()}%. Irrigation stopped to protect pump."
        )
        "NO FLOW" -> postAlert(
          AlertLevel.CRITICAL,
          "No flow detected. Pump stopped",
          "Pump was active but water flow was below ${s.cfg.minFlow} L/min. Check lines and valves."
        )
        "MAX RUN" -> postAlert(
          AlertLevel.WARNING,
          "Pump resting",
          "Longest run time (${s.cfg.maxRunSec.toInt() / 60} min) reached. Cooldown active."
        )
      }
    }
  }

  private suspend fun postAlert(level: AlertLevel, title: String, message: String) {
    val alert = AgriAlert(level = level, title = title, message = message)
    dao.insertAlert(
      AgriAlertEntity(
        timestamp = alert.timestamp,
        level = level.name,
        title = title,
        message = message,
        isRead = false
      )
    )
    _alertNotificationEvent.emit(alert)
  }

  private fun updateZonesFromTelemetry(s: AgriStatus) {
    val current = _zonesData.value
    if (current.isNotEmpty()) {
      val updated = current.mapIndexed { index, zone ->
        if (index == 0) {
          zone.copy(
            soilMoisturePct = s.soil,
            tankLevelPct = s.tank,
            flowRateLMin = s.flow,
            nutrientsPpm = s.nutr?.ppm?.toDouble() ?: zone.nutrientsPpm,
            isIrrigating = s.pump
          )
        } else {
          zone
        }
      }
      _zonesData.value = updated
    }
  }

  // Controller Actions
  suspend fun startTimedIrrigation(seconds: Int) {
    if (_isDemo.value) {
      if (simTank < simCfg.minTank) {
        simFault = "TANK LOW"
        return
      }
      simFault = ""
      simPump = true
      simWaterLeft = seconds
      simHold = 0
      _status.value = getSimulatedStatus()
    } else {
      val res = apiService.triggerWater(seconds)
      _status.value = res
    }
  }

  suspend fun stopIrrigation() {
    if (_isDemo.value) {
      simPump = false
      simWaterLeft = 0
      if (simMode == "auto") simHold = 600 // 10 min hold
      _status.value = getSimulatedStatus()
    } else {
      val res = apiService.setPumpState("off")
      _status.value = res
    }
  }

  suspend fun resumeAuto() {
    if (_isDemo.value) {
      simHold = 0
      _status.value = getSimulatedStatus()
    } else {
      val res = apiService.resumeAuto()
      _status.value = res
    }
  }

  suspend fun setAutoMode(enabled: Boolean) {
    val mode = if (enabled) "auto" else "manual"
    if (_isDemo.value) {
      simMode = mode
      if (mode == "manual") {
        simPump = false
        simWaterLeft = 0
      }
      _status.value = getSimulatedStatus()
    } else {
      val res = apiService.setMode(mode)
      _status.value = res
    }
  }

  suspend fun setNutrientMonitoring(enabled: Boolean) {
    if (_isDemo.value) {
      simNutrEn = enabled
      _status.value = getSimulatedStatus()
    } else {
      val res = apiService.updateConfig(mapOf("nutrEnabled" to if (enabled) "1" else "0"))
      _status.value = res
    }
  }

  suspend fun updateConfigKey(key: String, value: Double) {
    if (_isDemo.value) {
      when (key) {
        "onBelow" -> simCfg = simCfg.copy(onBelow = value)
        "offAbove" -> simCfg = simCfg.copy(offAbove = value)
        "minTank" -> simCfg = simCfg.copy(minTank = value)
        "maxRunSec" -> simCfg = simCfg.copy(maxRunSec = value)
        "cooldownSec" -> simCfg = simCfg.copy(cooldownSec = value)
        "minFlow" -> simCfg = simCfg.copy(minFlow = value)
        "soilDry" -> simCfg = simCfg.copy(soilDry = value)
        "soilWet" -> simCfg = simCfg.copy(soilWet = value)
        "nutrLowPpm" -> simCfg = simCfg.copy(nutrLowPpm = value)
        "nutrTarget" -> simCfg = simCfg.copy(nutrTarget = value)
      }
      _status.value = getSimulatedStatus()
    } else {
      val res = apiService.updateConfig(mapOf(key to value.toString()))
      _status.value = res
    }
  }

  suspend fun clearFault() {
    if (_isDemo.value) {
      simFault = ""
      simHold = 0
      _status.value = getSimulatedStatus()
    } else {
      val res = apiService.clearFault()
      _status.value = res
    }
  }

  suspend fun resetFlowTotal() {
    if (_isDemo.value) {
      simTotal = 0.0
      _status.value = getSimulatedStatus()
    } else {
      val res = apiService.resetFlow()
      _status.value = res
    }
  }

  suspend fun calibrateSoil(point: String) {
    if (_isDemo.value) {
      if (point == "dry") simCfg = simCfg.copy(soilDry = 2800.0)
      if (point == "wet") simCfg = simCfg.copy(soilWet = 1200.0)
      _status.value = getSimulatedStatus()
    } else {
      val res = apiService.calibrate(point)
      _status.value = res
    }
  }

  fun triggerDemoScenario(scenario: String) {
    scope.launch {
      if (_isDemo.value) {
        when (scenario) {
          "drain" -> {
            simNutrEn = true
            simSoil = max(simSoil, 45.0)
            simNutrPpm = simCfg.nutrLowPpm * 0.72
          }
          "feed" -> {
            simNutrPpm = simCfg.nutrTarget * 0.95
          }
          "dry" -> {
            simSoil = 20.0
          }
        }
        _status.value = getSimulatedStatus()
      } else {
        try {
          apiService.triggerDemoAction(scenario)
        } catch (_: Exception) {}
      }
    }
  }

  fun selectZone(zoneId: String) {
    _selectedZoneId.value = zoneId
  }

  fun setSiteName(name: String) {
    _siteName.value = name
  }

  fun connectToHost(ipAddress: String, isDemoMode: Boolean) {
    _isDemo.value = isDemoMode
    _host.value = ipAddress
    if (!isDemoMode) {
      apiService = AgriApiService.create(ipAddress)
    }
  }

  // Alerts Management
  suspend fun markAllAlertsRead() {
    dao.markAllAlertsRead()
  }

  suspend fun clearAlerts() {
    dao.clearAlerts()
  }

  // Pest & Disease Detection
  suspend fun analyzeCropPestImage(bitmap: Bitmap, imageUri: String?): Result<PestDiseaseDiagnosis> {
    val result = aiService.analyzeCropImage(bitmap, imageUri)
    result.onSuccess { diagnosis ->
      // Save scan to database
      dao.insertPestScan(
        PestScanEntity(
          timestamp = diagnosis.timestamp,
          cropName = diagnosis.cropName,
          diagnosis = diagnosis.diagnosis,
          confidenceScore = diagnosis.confidenceScore,
          severity = diagnosis.severity.name,
          affectedPart = diagnosis.affectedPart,
          symptomsJson = JSONArray(diagnosis.symptomsObserved).toString(),
          organicTreatmentsJson = JSONArray(diagnosis.organicTreatments).toString(),
          chemicalTreatmentsJson = JSONArray(diagnosis.chemicalTreatments).toString(),
          preventativeJson = JSONArray(diagnosis.preventativeMeasures).toString(),
          imageUri = imageUri
        )
      )

      // If high or critical severity, also post an alert to farm log!
      if (diagnosis.alertTriggered) {
        postAlert(
          level = if (diagnosis.severity == DiseaseSeverity.CRITICAL) AlertLevel.CRITICAL else AlertLevel.WARNING,
          title = "Pathogen Detected: ${diagnosis.diagnosis}",
          message = "${diagnosis.cropName} (${(diagnosis.confidenceScore * 100).toInt()}% conf): ${diagnosis.symptomsObserved.firstOrNull() ?: "Action required"}"
        )
      }
    }
    return result
  }

  suspend fun importSampleCropScan(sample: SampleCropImage) {
    val diag = sample.sampleDiagnosis
    dao.insertPestScan(
      PestScanEntity(
        timestamp = System.currentTimeMillis(),
        cropName = diag.cropName,
        diagnosis = diag.diagnosis,
        confidenceScore = diag.confidenceScore,
        severity = diag.severity.name,
        affectedPart = diag.affectedPart,
        symptomsJson = JSONArray(diag.symptomsObserved).toString(),
        organicTreatmentsJson = JSONArray(diag.organicTreatments).toString(),
        chemicalTreatmentsJson = JSONArray(diag.chemicalTreatments).toString(),
        preventativeJson = JSONArray(diag.preventativeMeasures).toString(),
        imageUri = null
      )
    )

    if (diag.alertTriggered) {
      postAlert(
        level = if (diag.severity == DiseaseSeverity.CRITICAL) AlertLevel.CRITICAL else AlertLevel.WARNING,
        title = "Pest Alert: ${diag.diagnosis}",
        message = "${diag.cropName}: Recommended treatment available."
      )
    }
  }

  // Yield Forecasting
  fun generateYieldForecast(input: YieldForecastInput): YieldForecastResult {
    val result = YieldPredictorEngine.calculateForecast(input)
    scope.launch {
      dao.insertYieldForecast(
        YieldForecastEntity(
          timestamp = result.timestamp,
          cropName = result.cropName,
          zoneId = input.zoneId,
          predictedYieldKg = result.predictedYieldKg,
          harvestWindowStart = result.harvestWindowStart,
          harvestWindowEnd = result.harvestWindowEnd,
          potentialScorePct = result.potentialScorePct
        )
      )
    }
    return result
  }
}
