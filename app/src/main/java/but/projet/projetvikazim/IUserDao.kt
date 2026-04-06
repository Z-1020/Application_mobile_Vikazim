package but.projet.projetvikazim

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface IUserDao {
    @Query("SELECT * FROM user LIMIT 1")
    suspend fun getStoredUserData(): UserData?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserData(user: UserData)

    @Update
    suspend fun updateUserData(user: UserData)

}