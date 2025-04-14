package com.companies.smartwaterintake.domain.service
import androidx.room.Entity
import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.companies.smartwaterintake.data.Day
import com.companies.smartwaterintake.data.Milliliters
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json

interface HydrationHistoryStore {
    suspend fun setDay(day: Day)
    fun day(date: LocalDate): Flow<Day?>
    suspend fun days(
        startDateExclusive: LocalDate,
        pageSize: Int = 20
    ): List<Day>

    suspend fun delete(date: LocalDate)
    suspend fun clear()
}

class SqliteHydrationHistoryStore(
    context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : HydrationHistoryStore {

    private val database: DayDatabase = Room.databaseBuilder(
        context = context.applicationContext,
        klass = DayDatabase::class.java,
        name = DATABASE_NAME
    ).build()

    override suspend fun setDay(day: Day) {
        database.dayDao().upsert(day.toDTO())
    }

    override fun day(date: LocalDate): Flow<Day?> {
        return database.dayDao().get(date).map { it?.fromDTO() }
    }

    override suspend fun days(
        startDateExclusive: LocalDate,
        pageSize: Int
    ): List<Day> = withContext(ioDispatcher) {
        database.dayDao().getDescending(startDateExclusive, pageSize).map(DayDTO::fromDTO)
    }

    override suspend fun delete(date: LocalDate) = withContext(ioDispatcher) {
        val day = database.dayDao().get(date).firstOrNull() ?: return@withContext
        database.dayDao().delete(day)
    }

    override suspend fun clear() = withContext(ioDispatcher) {
        database.clearAllTables()
    }

    companion object {
        private const val DATABASE_NAME = "day-database"
    }
}

@Entity(
    tableName = "day",
    indices = [Index("date", unique = true)]
)
data class DayDTO(
    val date: LocalDate,
    val hydration: List<Day.Hydration>,
    val goalMilliliters: Int,
    @PrimaryKey val id: String
) : Comparable<DayDTO> {
    override fun compareTo(other: DayDTO): Int = date compareTo other.date
}

private fun DayDTO.fromDTO(): Day = Day(
    date = date,
    hydration = hydration,
    goal = Milliliters(goalMilliliters),
    id = id
)

private fun Day.toDTO(): DayDTO = DayDTO(
    date = date,
    hydration = hydration,
    goalMilliliters = goal.value,
    id = id
)

@Dao
interface DayDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(vararg days: DayDTO)

    @Query("SELECT * FROM day WHERE date = :date LIMIT 1")
    fun get(date: LocalDate): Flow<DayDTO?>

    @Query(
        "SELECT * FROM day WHERE date < :startDateExclusive ORDER BY date desc LIMIT :limit "
    )
    fun getDescending(startDateExclusive: LocalDate, limit: Int): List<DayDTO>

    @Delete
    suspend fun delete(day: DayDTO)
}

@Database(entities = [DayDTO::class], version = 1)
@TypeConverters(LocalDateConverter::class, DayHydrationConverter::class)
abstract class DayDatabase : RoomDatabase() {
    abstract fun dayDao(): DayDao
}

class LocalDateConverter {
    @TypeConverter
    fun fromLocalDate(date: LocalDate): Int = date.toEpochDays()

    @TypeConverter
    fun toLocalDate(epochDays: Int): LocalDate = LocalDate.fromEpochDays(epochDays)
}

class DayHydrationConverter {
    @TypeConverter
    fun fromDayHydration(dayHydration: List<Day.Hydration>): String {
        return json.encodeToString(dayHydration)
    }

    @TypeConverter
    fun toDayHydration(serialized: String): List<Day.Hydration> {
        return json.decodeFromString(serialized)
    }

    companion object {
        val json = Json
    }
}


