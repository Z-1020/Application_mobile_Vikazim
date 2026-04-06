package but.projet.projetvikazim

import android.content.Context
import but.projet.projetvikazim.utils.ConnectionStatusUtils.isOnline
import org.json.JSONObject

class UserDataController() {
    private val profileController = ProfileController()

    suspend fun getUserData(urlString: String, token: String, context: Context): UserData? {
        if (isOnline(context)) {
            return try {
                val json = api.fetch(
                    "$baseUrl/users/$username",
                    "GET",
                    null,
                )
                val obj = JSONObject(json)
                val user = UserData(
                    id          = obj.getInt("id"),
                    username    = obj.getString("username"),
                    name        = obj.getString("name"),
                    surname     = obj.getString("surname"),
                    birthdate   = obj.getString("birthdate"),
                    address     = obj.getString("address"),
                    phone       = obj.getString("phone"),
                    email       = obj.getString("email"),
                    isLicensed  = obj.getBoolean("is_licensed"),
                    licenseNumber = obj.optString("license_number", ""),
                    chipCode    = obj.optString("chip_code", ""),
                    password = obj.getString("password"),
                    confirmPassword = obj.getString("confirmPassword")
                )
                dao.insertUser(user) // on met à jour le cache local
                user
            } catch (e: Exception) {
                dao.getUser(username) // fallback local
            }
        } else {
            return dao.getUser(username)
        }
    }
}