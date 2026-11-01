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
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.data.model.AgriAlert
import com.example.data.model.AlertLevel
import com.example.ui.theme.ActionGreen
import com.example.ui.theme.StatusCrit
import com.example.ui.theme.StatusInfo
import com.example.ui.theme.StatusOk
import com.example.ui.theme.StatusWarn
import com.example.ui.viewmodel.AgriViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AlertsScreen(
  viewModel: AgriViewModel,
  alerts: List<AgriAlert>,
  modifier: Modifier = Modifier
) {
  var selectedFilter by remember { mutableStateOf("all") } // "all", "crit", "warn", "info"

  val filteredAlerts = alerts.filter { alert ->
    when (selectedFilter) {
      "crit" -> alert.level == AlertLevel.CRITICAL
      "warn" -> alert.level == AlertLevel.WARNING
      "info" -> alert.level == AlertLevel.INFO
      else -> true
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 14.dp, vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {

    // 1. Event Log Card with Filters & Actions
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
            text = "Event log & alerts",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )

          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Button(
              onClick = { viewModel.markAllAlertsRead() },
              shape = RoundedCornerShape(6.dp),
              colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
              contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
              modifier = Modifier.height(32.dp).testTag("mark_read_btn")
            ) {
              Text("Mark read", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
            }
            Button(
              onClick = { viewModel.clearAlerts() },
              shape = RoundedCornerShape(6.dp),
              colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
              contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
              modifier = Modifier.height(32.dp).testTag("clear_alerts_btn")
            ) {
              Text("Clear", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Severity Filter Pills
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
        ) {
          listOf(
            "all" to "All",
            "crit" to "Critical",
            "warn" to "Warning",
            "info" to "Info"
          ).forEach { (f, label) ->
            val isSelected = selectedFilter == f
            Box(
              modifier = Modifier
                .weight(1f)
                .clickable { selectedFilter = f }
                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                .padding(vertical = 6.dp),
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

        // Alerts List
        if (filteredAlerts.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(Icons.Default.DoneAll, contentDescription = null, tint = StatusOk, modifier = Modifier.size(32.dp))
              Spacer(modifier = Modifier.height(4.dp))
              Text("No events logged for this filter", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
        } else {
          filteredAlerts.forEach { alert ->
            val (badgeColor, badgeBg) = when (alert.level) {
              AlertLevel.CRITICAL -> StatusCrit to StatusCrit.copy(alpha = 0.12f)
              AlertLevel.WARNING -> StatusWarn to StatusWarn.copy(alpha = 0.12f)
              AlertLevel.INFO -> StatusInfo to StatusInfo.copy(alpha = 0.12f)
            }
            val timeFmt = SimpleDateFormat("MMM d, HH:mm:ss", Locale.getDefault()).format(Date(alert.timestamp))

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
              verticalAlignment = Alignment.Top
            ) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(badgeBg)
                  .border(1.dp, badgeColor.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Text(
                  text = alert.level.name,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = badgeColor
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = alert.title,
                  style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = alert.message,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = timeFmt,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            )
          }
        }
      }
    }

    // 2. Phone Push Alerts (ntfy) Configuration Card
    Card(
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = ActionGreen)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Phone push notifications",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "Receive instant push alerts on your phone whenever critical faults occur (empty tank, blocked line, or nutrient deficiency) even when the app is closed.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))

        listOf(
          "1" to ("Install the free ntfy app" to "Available on Google Play or iOS App Store."),
          "2" to ("Subscribe to a private farm topic" to "Choose a unique topic name (e.g. smartagri-greenhouse-zone1)."),
          "3" to ("Configure on controller" to "Set NTFY_TOPIC in the ESP32 firmware sketch.")
        ).forEach { (num, text) ->
          Row(modifier = Modifier.padding(vertical = 3.dp)) {
            Box(
              modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
              contentAlignment = Alignment.Center
            ) {
              Text(num, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(text.first, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
              Text(text.second, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))
  }
}
