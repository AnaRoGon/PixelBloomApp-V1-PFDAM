package dam.pfdam.pixelbloom.data.model

import com.google.firebase.firestore.Exclude


/**
 * Representa una referencia dentro del feed de la aplicación.
 *
 * @property title Título de la referencia.
 * @property author Nombre del creador de la referencia.
 * @property description Breve explicación o contexto sobre la imagen.
 * @property imagePath Ruta de almacenamiento o URL de la imagen.
 * @property favorites Número de veces que los usuarios han marcado esta referencia como favorita.
 * @property createdAt Fecha de publicación o subida.
 * @property id Identificador del documento en Firestore. Excluido de la persistencia, en Firestore es el propio id del documento.
 */
data class Reference(
    val title: String = "",
    val author: String = "",
    val description: String = "",
    val imagePath: String = "",
    val favorites: Int = 0,
    val createdAt: String = "",
    @get:Exclude var id: String = "" //De tipo var para poder asignarlo en el fetch de datos.
)