package dam.pfdam.pixelbloom.utils

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import dam.pfdam.pixelbloom.R
import org.junit.Assert.assertEquals
import org.junit.Test

class FirebaseUtilsTest {
    //Varios test para comprobar que FirebaseUtils.translateError() devuelve los mensajes que queremos

    @Test
    fun translateError_withNull_returnsGenericError() {
        val messageRes = FirebaseUtils.translateError(null)
        assertEquals(R.string.error_generic_operation, messageRes)
    }

    @Test
    fun translateError_withGenericException_returnsGenericError() {
        val exception = Exception("Unknown error")
        val messageRes = FirebaseUtils.translateError(exception)
        assertEquals(R.string.error_generic_operation, messageRes)
    }

    @Test
    fun translateError_withNetworkException_returnsNetworkError() {
        val exception = FirebaseNetworkException("Network failed")
        val messageRes = FirebaseUtils.translateError(exception)
        assertEquals(R.string.error_network_request_failed, messageRes)
    }

    @Test
    fun translateError_withInvalidEmailAuthException_returnsInvalidEmailError() {
        val exception = FirebaseAuthException("ERROR_INVALID_EMAIL", "The email is invalid")
        val messageRes = FirebaseUtils.translateError(exception)
        assertEquals(R.string.error_invalid_email, messageRes)
    }

    @Test
    fun translateError_withUserNotFoundAuthException_returnsInvalidCredentialError() {
        val exception = FirebaseAuthException("ERROR_USER_NOT_FOUND", "User does not exist")
        val messageRes = FirebaseUtils.translateError(exception)
        assertEquals(R.string.error_invalid_credential, messageRes)
    }

    @Test
    fun translateError_withUserDisabledAuthException_returnsUserDisabledError() {
        val exception = FirebaseAuthException("ERROR_USER_DISABLED", "User disabled")
        val messageRes = FirebaseUtils.translateError(exception)
        assertEquals(R.string.error_user_disabled, messageRes)
    }

    @Test
    fun translateError_withEmailAlreadyInUseAuthException_returnsEmailAlreadyInUseError() {
        val exception = FirebaseAuthException("ERROR_EMAIL_ALREADY_IN_USE", "Email in use")
        val messageRes = FirebaseUtils.translateError(exception)
        assertEquals(R.string.error_email_already_in_use, messageRes)
    }

    @Test
    fun translateError_withWeakPasswordAuthException_returnsWeakPasswordError() {
        val exception = FirebaseAuthException("ERROR_WEAK_PASSWORD", "Weak password")
        val messageRes = FirebaseUtils.translateError(exception)
        assertEquals(R.string.error_weak_password, messageRes)
    }

    @Test
    fun translateError_withTooManyRequestsAuthException_returnsTooManyRequestsError() {
        val exception = FirebaseAuthException("ERROR_TOO_MANY_REQUESTS", "Too many requests")
        val messageRes = FirebaseUtils.translateError(exception)
        assertEquals(R.string.error_too_many_requests, messageRes)
    }

}