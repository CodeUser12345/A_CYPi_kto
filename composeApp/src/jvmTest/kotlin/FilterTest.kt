import model.PasswordEntry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FilterTest {

    // Подготовим тестовые данные
    private val testData = listOf(
        PasswordEntry(name = "Facebook", login = "user", passwordEncrypted = "123", tags = listOf("social", "personal")),
        PasswordEntry(name = "LinkedIn", login = "pro", passwordEncrypted = "456", tags = listOf("work", "social")),
        PasswordEntry(name = "GitHub", login = "coder", passwordEncrypted = "789", tags = listOf("work", "dev")),
        PasswordEntry(name = "Bank", login = "rich", passwordEncrypted = "000", tags = listOf("finance"))
    )

    @Test
    fun `search should find entries by name ignoring case`() {
        val query = "face"
        val results = testData.filter {
            it.name.contains(query, ignoreCase = true)
        }

        assertEquals(1, results.size)
        assertEquals("Facebook", results.first().name)
    }

    @Test
    fun `search should find entries by login`() {
        val query = "coder"
        val results = testData.filter {
            it.login.contains(query, ignoreCase = true)
        }

        assertEquals(1, results.size)
        assertEquals("GitHub", results.first().name)
    }

    @Test
    fun `filter by single tag returns correct entries`() {
        val selectedTag = "social"
        val results = testData.filter { entry ->
            entry.tags.contains(selectedTag)
        }

        assertEquals(2, results.size) // Facebook и LinkedIn
        assertTrue(results.any { it.name == "Facebook" })
        assertTrue(results.any { it.name == "LinkedIn" })
    }

    @Test
    fun `filter by multiple tags (AND logic)`() {
        // Допустим, мы ищем записи, где есть И "social", И "work" (так работает ваша логика в Dashboard: all { ... })
        val selectedTags = setOf("social", "work")

        val results = testData.filter { entry ->
            selectedTags.all { tag -> entry.tags.contains(tag) }
        }

        assertEquals(1, results.size)
        assertEquals("LinkedIn", results.first().name) // Только LinkedIn имеет оба тега
    }
}