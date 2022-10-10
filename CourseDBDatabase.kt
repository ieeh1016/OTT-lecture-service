package tv.formuler.mytvonline.technic

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CourseDBData::class], version = 1)
abstract class CourseDBDatabase : RoomDatabase() {
    abstract fun CourseDBDataDao(): CourseDBDataDao
}