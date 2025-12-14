package utils

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object SecurityUtils {
    private const val ALGORITHM = "AES"
    private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"
    private const val PBKDF2_ITERATIONS = 10000
    private const val KEY_LENGTH = 256
    private const val SALT_LENGTH = 16

    // Хеширование пароля с солью
    fun hashPassword(password: String, salt: ByteArray = generateSalt()): Pair<String, String> {
        val spec = PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = factory.generateSecret(spec).encoded
        return Pair(
            Base64.getEncoder().encodeToString(hash),
            Base64.getEncoder().encodeToString(salt)
        )
    }

    // Проверка пароля
    fun verifyPassword(password: String, storedHash: String, storedSalt: String): Boolean {
        try {
            val salt = Base64.getDecoder().decode(storedSalt)
            val (newHash, _) = hashPassword(password, salt)
            return newHash == storedHash
        } catch (e: Exception) {
            return false
        }
    }

    private fun generateSalt(): ByteArray {
        val salt = ByteArray(SALT_LENGTH)
        SecureRandom().nextBytes(salt)
        return salt
    }

    private fun getKey(password: String, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val key = factory.generateSecret(spec).encoded
        return SecretKeySpec(key, ALGORITHM)
    }

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
            e.printStackTrace()
            return data
        }
    }

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
            e.printStackTrace()
            return "Error"
        }
    }
}