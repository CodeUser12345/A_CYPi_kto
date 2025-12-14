import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import model.PasswordEntry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import java.util.UUID

class ModelTest {

    @Test
    fun `test PasswordEntry json serialization`() {
        val originalEntry = PasswordEntry(
            id = UUID.randomUUID().toString(),
            name = "Google",
            login = "test@gmail.com",
            passwordEncrypted = "encryptedString123==",
            url = "https://google.com",
            tags = listOf("work", "social"),
            notes = "My notes",
            isWeak = false
        )

        val jsonString = Json.encodeToString(originalEntry)

        assertTrue(jsonString.contains("Google"))
        assertTrue(jsonString.contains("test@gmail.com"))
        assertTrue(jsonString.contains("work"))

        val restoredEntry = Json.decodeFromString<PasswordEntry>(jsonString)

        assertEquals(originalEntry, restoredEntry)
    }

    @Test
    fun `test serialization handles empty optional fields`() {
        val simpleEntry = PasswordEntry(
            name = "Simple",
            login = "User",
            passwordEncrypted = "123"
        )

        val json = Json.encodeToString(simpleEntry)
        val restored = Json.decodeFromString<PasswordEntry>(json)

        assertEquals(simpleEntry.name, restored.name)
        assertEquals(0, restored.tags.size)
    }
}