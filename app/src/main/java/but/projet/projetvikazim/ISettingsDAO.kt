package but.projet.projetvikazim

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ISettingsDAO {
    @Query("SELECT * FROM settings WHERE id = 1")
    suspend fun getSettings(): SettingsData?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: SettingsData)
}