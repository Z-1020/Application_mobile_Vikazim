package but.projet.projetvikazim

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsData(
    @PrimaryKey val id: Int = 1,
    val customUrl: String?,
    val username: String?,
    val password: String?,
    val automaticLogin: Boolean?
)