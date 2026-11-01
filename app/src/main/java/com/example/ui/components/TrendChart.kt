package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AgriConfig
import com.example.data.model.AgriHistoryPoint
import com.example.ui.theme.ColorFlow
import com.example.ui.theme.ColorNutrients
import com.example.ui.theme.ColorSoil
import com.example.ui.theme.ColorTank
import com.example.ui.theme.StatusIdle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max

@Composable
fun TrendChart(
  historyPoints: List<AgriHistoryPoint>,
  activeKey: String, // "soil", "food", "tank", "flow"
  activeRange: String, // "5m", "1h", "24h"
  config: AgriConfig,
  onKeyChange: (String) -> Unit,
  onRangeChange: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  var scrubX by remember { mutableStateOf<Float?>(null) }
  var scrubPoint by remember { mutableStateOf<AgriHistoryPoint?>(null) }

  val (lineColor, maxScale, unit, thresholds) = when (activeKey) {
    "food" -> {
      val maxVal = max(config.nutrTarget * 1.3, (historyPoints.mapNotNull { it.food }.maxOrNull() ?: 1000.0) * 1.1)
      Quad(ColorNutrients, maxVal, " ppm", listOf(config.nutrLowPpm to "Alert ${config.nutrLowPpm.toInt()}"))
    }
    "tank" -> Quad(ColorTank, 100.0, "%", listOf(config.minTank to "Pause ${config.minTank.toInt()}%"))
    "flow" -> {
      val maxVal = max(4.0, (historyPoints.map { it.flow }.maxOrNull() ?: 0.0) * 1.2)
      Quad(ColorFlow, maxVal, " L/min", emptyList())
    }
    else -> Quad(
      ColorSoil,
      100.0,
      "%",
      listOf(
        config.onBelow to "Start ${config.onBelow.toInt()}%",
        config.offAbove to "Stop ${config.offAbove.toInt()}%"
      )
    )
  }

  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    modifier = modifier
      .fillMaxWidth()
      .testTag("trend_chart_card")
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      // Header and Key Selectors
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Telemetry trends",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface
        )

        // Range Pills: 5m / 1h / 24h
        Row(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
        ) {
          listOf("5m" to "5 min", "1h" to "1 h", "24h" to "24 h").forEach { (code, label) ->
            val isSelected = activeRange == code
            Box(
              modifier = Modifier
                .clickable { onRangeChange(code) }
                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Metric Switcher Tabs
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant)
          .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
      ) {
        listOf(
          "soil" to "Soil",
          "food" to "Nutrients",
          "tank" to "Tank",
          "flow" to "Flow"
        ).forEach { (key, title) ->
          val isSelected = activeKey == key
          Box(
            modifier = Modifier
              .weight(1f)
              .clickable { onKeyChange(key) }
              .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
              .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = title,
              fontSize = 12.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Scrubbing Readout Banner
      if (scrubPoint != null) {
        val p = scrubPoint!!
        val v = when (activeKey) {
          "food" -> p.food?.let { "${it.toInt()}" } ?: "—"
          "tank" -> "${p.tank.toInt()}"
          "flow" -> String.format("%.1f", p.flow)
          else -> "${p.soil.toInt()}"
        }
        val timeFmt = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(p.timestamp))

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(lineColor.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Scrubbed reading: $v$unit",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = lineColor
          )
          Text(
            text = timeFmt,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        Spacer(modifier = Modifier.height(6.dp))
      }

      // Interactive Canvas Chart
      val outlineCol = MaterialTheme.colorScheme.outlineVariant
      val textCol = MaterialTheme.colorScheme.onSurfaceVariant

      Canvas(
        modifier = Modifier
          .fillMaxWidth()
          .height(160.dp)
          .pointerInput(historyPoints, activeKey) {
            detectTapGestures(
              onPress = { offset ->
                scrubX = offset.x
                scrubPoint = findClosestPoint(offset.x, size.width.toFloat(), historyPoints)
              }
            )
          }
          .pointerInput(historyPoints, activeKey) {
            detectDragGestures(
              onDrag = { change, _ ->
                scrubX = change.position.x
                scrubPoint = findClosestPoint(change.position.x, size.width.toFloat(), historyPoints)
              },
              onDragEnd = {
                scrubX = null
                scrubPoint = null
              }
            )
          }
      ) {
        val width = size.width
        val height = size.height

        // Horizontal Gridlines
        for (i in 0..3) {
          val y = (height / 3f) * i
          drawLine(
            color = outlineCol.copy(alpha = 0.4f),
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = 1.dp.toPx()
          )
        }

        // Draw Threshold Guidelines
        thresholds.forEach { (threshVal, label) ->
          val y = height - (threshVal / maxScale).toFloat() * height
          drawLine(
            color = textCol.copy(alpha = 0.6f),
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = 1.5.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
          )
        }

        val pts = historyPoints.filter {
          when (activeKey) {
            "food" -> it.food != null
            else -> true
          }
        }

        if (pts.size >= 2) {
          val strokePath = Path()
          val fillPath = Path()

          pts.forEachIndexed { i, pt ->
            val v = when (activeKey) {
              "food" -> pt.food ?: 0.0
              "tank" -> pt.tank
              "flow" -> pt.flow
              else -> pt.soil
            }
            val x = (i.toFloat() / (pts.size - 1)) * width
            val y = height - (v / maxScale).toFloat().coerceIn(0f, 1f) * (height - 8f) - 4f

            if (i == 0) {
              strokePath.moveTo(x, y)
              fillPath.moveTo(x, height)
              fillPath.lineTo(x, y)
            } else {
              strokePath.lineTo(x, y)
              fillPath.lineTo(x, y)
            }
          }

          fillPath.lineTo(width, height)
          fillPath.close()

          // Draw Fill Gradient
          drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
              colors = listOf(lineColor.copy(alpha = 0.35f), lineColor.copy(alpha = 0.02f)),
              startY = 0f,
              endY = height
            )
          )

          // Draw Stroke Curve
          drawPath(
            path = strokePath,
            color = lineColor,
            style = Stroke(width = 2.5.dp.toPx())
          )

          // Draw Scrub Indicator Line
          scrubX?.let { sx ->
            val clampedX = sx.coerceIn(0f, width)
            drawLine(
              color = lineColor,
              start = Offset(clampedX, 0f),
              end = Offset(clampedX, height),
              strokeWidth = 1.5.dp.toPx(),
              pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            )
          }
        }
      }
    }
  }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

private fun findClosestPoint(
  xPos: Float,
  totalWidth: Float,
  points: List<AgriHistoryPoint>
): AgriHistoryPoint? {
  if (points.isEmpty()) return null
  val ratio = (xPos / totalWidth).coerceIn(0f, 1f)
  val index = (ratio * (points.size - 1)).toInt()
  return points.getOrNull(index)
}
