package but.projet.projetvikazim

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class SessionConnection {
    var apiToken by mutableStateOf("")
    var username by mutableStateOf("")
    var password by mutableStateOf("")
}