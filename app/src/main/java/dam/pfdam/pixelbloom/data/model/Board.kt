package dam.pfdam.pixelbloom.data.model


/**
 * Representa un tablero o colección de imágenes dentro de la aplicación.
 *
 * @property createdAt Fecha de creación del tablero.
 * @property id Identificador único del tablero en la base de datos.
 * @property imageRefs Lista de referencias a las imágenes que pertenecen al tablero.
 * @property title Nombre descriptivo dado al tablero por el usuario.
 * @property totalReferences Contador total de referencias asociadas a el tablero.
 */
data class Board(
    val createdAt: String = "",
    val id: String = "",
    val imageRefs: List<String> = emptyList(),
    val title: String = "",
    val totalReferences: Int = 0
)