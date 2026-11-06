package com.kevv.stayfree

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,
    val startTime: Long,
    val costPerUnit: Double,
    val unitsPerDay: Double,
    val unitName: String,
    val bestStreakMs: Long = 0,
    val archived: Boolean = false
)

@Entity(tableName = "cravings")
data class Craving(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val time: Long,
    val intensity: Int,
    val trigger: String,
    val note: String,
    val resisted: Boolean
)

@Entity(tableName = "relapses")
data class Relapse(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val time: Long,
    val streakMs: Long,
    val trigger: String
)

@Dao
interface AppDao {
    @Query("SELECT * FROM habits WHERE archived = 0 ORDER BY id")
    fun habits(): Flow<List<Habit>>

    @Insert suspend fun insertHabit(h: Habit): Long
    @Update suspend fun updateHabit(h: Habit)

    @Query("UPDATE habits SET archived = 1 WHERE id = :id")
    suspend fun archiveHabit(id: Long)

    @Query("SELECT * FROM cravings WHERE habitId = :id ORDER BY time DESC")
    fun cravings(id: Long): Flow<List<Craving>>

    @Insert suspend fun insertCraving(c: Craving)
    @Delete suspend fun deleteCraving(c: Craving)

    @Query("SELECT * FROM relapses WHERE habitId = :id ORDER BY time DESC")
    fun relapses(id: Long): Flow<List<Relapse>>

    @Insert suspend fun insertRelapse(r: Relapse)
}

@Database(entities = [Habit::class, Craving::class, Relapse::class], version = 1, exportSchema = false)
abstract class AppDb : RoomDatabase() {
    abstract fun dao(): AppDao

    companion object {
        @Volatile private var instance: AppDb? = null
        fun get(ctx: Context): AppDb = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(ctx.applicationContext, AppDb::class.java, "stayfree.db")
                .build().also { instance = it }
        }
    }
}
