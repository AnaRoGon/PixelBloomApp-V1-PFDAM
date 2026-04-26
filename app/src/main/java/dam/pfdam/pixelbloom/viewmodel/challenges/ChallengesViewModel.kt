package dam.pfdam.pixelbloom.viewmodel.challenges

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.*
import dam.pfdam.pixelbloom.R
import dam.pfdam.pixelbloom.data.model.Challenge
import dam.pfdam.pixelbloom.utils.Constants
import dam.pfdam.pixelbloom.utils.DateUtils

/**
 * ViewModel que gestiona la lógica de la librería de challenges.
 * Implementa el patrón MVVM encargándose de la comunicación con Firebase Firestore y Storage.
 * Expone estados observables mediante LiveData para que la vista reaccione a cambios en la lista
 * de challenges, estados de subida y mensajes de error y éxito.
 *
 * @property db Instancia de Firebase Firestore para realizar las consultas de bases de datos.
 * @property listener Registro del listener de Firestore para poder cancelarlo posteriormente.
 * @property challenges Estado observable que contiene la lista de challenges obtenidos.
 * @property _challenges Estado interno (mutable) que contiene la lista de challenges obtenidos.
 * @property errorMsg Estado obserbable que almacena el mensaje descriptivo en caso de error en las operaciones de datos. Si no hay error contiene null.
 * @property _errorMsg Estado interno (mutable) del mensaje de error actual. Almacena el ID del recurso.
 * @property successMsg Estado observable que almacena el mensaje descriptivo en caso de éxito en las operaciones de datos. Si no hay éxito contiene null.
 * @property _successMsg Estado interno (mutable) del mensaje de éxito actual. Almacena el ID del recurso.
 * @property selectedChallengeForEdit Estado observable que contiene el challenge seleccionada para editar. Si no hay challenge seleccionada contiene null.
 * @property _selectedChallengeForEdit Estado interno (mutable) del challenge seleccionado para editar.
 */
class ChallengesViewModel : ViewModel() {
    private val db = Firebase.firestore
    private var listener: ListenerRegistration? = null

    private val _challenges = MutableLiveData<List<Challenge>>()
    val challenges: LiveData<List<Challenge>> get() = _challenges

    private val _errorMsg = MutableLiveData<Int?>()
    val errorMsg: LiveData<Int?> get() = _errorMsg

    private val _successMsg = MutableLiveData<Int?>()
    val successMsg: LiveData<Int?> get() = _successMsg
    private val _selectedChallengeForEdit = MutableLiveData<Challenge?>(null)
    val selectedChallengeForEdit: LiveData<Challenge?> get() = _selectedChallengeForEdit

    //Bloque init para cargar los datos la primera vez que se inicia el viewModel
    init {
        fetchChallenges()
    }

    /**
     * Establece qué challenge se desea editar en la vista.     *
     * @param challenge Objeto [Challenge] a editar o null para limpiar la selección.
     */
    fun selectChallengeForEdit(challenge: Challenge?) {
        _selectedChallengeForEdit.value = challenge
    }

    /**
     * Inicia la escucha activa de cambios en la colección de challenges en Firestore.
     * Utiliza addSnapshotListener para recibir actualizaciones en tiempo real.
     */
    private fun fetchChallenges() {
        if (listener != null) return //para asegurarnos de que no se sobreescriba
        // Se hace la consulta a Firestore y se ordena por fecha de creación
        listener = db.collection(Constants.COLLECTION_CHALLENGES)
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED ||
                        FirebaseAuth.getInstance().currentUser == null
                    ) {
                        //Se cierra la sesión del usuario
                        _errorMsg.value = R.string.error_permission_denied
                        FirebaseAuth.getInstance().signOut()
                        return@addSnapshotListener
                    }
                    _errorMsg.value = R.string.error_loading_challenges
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    //Se obtienen los retos y se mapean a objetos
                    val challengeList = snapshot.mapNotNull { document ->
                        try {
                            document.toObject(Challenge::class.java).apply {
                                id = document.id
                            }
                        } catch (e: Exception) {
                            //Se lanza un error interno
                            Log.e("Firestore", "Error al cargar documento: ${e.message}")
                            null
                        }
                    }
                    _challenges.value =
                        challengeList //Se actualiza el estado interno con el listado de challenges
                }
            }
    }

    /**
     * Actualiza los datos de un challenge existente en Firestore.
     *
     * @param id Identificador del documento a actualizar.
     * @param description Nueva descripción.
     * @param palette Nueva paleta de colores para el reto (o la original si no cambió).
     * @param title Nuevo título.
     */
    fun updateChallenge(
        id: String,
        description: String,
        palette: List<String>,
        title: String,
    ) {
        //Se crea un map con los nuevos datos
        val challenge = mapOf(
            "description" to description,
            "palette" to palette,
            "title" to title,
        )
        //Se hace la actualización del challenge en Firestore
        db.collection(Constants.COLLECTION_CHALLENGES).document(id)
            .update(challenge)
            .addOnSuccessListener {
                //Se manda un mensaje de éxito
                _successMsg.value = R.string.challenge_updated_success
            }
            .addOnFailureListener {
                //o de error
                _errorMsg.value = R.string.error_updating_challenge
            }
    }

    /**
     * Almacena los metadatos de un nuevo challenge en la colección de Firestore.
     *
     * Utiliza la clase de utilidad [DateUtils] para formatear la fecha que se guardará en Firestore.
     *
     * @param description Descripción del challenge.
     * @param title Título del challenge.
     */
    fun saveChallengeToFirestore(
        description: String,
        palette: List<String>,
        title: String
    ) {
        //Obtenemos la fecha y la formateamos
        val currentTimestamp = DateUtils.getCurrentTimestamp()

        val challenge = hashMapOf(
            "createdAt" to currentTimestamp,
            "description" to description,
            "palette" to palette,
            "title" to title
        )

        //Almacenamos  el challenge en la colección con todos los datos
        db.collection(Constants.COLLECTION_CHALLENGES).add(challenge)
            .addOnSuccessListener {
                //Se avisa al usuario si se ha almacenado correctamente el nuevo reto
                _successMsg.value = R.string.challenge_saved_success
            }
            .addOnFailureListener {
                //En caso contrario se informa del error
                _errorMsg.value = R.string.error_saving_challenge
            }
    }

    /**
     * Elimina el challenge con el ID especificado de Firestore
     * @param id ID del documento en Firestore.
     */

    fun deleteChallenge(id: String) {

        //Borramos el documento de la coleccion con el ID indicado por parámetro
        db.collection(Constants.COLLECTION_CHALLENGES).document(id).delete()
            .addOnSuccessListener {
                //Si se ha borrado correctamente se avisa al usuario
                _successMsg.value = R.string.challenge_deleted_success
            }
            .addOnFailureListener {
                //EN caso contrario se informa del error
                _errorMsg.value = R.string.error_deleting_challenge
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

    // Al cerrar el ViewModel, se cierra el snatchop para que deje de escuchar
    override fun onCleared() {
        super.onCleared()
        listener?.remove()
    }

}