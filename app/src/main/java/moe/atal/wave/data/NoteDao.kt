package moe.atal.wave.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Insert
    suspend fun insert(note: Note)

    @Delete
    suspend fun delete(note : Note)

    @Query("SELECT * FROM notes ORDER BY time DESC")
    fun getAll() : Flow<List<Note>>

    @Query("SELECT * FROM notes ORDER BY time DESC LIMIT :k")
    fun getRecentK(k : Long) : Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE time >= :time ORDER BY time DESC")
    fun getAfter(time : Long) : Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE time >= :start and time <= :end ORDER BY time DESC")
    fun getBetween(start : Long, end : Long) : Flow<List<Note>>

}