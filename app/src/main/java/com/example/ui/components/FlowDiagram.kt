package com.example.ui.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AgriStatus
import com.example.ui.theme.ColorFlow
import com.example.ui.theme.ColorSoil
import com.example.ui.theme.ColorTank
import com.example.ui.theme.StatusIdle
import com.example.ui.theme.StatusInfo

@Composable
fun FlowDiagram(
  status: AgriStatus,
  onNodeClick: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val isRunning = status.pump

  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    modifier = modifier
      .fillMaxWidth()
      .testTag("flow_diagram_card")
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Irrigation flow circuit",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = if (isRunning) "Active Flowing" else "Idle Circuit",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
          color = if (isRunning) StatusInfo else MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Flow Pipeline Nodes
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Node 1: Water Tank
        FlowNodeItem(
          label = "Water Tank",
          value = "${status.tank.toInt()}%",
          progress = (status.tank / 100.0).toFloat().coerceIn(0f, 1f),
          progressColor = ColorTank,
          onClick = { onNodeClick("tank") },
          modifier = Modifier.fillMaxWidth()
        )

        // Pipe 1
        FlowPipeConnector(isRunning = isRunning)

        // Node 2: Pump
        FlowPumpNode(
          isRunning = isRunning,
          onClick = { onNodeClick("pump") },
          modifier = Modifier.fillMaxWidth()
        )

        // Pipe 2
        FlowPipeConnector(isRunning = isRunning)

        // Node 3: Flow Meter
        FlowNodeItem(
          label = "Flow Meter",
          value = "${String.format("%.1f", status.flow)} L/min",
          progress = (status.flow / 4.0).toFloat().coerceIn(0f, 1f),
          progressColor = ColorFlow,
          onClick = { onNodeClick("flow") },
          modifier = Modifier.fillMaxWidth()
        )

        // Pipe 3
        FlowPipeConnector(isRunning = isRunning)

        // Node 4: Field Soil
        FlowNodeItem(
          label = "Field Soil Beds",
          value = "${status.soil.toInt()}%",
          progress = (status.soil / 100.0).toFloat().coerceIn(0f, 1f),
          progressColor = ColorSoil,
          onClick = { onNodeClick("soil") },
          modifier = Modifier.fillMaxWidth()
        )
      }
    }
  }
}

@Composable
fun FlowNodeItem(
  label: String,
  value: String,
  progress: Float,
  progressColor: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(10.dp))
      .background(MaterialTheme.colorScheme.surfaceVariant)
      .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
      .clickable { onClick() }
      .padding(horizontal = 14.dp, vertical = 10.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurface
      )
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.width(90.dp)) {
          LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
              .fillMaxWidth()
              .height(8.dp)
              .clip(RoundedCornerShape(4.dp)),
            color = progressColor,
            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = value,
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface
        )
      }
    }
  }
}

@Composable
fun FlowPumpNode(
  isRunning: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(10.dp))
      .background(MaterialTheme.colorScheme.surfaceVariant)
      .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
      .clickable { onClick() }
      .padding(horizontal = 14.dp, vertical = 10.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(12.dp)
            .clip(CircleShape)
            .background(if (isRunning) StatusInfo else StatusIdle)
            .border(2.dp, if (isRunning) StatusInfo.copy(alpha = 0.4f) else Color.Transparent, CircleShape)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = "Irrigation Pump",
          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
          color = MaterialTheme.colorScheme.onSurface
        )
      }
      Text(
        text = if (isRunning) "Running" else "Standby",
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.Bold,
          color = if (isRunning) StatusInfo else MaterialTheme.colorScheme.onSurfaceVariant
        )
      )
    }
  }
}

@Composable
fun FlowPipeConnector(
  isRunning: Boolean,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "pipe_pulse")
  val pulseOffset by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 800, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "pulse_offset"
  )

  Canvas(
    modifier = modifier
      .width(28.dp)
      .height(20.dp)
  ) {
    val midX = size.width / 2f

    // Background Pipe
    drawLine(
      color = Color(0xFFD6DDD7).copy(alpha = 0.7f),
      start = Offset(midX, 0f),
      end = Offset(midX, size.height),
      strokeWidth = 6.dp.toPx()
    )

    if (isRunning) {
      // Flowing fluid pulses
      val y = pulseOffset * size.height
      drawCircle(
        color = ColorTank,
        radius = 4.dp.toPx(),
        center = Offset(midX, y)
      )
    }
  }
}
