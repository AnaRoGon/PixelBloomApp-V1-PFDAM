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
import dam.pfdam.pixelbloom.databinding.FragmentLoginBinding
import dam.pfdam.pixelbloom.ui.activity.MainActivity
import dam.pfdam.pixelbloom.viewmodel.AuthViewModel

/**
 * Fragmento encargado del inicio de sesión.
 *
 * Gestiona la validación visual de campos, la navegación al registro y el acceso
 * a la actividad principal tras un login exitoso gestionando la respuesta del [AuthViewModel].
 *
 * @property _binding Referencia privada al binding del layout.
 * @property binding Propiedad de acceso seguro al binding.
 * @property viewModel Lógica de negocio compartida para procesos de autenticación.
 */
class LoginFragment : Fragment(R.layout.fragment_login) {
    private var _binding: FragmentLoginBinding? = null
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
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }


    /**
     * Configura los listeners y los observadores del ViewModel
     * una vez la vista ha sido creada.
     *
     * @param view La vista del fragmento.
     * @param savedInstanceState Si no es null, contiene el estado previamente guardado de la actividad.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Se configuran los observadores y la limpieza reactiva de errores
        viewModelObservers()
        reactiveCleaning()
        setupListeners()
    }

    /**
     * Configura la observación de LiveDatas para reaccionar a cambios en el estado de AuthViewModel.
     * Gestiona tanto el éxito de la operación como la visualización de errores específicos.
     */
    private fun viewModelObservers() {
        // Observa si el login es correcto para navegar a la pantalla principal
        viewModel.authResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                goToMainActivity()
            }
        }

        // Muestra mensajes de error provenientes de Firebase y limpia el estado en el ViewModel
        viewModel.errorMsg.observe(viewLifecycleOwner) { errorResId ->
            errorResId?.let { resId ->
                val message = getString(resId)
                when (resId) {
                    R.string.error_invalid_email -> {
                        binding.outlinedTextInputEmail.error = message
                    }

                    R.string.error_weak_password -> {
                        binding.outlinedTextInputPassword.error = message
                    }
                    // Caso para el mensaje genérico de credenciales incorrectas
                    R.string.error_invalid_credential -> {
                        binding.outlinedTextInputEmail.error = message
                        binding.outlinedTextInputPassword.error = message
                    }

                    else -> Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }
                // Se limpian los errores en el ViewModel tras ser procesados
                viewModel.clearErrors()
            }
        }
    }

    /**
     * Centraliza la configuración de los clics de la interfaz.
     */
    private fun setupListeners() {
        // Al clicar en el botón para el login se lanza la función para el logeo
        binding.loginButton.setOnClickListener {
            handleLogin()
        }

        // Al clicar en el botón de signUp se navega al fragment para el Registro
        binding.signUpButton.setOnClickListener {
            goToSignUpFragment()
        }
    }

    /**
     * Extrae y valida los datos introducidos para iniciar el proceso de login.
     */
    private fun handleLogin() {
        clearUIErrors()
        val email = binding.outlinedTextInputEmail.editText?.text.toString().trim()
        val password = binding.outlinedTextInputPassword.editText?.text.toString().trim()
        //Si se han completado los campos se lanza la función de login del viewModel
        if (email.isNotEmpty() && password.isNotEmpty()) {
            viewModel.login(email, password)
        } else {
            //Si no se avisa al usuario de que los campos no pueden estar vacíos
            Toast.makeText(requireContext(), R.string.mandatory_fields_empty, Toast.LENGTH_SHORT)
                .show()
        }
    }

    /**
     * Realiza la transición al fragmento de registro de forma segura comprobando el destino actual.
     */
    private fun goToSignUpFragment() {
        val navAuthController = findNavController()
        if (navAuthController.currentDestination?.id == R.id.loginFragment) {
            findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
        }
    }


    /**
     * Limpia visualmente los errores de los campos para preparar una nueva validación.
     */
    private fun clearUIErrors() {
        binding.outlinedTextInputEmail.error = null
        binding.outlinedTextInputPassword.error = null
    }

    /**
     * Configura la limpieza reactiva de errores en los TextInputLayout al detectar cambios de texto.
     * Así, cuando el usuario escribe algo, el error desaparece.
     */
    private fun reactiveCleaning() {
        // Limpieza reactiva: desaparece el error al empezar a corregir
        binding.outlinedTextInputEmail.editText?.doOnTextChanged { _, _, _, _ ->
            binding.outlinedTextInputEmail.error = null
        }

        binding.outlinedTextInputPassword.editText?.doOnTextChanged { _, _, _, _ ->
            binding.outlinedTextInputPassword.error = null
        }
    }

    /**
     * Finaliza el flujo de autenticación y navega a la actividad principal.
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