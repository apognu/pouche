package com.github.apognu.push

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrations {
  val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
      database.execSQL("ALTER TABLE Message ADD COLUMN color TEXT NOT NULL DEFAULT '';")
    }
  }
}
