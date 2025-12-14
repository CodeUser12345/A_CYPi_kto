import utils.SecurityUtils
import kotlin.test.Test
import kotlin.test.assertEquals

class SecurityAdvancedTest {

    @Test
    fun `encryption handles Unicode and Emojis correctly`() {
        val complexData = "Пароль 🔑 с кириллицей и ❤️"
        val key = "MasterKey"

        val encrypted = SecurityUtils.encrypt(complexData, key)
        val decrypted = SecurityUtils.decrypt(encrypted, key)

        assertEquals(complexData, decrypted, "Decryption should fully restore Unicode strings")
    }
}