import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PasswordGeneratorTest {

    @Test
    fun `generated password matches requested length`() {
        val length = 20
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%"

        // Логика из вашего PasswordDialog
        val password = (1..length).map { chars.random() }.joinToString("")

        assertEquals(20, password.length)
    }

    @Test
    fun `generated password contains characters from allowed set`() {
        val chars = "ABC" // Ограниченный набор для теста
        val password = (1..50).map { chars.random() }.joinToString("")

        // Проверяем, что в пароле НЕТ символов, которых мы не разрешали
        assertTrue(password.all { it == 'A' || it == 'B' || it == 'C' })
    }
}