package dam.pfdam.pixelbloom.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dam.pfdam.pixelbloom.R
import dam.pfdam.pixelbloom.databinding.FragmentLanguageSelectorBinding
import dam.pfdam.pixelbloom.utils.PreferenceHelper

/**
 * Fragmento de tipo BottomSheet que permite al usuario seleccionar el idioma de la aplicación.
 *
 * Utiliza SharedPreferences para persistir la elección y recrea la actividad
 * para aplicar los cambios de localización de forma inmediata.
 *
 * @property _binding Referencia privada al binding del layout.
 * @property binding Propiedad de acceso seguro al binding (non-null).
 */
class LanguageSelectorFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentLanguageSelectorBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLanguageSelectorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Se inicializan los recyclerView
        setupRecyclerView()
        val currentLang = PreferenceHelper(requireContext()).getLanguage()

        // Resaltamos la opción actual visualmente
        if (currentLang == "es") {
            binding.btnSpanish.setBackgroundColor(requireContext().getColor(R.color.neutral_200))
        } else {
            binding.btnEnglish.setBackgroundColor(requireContext().getColor(R.color.neutral_200))
        }
    }

    /**
     * Configura los RecyclerViews con sus adaptadores personalizados y LayoutManagers.
     */
    private fun setupRecyclerView() {
        // Listeners para los botones de idioma
        binding.btnSpanish.setOnClickListener {
            PreferenceHelper(requireContext()).changeLanguage("es")
            restartActivity()
        }

        binding.btnEnglish.setOnClickListener {
            PreferenceHelper(requireContext()).changeLanguage("en")
            restartActivity()
        }
    }

    /**
     * Reinicia la actividad anfitriona para que los nuevos recursos de idioma se apliquen
     * evitando el parpadeo negro junto a las implementaciones en el theme.
     *
     */
    private fun restartActivity() {
        val activity = requireActivity()
        val intent = activity.intent
        activity.finish()
        activity.startActivity(intent)
    }

    /**
     * Libera la referencia al binding para evitar fugas de memoria al destruir la vista.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


