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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import but.projet.projetvikazim.ui.theme.Application_mobile_VikazimTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    if(sessionConnection.apiToken==""){
        Column() {
            var isSignUp: MutableState<Boolean> = remember { mutableStateOf<Boolean>(false) }

            ConnectionForm(sessionConnection)
        }
    } else {
        Column() {
            when (etatPage.value) {
                EtatPage.Vue -> ProfileInformation(modifier)
                EtatPage.ListeRequetes -> ListeRequetesPage()
            }

            PageNavbar(etatPage = etatPage)
        }
    }
}

@Composable
fun ProfileInformation(modifier: Modifier = Modifier) {
    Column (modifier = modifier) {
        Text("Profile")
        Column() {
            Text("Nom d'utilisateur :")
            Text("Nom :")
            Text("Prenom :")
            Text("Date de naissance :")
            Text("Adresse :")
            Text("Téléphone :")
            Text("Adresse mail :")
            Text("Licence :")
            Text("Puce :")
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
        ProfileInformation()
    }
}

@Composable
fun ConnectionForm(sessionConnection: SessionConnection){
    val isSignUpForm: MutableState<Boolean> = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val errorMessage: MutableState<String> = remember { mutableStateOf("") }

    if(errorMessage.value!=""){
        Text(errorMessage.value)
    }

    if(isSignUpForm.value){

        Column() {


            Text("Inscription")
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
            Row(){
                Checkbox(
                    checked = sessionConnection.isLicensed,
                    onCheckedChange = { sessionConnection.isLicensed = it }
                )
                Text("Je suis licencié")
            }


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
                Text("S'inscrire")
            }




            Button({isSignUpForm.value=false}) {
                Text("Se connecter")
            }
        }
    } else {
        Column() {
            Text("Connexion")
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

}


