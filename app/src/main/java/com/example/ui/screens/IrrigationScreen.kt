package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AgriStatus
import com.example.ui.components.IrrigationControlCard
import com.example.ui.components.StepperControl
import com.example.ui.components.TrendChart
import com.example.ui.theme.ColorSoil
import com.example.ui.theme.ColorTank
import com.example.ui.theme.StatusCrit
import com.example.ui.theme.StatusWarn
import com.example.ui.viewmodel.AgriViewModel
import com.example.ui.viewmodel.SmartAgriUiState

@Composable
fun IrrigationScreen(
  viewModel: AgriViewModel,
  uiState: SmartAgriUiState,
  modifier: Modifier = Modifier
) {
  val s = uiState.status
  val c = s.cfg
  var showResetDialog by remember { mutableStateOf(false) }

  if (showResetDialog) {
    AlertDialog(
      onDismissRequest = { showResetDialog = false },
      title = { Text("Reset water total?") },
      text = { Text("This will reset the total cumulative litres used back to 0.0 L.") },
      confirmButton = {
        Button(
          onClick = {
            viewModel.resetFlow()
            showResetDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = StatusCrit)
        ) {
          Text("Reset")
        }
      },
      dismissButton = {
        TextButton(onClick = { showResetDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 14.dp, vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {

    // 1. Irrigation Run & Mode Control
    IrrigationControlCard(
      status = s,
      selectedDurationSec = uiState.runDurationSec,
      onDurationSelect = { sec -> viewModel.setRunDurationSec(sec) },
      onStartIrrigation = { sec -> viewModel.startTimedIrrigation(sec) },
      onStopIrrigation = { viewModel.stopIrrigation() },
      onAutoModeToggle = { enabled -> viewModel.setAutoMode(enabled) },
      onResumeAuto = { viewModel.resumeAuto() }
    )

    // 2. Automatic Soil Moisture Rules Card
    Card(
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Text(
          text = "Automatic irrigation thresholds",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Start below stepper
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text("Start when soil is below", fontWeight = FontWeight.SemiBold)
            Text("Irrigation triggers automatically below this level", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          StepperControl(
            value = c.onBelow,
            step = 5.0,
            minVal = 5.0,
            maxVal = 95.0,
            unit = "%",
            onValueChange = { viewModel.updateConfig("onBelow", it) },
            testTagPrefix = "cfg_on_below"
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Stop above stepper
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text("Stop when soil moisture reaches", fontWeight = FontWeight.SemiBold)
            Text("Pump turns off when target soil moisture is reached", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          StepperControl(
            value = c.offAbove,
            step = 5.0,
            minVal = 10.0,
            maxVal = 100.0,
            unit = "%",
            onValueChange = { viewModel.updateConfig("offAbove", it) },
            testTagPrefix = "cfg_off_above"
          )
        }
      }
    }

    // 3. Soil Moisture Trend Chart
    TrendChart(
      historyPoints = when (uiState.activeRange) {
        "1h" -> viewModel.history1h.value
        "24h" -> viewModel.history24h.value
        else -> viewModel.history5m.value
      },
      activeKey = "soil",
      activeRange = uiState.activeRange,
      config = c,
      onKeyChange = { viewModel.setTrendKey(it) },
      onRangeChange = { viewModel.setRange(it) }
    )

    // 4. Water Tank Protection Card
    val isCriticalTank = s.tank < c.minTank
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
          Text("Water reservoir tank", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
          Text(
            text = "${s.tank.toInt()}% full",
            fontWeight = FontWeight.Bold,
            color = if (isCriticalTank) StatusCrit else ColorTank
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LinearProgressIndicator(
          progress = { (s.tank / 100.0).toFloat().coerceIn(0f, 1f) },
          color = if (isCriticalTank) StatusCrit else ColorTank,
          trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
          modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(5.dp))
        )

        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = if (isCriticalTank) "Critical water level! Refill tank before pump starts." else "Pump pauses automatically below ${c.minTank.toInt()}%.",
          style = MaterialTheme.typography.bodySmall,
          color = if (isCriticalTank) StatusCrit else MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text("Pause irrigation below", fontWeight = FontWeight.SemiBold)
            Text("Protects pump motor from cavitation & running dry", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          StepperControl(
            value = c.minTank,
            step = 5.0,
            minVal = 5.0,
            maxVal = 60.0,
            unit = "%",
            onValueChange = { viewModel.updateConfig("minTank", it) },
            testTagPrefix = "cfg_min_tank"
          )
        }
      }
    }

    // 5. Flow and Volume Card
    Card(
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Text("Flow telemetry & water consumed", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(10.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant)
              .padding(12.dp)
          ) {
            Column {
              Text(
                text = "${String.format("%.1f", s.total)} L",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
              Text("Total used", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }

          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(10.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant)
              .padding(12.dp)
          ) {
            Column {
              Text(
                text = "${String.format("%.1f", s.flow)} L/min",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
              Text("Flow now", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
          onClick = { showResetDialog = true },
          shape = RoundedCornerShape(8.dp),
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          modifier = Modifier.testTag("reset_flow_button")
        ) {
          Text("Reset water total", color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
        }
      }
    }

    // 6. Pump Protection Card
    Card(
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Text("Pump protection limits", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(14.dp))

        // Longest run
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text("Longest single run", fontWeight = FontWeight.SemiBold)
            Text("Pump stops after this continuous duration", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          StepperControl(
            value = c.maxRunSec / 60.0,
            step = 1.0,
            minVal = 1.0,
            maxVal = 30.0,
            unit = "min",
            onValueChange = { viewModel.updateConfig("maxRunSec", it * 60.0) },
            testTagPrefix = "cfg_max_run"
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Cooldown rest
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text("Rest after a long run", fontWeight = FontWeight.SemiBold)
            Text("Mandatory motor rest interval before restarting", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          StepperControl(
            value = c.cooldownSec / 60.0,
            step = 1.0,
            minVal = 0.0,
            maxVal = 30.0,
            unit = "min",
            onValueChange = { viewModel.updateConfig("cooldownSec", it * 60.0) },
            testTagPrefix = "cfg_cooldown"
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Min flow threshold
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text("Stop if flow is below", fontWeight = FontWeight.SemiBold)
            Text("Detects empty tank or blocked lines (0 to disable)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          StepperControl(
            value = c.minFlow,
            step = 0.1,
            minVal = 0.0,
            maxVal = 5.0,
            unit = "L/m",
            onValueChange = { viewModel.updateConfig("minFlow", it) },
            testTagPrefix = "cfg_min_flow"
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))
  }
}
