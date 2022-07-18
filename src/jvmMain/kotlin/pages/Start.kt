package pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import models.Trainer

@Composable
fun StartPage(trainers: List<Trainer>, changeScreen: (id: Int) -> Unit) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,

        modifier = Modifier.fillMaxSize(),
    ) {
        
    }
}