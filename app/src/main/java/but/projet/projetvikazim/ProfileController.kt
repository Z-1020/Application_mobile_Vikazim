package but.projet.projetvikazim

import but.projet.projetvikazim.api.APIFetcher
import org.json.JSONObject

class ProfileController {
    fun fetchProfile(urlString: String, token: String): JSONObject {
        return JSONObject(APIFetcher().fetchWithToken(urlString, "GET", null, token))
    }

    fun updateProfileInformation(urlString: String, token: String, json: JSONObject): JSONObject {
        return JSONObject(APIFetcher().fetchWithToken(urlString, "PATCH", json, token))
    }

    fun updatePassword(urlString: String, token: String, json: JSONObject): JSONObject {
        return JSONObject(APIFetcher().fetchWithToken(urlString, "PUT", json, token))
    }


}