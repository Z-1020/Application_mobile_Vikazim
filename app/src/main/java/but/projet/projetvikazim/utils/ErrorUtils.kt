package but.projet.projetvikazim

import org.json.JSONObject

object ErrorUtils {
    fun parseErrors(result: JSONObject): String {
        val errors = result.getJSONObject("errors")
        var errorString = ""
        for(error in errors.keys()){
            val errorText = errors.get(error).toString().replace("[\"","").replace("\",\"",".\n").replace("\"]",".")
            errorString=errorString+errorText+"\n"
        }

        return errorString
    }
}