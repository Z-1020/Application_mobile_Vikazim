package but.projet.projetvikazim

import android.content.Context
import but.projet.projetvikazim.utils.ConnectionStatusUtils.isOnline
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class UserDataController() {
    private val profileController = ProfileController()

    suspend fun getUserData(urlString: String, token: String, context: Context): UserData? {
        val dao = AppDatabase.getInstance(context).userDao()
        if (isOnline(context)) {
            var json: JSONObject

            if (token.isNotEmpty()) {
                json = withContext(Dispatchers.IO) {
                    profileController.fetchProfile(urlString + "/profile", token)
                }


                val user = UserData(
                    username    = json.getString("COM_PSEUDO"),
                    name        = json.getString("COM_NOM"),
                    surname     = json.getString("COM_PRENOM"),
                    birthdate   = json.getString("COM_DATE_NAISSANCE"),
                    address     = json.getString("COM_ADRESSE"),
                    phone       = json.getString("COM_TELEPHONE"),
                    email       = json.getString("COM_MAIL"),
                    licenseNumber = json.optString("ADH_NUM_LICENCIE", ""),
                    chipCode    = json.optString("ADH_NUM_PUCE", "")
                )
                return try {
                    dao.insertUserData(user)
                    user
                } catch (e: Exception) {
                    dao.getStoredUserData()
                }
            }
            return null
        } else {
            return dao.getStoredUserData()
        }
    }

    suspend fun updateUserData(urlString: String, token: String, context: Context, newJSON: JSONObject): JSONObject {
        val dao = AppDatabase.getInstance(context).userDao()
        val result: JSONObject
        val user = UserData(
            id          = 0,
            username    = newJSON.getString("username"),
            name        = newJSON.getString("name"),
            surname     = newJSON.getString("surname"),
            birthdate   = newJSON.getString("birthdate"),
            address     = newJSON.getString("address"),
            phone       = newJSON.getString("phone"),
            email       = newJSON.getString("email"),
            licenseNumber = newJSON.optString("license_number", ""),
            chipCode    = newJSON.optString("chip_code", "")
        )
        try {
            dao.insertUserData(user)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (isOnline(context)) {
             result = withContext(Dispatchers.IO) {
                profileController.updateProfileInformation(
                    urlString + "/profile",
                    token = token,
                    json = newJSON
                )
            }
            return result
        } else {
            result = JSONObject()
            result.put("status", "hors-ligne")
            return result
        }
    }

    suspend fun updatePassword(urlString: String, token: String, json: JSONObject): JSONObject {
        return withContext(Dispatchers.IO) {
            profileController.updatePassword(urlString + "/profile/password", token, json)
        }
    }



}