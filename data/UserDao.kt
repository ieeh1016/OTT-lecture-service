package tv.formuler.mytvonline.technic.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserDao {
    @Query("SELECT * FROM user")
    fun getAllUsers(): List<User>
    @Query("SELECT * FROM user WHERE id LIKE :id LIMIT 1")
    fun getUser(id: String): User
    @Insert
    fun insertUser(user: User)
    @Delete
    fun delete(user: User)
}