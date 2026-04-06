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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import but.projet.projetvikazim.api.SessionConnection
import but.projet.projetvikazim.ui.theme.Application_mobile_VikazimTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import kotlin.collections.iterator


val baseUrl: String = "https://devmobile.nathanaelheyberger.fr/api"


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
    val etatPage: MutableState<EtatPage> = remember { mutableStateOf<EtatPage>(EtatPage.Vue) }
    val sessionConnection = remember { SessionConnection() }

    Column() {
        when (etatPage.value) {
            EtatPage.Vue -> {
                if(sessionConnection.apiToken==""){
                    Column() {
                        ConnectionForm(sessionConnection)
                    }
                } else {
                    val profileController = remember { ProfileController() }
                    val profileJsonState = remember { mutableStateOf(JSONObject()) }
                    LaunchedEffect(sessionConnection.apiToken) {
                        if (sessionConnection.apiToken.isNotEmpty()) {
                            profileJsonState.value = withContext(Dispatchers.IO) {
                                profileController.fetchProfile(baseUrl+"/profile", sessionConnection.apiToken)
                            }
                        }
                    }
                    ProfileInformation(modifier, profileJsonState.value, sessionConnection, profileController)
                }
            }
            EtatPage.ListeRequetes -> ListeRequetesPage()
            EtatPage.Parametres -> {}
        }

        PageNavbar(etatPage = etatPage)
    }
}

@Composable
fun ProfileInformation(
    modifier: Modifier = Modifier,
    profileJson: JSONObject,
    sessionConnection: SessionConnection,
    profileController: ProfileController
    ) {
    val updatingProfile = remember { mutableStateOf(false) }

    if (profileJson.length() == 0) {
        Text("Chargement...")
    } else {

        val username = remember { mutableStateOf(profileJson.getString("COM_PSEUDO")) }
        val name = remember { mutableStateOf(profileJson.getString("COM_NOM")) }
        val surname = remember { mutableStateOf(profileJson.getString("COM_PRENOM")) }
        val birthdate = remember { mutableStateOf(profileJson.getString("COM_DATE_NAISSANCE")) }

        val address = remember { mutableStateOf(profileJson.getString("COM_ADRESSE")) }
        val phone = remember { mutableStateOf(profileJson.getString("COM_TELEPHONE")) }
        val email = remember { mutableStateOf(profileJson.getString("COM_MAIL")) }

        val adherentIsNull = profileJson.isNull("adherent")
        val license= remember { mutableStateOf("Aucune licence") }
        val chip= remember { mutableStateOf("—") }
        if(!adherentIsNull){
            val adherent = profileJson.getJSONObject("adherent")
            if(!adherent.isNull("ADH_NUM_LICENCIE")){
                license.value=adherent.getString("ADH_NUM_LICENCIE")
            }
            if(!adherent.isNull("ADH_NUM_PUCE")){
                chip.value=adherent.getString("ADH_NUM_PUCE")
            }
        }

        if(!updatingProfile.value){
            Text("Profile")
            Column() {
                Text("Nom d'utilisateur : " + username.value)
                Text("Nom : " + name.value)
                Text("Prenom : " + surname.value)
                Text("Date de naissance : " + birthdate.value)
                Text("Adresse : " + address.value)
                Text("Téléphone : " + phone.value)
                Text("Adresse mail : " + email.value)
                Text("Licence : "+license.value)
                Text("Puce : "+chip.value)
            }

            Button({
                updatingProfile.value=true
            }) {
                Text("Modifier mon profile")
            }

        } else {
            ProfileUpdateForm(username, name, surname, birthdate, address, phone, email, license, chip, updatingProfile, profileController, sessionConnection)

            PasswordUpdateForm(profileController, sessionConnection, updatingProfile)


        }
    }
}

@Composable
fun ProfileUpdateForm(
    username: MutableState<String>,
    name: MutableState<String>,
    surname: MutableState<String>,
    birthdate: MutableState<String>,
    address: MutableState<String>,
    phone: MutableState<String>,
    email: MutableState<String>,
    license: MutableState<String>,
    chip: MutableState<String>,
    updatingProfile: MutableState<Boolean>,
    profileController: ProfileController,
    sessionConnection: SessionConnection
) {
    val usernameCopy = remember { mutableStateOf(username.value) }
    val nameCopy = remember { mutableStateOf(name.value) }
    val surnameCopy = remember { mutableStateOf(surname.value) }

    val birthdateCopy = remember { mutableStateOf(birthdate.value) }

    val addressCopy = remember { mutableStateOf(address.value) }
    val phoneCopy = remember { mutableStateOf(phone.value) }
    val emailCopy = remember { mutableStateOf(email.value) }

    val licenseCopy = remember { mutableStateOf(license.value) }
    val chipCopy = remember { mutableStateOf(chip.value) }

    if(license.value=="Aucune licence"){
        licenseCopy.value=""
    }

    if(chip.value=="—"){
        chipCopy.value=""
    }

    val errorMessage: MutableState<String> = remember { mutableStateOf("") }

    Text("Modifier le profile")
    if(errorMessage.value!=""){
        Text(errorMessage.value)
    }
    Column() {
        TextField(
            value = usernameCopy.value,
            onValueChange = { usernameCopy.value = it},
            label = { Text("Nom d'utilisateur") }
        )
        TextField(
            value = surnameCopy.value,
            onValueChange = { surnameCopy.value = it},
            label = { Text("Prénom") }
        )
        TextField(
            value = nameCopy.value,
            onValueChange = { nameCopy.value = it},
            label = { Text("Nom") }
        )
        TextField(
            value = emailCopy.value,
            onValueChange = { emailCopy.value = it },
            label = { Text("Email") }
        )
        TextField(
            value = addressCopy.value,
            onValueChange = { addressCopy.value = it},
            label = { Text("Adresse") }
        )
        TextField(
            value = phoneCopy.value,
            onValueChange = { phoneCopy.value = it},
            label = { Text("Numéro de Téléphone") }
        )

        val birthdateArray = birthdateCopy.value.split("-")
        val year = remember { mutableStateOf(birthdateArray[0]) }
        val month = remember { mutableStateOf(birthdateArray[1]) }
        val day = remember { mutableStateOf(birthdateArray[2]) }

        Text("Date de naissance")
        Row {
            TextField(
                modifier = Modifier.weight(1f),
                value = day.value,
                onValueChange = {
                    day.value = it
                    birthdateCopy.value = "${year.value}-${month.value.padStart(2,'0')}-${day.value.padStart(2,'0')}"
                },
                label = { Text("Jour") }
            )
            TextField(
                modifier = Modifier.weight(1f),
                value = month.value,
                onValueChange = {
                    month.value = it
                    birthdateCopy.value = "${year.value}-${month.value.padStart(2,'0')}-${day.value.padStart(2,'0')}"
                },
                label = { Text("Mois") }
            )
            TextField(
                modifier = Modifier.weight(2f),
                value = year.value,
                onValueChange = {
                    year.value = it
                    birthdateCopy.value = "${year.value}-${month.value.padStart(2,'0')}-${day.value.padStart(2,'0')}"
                },
                label = { Text("Année") }
            )
        }
        TextField(
            value = licenseCopy.value,
            onValueChange = { licenseCopy.value = it },
            label = { Text("Numéro de licence") }
        )
        TextField(
            value = chipCopy.value,
            onValueChange = { chipCopy.value = it },
            label = { Text("Numéro de puce") }
        )
    }
    Row() {
        Button({
            updatingProfile.value=false
        }) {
            Text("Annuler")
        }
        val scope = rememberCoroutineScope()
        Button({
            val json = JSONObject().apply {
                put("COM_PSEUDO", usernameCopy.value)
                put("COM_PRENOM", surnameCopy.value)
                put("COM_NOM", nameCopy.value)
                put("COM_ADRESSE", addressCopy.value)
                put("COM_DATE_NAISSANCE", birthdateCopy.value)
                put("COM_TELEPHONE", phoneCopy.value)
                put("COM_MAIL", emailCopy.value)
                put("ADH_NUM_LICENCIE", licenseCopy.value)
                put("ADH_NUM_PUCE", chipCopy.value)
            }
            scope.launch {
                try {
                    val result = withContext(Dispatchers.IO) {
                        profileController.updateProfileInformation(
                            baseUrl + "/profile",
                            token = sessionConnection.apiToken,
                            json = json
                        )
                    }
                    System.out.println(result.toString())
                    if (!result.isNull("success")) {
                        if(result.getBoolean("success")){
                            username.value = usernameCopy.value
                            name.value = nameCopy.value
                            surname.value = surnameCopy.value
                            address.value = addressCopy.value
                            phone.value = phoneCopy.value
                            email.value = emailCopy.value
                            license.value = licenseCopy.value
                            chip.value = chipCopy.value
                            birthdate.value=birthdateCopy.value
                            updatingProfile.value=false
                        } else {
                            if (result.has("errors")) {
                                errorMessage.value = ErrorUtils.parseErrors(result)
                            }
                        }
                    } else {
                        if (result.has("errors")) {
                            errorMessage.value = ErrorUtils.parseErrors(result)
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }) {
            Text("Confirmer")
        }
    }
}

@Composable
fun PasswordUpdateForm(
    profileController: ProfileController,
    sessionConnection: SessionConnection,
    updatingProfile: MutableState<Boolean>
){
    val actualPassword = remember { mutableStateOf("") }
    val newPassword = remember { mutableStateOf("") }
    val newPasswordConfirmation = remember { mutableStateOf("") }

    val errorMessage: MutableState<String> = remember { mutableStateOf("") }

    Text("Modifier le mot de passe")
    if(errorMessage.value!=""){
        Text(errorMessage.value)
    }
    Column() {
        TextField(
            value = actualPassword.value,
            onValueChange = { actualPassword.value = it},
            label = { Text("Mot de passe actuel") },
            visualTransformation = PasswordVisualTransformation()

        )
        TextField(
            value = newPassword.value,
            onValueChange = { newPassword.value = it},
            label = { Text("Nouveau mot de passe") },
            visualTransformation = PasswordVisualTransformation()

        )
        TextField(
            value = newPasswordConfirmation.value,
            onValueChange = { newPasswordConfirmation.value = it},
            label = { Text("Confirmation du nouveau mot de passe") },
            visualTransformation = PasswordVisualTransformation()


        )
    }
    val scope = rememberCoroutineScope()
    Row() {
        Button({
            updatingProfile.value=false
        }) {
            Text("Annuler")
        }
        Button({
            val json = JSONObject().apply {
                put("current_password", actualPassword.value)
                put("password", newPassword.value)
                put("password_confirmation", newPasswordConfirmation.value)
            }
            scope.launch {
                try {
                    val result = withContext(Dispatchers.IO) {
                        profileController.updatePassword(
                            baseUrl + "/profile/password",
                            token = sessionConnection.apiToken,
                            json = json
                        )
                    }
                    if (result.has("errors")) {
                        errorMessage.value = ErrorUtils.parseErrors(result)
                    } else {
                        updatingProfile.value=false
                    }

                    System.out.println(result)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }){
            Text("Modifier le mot de passe")
        }
    }
}

@Composable
fun PageNavbar(modifier: Modifier = Modifier, etatPage: MutableState<EtatPage>){
    val pageAvantParametres = remember { mutableStateOf(EtatPage.Vue) }
    Row() {
        when(etatPage.value){
            EtatPage.ListeRequetes -> Button({ etatPage.value=EtatPage.Vue }) { Text("Page de Profile") }
            EtatPage.Vue -> Button({ etatPage.value=EtatPage.ListeRequetes }) { Text("Liste des requêtes en attentes") }
            EtatPage.Parametres -> Button({ etatPage.value=pageAvantParametres.value }) { Text("Retour") }
        }
        if(etatPage.value != EtatPage.Parametres){
            IconButton({
                pageAvantParametres.value=etatPage.value
                etatPage.value=EtatPage.Parametres
            }) {
                Icon(painter = painterResource(R.drawable.baseline_app_settings_alt_24), contentDescription = "Settings")
            }
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
        val sessionConnection= SessionConnection()
        val profileController= ProfileController()
        ProfileInformation(profileJson = profileJson.value, sessionConnection = sessionConnection, profileController = profileController)
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
                        errorMessage.value = ErrorUtils.parseErrors(result)
                    } else {
                        sessionConnection.apiToken = result.getString("token")
                    }
                } catch (e: JSONException){
                    e.printStackTrace()
                }
            }
        }, enabled = !sessionConnection.password.isEmpty()
                && !sessionConnection.username.isEmpty()
                && !sessionConnection.address.isEmpty()
                && !sessionConnection.email.isEmpty()
                && !sessionConnection.passwordConfirmation.isEmpty()
                && !sessionConnection.phone.isEmpty()
                && !sessionConnection.name.isEmpty()
                && !sessionConnection.surname.isEmpty()
                && !sessionConnection.birthdate.isEmpty()
                && !sessionConnection.username.isEmpty()
                && !sessionConnection.password.isEmpty()
                && (!sessionConnection.isLicensed || (!sessionConnection.chipCode.isEmpty() && !sessionConnection.licenseNumber.isEmpty()))
        ) {
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
