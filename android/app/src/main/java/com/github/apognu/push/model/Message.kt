package com.github.apognu.push.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Entity
data class Message(
  @PrimaryKey(autoGenerate = true) val id: Int,
  val uid: String,
  val title: String,
  val body: String,
  val banner: String,
  val date: String,
  val topic: String,
  val color: String
)

@Dao
interface MessageDao {
  @Query("SELECT * FROM message ORDER BY id DESC LIMIT 30")
  fun all(): Flow<List<Message>>

  @Query("SELECT * FROM message WHERE topic IN (:filters) ORDER BY id DESC LIMIT 30")
  fun ofSubscriptions(filters: List<String>): Flow<List<Message>>

  @Insert
  fun insert(message: Message)

  @Delete
  fun delete(message: Message)

  @Query("DELETE FROM message")
  fun deleteAll()
}
