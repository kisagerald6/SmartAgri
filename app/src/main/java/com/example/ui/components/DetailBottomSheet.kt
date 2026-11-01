package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AgriHistoryPoint
import com.example.data.model.AgriStatus
import com.example.ui.theme.ActionGreen
import com.example.ui.theme.ColorFlow
import com.example.ui.theme.ColorNutrients
import com.example.ui.theme.ColorSoil
import com.example.ui.theme.ColorTank
import com.example.ui.theme.StatusCrit
import com.example.ui.theme.StatusInfo
import com.example.ui.theme.StatusOk
import com.example.ui.theme.StatusWarn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailBottomSheet(
  kind: String, // "soil", "food", "tank", "flow", "pump"
  status: AgriStatus,
  historyPoints: List<AgriHistoryPoint>,
  onDismiss: () -> Unit,
  onUpdateConfig: (String, Double) -> Unit,
  onStartIrrigation: (Int) -> Unit,
  onStopIrrigation: () -> Unit,
  onResetFlow: () -> Unit,
  onToggleNutrients: (Boolean) -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val c = status.cfg
  val n = status.nutr

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    modifier = Modifier.testTag("detail_bottom_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        val title = when (kind) {
          "soil" -> "Soil moisture telemetry"
          "food" -> "Nutrient EC/TDS telemetry"
          "tank" -> "Water tank level"
          "flow" -> "Flow rate & volume"
          else -> "Pump operation"
        }
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface
        )
        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "Close")
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      when (kind) {
        "soil" -> {
          // Big Value
          Text(
            text = "${status.soil.toInt()}%",
            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
            color = ColorSoil
          )
          Text(
            text = if (status.soil < c.onBelow) "Below range. Irrigation starts automatically." else "Optimal root zone moisture.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(16.dp))

          // Stepper: Start below
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text("Start irrigation below", fontWeight = FontWeight.SemiBold)
              Text("Pump turns on when moisture drops here", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            StepperControl(
              value = c.onBelow,
              step = 5.0,
              minVal = 10.0,
              maxVal = 90.0,
              unit = "%",
              onValueChange = { onUpdateConfig("onBelow", it) },
              testTagPrefix = "detail_soil_start"
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Stepper: Stop above
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text("Stop irrigation at", fontWeight = FontWeight.SemiBold)
              Text("Pump stops once soil moisture reaches this", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            StepperControl(
              value = c.offAbove,
              step = 5.0,
              minVal = 20.0,
              maxVal = 100.0,
              unit = "%",
              onValueChange = { onUpdateConfig("offAbove", it) },
              testTagPrefix = "detail_soil_stop"
            )
          }
        }

        "food" -> {
          Text(
            text = if (n?.en == true) "${n.ppm} ppm" else "Monitoring Disabled",
            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
            color = ColorNutrients
          )
          Text(
            text = if (n?.en == true) {
              if (n.low) "Nutrient deficiency detected. Apply liquid fertiliser." else "Sufficient dissolved minerals for vegetative and fruit growth."
            } else "Probe unplugged or disabled.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(16.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text("Alert threshold", fontWeight = FontWeight.SemiBold)
              Text("Alerts when nutrients fall under this ppm", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            StepperControl(
              value = c.nutrLowPpm,
              step = 50.0,
              minVal = 100.0,
              maxVal = 2000.0,
              unit = "ppm",
              onValueChange = { onUpdateConfig("nutrLowPpm", it) },
              testTagPrefix = "detail_nutr_low"
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          Button(
            onClick = { onToggleNutrients(n?.en != true) },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (n?.en == true) StatusWarn else ActionGreen
            ),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(if (n?.en == true) "Disable nutrient probe" else "Enable nutrient monitoring")
          }
        }

        "tank" -> {
          Text(
            text = "${status.tank.toInt()}%",
            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
            color = ColorTank
          )
          Text(
            text = if (status.tank < c.minTank) "Critical water level! Auto shut-off engaged." else "Adequate reservoir supply.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(16.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text("Pause irrigation below", fontWeight = FontWeight.SemiBold)
              Text("Protects pump from running dry", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            StepperControl(
              value = c.minTank,
              step = 5.0,
              minVal = 5.0,
              maxVal = 60.0,
              unit = "%",
              onValueChange = { onUpdateConfig("minTank", it) },
              testTagPrefix = "detail_tank_min"
            )
          }
        }

        "flow" -> {
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
              Text("Current flow", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text(
                text = "${String.format("%.1f", status.flow)} L/min",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = ColorFlow
              )
            }
            Column(modifier = Modifier.weight(1f)) {
              Text("Total delivered", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text(
                text = "${String.format("%.1f", status.total)} L",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = ColorTank
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          Button(
            onClick = onResetFlow,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text("Reset total volume to 0 L", color = MaterialTheme.colorScheme.onSurface)
          }
        }

        else -> {
          Text(
            text = if (status.pump) "Pump Running" else "Pump Idle",
            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
            color = if (status.pump) StatusInfo else MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(16.dp))

          Button(
            onClick = { if (status.pump) onStopIrrigation() else onStartIrrigation(60) },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (status.pump) StatusCrit else ActionGreen
            ),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(if (status.pump) "Stop pump" else "Run pump (1 min)")
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}
