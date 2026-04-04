package but.projet.projetvikazim

import android.text.format.DateFormat

data class UserData(
    var username : String,
    var password : String,
    var confirmPassword : String,
    var name : String,
    var surname : String,
    var birthdate : DateFormat,
    var address : String,
    var phone : String,
    var email : String,
    var isLicensed : Boolean,
    var licenseNumber : String,
    var chipCode : String,

)
