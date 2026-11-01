package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
  entities = [
    AgriAlertEntity::class,
    FarmZoneEntity::class,
    PestScanEntity::class,
    YieldForecastEntity::class
  ],
  version = 2,
  exportSchema = false
)
abstract class AgriDatabase : RoomDatabase() {
  abstract fun agriDao(): AgriDao

  companion object {
    @Volatile
    private var INSTANCE: AgriDatabase? = null

    fun getDatabase(context: Context): AgriDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AgriDatabase::class.java,
          "smartagri_db"
        )
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
