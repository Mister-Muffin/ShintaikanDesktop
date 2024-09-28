package composables

import Screen
import Screen.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar

@Composable
fun FrameWindowScope.AppMenuBar(
    screenID: Screen,
    setScreenId: (Screen) -> Unit,
    setFallBackScreenId: (Screen) -> Unit,
    setForwardedScreenId: (Screen) -> Unit,
    dataLoading: Boolean,
    onMigrateTable: () -> Unit,
    exitApplication: () -> Unit
) {
    MenuBar {
        Menu("Datei", mnemonic = 'F', enabled = !dataLoading) {
            Item(
                "Startseite",
                onClick = { setScreenId(HOME) },
                shortcut = KeyShortcut(Key.Escape),
                enabled = screenID != HOME
            )
            Item("Beenden", onClick = { exitApplication() }, mnemonic = 'E')
        }
        Menu("Administration", mnemonic = 'A', enabled = screenID == HOME && !dataLoading) {
            Item(
                "Trainer verwalten",
                onClick = { setFallBackScreenId(screenID); setScreenId(PASSWORD); setForwardedScreenId(MANAGE_TRAINER) })
            Item(
                "Daten importieren",
                onClick = { setFallBackScreenId(screenID); setScreenId(PASSWORD); setForwardedScreenId(FETCH_DATA) })
            Item(
                "Programm aktualisieren",
                onClick = { setFallBackScreenId(screenID); setScreenId(PASSWORD); setForwardedScreenId(UPDATER) })
            Item(
                "Datenbank migrieren",
                onClick = { onMigrateTable() })
        }
        Menu("Mitglieder", mnemonic = 'P', enabled = !dataLoading) {
            Item("Daten abfragen", onClick = { setScreenId(MEMBER_STATS) })
            Item("Daten exportieren", onClick = { setScreenId(EXPORT_MEMBERS) })
        }
        Menu("Hilfe", mnemonic = 'H') {
            Item("Info", onClick = { setScreenId(HELP) })
        }
    }
}
