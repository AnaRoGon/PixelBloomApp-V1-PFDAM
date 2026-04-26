package dam.pfdam.pixelbloom.view.fragment.feed

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dam.pfdam.pixelbloom.R
import dam.pfdam.pixelbloom.utils.loadImage
import dam.pfdam.pixelbloom.databinding.FragmentFeedDetailsBinding
import dam.pfdam.pixelbloom.utils.ImageUtils
import dam.pfdam.pixelbloom.viewmodel.MainViewModel
import dam.pfdam.pixelbloom.viewmodel.feed.FeedViewModel
import dam.pfdam.pixelbloom.viewmodel.boards.BoardsViewModel
import dam.pfdam.pixelbloom.view.fragment.boards.BoardSelectorFragment
import kotlin.getValue

/**
 * Fragmento encargado de mostrar los detalles de una referencia.
 *
 * Presenta información detallada como título, descripción, imagen y contador de favoritos.
 * Permite a los usuarios marcar como favorita una referencia y a los administradores
 * editar o eliminar referencias existentes.
 *
 * @property _binding Referencia privada al binding del layout.
 * @property binding Propiedad de acceso seguro al binding (non-null).
 * @property viewModel ViewModel que gestiona la lógica de las referencias compartido en el feed.
 * @property mainViewModel ViewModel para verificar permisos de administrador.
 * @property referenceId Identificador de la referencia actual, obtenido de los argumentos de navegación.
 */
class FeedDetailsFragment : Fragment() {
    private var _binding: FragmentFeedDetailsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FeedViewModel by activityViewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private val boardsViewModel: BoardsViewModel by activityViewModels()
    private var referenceId: String? = null

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
        _binding = FragmentFeedDetailsBinding.inflate(inflater, container, false)
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
        referenceId = arguments?.getString("id")

        // Configuramos la observación de los LiveData
        setupViewModelObservers()
        //Se observa la referencia que nos llega
        observeReference()
        //Se implementan los listeners
        setupListeners()

    }

    /**
     * Configura los listeners de los botones y componentes de la interfaz.
     */
    private fun setupListeners() {
        // Se implementa el click listener del CheckBox que actualiza el estado de favorito con el ViewModel
        binding.favoriteButton.setOnClickListener {
            val isChecked = binding.favoriteButton.isChecked
            Log.d("DEBUG_FAV", "referenceId = $referenceId, isChecked = $isChecked")
            referenceId?.let {
                viewModel.updateFavoriteStatus(it, isChecked)
            }
        }

        // Listener para el botón de guardar en tablero
        binding.feedDetailsSaveButton.setOnClickListener {
            referenceId?.let { id ->
                val dialog = BoardSelectorFragment()
                val bundle = Bundle().apply {
                    putString("referenceId", id)
                }
                dialog.arguments = bundle
                dialog.show(parentFragmentManager, "BoardSelectorFragment")
            }
        }
    }

    /**
     * Configura la observación de LiveDatas para permisos de admin y mensajes del sistema.
     */
    private fun setupViewModelObservers() {
        //Controlamos los componentes que se mostrarán sólo si el usuario es admin.
        mainViewModel.isAdmin.observe(viewLifecycleOwner) { isAdmin ->
            if (isAdmin) {
                binding.adminOptionsContainer.visibility = View.VISIBLE
                binding.feedDetailsSaveButton.isEnabled = false
                //Se implementa el listener para el botón de delete
                binding.deleteButton.setOnClickListener {
                    // Creamos el diálogo de confirmación
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.delete_reference_title)
                        .setMessage(
                            getString(
                                R.string.delete_reference_message,
                                binding.feedDetailsTitle.text
                            )
                        )
                        .setPositiveButton(R.string.delete) { _, _ ->
                            // Si confirma, ejecutamos la lógica de borrado
                            deleteReference()
                        }
                        .setNegativeButton(R.string.cancel, null)
                        .show()
                }
                binding.editButton.setOnClickListener {
                    val currentRef = viewModel.references.value?.find { it.id == referenceId }
                    //Obtenemos la referencia a editar para poder obtener la información
                    viewModel.selectReferenceForEdit(currentRef)
                    val dialog = NewReferenceFragment()
                    dialog.show(parentFragmentManager, "NewReferenceFragment")
                }
            } else {
                binding.adminOptionsContainer.visibility = View.GONE
            }
        }

        // Observamos los mensajes de éxito
        viewModel.successMsg.observe(viewLifecycleOwner) { resId ->
            resId?.let {
                Toast.makeText(requireContext(), getString(it), Toast.LENGTH_SHORT).show()
                viewModel.clearSuccess()
            }
        }

        // Observamos los posibles errores
        viewModel.errorMsg.observe(viewLifecycleOwner) { resId ->
            resId?.let {
                Toast.makeText(requireContext(), getString(it), Toast.LENGTH_LONG).show()
                viewModel.clearErrors()
            }
        }

        // Observamos los mensajes del boardsViewModel también
        boardsViewModel.successMsg.observe(viewLifecycleOwner) { resId ->
            resId?.let {
                Toast.makeText(requireContext(), getString(it), Toast.LENGTH_SHORT).show()
                boardsViewModel.clearSuccess()
            }
        }

        boardsViewModel.errorMsg.observe(viewLifecycleOwner) { resId ->
            resId?.let {
                Toast.makeText(requireContext(), getString(it), Toast.LENGTH_LONG).show()
                boardsViewModel.clearErrors()
            }
        }
    }

    /**
     * Observa los cambios en la lista de referencias del [FeedViewModel].

     * Cuando la lista se actualiza, busca la referencia correspondiente al `referenceId` de este
     * fragmento. Si la encuentra, actualiza la interfaz de usuario (título, descripción, número de
     * favoritos e imagen) con los datos de esa referencia.
     *
     * Carga la imagen del usuario utilizando la clase [ImageUtils] que nos permite simplificar
     * la lógica de carga de imagenes con la librería de Picasso.
     *
     * También establece el estado del botón
     * de favoritos basándose en si la referencia llega marcada como favorita del ViewModel.
     */
    private fun observeReference() {
        viewModel.references.observe(viewLifecycleOwner) { references ->
            val reference = references.find { it.id == referenceId }

            if (reference != null) {
                binding.feedDetailsTitle.text = reference.title
                binding.feedDetailsAuthor.text = reference.author
                // Actualizamos el título de la toolbar
                (activity as? AppCompatActivity)?.supportActionBar?.title = reference.title

                binding.feedDetailsDescription.text = reference.description
                binding.feedDetailsNum.text = reference.favorites.toString()

                // Actualizamos el estado del CheckBox según si está en favoritos
                binding.favoriteButton.isChecked = viewModel.isReferenceFavorited(reference.id)
                // Carga de la imagen utilizando Picasso
                // tomamos la información desde la url
                binding.feedDetailsImage.loadImage(reference.imagePath)

            } else {
                //Si no hay referencia con ese id, volvemos atrás
                //Asi al eliminar una referencia se sale de la pantalla
                try {
                    findNavController().popBackStack()
                } catch (e: Exception) {
                    Log.e("FeedDetails", "Error al navegar hacia atrás: ${e.message}")
                }
            }
        }
    }

    /**
     * Solicita al ViewModel la eliminación de la referencia actual.
     */
    private fun deleteReference() {
        val currentRef = viewModel.references.value?.find { it.id == referenceId }
        referenceId?.let { id ->
            // Llamamos al ViewModel con ID y URL de la imagen
            viewModel.deleteReference(id, currentRef?.imagePath)
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
