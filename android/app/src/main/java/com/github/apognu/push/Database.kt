package com.github.apognu.push

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import com.github.apognu.push.model.Message
import com.github.apognu.push.model.MessageDao
import com.github.apognu.push.model.Subscription
import com.github.apognu.push.model.SubscriptionDao
import androidx.room.Database as IDatabase

@IDatabase(
  entities = [Subscription::class, Message::class],
  version = 4,
  exportSchema = false
)
abstract class Database : RoomDatabase() {
  companion object {
    private val MIGRATIONS: List<Migration> = listOf(
      Migrations.MIGRATION_1_2,
      Migrations.MIGRATION_2_3,
      Migrations.MIGRATION_3_4,
    )

    private var instance: Database? = null

    fun get(context: Context): Database {
      instance?.let {
        return it
      }

      @Suppress("SpreadOperator")
      return Room.databaseBuilder(context, Database::class.java, "pouche").addMigrations(*MIGRATIONS.toTypedArray()).build().also {
        instance = it
      }
    }
  }

  abstract fun subscriptions(): SubscriptionDao
  abstract fun messages(): MessageDao
}
