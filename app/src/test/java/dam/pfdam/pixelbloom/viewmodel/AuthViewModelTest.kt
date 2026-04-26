package dam.pfdam.pixelbloom.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import dam.pfdam.pixelbloom.R

class AuthViewModelTest {

    //Regla para que las operaciones de LiveData sean asíncronas

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    //Instancia de FirebaseAuth
    @MockK
    private lateinit var auth: FirebaseAuth
    //Tarea simulada
    @MockK
    private lateinit var task: Task<AuthResult>

    //Observador del LiveData authResult
    @MockK
    private lateinit var authResultObserver: Observer<Boolean>


    //instancia del viewModel a testear
    private lateinit var viewModel: AuthViewModel

    // Before -> Se ejecuta antes de que comiencen los tests. Como usamos firebase usamos mocks para simular las llamadas
    @Before
    fun setUp() {
        // Se inicializa la anotación para el mock
        MockKAnnotations.init(this)

        //Se mockea la inicialización de FirebaseAuth.getInstance()
        mockkStatic(FirebaseAuth::class)

        //Resultado que queremos cada vez que se llame a FirebaseAuth
        every { FirebaseAuth.getInstance() } returns auth
        //Creamos el ViewModel
        viewModel = AuthViewModel()

        //Observamos los LiveData para capturar sus valores
        viewModel.authResult.observeForever(authResultObserver)

        // Definimos el comportamiento por defecto de los observers (no hacer nada)
        every { authResultObserver.onChanged(any()) } just Runs
    }

    // After -> Se ejecuta después de terminar cada test para limpiar los datos de los mocks y que no haya conflictos.
    @After
    fun tearDown() {
        //Limpiamos los mocks
        unmockkAll()
    }

    // Este test verifica que authResult se actualiza correctamente en caso de inicio de sesión exitoso.
    @Test
    fun `login success sets authResult to true`() {
        // Given -> con un mail o correo simulados
        val email = "user01@example.com"
        val password = "1234565"

        //Se mockea la llamada a signInWithEmailAndPassword
        every { auth.signInWithEmailAndPassword(email, password) } returns task
        //Se define el comportamiento de la tarea para que sea exitosa
        every { task.isSuccessful } returns true

        //Capturamos el listener que se pasa a addOnCompleteListener
        val slot = slot<OnCompleteListener<AuthResult>>()
        every { task.addOnCompleteListener(capture(slot)) } answers {
            //Se ejecuta el listener simulado de que la tarea se completó
            slot.captured.onComplete(task)
            task
        }

        //When -> Se llama a la función de login
        viewModel.login(email, password)

        //Then -> authResultObserver cambia a true y se actualiza el LiveData
        verify { authResultObserver.onChanged(true) }
        assertEquals(true, viewModel.authResult.value)

    }

    // Este test verifica que authResult se actualiza correctamente en caso de inicio de sesión fallido.
    @Test
    fun `login failure sets authResult to false`() {
        // Given -> simulamos unos datos de usuario incorrectos
        val email = "user@example.com"
        val password = "wrong_password"
        val exception = Exception("Error genérico")
        //Cada vez que se llame a signInWithEmailAndPassword se mockea el resultado
        every {
            auth.signInWithEmailAndPassword(
                email,
                password
            )
        } returns task
        //Definimos el comportamiento de la tarea para que falle
        every { task.isSuccessful } returns false
        //Definimos el comportamiento de la tarea para que devuelva una excepción
        every { task.exception } returns exception
        //Capturamos el listener que se pasa a addOnCompleteListener
        val slot =
            slot<OnCompleteListener<AuthResult>>()
        every { task.addOnCompleteListener(capture(slot)) } answers {
            //Se ejecuta el listener simulado de que la tarea se completó
            slot.captured.onComplete(task)
            task
        }

        // When -> se llama a la función de logeo
        viewModel.login(email, password)
        // Then -> authResultObserver cambia a false y se actualiza el LiveData
        verify { authResultObserver.onChanged(false) }
        assertEquals(false, viewModel.authResult.value)
        assertEquals(
            R.string.error_generic_operation,
            viewModel.errorMsg.value
        )

    }

    // Este test verifica que authResult se actualiza correctamente en caso de registro exitoso.
    @Test
    fun `register success sets authResult to true`() {
        // Given -> con un mail o correo simulados
        val email = "user01@example.com"
        val password = "1234565"

        //Se mockea la llamada a signInWithEmailAndPassword
        every { auth.createUserWithEmailAndPassword(email, password) } returns task
        //Se define el comportamiento de la tarea para que sea exitosa
        every { task.isSuccessful } returns true

        //Capturamos el listener que se pasa a addOnCompleteListener
        val slot = slot<OnCompleteListener<AuthResult>>()
        every { task.addOnCompleteListener(capture(slot)) } answers {
            //Se ejecuta el listener simulado de que la tarea se completó
            slot.captured.onComplete(task)
            task
        }

        //When -> se llama a la función para hacer el registro
        viewModel.register(email, password)
        //Then -> Simulamos que el registro ha sido exitoso y se actualiza el LiveData
        verify { authResultObserver.onChanged(true) }
        //Se comprueba que authResult se actualiza correctamente
        assertEquals(true, viewModel.authResult.value)

    }

    // Este test verifica que authResult se actualiza correctamente en caso de registro fallido.
    @Test
    fun `register failure sets authResult to false`() {
        //Given -> simulamos unos datos de usuario incorrectos para el registro
        val email = "user@example"
        val password = "wrong_password"
        val exception = Exception("Registration Error")

        every {
            auth.createUserWithEmailAndPassword(email, password)
        } returns task
        //Definimos el comportamiento de la tarea para que falle
        every { task.isSuccessful } returns false
        //Y lance una excepción
        every { task.exception } returns exception
        //Capturamos el listener que se pasa a addOnCompleteListener
        val slot = slot<OnCompleteListener<AuthResult>>()
        every { task.addOnCompleteListener(capture(slot)) } answers {
            //Se ejecuta el listener simulado de que la tarea se completó
            slot.captured.onComplete(task)
            task
        }

        // When -> Se llama a la función de registro
        viewModel.register(email, password)

        // Then -> Simulamos que el registro ha fallado y se actualiza el LiveData
        verify { authResultObserver.onChanged(false) }
        assertEquals(false, viewModel.authResult.value)
        assertEquals(
            R.string.error_generic_operation,
            viewModel.errorMsg.value
        )
    }

}
