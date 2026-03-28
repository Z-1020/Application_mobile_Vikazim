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


    fun login(urlString: String): JSONObject{
        val json = JSONObject()
        json.put("username",username)
        json.put("password",password)
        val api = APIConnection()
        val response = api.fetch(urlString, "POST", json)
        val result = JSONObject(response)
        if(result.get("success")=="true") {
            apiToken = result.get("token").toString()
        }
        return result
    }
}