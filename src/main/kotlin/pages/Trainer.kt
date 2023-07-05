package pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import models.Trainer

@Composable
fun trainerSelector(trainers: List<Trainer>, changeScreen: (id: Int, activeTrainer: Trainer?) -> Unit) {


    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(all = 8.dp)) {
        Text("Wer bist du?", style = MaterialTheme.typography.h1)
        Divider(modifier = Modifier.padding(vertical = 16.dp))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxSize(),
        ) {
            var selectedTrainer: Trainer? by remember { mutableStateOf(null) }
            var selectedCotrainer: Trainer? by remember { mutableStateOf(null) }

            LazyVerticalGrid(GridCells.Fixed(4)) {
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
                            selectedTrainer = trainer; if (selectedCotrainer == trainer) selectedCotrainer = null
                        }.padding(24.dp)
                    ) {
                        RadioButton(
                            selectedTrainer == trainer,
                            onClick = null,
                            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colors.primary),
                            modifier = Modifier.size(32.dp)
                        )
                        Text(trainerNameExtended)
                    }
                }
            }
            Button(
                enabled = selectedTrainer != null, modifier = Modifier.width(350.dp).height(60.dp),
                onClick = { if (selectedTrainer != null) changeScreen(2, selectedTrainer) }
            ) {
                Text("Weiter")
            }
        }
    }
}
