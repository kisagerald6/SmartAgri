package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AgriDao {
  // Alerts
  @Query("SELECT * FROM agri_alerts ORDER BY timestamp DESC LIMIT 200")
  fun getAllAlerts(): Flow<List<AgriAlertEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAlert(alert: AgriAlertEntity): Long

  @Query("UPDATE agri_alerts SET isRead = 1")
  suspend fun markAllAlertsRead()

  @Query("DELETE FROM agri_alerts")
  suspend fun clearAlerts()

  // Zones
  @Query("SELECT * FROM agri_zones ORDER BY name ASC")
  fun getAllZones(): Flow<List<FarmZoneEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdateZone(zone: FarmZoneEntity)

  @Query("UPDATE agri_zones SET isSelected = (zoneId = :selectedId)")
  suspend fun selectZone(selectedId: String)

  @Query("DELETE FROM agri_zones WHERE zoneId = :zoneId")
  suspend fun deleteZone(zoneId: String)

  // Pest & Disease Scans
  @Query("SELECT * FROM pest_scans ORDER BY timestamp DESC LIMIT 50")
  fun getAllPestScans(): Flow<List<PestScanEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPestScan(scan: PestScanEntity): Long

  @Query("DELETE FROM pest_scans")
  suspend fun clearPestScans()

  // Yield Forecasts
  @Query("SELECT * FROM yield_forecasts ORDER BY timestamp DESC LIMIT 20")
  fun getAllYieldForecasts(): Flow<List<YieldForecastEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertYieldForecast(forecast: YieldForecastEntity): Long
}
