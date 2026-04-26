package dam.pfdam.pixelbloom.view.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dam.pfdam.pixelbloom.R
import dam.pfdam.pixelbloom.databinding.FragmentSettingsBinding
import dam.pfdam.pixelbloom.ui.activity.AuthActivity
import dam.pfdam.pixelbloom.viewmodel.AuthViewModel
import dam.pfdam.pixelbloom.utils.PreferenceHelper
import kotlin.getValue

/**
 *  Fragmento encargado de mostrar y gestionar los ajustes de la aplicación.
 *  Permite al usuario configurar preferencias personales y de la cuenta.
 * *
 * @property _binding Referencia privada al binding del layout.
 * @property binding Propiedad de acceso seguro al binding (non-null).
 * @property viewModel Instancia del ViewModel encargada de la lógica de sesión.
 */
class SettingsFragment : Fragment(R.layout.fragment_settings) {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by activityViewModels()

    /**
     * Infla el layout del fragmento y configura el binding.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Se infla el layout
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Se llama después de que la vista haya sido creada. Configura los listeners
     * para la navegación de la toolbar y el cierre del fragmento.
     *
     * @param view La vista creada.
     * @param savedInstanceState Si no es nulo, el fragmento se está reconstruyendo.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Se inicializan los recyclerView
        setupRecyclerView()

        // Configurar el estado del switch de modo segun el modo en el que está
        val currentTheme = PreferenceHelper(requireContext()).getTheme()
        binding.switchAppMode.isChecked = currentTheme == "dark"
    }

    /**
     * Configura los RecyclerViews con sus adaptadores personalizados y LayoutManagers.
     */
    private fun setupRecyclerView() {
        // Listener para cerrar el fragmento desde la toolbar
        binding.settingsToolbar.setNavigationOnClickListener {
            closeSettings()
        }
        // y al pulsar sobre el background oscuro se sale del menú
        binding.backgroundOverlay.setOnClickListener {
            closeSettings()
        }

        // Listener para el botón de cambio de idioma
        binding.languageButton.setOnClickListener {
            showLanguageBottomSheet()
        }

        // Listener para el switch de modo claro/oscuro
        binding.switchAppMode.setOnCheckedChangeListener { _, isChecked ->
            toggleTheme(isChecked)
        }
        // Listener para el botón de log out
        binding.logOutButton.setOnClickListener {
            try {
                //Cerramos sesión y volvemos a la pantalla de login
                viewModel.logOut()
                goToLogin()
            } catch (e: Exception) {
                Log.e("LOGOUT", "Error al cerrar sesión: ${e.message}")
            }
        }

        // Listener para el botón de about de la app
        binding.aboutButton.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.about)
                .setMessage(getString(R.string.about_message))
                .setPositiveButton(R.string.ok, null)
                .show()
        }
    }

    /**
     * Redirige al usuario a la pantalla de autenticación.
     * Limpia la pila de actividades para evitar que el usuario regrese
     * a la pantalla de ajustes tras cerrar sesión.
     */
    private fun goToLogin() {
        val intent = Intent(requireContext(), AuthActivity::class.java)
        //Limpiamos todos los datos que haya cargamos en la pila e indicamos que es una nueva tarea
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    /**
     * Muestra el selector de idioma utilizando un selector de idioma de tipo BottomSheetDialogFragment personalizado.
     */
    private fun showLanguageBottomSheet() {
        val languageSelector = LanguageSelectorFragment()
        languageSelector.show(parentFragmentManager, "languageSelector")
    }

    /**
     * Guarda la preferencia del tema utilizando PreferenceHelper y aplica el cambio visual.
     *
     * @param isDark Indica si se debe aplicar el modo oscuro (true) o el modo claro (false).
     */
    private fun toggleTheme(isDark: Boolean) {
        val newTheme = if (isDark) "dark" else "light"
        PreferenceHelper(requireContext()).changeTheme(newTheme)
    }

    /**
     * Cierra el fragmento de ajustes eliminándolo del gestor de fragmentos
     * y ocultando su contenedor en la actividad principal.
     */
    private fun closeSettings() {
        // Quitamos el fragmento de la vista de forma inmediata
        parentFragmentManager.beginTransaction()
            .remove(this)
            .commit()

        // Ocultamos el contenedor en la MainActivity para que
        // deje de bloquear los clics a las pantallas de abajo.
        val container = requireActivity().findViewById<View>(R.id.settings_container)
        container?.visibility = View.GONE
    }

    /**
     * Libera el binding al destruir la vista para evitar fugas de memoria.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


