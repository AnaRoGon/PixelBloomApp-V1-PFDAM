package dam.pfdam.pixelbloom.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth


import dam.pfdam.pixelbloom.utils.FirebaseUtils

/**
 * ViewModel que centraliza la lógica de autenticación del proyecto PixelBloom.
 *
 * Gestiona la comunicación directa con Firebase Auth para los procesos de inicio de sesión,
 * registro y cierre de sesión. Expone estados mediante LiveData para que la interfaz
 * reaccione a los cambios en el flujo de autenticación.
 *
 * @property auth Instancia de FirebaseAuth para realizar las peticiones al servicio de Google.
 * @property _authResult Estado interno (mutable) del éxito de la operación de autenticación.
 * @property authResult Estado observable (inmutable) para que la vista detecte el éxito del proceso.
 * @property _errorMsg Estado interno (mutable) del mensaje de error actual. Almacena el ID del recurso.
 * @property errorMsg Estado observable (inmutable) que contiene el ID del mensaje de error traducido para la UI.
 */
class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val _authResult = MutableLiveData<Boolean>()
    val authResult: LiveData<Boolean> get() = _authResult
    private val _errorMsg = MutableLiveData<Int?>()
    val errorMsg: LiveData<Int?> get() = _errorMsg

    /**
     * Inicia sesión de un usuario con las credenciales proporcionadas.
     *
     * Si el logeo es exitoso, se actualiza el estado interno a true.
     * Si no lo es, se convierte el error haciendo uso de la clase de utilidad
     * que diseñamos para traducir los errores de Firebase [FirebaseUtils]
     *
     * @param email Correo electrónico del usuario.     *
     * @param password Contraseña asociada a la cuenta.
     */
    fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _authResult.value = true
            } else {
                _errorMsg.value = FirebaseUtils.translateError(task.exception)
                _authResult.value = false
            }
        }
    }

    /**
     * Registra una nueva cuenta de usuario en Firebase Auth.
     * En caso de fallo, delega la traducción del error a la clase [FirebaseUtils].
     *
     * @param email Correo electrónico para el nuevo registro.
     * @param password Contraseña para la nueva cuenta.
     */
    fun register(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _authResult.value = true
            } else {
                _errorMsg.value = FirebaseUtils.translateError(task.exception)
                _authResult.value = false
            }
        }
    }

    /**
     * Cierra la sesión activa del usuario actual en el servicio de Firebase.
     */
    fun logOut() {
        auth.signOut()
    }

    /**
     * Limpia el mensaje de error actual en el LiveData para evitar duplicidad de avisos en la UI.
     */
    fun clearErrors() {
        _errorMsg.value = null
    }
}
