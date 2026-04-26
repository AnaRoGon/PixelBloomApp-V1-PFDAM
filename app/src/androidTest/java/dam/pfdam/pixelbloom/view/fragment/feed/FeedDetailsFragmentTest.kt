package dam.pfdam.pixelbloom.view.fragment.feed

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dam.pfdam.pixelbloom.R
import dam.pfdam.pixelbloom.data.model.Reference
import dam.pfdam.pixelbloom.ui.activity.MainActivity
import dam.pfdam.pixelbloom.utils.PreferenceHelper
import dam.pfdam.pixelbloom.viewmodel.MainViewModel
import dam.pfdam.pixelbloom.viewmodel.feed.FeedViewModel
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FeedDetailsFragmentTest {
    //Variable para almacenar el estado de la guía al ejecutarse el test
    private var interactiveGuideOldState: Boolean = false

    @Before
    fun setup() {
        //Obtenemos el contexto de la aplicación
        val context = ApplicationProvider.getApplicationContext<Context>()
        // Obtenemos el estado de la guía antes de ejecutar los tests
        interactiveGuideOldState = PreferenceHelper(context).getInteractiveGuideState()
        // Forzamos el estado completado de la guía para el test
        PreferenceHelper(context).setInteractiveGuideState(true)
    }

    @After
    fun tearDown(){
        // Restablecemos la preferencia al estado anterior a la prueba
        val context = ApplicationProvider.getApplicationContext<Context>()
        PreferenceHelper(context).setInteractiveGuideState(interactiveGuideOldState)
    }

    @Test
    fun feedDetailsFragment_showsReferenceDetails() {
        //Datos de mockeo para las pruebas
        val mockId = "mock-123"
        val mockTitle = "Título de prueba"
        val mockDescription = "Descripción de la referencia para prueba de mockeo"

        //Iniciamos el escenario de la actividad
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val viewModel = ViewModelProvider(activity)[FeedViewModel::class.java]

                //Se simula una referencia con los datos mockeados
                val mockedReference = Reference(
                    id = mockId,
                    title = mockTitle,
                    description = mockDescription,
                    favorites = 10
                )
                (viewModel.references as MutableLiveData).value = listOf(mockedReference)
                // Obtenemos el navegador de la actividad
                val navHostFragment = activity.supportFragmentManager
                    .findFragmentById(R.id.nav_host_fragment_main) as NavHostFragment
                //Le pasamos el id de la referencia mockeada y navegamos al fragment
                val bundle = Bundle().apply { putString("id", mockId) }
                navHostFragment.navController.navigate(R.id.feedDetailsFragment, bundle)
            }

            //Verificamos que los datos mockeados se han pasado correctamente
            //Se muestra el titulo indicado de la referencia
            onView(withId(R.id.feed_details_title))
                .check(matches(isDisplayed()))
                .check(matches(withText(mockTitle)))
            //Se muestra la descripción de la referencia
            onView(withId(R.id.feed_details_description))
                .check(matches(isDisplayed()))
                .check(matches(withText(mockDescription)))
        }
    }

    @Test
    fun feedDetailsFragment_showUIComponents_correctly() {
        //Datos de mockeo para las pruebas
        val mockId = "mock-456"
        val mockTitle = "Título de prueba"
        val mockDescription = "Descripción de la referencia para prueba de mockeo"

        //Iniciamos el escenario de la actividad
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val viewModel = ViewModelProvider(activity)[FeedViewModel::class.java]

                //Se simula una referencia con los datos mockeados
                val mockedReference = Reference(
                    id = mockId,
                    title = mockTitle,
                    description = mockDescription,
                    favorites = 10
                )
                (viewModel.references as MutableLiveData).value = listOf(mockedReference)
                // Obtenemos el navegador de la actividad
                val navHostFragment = activity.supportFragmentManager
                    .findFragmentById(R.id.nav_host_fragment_main) as NavHostFragment
                //Le pasamos el id de la referencia mockeada y navegamos al fragment
                val bundle = Bundle().apply { putString("id", mockId) }
                navHostFragment.navController.navigate(R.id.feedDetailsFragment, bundle)
            }

            //Se muestra el botón de favoritos
            onView(withId(R.id.favorite_button))
                .check(matches(isDisplayed()))
            //Se muestra el TextView para los "likes"
            onView(withId(R.id.feed_details_num))
                .check(matches(isDisplayed()))
            //Se muestra el botón para guardar la imagen
            onView(withId(R.id.feed_details_save_button))
                .check(matches(isDisplayed()))
            //Se muestra el image view de la imagen
            onView(withId(R.id.feed_details_image))
                .check(matches(isDisplayed()))
        }
    }

    @Test
    fun feedDetailsFragment_showsAdminOptions_whenUserIsAdmin() {

        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val viewModel = ViewModelProvider(activity)[FeedViewModel::class.java]
                val mainViewModel = ViewModelProvider(activity)[MainViewModel::class.java]

                (viewModel.references as MutableLiveData).value = listOf(Reference(id = "mocked-admin"))

                // Como isAdmin es un LiveData de solo lectura, usamos reflexión para acceder a
                // MutableLiveData privado y simular el usuario que inició la app tiene permisos de admin
                val field = MainViewModel::class.java.getDeclaredField("_isAdmin")
                field.isAccessible = true
                val mutableIsAdmin = field.get(mainViewModel) as MutableLiveData<Boolean>
                mutableIsAdmin.postValue(true)

                val navHostFragment = activity.supportFragmentManager
                    .findFragmentById(R.id.nav_host_fragment_main) as NavHostFragment
                navHostFragment.navController.navigate(R.id.feedDetailsFragment, Bundle().apply { putString("id", "mocked-admin") })
            }

            // Comprobamos que el contenedor de opciones de administrador está visible
            // y que aparecen los botones de editar y eliminar referencia
            onView(withId(R.id.admin_options_container))
                .check(matches(isDisplayed()))
            onView(withId(R.id.edit_button))
                .check(matches(isDisplayed()))
            onView(withId(R.id.delete_button))
                .check(matches(isDisplayed()))
        }
    }

    @Test
    fun feedDetailsFragment_hidesAdminOptions_forRegularUsers() {

        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val viewModel = ViewModelProvider(activity)[FeedViewModel::class.java]
                val mainViewModel = ViewModelProvider(activity)[MainViewModel::class.java]

                (viewModel.references as MutableLiveData).value = listOf(Reference(id = "mock-regular"))

                // Forzamos el rol no-admin de la misma manera
                val field = MainViewModel::class.java.getDeclaredField("_isAdmin")
                field.isAccessible = true
                val mutableIsAdmin = field.get(mainViewModel) as MutableLiveData<Boolean>
                mutableIsAdmin.postValue(false)

                val navHostFragment = activity.supportFragmentManager
                    .findFragmentById(R.id.nav_host_fragment_main) as NavHostFragment
                navHostFragment.navController.navigate(R.id.feedDetailsFragment, Bundle().apply { putString("id", "mock-regular") })
            }

            // Comprobamos que el contenedor de opciones de administrador desaparece de la view
            onView(withId(R.id.admin_options_container))
                .check(matches(withEffectiveVisibility(Visibility.GONE)))
        }
    }

    @Test
    fun feedDetailsFragment_showsCorrectFavoritesCount() {
        val expectedFavorites = 42
        val mockId = "fav-test"

        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val viewModel = ViewModelProvider(activity)[FeedViewModel::class.java]

                //Simulamos una referencia con un contador de "likes"
                (viewModel.references as MutableLiveData).value = listOf(Reference(
                    id = mockId,
                    favorites = expectedFavorites
                ))

                val navHostFragment = activity.supportFragmentManager
                    .findFragmentById(R.id.nav_host_fragment_main) as NavHostFragment
                navHostFragment.navController.navigate(R.id.feedDetailsFragment, Bundle().apply { putString("id", mockId) })
            }

            //Se muestra el botón de favoritos
            onView(withId(R.id.favorite_button))
                .check(matches(isDisplayed()))

            // Validamos que el TextView para los "likes" contiene explícitamente el contador inyectado
            onView(withId(R.id.feed_details_num))
                .check(matches(withText(expectedFavorites.toString())))
        }
    }
}
