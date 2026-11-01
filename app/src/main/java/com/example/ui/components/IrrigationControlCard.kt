package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.data.model.AgriStatus
import com.example.ui.theme.ActionGreen
import com.example.ui.theme.StatusCrit
import com.example.ui.theme.StatusInfo
import com.example.ui.theme.StatusWarn

@Composable
fun IrrigationControlCard(
  status: AgriStatus,
  selectedDurationSec: Int,
  onDurationSelect: (Int) -> Unit,
  onStartIrrigation: (Int) -> Unit,
  onStopIrrigation: () -> Unit,
  onAutoModeToggle: (Boolean) -> Unit,
  onResumeAuto: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isRunning = status.pump
  val isAuto = status.mode == "auto"

  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    modifier = modifier
      .fillMaxWidth()
      .testTag("irrigation_control_card")
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      // Header: State and Subtitle
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        Column {
          Text(
            text = if (isRunning) "Irrigating" else if (status.cooldownSec > 0) "Resting" else "Idle",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = if (isRunning) {
              if (status.waterLeft > 0) "${status.waterLeft}s remaining" else "Running for ${status.runSec}s"
            } else if (status.cooldownSec > 0) {
              "Rest ends in ${status.cooldownSec}s"
            } else if (isAuto) {
              "Starts below ${status.cfg.onBelow.toInt()}%, stops at ${status.cfg.offAbove.toInt()}%"
            } else {
              "Automatic irrigation is off"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(
              if (isRunning) StatusInfo.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
            )
            .border(
              1.dp,
              if (isRunning) StatusInfo.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant,
              RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text(
            text = if (isRunning) "Running" else if (isAuto) "Automatic" else "Manual",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isRunning) StatusInfo else MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Progress bar if running
      if (isRunning) {
        val totalSec = maxOf(1, status.waterLeft + status.runSec)
        val progress = if (status.waterLeft > 0) {
          (1f - (status.waterLeft.toFloat() / totalSec)).coerceIn(0f, 1f)
        } else {
          1f
        }
        LinearProgressIndicator(
          progress = { progress },
          modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp)),
          color = StatusInfo,
          trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
        )
        Spacer(modifier = Modifier.height(10.dp))
      }

      // Duration Selector Pills: 30s, 60s, 120s, 180s
      Text(
        text = "Run duration",
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(6.dp))
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant)
          .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
      ) {
        listOf(30 to "30 s", 60 to "1 min", 120 to "2 min", 180 to "3 min").forEach { (sec, label) ->
          val isSelected = selectedDurationSec == sec
          Box(
            modifier = Modifier
              .weight(1f)
              .clickable { onDurationSelect(sec) }
              .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
              .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = label,
              fontSize = 12.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Big Start / Stop Button
      Button(
        onClick = {
          if (isRunning) onStopIrrigation() else onStartIrrigation(selectedDurationSec)
        },
        colors = ButtonDefaults.buttonColors(
          containerColor = if (isRunning) StatusCrit else ActionGreen
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .testTag("irrigation_action_button")
      ) {
        Icon(
          imageVector = if (isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
          contentDescription = null,
          tint = Color.White
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = if (isRunning) "Stop irrigation" else "Start irrigation (${selectedDurationSec}s)",
          style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
          color = Color.White
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Automatic Irrigation Switch Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Automatic irrigation",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = if (isAuto) "Starts under ${status.cfg.onBelow.toInt()}%, stops at ${status.cfg.offAbove.toInt()}%." else "Off. Pump only triggers on manual start.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        Switch(
          checked = isAuto,
          onCheckedChange = { onAutoModeToggle(it) },
          colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = ActionGreen
          ),
          modifier = Modifier.testTag("auto_mode_switch")
        )
      }

      // Auto Hold Pause Indicator
      if (status.hold > 0 && isAuto) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(StatusWarn.copy(alpha = 0.12f))
            .border(1.dp, StatusWarn.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
            .padding(10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Auto paused for ${Math.ceil(status.hold / 60.0).toInt()} min (pump manually stopped).",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = StatusWarn,
            modifier = Modifier.weight(1f)
          )
          Button(
            onClick = onResumeAuto,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(6.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.testTag("resume_auto_button")
          ) {
            Text("Resume", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
          }
        }
      }
    }
  }
}
