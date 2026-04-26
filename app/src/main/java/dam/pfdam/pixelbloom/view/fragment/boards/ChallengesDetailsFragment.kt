package dam.pfdam.pixelbloom.view.fragment.boards

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dam.pfdam.pixelbloom.utils.loadImage
import dam.pfdam.pixelbloom.R
import dam.pfdam.pixelbloom.databinding.FragmentChallengesDetailsBinding
import dam.pfdam.pixelbloom.viewmodel.boards.UserChallengesViewModel

/**
 * Fragmento que muestra los detalles de un reto una vez ha sido aceptado por el user.
 *
 * @property _binding Referencia privada al binding del layout.
 * @property binding Propiedad de acceso seguro al binding (non-null).
 * @property userChallengeId Identificador del reto del usuario a visualizar.
 * @property userChallengesViewModel ViewModel para gestionar los retos del usuario actual.
 * @property selectedImageUri URI de la imagen seleccionada localmente por el usuario al resolver el reto.
 * @property isEditMode Indica si nos encontramos en modo edición del reto.
 * @property pickMedia Manejador de la selección de imágenes proporcionado por ActivityResultContracts.
 */
class ChallengesDetailsFragment : Fragment() {
    private var _binding: FragmentChallengesDetailsBinding? = null
    private val binding get() = _binding!!
    private var userChallengeId: String? = null

    //Se define el viewModel
    private val userChallengesViewModel: UserChallengesViewModel by activityViewModels()
    private var selectedImageUri: android.net.Uri? = null

    //Variables para comprobar si estamos en modo edición para la imagen
    private var isEditMode = false

    // Uso pickMedia para la selección de imágenes desde la galería del dispositivo
    val pickMedia = registerForActivityResult(PickVisualMedia()) { uri ->
        // Si el usuario selecciona una imagen, se muestra su URI
        if (uri != null) {
            Log.i("PruebaPhotoPicker", "URI seleccionada: $uri")
            //Se guarda la uri en una variable
            selectedImageUri = uri
            //Se muestra la imagen seleccionada en el imageView
            binding.userChallengeImage.setImageURI(uri)
            //Para mantener la información para procesos de larga duración, por ejemplo,
            //Si se esta subiendo al Storage una imagen
            val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
            context?.contentResolver?.takePersistableUriPermission(uri, flag)

            // Mostramos el botón para completar el reto una vez haya foto elegida
            binding.completeChallengeFloatingButton.show()
        } else {
            Log.i("PruebaPhotoPicker", "No se ha seleccionado ninguna imagen")
        }
    }


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
        _binding = FragmentChallengesDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Acciones posteriores a la creación de la vista. Se recuperan los argumentos,
     * se inician los observadores del [UserChallengesViewModel] y la configuración de listeners.
     *
     * @param view La vista devuelta por [onCreateView].
     * @param savedInstanceState Estado guardado del fragmento.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userChallengeId = arguments?.getString("challengeId")

        //Configuramos la observación de los LiveData
        setupViewModelObservers()

        //Se definen los listener
        setupListeners()
    }

    /**
     * Configura los listeners de los botones para eliminar el reto,
     * activar el modo edición, añadir imágenes locales o completar el reto.
     */
    private fun setupListeners() {

        //Para eliminar un reto se llama a la función de borrar.
        binding.deleteButton.setOnClickListener {
            deleteUserChallenge()
        }

        //Se define el listener para activar el modo edición
        binding.editButton.setOnClickListener {
            // Obtenemos el id del reto actual
            val userChallenge =
                userChallengesViewModel.userChallenges.value?.find { it.id == userChallengeId }
            //Si el challenge está incompleto se desactiva el modo edición
            if (userChallenge?.complete != true) {
                Toast.makeText(
                    requireContext(),
                    R.string.cannot_edit_incomplete_challenge,
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            } else {
                //Si no, se activa el modo edición
                isEditMode = true
                //Se añade el botón para confirmar que se ha completado el reto
                binding.addImg.isEnabled = true
                //Se avisa al usuario de que está en modo edición de reto ya que la UI, en este caso, necesita revisarse para ser más intuitiva
                Toast.makeText(requireContext(), R.string.edit_mode_activated, Toast.LENGTH_SHORT)
                    .show()
            }

        }
        //Se define el listener para añadir una imagen
        binding.addImg.setOnClickListener {
            //Utilizamos pickMedia para que el usuario suba la imagen deseada
            pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
        }

        //Se define el listener para completar un reto al pulsar el botón flotante
        binding.completeChallengeFloatingButton.setOnClickListener {
            //Si no hay imagen seleccionada no se puede completar el reto
            //Se avisa al usuario
            if (selectedImageUri == null) {
                Toast.makeText(
                    requireContext(),
                    R.string.select_image_error, Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Obtenemos el reto y la foto antigua para pasarla al userChallengesViewModel
            val currentUserChal =
                userChallengesViewModel.userChallenges.value?.find { it.id == userChallengeId }
            val oldImageUrl = currentUserChal?.userImagePath

            // Dependiento del modo en el que nos encontramos se mostrará un mensaje u otro en el alert dialog
            val dialogTitle =
                if (isEditMode) R.string.update_challenge_title else R.string.edit_or_delete_challenge
            val dialogMessage = if (isEditMode) {
                getString(R.string.update_challenge_message)
            } else {
                getString(
                    R.string.complete_challenge_message,
                    binding.challengeTitle.text
                )
            }

            // Creamos el alert dialog para confirmar la acción de completar el reto
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(dialogTitle)
                .setMessage(dialogMessage)
                .setPositiveButton(R.string.ok) { _, _ ->
                    // Si confirma se hace uso de la función del viewModel para completar un reto
                    userChallengeId?.let { id ->
                        userChallengesViewModel.completeChallenge(id, selectedImageUri, oldImageUrl)
                        isEditMode = false // Reseteamos el modo edición al enviar
                    }
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
    }

    /**
     * Configura los observadores para gestionar los mensajes de éxito o error
     * y las ventanas de carga.
     */
    private fun setupViewModelObservers() {

        //Observamos el challenge que nos llega
        observeUserChallenge()

        // Observamos errores desde el ViewModel
        userChallengesViewModel.errorMsg.observe(viewLifecycleOwner) { resId ->
            resId?.let {
                Toast.makeText(
                    requireContext(),
                    getString(it),
                    Toast.LENGTH_SHORT
                ).show()
                userChallengesViewModel.clearErrors()
            }
        }

        // Observamos mensajes de éxito
        userChallengesViewModel.successMsg.observe(viewLifecycleOwner) { resId ->
            resId?.let {
                Toast.makeText(
                    requireContext(),
                    getString(it),
                    Toast.LENGTH_SHORT
                ).show()
                userChallengesViewModel.clearSuccess()
            }
        }

        // Observamos estado de carga para actualizar la UI
        userChallengesViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.uploadProgressIndicator.visibility = View.VISIBLE
                binding.completeChallengeFloatingButton.isEnabled = false
                binding.addImg.isEnabled = false
                binding.editButton.isEnabled = false
            } else {
                binding.uploadProgressIndicator.visibility = View.GONE
                binding.completeChallengeFloatingButton.isEnabled = true
                binding.editButton.isEnabled = true
            }
        }
    }

    /**
     * Observa el estado del reto actual para actualizar dinámicamente la UI:
     * texto, estados de botones, y colores del palette almacenados.
     */
    private fun observeUserChallenge() {
        userChallengesViewModel.userChallenges.observe(viewLifecycleOwner) { userChallenges ->
            val userChallenge = userChallenges.find { it.id == userChallengeId }
            //Si obtenemos ID del reto actual, actualizamos la UI
            if (userChallenge != null) {
                binding.challengeTitle.text = userChallenge.title
                // Actualizamos el título de la toolbar
                (activity as? AppCompatActivity)?.supportActionBar?.title = userChallenge.title

                binding.userChallengeDescription.text = userChallenge.description
                binding.challengeStateText.text =
                    if (userChallenge.complete) getString(R.string.challenge_state_complete) else getString(
                        R.string.challenge_state_incomplete
                    )
                //Si el usuario no está en 'modo edición' del reto se desactiva el botón de añadir imagen
                //y el botón flotante de añadir reto completado.
                if (userChallenge.complete && !isEditMode) {
                    binding.addImg.isEnabled = false
                    binding.completeChallengeFloatingButton.hide()
                    binding.editButton.isEnabled = true

                } else {
                    binding.addImg.isEnabled = true
                    binding.editButton.isEnabled = true
                }

                //Almacenamos los colores de la paleta obtenidos
                val palette: List<String> = userChallenge.palette
                //y actualizamos la view en base a estos colores
                palette.let { colors ->
                    //Se crea un listado con las view que contendrán cada color
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

                if (userChallenge.complete && userChallenge.userImagePath.isNotEmpty()) {
                    // Si el reto está completo y tiene imagen, la cargamos desde Storage usando su url.
                    binding.userChallengeImage.loadImage(userChallenge.userImagePath)
                } else if (!userChallenge.complete && selectedImageUri != null) {
                    // Si no está completo pero ya seleccionó una foto local, se muestra en la ImageView.
                    binding.userChallengeImage.setImageURI(selectedImageUri)
                } else {
                    // Si no está completo o no tiene imagen predefinida, cargamos la imagen por defecto definida en la clase util para el ImageView.
                    binding.userChallengeImage.loadImage(null)
                }

            } else {
                try {
                    //Si no recibimos el ID del reto actual, volvemos a la pantalla anterior
                    //También sirve para cuando se elimina un reto volver inmediatamente a la pantalla anterior
                    findNavController().popBackStack()
                } catch (e: Exception) {
                    Log.e("UserChallengeDetails", "Error al navegar hacia atrás: ${e.message}")
                }
            }
        }
    }

    /**
     * Solicita confirmación al usuario para la eliminación del reto actual.
     * Si la confima delega la acción al [UserChallengesViewModel].
     */
    private fun deleteUserChallenge() {
        //Se obtiene el ID del reto actual
        val currentUserChal =
            userChallengesViewModel.userChallenges.value?.find { it.id == userChallengeId }
        userChallengeId?.let { id ->
            //Se lanza un alert dialog para confirmar la decisión de eliminar un reto guardado
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete_challenge_title)
                .setMessage(
                    getString(
                        R.string.delete_challenge_message,
                        binding.challengeTitle.text
                    )
                )
                .setPositiveButton(R.string.delete) { _, _ ->
                    //Se llama al userChallengesViewModel para borrar el reto
                    userChallengesViewModel.deleteUserChallenge(id, currentUserChal?.userImagePath)
                }
                .setNegativeButton(R.string.cancel, null)
                .show()

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


