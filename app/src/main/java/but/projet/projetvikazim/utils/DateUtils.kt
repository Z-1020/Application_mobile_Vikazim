package but.projet.projetvikazim.utils

import java.text.SimpleDateFormat
import java.util.Locale

object DateUtils {
    private val apiFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    // Convertit "2001-05-23" → "23/05/2001" pour l'affichage
    fun apiToDisplay(dateStr: String): String {
        return try {
            val date = apiFormat.parse(dateStr)
            if (date != null) displayFormat.format(date) else dateStr
        } catch (e: Exception) {
            dateStr
        }
    }

    // Convertit "23/05/2001" → "2001-05-23" pour l'API
    fun displayToApi(dateStr: String): String {
        return try {
            val date = displayFormat.parse(dateStr)
            if (date != null) apiFormat.format(date) else dateStr
        } catch (e: Exception) {
            dateStr
        }
    }
}