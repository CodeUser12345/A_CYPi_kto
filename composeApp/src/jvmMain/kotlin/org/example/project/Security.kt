/*import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object SecurityUtils {
    private const val ALGORITHM = "AES"

    // Создаем 32-байтный ключ из мастер-пароля
    private fun getKey(password: String): SecretKeySpec {
        val sha = MessageDigest.getInstance("SHA-256")
        val key = sha.digest(password.toByteArray(Charsets.UTF_8))
        return SecretKeySpec(key, ALGORITHM)
    }

    fun encrypt(data: String, secret: String): String {
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, getKey(secret))
        return Base64.getEncoder().encodeToString(cipher.doFinal(data.toByteArray()))
    }

    fun decrypt(data: String, secret: String): String {
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, getKey(secret))
        return String(cipher.doFinal(Base64.getDecoder().decode(data)))
    }
}

 */