package tv.formuler.mytvonline.technic

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

data class LoginRequest(
    val sSchool : String,
    val sName : String,
    val sGrade : String,
    val sClass : String,
    val sNumber : String
)

data class LoginResponse(
    @SerializedName("loginCode")
    val loginCode : Int,
    @SerializedName("sCode")
    val sCode : Int,
    @SerializedName("sSchool")
    val sSchool : String
)

data class WeekAllLessonRequest(
    val sSchool : String?,
    val sCode : String?
)

data class WeekAllLessonResponse(
    @SerializedName("classCode")
    val classCode : String?,
    @SerializedName("rows")
    val lessonInfoList : List<LessonInfo>?
)

data class SetProgressRequest(
    val sSchool : String?,
    val sCode : String?,
    val lCode : Int? = null,
    val runningTime : Int? = null
)

data class SetProgressResponse(
    @SerializedName("successCode")
    val successCode : Int
)

data class GetProgressRequest(
    val sSchool : String?,
    val sCode : String?,
    val lCode : Int?
)

data class GetProgressResonse(
    @SerializedName("runningTime")
    val runningTime : String
)

data class FeedbackRequest(
    val sSchool : String?,
    val sCode : String?,
    val lCode : Int?,
    val fContents : String?
)

data class FeedbackResponse(
    @SerializedName("successCode")
    val successCode : Int
)

@Parcelize
data class LessonInfo(
    val lCode : Int,
    val lName : String,
    val lContent : String,
    val lSubCode : Int,
    val lSubName : String,
    val lURL : String,
    val lDate : String,
    val lHour : String,
    val lStartTime : String,
    val lEndTime : String,
    val classCode : Int,
    val teacherCode : Int,
    val lProgress : Int
) : Parcelable