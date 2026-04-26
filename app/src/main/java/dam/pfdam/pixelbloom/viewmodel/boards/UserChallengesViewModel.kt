package dam.pfdam.pixelbloom.viewmodel.boards

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import dam.pfdam.pixelbloom.R
import dam.pfdam.pixelbloom.data.model.Challenge
import dam.pfdam.pixelbloom.data.model.UserChallenge
import dam.pfdam.pixelbloom.utils.Constants
import dam.pfdam.pixelbloom.utils.DateUtils
import dam.pfdam.pixelbloom.utils.ImageUtils

/**
 * ViewModel que gestiona la lógica de los retos aceptados por el usuario.
 *
 * Se encarga de la comunicación con la subcolección "userChallenges" de cada usuario en Firestore.
 * Expone estados observables mediante LiveData para que la vista reaccione a los cambios en la
 * lista de retos, estados de carga y notificaciones de éxito o error.
 *
 * @property db Instancia de Firebase Firestore para realizar las consultas de bases de datos.
 * @property auth Instancia de FirebaseAuth para acceder al usuario autenticado actual.
 * @property challengesListener Registro del listener de Firestore de los challenges del usuario.
 * @property _userChallenges Estado interno y mutable que contiene la lista de retos aceptados.
 * @property userChallenges Estado observable que proporciona la lista de retos aceptados por el usuario.
 * @property _errorMsg Estado interno y mutable del mensaje de error actual. Almacena el ID del recurso.
 * @property errorMsg Estado observable que almacena el mensaje descriptivo en caso de error en las operaciones de datos. Si no hay error contiene null.
 * @property _successMsg Estado interno y mutable del mensaje de éxito actual. Almacena el ID del recurso.
 * @property successMsg Estado observable que almacena el mensaje descriptivo en caso de éxito en las operaciones de datos. Si no hay éxito contiene null.
 * @property _isLoading Estado interno y mutable del estado de la solicitud a Firebase.
 * @property isLoading Estado observable que indica a la vista si hay una operación asíncrona en curso.
 */
class UserChallengesViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    private var challengesListener: ListenerRegistration? = null

    private val _userChallenges = MutableLiveData<List<UserChallenge>>()
    val userChallenges: LiveData<List<UserChallenge>> get() = _userChallenges

    private val _errorMsg = MutableLiveData<Int?>()
    val errorMsg: LiveData<Int?> get() = _errorMsg

    private val _successMsg = MutableLiveData<Int?>()
    val successMsg: LiveData<Int?> get() = _successMsg
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // Bloque init para cargar los datos la primera vez que se inicia el ViewModel
    init {
        fetchUserChallenges()
    }

    /**
     * Inicia la escucha de los retos aceptados por el usuario actual.
     * Utiliza addSnapshotListener para recibir actualizaciones en tiempo real de la subcolección de 'userChallenges'.
     */
    private fun fetchUserChallenges() {
        //UID del usuario actual
        val userId = auth.currentUser?.uid ?: return

        if (challengesListener != null) return //Si ya está escuchando se sale de la función

        //Se instancia el listener con la consulta a Firestore y se ordena por fecha de creación descendientemente
        //Los ultimos retos aceptados aparecerán antes
        challengesListener = db.collection(Constants.COLLECTION_USERS)
            .document(userId)
            .collection(Constants.SUBCOLLECTION_USER_CHALLENGES)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED || auth.currentUser == null) {
                        _errorMsg.value = R.string.error_permission_denied
                        auth.signOut()
                        return@addSnapshotListener
                    }
                    _errorMsg.value = R.string.error_loading_challenges
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    //Se obtiene el listado de retos y se mapea al objeto correspondiente
                    val userChallengesList = snapshot.mapNotNull { document ->
                        try {
                            document.toObject(UserChallenge::class.java)
                        } catch (e: Exception) {
                            //Se lanza un error interno
                            Log.e("Firestore", "Error al cargar reto de usuario: ${e.message}")
                            null
                        }
                    }
                    _userChallenges.value =
                        userChallengesList //Se actualiza el estado interno con el listado de retos
                }
            }
    }

    /**
     * Copia un reto global a la subcolección privada del usuario.
     *
     * Permite que el usuario tome un reto de la galería de restos disponibles y lo
     * almacene en su colección personal de retos. El estado inicial es de no completado y sin imagen.
     *
     * Para almacenar la fecha con el formato correcto se utiliza la clase [DateUtils].
     *
     * @param challenge Objeto [Challenge] de la colección de retos globales disponibles que el usuario desea aceptar.
     */
    fun acceptChallenge(challenge: Challenge) {
        val userId = auth.currentUser?.uid ?: return

        val userChallenge = UserChallenge(
            createdAt = DateUtils.getCurrentTimestamp(),
            description = challenge.description,
            id = challenge.id,
            complete = false,
            palette = challenge.palette,
            title = challenge.title,
            userImagePath = ""
        )
        //Se almacena el reto en la subcolección del 'userChallenges'
        db.collection(Constants.COLLECTION_USERS)
            .document(userId)
            .collection(Constants.SUBCOLLECTION_USER_CHALLENGES)
            .document(challenge.id)
            .set(userChallenge)
            .addOnSuccessListener {
                //Se manda un mensaje informando al usuario de que el reto se ha guardado en su colección
                _successMsg.value = R.string.challenge_accepted_success
            }
            .addOnFailureListener {
                //En caso contrario se informa del error
                _errorMsg.value = R.string.error_accepting_challenge
            }
    }

    /**
     * Función principal llamada por la UI para completar el reto.
     * Verifica que exista la URI y delega el proceso de subida.
     *
     * @param challengeId ID del reto a completar.
     * @param imageUri URI de la nueva imagen seleccionada.
     * @param oldImageUrl URL de la imagen anterior en caso de estarse editando.
     */
    fun completeChallenge(
        challengeId: String,
        imageUri: Uri? = null,
        oldImageUrl: String? = null
    ) {
        val userId = auth.currentUser?.uid ?: return

        if (imageUri == null) {
            // Un reto de pixel art requiere estrictamente una imagen subida para poder ser completado.
            _errorMsg.value = R.string.select_image_error
            return //Salimos de la función
        }
        //Si hay imagen se actualiza el reto para completarse con la imagen del usuario
        uploadChallengeImage(userId, challengeId, imageUri, oldImageUrl)
    }

    /**
     * Comprime la imagen y la sube a Firebase Storage. Si tiene éxito, se comprueba si ya hay una imagen.
     * Si ya había una imagen para el reto (el usuario puede editar el reto las veces que quiera)
     * se borra la antigua y actualiza Firestore.
     *
     * Se utiliza la clase util [ImageUtils] para comprimir la imagen antes de almacenarla en la base de datos.
     */
    private fun uploadChallengeImage(
        userId: String,
        challengeId: String,
        imageUri: Uri,
        oldImageUrl: String?
    ) {
        // Indicamos que se está cargando para poder mostrar la barra en la UI
        //De otro modo no se entiende qué está pasando
        _isLoading.value = true
        //Obtenemos la fecha actual actual para usarla en la ruta de la imagen
        val timestamp = System.currentTimeMillis()
        //Obtenemos la instancia de Firebase Storage
        val storageRef = FirebaseStorage.getInstance().reference
            .child("userChallenges/$userId/${challengeId}_$timestamp.jpg") //Creamos una ruta basándonos en el momento de la subida

        //Se intenta comprimir la imagen
        val compressedImage = ImageUtils.compressImage(getApplication(), imageUri)

        if (compressedImage == null) {
            _isLoading.value = false
            _errorMsg.value = R.string.error_compressing_image
            return
        }

        //EN caso de que se haya comprimido correctamente se sube la imagen
        storageRef.putBytes(compressedImage)
            .addOnSuccessListener {
                // Borramos la foto anterior para liberar espacio si la hubiera
                deleteOldImageIfNeeded(oldImageUrl)

                // Se obtiene la url de la imagen y se actualiza el reto con los datos
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    updateChallengeInFirestore(userId, challengeId, uri.toString())
                }.addOnFailureListener {
                    //Si no se ha podido obtener la url se informa al usuario
                    _isLoading.value = false
                    _errorMsg.value = R.string.error_uploading_image
                }
            }
            .addOnFailureListener {
                //Si no se ha podido subir la imagen se informa al usuario
                _isLoading.value = false
                _errorMsg.value = R.string.error_uploading_image
            }
    }

    /**
     * Busca y elimina la imagen antigua del bucket del usuario en Firebase Storage de forma silenciosa.
     * @param oldImageUrl url de la antigua imagen subida al reto
     */
    private fun deleteOldImageIfNeeded(oldImageUrl: String?) {
        if (oldImageUrl.isNullOrEmpty()) return //Si no hay imagen que borrar se sale de la función

        try {
            val oldRef = FirebaseStorage.getInstance().getReferenceFromUrl(oldImageUrl)
            oldRef.delete()
        } catch (e: Exception) {
            Log.e("Storage", "Error al borrar imagen antigua: ${e.message}")
        }
    }

    /**
     * Actualiza un reto en Firestore con el estado a completado y la URL de la imagen del usuario.
     *
     * @param userId Identificador único del usuario autenticado en Firebase Auth.
     * @param challengeId ID del reto a actualizar tras ser completado.
     * @param imageUrl Enlace HTTPS apuntando a la imagen subida en Firebase Storage, o string vacío si no se subió imagen.
     */
    private fun updateChallengeInFirestore(userId: String, challengeId: String, imageUrl: String) {
        val updates = mapOf(
            "complete" to true,
            "userImagePath" to imageUrl
        )
        //Se actualizan los datos en la subcolección del usuario
        db.collection(Constants.COLLECTION_USERS)
            .document(userId)
            .collection(Constants.SUBCOLLECTION_USER_CHALLENGES)
            .document(challengeId)
            .update(updates)
            .addOnSuccessListener {
                _isLoading.value = false
                _successMsg.value = R.string.challenge_completed_success
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _errorMsg.value = R.string.error_completing_challenge
            }
    }

    /**
     * Elimina un reto de los seleccionados por el usuario.
     *
     * Borra el registro del documento de la subcolección en Firestore y también trata de eliminar
     * la imagen generada en Firebase Storage en el caso de que el reto tuviera una asignada.
     *
     * @param challengeId Identificador del reto del usuario a eliminar.
     * @param imageUrl URL de la imagen almacenada en Firebase Storage asociada al reto, o cadena nula o vacía en el caso de no existir aún.
     */
    fun deleteUserChallenge(challengeId: String, imageUrl: String?) {
        val userId = auth.currentUser?.uid ?: return

        db.collection(Constants.COLLECTION_USERS)
            .document(userId)
            .collection(Constants.SUBCOLLECTION_USER_CHALLENGES)
            .document(challengeId)
            .delete()
            .addOnSuccessListener {
                //Se informa al usuario de que se ha eliminado el reto
                _successMsg.value = R.string.challenge_deleted_success

                if (!imageUrl.isNullOrEmpty()) {
                    try {
                        val storageRef = FirebaseStorage.getInstance()
                            .getReferenceFromUrl(imageUrl)
                        storageRef.delete()
                    } catch (e: Exception) {
                        //Se controla la excepción internamente
                        Log.e("Storage", "Error al eliminar la imagen de Storage: ${e.message}")
                    }
                }
            }
            .addOnFailureListener {
                //Se informa del error
                _errorMsg.value = R.string.error_deleting_challenge
            }
    }

    /**
     * Limpia el mensaje de error actual en el LiveData para evitar duplicidad de avisos en la UI.
     */
    fun clearErrors() {
        _errorMsg.value = null
    }

    /**
     * Limpia el mensaje de éxito actual en el LiveData para evitar duplicidad de avisos en la UI.
     */
    fun clearSuccess() {
        _successMsg.value = null
    }

    /**
     * Libera el listener asociado a Firestore al ser destruido el componente, para evitar fugas de memoria.
     */
    override fun onCleared() {
        super.onCleared()
        challengesListener?.remove()
    }
}
