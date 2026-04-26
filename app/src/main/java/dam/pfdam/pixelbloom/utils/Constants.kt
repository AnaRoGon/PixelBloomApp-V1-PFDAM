package dam.pfdam.pixelbloom.utils


/**
 * Objeto que concentra todas las constantes globales del proyecto.
 * Centraliza las claves compartidas y los nombres de colecciones en base de datos.
 */
object Constants {

    //Colecciones de Firestore
    const val COLLECTION_USERS = "users"
    const val COLLECTION_CHALLENGES = "challenges"
    const val COLLECTION_REFERENCES = "references"
    const val SUBCOLLECTION_BOARDS = "boards"
    const val SUBCOLLECTION_USER_CHALLENGES = "userChallenges"

    // SharedPreferences
    const val PREFS_MAIN = "pixel_bloom_prefs"
    const val KEY_ONBOARDING_DONE = "onboarding_done"
    const val KEY_INTERACTIVE_GUIDE_DONE = "interactive_guide_done"
    const val KEY_LANGUAGE = "language"
    const val KEY_FAVORITE_IDS = "favorite_ids"
    const val KEY_THEME = "theme"

    //URL base de la API ColorMagic
    const val BASE_URL = "https://colormagic.app/api/"
}
