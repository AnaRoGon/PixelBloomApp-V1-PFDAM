package dam.pfdam.pixelbloom.view.fragment.boards

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import dam.pfdam.pixelbloom.R
import dam.pfdam.pixelbloom.databinding.FragmentBoardSelectorBinding
import dam.pfdam.pixelbloom.view.fragment.boards.adapter.BoardSelectorAdapter
import dam.pfdam.pixelbloom.viewmodel.boards.BoardsViewModel

/**
 * Fragmento de tipo BottomSheet que permite al usuario seleccionar un tablero
 * para guardar una referencia específica.
 *
 * @property _binding Referencia privada al binding del layout.
 * @property binding Propiedad de acceso seguro al binding (non-null).
 * @property viewModel ViewModel que gestiona la lógica de tableros del usuario actual.
 * @property adapter Adaptador con vista simplificada para listar los tableros de destino.
 * @property referenceId Identificador de la referencia visual que se pretende guardar.
 * @property isMoveMode Indicador que determina si el selector se usa para mover de tablero una imagen o para guardarla por primera vez.
 */
class BoardSelectorFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentBoardSelectorBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BoardsViewModel by activityViewModels()

    // Se define el adaptador
    private lateinit var adapter: BoardSelectorAdapter
    private var referenceId: String? = null
    private var isMoveMode: Boolean = false

    /**
     * Infla el layout del fragmento de selección de tableros.
     *
     * @param inflater El objeto LayoutInflater usado para inflar vistas.
     * @param container Si no es nulo, es la vista a la que la UI del fragmento se adjuntará.
     * @param savedInstanceState Estado de la instancia guardado previamente.
     * @return Vista inflada.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBoardSelectorBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Configura el comportamiento inicial basado en los argumentos recibidos (modo guardar referencia  o modo mover referencia).
     *
     * @param view La vista devuelta por onCreateView.
     * @param savedInstanceState Estado de la instancia previamente guardado.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Obtenemos los ID de los argumentos
        referenceId = arguments?.getString("referenceId")
        isMoveMode = arguments?.getBoolean("isMoveMode", false) ?: false

        // Se inicializa el RecyclerView
        setupRecyclerView()
        // Se configuran los observadores
        setupViewModelObservers()

        // Si estamos en modo 'mover tablero', ocultamos la opción de crear tablero
        // para simplificar la UX/UI y evitar errores. Es decir, cuando se mueve una imagen de un
        // tablero a otro y se abre este selector no nos interesa que se puedan crear tableros
        if (isMoveMode) {
            binding.btnCreateBoard.visibility = View.GONE
        }
        //Si no se está en modo edición se muestra el botón para crear tablero
        //y define el listener para el boton
        binding.btnCreateBoard.setOnClickListener {
            showCreateBoardDialog()
        }
    }

    /**
     * Establece los observadores del [BoardsViewModel] para actualizar la lista del RecyclerView
     * cuando los tableros del usuario cambian.
     */
    private fun setupViewModelObservers() {
        // Observamos los cambios en los tableros para actualizar la lista del selector
        viewModel.boards.observe(viewLifecycleOwner) { boards ->
            adapter.updateData(boards)
        }
    }

    /**
     * Muestra un Dialog para la creación de un nuevo tablero,
     * y asocia la referencia actual (si la hay) en el nuevo tablero una vez creado.
     */
    private fun showCreateBoardDialog() {
        //Inflamos el layout del diálogo
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_create_board, null)
        //Obtenemos el campo de texto para editar el nombre
        val editText = dialogView.findViewById<TextInputEditText>(R.id.text_new_board_name)
        //Mostramos el diálogo
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.create_new_board)
            .setView(dialogView) //Se pasa el dialog para editar nombre
            .setCancelable(false)
            .setPositiveButton(R.string.save_image) { _, _ ->
                val title = editText.text.toString().trim()
                if (title.isEmpty()) {
                    Toast.makeText(requireContext(), R.string.board_name_empty, Toast.LENGTH_SHORT)
                        .show()
                } else {
                    // Guardamos la referencia indicada en el tablero
                    viewModel.createBoard(title, referenceId)
                    dismiss()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    /**
     * Configura el RecyclerView utilizando el adaptador [BoardSelectorAdapter],
     * y controla la lógica de selección de tablero en modo movimiento o guardado por defecto.
     */
    private fun setupRecyclerView() {
        // Configuramos el diseño del recyclerView
        binding.recyclerViewBoardsSelector.layoutManager = LinearLayoutManager(requireContext())

        // Y el adaptador
        adapter = BoardSelectorAdapter(emptyList()) { board ->
            if (isMoveMode) {
                // Si estamos en modo 'mover tablero'
                // usamos la función del viewModel indicar el id del tablero
                // donde se quiere mover la imagen y que se actualice el split button en el fragment de detalles.
                viewModel.selectBoardForMove(board)
            } else {
                // Modo guardado estándar (desde Feed). Se añade el tablero directamente
                //con la referencia seleccionada
                referenceId?.let { refId ->
                    viewModel.addReferenceToBoard(board.id, refId)
                }
            }
            dismiss()
        }
        //Se enlaza el adaptador con el recycler
        binding.recyclerViewBoardsSelector.adapter = adapter
    }

    /**
     * Libera el binding y limpia las asignaciones de memoria al destruirse la vista.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
