package but.projet.projetvikazim

import but.projet.projetvikazim.api.APIFetcher
import org.json.JSONObject

class ProfileController {
    fun fetchProfile(urlString: String, token: String): JSONObject {
        return JSONObject(APIFetcher().fetchWithToken(urlString, "GET", null, token))
    }

}