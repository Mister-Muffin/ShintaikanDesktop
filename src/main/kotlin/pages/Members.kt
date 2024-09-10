package pages

import Screen
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dialogs.PasswordPrompt
import dialogs.StickerDialog
import getTotalTrainingSessions
import gretting
import model.Member
import model.Participation
import next
import stickerUnits
import java.util.*

private const val CHECKBOX_PADDING = 16

@Composable
fun MemberSelector(
    members: List<Member>,
    participations: List<Participation>,
    activeTrainer: Member,
    password: String,
    clearUnitsSinceLastExam: (Member) -> Unit,
    updateSticker: (Member, Int, String) -> Unit,
    incrementTrainerUnits: (Member) -> Unit,
    addParticipation: (participants: String, isExam: Boolean) -> Unit,
    changeScreen: (screen: Screen) -> Unit
) {
    val searchQuery = remember { mutableStateOf("") }
    var handleAsExam by remember { mutableStateOf(false) }

    val allMembers = remember { mutableStateListOf<Member>() }
    val newMembers = remember { mutableStateListOf<Member>() }

    remember { allMembers.addAll(members) }

    val checkedColors = remember { mutableStateListOf<DegreeColor>() }
    val checkedGroups = remember { mutableStateListOf<Group>() }

    fun <T : FilterOption> findMatch(s: String, options: List<T>, exactMach: Boolean): Boolean {
        return if (exactMach) options.any { a -> s.lowercase() == a.databaseName.lowercase() }
        else options.any { a -> s.lowercase().contains(a.databaseName.lowercase()) }
    }

    var showStickerDialog by remember { mutableStateOf(false) }
    var showCheckboxPasswordDialog by remember { mutableStateOf(false) }

    val studentsStickers = remember { mutableListOf<Member>() }

    fun submit(isExam: Boolean) {
        val participants = newMembers.joinToString(",") { it.id.toString() }
        for (member in newMembers) {

            if (isExam) clearUnitsSinceLastExam(member) // set this to 0, so it won't get added in the future

            if (member.receivedStickerNumber != stickerUnits.keys.last()) // Wer 800 aufkelber hat, bekommt keinen weiteren (catch indexOutOfBounds) // TODO: Das sieht schlimm aus, wtf
                if (getTotalTrainingSessions(member, participations) // ALLE Trainingseinheiten
                    >= stickerUnits.next(member.receivedStickerNumber).first
                ) studentsStickers.add(member)
        }
        addParticipation(participants, isExam)
        incrementTrainerUnits(activeTrainer)

        if (studentsStickers.isEmpty()) changeScreen(Screen.SUCCESS)
        else showStickerDialog = true
    }

    val leftLazyState = rememberLazyListState()
    val rightLazyState = rememberLazyListState()

    val greeting = remember { gretting() }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(all = 8.dp)) {

        if (showStickerDialog) {
            StickerDialog(studentsStickers, participations, updateSticker, activeTrainer) {
                showStickerDialog = false
                changeScreen(Screen.SUCCESS)
            }
        }

        if (showCheckboxPasswordDialog) {
            PasswordPrompt(
                password = password,
                cancel = { showCheckboxPasswordDialog = false },
                result = { pwCorrect ->
                    handleAsExam = pwCorrect
                    showCheckboxPasswordDialog = !pwCorrect
                } // if password correct, set requirePasswort to false
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
                        }.sortedBy { it.prename }.sortedByDescending { it.level }.toList()
                    )
                    { /* linke spalte */ student ->
                        ListBox(student) {
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
            Card(elevation = 10.dp) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.width(500.dp).fillMaxHeight().padding(8.dp)
                ) {
                    // Search Field
                    OutlinedTextField(
                        searchQuery.value,
                        singleLine = true,
                        label = {
                            Text(
                                "Suchen",
                                style = TextStyle.Default.copy(fontSize = 16.sp)
                            )
                        },
                        leadingIcon = { Icon(Icons.Default.Search, "Search Icon") },
                        onValueChange = { newVal ->
                            searchQuery.value = newVal.lowercase(Locale.getDefault())
                        },
                        modifier = Modifier.fillMaxWidth(.75f)
                    )
                    Column {
                        CustomFilter(DegreeColor.values(), checkedColors)
                        Divider(modifier = Modifier.padding(vertical = 30.dp))
                        CustomFilter(Group.values(), checkedGroups)
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth().height(60.dp)
                                .clickable {
                                    // just remove the tick if it was checked without password
                                    if (handleAsExam) handleAsExam = false
                                    else showCheckboxPasswordDialog = true
                                }) {
                            Checkbox(
                                checked = handleAsExam,
                                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colors.primary),
                                onCheckedChange = null,
                                modifier = Modifier.padding(CHECKBOX_PADDING.dp)
                            )
                            if (handleAsExam)
                                Text(
                                    text = "Prüfung!", // TODO: Sieht nach schlechter UX aus
                                    textDecoration = TextDecoration.Underline,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 35.sp,
                                    textAlign = TextAlign.Center,
                                )
                            else
                                Text(
                                    "Auswahl als Prüfung eintragen",
                                    textAlign = TextAlign.Center,
                                )
                        }

                        Spacer(Modifier.height(4.dp))
                        // Eingabe bestätigen
                        Button(
                            enabled = newMembers.isNotEmpty(),
                            modifier = Modifier.fillMaxWidth().height(60.dp),
                            onClick = { submit(handleAsExam) }) {
                            Text(
                                textAlign = TextAlign.Center,
                                text = if (newMembers.isEmpty()) "Teilnehmer aus der ersten Spalte auswählen"
                                else "${newMembers.size} Teilnehmer eintragen!"
                            )
                        }
                    }
                }

            }

            Row {
                LazyColumn(state = rightLazyState, modifier = Modifier.fillMaxHeight().width(250.dp)) {
                    items(newMembers.asSequence()
                        .sortedBy { it.prename }
                        .sortedByDescending { it.level }
                        .toList()) { student ->
                        ListBox(student) {
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

@Composable
private fun <T : FilterOption> CustomFilter(filterOptions: Array<T>, checked: MutableList<T>) {
    LazyVerticalGrid(GridCells.Fixed(2)) { // filter
        items(filterOptions) { option ->

            fun handleChecked() {
                //if (farbe.value == option) farbe.value = "" else farbe.value = option
                if (checked.contains(option)) checked.remove(option) else checked.add(option)
            }

            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.width(200.dp)
                    .clickable { handleChecked() }) {
                Checkbox(
                    checked = checked.contains(option),
                    colors = when (option) {
                        is DegreeColor -> option.checkboxColors
                        else -> CheckboxDefaults.colors(MaterialTheme.colors.primary)
                    },
                    onCheckedChange = null,
                    modifier = Modifier.padding(CHECKBOX_PADDING.dp)
                )
                Text(text = option.optionName, modifier = Modifier.fillMaxWidth(.5f))
            }

        }
    }
}

@Composable
private fun ListBox(member: Member, onBoxClicked: () -> Unit) {
    Box(
        modifier = Modifier
            .width(250.dp)
            .height(25.dp)
            .drawWithCache {
                val gradient = Brush.horizontalGradient(
                    colors = DegreeColor.getColorList(member.level).let { list ->
                        if (list.size < 2) list + list
                        else list
                    },
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
            color = if (DegreeColor.getDegreeList(member.level).first()?.isDark == true)
                Color.White
            else
                Color.Black,
            modifier = Modifier.padding(start = 8.dp),
            text = "${member.prename} ${member.surname}"
        )
    }
}

enum class DegreeColor(
    private val color: Color,
    override val optionName: String,
    val isDark: Boolean,
    private val checkmarkColor: Color? = null
) : FilterOption {
    WHITE(color = Color.White, optionName = "Weiß", false, checkmarkColor = Color.Black) {
        override val databaseName = "Weiss"
    },
    YELLOW(color = Color(0xffffff35), optionName = "Gelb", isDark = false, checkmarkColor = Color.Black),
    RED(color = Color(0xffff0004), optionName = "Rot", isDark = false),
    ORANGE(color = Color(0xffffaa00), optionName = "Orange", isDark = false),
    GREEN(color = Color(0xff00aa00), optionName = "Grün", isDark = false),
    BLUE(color = Color(0xff0055ff), optionName = "Blau", isDark = true),
    PURPLE(color = Color(0xff5500ff), optionName = "Violett", isDark = true),
    BROWN(color = Color(0xffaa5500), optionName = "Braun", isDark = true),
    BLACK(color = Color.Black, optionName = "Schwarz", isDark = true);

    val checkboxColors
        @Composable get() = if (checkmarkColor == null) CheckboxDefaults.colors(checkedColor = color) else CheckboxDefaults.colors(
            checkedColor = color,
            checkmarkColor = checkmarkColor
        )

    companion object {
        fun getDegreeList(level: String) = level.trim().split(" ").last().split("-").map {
            // Could maybe use some better fallback or error
            values().find { color -> color.databaseName.lowercase() == it.lowercase() }
        }

        // TODO: This still isn't really nice, but a definite improvement
        fun getColorList(level: String) = getDegreeList(level).map {
            // Could maybe use some better fallback or error
            it?.color ?: Color.Transparent
        }
    }
}

interface FilterOption {
    val optionName: String
    val databaseName: String
        get() = optionName
}

private enum class Group(override val optionName: String, override val databaseName: String = optionName) :
    FilterOption {
    BENJAMINI("Karamini", databaseName = "Benjamini"),
    KIDS("Kinder Karate"),
    YOUTH("Jugend Karate"),
    NORMAL("Karate")
}
