package dam.pfdam.pixelbloom.view.fragment.feed

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dam.pfdam.pixelbloom.utils.loadImage
import dam.pfdam.pixelbloom.R
import dam.pfdam.pixelbloom.databinding.FragmentNewReferenceBinding
import dam.pfdam.pixelbloom.viewmodel.feed.FeedViewModel
import kotlin.getValue

/**
 * Fragmento de tipo BottomSheet encargado de la creación y edición de referencias.
 *
 * Utiliza un [BottomSheetDialogFragment] para presentar un formulario donde el administrador
 * puede introducir datos de una nueva referencia o modificar una existente.
 * Incluye funcionalidad para seleccionar una imagen de la galería mediante [PickVisualMedia].
 *
 * @property _binding Referencia privada al binding del layout.
 * @property binding Propiedad de acceso seguro al binding (non-null).
 * @property selectedImageUri URI de la imagen seleccionada localmente.
 * @property viewModel ViewModel que gestiona la persistencia de las referencias.
 * @property isEditMode Indica si el fragmento se encuentra en modo edición o creación.
 */
class NewReferenceFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentNewReferenceBinding? = null
    private val binding get() = _binding!!
    private var selectedImageUri: android.net.Uri? = null
    private val viewModel: FeedViewModel by activityViewModels()

    //Variables para comprobar si estamos en modo edición para desactivar el botón de añadir imagen
    private var isEditMode = false

    // Uso pickMedia para la selección de imágenes desde la galería
    val pickMedia = registerForActivityResult(PickVisualMedia()) { uri ->
        // Si el usuario selecciona una imagen, se muestra su URI
        if (uri != null) {
            Log.i("PruebaPhotoPicker", "URI seleccionada: $uri")
            //Se guarda la uri en una variable
            selectedImageUri = uri
            //Se muestra la imagen seleccionada en el imageView
            binding.referenceImage.setImageURI(uri)
            //Para mantener la información para procesos de larga duración, por ejemplo,
            //Si se esta subiendo al Storage una imagen
            val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
            context?.contentResolver?.takePersistableUriPermission(uri, flag)
        } else {
            //NO se hace nada
            Log.i("PruebaPhotoPicker", "No se ha seleccionado ninguna imagen")
        }
    }

    /**
     * Infla el layout del fragmento y configura el binding.
     *
     * @param inflater El objeto LayoutInflater.
     * @param container El contenedor padre.
     * @param savedInstanceState El estado guardado.
     * @return La vista raíz del binding.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNewReferenceBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Inicializa la interfaz de usuario y verifica el modo de operación (creación o edición).
     *
     * @param view La vista creada.
     * @param savedInstanceState El estado guardado.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        checkEditMode()
    }

    /**
     * Configura los listeners de los botones de la interfaz.
     */
    private fun setupListeners() {
        //Se implementan los listeners para los botones
        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        binding.okButton.setOnClickListener {
            validateAndSaveReference()
        }

        binding.addImg.setOnClickListener {
            //De momento la idea es que se seleccionen sólo imagenes. A futuro podría ampliarse
            // y que el usuario pudiera seleccionar vídeos, gif e imágenes.
            pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
        }
    }


    /**
     * Valida los campos del formulario y procede a guardar o actualizar la referencia en el ViewModel.
     * Muestra mensajes de error si faltan campos obligatorios.
     */
    private fun validateAndSaveReference() {
        // Extraemos el valor de los campos de texto.
        val author = binding.outlinedTextInputAuthor.editText?.text.toString().trim()
        val title = binding.outlinedTextInputTitle.editText?.text.toString().trim()
        val description = binding.outlinedTextAreaDescription.editText?.text.toString().trim()

        //Se controla que se hayan completado todos
        if (author.isEmpty() || title.isEmpty() || description.isEmpty()) {
            Toast.makeText(
                requireContext(),
                R.string.complete_all_fields,
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Comprobamos si estamos en modo edición
        if (isEditMode) {
            // Obtenemos la referencia original
            val originalRef = viewModel.selectedReferenceForEdit.value

            // En modo edición la imagen es inmutable, por lo que usamos siempre la original
            val finalImagePath = originalRef?.imagePath ?: ""
            // Se pintan los datos de la referencia obtenida
            originalRef?.id?.let { id ->
                viewModel.updateReference(id, author, description, finalImagePath, title)
            }

        } else {
            //Sino, estamos en el modo creación y es obligatorio añadir la imagen
            if (selectedImageUri !== null) {
                //Si hay referencia guarda en la base de datos
                viewModel.uploadReference(author, description, selectedImageUri!!, title)
            } else {
                Toast.makeText(
                    requireContext(),
                    R.string.select_image_error,
                    Toast.LENGTH_SHORT
                ).show()
                return

            }
        }
        dismiss() // Cerramos el modal tras enviar la orden
    }

    /**
     * Verifica si hay una referencia seleccionada para editar en el ViewModel.
     * Si existe, configura la UI para el modo edición rellenando los campos con los datos actuales.
     */
    private fun checkEditMode() {
        val ref = viewModel.selectedReferenceForEdit.value
        if (ref != null) {
            //Estamos en el modo edición
            isEditMode = true
            //cargamos los datos en el layout
            binding.titleTextView.text =
                getString(R.string.edit_reference) //Se cambia el titulo del modal
            binding.outlinedTextInputAuthor.editText?.setText(ref.author)
            binding.outlinedTextInputTitle.editText?.setText(ref.title)
            binding.outlinedTextAreaDescription.editText?.setText(ref.description)
            // Cargamos la imagen actual para que se vea mientras se edita con nuestra funcion personalizada
            binding.referenceImage.loadImage(ref.imagePath)

            //Inhabilitamos el botón de añadir imagen
            binding.addImg.isEnabled = false
        }
    }

    /**
     * Limpia la referencia de edición en el ViewModel y libera el binding al destruir la vista.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.selectReferenceForEdit(null)
        _binding = null
    }
}



