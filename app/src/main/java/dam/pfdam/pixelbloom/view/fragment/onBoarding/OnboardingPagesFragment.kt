package dam.pfdam.pixelbloom.view.fragment.onBoarding

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dam.pfdam.pixelbloom.databinding.FragmentOnboardingPagesBinding
import dam.pfdam.pixelbloom.ui.activity.AuthActivity
import dam.pfdam.pixelbloom.utils.PreferenceHelper

/**
 * Fragmento encargado de contener y mostrar las páginas individuales de la guía interactiva (OnBoarding).
 *
 * Cada instancia de este fragmento representa una pantalla informativa con su título, descripción e imagen.
 * En la última pantalla, muestra el botón para finalizar la guía y entrar a la aplicación.
 *  @property _binding Referencia privada al binding del layout.
 *  @property binding Propiedad de acceso seguro al binding (non-null).
 */
class OnboardingPagesFragment : Fragment() {
    private var _binding: FragmentOnboardingPagesBinding? = null
    private val binding get() = _binding!!

    /**
     * Infla el layout del fragmento y configura el binding inicial.
     *
     * @param inflater El objeto LayoutInflater que se puede usar para inflar cualquier vista en el fragmento.
     * @param container Si no es nulo, es la vista a la que la UI del fragmento se adjuntará.
     * @param savedInstanceState Si no es nulo, este fragmento se está reconstruyendo a partir de un estado guardado.
     * @return Retorna la View de la jerarquía del fragmento.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Se infla el layout
        _binding = FragmentOnboardingPagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Configura la navegación a la pantalla de inicio de la app y el estado del sharedPreferences.
     * Además, define el listener para el floating button.
     *
     * @param view La vista devuelta por [onCreateView].
     * @param savedInstanceState Estado guardado del fragmento.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.letsGoButton.setOnClickListener {
            // Pasamos el valor del sharedPreferences para el onBoarding a true
            PreferenceHelper(requireContext()).setOnboardingState(true)

            // Se navega al activity
            val intent = Intent(requireContext(), AuthActivity::class.java)
            startActivity(intent)

            // Se cierra la actividad actual para que se quite de la pila y no se pueda volver atrás.
            activity?.finish()
        }
    }
}