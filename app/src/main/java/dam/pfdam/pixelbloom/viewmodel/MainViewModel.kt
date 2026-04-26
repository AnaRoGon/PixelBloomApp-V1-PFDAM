package dam.pfdam.pixelbloom.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import dam.pfdam.pixelbloom.R
import dam.pfdam.pixelbloom.data.model.User
import dam.pfdam.pixelbloom.utils.Constants

/**
 * ViewModel asociado a la MainActivity que gestiona el estado global del usuario.
 *
 * Se encarga de la sincronización entre la autenticación de Firebase y la base de datos
 * Firestore, asegurando que cada usuario tenga su perfil creado y verificando el rol del
 * usuario, que se almacena en el estado interno del ViewModel.
 *
 * @property db Referencia a la base de datos Firestore para operaciones de persistencia.
 * @property auth Instancia del servicio de autenticación de Firebase.
 * @property currentUser Referencia al objeto de usuario autenticado actualmente.
 * @property _isAdmin Estado interno (mutable) que indica si el usuario tiene permisos de administrador.
 * @property isAdmin Estado observable (inmutable) para que la vista reaccione al rol de la usuaria.
 * @property _errorMsg Estado interno (mutable) del mensaje de error actual. Almacena el ID del recurso.
 * @property errorMsg Estado observable (inmutable) que contiene el ID del mensaje de error traducido para la UI.
 * @property _successMsg Estado interno (mutable) del mensaje de éxito actual. Almacena el ID del recurso.
 * @property successMsg Estado observable (inmutable) que contiene el ID del mensaje de éxito traducido para la UI.
 */
class MainViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser
    private val _isAdmin = MutableLiveData<Boolean>()
    val isAdmin: LiveData<Boolean> get() = _isAdmin

    private val _errorMsg = MutableLiveData<Int?>()
    val errorMsg: LiveData<Int?> get() = _errorMsg

    private val _successMsg = MutableLiveData<Int?>()
    val successMsg: LiveData<Int?> get() = _successMsg

    //Se lanza la peticion para verificar el rol del usuario al iniciar el viewModel
    init {
        isAdminUser()
    }

    /**
     * Verifica si el usuario actual ya tiene un documento en la colección de "users" de Firestore.
     * Aunque en firebase Auth se haya registrado un usuario esto no significa que tenga una colección
     * con sus datos almacenados en firestore. Necesitamos esto para tener unos datos predeterminados
     * del perfil del usuario y modificar el campo de rol para definir usuarios de tipo 'admin'
     *
     * Si el documento ya existe, no realiza ninguna acción para evitar sobrescribir datos.
     * Si no existe, crea un nuevo documento con el UID del usuario autenticado y los datos iniciales.
     */
    fun checkAndCreateUser(defaultName: String) {
        if (currentUser != null) {
            // Referencia al documento del usuario en Firestore usando su UID
            val userRef = db.collection(Constants.COLLECTION_USERS).document(currentUser.uid)

            // Consultamos si el documento existe
            userRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    // El usuario ya existe, no hacemos nada para preservar sus datos
                    Log.d("MainViewModel", "El usuario ya existe, no se sobrescribe.")
                } else {
                    // El usuario no existe, creamos una nueva instancia de ''User con datos básicos
                    val user = User(
                        displayName = currentUser.displayName ?: defaultName,
                        email = currentUser.email ?: ""
                        // role y preferences usan los valores por defecto al iniciar por primera vez
                    )

                    // Guardamos el nuevo usuario en Firestore
                    // y se mandan los mensajes de error y éxito
                    userRef.set(user)
                        .addOnSuccessListener {
                            _successMsg.value = R.string.user_created_success
                        }
                        .addOnFailureListener {
                            _errorMsg.value = R.string.error_creating_user
                        }
                }
            }.addOnFailureListener { exception ->
                //Si salta una excepción se manda un mensaje interno de error interno
                Log.e("MainViewModel", "Error al verificar usuario: ${exception.message}")
            }
        }
    }

    /**
     * Consulta si el usuario actual tiene rol de administrador.
     * y se almacena en el estado interno del viewModel.
     */
    fun isAdminUser() {
        //Obtenemos el UID del usuario actual
        val useRef = currentUser?.uid ?: return
        //Consultamos si el usuario tiene rol de administrador
        db.collection(Constants.COLLECTION_USERS).document(useRef)
            .get()
            .addOnSuccessListener { result ->
                if (result != null && result.exists()) {
                    val user = result.toObject(User::class.java)
                    _isAdmin.postValue(user?.role == "admin")
                } else {
                    _isAdmin.postValue(false)
                }
            }
    }

    /**
     * Limpia el mensaje de error actual para evitar duplicidad de avisos en la UI.
     */
    fun clearErrors() {
        _errorMsg.value = null
    }

    /**
     * Limpia el mensaje de éxito actual para evitar duplicidad de avisos en la UI.
     */
    fun clearSuccess() {
        _successMsg.value = null
    }

}
