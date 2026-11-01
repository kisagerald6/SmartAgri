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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.ui.components.StepperControl
import com.example.ui.components.TrendChart
import com.example.ui.theme.ActionGreen
import com.example.ui.theme.ColorNutrients
import com.example.ui.theme.StatusCrit
import com.example.ui.theme.StatusOk
import com.example.ui.theme.StatusWarn
import com.example.ui.viewmodel.AgriViewModel
import com.example.ui.viewmodel.SmartAgriUiState
import kotlin.math.max

@Composable
fun NutrientsScreen(
  viewModel: AgriViewModel,
  uiState: SmartAgriUiState,
  modifier: Modifier = Modifier
) {
  val s = uiState.status
  val c = s.cfg
  val n = s.nutr
  val isEnabled = n?.en == true

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 14.dp, vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {

    // 1. Setup Card if monitoring not turned on
    if (!isEnabled) {
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("Set up nutrient monitoring", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Connect an analog EC/TDS soil probe to pin 32 on the controller to monitor fertilizer concentration and receive deficiency alerts.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(14.dp))

          // 3 Steps
          listOf(
            "1" to ("Connect the probe" to "Analog EC/TDS probe to GPIO pin 32 on the ESP32 controller."),
            "2" to ("Turn on monitoring" to "Press the button below to start reading ppm telemetry."),
            "3" to ("Set the alert level" to "400 ppm is a standard starting baseline. Adjust for your specific crop.")
          ).forEach { (num, content) ->
            Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
              Box(
                modifier = Modifier
                  .size(24.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
              ) {
                Text(num, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(content.first, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(content.second, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          Button(
            onClick = { viewModel.setNutrientMonitoring(true) },
            colors = ButtonDefaults.buttonColors(containerColor = ActionGreen),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().testTag("enable_nutrients_btn")
          ) {
            Text("Turn on nutrient monitoring", fontWeight = FontWeight.Bold)
          }
        }
      }
    } else {
      // 2. Active Nutrient Level Card with Multi-Segment Meter
      val ppm = n.ppm
      val verdict = when {
        n.low -> "Low. Apply balanced fertiliser."
        !n.reliable -> "On hold until soil moisture is at least 30%."
        ppm < c.nutrLowPpm * 1.5 -> "Normal, but falling."
        ppm > c.nutrTarget -> "High. Rich fertilizer reservoir present."
        else -> "Normal. Crops are well fed."
      }

      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Nutrient concentration", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (n.low) StatusWarn.copy(alpha = 0.15f) else StatusOk.copy(alpha = 0.15f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = if (n.low) "Low" else "Optimal",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (n.low) StatusWarn else StatusOk
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          Row(verticalAlignment = Alignment.Bottom) {
            Text(
              text = "$ppm",
              style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
              color = ColorNutrients
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "ppm EC/TDS",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(bottom = 6.dp)
            )
          }

          Text(
            text = verdict,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = if (n.low) StatusWarn else MaterialTheme.colorScheme.onSurface
          )

          Spacer(modifier = Modifier.height(20.dp))

          // Multi-zone color gauge meter
          val maxMeter = max(c.nutrTarget * 1.4, max(ppm * 1.1, c.nutrLowPpm * 2.2))
          val lowPct = (c.nutrLowPpm / maxMeter).toFloat().coerceIn(0.1f, 0.45f)
          val okPct = ((c.nutrTarget - c.nutrLowPpm) / maxMeter).toFloat().coerceIn(0.2f, 0.6f)
          val pointerPct = (ppm / maxMeter).toFloat().coerceIn(0.02f, 0.98f)

          Column(modifier = Modifier.fillMaxWidth()) {
            // Gauge Bar
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(18.dp)
                .clip(RoundedCornerShape(6.dp))
            ) {
              Row(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(lowPct).fillMaxSize().background(Color(0xFFB94A5B)), contentAlignment = Alignment.Center) {
                  Text("Low", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Box(modifier = Modifier.weight(okPct).fillMaxSize().background(Color(0xFF2E8B5C)), contentAlignment = Alignment.Center) {
                  Text("Good", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Box(modifier = Modifier.weight(max(0.1f, 1f - lowPct - okPct)).fillMaxSize().background(Color(0xFF4A6FB5)), contentAlignment = Alignment.Center) {
                  Text("High", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
              }
            }

            Spacer(modifier = Modifier.height(4.dp))
            // Pin indicator
            Text(
              text = "Current reading: $ppm ppm (Alert threshold: ${c.nutrLowPpm.toInt()} ppm)",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Reliability check: Readings count only while soil moisture is at least 30% (currently ${s.soil.toInt()}%).",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      // 3. Alert Settings Card
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("Nutrient alert parameters", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
          Spacer(modifier = Modifier.height(14.dp))

          // Stepper: Low PPM alert
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text("Alert when nutrients drop below", fontWeight = FontWeight.SemiBold)
              Text("Triggers after 20 seconds under this level", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            StepperControl(
              value = c.nutrLowPpm,
              step = 50.0,
              minVal = 50.0,
              maxVal = 3000.0,
              unit = "ppm",
              onValueChange = { viewModel.updateConfig("nutrLowPpm", it) },
              testTagPrefix = "cfg_nutr_low"
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Monitoring Toggle Switch
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text("Nutrient monitoring", fontWeight = FontWeight.SemiBold)
              Text("Turn off if probe is unplugged or servicing", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
              checked = isEnabled,
              onCheckedChange = { viewModel.setNutrientMonitoring(it) },
              colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ActionGreen)
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Stepper: Full target reading
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text("Target full reading", fontWeight = FontWeight.SemiBold)
              Text("Well-fed baseline for your crop (scales meter)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            StepperControl(
              value = c.nutrTarget,
              step = 100.0,
              minVal = 200.0,
              maxVal = 5000.0,
              unit = "ppm",
              onValueChange = { viewModel.updateConfig("nutrTarget", it) },
              testTagPrefix = "cfg_nutr_target"
            )
          }
        }
      }

      // 4. Nutrient Trend Chart
      TrendChart(
        historyPoints = when (uiState.activeRange) {
          "1h" -> viewModel.history1h.value
          "24h" -> viewModel.history24h.value
          else -> viewModel.history5m.value
        },
        activeKey = "food",
        activeRange = uiState.activeRange,
        config = c,
        onKeyChange = { viewModel.setTrendKey(it) },
        onRangeChange = { viewModel.setRange(it) }
      )

      // 5. Action Guide: What to do when nutrients are low
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("What to do when nutrients are low", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
          Spacer(modifier = Modifier.height(10.dp))

          listOf(
            "1" to ("Apply liquid or water-soluble fertiliser" to "Use balanced NPK at label rate for your commercial crop."),
            "2" to ("Irrigate to dissolve into root zone" to "Water transports dissolved salts and nutrients directly to feeder root hairs."),
            "3" to ("Recheck reading after one hour" to "PPM reading will rise as fertilizer dissolves; alerts clear automatically when normal.")
          ).forEach { (num, pair) ->
            Row(modifier = Modifier.padding(vertical = 4.dp)) {
              Box(
                modifier = Modifier
                  .size(22.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
              ) {
                Text(num, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(pair.first, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(pair.second, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))
  }
}
