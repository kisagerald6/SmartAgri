package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StepperControl(
  value: Double,
  step: Double,
  minVal: Double,
  maxVal: Double,
  unit: String,
  onValueChange: (Double) -> Unit,
  testTagPrefix: String = "stepper",
  modifier: Modifier = Modifier
) {
  val formattedVal = if (step < 1.0) String.format("%.1f", value) else "${value.toInt()}"

  Row(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .background(MaterialTheme.colorScheme.surface)
      .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp)),
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Minus Button
    Box(
      modifier = Modifier
        .size(38.dp)
        .clickable(enabled = value > minVal) {
          val next = (value - step).coerceIn(minVal, maxVal)
          onValueChange(Math.round(next * 100.0) / 100.0)
        }
        .testTag("${testTagPrefix}_minus"),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "−",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = if (value > minVal) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
      )
    }

    // Value Display
    Box(
      modifier = Modifier
        .widthIn(min = 64.dp)
        .padding(horizontal = 4.dp),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "$formattedVal $unit",
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
      )
    }

    // Plus Button
    Box(
      modifier = Modifier
        .size(38.dp)
        .clickable(enabled = value < maxVal) {
          val next = (value + step).coerceIn(minVal, maxVal)
          onValueChange(Math.round(next * 100.0) / 100.0)
        }
        .testTag("${testTagPrefix}_plus"),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "+",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = if (value < maxVal) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
      )
    }
  }
}
