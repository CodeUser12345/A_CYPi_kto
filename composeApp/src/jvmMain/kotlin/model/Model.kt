package model

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Модель данных, представляющая запись о пароле.
 * Класс аннотирован @Serializable для возможности экспорта в JSON.
 *
 * @property id Уникальный идентификатор (UUID).
 * @property name Название сервиса или сайта.
 * @property login Логин пользователя.
 * @property passwordEncrypted Пароль в зашифрованном виде.
 * @property url Адрес веб-сайта.
 * @property tags Список тегов для фильтрации.
 * @property notes Заметки.
 * @property isWeak Флаг, указывающий, является ли пароль слабым.
 */
@Serializable
data class PasswordEntry(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val login: String,
    val passwordEncrypted: String,
    val url: String = "",
    val tags: List<String> = emptyList(),
    val notes: String = "",
    val isWeak: Boolean = false
)

/**
 * Состояния навигации приложения.
 * @property LOGIN Экран авторизации.
 * @property DASHBOARD Основной рабочий экран.
 */
enum class ScreenState {
    LOGIN, DASHBOARD
}