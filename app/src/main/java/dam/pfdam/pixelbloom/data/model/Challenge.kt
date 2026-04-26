package dam.pfdam.pixelbloom.data.model

import com.google.firebase.firestore.Exclude


/**
 * Define los desafíos creativos disponibles para los usuarios.
 *
 * @property createdAt Fecha en la que se publicó el desafío.
 * @property description Explicación inspiradora del reto que el usuario debe realizar.
 * @property palette Lista de códigos hex para completar el desafío.
 * @property title Nombre del desafío.
 * @property id Identificador del documento en Firestore. Excluido de la persistencia, en Firestore es el propio id del documento.
 */
data class Challenge(
    val createdAt: String = "",
    val description: String = "",
    val palette: List<String> = emptyList(),
    val title: String = "",
    @get:Exclude var id: String = "" //De tipo var para poder asignarlo en el fetch de datos.
)