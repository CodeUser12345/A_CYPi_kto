package utils

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Утилитарный объект, предоставляющий криптографические примитивы для защиты данных приложения.
 *
 * Реализует стандарты безопасности:
 * - **Шифрование данных**: Симметричный алгоритм AES-256 в режиме сцепления блоков (CBC) с дополнением PKCS5.
 * - **Хеширование паролей**: Алгоритм PBKDF2WithHmacSHA256 с 10 000 итераций.
 * - **Генерация случайных чисел**: SecureRandom для создания соли и векторов инициализации (IV).
 */
object SecurityUtils {
    private const val ALGORITHM = "AES"
    private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"
    private const val PBKDF2_ITERATIONS = 10000
    private const val KEY_LENGTH = 256
    private const val SALT_LENGTH = 16

    /**
     * Генерирует хеш пароля с использованием случайной соли.
     * Предназначен для безопасного хранения мастер-пароля.
     *
     * @param password Пароль в открытом виде.
     * @param salt Соль. Если не указана, генерируется новая случайная соль.
     * @return [Pair], содержащая:
     * - `first`: Хеш пароля (Base64 строка).
     * - `second`: Соль (Base64 строка).
     */
    fun hashPassword(password: String, salt: ByteArray = generateSalt()): Pair<String, String> {
        val spec = PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = factory.generateSecret(spec).encoded
        return Pair(
            Base64.getEncoder().encodeToString(hash),
            Base64.getEncoder().encodeToString(salt)
        )
    }

    /**
     * Проверяет, соответствует ли введенный пароль сохраненному хешу.
     *
     * @param password Пароль для проверки.
     * @param storedHash Сохраненный хеш (Base64).
     * @param storedSalt Сохраненная соль (Base64).
     * @return `true`, если пароль верный, иначе `false`.
     */
    fun verifyPassword(password: String, storedHash: String, storedSalt: String): Boolean {
        try {
            val salt = Base64.getDecoder().decode(storedSalt)
            val (newHash, _) = hashPassword(password, salt)
            return newHash == storedHash
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Генерирует криптографически стойкую случайную последовательность байт (соль).
     */
    private fun generateSalt(): ByteArray {
        val salt = ByteArray(SALT_LENGTH)
        SecureRandom().nextBytes(salt)
        return salt
    }

    /**
     * Формирует секретный ключ AES на основе пароля и соли.
     */
    private fun getKey(password: String, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val key = factory.generateSecret(spec).encoded
        return SecretKeySpec(key, ALGORITHM)
    }

    /**
     * Шифрует произвольные строковые данные.
     *
     * Генерирует уникальную соль и вектор инициализации (IV) для каждой операции шифрования.
     * Результирующая строка содержит `Salt + IV + CipherText`.
     *
     * @param data Строка для шифрования.
     * @param secret Мастер-пароль (используется для генерации ключа).
     * @return Зашифрованная строка в формате Base64.
     */
    fun encrypt(data: String, secret: String): String {
        try {
            val salt = generateSalt()
            val iv = ByteArray(16)
            SecureRandom().nextBytes(iv)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getKey(secret, salt), IvParameterSpec(iv))

            val encrypted = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
            val result = ByteArray(salt.size + iv.size + encrypted.size)
            System.arraycopy(salt, 0, result, 0, salt.size)
            System.arraycopy(iv, 0, result, salt.size, iv.size)
            System.arraycopy(encrypted, 0, result, salt.size + iv.size, encrypted.size)

            return Base64.getEncoder().encodeToString(result)
        } catch (e: Exception) {
            // e.printStackTrace()
            return data
        }
    }

    /**
     * Расшифровывает данные.
     *
     * Извлекает соль и IV из входной строки, восстанавливает ключ и дешифрует данные.
     *
     * @param data Зашифрованная строка (Base64).
     * @param secret Мастер-пароль.
     * @return Расшифрованная исходная строка. Возвращает "Error" в случае сбоя.
     */
    fun decrypt(data: String, secret: String): String {
        try {
            val decoded = Base64.getDecoder().decode(data)
            val salt = decoded.copyOfRange(0, SALT_LENGTH)
            val iv = decoded.copyOfRange(SALT_LENGTH, SALT_LENGTH + 16)
            val encrypted = decoded.copyOfRange(SALT_LENGTH + 16, decoded.size)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, getKey(secret, salt), IvParameterSpec(iv))

            return String(cipher.doFinal(encrypted), Charsets.UTF_8)
        } catch (e: Exception) {
            // e.printStackTrace()
            return "Error"
        }
    }
}