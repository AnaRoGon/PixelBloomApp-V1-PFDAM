package dam.pfdam.pixelbloom.utils

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import dam.pfdam.pixelbloom.R

/**
 * Utilidades para interactuar con Firebase.
 */
object FirebaseUtils {

    /**
     * Traduce las excepciones técnicas de Firebase a mensajes amigables para el usuario.
     * @param exception La excepción lanzada por Firebase.
     * @return El mensaje de error o informativo según la excepción recibida de Firebase.
     */
    fun translateError(exception: Exception?): Int {
        if (exception is FirebaseNetworkException) {
            return R.string.error_network_request_failed
        }

        val errorCode = (exception as? FirebaseAuthException)?.errorCode
        val errorMessage = exception?.message ?: ""

        return when {
            errorCode == "ERROR_INVALID_EMAIL" ->
                R.string.error_invalid_email

            errorCode == "ERROR_INVALID_CREDENTIAL" || 
            errorCode == "ERROR_USER_NOT_FOUND" || 
            errorCode == "ERROR_WRONG_PASSWORD" ->
                R.string.error_invalid_credential

            errorCode == "ERROR_USER_DISABLED" ->
                R.string.error_user_disabled

            errorCode == "ERROR_EMAIL_ALREADY_IN_USE" || 
            errorCode == "ERROR_CREDENTIAL_ALREADY_IN_USE" ->
                R.string.error_email_already_in_use

            errorCode == "ERROR_WEAK_PASSWORD" ->
                R.string.error_weak_password

            errorCode == "ERROR_TOO_MANY_REQUESTS" || errorMessage.contains(
                "blocked all requests",
                ignoreCase = true
            ) -> {
                R.string.error_too_many_requests
            }

            else -> R.string.error_generic_operation
        }
    }
}