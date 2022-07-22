package dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
public fun passwordDialog(result: (pwCorrect: Boolean) -> Unit) {
    val passwordFieldVal = remember { mutableStateOf("") }
    Dialog(
        state = rememberDialogState(position = WindowPosition(Alignment.Center), width = 700.dp),
        title = "Passworteingabe",
        onCloseRequest = { result(false) }
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Bitte gib das Passwort ein")
            OutlinedTextField(
                value = passwordFieldVal.value,
                visualTransformation = VisualTransformation {
                    TransformedText(
                        AnnotatedString("*".repeat(passwordFieldVal.value.length)), OffsetMapping.Identity
                    )
                },
                singleLine = true,
                onValueChange = { passwordFieldVal.value = it })
            Button(onClick = { result(passwordFieldVal.value == "test") }) { Text("OK") }
        }
    }

}