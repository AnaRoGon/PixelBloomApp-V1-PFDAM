package dam.pfdam.pixelbloom.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Utilidades para el manejo de fechas y formatos.
 */
object DateUtils {
    private const val DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss"

    /**
     * Obtiene la fecha y hora actual del sistema formateada como String.
     * @return String de la fecha actual en formato '2026-04-07T18:00:00'.
     */
    fun getCurrentTimestamp(): String {
        val formatter = DateTimeFormatter.ofPattern(DATE_PATTERN)
        return LocalDateTime.now().format(formatter)
    }
}
