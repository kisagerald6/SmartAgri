package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import com.example.ui.theme.ActionGreen
import com.example.ui.theme.StatusCrit
import com.example.ui.viewmodel.AgriViewModel
import com.example.ui.viewmodel.SmartAgriUiState

@Composable
fun SettingsScreen(
  viewModel: AgriViewModel,
  uiState: SmartAgriUiState,
  onNavigateToConnect: () -> Unit,
  modifier: Modifier = Modifier
) {
  var siteNameInput by remember { mutableStateOf(uiState.siteName) }
  val s = uiState.status
  val c = s.cfg

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 14.dp, vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {

    // 1. Site Name Card
    Card(
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text("Farm & Facility Name", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
          value = siteNameInput,
          onValueChange = {
            siteNameInput = it
            viewModel.setSiteName(it)
          },
          label = { Text("Site display name") },
          modifier = Modifier.fillMaxWidth().testTag("site_name_input"),
          singleLine = true
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Shown in the top operations bar and export telemetry logs.",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    // 2. Controller Connection Card
    Card(
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Router, contentDescription = null, tint = ActionGreen)
          Spacer(modifier = Modifier.width(8.dp))
          Text("ESP32 Hardware Controller", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }
        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = if (uiState.isDemo) {
            "Currently running in autonomous Farm Simulation Mode."
          } else {
            "Connected to ${uiState.host} · Signal ${s.rssi} dBm · Uptime ${s.uptime / 3600} hours"
          },
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          Button(
            onClick = onNavigateToConnect,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ActionGreen)
          ) {
            Text("Switch Controller / Demo")
          }
        }
      }
    }

    // 3. Soil Sensor Calibration Card
    Card(
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Build, contentDescription = null, tint = ActionGreen)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Calibrate Soil Moisture Probe", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "Dry calibration reads ${c.soilDry.toInt()}, wet reads ${c.soilWet.toInt()}. Live sensor ADC reads ${s.soilRaw} now.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Step 1: Dry in air
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text("1. Sensor in dry air", fontWeight = FontWeight.SemiBold)
            Text("Keep probe dry and clear of hands", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          Button(
            onClick = { viewModel.calibrate("dry") },
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.testTag("cal_dry_btn")
          ) {
            Text("Record dry point", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Step 2: Wet in water
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text("2. Sensor in water cup", fontWeight = FontWeight.SemiBold)
            Text("Submerged to the marked line", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          Button(
            onClick = { viewModel.calibrate("wet") },
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.testTag("cal_wet_btn")
          ) {
            Text("Record wet point", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
          }
        }
      }
    }

    // 4. Device Preferences Card
    Card(
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Palette, contentDescription = null, tint = ActionGreen)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Device & Display Preferences", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Theme Mode Selector
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text("Appearance", fontWeight = FontWeight.SemiBold)
            Text("Light is optimized for outdoor sunlight", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }

          Row(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant)
              .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
          ) {
            listOf("light" to "Light", "dark" to "Dark", "auto" to "Auto").forEach { (m, label) ->
              val isSelected = uiState.themeMode == m
              Box(
                modifier = Modifier
                  .clickable { viewModel.setThemeMode(m) }
                  .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                  .padding(horizontal = 10.dp, vertical = 6.dp)
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
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Sound & Vibration Switch
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text("Haptic & Sound Alerts", fontWeight = FontWeight.SemiBold)
            Text("Vibrate phone when critical events occur", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          Switch(
            checked = uiState.soundVibrationEnabled,
            onCheckedChange = { viewModel.setSoundVibration(it) },
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ActionGreen)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))
  }
}
