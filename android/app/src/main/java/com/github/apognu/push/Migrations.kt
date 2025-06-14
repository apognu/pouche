package com.github.apognu.push

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrations {
  val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
      database.execSQL("ALTER TABLE Message ADD COLUMN color TEXT NOT NULL DEFAULT '';")
    }
  }

  val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
      database.execSQL("ALTER TABLE Message ADD COLUMN emoji TEXT NOT NULL DEFAULT '';")
    }
  }

  val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
      database.execSQL("ALTER TABLE Message ADD COLUMN markdown INT NOT NULL DEFAULT 0;")
    }
  }
}
