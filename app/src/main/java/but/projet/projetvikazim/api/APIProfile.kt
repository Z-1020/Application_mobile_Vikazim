package but.projet.projetvikazim.api

import org.json.JSONObject

class APIProfile {
    fun fetchProfile(urlString: String): JSONObject {
        val json = JSONObject().apply {
            put("username", username)
            put("password", password)
        }
        return JSONObject(APIFetcher().fetch(urlString, "POST", json))
    }

}