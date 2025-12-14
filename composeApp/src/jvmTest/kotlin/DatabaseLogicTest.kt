import data.DatabaseManager
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DatabaseLogicTest {

    @BeforeTest
    fun setup() {
        // Подключаемся к базе данных В ПАМЯТИ
        Database.connect("jdbc:sqlite::memory:", "org.sqlite.JDBC")
    }

    @Test
    fun `test database schema creation and insert`() {
        transaction {
            SchemaUtils.create(DatabaseManager.PasswordEntries)

            DatabaseManager.PasswordEntries.insert {
                it[id] = "uuid-1"
                it[name] = "TestService"
                it[login] = "Login"
                it[passwordEncrypted] = "Pass"
                it[createdAt] = System.currentTimeMillis()
                it[updatedAt] = System.currentTimeMillis()
            }

            val count = DatabaseManager.PasswordEntries.selectAll().count()
            assertEquals(1, count)

            val row = DatabaseManager.PasswordEntries.select {
                DatabaseManager.PasswordEntries.id eq "uuid-1"
            }.single()

            assertEquals("TestService", row[DatabaseManager.PasswordEntries.name])
        }
    }

    @Test
    fun `test tags relationship logic`() {
        transaction {
            SchemaUtils.create(DatabaseManager.TagsTable)

            val tagName = "Finance"
            DatabaseManager.TagsTable.insert {
                it[name] = tagName
            }

            val tags = DatabaseManager.TagsTable.selectAll().map { it[DatabaseManager.TagsTable.name] }
            assertTrue(tags.contains("Finance"))
            assertEquals(1, tags.size)
        }
    }
}