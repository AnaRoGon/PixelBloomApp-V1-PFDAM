package dam.pfdam.pixelbloom.viewmodel.boards

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import dam.pfdam.pixelbloom.data.model.Board
import io.mockk.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class BoardsViewModelTest {

    // Regla para que las operaciones de LiveData sean asíncronas
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    // Instancia del viewModel a testear
    private lateinit var viewModel: BoardsViewModel

    // Before -> Se ejecuta antes de que comiencen los tests. Como usamos firebase debemos de usar mocks para simular las llamadas
    @Before
    fun setUp() {
        // Inicializamos las anotaciones de MockK
        MockKAnnotations.init(this)

        // Mockeamos FirebaseAuth que usamos en el init del viewModel
        mockkStatic(FirebaseAuth::class)

        // Se crea un objeto 'falso' que simula el comportamiento de FirebaseAuth
        val authMock = mockk<FirebaseAuth>(relaxed = true)

        //Cada vez que simulemos la llamada nos devolverá el valor indicado en authblock
        every { FirebaseAuth.getInstance() } returns authMock

        // Mockeamos FirebaseFirestore porque el init también llama a db.collection()
        mockkStatic("com.google.firebase.firestore.FirestoreKt")
        // Se crea un objeto 'falso' que simula el comportamiento de FirebaseFirestore
        val firestoreMock = mockk<FirebaseFirestore>(relaxed = true)
        // Cada vez que simulemos la llamada nos devolverá el valor indicado en firestoreblock
        every { Firebase.firestore } returns firestoreMock

        // Creamos el ViewModel
        viewModel = BoardsViewModel()
    }

    // After -> Se ejecuta después de terminar cada test para limpiar los datos de los mocks y que no haya conflictos.
    @After
    fun tearDown() {
        unmockkAll()
    }

    //Tests para comprobar que el LiveData de los mensajes de error y éxito se actualiza correctamente
    @Test
    fun `clearErrors updates LiveData to null`() {
        // Given -> estado inicial. Asignamos un error simulado
        (viewModel.errorMsg as MutableLiveData).value = 12345

        // When -> se limpia el error con la función del viewModel
        viewModel.clearErrors()

        // Then -> Se comprueba que el resultado es el que esperamos. En este caso, null
        assertEquals(null, viewModel.errorMsg.value)
    }

    //Test para comprobar que el LiveData del mensaje de éxito se actualiza correctamente
    @Test
    fun `clearSuccess updates LiveData to null`() {
        // Given -> Estado inicial. Se asigna un mensaje de éxito simulado
        (viewModel.successMsg as MutableLiveData).value = 54321

        // When -> se limpia el mensaje con la función del viewModel
        viewModel.clearSuccess()

        // Then ->  Se comprueba que el resultado es el que esperamos. En este caso, null
        assertEquals(null, viewModel.successMsg.value)
    }

    //Test para comprobar que el LiveData del tablero seleccionado se actualiza correctamente
    @Test
    fun `selectBoardForMove updates selectedBoardForMove LiveData`() {
        // Given -> creamos un tablero simulado
        val mockBoard = Board(id = "board_test", title = "Tablero Prueba")

        // When -> se llama a la función para mover una referencia a un tablero
        viewModel.selectBoardForMove(mockBoard)

        // Then -> comprobamos que el LiveData ha guardado ese mismo tablero
        assertEquals(mockBoard, viewModel.selectedBoardForMove.value)
    }
}