package tv.formuler.mytvonline.technic.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    @PrimaryKey val id: String,
    @ColumnInfo val sId: String,
    @ColumnInfo val sName: String,
    @ColumnInfo val gradeNumber: String,
    @ColumnInfo val classNumber: String,
    @ColumnInfo val studentNumber: String,
    @ColumnInfo val name: String
)
