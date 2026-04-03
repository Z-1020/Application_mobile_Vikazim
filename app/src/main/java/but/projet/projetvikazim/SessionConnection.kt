package but.projet.projetvikazim

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import but.projet.projetvikazim.api.APIConnection
import org.json.JSONObject

class SessionConnection {
    var apiToken by mutableStateOf("")
    var username by mutableStateOf("")
    var password by mutableStateOf("")
    var passwordConfirmation by mutableStateOf("")
    var surname by mutableStateOf("")
    var name by mutableStateOf("")
    var birthdate by mutableStateOf("")  // format "YYYY-MM-DD"
    var address by mutableStateOf("")
    var phone by mutableStateOf("")
    var email by mutableStateOf("")
    var isLicensed by mutableStateOf(false)
    var licenseNumber by mutableStateOf("")
    var chipCode by mutableStateOf("")

    fun login(urlString: String): JSONObject {
        val json = JSONObject().apply {
            put("username", username)
            put("password", password)
        }
        return JSONObject(APIConnection().fetch(urlString, "POST", json))
    }

    fun signup(urlString: String): JSONObject {
        val json = JSONObject().apply {
            put("username", username)
            put("password", password)
            put("password_confirmation", passwordConfirmation)
            put("surname", surname)
            put("name", name)
            put("birthdate", birthdate)
            put("address", address)
            put("phone", phone)
            put("email", email)
            if (isLicensed) {
                put("adherentCheck", "on")
                put("license_number", licenseNumber)
                put("chip_code", chipCode)
            }
        }
        return JSONObject(APIConnection().fetch(urlString, "POST", json))
    }
}
