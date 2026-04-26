package dam.pfdam.pixelbloom.ui.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import dam.pfdam.pixelbloom.R
import dam.pfdam.pixelbloom.databinding.ActivityMainBinding
import dam.pfdam.pixelbloom.view.fragment.SettingsFragment
import dam.pfdam.pixelbloom.viewmodel.MainViewModel
import dam.pfdam.pixelbloom.utils.PreferenceHelper
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence

/** Actividad principal de la aplicación.
 *
 * Gestiona la navegación principal mediante BottomNavigationView y Navigation Component,
 * encargándose de mostrar la interfaz una vez que el usuario ha iniciado sesión.
 * Coordina la visualización de la Toolbar y el acceso al menú de ajustes.
 *
 * @property viewModel ViewModel encargado de la lógica a nivel de actividad.
 * @property _binding Referencia privada al binding.
 * @property binding Objeto de binding para acceder a los componentes de la vista.
 * @property appBarConfiguration Configuración para el comportamiento de la barra superior.
 * @property navController Controlador de navegación para el flujo entre fragmentos.
 */
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController

    /**
     * Función llamada al crear la actividad.
     * Inicializa la actividad, configura el diseño y gestiona los márgenes para el modo edge-to-edge.
     * Configura la Toolbar, el BottomNavigationView y verifica el estado del usuario.
     *
     * @param savedInstanceState Si no es null, contiene el estado previamente guardado de la actividad.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)

            // Al aplicar el padding completo en el contenedor root (R.id.main),
            // este contenedor ya se encarga de apartar el contenido de las barras del sistema.
            // Por tanto, CONSUMIMOS el inset para evitar que se propague a los hijos
            // (como el BottomNavigationView) y que estos vuelvan a aplicar el padding por duplicado
            // en las recreaciones de actividad (que es el problema que tenemos al cargar el modo oscuro).
            WindowInsetsCompat.CONSUMED
        }
        // Obtenemos y aplicamos las preferencias almacenadas
        PreferenceHelper(this).applyPreferences()

        // Configuramos la toolbar para que se muestre en la actividad
        setSupportActionBar(binding.mainToolbar)
        // Ocultamos el título por defecto de la ActionBar para usar nuestro diseño personalizado
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Configuramos el controlador de navegación para la actividad
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_main) as NavHostFragment
        navController = navHostFragment.navController
        // Configuramos la navegación en el BottomNavigationView
        setupNavigation()
        // Escuchamos los cambios en la pila de navegación para personalizar la toolbar y el menú de ajustes
        setupDestinationChangeListener()
        //Se observan los cambios en el rol del usuario
        setupObservers()
        // Configuramos el listener para el botón de menu
        setupListeners()

        // Verificamos o creamos el perfil del usuario en Firestore usando el viewModel
        viewModel.checkAndCreateUser(getString(R.string.default_user_name))
    }


    /**
     * Configura el BottomNavigationView y define los destinos de nivel superior
     * para la gestión automática de la flecha de retroceso.
     */
    private fun setupNavigation() {
        binding.bottomNavigation.setupWithNavController(navController)

        // Se definen las pantallas principales que no muestran la flecha atrás
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.feedFragment,
                R.id.boardsFragment,
                R.id.challengesFragment
            )
        )
        // Vinculamos la toolbar con el navcontroller
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    /**
     * Inicializa los listeners de clic para los componentes de la actividad,
     * como el botón para abrir el menú de ajustes.
     */
    private fun setupListeners() {
        binding.btnMainMenu.setOnClickListener {
            // Hacemos visible el contenedor de ajustes
            binding.settingsContainer.visibility = View.VISIBLE
            // Cargamos el fragmento de ajustes con una animación personalizada
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_right,
                    R.anim.slide_in_right,
                    R.anim.slide_out_right
                )
                .add(R.id.settings_container, SettingsFragment())
                .addToBackStack(null) // Permite cerrar el menú con el botón atrás del sistema
                .commit()
        }
    }

    /**
     * Configura el listener para los cambios de destino en la navegación.
     * Personaliza el estado de la Toolbar, la visibilidad del logo y la selección
     * del BottomNavigationView en función de la pantalla actual.
     */
    private fun setupDestinationChangeListener() {

        navController.addOnDestinationChangedListener { _, destination, _ ->
            // Toolbar y logo: comportamiento común a todos los fragmentos de detalle
            when (destination.id) {
                R.id.feedDetailsFragment,
                R.id.acceptChallengeFragment,
                R.id.boardDetailsImageFragment,
                R.id.boardListFragment,
                R.id.challengesDetailsFragment -> {
                    supportActionBar?.setDisplayHomeAsUpEnabled(true)
                    supportActionBar?.setDisplayShowTitleEnabled(true)
                    binding.toolbarLogoContainer.visibility = View.GONE
                }

                else -> {
                    supportActionBar?.setDisplayHomeAsUpEnabled(false)
                    supportActionBar?.setDisplayShowTitleEnabled(false)
                    binding.toolbarLogoContainer.visibility = View.VISIBLE
                }
            }

            // BOttom Menu: marcamos el item según el fragmento actual de detalles en el que nos encontramos
            when (destination.id) {
                R.id.feedDetailsFragment ->
                    binding.bottomNavigation.menu.findItem(R.id.feedFragment).isChecked = true

                R.id.acceptChallengeFragment ->
                    binding.bottomNavigation.menu.findItem(R.id.challengesFragment).isChecked = true

                R.id.boardDetailsImageFragment,
                R.id.boardListFragment ->
                    binding.bottomNavigation.menu.findItem(R.id.boardsFragment).isChecked = true
                // Caso especial: puede venir de Boards o de Challenges
                R.id.challengesDetailsFragment -> {
                    val previousDestinationId =
                        navController.previousBackStackEntry?.destination?.id
                    if (previousDestinationId == R.id.boardsFragment) {
                        binding.bottomNavigation.menu.findItem(R.id.boardsFragment).isChecked = true
                    } else if (previousDestinationId == R.id.challengesFragment) {
                        binding.bottomNavigation.menu.findItem(R.id.challengesFragment).isChecked =
                            true
                    }
                }
            }
        }
    }

    /**
     * Configura la observación del LiveData del ViewModel de autenticación.
     * Escucha cambios en el rol del usuario y muestra la guía interactiva.
     */
    private fun setupObservers() {
        // Observamos el rol del usuario para adaptar los textos de la guía interactiva
        viewModel.isAdmin.observe(this) { isAdmin ->
            if (isAdmin != null) {
                // Usamos post para asegurar que la vista está completamente inflada y renderizada antes de mostrar la guía
                binding.root.post {
                    showInteractiveGuide(isAdmin)
                }
            }
        }

        // Observamos los mensajes de error
        viewModel.errorMsg.observe(this) { resId ->
            resId?.let {
                Toast.makeText(this, getString(it), Toast.LENGTH_LONG).show()
                viewModel.clearErrors()
            }
        }

        // Observamos los mensajes de éxito
        viewModel.successMsg.observe(this) { resId ->
            resId?.let {
                Toast.makeText(this, getString(it), Toast.LENGTH_SHORT).show()
                viewModel.clearSuccess()
            }
        }
    }

    /**
     * Gestiona la navegación facilitando el retroceso entre fragmentos.
     *
     * @return Boolean indicando si la navegación fue gestionada por el NavController.
     */
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    /**
     * Muestra una guía interactiva la primera vez que el usuario ingresa a la app.
     *
     * Dependiendo de si es un usuario administrador o estándar, se muestran descripciones
     * personalizadas de las pantallas principales de la app y el menú de ajustes.
     * Finaliza marcando el estado en [PreferenceHelper] como completado para evitar repetirse.
     *
     * @param isAdmin Indica si el usuario actual tiene permisos de administrador.
     */
    private fun showInteractiveGuide(isAdmin: Boolean) {
        // Verificamos que no se haya mostrado antes la guía interactiva
        if (!PreferenceHelper(this).getInteractiveGuideState()) {
            val feedDesc =
                if (isAdmin) getString(R.string.guide_feed_desc_admin)
                else getString(R.string.guide_feed_desc_user)

            val boardsDesc =
                if (isAdmin) getString(R.string.guide_boards_desc_admin)
                else getString(R.string.guide_boards_desc_user)

            val challengesDesc =
                if (isAdmin) getString(R.string.guide_challenges_desc_admin)
                else getString(R.string.guide_challenges_desc_user)

            TapTargetSequence(this)
                .targets(
                    TapTarget.forView(
                        binding.bottomNavigation.findViewById(R.id.feedFragment),
                        getString(R.string.feed),
                        feedDesc
                    )
                        .id(1)
                        .outerCircleColor(R.color.purple)
                        .targetCircleColor(R.color.white)
                        .descriptionTextColor(R.color.neutral_50)
                        .tintTarget(false)
                        .cancelable(false),
                    TapTarget.forView(
                        binding.bottomNavigation.findViewById(R.id.boardsFragment),
                        getString(R.string.boards),
                        boardsDesc
                    )
                        .id(2)
                        .outerCircleColor(R.color.purple)
                        .targetCircleColor(R.color.white)
                        .descriptionTextColor(R.color.neutral_50)
                        .tintTarget(false)
                        .cancelable(false),
                    TapTarget.forView(
                        binding.bottomNavigation.findViewById(R.id.challengesFragment),
                        getString(R.string.challenges),
                        challengesDesc
                    )
                        .id(3)
                        .outerCircleColor(R.color.purple)
                        .targetCircleColor(R.color.white)
                        .descriptionTextColor(R.color.neutral_50)
                        .tintTarget(false)
                        .cancelable(false),
                    TapTarget.forView(
                        binding.btnMainMenu,
                        getString(R.string.settings),
                        getString(R.string.guide_settings_desc)
                    )
                        .id(4)
                        .outerCircleColor(R.color.purple)
                        .targetCircleColor(R.color.white)
                        .descriptionTextColor(R.color.neutral_50)
                        .tintTarget(false)
                        .cancelable(false)
                )
                .listener(object : TapTargetSequence.Listener {
                    override fun onSequenceFinish() {
                        // Se marca como hecha para que no vuelva a salir
                        PreferenceHelper(this@MainActivity).setInteractiveGuideState(true)
                        // Devolvemos al usuario a la pantalla principal al finalizar
                        binding.bottomNavigation.selectedItemId = R.id.feedFragment
                    }

                    override fun onSequenceStep(lastTarget: TapTarget?, targetClicked: Boolean) {
                        // Cambiamos el fragmento en función del paso que acabamos de pasar
                        when (lastTarget?.id()) {
                            1 -> binding.bottomNavigation.selectedItemId = R.id.boardsFragment
                            2 -> binding.bottomNavigation.selectedItemId = R.id.challengesFragment
                        }
                    }

                    override fun onSequenceCanceled(lastTarget: TapTarget?) {
                        // Si se cancela la guía, también la damos por vista
                        // Actualmente no hay implementado ningún botón específico para cancelar
                        PreferenceHelper(this@MainActivity).setInteractiveGuideState(true)
                        binding.bottomNavigation.selectedItemId = R.id.feedFragment
                    }
                })
                .start()
        }
    }

}