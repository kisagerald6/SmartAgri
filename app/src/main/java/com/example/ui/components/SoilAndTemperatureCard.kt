package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ActionGreen
import com.example.ui.theme.ColorSoil
import com.example.ui.theme.ColorTank
import com.example.ui.theme.StatusCrit
import com.example.ui.theme.StatusInfo
import com.example.ui.theme.StatusOk
import com.example.ui.theme.StatusWarn

/**
 * Visualizes real-time soil moisture and temperature telemetry data using
 * a Material 3 Card layout with animated progress indicators, target agronomic thresholds,
 * and status chips.
 */
@Composable
fun SoilAndTemperatureCard(
  soilMoisturePct: Double,
  temperatureC: Double,
  zoneName: String = "Root Zone Telemetry",
  targetMoistureMinPct: Double = 35.0,
  targetMoistureMaxPct: Double = 65.0,
  optimalTempMinC: Double = 18.0,
  optimalTempMaxC: Double = 28.0,
  modifier: Modifier = Modifier
) {
  // Moisture Status
  val (moistureStatusText, moistureColor) = when {
    soilMoisturePct < targetMoistureMinPct -> "Dry (Action Needed)" to StatusWarn
    soilMoisturePct > targetMoistureMaxPct + 15.0 -> "Saturated (Waterlogging)" to StatusCrit
    soilMoisturePct > targetMoistureMaxPct -> "Adequate / High" to StatusInfo
    else -> "Optimal Field Capacity" to StatusOk
  }

  // Temperature Status
  val (tempStatusText, tempColor) = when {
    temperatureC < optimalTempMinC -> "Cool / Growth Slowdown" to StatusInfo
    temperatureC > optimalTempMaxC + 4.0 -> "Heat Stress Detected" to StatusCrit
    temperatureC > optimalTempMaxC -> "Warm / Watch Venting" to StatusWarn
    else -> "Ideal Canopy Temp" to StatusOk
  }

  val animatedMoistureProgress by animateFloatAsState(
    targetValue = (soilMoisturePct / 100.0).toFloat().coerceIn(0f, 1f),
    label = "moisture_progress"
  )

  // Normalize temperature between 0°C and 50°C for progress visualization
  val animatedTempProgress by animateFloatAsState(
    targetValue = ((temperatureC - 0.0) / (50.0 - 0.0)).toFloat().coerceIn(0f, 1f),
    label = "temp_progress"
  )

  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    modifier = modifier
      .fillMaxWidth()
      .testTag("soil_temp_realtime_card")
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      // Card Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(28.dp)
              .clip(CircleShape)
              .background(ActionGreen.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Sensors,
              contentDescription = null,
              tint = ActionGreen,
              modifier = Modifier.size(16.dp)
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = zoneName,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(ActionGreen.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
          Text(
            text = "Real-time Telemetry",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = ActionGreen
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Section 1: Soil Moisture Indicator
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant)
          .padding(12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.WaterDrop,
              contentDescription = "Soil moisture",
              tint = ColorSoil,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Soil Moisture",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          Row(verticalAlignment = Alignment.Bottom) {
            Text(
              text = "${soilMoisturePct.toInt()}",
              style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
              color = moistureColor
            )
            Text(
              text = "%",
              style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(bottom = 2.dp, start = 2.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Linear Progress Indicator with rounded cap
        LinearProgressIndicator(
          progress = { animatedMoistureProgress },
          modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(5.dp)),
          color = moistureColor,
          trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
          strokeCap = StrokeCap.Round
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(moistureColor)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
              text = moistureStatusText,
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = moistureColor
            )
          }

          Text(
            text = "Target: ${targetMoistureMinPct.toInt()}% – ${targetMoistureMaxPct.toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Section 2: Temperature Indicator
      val tempF = (temperatureC * 9.0 / 5.0) + 32.0
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant)
          .padding(12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.DeviceThermostat,
              contentDescription = "Temperature",
              tint = StatusWarn,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Canopy Temperature",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          Row(verticalAlignment = Alignment.Bottom) {
            Text(
              text = String.format("%.1f", temperatureC),
              style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
              color = tempColor
            )
            Text(
              text = "°C",
              style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(bottom = 2.dp, start = 2.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "(${String.format("%.1f", tempF)}°F)",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(bottom = 2.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Linear Progress Indicator for Temperature (0°C to 50°C scale)
        LinearProgressIndicator(
          progress = { animatedTempProgress },
          modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(5.dp)),
          color = tempColor,
          trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
          strokeCap = StrokeCap.Round
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(tempColor)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
              text = tempStatusText,
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = tempColor
            )
          }

          Text(
            text = "Optimal: ${optimalTempMinC.toInt()}°C – ${optimalTempMaxC.toInt()}°C",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }
}
