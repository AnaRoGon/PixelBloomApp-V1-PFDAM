package dam.pfdam.pixelbloom.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dam.pfdam.pixelbloom.R
import dam.pfdam.pixelbloom.databinding.FragmentTransitionBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


/**
 * Fragmento de transición que muestra una animación tras aceptar un reto.
 *
 * Utiliza corrutinas para mantener la pantalla visible durante un breve periodo de tiempo
 * antes de navegar automáticamente a los detalles del reto aceptado.
 *
 * @property _binding Referencia privada al binding del layout.
 * @property binding Propiedad de acceso seguro al binding (non-null).
 */
class TransitionFragment : Fragment() {

    private var _binding: FragmentTransitionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Se infla el layout
        _binding = FragmentTransitionBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Configura la lógica de transición una vez que la vista ha sido creada.
     * Inicia una corrutina que espera 3 segundos antes de redirigir al usuario
     * al fragmento de detalles del reto, pasando el ID del reto recibido por argumentos.
     *
     * @param view La vista creada.
     * @param savedInstanceState Estado de la instancia guardado previamente, si existe.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtenemos el ID que nos llega del fragmento anterior
        val challengeId = arguments?.getString("challengeId")

        //Establecemos el tiempo de duración hasta navegar a la pantalla de detalles del reto
        viewLifecycleOwner.lifecycleScope.launch {
            //Esperamos 3 segundos
            delay(3000)

            // Navegamos al detalle del reto pasando el ID como argumento
            val bundle = Bundle().apply {
                putString("challengeId", challengeId)
            }
            findNavController().navigate(
                R.id.action_transitionFragment_to_challengesDetailsFragment,
                bundle
            )
        }
    }

}