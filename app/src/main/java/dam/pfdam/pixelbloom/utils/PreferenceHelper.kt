package dam.pfdam.pixelbloom.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.core.os.LocaleListCompat

/**
 * Clase de utilidad para gestionar las preferencias compartidas en el sharedPreference de la aplicación.
 * Centraliza el acceso a los datos almacenados en las preferencias locales para evitar duplicación de código
 * y asegura que los cambios se reflejen de manera persistente mediante el uso de AppCompatDelegate.
 *
 * @param context Contexto necesario para obtener la instancia local de SharedPreferences a nivel de aplicación.
 */
class PreferenceHelper(context: Context) { //Para recibir el contexto tenía que ser una class no un objetc
    // Obtenemos las preferencias a nivel de aplicación y las almacenamos en una variable
    private val sharedPrefs = context.applicationContext.getSharedPreferences(
        Constants.PREFS_MAIN, Context.MODE_PRIVATE
    )

    /**
     * Guarda una cadena de texto en las preferencias.
     *
     * @param key Clave con la que se guardará el valor.
     * @param value Valor a guardar.
     */
    fun saveString(key: String, value: String) {
        sharedPrefs.edit { putString(key, value) }
    }

    /**
     * Obtiene el idioma actualmente guardado en las preferencias de la aplicación.
     *
     * @return El código del idioma guardado. Por defecto retorna "en".
     */
    fun getLanguage(): String {
        return sharedPrefs.getString(Constants.KEY_LANGUAGE, "en") ?: "en"
    }

    /**
     * Cambia el idioma de la aplicación, guardándolo en las preferencias
     * y aplicándolo en la configuración del sistema.

     * Hace una comprobación antes de aplicar el nuevo idioma para evitar cambios innecesarios.
     *
     * @param newLanguage El código del nuevo idioma a aplicar.
     */
    fun changeLanguage(newLanguage: String) {
        if (newLanguage != getLanguage()) {
            saveString(Constants.KEY_LANGUAGE, newLanguage)
            // Aplicamos el nuevo idioma
            val appLocales = LocaleListCompat.forLanguageTags(newLanguage)
            AppCompatDelegate.setApplicationLocales(appLocales)
        }
    }

    /**
     * Obtiene el tema visual (modo claro u oscuro) guardado en las preferencias de la aplicación.
     *
     * @return El código del tema guardado. Por defecto retorna "light".
     */
    fun getTheme(): String {
        return sharedPrefs.getString(Constants.KEY_THEME, "light") ?: "light"
    }

    /**
     * Cambia el tema de la aplicación, guardándolo localmente en preferencias
     * y aplicándolo al sistema a través de `AppCompatDelegate`.
     *
     * Hace una comprobación antes de aplicar el nuevo tema para evitar cambios innecesarios.
     *
     * @param newTheme El código del nuevo tema a aplicar.
     */
    fun changeTheme(newTheme: String) {
        val currentTheme = getTheme()
        if (newTheme != currentTheme) {
            saveString(Constants.KEY_THEME, newTheme)

            // Aplicamos el nuevo tema
            val nightMode = when (newTheme) {
                "dark" -> AppCompatDelegate.MODE_NIGHT_YES
                "light" -> AppCompatDelegate.MODE_NIGHT_NO
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            if (AppCompatDelegate.getDefaultNightMode() != nightMode) {
                AppCompatDelegate.setDefaultNightMode(nightMode)
            }
        }
    }

    /**
     * Devuelve el estado del onBoarding en las preferencias.
     *
     * @return `true` si el onboarding ya se visualizó, `false` en caso contrario.
     */
    fun getOnBoardingState(): Boolean {
        return sharedPrefs.getBoolean(Constants.KEY_ONBOARDING_DONE, false)
    }

    /**
     * Establece el estado del onboarding en las preferencias al true o false, según se indique.
     *
     * @param isCompleted estado del onboarding.
     */
    fun setOnboardingState(isCompleted: Boolean) {
        sharedPrefs.edit {
            putBoolean(Constants.KEY_ONBOARDING_DONE, isCompleted)
        }
    }

    /**
     * Devuelve el estado de la guía interactiva en las preferencias.
     *
     * @return `true` si la guía ha sido completada, `false` en caso contrario.
     */
    fun getInteractiveGuideState(): Boolean {
        return sharedPrefs.getBoolean(Constants.KEY_INTERACTIVE_GUIDE_DONE, false)
    }

    /**
     * Establece el estado de la guía en las preferencias a true o false, según se indique.
     * Útil para restablecer el estado tras finalizar un test.
     *
     * @param isCompleted estado de la guía.
     */
    fun setInteractiveGuideState(isCompleted: Boolean) {
        sharedPrefs.edit {
            putBoolean(Constants.KEY_INTERACTIVE_GUIDE_DONE, isCompleted)
        }
    }

    /**
     * Aplica globalmente tanto el tema como el idioma que el usuario tenía almacenados.
     */
    fun applyPreferences() {
        // Se carga y aplica el tema guardado
        val nightMode = when (getTheme()) {
            "dark" -> AppCompatDelegate.MODE_NIGHT_YES
            "light" -> AppCompatDelegate.MODE_NIGHT_NO
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)

        // Se carga y aplica el idioma guardado
        val languageTag = getLanguage()
        val appLocales = LocaleListCompat.forLanguageTags(languageTag)
        AppCompatDelegate.setApplicationLocales(appLocales)
    }

}
