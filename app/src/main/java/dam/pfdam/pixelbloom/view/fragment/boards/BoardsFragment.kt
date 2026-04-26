package dam.pfdam.pixelbloom.view.fragment.boards

import android.os.Bundle
import dam.pfdam.pixelbloom.viewmodel.boards.UserChallengesViewModel
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import dam.pfdam.pixelbloom.R
import dam.pfdam.pixelbloom.databinding.FragmentBoardsBinding
import dam.pfdam.pixelbloom.view.fragment.boards.adapter.BoardsAdapter
import dam.pfdam.pixelbloom.view.fragment.boards.adapter.UserChallengesAdapter
import dam.pfdam.pixelbloom.viewmodel.MainViewModel
import dam.pfdam.pixelbloom.viewmodel.boards.BoardsViewModel

/**
 * Fragmento encargado de mostrar los tableros y retos del usuario.
 *
 * Utiliza un RecyclerView con un [GridLayoutManager] para los tableros y otro con un
 * [LinearLayoutManager] horizontal para los retos. Cada uno de los recycler consume un adaptador
 * personalizado para mostrar los elementos correspondientes.
 *
 * @property _binding Referencia privada al binding del layout.
 * @property binding Propiedad de acceso seguro al binding (non-null).
 * @property boardsAdapter Adaptador personalizado para el RecyclerView con la lista de tableros.
 * @property userChallengesAdapter Adaptador personalizado para el carrusel de retos.
 * @property boardsViewModel ViewModel encargado de gestionar la lista de tableros.
 * @property userChallengesViewModel ViewModel encargado de gestionar los retos aceptados.
 * @property mainViewModel ViewModel principal para verificar rol del usuario y mostrar o no la sección.
 */
class BoardsFragment : Fragment() {

    private var _binding: FragmentBoardsBinding? = null
    private val binding get() = _binding!!

    //Añadimos el adaptador para los tableros y los retos
    private lateinit var boardsAdapter: BoardsAdapter
    private lateinit var userChallengesAdapter: UserChallengesAdapter

    //Definimos los viewmodels
    private val boardsViewModel: BoardsViewModel by activityViewModels()
    private val userChallengesViewModel: UserChallengesViewModel by activityViewModels()
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
        //Se infla el layout
        _binding = FragmentBoardsBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Configura los componentes de la interfaz, observadores y la carga inicial de datos.
     * Además, define el listener para el floating button.
     *
     * @param view La vista devuelta por [onCreateView].
     * @param savedInstanceState Estado guardado del fragmento.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Se inicializan los recyclerView
        setupRecyclerView()
        //Configuramos la observación de los ViewModel
        setupViewModelObservers()

        //Definimos el listener del floating buttom
        binding.createNewBoardFloatingButton.setOnClickListener {
            showCreateBoardDialog()
        }

    }

    /**
     * Configura los RecyclerViews con sus adaptadores personalizados y LayoutManagers.
     */
    private fun setupRecyclerView() {
        // Se configura el adaptador para boards
        boardsAdapter = BoardsAdapter(emptyList()) { selectedItem ->
            val bundle = Bundle().apply {
                putString("boardId", selectedItem.id)
            }

            //Configuramos la navegación al recyclerview al listado de imagenes almacenadas en ese board.
            val navController = findNavController()
            if (navController.currentDestination?.id == R.id.boardsFragment) {
                navController.navigate(R.id.action_boardsFragment_to_boardListFragment, bundle)
            }
        }

        //Se define el estilo del recycler
        val gridLayoutManager = GridLayoutManager(requireContext(), 2) // Grid en columnas de dos
        binding.recyclerViewBoards.layoutManager = gridLayoutManager
        //Se enlaza el recycler con su adaptador
        binding.recyclerViewBoards.adapter = boardsAdapter

        //Se configura el adaptador para los retos
        userChallengesAdapter =
            UserChallengesAdapter(emptyList()) { selectedItem ->
                val bundle = Bundle().apply {
                    putString("challengeId", selectedItem.id)
                }

                //Se configura la navegación para el acceso a detalles del reto al seleccionar un item
                val navController = findNavController()
                // Por seguridad para que la app no se crashee se hace la comprobación de que estamos en boards
                if (navController.currentDestination?.id == R.id.boardsFragment) {
                    navController.navigate(
                        R.id.action_boardsFragment_to_challengesDetailsFragment,
                        bundle
                    )
                }
            }
        //Se define el estilo del recycler
        val horizontalLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.carouselRecyclerView.layoutManager = horizontalLayoutManager
        //Se enlaza el recycler con su adaptador
        binding.carouselRecyclerView.adapter = userChallengesAdapter
    }

    /**
     * Configura la observación de los LiveData del BoardsViewModel y MainViewModel.
     * Escucha cambios en los retos del usuario y muestra errores/éxito mediante Toast.
     */
    private fun setupViewModelObservers() {
        // Observamos el rol del usuario
        mainViewModel.isAdmin.observe(viewLifecycleOwner) { isAdmin ->
            if (isAdmin) {
                // Si es admin se ocultan los recycler view y se muestra un mensaje informativo
                binding.recyclerViewBoards.visibility = View.GONE
                binding.bottomChallengesContainer.visibility = View.GONE
                binding.createNewBoardFloatingButton.visibility = View.GONE
                binding.adminMessage.visibility = View.VISIBLE
            } else {
                // Si es un usuario estándar se muestran los recyclerview y se cargan los datos
                binding.recyclerViewBoards.visibility = View.VISIBLE
                binding.bottomChallengesContainer.visibility = View.VISIBLE
                binding.adminMessage.visibility = View.GONE
                //Se observan los cambios para la sección de retos
                userChallengesViewModel.userChallenges.observe(viewLifecycleOwner) { challenges ->
                    if (challenges.isEmpty()) {
                        //Si no hay retos se muestra un mensaje informativo al usuario para
                        //recomendarle añadir retos a esta sección.
                        binding.userMessage3.visibility = View.VISIBLE
                        binding.noUserChallImg1.visibility = View.VISIBLE
                        binding.noUserChallImg2.visibility = View.VISIBLE
                    } else {
                        binding.userMessage3.visibility = View.GONE
                        binding.noUserChallImg1.visibility = View.GONE
                        binding.noUserChallImg2.visibility = View.GONE
                    }
                    //Se actualizan los datos para mantener la coherencia en la app
                    userChallengesAdapter.updateData(challenges)
                }
                //Se observan los cambios para la sección de boards
                boardsViewModel.boards.observe(viewLifecycleOwner) { boards ->
                    if (boards.isEmpty()) {
                        //Si no hay tableros se muestra un mensaje informativo al usuario para
                        //recomendarle añadir tableros a esta sección.
                        binding.userMessage1.visibility = View.VISIBLE
                        binding.userMessage2.visibility = View.VISIBLE
                        binding.noBoardImg.visibility = View.VISIBLE
                    } else {
                        binding.userMessage1.visibility = View.GONE
                        binding.userMessage2.visibility = View.GONE
                        binding.noBoardImg.visibility = View.GONE
                    }
                    //Se actualizan los datos para mantener la coherencia en la app
                    boardsAdapter.updateData(boards)
                }
            }
        }

        // Observamos posibles mensajes de error de ambos viewmodels
        boardsViewModel.errorMsg.observe(viewLifecycleOwner) { resId ->
            resId?.let {
                Toast.makeText(requireContext(), getString(it), Toast.LENGTH_SHORT).show()
                boardsViewModel.clearErrors()
            }
        }
        userChallengesViewModel.errorMsg.observe(viewLifecycleOwner) { resId ->
            resId?.let {
                Toast.makeText(requireContext(), getString(it), Toast.LENGTH_SHORT).show()
                userChallengesViewModel.clearErrors()
            }
        }

        // Y posible mensajes de éxito
        boardsViewModel.successMsg.observe(viewLifecycleOwner) { resId ->
            resId?.let {
                Toast.makeText(requireContext(), getString(it), Toast.LENGTH_SHORT).show()
                boardsViewModel.clearSuccess()
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
     * Muestra un Dialog que permite al usuario introducir el título y crear un nuevo tablero vacío,
     * llamando a la función correspondiente en [BoardsViewModel].
     */
    private fun showCreateBoardDialog() {
        //Inflamos el layout del dialog
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_create_board, null)
        //Obtenemos el campo de texto
        val editText = dialogView.findViewById<TextInputEditText>(R.id.text_new_board_name)
        //Lanzamos el dialog personalizado para crear tableros nuevos
        //que llama a la función del viewmodel con la referencia en null
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.create_new_board)
            .setView(dialogView)
            .setCancelable(false)
            .setPositiveButton(R.string.save_image) { _, _ ->
                val title = editText.text.toString().trim()
                if (title.isNotEmpty()) {
                    // Creamos el tablero vacío inicialmente
                    boardsViewModel.createBoard(title, null)
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    /**
     * Libera la referencia al binding para evitar fugas de memoria al destruir la vista.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}




