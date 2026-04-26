package dam.pfdam.pixelbloom.view.fragment.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dam.pfdam.pixelbloom.R
import dam.pfdam.pixelbloom.databinding.FragmentSignUpBinding
import dam.pfdam.pixelbloom.ui.activity.MainActivity
import dam.pfdam.pixelbloom.viewmodel.AuthViewModel

/**
 * Fragmento encargado del registro de nuevos usuarios en la aplicación.
 *
 * Permite al usuario crear una cuenta mediante email y contraseña, validando
 * la coincidencia de credenciales y gestionando la respuesta del [AuthViewModel].
 *
 * @property _binding Referencia al binding del layout.
 * @property binding Acceso seguro a la vista.
 * @property viewModel Lógica de negocio compartida para procesos de autenticación.
 */
class SignUpFragment : Fragment(R.layout.fragment_sign_up) {
    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by activityViewModels() //El uso de activityViewModels() permite compartir el ViewModel entre fragmentos y actividades

    /**
     * Infla el layout del fragmento y configura el binding.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Configura los observadores para el resultado del registro y la limpieza reactiva de los campos.
     * Resetea los mensajes de error
     *
     * @param view La vista del fragmento.
     * @param savedInstanceState Si no es null, contiene el estado previamente guardado de la actividad.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Escuchar el resultado del registro que nos devuelve el viewModel
        viewModelObservers()
        reactiveCleaning()
        setupListeners()
    }

    /**
     * Observa los estados del ViewModel para reaccionar a la creación de cuenta o errores.
     */
    private fun viewModelObservers() {

        // Observa si el login es correcto para navegar a la pantalla principal
        viewModel.authResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                goToMainActivity()
            }
        }
        //Muestra los mensajes de error provenientes de Firebase y limpia el estado en el ViewModel
        viewModel.errorMsg.observe(viewLifecycleOwner) { errorResId ->
            errorResId?.let { resId ->
                val message = getString(resId)
                when (resId) {
                    R.string.error_invalid_email, R.string.error_email_already_in_use -> {
                        binding.outlinedTextFieldEmail.error = message
                    }

                    R.string.error_weak_password -> {
                        binding.outlinedTextFieldPassword.error = message
                    }

                    R.string.error_invalid_credential -> {
                        binding.outlinedTextFieldEmail.error = message
                        binding.outlinedTextFieldPassword.error = message
                    }

                    else -> Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }
                viewModel.clearErrors()
            }
        }
    }

    /**
     * Centraliza la configuración de los clics de la interfaz y navegacion.
     */
    private fun setupListeners() {
        //  Al clicar en el botón para el registro se lanza la función para el registro
        binding.signUpButton.setOnClickListener {
            handleSignUp()
        }

        // Al clicar el botón de login se navega hacia atrás
        binding.textButtonLogin.setOnClickListener {
            findNavController().popBackStack()
        }

        // Al clicar sobre la flecha de navegación se va hacia atrás
        binding.signUpToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    /**
     * Recolecta los datos de la UI y realiza validaciones locales antes
     * de delegar el registro en el ViewModel.
     */
    private fun handleSignUp() {
        //Se limpian los errores
        clearUIErrors()
        // Se obtienen los datos de los campos
        val email = binding.outlinedTextFieldEmail.editText?.text.toString().trim()
        val password = binding.outlinedTextFieldPassword.editText?.text.toString().trim()
        val confirmPassword =
            binding.outlinedTextFieldConfirmPassword.editText?.text.toString().trim()

        // Se comprueba que los campos no estén vacíos
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(requireContext(), R.string.complete_all_fields, Toast.LENGTH_SHORT)
                .show()
            return
        }

        // Se comprueba que el email tenga un formato válido
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.outlinedTextFieldEmail.error = getString(R.string.error_invalid_email)
            return
        }

        // Se verifica la longitud de la contraseña
        if (password.length < 6) {
            binding.outlinedTextFieldPassword.error = getString(R.string.error_weak_password)
            return
        }

        // Se comprueba que las contraseñas coinciden
        if (password != confirmPassword) {
            binding.outlinedTextFieldConfirmPassword.error =
                getString(R.string.passwords_dont_match)
            return
        }

        // Si todas las validaciones locales pasan, procedemos al registro
        //Aunque firebase ya realiza una validación, de este modo evitamos una llamada a Firebase innecesaria
        viewModel.register(email, password)
    }


    /**
     * Limpia visualmente los errores de los campos para preparar una nueva validación.
     */
    private fun clearUIErrors() {
        binding.outlinedTextFieldEmail.error = null
        binding.outlinedTextFieldPassword.error = null
        binding.outlinedTextFieldConfirmPassword.error = null
    }

    /**
     * Configura la limpieza reactiva de errores en los TextInputLayout al detectar cambios de texto.
     */
    private fun reactiveCleaning() {
        binding.outlinedTextFieldEmail.editText?.doOnTextChanged { _, _, _, _ ->
            binding.outlinedTextFieldEmail.error = null
        }
        binding.outlinedTextFieldPassword.editText?.doOnTextChanged { _, _, _, _ ->
            binding.outlinedTextFieldPassword.error = null
        }
        binding.outlinedTextFieldConfirmPassword.editText?.doOnTextChanged { _, _, _, _ ->
            binding.outlinedTextFieldConfirmPassword.error = null
        }
    }

    /**
     * Finaliza el flujo de autenticación y lanza la actividad principal de la aplicación.
     */
    private fun goToMainActivity() {
        val intent = Intent(requireActivity(), MainActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    /**
     * Libera el binding al destruir la vista para evitar fugas de memoria.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        // Limpiamos el binding para evitar fugas de memoria
        _binding = null
        //Se limpian los errores
        viewModel.clearErrors()
    }

}

