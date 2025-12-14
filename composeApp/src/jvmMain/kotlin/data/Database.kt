package data

import model.PasswordEntry
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.util.*

object DatabaseManager {
    private const val DB_NAME = "passwords.db"

    private fun getConnection() {
        val dbFile = File(System.getProperty("user.home"), ".password-manager")
        if (!dbFile.exists()) dbFile.mkdirs()

        Database.connect("jdbc:sqlite:${dbFile.absolutePath}/$DB_NAME", "org.sqlite.JDBC")
    }

    init {
        getConnection()
        createTables()
    }

    private fun createTables() {
        transaction {
            SchemaUtils.create(PasswordEntries, Settings)
        }
    }

    object PasswordEntries : Table("passwords") {
        val id = varchar("id", 36)
        val name = varchar("name", 255)
        val login = varchar("login", 255).nullable()
        val passwordEncrypted = text("password_encrypted")
        val url = varchar("url", 500).nullable()
        val folder = varchar("folder", 100).default("Основная")
        val tags = text("tags").nullable()
        val notes = text("notes").nullable()
        val isWeak = bool("is_weak").default(false)
        val createdAt = long("created_at")
        val updatedAt = long("updated_at")

        override val primaryKey = PrimaryKey(id)
    }

    object Settings : Table("settings") {
        val key = varchar("key", 50)
        val value = text("value")

        override val primaryKey = PrimaryKey(key)
    }

    // Сохранение пароля
    fun savePassword(entry: PasswordEntry) {
        transaction {
            PasswordEntries.replace {
                it[id] = entry.id
                it[name] = entry.name
                it[login] = entry.login
                it[passwordEncrypted] = entry.passwordEncrypted
                it[url] = entry.url
                it[folder] = entry.folder
                it[tags] = entry.tags.joinToString(",")
                it[notes] = entry.notes
                it[isWeak] = entry.isWeak
                it[createdAt] = entry.id.hashCode().toLong() // временно
                it[updatedAt] = System.currentTimeMillis()
            }
        }
    }

    // Получение всех паролей
    fun getAllPasswords(): List<PasswordEntry> {
        return transaction {
            PasswordEntries.selectAll().map {
                PasswordEntry(
                    id = it[PasswordEntries.id],
                    name = it[PasswordEntries.name],
                    login = it[PasswordEntries.login] ?: "",
                    passwordEncrypted = it[PasswordEntries.passwordEncrypted],
                    url = it[PasswordEntries.url] ?: "",
                    folder = it[PasswordEntries.folder],
                    tags = it[PasswordEntries.tags]?.split(",")?.filter { tag -> tag.isNotEmpty() } ?: emptyList(),
                    notes = it[PasswordEntries.notes] ?: "",
                    isWeak = it[PasswordEntries.isWeak]
                )
            }
        }
    }

    // Удаление пароля
    fun deletePassword(id: String) {
        transaction {
            PasswordEntries.deleteWhere { PasswordEntries.id eq id }
        }
    }

    // Сохранение настройки
    fun saveSetting(key: String, value: String) {
        transaction {
            Settings.replace {
                it[Settings.key] = key
                it[Settings.value] = value
            }
        }
    }

    // Получение настройки
    fun getSetting(key: String): String? {
        return transaction {
            Settings.select { Settings.key eq key }
                .firstOrNull()
                ?.get(Settings.value)
        }
    }

    // Получение всех папок
    fun getAllFolders(): List<String> {
        return transaction {
            (PasswordEntries.slice(PasswordEntries.folder)
                .selectAll()
                .map { it[PasswordEntries.folder] } + "Основная").distinct()
        }
    }
}