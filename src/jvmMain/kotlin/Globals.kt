val stickerUnits = mapOf(
    0 to "", 25 to "Schlange", 50 to "Tiger", 75 to "Rabe", 100 to "Drache", 150 to "Adler",
    200 to "Fuchs", 300 to "Phoenix", 500 to "Gottesanbeterin", 800 to "Reier"
)

fun Int.nextStickerUnit() = stickerUnits.next(this)
fun Pair<Int, String?>.nextStickerUnit() = first.nextStickerUnit()

val levels = mapOf(
    "z Kyu weiss" to LevelRequirements(units = 0),
    "9/10 Kyu weiss-rot" to LevelRequirements(months = 3, units = 10),
    "9. Kyu weiss-gelb" to LevelRequirements(units = 10),
    "8/9 Kyu gelb-rot" to LevelRequirements(units = 10),
    "8. Kyu gelb" to LevelRequirements(months = 3, units = 10, age = 7),
    "7/8 Kyu gelb-orange" to LevelRequirements(units = 15),
    "7. Kyu orange" to LevelRequirements(months = 4, units = 20, age = 9),
    "6/7 Kyu orange-grün" to LevelRequirements(units = 22),
    "6. Kyu grün" to LevelRequirements(months = 5, units = 30, age = 11),
    "5/6 Kyu grün-blau" to LevelRequirements(units = 22),
    "5. Kyu blau" to LevelRequirements(months = 5, units = 30, age = 13),
    "4. Kyu violett" to LevelRequirements(months = 8, units = 45, age = 14),
    "3. Kyu braun" to LevelRequirements(months = 8, units = 45, age = 15),
    "2. Kyu braun" to LevelRequirements(months = 8, units = 45, age = 16),
    "1. Kyu braun" to LevelRequirements(months = 9, units = 60, age = 17),
    "1. Dan schwarz" to LevelRequirements(months = 10, units = 60, age = 18)
)


data class LevelRequirements(
    val months: Long = 0,
    val units: Int,
    val age: Int = 0
)
