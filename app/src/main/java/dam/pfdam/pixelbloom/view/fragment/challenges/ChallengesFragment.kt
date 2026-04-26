package dam.pfdam.pixelbloom.view.fragment.challenges

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dam.pfdam.pixelbloom.R
import dam.pfdam.pixelbloom.databinding.FragmentChallengesBinding
import dam.pfdam.pixelbloom.view.fragment.challenges.adapter.ChallengesAdapter
import dam.pfdam.pixelbloom.viewmodel.MainViewModel
import dam.pfdam.pixelbloom.viewmodel.challenges.ChallengesViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Fragmento encargado de mostrar la lista de retos disponibles.
 *
 * Implementa el patrón MVVM utilizando [ChallengesViewModel] para gestionar los datos
 * y [MainViewModel] para la validación de roles del usuario.
 * Utiliza un RecyclerView con un [LinearLayoutManager] para presentar los retos.
 *
 * @property _binding Referencia privada al binding del layout.
 * @property binding Propiedad de acceso seguro al binding (non-null).
 * @property adapter Adaptador personalizado para el RecyclerView con la lista de retos.
 * @property viewModel ViewModel para gestionar y proveer los retos globales.
 * @property mainViewModel ViewModel maestro para verificar permisos de rol.
 */
class ChallengesFragment : Fragment() {
    private var _binding: FragmentChallengesBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ChallengesAdapter
    private val viewModel: ChallengesViewModel by activityViewModels()
    private val mainViewModel: MainViewModel by activityViewModels()

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
    ): View {
        //Se infla el layout
        _binding = FragmentChallengesBinding.inflate(inflater, container, false)
        return binding.root
    }


    /**
     * Configura los componentes de la interfaz, observadores y carga inicial de datos.
     *
     * @param view La vista devuelta por [onCreateView].
     * @param savedInstanceState Estado guardado del fragmento.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Se actualiza la view con la fecha real
        updateDate()

        // Se inicializa el recycler view
        setupRecyclerView()

        // Configuramos la observación de los LiveData
        setupViewModelObservers()
    }

    /**
     * Libera la referencia al binding para evitar fugas de memoria al destruir la vista.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    /**
     * Configura el RecyclerView con el adaptador personalizado y el LayoutManager.
     * Define el comportamiento del click para navegar a los detalles.
     */
    private fun setupRecyclerView() {
        //RecyclerView de tipo Linear
        binding.recyclerViewChallenges.layoutManager = LinearLayoutManager(requireContext())

        //Se crea el adaptador y se le pasa la lista de challenges y el click listener
        adapter = ChallengesAdapter(emptyList()) { selectedItem ->
            val isAdmin = mainViewModel.isAdmin.value == true

            if (isAdmin) {
                // Si es admin, preparamos el ViewModel para edición y mostramos el BottomSheet en modo editar reto
                viewModel.selectChallengeForEdit(selectedItem)
                val editSheet = NewChallengeFragment()
                editSheet.show(parentFragmentManager, "editSheet")
            } else {
                //Si el usuario es estándar, se navega a la pantalla de detalle
                val bundle = Bundle().apply {
                    putString("id", selectedItem.id)
                }
                val navController = findNavController()
                // Por seguridad para que la app no se crashee se hace la comprobación de que estamos en challenges
                if (navController.currentDestination?.id == R.id.challengesFragment) {
                    navController.navigate(
                        R.id.action_challengesFragment_to_acceptChallengeFragment,
                        bundle
                    )
                }
            }
        }
        //Se le vincula el adapter
        binding.recyclerViewChallenges.adapter = adapter
    }

    /**
     * Obtiene la fecha actual del sistema y la formatea para actualizar
     * la vista correspondiente en la interfaz.
     */
    private fun updateDate() {
        // Se obtiene la fecha actual del sistema
        val today = LocalDate.now()
        // Se establece el patrón de formateo recuperandolo de strings.xml
        val pattern = getString(R.string.date_format_pattern)
        val formatter = DateTimeFormatter.ofPattern(pattern, Locale.getDefault())
        // Se formatea y se asigna el valor
        val formattedDate = today.format(formatter)
        binding.challengesDate.text = formattedDate
    }

    /**
     * Configura la observación de los LiveDatas del ViewModel.
     * Permite reaccionar a cambios en los datos y a posibles errores.
     */
    private fun setupViewModelObservers() {
        //Observador para actualizar la lista de retos
        viewModel.challenges.observe(viewLifecycleOwner) { challenges ->
            adapter.updateData(challenges)
        }

        //Se observan si llegan mensajes de error
        viewModel.errorMsg.observe(viewLifecycleOwner) { resId ->
            resId?.let {
                Toast.makeText(requireContext(), getString(it), Toast.LENGTH_LONG).show()
                viewModel.clearErrors()
            }
        }

        // Observamos los mensajes de éxito
        viewModel.successMsg.observe(viewLifecycleOwner) { resId ->
            resId?.let {
                Toast.makeText(requireContext(), getString(it), Toast.LENGTH_SHORT).show()
                viewModel.clearSuccess()
            }
        }

        //Controlamos los componentes que se mostrarán sólo si el usuario es admin.
        mainViewModel.isAdmin.observe(viewLifecycleOwner) { isAdmin ->
            if (isAdmin) {
                binding.addChallengeFloatingButton.show()
                // EL botón flotante se configura para añadir un nuevo reto
                binding.addChallengeFloatingButton.setOnClickListener {
                    // Reseteamos el estado a null para que entre en modo Creación y no modo Edición del reto
                    viewModel.selectChallengeForEdit(null)
                    val addSheet = NewChallengeFragment()
                    addSheet.show(parentFragmentManager, "addSheet")
                }
            } else {
                //PAra los usuarios estándar permanecerá oculto
                binding.addChallengeFloatingButton.hide()
            }
        }
    }

}