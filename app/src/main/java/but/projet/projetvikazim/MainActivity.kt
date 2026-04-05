package but.projet.projetvikazim

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import but.projet.projetvikazim.api.SessionConnection
import but.projet.projetvikazim.ui.theme.Application_mobile_VikazimTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

val baseUrl: String = "https://devmobile.nathanaelheyberger.fr/api";


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Application_mobile_VikazimTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Main(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Main(modifier: Modifier){
    var etatPage: MutableState<EtatPage> = remember { mutableStateOf<EtatPage>(EtatPage.Vue) }
    val sessionConnection = remember { SessionConnection() }

    Column() {
        when (etatPage.value) {
            EtatPage.Vue -> {
                if(sessionConnection.apiToken==""){
                    Column() {
                        ConnectionForm(sessionConnection)
                    }
                } else {
                    val profileController = ProfileController()
                    var profileJsonState = remember { mutableStateOf(JSONObject()) }
                    LaunchedEffect(sessionConnection.apiToken) {
                        if (sessionConnection.apiToken.isNotEmpty()) {
                            profileJsonState.value = withContext(Dispatchers.IO) {
                                profileController.fetchProfile(baseUrl+"/profile", sessionConnection.apiToken)
                            }
                        }
                    }
                    ProfileInformation(modifier, profileJsonState.value)
                }
            }
            EtatPage.ListeRequetes -> ListeRequetesPage()
        }

        PageNavbar(etatPage = etatPage)
    }
}

@Composable
fun ProfileInformation(modifier: Modifier = Modifier, profileJson: JSONObject) {

    Column (modifier = modifier) {
        Text("Profile")
        Column() {
            if (profileJson.length() == 0) {
                Text("Chargement...")
            } else {
                Text("Nom d'utilisateur : " + profileJson.getString("COM_PSEUDO"))
                Text("Nom : " + profileJson.getString("COM_NOM"))
                Text("Prenom : " + profileJson.getString("COM_PRENOM"))
                Text("Date de naissance : " + profileJson.getString("COM_DATE_NAISSANCE"))
                Text("Adresse : " + profileJson.getString("COM_ADRESSE"))
                Text("Téléphone : " + profileJson.getString("COM_TELEPHONE"))
                Text("Adresse mail : " + profileJson.getString("COM_MAIL"))
                val adherentIsNull = profileJson.isNull("adherent")
                var license : String
                var chip : String
                if(adherentIsNull){
                    license="Aucune licence"
                    chip="—"
                } else {
                    val adherent = profileJson.getJSONObject("adherent")
                    license=adherent.getString("ADH_NUM_LICENCIE")
                    chip=adherent.getString("ADH_NUM_PUCE")

                }
                Text("Licence : "+license)
                Text("Puce : "+chip)
            }

        }

        Button({

        }) {
            Text("Modifier mon profile")
        }
    }
}

@Composable
fun PageNavbar(modifier: Modifier = Modifier, etatPage: MutableState<EtatPage>){

    Row() {
        when(etatPage.value){
            EtatPage.ListeRequetes -> Button({ etatPage.value=EtatPage.Vue }) { Text("Page de Profile") }
            EtatPage.Vue -> Button({ etatPage.value=EtatPage.ListeRequetes }) { Text("Liste des requêtes en attentes") }
        }

    }

}

@Composable
fun ListeRequetesPage(modifier: Modifier = Modifier) {
    Column (modifier = modifier) {
        Text("Requêtes en attentes")
        Column() {

        }

    }
}

@Preview(showBackground = true)
@Composable
fun MainPreview() {
    Main(modifier = Modifier)
}

@Preview(showBackground = true)
@Composable
fun ProfileInformationPreview() {
    Application_mobile_VikazimTheme {
        val profileJson = remember { mutableStateOf(JSONObject()) }
        ProfileInformation(profileJson = profileJson.value)
    }
}

@Composable
fun ConnectionForm(sessionConnection: SessionConnection){
    val isSignUpForm: MutableState<Boolean> = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val errorMessage: MutableState<String> = remember { mutableStateOf("") }



    if(isSignUpForm.value){
        signUp(sessionConnection, scope, errorMessage, isSignUpForm)

    } else {
        login(sessionConnection, scope, errorMessage, isSignUpForm)
    }

}

@Composable
fun signUp(
    sessionConnection: SessionConnection,
    scope: CoroutineScope,
    errorMessage: MutableState<String>,
    isSignUpForm: MutableState<Boolean>
){
    Column() {
        Text("Inscription")
        if(errorMessage.value!=""){
            Text(errorMessage.value)
        }
        TextField(
            value = sessionConnection.username,
            onValueChange = { sessionConnection.username = it},
            label = { Text("Nom d'utilisateur") },
            placeholder = { Text("") }
        )
        TextField(
            value = sessionConnection.password,
            onValueChange = { sessionConnection.password = it},
            label = { Text("Mot de passe") },
            placeholder = { Text("") },
            visualTransformation = PasswordVisualTransformation()
        )
        TextField(
            value = sessionConnection.passwordConfirmation,
            onValueChange = { sessionConnection.passwordConfirmation = it},
            label = { Text("Confirmation mot de passe") },
            placeholder = { Text("") },
            visualTransformation = PasswordVisualTransformation()
        )
        TextField(
            value = sessionConnection.surname,
            onValueChange = { sessionConnection.surname = it},
            label = { Text("Prénom") },
            placeholder = { Text("") }
        )
        TextField(
            value = sessionConnection.name,
            onValueChange = { sessionConnection.name = it},
            label = { Text("Nom") },
            placeholder = { Text("") }
        )
        TextField(
            value = sessionConnection.email,
            onValueChange = { sessionConnection.email = it },
            label = { Text("Email") },
            placeholder = { Text("") }
        )
        TextField(
            value = sessionConnection.address,
            onValueChange = { sessionConnection.address = it},
            label = { Text("Adresse") },
            placeholder = { Text("") }
        )
        TextField(
            value = sessionConnection.phone,
            onValueChange = { sessionConnection.phone = it},
            label = { Text("Numéro de Téléphone") },
            placeholder = { Text("") }
        )
        val day   = remember { mutableStateOf("") }
        val month = remember { mutableStateOf("") }
        val year  = remember { mutableStateOf("") }

        val updateBirthdate = {
            sessionConnection.birthdate = "${year.value}-${month.value.padStart(2, '0')}-${day.value.padStart(2, '0')}"
        }

        Text("Date de naissance")
        Row {
            TextField(
                modifier = Modifier.weight(1f),
                value = day.value,
                onValueChange = { if (it.length <= 2 && it.all { c -> c.isDigit() }) { day.value = it; updateBirthdate() } },
                label = { Text("Jour") }
            )
            TextField(
                modifier = Modifier.weight(1f),
                value = month.value,
                onValueChange = { if (it.length <= 2 && it.all { c -> c.isDigit() }) { month.value = it; updateBirthdate() } },
                label = { Text("Mois") }
            )
            TextField(
                modifier = Modifier.weight(2f),
                value = year.value,
                onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) { year.value = it; updateBirthdate() } },
                label = { Text("Année") }
            )
        }

        Row(){
            Checkbox(
                checked = sessionConnection.isLicensed,
                onCheckedChange = { sessionConnection.isLicensed = it }
            )
            Text("Je suis licencié")
        }

        if(sessionConnection.isLicensed) {
            TextField(
                value = sessionConnection.licenseNumber,
                onValueChange = { sessionConnection.licenseNumber = it },
                label = { Text("Numéro de licence") },
                placeholder = { Text("") }
            )
            TextField(
                value = sessionConnection.chipCode,
                onValueChange = { sessionConnection.chipCode = it },
                label = { Text("Numéro de puce") },
                placeholder = { Text("") }
            )
        }
        Button({
            scope.launch {
                try {
                    val result = withContext(Dispatchers.IO) {
                        println("début")
                        sessionConnection.signup(baseUrl + "/signup")


                    }
                    println(result.toString())
                    if (result.has("errors")) {
                        val errors = result.getJSONObject("errors")
                        var errorString = ""
                        for(error in errors.keys()){
                            val errorText = errors.get(error).toString().replace("[\"","").replace("\",\"",".\n").replace("\"]",".")
                            errorString=errorString+errorText+"\n"
                        }
                        errorMessage.value = errorString
                    } else {
                        sessionConnection.apiToken = result.getString("token")
                    }
                } catch (e: JSONException){
                    e.printStackTrace()
                }

            }
        }, enabled = !sessionConnection.password.isEmpty() && !sessionConnection.username.isEmpty()) {
            Text("S'inscrire")
        }




        Button({isSignUpForm.value=false}) {
            Text("Se connecter")
        }
    }
}

@Composable
fun login(
    sessionConnection: SessionConnection,
    scope: CoroutineScope,
    errorMessage: MutableState<String>,
    isSignUpForm: MutableState<Boolean>
){
    Column() {
        Text("Connexion")
        if(errorMessage.value!=""){
            Text(errorMessage.value)
        }
        TextField(
            value = sessionConnection.username,
            onValueChange = { sessionConnection.username = it},
            label = { Text("Nom d'utilisateur") },
            placeholder = { Text("") }
        )
        TextField(
            value = sessionConnection.password,
            onValueChange = { sessionConnection.password = it},
            label = { Text("Mot de passe") },
            placeholder = { Text("") },
            visualTransformation = PasswordVisualTransformation()

        )
        Button({
            scope.launch {
                try {
                    val result = withContext(Dispatchers.IO) {
                        println("début")
                        sessionConnection.login(baseUrl + "/login")


                    }
                    println(result.toString())
                    if (!result.getBoolean("success")) {
                        errorMessage.value = result.getString("message")
                    } else {
                        sessionConnection.apiToken = result.getString("token")
                    }
                } catch (e: JSONException){
                    e.printStackTrace()
                }

            }
        }, enabled = !sessionConnection.password.isEmpty() && !sessionConnection.username.isEmpty()) {
            Text("Se connecter")
        }

        Button({isSignUpForm.value=true}) {
            Text("S'inscrire")
        }
    }
}
