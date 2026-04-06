package but.projet.projetvikazim

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "user")
data class UserData(
    @PrimaryKey
    val id: Int = 1,
    var username : String,
    var name : String,
    var surname : String,
    var birthdate : String,
    var address : String,
    var phone : String,
    var email : String,
    var licenseNumber : String?,
    var chipCode : String?

)
