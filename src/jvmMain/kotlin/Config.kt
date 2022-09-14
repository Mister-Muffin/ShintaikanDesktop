data class Config(
    val settings: Settings,
) {
    data class Settings(
        val ip: String,
        val port: String,
        val user: String,
        val password: String,
        val database: String
    )
}
