package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.ui.theme.ActionGreen
import com.example.ui.theme.ForestDark
import com.example.ui.theme.StatusOk
import com.example.ui.theme.StatusWarn

@Composable
fun QrScannerDialog(
  onDismiss: () -> Unit,
  onIpScanned: (String) -> Unit
) {
  val context = LocalContext.current
  var hasCameraPermission by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    )
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    hasCameraPermission = isGranted
  }

  LaunchedEffect(Unit) {
    if (!hasCameraPermission) {
      permissionLauncher.launch(Manifest.permission.CAMERA)
    }
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.85f))
        .padding(20.dp)
        .testTag("qr_scanner_dialog"),
      contentAlignment = Alignment.Center
    ) {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          // Top Header
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = ActionGreen)
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Scan Controller QR Code",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
            }
            IconButton(onClick = onDismiss) {
              Icon(Icons.Default.Close, contentDescription = "Close")
            }
          }

          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = "Point camera at the QR code displayed on the ESP32 OLED screen or controller casing.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(16.dp))

          // Scanning Viewfinder Box with Laser Animation
          val infiniteTransition = rememberInfiniteTransition(label = "laser_scan")
          val laserY by infiniteTransition.animateFloat(
            initialValue = 0.1f,
            targetValue = 0.9f,
            animationSpec = infiniteRepeatable(
              animation = tween(1200, easing = LinearEasing),
              repeatMode = RepeatMode.Reverse
            ),
            label = "laser_y"
          )

          Box(
            modifier = Modifier
              .size(220.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(Color(0xFF14201A))
              .border(2.dp, ActionGreen, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
          ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
              val w = size.width
              val h = size.height

              // Draw Reticle Corners
              val cornerLen = 28.dp.toPx()
              val strokeW = 4.dp.toPx()
              val cColor = Color(0xFF2FA46A)

              // Top-left
              drawLine(cColor, Offset(10f, 10f), Offset(10f + cornerLen, 10f), strokeW)
              drawLine(cColor, Offset(10f, 10f), Offset(10f, 10f + cornerLen), strokeW)
              // Top-right
              drawLine(cColor, Offset(w - 10f, 10f), Offset(w - 10f - cornerLen, 10f), strokeW)
              drawLine(cColor, Offset(w - 10f, 10f), Offset(w - 10f, 10f + cornerLen), strokeW)
              // Bottom-left
              drawLine(cColor, Offset(10f, h - 10f), Offset(10f + cornerLen, h - 10f), strokeW)
              drawLine(cColor, Offset(10f, h - 10f), Offset(10f, h - 10f - cornerLen), strokeW)
              // Bottom-right
              drawLine(cColor, Offset(w - 10f, h - 10f), Offset(w - 10f - cornerLen, h - 10f), strokeW)
              drawLine(cColor, Offset(w - 10f, h - 10f), Offset(w - 10f, h - 10f - cornerLen), strokeW)

              // Moving Laser Beam
              val currentY = h * laserY
              drawLine(
                color = Color(0xFF4CC38A),
                start = Offset(20f, currentY),
                end = Offset(w - 20f, currentY),
                strokeWidth = 2.5.dp.toPx()
              )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                imageVector = Icons.Default.QrCode,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.4f),
                modifier = Modifier.size(64.dp)
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = if (hasCameraPermission) "Align QR within frame" else "Camera permission required",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 11.sp
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Quick Simulation / Optical Barcode Presets
          Text(
            text = "Or tap detected controller QR code to auto-connect:",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(8.dp))

          Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            listOf(
              "192.168.1.50" to "Greenhouse Main Wi-Fi LAN",
              "192.168.4.1" to "SmartAgri AP Direct (No Router)",
              "10.0.0.120" to "Commercial Farm Subnet"
            ).forEachIndexed { idx, (ip, desc) ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .background(MaterialTheme.colorScheme.surfaceVariant)
                  .clickable {
                    onIpScanned(ip)
                    onDismiss()
                  }
                  .padding(horizontal = 12.dp, vertical = 8.dp)
                  .testTag("scan_preset_$idx"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(ip, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                  Text(desc, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(ActionGreen.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                  Text("Use IP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ActionGreen)
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
            Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
      }
    }
  }
}
