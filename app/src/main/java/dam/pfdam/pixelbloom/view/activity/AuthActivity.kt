package dam.pfdam.pixelbloom.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import dam.pfdam.pixelbloom.R
import dam.pfdam.pixelbloom.databinding.ActivityAuthBinding
import dam.pfdam.pixelbloom.utils.PreferenceHelper


/**
 * Actividad encargada de gestionar el flujo de autenticación de la aplicación.
 *
 * Verifica si el usuario ya ha iniciado sesión para redirigirlo a la pantalla principal
 * o muestra las pantallas de login/registro.
 *
 * @property _binding Referencia privada al binding.
 * @property binding Objeto de binding para acceder a los componentes de la vista.
 */
class AuthActivity : AppCompatActivity() {
    private var _binding: ActivityAuthBinding? = null
    private val binding get() = _binding!!

    /**
     * Función llamada al crear la actividad.
     * Comprueba si hay un usuario iniciado en la app con anterioridad y en función del resultado
     * redirecciona a la actividad principal o a la pantalla de inicio de sesión.
     *
     * @param savedInstanceState Si no es null, contiene el estado previamente guardado de la actividad.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Si al iniciar la app el usuario ya está registrado vamos directamente a la MainActivity
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            goToMainActivity()
            return
        }
        // Si no hay usuario registrado se infla y se manda a la pantalla de inicio de sesión
        // predefinida en el nav_graph
        _binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.auth)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Obtenemos el idioma por defecto desde sharedPreferences
        PreferenceHelper(this).applyPreferences()
    }

    /**
     * Navega a la actividad principal de la aplicación.
     * Inicia [MainActivity] y finaliza la actividad actual para evitar volver atrás.
     */
    fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

}