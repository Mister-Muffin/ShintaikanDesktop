package pages

import Screen
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SuccessPage(changeScreen: (screen: Screen) -> Unit) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround,
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text(
            text = "Die Teilnehmer wurden erfolgreich eingetragen.",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold
        )
        Button(
            modifier = Modifier.width((60 * 5).dp).height(60.dp),
            onClick = { changeScreen(Screen.HOME) }) {
            Text("Weiter")
        }
    }
}
