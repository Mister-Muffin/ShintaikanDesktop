package pages

import Screen
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import models.Trainer

const val COMPONENT_WIDTH = 250

@Composable
fun TrainerSelector(trainers: List<Trainer>, changeScreen: (screen: Screen, activeTrainer: Trainer) -> Unit) {

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(all = 8.dp)) {
        Text("Wer bist du?", style = MaterialTheme.typography.h1)
        Divider(modifier = Modifier.padding(vertical = 16.dp))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxSize(),
        ) {
            var selectedTrainer: Trainer? by remember { mutableStateOf(null) }

            LazyColumn() {
                items(trainers.sortedBy { it.prename }) { trainer ->
                    var trainerNameExtended = trainer.prename
                    var trainerNameWasExtended = false
                    trainers.forEach { t ->
                        var i = 0
                        if (trainer.prename == t.prename && trainer.surname != t.surname) {
                            while ((t.prename + " " + t.surname).contains(trainerNameExtended)) {
                                if (trainerNameExtended == trainer.prename) trainerNameExtended += " "
                                trainerNameExtended += trainer.surname[i]
                                i++
                                trainerNameWasExtended = true
                            }
                        }
                    }
                    if (trainerNameWasExtended) trainerNameExtended += "."
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            selectedTrainer = trainer
                        }.padding(24.dp).width(COMPONENT_WIDTH.dp)
                    ) {
                        RadioButton(
                            selectedTrainer == trainer,
                            onClick = null,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(trainerNameExtended)
                    }
                }
            }

            Button(
                enabled = selectedTrainer != null, modifier = Modifier.width(COMPONENT_WIDTH.dp),
                onClick = { if (selectedTrainer != null) changeScreen(Screen.SELECT_MEMBER, selectedTrainer!!) }
            ) {
                Text("Weiter")
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, "")
            }
        }
    }
}
