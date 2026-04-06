package but.projet.projetvikazim.utils

import org.json.JSONObject
import kotlin.collections.iterator

object ErrorUtils {
    fun parseErrors(result: JSONObject): String {
        val errors = result.optJSONObject("errors") ?: return ""
        var errorString = ""
        for(error in errors.keys()){
            val errorText = errors.get(error).toString().replace("[\"","").replace("\",\"",".\n").replace("\"]",".")
            errorString=errorString+errorText+"\n"
        }

        return errorString
    }
}