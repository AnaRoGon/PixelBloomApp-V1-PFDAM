package dam.pfdam.pixelbloom.view.fragment.challenges

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import dam.pfdam.pixelbloom.databinding.FragmentAcceptChallengeBinding
import androidx.core.graphics.toColorInt
import androidx.fragment.app.activityViewModels
import dam.pfdam.pixelbloom.R
import dam.pfdam.pixelbloom.viewmodel.boards.UserChallengesViewModel
import dam.pfdam.pixelbloom.viewmodel.challenges.ChallengesViewModel
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavOptions


/**
 * Fragmento encargado de mostrar los detalles de un reto antes de aceptarlo.
 *
 * Permite al usuario visualizar la descripción y la paleta de colores asociada al reto.
 *
 * @property _binding Referencia privada al binding del layout.
 * @property binding Propiedad de acceso seguro al binding (non-null).
 * @property challengeId Identificador del reto actual, obtenido de los argumentos de navegación.
 * @property viewModel ViewModel que gestiona la lógica de los retos.
 * @property userChallengesViewModel ViewModel que gestiona la lógica de los retos del usuario.
 */
class AcceptChallengeFragment : Fragment() {

    private var _binding: FragmentAcceptChallengeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChallengesViewModel by activityViewModels() // Global
    private val userChallengesViewModel: UserChallengesViewModel by activityViewModels() // User
    private var challengeId: String? = null

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
        _binding = FragmentAcceptChallengeBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Inicializa los datos, observadores y listeners de la interfaz.
     *
     * @param view La vista devuelta por [onCreateView].
     * @param savedInstanceState Estado guardado del fragmento.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Pasamos el id del challenge a la pantalla de detalles.
        challengeId = arguments?.getString("id")

        //Se configura la observación de los LiveData
        setupViewModelObservers()
        //Se observa el challenge que nos llega
        observeChallenge()
        //Se implementan los listeners
        setupListeners()

    }

    /**
     * Configura los listeners de los botones y componentes de la interfaz.
     */
    private fun setupListeners() {
        //PAra el botón de cancelar
        binding.cancelButton.setOnClickListener {
            //volvemos atrás
            findNavController().popBackStack()
        }
        //Para el botón de confirmar
        binding.okButton.setOnClickListener {
            // Buscamos si el reto ya ha sido aceptado antes
            val alreadyAccepted =
                userChallengesViewModel.userChallenges.value?.any { it.id == challengeId } == true

            if (alreadyAccepted) {
                // Se pasa el id del challenge a la pantalla de detalles
                val bundle = Bundle().apply {
                    putString("challengeId", challengeId)
                }
                //Se establece la opción de que, vayamos al detalle del reto y se borre la pila de navegación
                val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.acceptChallengeFragment, true)
                    .build()
                // Navegamos al detalle del reto del user que ya está aceptado
                findNavController().navigate(
                    R.id.action_acceptChallengeFragment_to_challengesDetailsFragment,
                    bundle,
                    navOptions
                )
                return@setOnClickListener
            }

            // Si no está aceptado, procedemos a aceptarlo
            //Lanzando la animación de aceptar reto
            val bundle = Bundle().apply {
                putString("challengeId", challengeId)
            }
            findNavController().navigate(
                R.id.action_acceptChallengeFragment_to_transitionFragment,
                bundle
            )
            // Buscamos el reto actual y lo aceptamos haciendo uso del viewModel
            viewModel.challenges.value?.find { it.id == challengeId }?.let { challenge ->
                userChallengesViewModel.acceptChallenge(challenge)
            }
        }
    }

    /**
     * Configura la observación del listado de retos para filtrar y mostrar el reto seleccionado.
     * Si el reto no existe (por ejemplo, tras ser borrado), se cierra el fragmento.
     */
    private fun observeChallenge() {
        viewModel.challenges.observe(viewLifecycleOwner) { challenge ->
            val challenge = challenge.find { it.id == challengeId }

            if (challenge != null) {
                //Se pone la descripción del challenge
                binding.challengeDescriptionText.text = challenge.description
                // Actualizamos el título de la toolbar
                (activity as? AppCompatActivity)?.supportActionBar?.title = challenge.title
                //Obtenemos el listado de colores de la paleta
                val palette: List<String> = challenge.palette
                //Actualizamos los colores de la paleta
                palette.let { colors ->
                    val colorViews = listOf(
                        binding.hex01,
                        binding.hex02,
                        binding.hex03,
                        binding.hex04,
                        binding.hex05
                    )

                    //Usamos mutate() para crear una instancia única de este fondo y no afectar al resto de la app
                    colors.zip(colorViews).forEach { (color, view) ->
                        view.background.mutate().setTint(color.toColorInt())
                    }
                }

                // Verificamos si ya está aceptado para reflejarlo en la interfaz (botón OK a Ver Reto)
                val alreadyAccepted =
                    userChallengesViewModel.userChallenges.value?.any { it.id == challengeId } == true
                if (alreadyAccepted) {
                    // Se pone un icono de ojo para que el usuario entienda que al clickar aqui irá a los detalles del reto ya aceptado de su panel
                    binding.okButton.setIconResource(R.drawable.ic_eye)
                } else {
                    //Se pone el icono de confirmar reto
                    binding.okButton.setIconResource(R.drawable.ic_confirm)
                }

            } else {
                //Si no hay challenge con ese id, volvemos atrás
                //Asi al eliminar una challenge se sale de la pantalla
                try {
                    findNavController().popBackStack()
                } catch (e: Exception) {
                    Log.e("FeedDetails", "Error al navegar hacia atrás: ${e.message}")
                }
            }
        }
    }


    /**
     * Configura la observación de LiveDatas para permisos de admin y mensajes del sistema.
     */
    private fun setupViewModelObservers() {

        // Observamos los posibles errores
        viewModel.errorMsg.observe(viewLifecycleOwner) { resId ->
            resId?.let {
                Toast.makeText(requireContext(), getString(it), Toast.LENGTH_LONG).show()
                viewModel.clearErrors()
            }
        }
        userChallengesViewModel.errorMsg.observe(viewLifecycleOwner) { resId ->
            resId?.let {
                Toast.makeText(requireContext(), getString(it), Toast.LENGTH_LONG).show()
                userChallengesViewModel.clearErrors()
            }
        }

        // Observamos los mensajes de éxito
        viewModel.successMsg.observe(viewLifecycleOwner) { resId ->
            resId?.let {
                Toast.makeText(requireContext(), getString(it), Toast.LENGTH_SHORT).show()
                viewModel.clearSuccess()
            }
        }

        userChallengesViewModel.successMsg.observe(viewLifecycleOwner) { resId ->
            resId?.let {
                Toast.makeText(requireContext(), getString(it), Toast.LENGTH_SHORT).show()
                userChallengesViewModel.clearSuccess()
            }
        }
    }

    /**
     * Libera la challenge al binding para evitar fugas de memoria al destruir la vista.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}

