package dam.pfdam.pixelbloom.view.fragment.challenges

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dam.pfdam.pixelbloom.data.network.RetrofitInstance
import dam.pfdam.pixelbloom.databinding.FragmentNewChallengeBinding
import kotlinx.coroutines.launch
import androidx.core.graphics.toColorInt
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dam.pfdam.pixelbloom.R
import dam.pfdam.pixelbloom.viewmodel.challenges.ChallengesViewModel

/**
 * Dialogo BottomSheet encargado de la creación y edición de retos.
 *
 * Permite a los administradores introducir un título, descripción y generar una paleta
 * de colores mediante una búsqueda por palabra clave en la API de ColorMagic.
 * Gestiona tanto el modo de creación como el de edición/borrado de retos existentes.
 *
 * @property _binding Referencia privada al binding del layout.
 * @property binding Propiedad de acceso seguro al binding (non-null).
 * @property viewModel ViewModel que gestiona la persistencia de los retos.
 * @property isEditMode Indica si el fragmento se ha abierto para editar un reto existente.
 * @property currentSelectedPalette Paleta de colores seleccionada actualmente para el reto.
 */
class NewChallengeFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentNewChallengeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChallengesViewModel by activityViewModels()

    //Variables para comprobar si estamos en modo edición para desactivar el botón de añadir imagen
    private var isEditMode = false

    // Al principio de la clase, junto a isEditMode
    private var currentSelectedPalette: List<String>? = null


    /**
     * Infla el layout del fragmento y configura el binding.
     *
     * @param inflater El objeto LayoutInflater.
     * @param container El contenedor padre.
     * @param savedInstanceState El estado guardado.
     * @return La vista raíz del binding.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Se infla el dialog
        _binding = FragmentNewChallengeBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Inicializa la interfaz y verifica si el diálogo fue abierto para la edición
     * de un reto existente.
     *
     * @param view La vista devuelta por [onCreateView].
     * @param savedInstanceState Estado de la instancia guardado previamente.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        checkEditMode()
    }


    /**
     * Configura los listeners de los botones y componentes de la interfaz.
     */
    private fun setupListeners() {
        binding.cancelButton.setOnClickListener {
            dismiss()
        }
        binding.okButton.setOnClickListener {
            validateAndSaveChallenge()
        }
        binding.deleteButton.setOnClickListener {
            //Obtenemos el title del challenge
            val title = viewModel.selectedChallengeForEdit.value?.title.toString()
            // Creamos el diálogo de confirmación
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete_challenge_title)
                .setMessage(getString(R.string.delete_challenge_message, title))
                .setPositiveButton(R.string.delete) { _, _ ->
                    // Si confirma, ejecutamos la lógica de borrado
                    deleteChallenge()
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
        binding.searchPalette.setOnClickListener {
            val query = binding.outlinedTextInputSearch.editText?.text.toString().trim()
            if (query.isNotEmpty()) {
                searchPalette(query)
            } else {
                Toast.makeText(
                    requireContext(),
                    R.string.write_something_search,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Verifica si se ha seleccionado un reto para editar en el ViewModel.
     * En caso afirmativo, pre-carga sus datos en los campos del formulario.
     */
    private fun checkEditMode() {
        val chal = viewModel.selectedChallengeForEdit.value
        //Si no obtenemos el id de un reto
        if (chal != null) {
            //Estamos en el modo edición
            isEditMode = true
            //cargamos los datos en la view
            binding.titleTextView.text =
                getString(R.string.edit_or_delete_challenge) //Se cambia el titulo del modal
            binding.outlinedTextAreaDescription.editText?.setText(chal.description)
            binding.outlinedTextInputTitle.editText?.setText(chal.title)

            //Obtenemos la paleta y la pintamos en la view
            displayRandomPalette(chal.palette)

            //Inhabilitamos el botón de buscar paleta y el campo de texto respectivo
            binding.searchPalette.isEnabled = false
            binding.outlinedTextInputSearch.isEnabled = false
            binding.deleteButton.visibility = View.VISIBLE
        }
    }

    /**
     * Valida que los campos obligatorios esten completos y guarda o actualiza el reto.
     */
    private fun validateAndSaveChallenge() {
        // Extraemos el valor de los campos de texto.
        val title = binding.outlinedTextInputTitle.editText?.text.toString().trim()
        val description = binding.outlinedTextAreaDescription.editText?.text.toString().trim()

        //Se recupera la paleta obtenida de la API
        val selectedPalette = currentSelectedPalette

        // Si no es modo edición, es obligatorio tener una paleta generada
        if (!isEditMode && selectedPalette == null) {
            Toast.makeText(requireContext(), R.string.generate_palette_first, Toast.LENGTH_SHORT)
                .show()
            return
        }

        //Se controla que se hayan completado todos los campos de texto
        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(
                requireContext(),
                R.string.complete_all_fields,
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Si estamos en modo edición
        if (isEditMode) {
            // Obtenemos el challenge original
            val originalChal = viewModel.selectedChallengeForEdit.value

            // En modo edición la paleta es inmmutable, por lo que usamos siempre la original
            val originalPalette = originalChal?.palette

            if (originalPalette != null) {
                originalChal.id.let { id ->
                    viewModel.updateChallenge(id, description, originalPalette, title)
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    R.string.error_getting_original_palette,
                    Toast.LENGTH_SHORT
                ).show()
            }

        } else {
            //Almacenamos el nuevo reto en firestore
            if (selectedPalette != null) {
                viewModel.saveChallengeToFirestore(description, selectedPalette, title)
            } else {
                Toast.makeText(
                    requireContext(),
                    R.string.must_generate_palette,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        dismiss() // Cerramos el modal tras enviar la orden
    }

    /**
     * Realiza una búsqueda de paletas de colores en la API externa basada en una palabra clave.
     * Gestiona el estado de carga y actualiza la visualización de la paleta obtenida.
     *
     * @param query Palabra clave para la generación de la paleta (ej: "océano").
     */
    private fun searchPalette(query: String) {
        // Durante la llamada a la API se muestra la barra de carga y se desactivan los botones
        binding.loadingProgress.visibility = View.VISIBLE
        binding.paletteContainer.visibility = View.GONE
        binding.searchPalette.isEnabled = false

        // Lanzamos la corrutina para conectarnos a la API
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.getPaletteColor(query)
                if (response.isSuccessful) { //Si obtenemos un http 200 (OK)
                    response.body()?.let { paletteList ->
                        if (paletteList.isNotEmpty()) {
                            //Obtenemos una paleta aleatoria
                            val randomPalette = paletteList.random()
                            //Pintamos los colores en la UI
                            displayRandomPalette(randomPalette.colors)
                        } else {
                            //Se muestra un mensaje de error
                            Toast.makeText(
                                requireContext(),
                                R.string.palette_not_found,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else { //SI falla la conexión a la API
                    //Se informa al usuario
                    Toast.makeText(
                        requireContext(),
                        R.string.error_getting_palette,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("API_EXCEPTION", "Excepción de red o parseo: ${e.message}", e)
            } finally {
                // Ocultar la barra de carga y volver a habilitar el botón siempre, falle o tenga éxito, para poder generar otra paleta si no es del gusto del admin
                binding.loadingProgress.visibility = View.GONE
                binding.searchPalette.isEnabled = true
            }
        }
    }

    /**
     * Muestra visualmente la paleta de colores en los componentes correspondientes de la vista.
     *
     * @param colors Lista de códigos de color en formato Hexadecimal.
     */
    private fun displayRandomPalette(colors: List<String>) {
        currentSelectedPalette =
            colors //Se guarda la paleta para poder almacenarla en firestore cuando se clickee el boton de aceptar
        binding.paletteContainer.visibility = View.VISIBLE
        colors.let { colors ->
            //Se crea un listado con las view que contendrán cada color
            val colorViews = listOf(
                binding.hex01,
                binding.hex02,
                binding.hex03,
                binding.hex04,
                binding.hex05
            )
            //Al utilizar zip hacemos que cada color quede enlazado a la vista correspondiente y mutamos el background para no afectar a otras pantallas
            colors.zip(colorViews).forEach { (color, view) ->
                //Usamos mutate() para crear una instancia única de este fondo y no afectar al resto de la app
                view.background.mutate().setTint(color.toColorInt())
            }
        }

    }

    /**
     * Ejecuta la eliminación definitiva del reto actual si se confirma en el diálogo.
     */
    private fun deleteChallenge() {
        // Obtenemos el challenge original
        val originalChal = viewModel.selectedChallengeForEdit.value
        originalChal?.let { chal ->
            // Llamamos al ViewModel con ID para eliminarlo
            viewModel.deleteChallenge(chal.id)
            dismiss() //Cerramos
        }
    }
}
