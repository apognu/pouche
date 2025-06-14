package com.github.apognu.push.model

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity
data class Subscription(
  @PrimaryKey(autoGenerate = true) val id: Int,
  val slug: String
)

@Dao
interface SubscriptionDao {
  @Query("SELECT * FROM subscription")
  fun all(): Flow<List<Subscription>>

  @Query("SELECT * FROM subscription")
  fun allOnce(): List<Subscription>

  @Query("DELETE FROM subscription WHERE slug = :slug")
  fun delete(slug: String)

  @Insert
  fun insert(slug: Subscription)
}
