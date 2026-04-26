package dam.pfdam.pixelbloom.data.model

/**
 * Representa el estado y la participación de un usuario en un desafío específico.
 *
 * @property createdAt Fecha en la que el usuario aceptó el desafío.
 * @property description Frase inspiradora heredada del desafío.
 * @property id Identificador único del desafío del usuario.
 * @property complete Indica si el usuario ha finalizado el reto satisfactoriamente.
 * @property palette Paleta de colores utilizada para este desafío.
 * @property title Título del desafío.
 * @property userImagePath Ruta a la imagen subida por el usuario como resultado del desafío.
 */

data class UserChallenge(
    val createdAt: String = "",
    val description: String = "",
    val id: String = "",
    val complete: Boolean = false,
    val palette: List<String> = emptyList(),
    val title: String = "",
    val userImagePath: String = ""
)