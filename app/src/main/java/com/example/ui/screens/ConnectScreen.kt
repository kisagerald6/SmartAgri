package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.example.ui.components.QrScannerDialog
import com.example.ui.theme.ActionGreen
import com.example.ui.theme.ForestDark
import com.example.ui.viewmodel.AgriViewModel

@Composable
fun ConnectScreen(
  viewModel: AgriViewModel,
  onConnected: () -> Unit,
  modifier: Modifier = Modifier
) {
  var ipInput by remember { mutableStateOf(viewModel.uiState.value.host) }
  var showQrScanner by remember { mutableStateOf(false) }

  if (showQrScanner) {
    QrScannerDialog(
      onDismiss = { showQrScanner = false },
      onIpScanned = { scannedIp ->
        ipInput = scannedIp
        viewModel.connectToHost(scannedIp, isDemo = false)
        onConnected()
      }
    )
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
  ) {
    // Top Hero Banner
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(ForestDark)
        .padding(24.dp)
    ) {
      Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Eco, contentDescription = null, tint = ActionGreen, modifier = Modifier.size(32.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "SmartAgri",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White
          )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = "Commercial Farm & Greenhouse Operations Console",
          style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
          color = Color.White
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "Real-time crop sensors, automated irrigation rules, AI pathology detection, and yield forecasting.",
          style = MaterialTheme.typography.bodyMedium,
          color = Color.White.copy(alpha = 0.8f)
        )
      }
    }

    // Connect Form
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text(
            text = "Connect to ESP32 Farm Controller",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Power on your ESP32 controller and enter the IP address shown on its display or assigned by your router:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(14.dp))

          OutlinedTextField(
            value = ipInput,
            onValueChange = { ipInput = it },
            label = { Text("Controller IP Address") },
            placeholder = { Text("192.168.1.50") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("controller_ip_input")
          )

          Spacer(modifier = Modifier.height(12.dp))

          // QR Code Scanner Button
          Button(
            onClick = { showQrScanner = true },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(44.dp)
              .testTag("scan_qr_button")
          ) {
            Icon(
              imageVector = Icons.Default.QrCodeScanner,
              contentDescription = "Scan QR code",
              tint = ActionGreen,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Scan Controller QR Code",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Button(
              onClick = {
                viewModel.connectToHost(ipInput, isDemo = false)
                onConnected()
              },
              colors = ButtonDefaults.buttonColors(containerColor = ActionGreen),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f).height(46.dp).testTag("connect_btn")
            ) {
              Text("Connect", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
              onClick = {
                viewModel.connectToHost(ipInput, isDemo = true)
                onConnected()
              },
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f).height(46.dp).testTag("view_demo_btn")
            ) {
              Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Launch Demo", fontWeight = FontWeight.SemiBold)
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Text(
            text = "No Wi-Fi router nearby? The controller creates its own access point called 'SmartAgri'. Join it on your device and connect using IP 192.168.4.1.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      // Feature Highlights
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("Commercial Platform Capabilities", fontWeight = FontWeight.Bold)

          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Sensors, contentDescription = null, tint = ActionGreen, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("6-in-1 Telemetry: Soil, Nutrients EC, Tank, Flow, Temp & DLI Light", fontSize = 13.sp)
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.WaterDrop, contentDescription = null, tint = ActionGreen, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Automated Irrigation & Pump Dry-Run Protection", fontSize = 13.sp)
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Eco, contentDescription = null, tint = ActionGreen, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("AI Crop Pest & Disease Diagnostic with Treatment Protocols", fontSize = 13.sp)
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = ActionGreen, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Predictive Harvest Yield Forecasting & Risk Mitigation", fontSize = 13.sp)
          }
        }
      }
    }
  }
}
