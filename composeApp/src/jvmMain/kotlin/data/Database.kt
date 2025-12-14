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
            SchemaUtils.create(PasswordEntries, Settings, TagsTable)
        }
    }

    object PasswordEntries : Table("passwords") {
        val id = varchar("id", 36)
        val name = varchar("name", 255)
        val login = varchar("login", 255).nullable()
        val passwordEncrypted = text("password_encrypted")
        val url = varchar("url", 500).nullable()
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

    // НОВАЯ ТАБЛИЦА ДЛЯ ТЕГОВ
    object TagsTable : Table("tags") {
        val id = integer("id").autoIncrement()
        val name = varchar("name", 100).uniqueIndex()

        override val primaryKey = PrimaryKey(id)
    }

    // Сохранение пароля
    fun savePassword(entry: PasswordEntry) {
        transaction {
            // Проверяем, существует ли запись
            val exists = PasswordEntries.select { PasswordEntries.id eq entry.id }.count() > 0

            if (exists) {
                // Обновляем существующую запись
                PasswordEntries.update({ PasswordEntries.id eq entry.id }) {
                    it[name] = entry.name
                    it[login] = entry.login
                    it[passwordEncrypted] = entry.passwordEncrypted
                    it[url] = entry.url
                    it[tags] = entry.tags.joinToString(",")
                    it[notes] = entry.notes
                    it[isWeak] = entry.isWeak
                    it[updatedAt] = System.currentTimeMillis()
                }
            } else {
                // Вставляем новую запись
                PasswordEntries.insert {
                    it[id] = entry.id
                    it[name] = entry.name
                    it[login] = entry.login
                    it[passwordEncrypted] = entry.passwordEncrypted
                    it[url] = entry.url
                    it[tags] = entry.tags.joinToString(",")
                    it[notes] = entry.notes
                    it[isWeak] = entry.isWeak
                    it[createdAt] = System.currentTimeMillis()
                    it[updatedAt] = System.currentTimeMillis()
                }
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

    // СОХРАНЕНИЕ ТЕГА (ИСПРАВЛЕННАЯ)
    fun saveTag(tag: String): Boolean {
        return try {
            transaction {
                // Проверяем, существует ли тег
                val exists = TagsTable.select { TagsTable.name eq tag }.count() > 0
                if (!exists) {
                    TagsTable.insert {
                        it[name] = tag
                    }
                    true
                } else {
                    false // Тег уже существует
                }
            }
        } catch (e: Exception) {
            false
        }
    }

    // ПОЛУЧЕНИЕ ВСЕХ ТЕГОВ
    fun getAllTags(): List<String> {
        return transaction {
            TagsTable.selectAll().map { it[TagsTable.name] }
        }
    }

    // УДАЛЕНИЕ ТЕГА
    fun deleteTag(tag: String) {
        transaction {
            // Удаляем тег из таблицы тегов
            TagsTable.deleteWhere { TagsTable.name eq tag }

            // Удаляем тег у всех паролей
            PasswordEntries.selectAll().forEach { row ->
                val currentTags = row[PasswordEntries.tags]?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
                val updatedTags = currentTags.filter { it != tag }
                PasswordEntries.update({ PasswordEntries.id eq row[PasswordEntries.id] }) {
                    it[tags] = updatedTags.joinToString(",")
                }
            }
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
}