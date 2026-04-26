package dam.pfdam.pixelbloom.data.model


/**
 * Representa el perfil de un usuario autenticado en la plataforma. Las propiedades del usuario se almacenan en la colección 'Users' de
 * Firestore con un UID como identificador único que no es necesario indicar en la data class.
 *
 * @property email correo con el que se registra el usuario.
 * @property displayName Nombre visible del perfil del usuario. Se omite en esta primera versión, se asigna "Usuario" por defecto.
 * @property role Permisos asignados ("user", "admin"). Controla el acceso a funciones específicas.
 * @property preferences Configuración del idioma y tema de la app. Por defecto 'en' y 'light'
 */

data class User(
    val email: String = "",
    val displayName: String = "Usuario",
    val role: String = "user",
    val preferences: Map<String, String> = mapOf("language" to "en", "theme" to "light")
)