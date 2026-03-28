package but.projet.projetvikazim.api

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection


class APIConnection {
    fun fetch(urlString: String, methods: String, jsonObject: JSONObject?): String {
        val url = URL(urlString)
        val conn = url.openConnection() as HttpsURLConnection
        conn.connectTimeout = 5000
        conn.readTimeout = 5000
        conn.doInput = true
        conn.setRequestProperty("Accept","application/json")
        conn.requestMethod = methods
        if(jsonObject!=null){
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            conn.doOutput = true
            val flux = conn.getOutputStream()
            flux.write(jsonObject.toString().toByteArray(Charsets.UTF_8))
            flux.close()
        }
        conn.connect()
        val stream = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
        val response = stream.bufferedReader().readText()
        conn.disconnect()
        println("fin")
        return response
    }
}