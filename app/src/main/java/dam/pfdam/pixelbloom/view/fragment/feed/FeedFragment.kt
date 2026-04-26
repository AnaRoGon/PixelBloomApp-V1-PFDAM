package dam.pfdam.pixelbloom.view.fragment.feed

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import dam.pfdam.pixelbloom.R
import dam.pfdam.pixelbloom.databinding.FragmentFeedBinding
import dam.pfdam.pixelbloom.view.fragment.feed.adapter.FeedAdapter
import dam.pfdam.pixelbloom.viewmodel.MainViewModel
import dam.pfdam.pixelbloom.viewmodel.feed.FeedViewModel

/**
 * Fragmento de la interfaz de usuario encargado de mostrar el feed de referencias.
 *
 * Implementa el patrón MVVM utilizando [FeedViewModel] para gestionar los datos
 * y [MainViewModel] para la validación de roles de administrador.
 * Utiliza un RecyclerView con un [GridLayoutManager] de dos columnas para presentar las imágenes.
 *
 * @property _binding Referencia privada al binding del layout.
 * @property binding Propiedad de acceso seguro al binding (non-null).
 * @property adapter Adaptador personalizado para el RecyclerView con la lista de referencias.
 * @property viewModel ViewModel que gestiona la lógica y el estado del feed.
 * @property mainViewModel ViewModel principal para gestiones globales como el rol de usuario.
 */
class FeedFragment : Fragment() {
    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: FeedAdapter
    private val viewModel: FeedViewModel by activityViewModels()
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
    ): View? {
        // Se infla el layout
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
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

        // Se inicializa el recycler view
        setupRecyclerView()
        // Configuramos la observación de los LiveData
        setupViewModelObservers()
    }

    /**
     * Configura el RecyclerView con el adaptador personalizado y el LayoutManager.
     * Define el comportamiento del click para navegar a los detalles.
     */
    private fun setupRecyclerView() {
        //El diseño del recycler view es de tipo grid con dos columnas
        binding.recyclerViewFeed.layoutManager = GridLayoutManager(requireContext(), 2)

        //Se crea el adaptador y se le pasa la lista de referencias y el click listener
        adapter = FeedAdapter(emptyList()) { selectedItem ->
            val bundle = Bundle().apply {
                putString("id", selectedItem.id)
            }
            // Navegamos a la pantalla de detalles cuando se selecciona un item
            val navController = findNavController()
            // Por seguridad para que la app no se crashee se hace la comprobación de que estamos en el feed
            if (navController.currentDestination?.id == R.id.feedFragment) {
                navController.navigate(R.id.action_feedFragment_to_feedDetailsFragment, bundle)
            }
        }
        //Se le vincula el adapter
        binding.recyclerViewFeed.adapter = adapter
    }

    /**
     * Configura la observación de los LiveDatas del ViewModel.
     * Permite reaccionar a cambios en los datos y a posibles errores.
     */
    private fun setupViewModelObservers() {
        // Observamos la lista de referencias para mantenerla actualizada
        viewModel.references.observe(viewLifecycleOwner) { references ->
            adapter.updateData(references)
        }

        // Observamos los mensajes de error
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
                //Se muestra el botón de añadir referencia para administradores
                binding.addReferenceFloatingButton.show()
                binding.addReferenceFloatingButton.setOnClickListener {
                    val addSheet = NewReferenceFragment()
                    addSheet.show(parentFragmentManager, "addSheet")
                }
            } else {
                //PAra los usuarios estándar permanecerá oculto
                binding.addReferenceFloatingButton.hide()
            }
        }
    }

    /**
     * Libera la referencia al binding para evitar fugas de memoria al destruir la vista.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
