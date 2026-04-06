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

    suspend fun updateUserData(
        urlString: String,
        token: String,
        context: Context,
        newJSON: JSONObject
    ): JSONObject {
        val dao = AppDatabase.getInstance(context).userDao()
        val result: JSONObject
        val user = UserData(
            id          = 0,
            username    = newJSON.getString("COM_PSEUDO"),
            name        = newJSON.getString("COM_NOM"),
            surname     = newJSON.getString("COM_PRENOM"),
            birthdate   = newJSON.getString("COM_DATE_NAISSANCE"),
            address     = newJSON.getString("COM_ADRESSE"),
            phone       = newJSON.getString("COM_TELEPHONE"),
            email       = newJSON.getString("COM_MAIL"),
            licenseNumber = newJSON.optString("ADH_NUM_LICENCIE", ""),
            chipCode    = newJSON.optString("ADH_NUM_PUCE", "")
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
            val pendingFetchDao = AppDatabase.getInstance(context).pendingRequestDao()
            pendingFetchDao.insert(
                request = PendingFetchData(
                    method = "PATCH",
                    url = urlString + "/profile",
                    bodyJson = newJSON.toString()
                )
            )
            result = JSONObject()
            result.put("status", "hors-ligne")
            return result
        }
    }

    suspend fun updatePassword(
        urlString: String,
        token: String,
        json: JSONObject,
        isOnline: Boolean,
        current: Context
    ): JSONObject {
        val pendingFetchDao = AppDatabase.getInstance(current).pendingRequestDao()

        return withContext(Dispatchers.IO) {
            if(isOnline){
                profileController.updatePassword(urlString + "/profile/password", token, json)
            } else {
                pendingFetchDao.insert(
                    request = PendingFetchData(
                        method = "PATCH",
                        url = urlString + "/profile/password",
                        bodyJson = json.toString()
                    )
                )
                JSONObject()
            }
        }
    }



}