import data.DatabaseManager
import model.PasswordEntry
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class DatabaseManagerTest {

    @BeforeTest
    fun setup() {
        // Используем уникальный временный файл для каждого запуска теста.
        val testDbName = "test_db_${System.nanoTime()}.db"

        // Подключаемся к файловой БД
        Database.connect("jdbc:sqlite:$testDbName", "org.sqlite.JDBC")

        transaction {
            // Создаем таблицы
            SchemaUtils.drop(DatabaseManager.PasswordEntries, DatabaseManager.TagsTable, DatabaseManager.Settings)
            SchemaUtils.create(DatabaseManager.PasswordEntries, DatabaseManager.TagsTable, DatabaseManager.Settings)
        }

        // Удаляем файл базы данных после завершения тестов, чтобы не мусорить на диске
        File(testDbName).deleteOnExit()
    }

    @Test
    fun `savePassword should update existing entry if ID matches`() {
        val id = "unique-id-123"
        val entry1 = PasswordEntry(id = id, name = "OldName", login = "OldLogin", passwordEncrypted = "123")

        // 1. Транзакция на запись
        DatabaseManager.savePassword(entry1)

        val entry2 = entry1.copy(name = "NewName", login = "NewLogin")
        // 2. Транзакция на обновление (БД не удалится, так как она в файле)
        DatabaseManager.savePassword(entry2)

        // 3. Транзакция на чтение
        val all = DatabaseManager.getAllPasswords()
        assertEquals(1, all.size, "Should be only 1 entry because IDs match")
        assertEquals("NewName", all.first().name, "Name should be updated")
    }

    @Test
    fun `deleteTag should remove tag from PasswordEntries`() {
        val entry = PasswordEntry(
            name = "Job",
            login = "me",
            passwordEncrypted = "123",
            tags = listOf("work", "important")
        )
        DatabaseManager.savePassword(entry)
        DatabaseManager.saveTag("work")
        DatabaseManager.saveTag("important")

        DatabaseManager.deleteTag("work")

        val updatedEntry = DatabaseManager.getAllPasswords().first()

        assertEquals(1, updatedEntry.tags.size)
        assertTrue(updatedEntry.tags.contains("important"))
        assertFalse(updatedEntry.tags.contains("work"))
    }

    @Test
    fun `settings should save and retrieve values`() {
        DatabaseManager.saveSetting("theme", "dark")
        val value = DatabaseManager.getSetting("theme")

        assertEquals("dark", value)

        val missing = DatabaseManager.getSetting("language")
        assertNull(missing)
    }
}