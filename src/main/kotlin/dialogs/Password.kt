package dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.unit.dp
import pages.COMPONENT_WIDTH

@Composable
fun PasswordPrompt(password: String, cancel: () -> Unit, result: (pwCorrect: Boolean) -> Unit) {

    var textViewText by remember { mutableStateOf("Bitte gib das Passwort ein:") }
    var passwordFieldVal by remember { mutableStateOf("") }
    var errorTextField by remember { mutableStateOf(false) }

    fun checkPasswordAndReturn() {
        val passwordCorrect = passwordFieldVal == password // <- Password
        errorTextField = !passwordCorrect
        textViewText = if (passwordCorrect) textViewText else "Passwort falsch!"
        result(passwordCorrect)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Passwortabfrage", style = MaterialTheme.typography.h6)
        Divider(modifier = Modifier.padding(vertical = 16.dp))
        Text(textViewText, modifier = Modifier.padding(bottom = 16.dp))
        OutlinedTextField(
            value = passwordFieldVal,
            visualTransformation = {
                TransformedText(
                    AnnotatedString("*".repeat(passwordFieldVal.length)), OffsetMapping.Identity
                )
            },
            singleLine = true,
            isError = errorTextField,
            leadingIcon = { Icon(Icons.Outlined.Lock, "") },
            modifier = Modifier.padding(bottom = 16.dp).onKeyEvent { keyEvent ->
                if (keyEvent.key != Key.Enter) return@onKeyEvent false
                if (keyEvent.type == KeyEventType.KeyUp) {
                    checkPasswordAndReturn()
                }
                true
            },
            onValueChange = { passwordFieldVal = it })
        Button(modifier = Modifier.width(COMPONENT_WIDTH.dp),
            onClick = { checkPasswordAndReturn() }) {
            Text("Weiter")
        }
        Button(modifier = Modifier.width(COMPONENT_WIDTH.dp),
            onClick = { cancel() }) {
            Text("Zurück")
        }
    }

}
