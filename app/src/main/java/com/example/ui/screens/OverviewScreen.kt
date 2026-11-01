package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AgriAlert
import com.example.data.model.AlertLevel
import com.example.ui.components.BannerAlert
import com.example.ui.components.FlowDiagram
import com.example.ui.components.IrrigationControlCard
import com.example.ui.components.KpiGrid
import com.example.ui.components.SoilAndTemperatureCard
import com.example.ui.components.TrendChart
import com.example.ui.theme.ActionGreen
import com.example.ui.theme.ColorSoil
import com.example.ui.theme.ColorTank
import com.example.ui.theme.StatusCrit
import com.example.ui.theme.StatusInfo
import com.example.ui.theme.StatusOk
import com.example.ui.theme.StatusWarn
import com.example.ui.viewmodel.AgriViewModel
import com.example.ui.viewmodel.SmartAgriUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OverviewScreen(
  viewModel: AgriViewModel,
  uiState: SmartAgriUiState,
  alerts: List<AgriAlert>,
  onNavigateTo: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val scrollState = rememberScrollState()
  val s = uiState.status
  val activeEnv = uiState.activeZone

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(horizontal = 14.dp, vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {

    // 1. Zone Selector Chip Carousel
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // "All Farm" Option
      val isAll = uiState.selectedZoneId == "all"
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(20.dp))
          .background(if (isAll) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
          .border(
            1.dp,
            if (isAll) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
            RoundedCornerShape(20.dp)
          )
          .clickable { viewModel.selectZone("all") }
          .padding(horizontal = 14.dp, vertical = 7.dp)
          .testTag("zone_all_chip")
      ) {
        Text(
          text = "All Farm Overview",
          fontSize = 12.sp,
          fontWeight = if (isAll) FontWeight.Bold else FontWeight.Medium,
          color = if (isAll) Color.White else MaterialTheme.colorScheme.onSurface
        )
      }

      // Zone Chips
      uiState.zones.forEach { zone ->
        val isSelected = uiState.selectedZoneId == zone.zoneId
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
            .border(
              1.dp,
              if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
              RoundedCornerShape(20.dp)
            )
            .clickable { viewModel.selectZone(zone.zoneId) }
            .padding(horizontal = 14.dp, vertical = 7.dp)
            .testTag("zone_${zone.zoneId}_chip")
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            if (zone.isIrrigating) {
              Box(
                modifier = Modifier
                  .size(6.dp)
                  .clip(CircleShape)
                  .background(if (isSelected) Color.White else StatusInfo)
              )
              Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
              text = zone.name,
              fontSize = 12.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }
    }

    // 2. Microclimate Environmental Bar (Temp, Humidity, Light DLI, Soil)
    if (activeEnv != null) {
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = activeEnv.name,
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = activeEnv.cropType,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            // Temperature
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(Icons.Default.DeviceThermostat, contentDescription = null, tint = StatusWarn, modifier = Modifier.size(18.dp))
              Text(
                text = "${String.format("%.1f", activeEnv.temperatureC)}°C",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
              Text("Temp", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Humidity
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(Icons.Default.WaterDrop, contentDescription = null, tint = StatusInfo, modifier = Modifier.size(18.dp))
              Text(
                text = "${activeEnv.humidityPct.toInt()}%",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
              Text("RH", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Light DLI
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(Icons.Default.WbSunny, contentDescription = null, tint = Color(0xFFD69E2E), modifier = Modifier.size(18.dp))
              Text(
                text = "${String.format("%.1f", activeEnv.lightDliMol)} mol",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
              Text("DLI Light", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Soil Moisture
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(Icons.Default.Sensors, contentDescription = null, tint = ColorSoil, modifier = Modifier.size(18.dp))
              Text(
                text = "${activeEnv.soilMoisturePct.toInt()}%",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
              Text("Soil", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
        }
      }
    }

    // 3. Demo Simulator Quick Bar (if in demo mode)
    if (uiState.isDemo) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .background(StatusWarn.copy(alpha = 0.08f))
          .border(1.dp, StatusWarn.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
          .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Demo Mode",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
          color = StatusWarn
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          Button(
            onClick = { viewModel.triggerDemoScenario("drain") },
            shape = RoundedCornerShape(6.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
            modifier = Modifier.height(30.dp)
          ) {
            Text("Drain Nutrients", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
          }
          Button(
            onClick = { viewModel.triggerDemoScenario("dry") },
            shape = RoundedCornerShape(6.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
            modifier = Modifier.height(30.dp)
          ) {
            Text("Dry Soil", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
          }
          Button(
            onClick = { viewModel.triggerDemoScenario("feed") },
            shape = RoundedCornerShape(6.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
            modifier = Modifier.height(30.dp)
          ) {
            Text("Feed", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
          }
        }
      }
    }

    // 4. Status Banner
    BannerAlert(
      banner = uiState.banner,
      onActionClick = { action ->
        when (action) {
          "run" -> viewModel.startTimedIrrigation()
          "stop" -> viewModel.stopIrrigation()
          "clear" -> viewModel.clearFault()
          "resume" -> viewModel.resumeAuto()
          "go:connect" -> onNavigateTo("settings")
          "go:irrigation" -> onNavigateTo("irrigation")
          "go:nutrients" -> onNavigateTo("nutrients")
          "go:alerts" -> onNavigateTo("alerts")
        }
      }
    )

    // 5. Real-time Soil Moisture & Temperature Visualizer Card
    SoilAndTemperatureCard(
      soilMoisturePct = s.soil,
      temperatureC = activeEnv?.temperatureC ?: 24.5,
      zoneName = activeEnv?.name ?: "Commercial Greenhouse Root Zone",
      targetMoistureMinPct = s.cfg.onBelow,
      targetMoistureMaxPct = s.cfg.offAbove
    )

    // 6. Six Primary KPI Cards
    KpiGrid(
      status = s,
      history5m = viewModel.history5m.value,
      onCardClick = { kind -> viewModel.openDetailSheet(kind) }
    )

    // 6. Irrigation Control Widget
    IrrigationControlCard(
      status = s,
      selectedDurationSec = uiState.runDurationSec,
      onDurationSelect = { sec -> viewModel.setRunDurationSec(sec) },
      onStartIrrigation = { sec -> viewModel.startTimedIrrigation(sec) },
      onStopIrrigation = { viewModel.stopIrrigation() },
      onAutoModeToggle = { enabled -> viewModel.setAutoMode(enabled) },
      onResumeAuto = { viewModel.resumeAuto() }
    )

    // 7. Irrigation Flow Diagram
    FlowDiagram(
      status = s,
      onNodeClick = { kind -> viewModel.openDetailSheet(kind) }
    )

    // 8. Trends Chart
    val activeHistory = when (uiState.activeRange) {
      "1h" -> viewModel.history1h.value
      "24h" -> viewModel.history24h.value
      else -> viewModel.history5m.value
    }
    TrendChart(
      historyPoints = activeHistory,
      activeKey = uiState.activeTrendKey,
      activeRange = uiState.activeRange,
      config = s.cfg,
      onKeyChange = { viewModel.setTrendKey(it) },
      onRangeChange = { viewModel.setRange(it) }
    )

    // 9. Recent Events Section
    Card(
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Recent farm events",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "View all",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = ActionGreen,
            modifier = Modifier.clickable { onNavigateTo("alerts") }
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (alerts.isEmpty()) {
          Text(
            text = "No events logged yet.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        } else {
          alerts.take(4).forEach { alert ->
            val badgeColor = when (alert.level) {
              AlertLevel.CRITICAL -> StatusCrit
              AlertLevel.WARNING -> StatusWarn
              AlertLevel.INFO -> StatusInfo
            }
            val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(alert.timestamp))

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(badgeColor)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = alert.title,
                  style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = alert.message,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 1
                )
              }
              Text(
                text = timeStr,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))
  }
}
