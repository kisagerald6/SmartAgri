package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlertLevel
import com.example.data.model.CropGrowthStage
import com.example.data.model.SoilType
import com.example.ui.components.StepperControl
import com.example.ui.theme.ActionGreen
import com.example.ui.theme.ColorSoil
import com.example.ui.theme.ColorTank
import com.example.ui.theme.StatusCrit
import com.example.ui.theme.StatusOk
import com.example.ui.theme.StatusWarn
import com.example.ui.viewmodel.AgriViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun YieldForecastScreen(
  viewModel: AgriViewModel,
  modifier: Modifier = Modifier
) {
  val input by viewModel.yieldInput.collectAsState()
  val result by viewModel.yieldResult.collectAsState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 14.dp, vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {

    // 1. Title Banner
    Card(
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Insights, contentDescription = null, tint = ActionGreen, modifier = Modifier.size(28.dp))
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "Predictive Crop Yield Forecasting",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Agronomic simulation model combining weather, soil & phenology",
              style = MaterialTheme.typography.labelSmall,
              color = ActionGreen
            )
          }
        }
      }
    }

    // 2. Yield Forecast Primary Prediction Card
    if (result != null) {
      val r = result!!
      val harvestStartStr = SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(r.harvestWindowStart))
      val harvestEndStr = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(r.harvestWindowEnd))

      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(2.dp, ActionGreen.copy(alpha = 0.8f)),
        modifier = Modifier.fillMaxWidth().testTag("yield_prediction_card")
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Estimated Commercial Yield",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Row(verticalAlignment = Alignment.Bottom) {
                Text(
                  text = String.format("%,.0f", r.predictedYieldKg),
                  style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                  color = ActionGreen
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "kg total",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.padding(bottom = 6.dp)
                )
              }
            }

            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(ActionGreen.copy(alpha = 0.15f))
                .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Text(
                text = "${String.format("%.1f", r.predictedYieldPerSqM)} kg/m²",
                fontWeight = FontWeight.Bold,
                color = ActionGreen,
                fontSize = 14.sp
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Potential Score Progress Bar
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "Yield Potential Index",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = "${r.potentialScorePct}% of theoretical max",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = ActionGreen
            )
          }

          Spacer(modifier = Modifier.height(4.dp))
          LinearProgressIndicator(
            progress = { r.potentialScorePct / 100f },
            modifier = Modifier
              .fillMaxWidth()
              .height(8.dp)
              .clip(RoundedCornerShape(4.dp)),
            color = ActionGreen,
            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
          )

          Spacer(modifier = Modifier.height(14.dp))

          // Harvest Window
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant)
              .padding(12.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.CalendarToday, contentDescription = null, tint = ActionGreen, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "Projected Harvest Window",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = "$harvestStartStr – $harvestEndStr",
                  style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onSurface
                )
              }

              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(MaterialTheme.colorScheme.primaryContainer)
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text(
                  text = "~${r.daysUntilHarvest} days to harvest",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              }
            }
          }
        }
      }

      // 3. Risk Factors & Optimization Levers
      if (r.riskFactors.isNotEmpty()) {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Environmental Yield Restraints Detected",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))

            r.riskFactors.forEach { factor ->
              val sevColor = when (factor.severity) {
                AlertLevel.CRITICAL -> StatusCrit
                AlertLevel.WARNING -> StatusWarn
                AlertLevel.INFO -> StatusOk
              }

              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .background(sevColor.copy(alpha = 0.08f))
                  .border(1.dp, sevColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                  .padding(10.dp)
              ) {
                Column {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(factor.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = sevColor)
                    Text("–${factor.impactPct.toInt()}% yield", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = sevColor)
                  }
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(factor.mitigationRecommendation, style = MaterialTheme.typography.bodySmall)
                }
              }
              Spacer(modifier = Modifier.height(8.dp))
            }
          }
        }
      }

      // 4. Action Items to Maximize Yield
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Agronomic Optimization Actions",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(8.dp))

          r.actionItems.forEach { action ->
            Row(modifier = Modifier.padding(vertical = 3.dp)) {
              Text("✓ ", fontWeight = FontWeight.Bold, color = ActionGreen)
              Text(action, style = MaterialTheme.typography.bodySmall)
            }
          }
        }
      }
    }

    // 5. Model Input Variables Card
    Card(
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text(
          text = "Model Parameters & Farm Conditions",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Crop Selection Pills
        Text("Crop Variety", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(4.dp))
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf(
            "Commercial Greenhouse Tomato",
            "Sweet Bell Pepper",
            "English Cucumber",
            "Butterhead Lettuce",
            "Albion Strawberry"
          ).forEach { crop ->
            val isSelected = input.cropName == crop
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                .clickable { viewModel.updateYieldInput(cropName = crop) }
                .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Text(
                text = crop,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Growth Stage Selection Pills
        Text("Current Phenological Growth Stage", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(4.dp))
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          CropGrowthStage.entries.forEach { stage ->
            val isSelected = input.growthStage == stage
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSelected) ActionGreen else MaterialTheme.colorScheme.surfaceVariant)
                .clickable { viewModel.updateYieldInput(growthStage = stage) }
                .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Text(
                text = stage.displayName,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Soil / Substrate Type
        Text("Soil & Growing Substrate", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(4.dp))
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          SoilType.entries.forEach { soil ->
            val isSelected = input.soilType == soil
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSelected) ColorSoil else MaterialTheme.colorScheme.surfaceVariant)
                .clickable { viewModel.updateYieldInput(soilType = soil) }
                .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Text(
                text = soil.displayName,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sliders for Microclimate Tuning
        Text("Average Day Temperature (${input.avgDayTempC.toInt()}°C)", style = MaterialTheme.typography.labelSmall)
        Slider(
          value = input.avgDayTempC.toFloat(),
          onValueChange = { viewModel.updateYieldInput(avgDayTempC = it.toDouble()) },
          valueRange = 15f..38f,
          colors = SliderDefaults.colors(thumbColor = ActionGreen, activeTrackColor = ActionGreen)
        )

        Text("Average Night Temperature (${input.avgNightTempC.toInt()}°C)", style = MaterialTheme.typography.labelSmall)
        Slider(
          value = input.avgNightTempC.toFloat(),
          onValueChange = { viewModel.updateYieldInput(avgNightTempC = it.toDouble()) },
          valueRange = 10f..25f,
          colors = SliderDefaults.colors(thumbColor = ActionGreen, activeTrackColor = ActionGreen)
        )

        Text("Daily Light Integral (${String.format("%.1f", input.dailyLightIntegral)} mol/m²/d)", style = MaterialTheme.typography.labelSmall)
        Slider(
          value = input.dailyLightIntegral.toFloat(),
          onValueChange = { viewModel.updateYieldInput(lightDli = it.toDouble()) },
          valueRange = 8f..35f,
          colors = SliderDefaults.colors(thumbColor = Color(0xFFD69E2E), activeTrackColor = Color(0xFFD69E2E))
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))
  }
}
