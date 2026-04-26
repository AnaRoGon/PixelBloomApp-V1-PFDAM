package dam.pfdam.pixelbloom.data.ui

/**
 * Representa los datos visuales y de texto para meter en la pantalla de onboarding de la app.
 *
 * @property image imagen que se mostrará en la pantalla.
 * @property title Titular principal de la pantalla.
 * @property description Mensaje detallado que inicia al usuario en la app.
 */
data class OnBoarding(
    val image: Int,
    val title: String,
    val description: String
)