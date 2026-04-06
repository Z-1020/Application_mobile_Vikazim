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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import but.projet.projetvikazim.api.SessionConnection
import but.projet.projetvikazim.ui.theme.Application_mobile_VikazimTheme
import but.projet.projetvikazim.utils.ConnectionStatusUtils
import but.projet.projetvikazim.utils.ErrorUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject


var basedUrl = "https://devmobile.nathanaelheyberger.fr/api"

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
    val context = LocalContext.current
    val online = remember { mutableStateOf(false) }

    val settingsDataDao = AppDatabase.getInstance(context).settingsDao()
    val userData = remember { mutableStateOf<UserData?>(null) }
    val userDataController = remember { UserDataController() }

    val registeredUsername = remember { mutableStateOf("") }
    val registeredPassword = remember { mutableStateOf("") }
    val automaticLogin = remember { mutableStateOf(false) }
    val usedUrl = remember { mutableStateOf(basedUrl) }

    //charger les paramètres + online
    LaunchedEffect(Unit) {
        online.value = ConnectionStatusUtils.isOnline(context)
        val settings = settingsDataDao.getSettings()
        if (settings != null) {
            automaticLogin.value = settings.automaticLogin == true
            registeredUsername.value = settings.username.toString()
            registeredPassword.value = settings.password.toString()
            if(settings.customUrl?.isNotEmpty() == true){
                usedUrl.value = settings.customUrl
            }
            println(automaticLogin.value)
            println(registeredUsername.value)
            println(registeredPassword.value)
            println(usedUrl.value)
        }
    }

    Column() {
        when (etatPage.value) {
            EtatPage.Vue -> {
                if(!online.value){
                    //hors-ligne
                    LaunchedEffect(sessionConnection.apiToken) {
                        userData.value = userDataController.getUserData(
                            urlString = usedUrl.value,
                            token = sessionConnection.apiToken,
                            context = context
                        )
                    }
                    ProfileInformation(modifier, userData.value, sessionConnection, online.value, userDataController, usedUrl)

                } else {
                    //enligne
                    if(sessionConnection.apiToken==""){
                        //il faut une connexion
                        if(automaticLogin.value){
                            //connexion automatique
                            val automaticLoginError: MutableState<String> = remember { mutableStateOf("") }
                            Text(automaticLoginError.value)
                            val scope = rememberCoroutineScope()
                            LaunchedEffect(sessionConnection.apiToken) {
                                sessionConnection.username=registeredUsername.value
                                sessionConnection.password=registeredPassword.value
                                    scope.launch {
                                        try {
                                            val result = withContext(Dispatchers.IO) {
                                                println("début")
                                                sessionConnection.login(usedUrl.value + "/login")
                                            }
                                            println(result.toString())
                                            if (!result.getBoolean("success")) {
                                                automaticLoginError.value = result.getString("message")
                                            } else {
                                                sessionConnection.apiToken = result.getString("token")
                                            }
                                        } catch (e: JSONException){
                                            e.printStackTrace()
                                        }
                                    }
                            }
                        } else {
                            //connexion pas automatique
                            Column() {
                                ConnectionForm(sessionConnection, usedUrl)
                            }
                        }

                    } else {
                        //il faut pas de connexion supplémentaire
                        LaunchedEffect(sessionConnection.apiToken) {
                            userData.value = userDataController.getUserData(
                                urlString = usedUrl.value,
                                token = sessionConnection.apiToken,
                                context = context
                            )
                        }
                        ProfileInformation(modifier, userData.value, sessionConnection, online.value, userDataController, usedUrl)
                    }
                }
            }
            EtatPage.ListeRequetes -> ListeRequetesPage()
            EtatPage.Parametres -> Parametres()
        }

        PageNavbar(etatPage = etatPage)
    }
}

@Composable
fun Parametres(){
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val settingsDao = AppDatabase.getInstance(context).settingsDao()

    val automaticLogin = remember { mutableStateOf(false) }
    val username = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val customUrl = remember { mutableStateOf("") }
    val message = remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        val settings = settingsDao.getSettings()
        if(settings != null){
            automaticLogin.value = settings.automaticLogin == true
            username.value = settings.username.toString()
            password.value = settings.password.toString()
            customUrl.value = settings.customUrl.toString()
        }
    }
    Column {
        Text("Paramètres")
        if(message.value.isNotEmpty()){
            Text(message.value)
        }
        Row {
            Checkbox(
                checked = automaticLogin.value,
                onCheckedChange = { automaticLogin.value = it }
            )
            Text("Connexion automatique")
        }

        TextField(
            value = username.value,
            onValueChange = { username.value = it },
            label = { Text("Nom d'utilisateur sauvegardé") }
        )

        TextField(
            value = password.value,
            onValueChange = { password.value = it },
            label = { Text("Mot de passe sauvegardé") },
            visualTransformation = PasswordVisualTransformation()
        )
        TextField(
            value = customUrl.value,
            onValueChange = { customUrl.value = it },
            label = { Text("URL Customizé d'API") },
            placeholder = { Text("https://mon-api.fr/api") }
        )
        Row {
            Button({
                scope.launch {
                    val settings = SettingsData(
                        id = 1,
                        username = username.value,
                        password = password.value,
                        automaticLogin = automaticLogin.value,
                        customUrl = customUrl.value
                    )
                    settingsDao.saveSettings(settings)
                    message.value = "Paramètres sauvegardés"
                }
            }) {
                Text("Sauvegarder")
            }
        }
    }
}

@Composable
fun ProfileInformation(
    modifier: Modifier = Modifier,
    userData: UserData?,
    sessionConnection: SessionConnection,
    isOnline: Boolean,
    userDataController: UserDataController,
    usedUrl: MutableState<String>
) {
    val updatingProfile = remember { mutableStateOf(false) }

    if (userData==null) {
        if(isOnline){
            Text("Chargement...")
        } else {
            Text("Application hors ligne et aucune données de profil stockées")
        }

    } else {
        val username = remember { mutableStateOf(userData.username) }
        val name = remember { mutableStateOf(userData.name) }
        val surname = remember { mutableStateOf(userData.surname) }
        val birthdate = remember { mutableStateOf(userData.birthdate) }

        val address = remember { mutableStateOf(userData.address) }
        val phone = remember { mutableStateOf(userData.phone) }
        val email = remember { mutableStateOf(userData.email) }

        val license= remember { mutableStateOf("Aucune licence") }
        val chipCode= remember { mutableStateOf("—") }

        if(userData.licenseNumber != null){
            license.value=userData.licenseNumber.toString()
        }
        if(userData.chipCode != null){
            chipCode.value=userData.chipCode.toString()
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
                Text("Puce : "+chipCode.value)
            }

            Button({
                updatingProfile.value=true
            }) {
                Text("Modifier mon profile")
            }

        } else {
            ProfileUpdateForm(username, name, surname, birthdate, address, phone, email, license, chipCode, updatingProfile, sessionConnection, userDataController, usedUrl)

            PasswordUpdateForm(sessionConnection, updatingProfile, userDataController, usedUrl)


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
    sessionConnection: SessionConnection,
    userDataController: UserDataController,
    usedUrl: MutableState<String>
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

    val context = LocalContext.current
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

                    val result = userDataController.updateUserData(usedUrl.value, sessionConnection.apiToken, context, json)

                    println(result.toString())
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
                        } else {

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
    sessionConnection: SessionConnection,
    updatingProfile: MutableState<Boolean>,
    userDataController: UserDataController,
    usedUrl: MutableState<String>
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
                    val result = userDataController.updatePassword(usedUrl.value, sessionConnection.apiToken, json)

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



@Composable
fun ConnectionForm(sessionConnection: SessionConnection, usedUrl: MutableState<String>){
    val isSignUpForm: MutableState<Boolean> = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val errorMessage: MutableState<String> = remember { mutableStateOf("") }

    if(isSignUpForm.value){
        signUp(sessionConnection, scope, errorMessage, isSignUpForm, usedUrl)

    } else {
        login(sessionConnection, scope, errorMessage, isSignUpForm, usedUrl)
    }

}

@Composable
fun signUp(
    sessionConnection: SessionConnection,
    scope: CoroutineScope,
    errorMessage: MutableState<String>,
    isSignUpForm: MutableState<Boolean>,
    usedUrl: MutableState<String>
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
                        sessionConnection.signup(usedUrl.value + "/signup")
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
    isSignUpForm: MutableState<Boolean>,
    usedUrl: MutableState<String>
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
                        sessionConnection.login(usedUrl.value + "/login")
                    }
                    println(result.toString())
                    println("avant")
                    if (!result.getBoolean("success")) {
                        errorMessage.value = result.getString("message")
                    } else {
                        sessionConnection.apiToken = result.getString("token")
                    }
                    println("après")
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
