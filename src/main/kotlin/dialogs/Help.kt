package dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import getRunningJar
import pages.COMPONENT_WIDTH
import kotlin.reflect.KClass

@Composable
fun HelpDialog(drivePath: String, kclass: KClass<out FrameWindowScope>, onDismiss: () -> Unit) {
    val cmdPath = getRunningJar(kclass)

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Hilfe/Info", style = MaterialTheme.typography.h6)
        Divider(modifier = Modifier.padding(vertical = 10.dp))
        Text("Pfad für die Importdatei: ${drivePath}transferHauseDojo.csv")
        Text("Programmpfad: $cmdPath")
        Button(onClick = onDismiss, modifier = Modifier.width(COMPONENT_WIDTH.dp).padding(vertical = 10.dp)) {
            Text("Zurück")
        }
    }
}
