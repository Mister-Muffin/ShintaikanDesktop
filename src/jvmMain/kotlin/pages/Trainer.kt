import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import models.Trainer

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TrainerSelector(trainers: List<Trainer>, changeScreen: (id: Int) -> Unit) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,

        modifier = Modifier.fillMaxSize(),
    ) {
        Text("Wer bist du?")
        var selectedTrainer: Trainer? by remember { mutableStateOf(null) }
        var selectedCotrainer: Trainer? by remember { mutableStateOf(null) }

        LazyVerticalGrid(
            cells = GridCells.Fixed(4)
        ) {
            items(trainers.filter { !it.onlyCotrainer }) { trainer ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        selectedTrainer = trainer; if (selectedCotrainer == trainer) selectedCotrainer = null
                    }.padding(24.dp)
                ) {
                    RadioButton(
                        selectedTrainer == trainer,
                        onClick = null,
                        colors = RadioButtonDefaults.colors(selectedColor = Color.Gray),
                        modifier = Modifier.size(32.dp)
                    )
                    Text(trainer.name)
                }
            }
        }
        Text("Cotrainer")
        var cotrainerExpanded by remember { mutableStateOf(false) }
        Row {
            Text(
                text = selectedCotrainer?.name ?: "Keiner",
                modifier = Modifier.clickable { cotrainerExpanded = !cotrainerExpanded })
            DropdownMenu(
                expanded = cotrainerExpanded,
                onDismissRequest = { cotrainerExpanded = false }
            ) {
                DropdownMenuItem(onClick = {
                    selectedCotrainer = null; cotrainerExpanded = false
                }) { Text("Keiner") }
                Divider()
                for (cotrainer in trainers.filter { it != selectedTrainer }) {
                    DropdownMenuItem(onClick = { selectedCotrainer = cotrainer; cotrainerExpanded = false }) {
                        Text(
                            cotrainer.name
                        )
                    }
                }
            }
        }
        Button(
            enabled = selectedTrainer != null,
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray, contentColor = Color.White),
            modifier = Modifier.width(350.dp).height(60.dp),
            onClick = { changeScreen(2) }
        ) {
            Text("Weiter")
        }
    }
}