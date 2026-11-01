package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ai.YieldPredictorEngine
import com.example.data.model.CropGrowthStage
import com.example.data.model.SoilType
import com.example.data.model.YieldForecastInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SmartAgri", appName)
  }

  @Test
  fun `yield predictor generates valid commercial forecast`() {
    val input = YieldForecastInput(
      cropName = "Commercial Greenhouse Tomato",
      areaSqMeters = 1000.0,
      growthStage = CropGrowthStage.FLOWERING,
      soilType = SoilType.HYDRO_COCO,
      avgDayTempC = 25.0,
      avgNightTempC = 18.0,
      dailyLightIntegral = 22.0,
      avgSoilMoisturePct = 55.0,
      avgNutrientsPpm = 900.0
    )

    val result = YieldPredictorEngine.calculateForecast(input)
    assertTrue("Predicted yield should be positive", result.predictedYieldKg > 5000.0)
    assertTrue("Harvest window should be in future", result.harvestWindowStart > System.currentTimeMillis())
    assertTrue("Potential score should be high under optimal conditions", result.potentialScorePct >= 80)
  }
}
