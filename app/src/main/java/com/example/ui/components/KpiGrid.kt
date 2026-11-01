package com.example.ui.components

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AgriHistoryPoint
import com.example.data.model.AgriStatus
import com.example.ui.theme.ColorFlow
import com.example.ui.theme.ColorNutrients
import com.example.ui.theme.ColorSoil
import com.example.ui.theme.ColorTank
import com.example.ui.theme.StatusCrit
import com.example.ui.theme.StatusIdle
import com.example.ui.theme.StatusInfo
import com.example.ui.theme.StatusOk
import com.example.ui.theme.StatusWarn
import kotlin.math.max
import kotlin.math.min

@Composable
fun KpiGrid(
  status: AgriStatus,
  history5m: List<AgriHistoryPoint>,
  onCardClick: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val c = status.cfg
  val n = status.nutr

  // Soil
  val soilStatus = when {
    status.soil < c.onBelow -> StatusWarn to "Below range"
    status.soil >= c.offAbove -> StatusInfo to "Wet"
    else -> StatusOk to "In range"
  }

  // Nutrients
  val foodStatus = when {
    n == null || !n.en -> StatusIdle to "Not enabled"
    n.low -> StatusWarn to "Low"
    !n.reliable -> StatusIdle to "On hold"
    n.ppm < c.nutrLowPpm * 1.5 -> StatusWarn to "Falling"
    else -> StatusOk to "Normal"
  }

  // Tank
  val tankStatus = when {
    status.tank < c.minTank -> StatusCrit to "Critical"
    status.tank < 35.0 -> StatusWarn to "Low"
    else -> StatusOk to "Normal"
  }

  // Flow
  val flowStatus = if (status.flow > 0.05) StatusInfo to "Flowing" else StatusIdle to "Idle"

  // Pump
  val pumpStatus = if (status.pump) StatusInfo to (if (status.mode == "auto") "Automatic" else "Manual") else StatusIdle to (if (status.mode == "auto") "Automatic" else "Manual")

  Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
    // Row 1: Soil, Nutrients
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      KpiCard(
        label = "Soil moisture",
        value = "${status.soil.toInt()}",
        unit = "%",
        statusColor = soilStatus.first,
        statusText = soilStatus.second,
        lineColor = ColorSoil,
        sparkPoints = history5m.map { it.soil },
        onClick = { onCardClick("soil") },
        testTag = "kpi_soil",
        modifier = Modifier.weight(1f)
      )
      KpiCard(
        label = "Nutrients",
        value = if (n?.en == true) "${n.ppm}" else "—",
        unit = if (n?.en == true) "ppm" else "",
        statusColor = foodStatus.first,
        statusText = foodStatus.second,
        lineColor = ColorNutrients,
        sparkPoints = history5m.mapNotNull { it.food },
        onClick = { onCardClick("food") },
        testTag = "kpi_nutrients",
        modifier = Modifier.weight(1f)
      )
    }

    // Row 2: Tank, Flow rate
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      KpiCard(
        label = "Tank level",
        value = "${status.tank.toInt()}",
        unit = "%",
        statusColor = tankStatus.first,
        statusText = tankStatus.second,
        lineColor = ColorTank,
        sparkPoints = history5m.map { it.tank },
        onClick = { onCardClick("tank") },
        testTag = "kpi_tank",
        modifier = Modifier.weight(1f)
      )
      KpiCard(
        label = "Flow rate",
        value = String.format("%.1f", status.flow),
        unit = "L/min",
        statusColor = flowStatus.first,
        statusText = flowStatus.second,
        lineColor = ColorFlow,
        sparkPoints = history5m.map { it.flow },
        onClick = { onCardClick("flow") },
        testTag = "kpi_flow",
        modifier = Modifier.weight(1f)
      )
    }

    // Row 3: Water used, Pump
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      KpiCard(
        label = "Water used",
        value = String.format("%.1f", status.total),
        unit = "L",
        statusColor = StatusIdle,
        statusText = "Total used",
        lineColor = ColorTank,
        sparkPoints = emptyList(),
        onClick = { onCardClick("flow") },
        testTag = "kpi_used",
        modifier = Modifier.weight(1f)
      )
      KpiCard(
        label = "Irrigation Pump",
        value = if (status.pump) "Running" else "Off",
        unit = "",
        statusColor = if (status.pump) StatusInfo else StatusIdle,
        statusText = pumpStatus.second,
        lineColor = StatusInfo,
        sparkPoints = emptyList(),
        onClick = { onCardClick("pump") },
        testTag = "kpi_pump",
        modifier = Modifier.weight(1f)
      )
    }
  }
}

@Composable
fun KpiCard(
  label: String,
  value: String,
  unit: String,
  statusColor: Color,
  statusText: String,
  lineColor: Color,
  sparkPoints: List<Double>,
  onClick: () -> Unit,
  testTag: String,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    modifier = modifier
      .clickable { onClick() }
      .testTag(testTag)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp)
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(4.dp))
      Row(verticalAlignment = Alignment.Bottom) {
        Text(
          text = value,
          style = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp
          ),
          color = MaterialTheme.colorScheme.onSurface
        )
        if (unit.isNotEmpty()) {
          Spacer(modifier = Modifier.width(3.dp))
          Text(
            text = unit,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 3.dp)
          )
        }
      }

      // Mini Sparkline
      if (sparkPoints.size >= 2) {
        Spacer(modifier = Modifier.height(6.dp))
        SparklineCanvas(
          points = sparkPoints,
          color = lineColor,
          modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
        )
      } else {
        Spacer(modifier = Modifier.height(8.dp))
      }

      Spacer(modifier = Modifier.height(4.dp))
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(7.dp)
            .clip(CircleShape)
            .background(statusColor)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = statusText,
          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
          color = statusColor
        )
      }
    }
  }
}

@Composable
fun SparklineCanvas(
  points: List<Double>,
  color: Color,
  modifier: Modifier = Modifier
) {
  Canvas(modifier = modifier) {
    if (points.size < 2) return@Canvas

    val minV = points.minOrNull() ?: 0.0
    val maxV = points.maxOrNull() ?: 100.0
    val span = max(0.1, maxV - minV)

    val path = Path()
    val width = size.width
    val height = size.height

    points.forEachIndexed { i, v ->
      val x = (i.toFloat() / (points.size - 1)) * width
      val y = height - ((v - minV) / span).toFloat() * (height - 4f) - 2f
      if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }

    drawPath(
      path = path,
      color = color,
      style = Stroke(width = 2.dp.toPx())
    )
  }
}
