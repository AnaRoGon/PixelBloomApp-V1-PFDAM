package dam.pfdam.pixelbloom.view.activity


import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import dam.pfdam.pixelbloom.R
import dam.pfdam.pixelbloom.data.ui.OnBoarding
import dam.pfdam.pixelbloom.databinding.ActivityOnboardingBinding
import dam.pfdam.pixelbloom.ui.activity.AuthActivity
import dam.pfdam.pixelbloom.view.fragment.onBoarding.adapter.OnBoardingAdapter
import dam.pfdam.pixelbloom.utils.PreferenceHelper

/**
 * Actividad encargada de gestionar e iniciar la primera toma de contacto del usuario con la app.
 *
 * Muestra el SplashScreen propio del sistema y el onBoarding de la app.
 * Controla mediante SharedPreferences si el usuario ya ha completado este proceso para
 * redirigirlo directamente a la [AuthActivity] y no volver a mostrar el onBoarding en otras ejecuciones.
 *
 * @property binding Objeto de binding para acceder a los componentes de la vista.
 * @property _binding Referencia privada al binding.
 * @property onBoardingList Lista con los datos de las pantallas a mostrar para el flujo de OnBoarding.
 * @property adapter Adaptador que gestiona la visualización de la lista de pantallas.
 */
class OnboardingActivity : AppCompatActivity() {

    private var _binding: ActivityOnboardingBinding? = null
    private val binding get() = _binding!!

    private lateinit var onBoardingList: List<OnBoarding>

    private lateinit var adapter: OnBoardingAdapter


    /**
     * Función llamada al crear la actividad.
     * Configura el diseño Edge-to-Edge y carga el [OnBoardingAdapter].
     * También evalúa si se debe omitir el onboarding para llevar al usuario a la siguiente pantalla.
     *
     * @param savedInstanceState Si no es null, contiene el estado previamente guardado de la actividad.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen() //Necesario para que se aplique el splash personalizado
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.onboarding_activity)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Aplicamos las preferencias almacenadas
        PreferenceHelper(this).applyPreferences()

        //Verificamos que el usuario no haya visualizado ya el onboarding
        if (PreferenceHelper(this).getOnBoardingState()) {
            goToAuthActivity()
            return
        }
        //Si no se ha visualizado el onboarding se cargan las pantallas utilizando la clase OnBoarding
        onBoardingList = listOf(
            OnBoarding(
                image = R.drawable.on_boarding_a,
                title = getString(R.string.onboarding_title_1),
                description = getString(R.string.onboarding_desc_1)

            ),
            OnBoarding(
                image = R.drawable.on_boarding_b,
                title = getString(R.string.onboarding_title_2),
                description = getString(R.string.onboarding_desc_2)
            ),
            OnBoarding(
                image = R.drawable.on_boarding_c,
                title = getString(R.string.onboarding_title_3),
                description = getString(R.string.onboarding_desc_3)
            )
        )
        //Se vincula el adaptador con la lista de pantallas
        adapter = OnBoardingAdapter(onBoardingList) {
            //Al pulsar el botón se marca como completado el onboarding y se va la pantalla de autenticación
            PreferenceHelper(this).setOnboardingState(true)
            goToAuthActivity()
        }
        binding.viewPagerOnboarding.adapter = adapter

        // Logica para actualizar los puntos de navegación al pasar de página
        binding.viewPagerOnboarding.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == onBoardingList.lastIndex) {
                    binding.dotsContainer.visibility = android.view.View.GONE
                } else {
                    //Controlamos el efecto de los puntos de navegación según la página en la que nos encontramos
                    binding.dotsContainer.visibility = android.view.View.VISIBLE
                    binding.dot1.setBackgroundResource(if (position == 0) R.drawable.indicator_dot_selected else R.drawable.indicator_dot_unselected)
                    binding.dot2.setBackgroundResource(if (position == 1) R.drawable.indicator_dot_selected else R.drawable.indicator_dot_unselected)
                    binding.dot3.setBackgroundResource(if (position == 2) R.drawable.indicator_dot_selected else R.drawable.indicator_dot_unselected)
                }
            }
        })
    }

    /**
     * Navega a la actividad de autenticación ([AuthActivity]) de la aplicación.
     * Finaliza la actividad actual de Onboarding para que el usuario no pueda volver al proceso
     * una vez lo ha completado al presionar la flecha de retroceso del sistema.
     */
    private fun goToAuthActivity() {
        startActivity(Intent(this, AuthActivity::class.java))
        finish() // Cerramos la activity de onboarding para que no se pueda volver atrás
    }
}