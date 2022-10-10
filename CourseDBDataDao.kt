package tv.formuler.mytvonline.technic

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CourseDBDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg dataSet: CourseDBData) // 가변인자

    @Query("SELECT * FROM courseDBdata WHERE sCode=:query and sSchool=:query2")
    suspend fun getCourseData(query: String , query2: String): List<CourseDBData>?


    @Query("SELECT * FROM courseDBdata")
    suspend fun getAllCourseData(): List<CourseDBData>?

    @Query("SELECT lProgress From courseDBdata WHERE lcode =:query")
    suspend fun getlProgress(query: Int): Int?

    @Query("SELECT lDuration From courseDBdata WHERE lcode =:query")
    suspend fun getlDuration(query: Int): Int?

    @Query("DELETE FROM courseDBdata")
    suspend fun clearAll()

}