/* import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class PasswordEntry(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val login: String,
    val passwordEncrypted: String, // Храним зашифрованным
    val url: String,
    val folder: String = "Основная",
    val tags: List<String> = emptyList(),
    val notes: String = "",
    val isWeak: Boolean = false
)

enum class ScreenState {
    LOGIN, DASHBOARD
}

 */