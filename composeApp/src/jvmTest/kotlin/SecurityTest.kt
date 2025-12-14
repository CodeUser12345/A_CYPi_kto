import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import utils.SecurityUtils

class SecurityTest {

    @Test
    fun `encrypting same data twice produces different results (Salt verification)`() {
        val data = "MySecretData"
        val key = "MasterKey123"

        val enc1 = SecurityUtils.encrypt(data, key)
        val enc2 = SecurityUtils.encrypt(data, key)

        // Зашифрованные строки НЕ должны совпадать (разная соль)
        assertNotEquals(enc1, enc2, "Encryption should be probabilistic")

        // Но расшифровываться должны одинаково
        assertEquals(data, SecurityUtils.decrypt(enc1, key))
        assertEquals(data, SecurityUtils.decrypt(enc2, key))
    }

    @Test
    fun `test empty string encryption`() {
        val data = ""
        val key = "Key"

        val enc = SecurityUtils.encrypt(data, key)
        val dec = SecurityUtils.decrypt(enc, key)

        assertEquals(data, dec)
    }

    @Test
    fun `test decrypt with wrong password returns junk or error`() {
        val data = "SensitiveInfo"
        val key = "CorrectKey"
        val wrongKey = "WrongKey"

        val enc = SecurityUtils.encrypt(data, key)
        val dec = SecurityUtils.decrypt(enc, wrongKey)

        assertEquals("Error", dec)
    }

    @Test
    fun `verify password logic handles salts correctly`() {
        val password = "UserPassword"

        val (hash, salt) = SecurityUtils.hashPassword(password)
        val fakeSalt = "Ag== "
        val isValid = SecurityUtils.verifyPassword(password, hash, fakeSalt)

        assertFalse(isValid)
    }
}