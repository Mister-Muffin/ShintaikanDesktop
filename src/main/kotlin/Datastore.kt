import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Serializable
data class Datastore(
    val lastImport: String = LocalDateTime.of(LocalDate.EPOCH, LocalTime.MIDNIGHT).toString()
) {
    private val lastImportDate: LocalDateTime
        get() = LocalDateTime.parse(lastImport)
    val lastImportPretty: String
        get() = DateTimeFormatter.ofPattern("EEEE, dd.MM.yyyy HH:mm:ss").format(lastImportDate)
}
