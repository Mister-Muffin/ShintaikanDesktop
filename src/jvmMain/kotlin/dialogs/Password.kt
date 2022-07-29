package dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState

@Composable
fun passwordDialog(result: (pwCorrect: Boolean) -> Unit, onDissmiss: () -> Unit) {

    var passwordFieldVal by remember { mutableStateOf("test") } //TODO: Set empty string for production
    var errorTextField by remember { mutableStateOf(false) }

    Dialog(
        state = rememberDialogState(position = WindowPosition(Alignment.Center), width = 700.dp),
        title = "Passworteingabe",
        onCloseRequest = onDissmiss
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Bitte gib das Passwort ein")
            OutlinedTextField(
                value = passwordFieldVal,
                visualTransformation = VisualTransformation {
                    TransformedText(
                        AnnotatedString("*".repeat(passwordFieldVal.length)), OffsetMapping.Identity
                    )
                },
                singleLine = true,
                isError = errorTextField,
                onValueChange = { passwordFieldVal = it })
            Button(onClick = {
                val passwordCorrect = passwordFieldVal == "test" // <- Password
                errorTextField = !passwordCorrect

                result(passwordCorrect)
            }) { Text("OK") }
        }
    }

}