package dam.pfdam.pixelbloom.view.fragment.feed

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import dam.pfdam.pixelbloom.R
import dam.pfdam.pixelbloom.ui.activity.MainActivity
import dam.pfdam.pixelbloom.utils.PreferenceHelper
import dam.pfdam.pixelbloom.viewmodel.MainViewModel
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FeedFragmentTest {
    //Variable para almacenar el estado de la guía al ejecutarse el test
    private var interactiveGuideOldState: Boolean = false

    @Before
    fun setup() {
        //Obtenemos el contexto de la aplicación
        val context = ApplicationProvider.getApplicationContext<Context>()
        // Obtenemos el estado de la guía antes de ejecutar los tests
        interactiveGuideOldState = PreferenceHelper(context).getInteractiveGuideState()
        // Forzamos el estado de la guía a completado para los tests
        PreferenceHelper(context).setInteractiveGuideState(true)
    }

    @After
    fun tearDown(){
        // Restablecemos la preferencia al estado anterior a la prueba
        val context = ApplicationProvider.getApplicationContext<Context>()
        PreferenceHelper(context).setInteractiveGuideState(interactiveGuideOldState)
    }

    @Test
    fun feedFragment_showsRecyclerView() {
        // Iniciamos el escenario de la actividad
        ActivityScenario.launch(MainActivity::class.java).use {
            // Verificamos que se muestra el RecyclerView
            onView(withId(R.id.recycler_view_feed))
                .check(matches(isDisplayed()))
        }
    }

    @Test
    fun feedFragment_showsAdminControls_whenUserIsAdmin() {
        //Iniciamos el escenario de la actividad
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val mainViewModel = ViewModelProvider(activity)[MainViewModel::class.java]
                
                // Como isAdmin es un LiveData de solo lectura, usamos reflexión para acceder a
                // MutableLiveData privado y simular el usuario que inició la app tiene permisos de admin
                val field = MainViewModel::class.java.getDeclaredField("_isAdmin")
                field.isAccessible = true
                val mutableIsAdmin = field.get(mainViewModel) as MutableLiveData<Boolean>
                mutableIsAdmin.postValue(true)
            }

            // Comprobamos que el botón flotante para agregar referencias está disponible
            onView(withId(R.id.add_reference_floating_button))
                .check(matches(isDisplayed()))
        }
    }
}