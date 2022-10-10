package tv.formuler.mytvonline.technic

import androidx.room.Database
import androidx.room.RoomDatabase
import tv.formuler.mytvonline.technic.data.User
import tv.formuler.mytvonline.technic.data.UserDao

@Database(entities = arrayOf(User::class), version = 1)
abstract class TecDatabase: RoomDatabase() {
    companion object{
        const val DB_NAME= "TecDatabase"
    }
    abstract fun userDao(): UserDao
}