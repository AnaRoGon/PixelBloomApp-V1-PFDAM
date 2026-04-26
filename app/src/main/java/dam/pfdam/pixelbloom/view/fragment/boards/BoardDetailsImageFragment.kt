package dam.pfdam.pixelbloom.view.fragment.boards

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dam.pfdam.pixelbloom.utils.loadImage
import dam.pfdam.pixelbloom.R
import dam.pfdam.pixelbloom.databinding.FragmentBoardDetailsImageBinding
import dam.pfdam.pixelbloom.viewmodel.feed.FeedViewModel
import dam.pfdam.pixelbloom.viewmodel.boards.BoardsViewModel
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dam.pfdam.pixelbloom.data.model.Reference
import dam.pfdam.pixelbloom.utils.ImageUtils
import kotlin.getValue

/**
 * Fragmento que muestra los detalles de la referencia guardada en el tablero.
 *
 * @property _binding Referencia privada al binding del layout.
 * @property binding Propiedad de acceso seguro al binding (non-null).
 * @property feedViewModel ViewModel compartido para acceder a los datos actualizados del feed.
 * @property boardsViewModel ViewModel compartido para la gestión de tableros.
 * @property referenceId ID de la referencia visualizada.
 * @property currentBoardId ID del tablero actual al que pertenece la imagen.
 */
class BoardDetailsImageFragment : Fragment() {
    private var _binding: FragmentBoardDetailsImageBinding? = null
    private val binding get() = _binding!!
    private val feedViewModel: FeedViewModel by activityViewModels()
    private val boardsViewModel: BoardsViewModel by activityViewModels()
    private var referenceId: String? = null
    private var currentBoardId: String? = null

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
        _binding = FragmentBoardDetailsImageBinding.inflate(inflater, container, false)
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
        //Obtenemos los ID de la referencia y del tablero que se nos pasan por argumentos
        referenceId = arguments?.getString("referenceId")
        currentBoardId = arguments?.getString("boardId")
        //Se observan los cambios en los datos del ViewModel
        setupViewModelObservers()
        //Se configuran los listeners
        setupListeners()
    }

    /**
     * Configura los listeners de los botones para eliminar, mover o cambiar de tablero la imagen.
     */
    private fun setupListeners() {
        binding.deleteButton.setOnClickListener {
            val refId = referenceId
            val boardId = currentBoardId
            //Si no tenemos referencia o tablero no hacemos nada
            if (refId == null || boardId == null) return@setOnClickListener

            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete_reference_title)
                .setMessage(
                    getString(
                        R.string.delete_reference_from_board_message,
                        binding.imageDetailsTitle.text
                    )
                )
                .setPositiveButton(R.string.delete) { _, _ ->
                    //Se elimina la referencia del tablero en el que se encuentra
                    boardsViewModel.deleteReferenceBoard(boardId, refId)
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }

        // Listener para mover la imagen al tablero indicado
        binding.confirmSaveButton.setOnClickListener {
            val refId = referenceId
            val fromId = currentBoardId
            val targetBoard = boardsViewModel.selectedBoardForMove.value
            val toId = targetBoard?.id

            if (refId != null && fromId != null && toId != null) {
                // Solo movemos si el tablero es distinto
                if (fromId == toId) {
                    Toast.makeText(requireContext(), R.string.error_same_board, Toast.LENGTH_SHORT)
                        .show()
                } else {
                    boardsViewModel.moveReferenceToBoard(refId, fromId, toId)
                }
            } else if (toId == null) {
                // Si no hay destino, notificamos al usuario que debe usar la flecha
                Toast.makeText(requireContext(), R.string.select_board_first, Toast.LENGTH_SHORT)
                    .show()
            }
        }

        //Se activa el listener para abrir el selector de tableros
        binding.boardsOptions.setOnClickListener {
            openBoardSelector()
        }
    }

    /**
     * Abre el fragmento [BoardSelectorFragment] permitiendo seleccionar un nuevo tablero de destino.
     */
    private fun openBoardSelector() {
        referenceId?.let { id ->
            val dialog = BoardSelectorFragment()
            val bundle = Bundle().apply {
                putString("referenceId", id)
                putBoolean("isMoveMode", true) // Indicamos que es para el split button
            }
            dialog.arguments = bundle
            dialog.show(parentFragmentManager, "BoardSelectorFragment")
        }
    }

    /**
     * Observa los cambios en los datos del ViewModel relacionados con la navegación,
     * mensajes de feedback y la obtención de la información de la referencia.
     */
    private fun setupViewModelObservers() {

        // Truncamos el texto del botón si es mayor a 5 chars para controlar el diseño del botón
        //Al inicio
        boardsViewModel.boards.value?.find { it.id == currentBoardId }?.let { board ->
            val displayTitle =
                if (board.title.length > 5) "${board.title.take(5)}..." else board.title
            binding.confirmSaveButton.text = displayTitle
        }

        // Resetemos la selección previa si la hubiera de un tablero antes
        boardsViewModel.clearSelectedBoardForMove()

        // Observamos el tablero seleccionado para actualizar el texto del botón con el truncado
        boardsViewModel.selectedBoardForMove.observe(viewLifecycleOwner) { targetBoard ->
            if (targetBoard != null) {
                val displayTitle =
                    if (targetBoard.title.length > 5) "${targetBoard.title.take(5)}..." else targetBoard.title
                binding.confirmSaveButton.text = displayTitle
            }
        }

        // Observamos el tablero actual para actualizar la imagen
        boardsViewModel.selectedBoardReferences.observe(viewLifecycleOwner) { references ->
            val reference = references.find { it.id == referenceId }
            if (reference != null) {
                displayReference(reference)
            }
        }

        // También observamos FeedViewModel por si los datos cambian globalmente
        feedViewModel.references.observe(viewLifecycleOwner) { references ->
            val reference = references.find { it.id == referenceId }
            if (reference != null) {
                displayReference(reference)

            } else if (references.isNotEmpty() && boardsViewModel.selectedBoardReferences.value?.find { it.id == referenceId } == null) {
                // Solo volvemos atrás si la lista no está vacía Y no se encuentra en ninguna de las dos fuentes
                // lo que indica que probablemente ha sido borrada globalmente.
                try {
                    findNavController().popBackStack()
                } catch (e: Exception) {
                    //Mostramos un mensaje de error si no se puede navegar hacia atrás a nivel interno
                    Log.e("BoardDetailsImage", "Error al navegar hacia atrás: ${e.message}")
                }
            }
        }

        // Observamos errores
        boardsViewModel.errorMsg.observe(viewLifecycleOwner) { resId ->
            resId?.let {
                Toast.makeText(requireContext(), getString(resId), Toast.LENGTH_SHORT).show()
                boardsViewModel.clearErrors()
            }
        }

        // Observamos mensajes de éxito
        boardsViewModel.successMsg.observe(viewLifecycleOwner) { resId ->
            resId?.let {
                // Se lanza cualquier mensaje de éxito con el texto del viewModel
                Toast.makeText(requireContext(), getString(it), Toast.LENGTH_SHORT).show()
                // Se limpia el mensaje de éxito
                boardsViewModel.clearSuccess()

                // Volvemos atrás si el mensaje nos indica que la referencia ha sido movida, guardada o eliminada del tablero
                if (it == R.string.board_image_moved_success ||
                    it == R.string.board_image_saved_success ||
                    it == R.string.board_image_deleted_success
                ) {
                    findNavController().popBackStack()
                }
            }
        }
    }

    /**
     * Actualiza la interfaz gráfica con los detalles de la referencia proporcionada.
     *
     * Carga la imagen del usuario utilizando la clase [ImageUtils] que nos permite simplificar
     * la lógica de carga de imagenes con la librería de Picasso.
     * @param reference Objeto que contiene título, autor, descripción y la ruta de la imagen.
     */
    private fun displayReference(reference: Reference) {
        binding.imageDetailsTitle.text = reference.title
        binding.imageDetailsAuthor.text = reference.author
        // Actualizamos el título de la toolbar
        (activity as? AppCompatActivity)?.supportActionBar?.title = reference.title

        binding.imageDetailsDescription.text = reference.description
        binding.feedDetailsImage.loadImage(reference.imagePath)
    }
}