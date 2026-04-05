package but.projet.projetvikazim

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.annotation.RequiresPermission
import androidx.collection.objectFloatMapOf
import but.projet.projetvikazim.api.APIConnection
import org.json.JSONObject

class UserRepository(private val context: Context) {

    private val dao = AppDatabase.getInstance(context).userDao()
    private val api = APIConnection()

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    private fun isOnline(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

        // Lecture : réseau si dispo, sinon Room
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    suspend fun getUser(username: String, token: String): UserData? {
        if (isOnline()) {
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
                    confirmPassword = obj.getString("confirmPassword"),
                    isModify    = false
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

        // Écriture : Room immédiatement (marqué dirty), puis sync si online
        @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
        suspend fun updateUser(user: UserData) {
            val modify = user.copy(isModify = true)
            dao.updateUser(modify)

            if (isOnline()) {
                syncModify()
            }
        }

        // Synchronisation : envoie tous les enregistrements "sales" au serveur
        suspend fun syncModify() {
            val modifyUsers = dao.getModifyUsers()
            for (user in modifyUsers) {
                try {
                    val body = JSONObject().apply {
                        put("name", user.name)
                        put("surname", user.surname)
                        put("birthdate", user.birthdate)
                        put("address", user.address)
                        put("phone", user.phone)
                        put("email", user.email)
                        put("is_licensed", user.isLicensed)
                        put("license_number", user.licenseNumber)
                        put("chip_code", user.chipCode)
                    }
                    api.fetch("$baseUrl/users/${user.username}", "PUT", body)
                    dao.markClean(user.username)
                } catch (e: Exception) {

                }
            }
        }
    }
