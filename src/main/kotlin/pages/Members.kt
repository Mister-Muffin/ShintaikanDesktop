package pages

import Screen
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.NoteAdd
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dialogs.AddNoteDialog
import dialogs.PasswordPrompt
import dialogs.StickerDialog
import dialogs.ViewNotesDialog
import getTotalTrainingSessions
import gretting
import model.Member
import model.Participation
import model.withNotesForMember
import next
import stickerUnits
import java.util.*
import kotlin.random.Random

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
    addParticipation: (participant: Member, isExam: Boolean, note: String, trainerId: Int) -> Unit,
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
    var studentNoteEdit: Member? by remember { mutableStateOf(null) }
    var studentNoteView: Member? by remember { mutableStateOf(null) }
    var showCheckboxPasswordDialog by remember { mutableStateOf(false) }

    val studentNotesMap = remember { mutableMapOf<Member, String>() }

    val studentsStickers = remember { mutableListOf<Member>() }

    fun submit(isExam: Boolean) {
        val participants = newMembers.toList()

        for (member in newMembers) {
            if (isExam) clearUnitsSinceLastExam(member) // set this to 0, so it won't get added in the future

            if (member.receivedStickerNumber != stickerUnits.keys.last()) // Wer 800 aufkelber hat, bekommt keinen weiteren (catch indexOutOfBounds)
                if (getTotalTrainingSessions(member, participations) // ALLE Trainingseinheiten
                    >= stickerUnits.next(member.receivedStickerNumber).first
                ) studentsStickers.add(member)
        }

        // Add all participants
        participants.forEach { participant ->
            addParticipation(participant, isExam, studentNotesMap[participant] ?: "", activeTrainer.id)
        }

        incrementTrainerUnits(activeTrainer)

        if (studentsStickers.isEmpty()) changeScreen(Screen.SUCCESS)
        else showStickerDialog = true
    }

    val leftLazyState = rememberLazyListState()
    val rightLazyState = rememberLazyListState()

    val greeting = remember { gretting() }
    val infiniteTransition = rememberInfiniteTransition()
    val showEasterEgg by remember {
        mutableStateOf(
            activeTrainer.prename == "Rüdiger" &&
                    activeTrainer.surname == "Walz" &&
                    Random.nextInt(1, 10 + 1) == 1
        )
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(all = 8.dp)) {

        if (showStickerDialog) {
            StickerDialog(studentsStickers, participations, updateSticker, activeTrainer) {
                showStickerDialog = false
                changeScreen(Screen.SUCCESS)
            }
        }

        if (studentNoteEdit != null) {
            AddNoteDialog(studentNotesMap[studentNoteEdit!!] ?: "") { note, save ->
                if (save) {
                    studentNotesMap[studentNoteEdit!!] = note
                }
                studentNoteEdit = null
            }
        }
        if (studentNoteView != null) {
            ViewNotesDialog(studentNoteView, participations.withNotesForMember(studentNoteView!!)) {
                studentNoteView = null
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
        if (showEasterEgg) {
            // Infinite transition to animate color back and forth
            val animatedColor by infiniteTransition.animateColor(
                initialValue = MaterialTheme.typography.h1.color,
                targetValue = Color.Red,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 10000),  // Duration of one color transition
                    repeatMode = RepeatMode.Reverse  // Animate back and forth
                )
            )
            Text(
                "$greeting ${activeTrainer.prename}, du hast's drauf!",
                style = MaterialTheme.typography.h1,
                color = animatedColor
            )
        } else
            Text("$greeting ${activeTrainer.prename}, bitte Teilnehmer auswählen", style = MaterialTheme.typography.h1)
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
                        val notes = participations.withNotesForMember(student)
                        ListBox(student, notes, { studentNoteView = student }) {
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
                        CustomFilter(DegreeColor.entries.toTypedArray(), checkedColors)
                        Divider(modifier = Modifier.padding(vertical = 30.dp))
                        CustomFilter(Group.entries.toTypedArray(), checkedGroups)
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
                                    text = "Prüfung!",
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
                        ListBox(student, null, { studentNoteEdit = student }) {
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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ListBox(
    member: Member,
    notes: List<Participation>? = null,
    onButtonClicked: () -> Unit,
    onBoxClicked: () -> Unit
) {
    var pointerHover by remember { mutableStateOf(false) }

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
            .clickable { onBoxClicked() }
            .onPointerEvent(PointerEventType.Enter) {
                pointerHover = true
            }
            .onPointerEvent(PointerEventType.Exit) {
                pointerHover = false
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Text(
                fontSize = 12.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.W500,
                color = if (DegreeColor.getDegreeList(member.level).first()?.isDark == true)
                    Color.White
                else
                    Color.Black,
                modifier = Modifier,
                text = "${member.prename} ${member.surname}"
            )
            // if "notes" is null, the Box is on the right side, and the add note button should show on hover.
            // Otherwise, the list would at least be empty, meaning the Box is on the left side
            // and the note icon will be shown if necessary
            if (pointerHover && notes == null) {
                IconButton(onClick = onButtonClicked, modifier = Modifier.size(22.dp).padding(0.dp)) {
                    Icon(Icons.Outlined.NoteAdd, null)
                }
            } else if (!notes.isNullOrEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier.clickable { onButtonClicked() }.width(40.dp)
                ) {
                    IconButton(onClick = onButtonClicked, modifier = Modifier.size(22.dp).padding(0.dp)) {
                        Icon(Icons.Outlined.Description, null)
                    }
                    Text(notes.size.toString().takeUnless { notes.size > 3 } ?: "3+", fontSize = 10.sp)
                }
            }
        }
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
            entries.find { color -> color.databaseName.lowercase() == it.lowercase() }
        }

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
