package dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PasswordPrompt(password: String, result: (pwCorrect: Boolean) -> Unit) {

    var textViewText by remember { mutableStateOf("Bitte gib das Passwort ein") }
    var passwordFieldVal by remember { mutableStateOf("") }
    var errorTextField by remember { mutableStateOf(false) }

    fun checkPasswordAndReturn() {
        val passwordCorrect = passwordFieldVal == password // <- Password
        errorTextField = !passwordCorrect
        textViewText = if (passwordCorrect) textViewText else "Passwort falsch!"
        result(passwordCorrect)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
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
            modifier = Modifier.padding(bottom = 16.dp).onKeyEvent { keyEvent ->
                if (keyEvent.key != Key.Enter) return@onKeyEvent false
                if (keyEvent.type == KeyEventType.KeyUp) {
                    checkPasswordAndReturn()
                }
                true
            },
            onValueChange = { passwordFieldVal = it })
        Button(onClick = {
            checkPasswordAndReturn()
        }) { Text("OK") }
    }

}
