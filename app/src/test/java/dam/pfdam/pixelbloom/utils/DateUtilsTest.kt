package dam.pfdam.pixelbloom.utils

import org.junit.Assert.*
import org.junit.Test

class DateUtilsTest {

    @Test
    fun verify_pattern_format_is_not_empty() {
        // Se ejecuta la función para obtener el timestamp
        val timestamp: String = DateUtils.getCurrentTimestamp()

        // Verificamos que no sea nulo o vacío
        assertTrue(timestamp.isNotEmpty())
    }

    @Test
    fun verify_pattern_format() {
        // Se ejecuta la función para obtener el timestamp
        val timestamp: String = DateUtils.getCurrentTimestamp()

        //Se comprueba que el formato devuelto corresponde con el esperado
        val regex = Regex("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}$")
        assertTrue(regex.matches(timestamp))
    }
}