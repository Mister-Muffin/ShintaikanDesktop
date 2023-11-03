package composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.onClick
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import showFileDialog


/**
 * @param onProceed called when the dialog window closes
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FilePicker(
    window: ComposeWindow,
    onProceed: (path: String) -> Unit
) {
    var path by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            path,
            readOnly = true,
            onValueChange = { path = it },
            trailingIcon = {
                Icon(
                    Icons.Outlined.FolderOpen,
                    "",
                    modifier = Modifier.onClick {
                        showFileDialog(window) { directory, name ->
                            if (!directory.isNullOrEmpty() && !name.isNullOrEmpty()) {
                                path = directory + name
                                onProceed(path)
                            } else {
                                path = ""
                                onProceed(path)
                            }
                        }
                    })
            }
        )


    }

}

/**
 * Checks if a filename matches the given extension
 * @return The according status
 * @see States
 */
fun getStatus(path: String, fileExtension: String): States {
    return if (path.isEmpty()) {
        States.NO_FILE_SELECTED
    } else if (checkFileExtension(path, fileExtension)) {
        States.READY
    } else {
        States.ILLEGAL_FILE
    }
}

/**
 * @param fileName the filename to check
 * @param extension to check against
 * @return if the end of the provided String
 * maches the provided extension
 */
fun checkFileExtension(fileName: String, extension: String): Boolean {
    return fileName.lowercase().endsWith(extension)
}

enum class States(val status: String) {
    NO_FILE_SELECTED("Keine Datei ausgewählt"),
    ILLEGAL_FILE("Falsche Datei!"),
    BUSY("Bitte warten..."),
    READY("Bereit"),
    DONE("Fertig")
}

class IllegalFileException(s: String) : Throwable(s)
