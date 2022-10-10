package tv.formuler.mytvonline.technic

import androidx.room.ColumnInfo
import androidx.room.Entity
import org.json.JSONObject

@Entity(primaryKeys = arrayOf("sCode","lCode","sSchool"))
data class CourseDBData(
    @ColumnInfo(name = "lDate") var lDate: String="",
    @ColumnInfo(name = "lCode") var lCode: Int=0,
    @ColumnInfo(name = "sSchool") var sSchool: String="",
    @ColumnInfo(name = "sCode") var sCode: String="",
    @ColumnInfo(name = "lProgress") var lProgress: Int=0,
    @ColumnInfo(name = "lDuration") var lDuration: Int=0,
    @ColumnInfo(name = "lHour") var lHour: String=""
    )
