package dam.pfdam.pixelbloom.data.model

/**
 * Representa la información de una paleta de colores recuperada de la API de Color Magic.
 *
 * @property id Identificador único de la paleta.
 * @property colors Lista de códigos hexadecimales que componen la paleta.
 * @property tags Etiquetas temáticas o descriptivas de los colores de la paleta.
 * @property text Texto original utilizado como origen o concepto que generó la paleta.
 * @property likesCount Total de "me gusta" recibidos por la comunidad.
 * @property normalizedHash Cadena normalizada de control/hash para la paleta.
 * @property createdAt Fecha de creación de la paleta devuelta por la API.
 */
data class PaletteData(
    val id: String = "",
    val colors: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val text: String = "",
    val likesCount: Int = 0,
    val normalizedHash: String = "",
    val createdAt: String = ""
)
