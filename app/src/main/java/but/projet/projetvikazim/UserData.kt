package but.projet.projetvikazim

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "user")
data class UserData(
    @PrimaryKey
    var id : Int,
    var username : String,
    var password : String,
    var confirmPassword : String,
    var name : String,
    var surname : String,
    var birthdate : String,
    var address : String,
    var phone : String,
    var email : String,
    var isLicensed : Boolean,
    var licenseNumber : String,
    var chipCode : String

)
