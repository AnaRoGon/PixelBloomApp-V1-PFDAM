package dam.pfdam.pixelbloom.viewmodel.boards

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.*
import dam.pfdam.pixelbloom.R
import dam.pfdam.pixelbloom.data.model.Board
import dam.pfdam.pixelbloom.data.model.Reference
import dam.pfdam.pixelbloom.utils.Constants
import dam.pfdam.pixelbloom.utils.DateUtils
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.tasks.Task

/**
 * ViewModel que gestiona la lógica de los tableros del usuario.
 *
 * Implementa el patrón MVVM y se encarga de aislar de la vista las tareas con la subcolección
 * "boards" en Firestore específica de cada usuario. Sus responsabilidades incluyen crear,
 * leer, actualizar y eliminar tableros junto con sus referencias.
 *
 * @property db Instancia de Firebase Firestore usada como base de datos principal.
 * @property auth Instancia de FirebaseAuth para asegurar que sólo el usuario autenticado actúa.
 * @property boardsListener Registro del listener de Firestore para la colección de todos los tableros del usuario.
 * @property boardRefsListener Registro del listener de Firestore para la colección de referencias dentro de un único tablero.
 * @property _boards Estado interno y mutable con el listado principal de tableros.
 * @property boards Estado observable que la vista consume para presentar la colección de tableros.
 * @property _errorMsg Estado interno y mutable del mensaje de error actual. Almacena el ID del recurso.
 * @property errorMsg Estado observable que almacena el mensaje descriptivo en caso de error en las operaciones de datos. Si no hay error contiene null.
 * @property _successMsg Estado interno y mutable del mensaje de éxito actual. Almacena el ID del recurso.
 * @property successMsg Estado observable que almacena el mensaje descriptivo en caso de éxito en las operaciones de datos. Si no hay éxito contiene null.
 * @property _isLoading Estado interno mutable del estado de la solicitud a Firebase.
 * @property isLoading Estado observable que indica a la vista si hay una operación asíncrona en curso.
 * @property _selectedBoardReferences Estado interno mutable del listado de referencias del tablero.
 * @property selectedBoardReferences Estado observable que contiene el listado de referencias de un tablero seleccionado.
 * @property _selectedBoardForMove Estado interno mutable para la herramienta de mover tablero.
 * @property selectedBoardForMove Estado observable utilizado en el split button para guardar temporalmente qué tablero queremos mover.
 */
class BoardsViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    private var boardsListener: ListenerRegistration? = null
    private var boardRefsListener: ListenerRegistration? = null

    private val _boards = MutableLiveData<List<Board>>()
    val boards: LiveData<List<Board>> get() = _boards

    private val _errorMsg = MutableLiveData<Int?>()
    val errorMsg: LiveData<Int?> get() = _errorMsg

    private val _successMsg = MutableLiveData<Int?>()
    val successMsg: LiveData<Int?> get() = _successMsg

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _selectedBoardReferences = MutableLiveData<List<Reference>>()
    val selectedBoardReferences: LiveData<List<Reference>> get() = _selectedBoardReferences

    private val _selectedBoardForMove = MutableLiveData<Board?>(null)
    val selectedBoardForMove: LiveData<Board?> get() = _selectedBoardForMove

    //Bloque init para cargar los datos la primera vez que se inicia el viewModel
    init {
        fetchUserBoards()
    }

    /**
     * Inicia la escucha de los tableros creados por el usuario actual.
     * Utiliza addSnapshotListener para recibir actualizaciones en tiempo real de la subcolección de 'boards'.
     */
    private fun fetchUserBoards() {
        //UID del usuario actual
        val userId = auth.currentUser?.uid ?: return

        if (boardsListener != null) return //Si ya se está escuchando salimos de la función

        //Se instancia el listener con la consulta de los tableros del usuario desde el acceso a la subcolección en firestore
        //Se ordenan los tableros recibidos para que el último creado aparezca primero
        boardsListener = db.collection(Constants.COLLECTION_USERS)
            .document(userId)
            .collection(Constants.SUBCOLLECTION_BOARDS)
            // Se muestran por orden de fecha descendente (más nuevos primero)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED || auth.currentUser == null) {
                        _errorMsg.value = R.string.error_permission_denied
                        auth.signOut()
                        return@addSnapshotListener
                    }
                    _errorMsg.value = R.string.error_loading_boards
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    //Se obtiene el listado de tableros y se mapea al objeto correspondiente
                    val boardsList = snapshot.mapNotNull { document ->
                        try {
                            // Se mapea cada documento a un objeto Board directamente
                            document.toObject(Board::class.java)
                        } catch (e: Exception) {
                            Log.e("Firestore", "Error al cargar tablero del usuario: ${e.message}")
                            null
                        }
                    }
                    resolveBoardImageUrls(boardsList) //utilizamos la función para obtener las url de las imagenes del tablero que nos llega
                }
            }
    }

    /**
     * Obtiene la URL de las imágenes en Firestore a partir de sus identificadores.
     * Extrae un máximo de tres referencias por tablero para mostrar en la previsualización.
     *
     * @param boardsList Lista de tableros a procesar.
     */
    private fun resolveBoardImageUrls(boardsList: List<Board>) {
        //Si la lista de tablero está vacía se almacena un listado vacío y salimos
        if (boardsList.isEmpty()) {
            _boards.value = emptyList()
            return
        }
        //Variables para obtener las referencias de los tableros de forma dinámica
        val updatedBoards = boardsList.toMutableList() //Se crea una copia mutable del tablero
        val tasksUrlReferencesRequest =
            mutableListOf<Task<*>>() //Y una lista de tareas para esperar la tarea de consulta a la bd

        //Tomamos la lista de tableros para obtener las referencias que contiene
        boardsList.forEachIndexed { index, board ->
            //Se toman los primeros tres identificadores de la lista
            val idsToFetch = board.imageRefs.take(3)
            //Si hay identificadores se instancia la consulta a Firestore
            if (idsToFetch.isNotEmpty()) {
                //Se instancia la consulta a Firestore
                val task = db.collection(Constants.COLLECTION_REFERENCES)
                    .whereIn(FieldPath.documentId(), idsToFetch)//Se filtra por los identificadores
                    .get()
                    .addOnSuccessListener { snapshots ->
                        val urlMap = snapshots.documents.associate {
                            it.id to (it.getString("imagePath")
                                ?: "") //Se obtiene la URL de cada referencia, si no hay se pone una cadena vacía
                        }
                        // Sustituimos el ID por la URL en la lista de referencias
                        val resolvedRefs = board.imageRefs.map { id ->
                            urlMap[id] ?: id // Si no está en Firestore se queda igual
                        }
                        //Se actualiza el tablero con su lista de referencias asociada
                        updatedBoards[index] = board.copy(imageRefs = resolvedRefs)
                    }
                tasksUrlReferencesRequest.add(task) //Se añade el resultado de la consulta al listado de tareas
            }
        }

        if (tasksUrlReferencesRequest.isEmpty()) {
            _boards.value = updatedBoards //Actualizamos la lista si está vacía
        } else {
            Tasks.whenAllComplete(tasksUrlReferencesRequest)
                .addOnCompleteListener { //Manejamos el resultado de la tarea de obtener las referencias de tablero de manera asíncrona
                    _boards.value =
                        updatedBoards //Y si se han completado se obtienen los tableros con sus referencias actualizadas.
                }.addOnFailureListener {
                    //Si no se pueden obtener se informa al usuario
                    _errorMsg.value = R.string.error_loading_boards
                }
        }
    }


    /**
     * Inicia la escucha de los boards del usuario actual.
     * Utiliza addSnapshotListener para recibir actualizaciones en tiempo real de la subcolección de 'boards'.

     * @param boardId identificados único del tablero del que se quieren obtener las referencias.
     */
    fun fetchBoardReferences(boardId: String) {

        //Obtenemos el UID del usuario actual
        val userId = auth.currentUser?.uid ?: return
        // Limpiamos listener previo para poder actualizar la lista
        boardRefsListener?.remove()
        //Marcamos el estado de carga
        _isLoading.value = true

        //Se instancia el listener con la consulta a Firestore
        boardRefsListener = db.collection(Constants.COLLECTION_USERS)
            .document(userId)
            .collection(Constants.SUBCOLLECTION_BOARDS)
            .document(boardId)//Accedemos a la ruta de la subcolección de tableros del user con el id del tablero que se pasa por parámetro
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED || auth.currentUser == null) {
                        _errorMsg.value = R.string.error_listening_board
                        auth.signOut()
                        return@addSnapshotListener
                    }
                    _errorMsg.value = R.string.error_loading_boards
                    return@addSnapshotListener
                }
                //Almacenamos la 'fotografía' como un objeto del tipo 'Board' para poder acceder a sus propiedades
                val boardSnapshot = snapshot?.toObject(Board::class.java)
                //Verificamos que no tenemos datos vacíos
                if (boardSnapshot != null && boardSnapshot.imageRefs.isNotEmpty()) {
                    db.collection(Constants.COLLECTION_REFERENCES)
                        .whereIn(
                            FieldPath.documentId(),
                            boardSnapshot.imageRefs
                        )//Filtramos por los identificadores de las referencias
                        .get()
                        .addOnSuccessListener { refSnapshots ->
                            //Obtenemos las referencias del tablero
                            val boardReferences = refSnapshots.documents.mapNotNull { doc ->
                                doc.toObject(Reference::class.java)?.apply {
                                    id = doc.id
                                }
                            }
                            //Se actualiza el estado con el listado de referencias
                            _selectedBoardReferences.value = boardReferences
                            _isLoading.value = false
                        }
                        .addOnFailureListener {
                            _errorMsg.value = R.string.error_loading_boards
                            _isLoading.value = false
                        }
                } else {
                    //Si no hay referencias se manda un listado vacío y se termina la carga
                    _selectedBoardReferences.value = emptyList()
                    _isLoading.value = false
                }
            }
    }

    /**
     * Crea un nuevo tablero que puede o no tener referencias.
     *
     * @param title nombre del tablero asignado por el usuario.
     * @param initialReferenceId referencia almacenada en el tablero.
     */
    fun createBoard(title: String, initialReferenceId: String? = null) {
        //UID del usuario actual
        val userId = auth.currentUser?.uid ?: return
        //Referencia a la subcolección de tableros del usuario
        val boardRef = db.collection(Constants.COLLECTION_USERS)
            .document(userId)
            .collection(Constants.SUBCOLLECTION_BOARDS)
            .document()
        //Se comprueba si viene referencia y si no se crea un listado vacío para almacenarlas cuando sea necesario.
        val imageRefs = if (initialReferenceId != null) listOf(initialReferenceId) else emptyList()
        //Se crea el nuevo tablero con sus datos
        val newBoard = Board(
            id = boardRef.id,
            title = title,
            imageRefs = imageRefs,
            createdAt = DateUtils.getCurrentTimestamp()
        )
        //Se almacena el nuevo tablero en la subcolección de tableros del usuario
        boardRef.set(newBoard)
            .addOnSuccessListener {
                if (initialReferenceId != null) {
                    //Se avisa al usuario de que se ha creado el tablero y se ha guardado la imagen
                    _successMsg.value = R.string.board_image_saved_success
                    _successMsg.value = R.string.board_created_success
                } else {
                    //Si no había referencia se avisa al usuario de que se ha creado el tablero
                    _successMsg.value = R.string.board_created_success
                }
            }
            .addOnFailureListener {
                //EN caso de que no haya podido crearse el tablero se avisa al usuario
                _errorMsg.value = R.string.error_loading_boards
            }
    }

    /**
     * Mueve una referencia de un tablero a otro.
     * @param referenceId identificador de la referencia a mover.
     * @param fromBoardId ID del tablero desde el que se ha extraído la referencia.
     * @param toBoardId ID del tablero al que se añadirá la referencia.
     */
    fun moveReferenceToBoard(referenceId: String, fromBoardId: String, toBoardId: String) {
        //UID del usuario actual
        val userId = auth.currentUser?.uid ?: return

        //Si el tablero indicado es el mismo se avisa al usuario y salimos de la función
        if (fromBoardId == toBoardId) {
            _errorMsg.value = R.string.error_same_board
            return
        }

        //Referencia a la subcolección del tablero de origen del usuario
        val fromBoard = db.collection(Constants.COLLECTION_USERS)
            .document(userId)
            .collection(Constants.SUBCOLLECTION_BOARDS)
            .document(fromBoardId)
        //Se utiliza la función de runBatch de Firestore que nos garantiza que o se hacen los dos pasos correctamente, o no se hace ninguno
        //Es decir, se elimina la referencia de un tablero y se pasa al otro.
        db.runBatch { batch ->
            //Se elimina la referencia del tablero de origen
            batch.update(fromBoard, "imageRefs", FieldValue.arrayRemove(referenceId))

            if (toBoardId.isNotEmpty()) {
                //Se añade la referencia al tablero de destino
                val toBoard = db.collection(Constants.COLLECTION_USERS)
                    .document(userId)
                    .collection(Constants.SUBCOLLECTION_BOARDS)
                    .document(toBoardId)

                batch.update(toBoard, "imageRefs", FieldValue.arrayUnion(referenceId))
            }
        }.addOnSuccessListener {
            //Si se ha podido mover se informa al usuario y se limpia el tablero seleccionado para mover
            _successMsg.value = R.string.board_image_moved_success
            _selectedBoardForMove.value = null
        }.addOnFailureListener {
            //En caso contrario se avisa al usuario
            _errorMsg.value = R.string.error_moving_image
        }
    }

    /**
     * Selecciona un tablero para iniciar el movimiento de una imagen.
     *
     * @param board El tablero seleccionado.
     */
    fun selectBoardForMove(board: Board) {
        _selectedBoardForMove.value = board
    }

    /**
     * Reseteo de la variable que contiene el tablero seleccionado para mover.
     */
    fun clearSelectedBoardForMove() {
        _selectedBoardForMove.value = null
    }

    /**
     * Añade una referencia a un tablero existente.
     *
     * @param boardId ID del tablero donde se almacenará la referencia.
     * @param referenceId ID de la referencia seleccionada.
     */
    fun addReferenceToBoard(boardId: String, referenceId: String) {
        //UID del usuario actual
        val userId = auth.currentUser?.uid ?: return
        //Variable para almacenar temporalmente la subcolección del tablero del usuario que se recibe por parámetro
        val boardRef = db.collection(Constants.COLLECTION_USERS)
            .document(userId)
            .collection(Constants.SUBCOLLECTION_BOARDS)
            .document(boardId)
        //Se comprueba si la referencia ya está en el tablero
        boardRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                //Se obtiene el objeto del tablero
                val board = document.toObject(Board::class.java)
                //Si el tablero ya contiene la referencia se avisa al usuario y salimos
                if (board != null && board.imageRefs.contains(referenceId)) {
                    _errorMsg.value = R.string.error_same_board
                    return@addOnSuccessListener
                }
                // Utilizamos arrayUnion para evitar duplicados en la lista de IDs
                db.runBatch { batch ->
                    batch.update(
                        boardRef,
                        "imageRefs",
                        FieldValue.arrayUnion(referenceId)
                    ) //Se actualiza añadiendo la nueva referencia si no existe
                }.addOnSuccessListener {
                    //Se informa al usuario de que se ha guardado la referencia
                    _successMsg.value = R.string.board_image_saved_success
                }.addOnFailureListener {
                    //Si no se puede actualizar se avisa al usuario
                    _errorMsg.value = R.string.error_saving_to_board
                }
            } else {
                //Si no se puede almacenar la referencia se avisa al usuario
                _errorMsg.value = R.string.error_saving_to_board
            }
        }.addOnFailureListener {
            //Se manda un mensaje de error también si falla la consulta
            _errorMsg.value = R.string.error_saving_to_board
        }
    }

    /**
     * Elimina una referencia concreta de un tablero.
     *
     * @param boardId ID del tablero que contiene la referencia.
     * @param referenceId ID de la referencia a eliminar.
     */
    fun deleteReferenceBoard(boardId: String, referenceId: String) {
        //UID del usuario actual
        val userId = auth.currentUser?.uid ?: return
        //Eliminamos la referencia del tablero
        db.collection(Constants.COLLECTION_USERS)
            .document(userId)
            .collection(Constants.SUBCOLLECTION_BOARDS)
            .document(boardId)
            .update("imageRefs", FieldValue.arrayRemove(referenceId))//Se elimina la referencia
            .addOnSuccessListener {
                //Si ha ido bien se informa al usuario
                _successMsg.value = R.string.board_image_deleted_success
            }
            .addOnFailureListener {
                //Si no se ha podido eliminar se avisa al usuario
                _errorMsg.value = R.string.error_deleting_image
            }
    }

    /**
     * Elimina el tablero que se le pasa como parámetro del usuario actual.
     *
     * @param boardId ID del tablero a eliminar.
     */
    fun deleteBoard(boardId: String?) {
        //UID del usuario actual
        val userId = auth.currentUser?.uid ?: return
        //Eliminamos el tablero de la subcolección de boards del usuario
        db.collection(Constants.COLLECTION_USERS)
            .document(userId)
            .collection(Constants.SUBCOLLECTION_BOARDS)
            .document(boardId ?: return)
            .delete()
            .addOnSuccessListener {
                _successMsg.value = R.string.board_deleted_success
            }.addOnFailureListener {
                _errorMsg.value = R.string.error_deleting_board
            }
    }

    /**
     * Actualiza el nombre de un tablero existente.
     *
     * @param boardId ID del tablero al que se le quiere cambiar el nombre.
     * @param newTitle titulo que tendrá el tablero.
     */
    fun updateBoard(boardId: String, newTitle: String) {
        //Se crea un map con el campo modificado
        val board = mapOf("title" to newTitle)
        //Se actualiza el titulo del tablero en Firestore
        db.collection(Constants.COLLECTION_USERS).document(auth.currentUser?.uid ?: return)
            .collection(Constants.SUBCOLLECTION_BOARDS).document(boardId)
            .update(board)
            .addOnSuccessListener {
                _successMsg.value = R.string.board_updated_success
            }
            .addOnFailureListener {
                _errorMsg.value = R.string.error_updating_board
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
        boardsListener?.remove()
        boardRefsListener?.remove()
    }
}