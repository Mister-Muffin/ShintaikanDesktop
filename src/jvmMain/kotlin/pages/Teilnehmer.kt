package pages

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dialogs.passwordPrompt
import dialogs.stickerDialog
import getTotalTrainingSessions
import gretting
import models.*
import next
import stickerUnits
import java.util.*

private val farben = arrayOf("Weiss", "Gelb", "Orange", "Grün", "Blau", "Violett", "Braun", "Schwarz")

@Composable
fun teilnehmerSelector(members: List<Member>, activeTrainer: Trainer, changeScreen: (id: Int) -> Unit) {

    val searchQuery = remember { mutableStateOf("") }
    var handleAsExam by remember { mutableStateOf(false) }

    val newMembers = remember { mutableStateListOf<Member>() }
    val allMembers = remember { mutableStateListOf<Member>() }
    remember {
        for (student in members) {
            allMembers.add(student)
        }
    }

    val teilnahme = loadTeilnahme()

    val checkedColors = remember { mutableStateListOf<String>() }

    val groups = arrayOf("Benjamini", "Kinder Karate", "Jugend Karate", "Karate")
    val checkedGroups = remember { mutableStateListOf<String>() }

    fun findMatch(s: String, strings: List<String>, exactMach: Boolean): Boolean {
        return if (exactMach) strings.any { a -> s.lowercase() == a.lowercase() }
        else strings.any { a -> s.lowercase().contains(a.lowercase()) }
    }

    var showStickerDialog by remember { mutableStateOf(false) }
    var showCheckboxPasswordDialog by remember { mutableStateOf(false) }

    val studentsStickers = remember { mutableListOf<Member>() }

    fun submit(isExam: Boolean) {
        var teilnahmeString = ""
        for (student in newMembers) {
            teilnahmeString = teilnahmeString + student.id + ","

            if (student.sticker_recieved != stickerUnits.keys.last()) // Wer 800 aufkelber hat, bekommt keinen weiteren (catch indexOutOfBounds)
                if (getTotalTrainingSessions(student, teilnahme) // ALLE Trainingseinheiten
                    >= stickerUnits.next(student.sticker_recieved).first
                ) studentsStickers.add(student)
        }
        insertTeilnahme(teilnahmeString, isExam)
        increaseTrainerUnitCount(activeTrainer)

        if (studentsStickers.isEmpty()) changeScreen(3)
        else showStickerDialog = true
    }

    val leftLazyState = rememberLazyListState()
    val rightLazyState = rememberLazyListState()

    val greeting = remember { gretting() }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(all = 8.dp)) {

        if (showStickerDialog) {
            stickerDialog(studentsStickers, activeTrainer) {
                // hier werden alle member wieder aufgelistet, bei denen 'sticker_show_again' true war
                it.forEach { member ->
                    if (member.sticker_show_again) {
                        studentsStickers.clear()
                        it.forEach { studentsStickers.add(it) }
                    } else {
                        showStickerDialog = false
                        changeScreen(3)
                    }
                }
            }
        }

        if (showCheckboxPasswordDialog) {
            passwordPrompt(
                result = { pwCorrect ->
                    handleAsExam = pwCorrect
                    showCheckboxPasswordDialog = !pwCorrect
                }, // if password correct, set requirePasswort to false
                onDissmiss = { showCheckboxPasswordDialog = false }
            )
        }

        Text("$greeting ${activeTrainer.prename}, Teilnehmer auswählen", style = MaterialTheme.typography.h1)
        Divider(modifier = Modifier.padding(vertical = 16.dp))
        Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxSize()) {
            Row {
                LazyColumn(state = leftLazyState, modifier = Modifier.fillMaxHeight().width(250.dp)) {
                    items(allMembers.asSequence()
                        .filter { s ->
                            // filter color checkboxes
                            if (checkedColors.isEmpty()) true
                            else findMatch(s.level, checkedColors, false)
                        }.filter { s ->
                            // filter group checkboxes on top
                            if (checkedGroups.isEmpty()) true
                            else findMatch(s.group, checkedGroups, true)
                        }.filter {
                            // filter again for search ->
                            arrayListOf(
                                it.prename.lowercase(Locale.getDefault()),
                                it.surname.lowercase(Locale.getDefault())
                            ).joinToString(" ") // "prename surname"
                                .contains(searchQuery.value)
                            // <- filter again for search
                        }.sortedByDescending { it.id }.sortedByDescending { it.level }.toList()
                    )
                    { /* linke spalte */ student ->
                        listBox(student) {
                            newMembers.add(student)
                            allMembers.remove(student)
                            searchQuery.value = ""
                        }
                        Divider(modifier = Modifier.width(250.dp))
                    }
                }
                VerticalScrollbar(
                    modifier = Modifier.fillMaxHeight(),
                    adapter = rememberScrollbarAdapter(
                        scrollState = leftLazyState
                    )
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.width(500.dp).fillMaxHeight()
            ) {
                Column { // search column
                    Text("Suchen:")
                    TextField(searchQuery.value, onValueChange = { newVal ->
                        searchQuery.value = newVal.lowercase(Locale.getDefault())
                    })
                }
                Column {
                    customFilter(farben, checkedColors)
                    Divider(modifier = Modifier.padding(vertical = 30.dp))
                    customFilter(groups, checkedGroups)
                }
                Column {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(4.dp)
                            .clickable {
                                // just remove the tick if it was checked without password
                                if (handleAsExam) handleAsExam = false
                                else showCheckboxPasswordDialog = true
                            }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = handleAsExam,
                                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colors.primary),
                                onCheckedChange = {
                                    // just remove the tick if it was checked without password
                                    if (handleAsExam) handleAsExam = false
                                    else showCheckboxPasswordDialog = true

                                })
                            if (handleAsExam)
                                Text(
                                    text = "Prüfung!",
                                    textDecoration = TextDecoration.Underline,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 35.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth(.9f)
                                )
                            else
                                Text(
                                    "Auswahl als Prüfung eintragen",
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth(.9f)
                                )
                        }
                    }

                    Button( // eingabe bestätigen
                        enabled = newMembers.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        onClick = { submit(handleAsExam) }) {
                        Text(
                            textAlign = TextAlign.Center,
                            text = if (newMembers.isEmpty()) "Teilnehmen aus der ersten Spalte auswählen" else "Eingabe bestätigen!"
                        )
                    }
                }

            }

            Row {
                LazyColumn(state = rightLazyState, modifier = Modifier.fillMaxHeight().width(250.dp)) {
                    items(newMembers) { student ->
                        listBox(student) {
                            allMembers.add(student)
                            newMembers.remove(student)
                        }
                        Divider(modifier = Modifier.width(250.dp))
                    }
                }
                VerticalScrollbar(
                    modifier = Modifier.fillMaxHeight(),
                    adapter = rememberScrollbarAdapter(
                        scrollState = rightLazyState
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun customFilter(filterOptions: Array<String>, checked: MutableList<String>) {
    LazyVerticalGrid(cells = GridCells.Fixed(2)) { // filter
        items(filterOptions) { option ->

            fun handleChecked() {
                //if (farbe.value == option) farbe.value = "" else farbe.value = option
                if (checked.contains(option)) checked.remove(option) else checked.add(option)
            }

            Box(
                modifier = Modifier.width(200.dp)
                    .clickable { handleChecked() }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = checked.contains(option),
                        colors = getCheckBoxColor(option),
                        onCheckedChange = { handleChecked() },
                    )
                    Text(text = if (option == "Benjamini") "Karamini" else option)
                }
            }
        }
    }
}

@Composable
private fun getCheckBoxColor(option: String): CheckboxColors {
    when (option) {
        farben[0] -> {
            return CheckboxDefaults.colors(checkedColor = DEGREECOLORS.WHITE.color, checkmarkColor = Color.Black)
        }

        farben[1] -> {
            return CheckboxDefaults.colors(checkedColor = DEGREECOLORS.YELLOW.color, checkmarkColor = Color.Black)
        }

        farben[2] -> {
            return CheckboxDefaults.colors(checkedColor = DEGREECOLORS.ORANGE.color)
        }

        farben[3] -> {
            return CheckboxDefaults.colors(checkedColor = DEGREECOLORS.GREEN.color)
        }

        farben[4] -> {
            return CheckboxDefaults.colors(checkedColor = DEGREECOLORS.BLUE.color)
        }

        farben[5] -> {
            return CheckboxDefaults.colors(checkedColor = DEGREECOLORS.PURPLE.color)
        }

        farben[6] -> {
            return CheckboxDefaults.colors(checkedColor = DEGREECOLORS.BROWN.color)
        }

        else -> return CheckboxDefaults.colors(MaterialTheme.colors.primary)
    }
}

@Composable
private fun listBox(member: Member, onBoxClicked: () -> Unit) {
    Box(
        modifier = Modifier
            .width(250.dp)
            .height(25.dp)
            .drawWithCache {
                val gradient = Brush.horizontalGradient(
                    colors = listOf(
                        boxColor(member)[0],
                        boxColor(member)[1]
                    ),
                    startX = size.width / 2 - 1,
                    endX = size.width / 2 + 1,
                )
                onDrawBehind {
                    drawRect(gradient)
                }
            }
            .clickable { onBoxClicked() },
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            fontSize = 12.sp,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.W500,
            color = if (
                member.level.contains("5. Kyu blau") ||
                member.level.contains("4. Kyu violett") ||
                member.level.contains(". Kyu braun") ||
                member.level.contains(". Dan schwarz")
            ) Color.White
            else Color.Black,
            modifier = Modifier.padding(start = 8.dp),
            text = "${member.prename} ${member.surname}"
        )
    }
}

private fun boxColor(member: Member): Array<Color> {
    val boxColor: Array<Color> = when {
        member.level.contains("z Kyu weiss") -> {
            arrayOf(DEGREECOLORS.WHITE.color, DEGREECOLORS.WHITE.color)
        }

        member.level.contains("9. Kyu weiss-gelb") -> {
            arrayOf(DEGREECOLORS.WHITE.color, DEGREECOLORS.YELLOW.color)
        }

        member.level.contains("9/10 Kyu  weiss-rot") -> {
            arrayOf(DEGREECOLORS.WHITE.color, DEGREECOLORS.RED.color)
        }

        member.level.contains("8. Kyu gelb") -> {
            arrayOf(DEGREECOLORS.YELLOW.color, DEGREECOLORS.YELLOW.color)
        }

        member.level.contains("7. Kyu orange") -> {
            arrayOf(DEGREECOLORS.ORANGE.color, DEGREECOLORS.ORANGE.color)
        }

        member.level.contains("7/8 Kyu gelb-orange") -> {
            arrayOf(DEGREECOLORS.YELLOW.color, DEGREECOLORS.ORANGE.color)
        }

        member.level.contains("6. Kyu grün") -> {
            arrayOf(DEGREECOLORS.GREEN.color, DEGREECOLORS.GREEN.color)
        }

        member.level.contains("6/7 Kyu orange-grün") -> {
            arrayOf(DEGREECOLORS.ORANGE.color, DEGREECOLORS.GREEN.color)
        }

        member.level.contains("5. Kyu blau") -> {
            arrayOf(DEGREECOLORS.BLUE.color, DEGREECOLORS.BLUE.color)
        }

        member.level.contains("5/6 Kyu grün-blau") -> {
            arrayOf(DEGREECOLORS.GREEN.color, DEGREECOLORS.BLUE.color)
        }

        member.level.contains("4. Kyu violett") -> {
            arrayOf(DEGREECOLORS.PURPLE.color, DEGREECOLORS.PURPLE.color)
        }

        member.level.contains(". Kyu braun") -> {
            arrayOf(DEGREECOLORS.BROWN.color, DEGREECOLORS.BROWN.color)
        }

        member.level.contains(". Dan schwarz") -> {
            arrayOf(DEGREECOLORS.BLACK.color, DEGREECOLORS.BLACK.color)
        }

        else -> {
            arrayOf(Color.White, Color.White)
        }
    }
    return boxColor
}

enum class DEGREECOLORS(val color: Color) {
    WHITE(Color.White),
    YELLOW(Color(0xffffff35)),
    RED(Color(0xffff0004)),
    ORANGE(Color(0xffffaa00)),
    GREEN(Color(0xff00aa00)),
    BLUE(Color(0xff0055ff)),
    PURPLE(Color(0xff5500ff)),
    BROWN(Color(0xffaa5500)),
    BLACK(Color.Black)
}
