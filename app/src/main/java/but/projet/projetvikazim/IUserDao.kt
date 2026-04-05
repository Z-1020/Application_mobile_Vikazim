package but.projet.projetvikazim

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface IUserDao {
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUser(username: String): UserData?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserData)

    @Update
    suspend fun updateUser(user: UserData)

    @Query("SELECT * FROM users WHERE isModify = 1")
    suspend fun getDirtyUsers(): List<UserData>

    @Query("UPDATE users SET isModify = 0 WHERE username = :username")
    suspend fun markClean(username: String)

}