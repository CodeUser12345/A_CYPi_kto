package data

import model.PasswordEntry
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.util.*

/**
 * Менеджер базы данных SQLite.
 * Отвечает за инициализацию соединения, создание схемы и выполнение всех CRUD-операций.
 * База данных хранится локально в файле `passwords.db`.
 */
object DatabaseManager {
    private const val DB_NAME = "passwords.db"

    /**
     * Устанавливает JDBC соединение с базой данных SQLite.
     * Создает директорию `.password-manager` в домашней папке пользователя при необходимости.
     */
    private fun getConnection() {
        val dbFile = File(System.getProperty("user.home"), ".password-manager")
        if (!dbFile.exists()) dbFile.mkdirs()

        Database.connect("jdbc:sqlite:${dbFile.absolutePath}/$DB_NAME", "org.sqlite.JDBC")
    }

    init {
        getConnection()
        createTables()
    }

    /**
     * Создает таблицы в базе данных, если они отсутствуют.
     */
    private fun createTables() {
        transaction {
            SchemaUtils.create(PasswordEntries, Settings, TagsTable)
        }
    }

    /**
     * ORM-определение таблицы паролей.
     */
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

    /**
     * ORM-определение таблицы настроек (Key-Value).
     */
    object Settings : Table("settings") {
        val key = varchar("key", 50)
        val value = text("value")

        override val primaryKey = PrimaryKey(key)
    }

    /**
     * ORM-определение таблицы тегов.
     */
    object TagsTable : Table("tags") {
        val id = integer("id").autoIncrement()
        val name = varchar("name", 100).uniqueIndex()

        override val primaryKey = PrimaryKey(id)
    }

    /**
     * Сохраняет или обновляет запись пароля.
     * @param entry Объект [PasswordEntry].
     */
    fun savePassword(entry: PasswordEntry) {
        transaction {
            val exists = PasswordEntries.select { PasswordEntries.id eq entry.id }.count() > 0

            if (exists) {
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

    /**
     * Возвращает список всех записей паролей.
     */
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

    /**
     * Удаляет запись пароля по ID.
     */
    fun deletePassword(id: String) {
        transaction {
            PasswordEntries.deleteWhere { PasswordEntries.id eq id }
        }
    }

    /**
     * Сохраняет новый тег, если он уникален.
     * @return true, если успешно добавлен.
     */
    fun saveTag(tag: String): Boolean {
        return try {
            transaction {
                val exists = TagsTable.select { TagsTable.name eq tag }.count() > 0
                if (!exists) {
                    TagsTable.insert {
                        it[name] = tag
                    }
                    true
                } else {
                    false
                }
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Получает список всех тегов.
     */
    fun getAllTags(): List<String> {
        return transaction {
            TagsTable.selectAll().map { it[TagsTable.name] }
        }
    }

    /**
     * Удаляет тег из таблицы тегов и убирает его из всех записей паролей.
     */
    fun deleteTag(tag: String) {
        transaction {
            TagsTable.deleteWhere { TagsTable.name eq tag }

            PasswordEntries.selectAll().forEach { row ->
                val currentTags = row[PasswordEntries.tags]?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
                val updatedTags = currentTags.filter { it != tag }
                PasswordEntries.update({ PasswordEntries.id eq row[PasswordEntries.id] }) {
                    it[tags] = updatedTags.joinToString(",")
                }
            }
        }
    }

    /**
     * Сохраняет настройку (ключ-значение).
     */
    fun saveSetting(key: String, value: String) {
        transaction {
            Settings.replace {
                it[Settings.key] = key
                it[Settings.value] = value
            }
        }
    }

    /**
     * Получает значение настройки.
     */
    fun getSetting(key: String): String? {
        return transaction {
            Settings.select { Settings.key eq key }
                .firstOrNull()
                ?.get(Settings.value)
        }
    }
}