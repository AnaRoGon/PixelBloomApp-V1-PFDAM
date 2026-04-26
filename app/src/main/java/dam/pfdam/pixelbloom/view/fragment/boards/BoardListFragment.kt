package dam.pfdam.pixelbloom.view.fragment.boards

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import dam.pfdam.pixelbloom.R
import dam.pfdam.pixelbloom.databinding.FragmentBoardListBinding
import dam.pfdam.pixelbloom.view.fragment.feed.adapter.FeedAdapter
import dam.pfdam.pixelbloom.viewmodel.boards.BoardsViewModel
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText

/**
 * Fragmento encargado de mostrar la lista de tableros mediante un RecyclerView.
 * Permite editar el nombre del tablero y eliminar el mismo.
 *
 * @property _binding Referencia privada al binding del layout.
 * @property binding Propiedad de acceso seguro al binding (non-null).
 * @property feedAdapter Adaptador para el RecyclerView que muestra las referencias almacenadas en cada tablero.
 * @property boardId Identificador del tablero actual del que se muestran los detalles.
 * @property boardsViewModel ViewModel específico para gestionar las acciones sobre el tablero actual.
 */
class BoardListFragment : Fragment() {
    private var _binding: FragmentBoardListBinding? = null
    private val binding get() = _binding!!
    private lateinit var feedAdapter: FeedAdapter
    private var boardId: String? = null
    private val boardsViewModel: BoardsViewModel by activityViewModels()

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
        _binding = FragmentBoardListBinding.inflate(inflater, container, false)
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

        // Obtenemos el ID del tablero de los argumentos al inicio
        boardId = arguments?.getString("boardId")

        // Configuramos el RecyclerView
        setupRecyclerView()
        // Configuramos la observación del viewModel
        setupViewModelObservers()

        // Cargamos las referencias del tablero si ya tenemos su ID
        boardId?.let { id ->
            boardsViewModel.fetchBoardReferences(id)

            // Ponemos el título del tablero si ya lo tenemos cargado
            boardsViewModel.boards.value?.find { it.id == id }?.let { board ->
                // Se pone el título del tablero a la toolbar
                (activity as? AppCompatActivity)?.supportActionBar?.title = board.title
            }
        }
        // Configuramos el menú de opciones en la toolbar por defecto
        setupPopUpMenu()
    }


    /**
     * Configura el RecyclerView y su adaptador para visualizar las referencias guardadas
     * en el tablero utilizando una disposición en Grid.
     */
    private fun setupRecyclerView() {
        //Se define el estilo del recycler
        binding.recyclerViewBoardList.layoutManager = GridLayoutManager(requireContext(), 2)
        //Se configura el adaptador
        feedAdapter = FeedAdapter(emptyList()) { reference ->
            val bundle = Bundle().apply {
                putString("referenceId", reference.id)
                putString("boardId", boardId) // Ahora accesible como propiedad de clase
            }
            // Se configura la navegación al detalle de la imagen
            findNavController().navigate(
                R.id.action_boardListFragment_to_boardDetailsImageFragment,
                bundle
            )
        }
        //Se enlaza el recycler con su adaptador
        binding.recyclerViewBoardList.adapter = feedAdapter
    }

    /**
     * Establece los observadores del [BoardsViewModel] para gestionar
     * los estados de carga y la lista de referencias asociadas al tablero actual.
     * Mantiene el titulo del board actualizado en la toolbar.
     */
    private fun setupViewModelObservers() {
        //Se observa si se está cargando la información
        boardsViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                //Mientras se cargan los datos se muestra un indicador de carga.
                binding.loadingProgress.visibility = View.VISIBLE
                binding.recyclerViewBoardList.visibility = View.GONE
                binding.userMessage1.visibility = View.GONE
                binding.userMessage2.visibility = View.GONE
                binding.noBloomsImg.visibility = View.GONE
            } else {
                //Cuando los datos se han cargado se oculta el indicador de carga
                binding.loadingProgress.visibility = View.GONE
                //Se observan los cambios para la sección de boards para mantener la lista actualizada
                boardsViewModel.selectedBoardReferences.observe(viewLifecycleOwner) { references ->
                    feedAdapter.updateData(references)
                    //Si hay datos pero el listado de referencias está vacío se muestra un mensaje informativo
                    if (references.isEmpty()) {
                        binding.userMessage1.visibility = View.VISIBLE
                        binding.userMessage2.visibility = View.VISIBLE
                        binding.noBloomsImg.visibility = View.VISIBLE
                        binding.recyclerViewBoardList.visibility = View.GONE
                    } else {
                        //Si hay datos se muestra el recycler
                        binding.userMessage1.visibility = View.GONE
                        binding.userMessage2.visibility = View.GONE
                        binding.noBloomsImg.visibility = View.GONE
                        binding.recyclerViewBoardList.visibility = View.VISIBLE
                    }
                }
            }
        }

        // Observamos los tableros para actualizar el título si cambia
        boardsViewModel.boards.observe(viewLifecycleOwner) { boards ->
            boards.find { it.id == boardId }?.let { board ->
                // Actualizamos el título de la toolbar
                (activity as? AppCompatActivity)?.supportActionBar?.title = board.title
            }
        }

        //Observamos los mensaje de error
        boardsViewModel.errorMsg.observe(viewLifecycleOwner) { resId ->
            resId?.let {
                Toast.makeText(requireContext(), getString(it), Toast.LENGTH_SHORT).show()
                boardsViewModel.clearErrors()
            }
        }
        //Observamos los mensaje de éxito
        boardsViewModel.successMsg.observe(viewLifecycleOwner) { resId ->
            resId?.let {
                Toast.makeText(requireContext(), getString(it), Toast.LENGTH_SHORT).show()
                boardsViewModel.clearSuccess()
            }
        }
    }

    /**
     * Configura el PopUpMenu de la toolbar.
     * Ofrece las funcionalidades de edición del nombre del tablero y la eliminación del mismo.
     */
    private fun setupPopUpMenu() {
        //Se configura el menu de opciones en la toolbar
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Inflamos el menu en el xml
                menuInflater.inflate(R.menu.options_menu, menu)
            }

            //Se configuran las opciones
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    //Si se selecciona la opción de editar
                    R.id.edit_option -> {
                        //Se obtiene el id del tablero
                        boardId?.let { id ->
                            //Se infla el layout del diálogo
                            val dialogView =
                                LayoutInflater.from(requireContext())
                                    .inflate(R.layout.dialog_create_board, null)
                            //Se obtiene el campo de texto para editar el nombre
                            val editText =
                                dialogView.findViewById<TextInputEditText>(R.id.text_new_board_name)
                            //Se lanza un alert dialog para confirmar la acción
                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle(R.string.edit_name)
                                .setView(dialogView) //Se pasa el dialog para editar nombre
                                .setMessage(getString(R.string.edit_board_message))
                                .setPositiveButton(R.string.edit_name) { _, _ ->
                                    //Se actualiza el nombre del tablero
                                    val title = editText.text.toString().trim()
                                    if (title.isNotEmpty()) {
                                        // Pasamos la id del tablero y actualizamos el nombre
                                        boardsViewModel.updateBoard(id, title)
                                    } else {
                                        Toast.makeText(
                                            requireContext(),
                                            R.string.board_name_empty,
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
                                    }
                                }
                                .setNegativeButton(R.string.cancel, null)
                                .show()
                        }
                        true
                    }
                    //Si se selecciona la opción de eliminar
                    R.id.delete_option -> {
                        //Se lanza un alert dialog para confirmar la acción
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(R.string.delete_board)
                            .setMessage(getString(R.string.delete_board_message))
                            .setPositiveButton(R.string.delete) { _, _ ->
                                // Eliminamos el tablero y todas las referencias
                                boardId?.let { id ->
                                    boardsViewModel.deleteBoard(id)
                                    findNavController().popBackStack() //Volvemos a la pantalla anterior
                                }
                            }
                            .setNegativeButton(R.string.cancel, null)
                            .show()
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
}