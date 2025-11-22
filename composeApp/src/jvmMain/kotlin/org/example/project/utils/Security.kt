package org.example.project.utils

import java.security.MessageDigest
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object SecurityUtils {
    private const val ALGORITHM = "AES"

    private fun getKey(password: String): SecretKeySpec {
        val sha = MessageDigest.getInstance("SHA-256")
        val key = sha.digest(password.toByteArray(Charsets.UTF_8))
        return SecretKeySpec(key, ALGORITHM)
    }

    fun encrypt(data: String, secret: String): String {
        try {
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, getKey(secret))
            return Base64.getEncoder().encodeToString(cipher.doFinal(data.toByteArray()))
        } catch (e: Exception) {
            return data
        }
    }

    fun decrypt(data: String, secret: String): String {
        try {
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, getKey(secret))
            return String(cipher.doFinal(Base64.getDecoder().decode(data)))
        } catch (e: Exception) {
            return "Error"
        }
    }
}