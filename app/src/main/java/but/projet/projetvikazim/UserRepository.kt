package but.projet.projetvikazim

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.annotation.RequiresPermission
import androidx.collection.objectFloatMapOf
import but.projet.projetvikazim.api.APIFetcher
import org.json.JSONObject

class UserRepository(private val context: Context) {

    private val dao = AppDatabase.getInstance(context).userDao()
    private val api = APIFetcher()

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    private fun isOnline(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

        // Lecture : réseau si dispo, sinon Room
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)


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
