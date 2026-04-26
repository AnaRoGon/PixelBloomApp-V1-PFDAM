package dam.pfdam.pixelbloom.viewmodel.feed

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import dam.pfdam.pixelbloom.data.model.Reference
import dam.pfdam.pixelbloom.utils.Constants
import java.util.UUID
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import dam.pfdam.pixelbloom.R
import dam.pfdam.pixelbloom.utils.ImageUtils
import dam.pfdam.pixelbloom.utils.DateUtils

/**
 * ViewModel que gestiona la lógica de la librería de referencias. *
 * Implementa el patrón MVVM encargándose de la comunicación con Firebase Firestore y Storage.
 * Expone estados observables mediante LiveData para que la vista reaccione a cambios en la lista
 * de referencias, estados de subida y mensajes de error y éxito.
 *
 * @property db Instancia de Firebase Firestore para realizar las consultas de bases de datos.
 * @property listener Registro del listener de Firestore para poder cancelarlo posteriormente.
 * @property storage Instancia de Firebase Storage para gestionar archivos multimedia (imágenes).
 * @property sharedPrefs Preferencias compartidas para almacenar datos los favoritos en local.
 * @property references Estado observable que contiene la lista de referencias obtenidas.
 * @property _references Estado interno (mutable) que contiene la lista de referencias obtenidas.
 * @property errorMsg Estado obserbable que almacena el mensaje descriptivo en caso de error en las operaciones de datos. Si no hay error contiene null.
 * @property _errorMsg Estado interno (mutable) del mensaje de error actual. Almacena el ID del recurso.
 * @property successMsg Estado observable que almacena el mensaje descriptivo en caso de éxito en las operaciones de datos. Si no hay éxito contiene null.
 * @property _successMsg Estado interno (mutable) del mensaje de éxito actual. Almacena el ID del recurso.
 * @property selectedReferenceForEdit Estado observable que contiene la referencia seleccionada para editar. Si no hay referencia seleccionada contiene null.
 */
class FeedViewModel(application: Application) : AndroidViewModel(application) {
    private val db = Firebase.firestore
    private var listener: ListenerRegistration? = null
    private val storage = FirebaseStorage.getInstance()
    private val sharedPrefs =
        application.getSharedPreferences(Constants.PREFS_MAIN, Context.MODE_PRIVATE)

    private val _references = MutableLiveData<List<Reference>>()
    val references: LiveData<List<Reference>> get() = _references

    private val _errorMsg = MutableLiveData<Int?>()
    val errorMsg: LiveData<Int?> get() = _errorMsg

    private val _successMsg = MutableLiveData<Int?>()
    val successMsg: LiveData<Int?> get() = _successMsg
    val selectedReferenceForEdit = MutableLiveData<Reference?>(null)

    //Bloque init para cargar los datos la primera vez que se crea el viewModel
    init {
        fetchReferences()
    }

    /**
     * Establece qué referencia se desea editar en la vista.
     * @param reference Objeto [Reference] a editar o null para limpiar la selección.
     */
    fun selectReferenceForEdit(reference: Reference?) {
        selectedReferenceForEdit.value = reference
    }

    /**
     * Inicia la escucha activa de cambios en la colección de referencias en Firestore.
     * Utiliza addSnapshotListener para recibir actualizaciones en tiempo real.
     */
    fun fetchReferences() {
        if (listener != null) return //para asegurarnos de que no se sobreescriba
        // Se hace la consulta a Firestore y se ordena por fecha de creación
        listener = db.collection(Constants.COLLECTION_REFERENCES)
            .orderBy(
                "createdAt",
                com.google.firebase.firestore.Query.Direction.ASCENDING
            )
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED ||
                        FirebaseAuth.getInstance().currentUser == null
                    ) {
                        //Se cierra la sesión del usuario por seguridad
                        _errorMsg.value = R.string.error_permission_denied
                        FirebaseAuth.getInstance().signOut()
                        return@addSnapshotListener
                    }
                    //Se manda un error si no se obtienen referencias
                    _errorMsg.value = R.string.error_loading_references
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    //Se obtienen las referencias y se mapean a objetos
                    val referenceList = snapshot.mapNotNull { document ->
                        try {
                            document.toObject(Reference::class.java).apply {
                                id = document.id
                            }
                        } catch (e: Exception) {
                            //Se pasa el error de manera interna
                            Log.e("Firestore", "Error al cargar documento: ${e.message}")
                            null
                        }
                    }
                    _references.value =
                        referenceList //Se actualiza el estado interno con el listado de referencias
                }
            }
    }

    /**
     * Actualiza el estado de favorito de una referencia tanto localmente como en Firestore.
     *
     * @param id Identificador único de la referencia.
     * @param isFavorite True si se marca como favorito, false en caso contrario.
     */
    fun updateFavoriteStatus(id: String, isFavorite: Boolean) {
        //referencia al documento de firestore
        val referenceRef = db.collection(Constants.COLLECTION_REFERENCES).document(id)
        //verificamos el estado actual en shared preferences para evitar actualizaciones innecesaias.
        val currentlyFavorite = isReferenceFavorited(id)
        if (currentlyFavorite == isFavorite) return //Si ya está en favoritos no se hace nada

        //Actualizamos localmente
        updateLocalFavorites(id, isFavorite)

        //Actualizamos en Firestore
        val increment =
            if (isFavorite) 1L else -1L //Si es favorito se incrementa en 1, si no se decrementa en 1
        //Se actualiza el valor de favoritos con increment.
        referenceRef.update("favorites", FieldValue.increment(increment))
            .addOnSuccessListener {
                //comprobamos que se ha actualizado correctamente de manera interna
                Log.d("Firestore", "Favorito actualizado correctamente")
            }
            .addOnFailureListener {
                //Solo en caso de que la actualización falle se avisa al usaurio del error
                _errorMsg.value = R.string.error_saving_favorite
                //Se actualiza localmente
                updateLocalFavorites(id, !isFavorite)
            }
    }

    /**
     * Actualiza la lista de identificadores de favoritos guardada localmente en SharedPreferences.
     *
     * @param id Identificador único de la referencia a actualizar.
     * @param isFavorited True para añadir el ID a la lista local, false para eliminarlo.
     */
    fun updateLocalFavorites(id: String, isFavorited: Boolean) {
        val currentFavs =
            sharedPrefs.getStringSet(Constants.KEY_FAVORITE_IDS, emptySet())?.toMutableSet()
                ?: mutableSetOf()
        //Actualizamos la lista de favoritos.
        if (isFavorited) currentFavs.add(id) else currentFavs.remove(id)
        //Se guardan los cambios en shared preferences
        sharedPrefs.edit {
            putStringSet(Constants.KEY_FAVORITE_IDS, currentFavs)
        }
    }

    /**
     * Comprueba si una referencia específica está marcada como favorita localmente.
     *
     * @param id Identificador de la referencia.
     * @return True si existe en las preferencias de favoritos, false en caso contrario.
     */
    fun isReferenceFavorited(id: String): Boolean {
        val favs = sharedPrefs.getStringSet(Constants.KEY_FAVORITE_IDS, emptySet())
        // Si la lista contiene el id de la referencia, está en favoritos
        // Si no existe lista devuelve false.
        return favs?.contains(id) == true
    }


    /**
     * Almacena una nueva referencia en FIrestore y Storage haciendo uso de la clase
     * de utilidad [ImageUtils] para comprimir la imagen antes de subirla.
     *
     * @param author Nombre del autor de la obra.
     * @param description Descripción detallada de la referencia.
     * @param imageUri URI local de la imagen seleccionada por el usuario.
     * @param title Título representativo de la referencia.
     */
    fun uploadReference(author: String, description: String, imageUri: Uri, title: String) {
        // Define la ruta de destino y el nombre UUID  del archivo en Firebase Storage
        val fileRef = storage.reference.child("references/ref${UUID.randomUUID()}")
        try {
            //Obtenemos la imagen comprimida
            val compressedImage = ImageUtils.compressImage(getApplication(), imageUri)
            //Si la compresión de la imagen no nos devuelve un null
            if (compressedImage != null) {
                //Se intenta subir la imagen a Storage comprimida
                fileRef.putBytes(compressedImage)
                    .addOnSuccessListener {
                        //Obtenemos la url pública de la imagen
                        fileRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                            //Una vez subida la imagen y almacenada se guarda la referencia
                            saveReferenceToFirestore(
                                author,
                                description,
                                0,
                                downloadUrl.toString(),
                                title
                            )
                        }
                    }
                    .addOnFailureListener {
                        _errorMsg.value = R.string.error_uploading_image
                    }
            } else {
                _errorMsg.value = R.string.error_compressing_image
            }
        } catch (e: Exception) {
            _errorMsg.value = R.string.error_uploading_image
        }
    }


    /**
     * Almacena los metadatos de una referencia en la colección de Firestore.
     * Utiliza la clase de utilidad [DateUtils] para formatear la fecha que se guardará en Firestore.
     *
     * @param author Autor de la referencia.
     * @param description Descripción de la referencia.
     * @param favorites Contador inicial de favoritos (por defecto 0).
     * @param imagePath URL pública de la imagen en Firebase Storage.
     * @param title Título de la referencia.
     */
    fun saveReferenceToFirestore(
        author: String,
        description: String,
        favorites: Int = 0,
        imagePath: String,
        title: String
    ) {
        //Obtenemos la fecha y la formateamos
        val currentTimestamp = DateUtils.getCurrentTimestamp()
        //Se crea un map con los campos a guardar
        val reference = mapOf(
            "author" to author,
            "createdAt" to currentTimestamp,
            "description" to description,
            "favorites" to favorites,
            "imagePath" to imagePath,
            "title" to title
        )

        //Almacenamos la referencia en la colección con todos los datos
        db.collection(Constants.COLLECTION_REFERENCES).add(reference)
            .addOnSuccessListener {
                _successMsg.value = R.string.reference_saved_success
            }
            .addOnFailureListener {
                _errorMsg.value = R.string.error_saving_reference
            }
    }

    /**
     * Actualiza los datos de una referencia existente en Firestore.
     *
     * @param id Identificador del documento a actualizar.
     * @param author Nuevo autor.
     * @param description Nueva descripción.
     * @param imagePath Nueva ruta de imagen (o la actual si no cambió).
     * @param title Nuevo título.
     */
    fun updateReference(
        id: String,
        author: String,
        description: String,
        imagePath: String,
        title: String
    ) {
        //Se crea un map con los datos a actualizar
        val reference = mapOf(
            "author" to author,
            "description" to description,
            "imagePath" to imagePath,
            "title" to title,
        )
        //Actualizamos la referencia en Firestore
        db.collection(Constants.COLLECTION_REFERENCES).document(id)
            .update(reference)
            .addOnSuccessListener {
                _successMsg.value = R.string.reference_updated_success
            }
            .addOnFailureListener {
                _errorMsg.value = R.string.error_updating_reference
            }
    }

    /**
     * Elimina una referencia de Firestore y su archivo de imagen asociado en Storage.
     * @param id ID del documento en Firestore.
     * @param imageUrl URL de la imagen en Storage para borrar el archivo físico.
     */

    fun deleteReference(id: String, imageUrl: String?) {
        //Borramos el documento de la coleccion con el id indicado por parámetro
        db.collection(Constants.COLLECTION_REFERENCES).document(id).delete()
            .addOnSuccessListener {
                Log.d("Firestore", "Documento eliminado con éxito")
                _successMsg.value = R.string.reference_deleted_success
                //Si la imagen no es null o vacía (que no debería de serlo, pero por si acaso) se elimina de Storage
                if (!imageUrl.isNullOrEmpty()) {
                    try {
                        //Borramos la imagen asociada
                        storage.getReferenceFromUrl(imageUrl).delete()
                    } catch (e: Exception) {
                        _errorMsg.value = R.string.error_deleting_reference
                    }
                }
            }
            .addOnFailureListener {
                _errorMsg.value = R.string.error_deleting_reference
                Log.e("Firestore", "Error al eliminar la referencia")
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