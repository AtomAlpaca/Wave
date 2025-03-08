package moe.atal.wave.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id   : Long = 0,
    val time : Long,
    val mood : Long,
    val note : String

)