package but.projet.projetvikazim

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import but.projet.projetvikazim.ui.theme.Application_mobile_VikazimTheme

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

    Column() {
        when(etatPage.value){
            EtatPage.Vue -> ProfileInformation(modifier)
            EtatPage.ListeRequetes -> ListeRequetesPage()
        }
        PageNavbar(etatPage = etatPage)
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

