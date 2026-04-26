package dam.pfdam.pixelbloom.view.fragment.onBoarding.adapter

import android.view.View
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dam.pfdam.pixelbloom.R
import dam.pfdam.pixelbloom.data.ui.OnBoarding
import dam.pfdam.pixelbloom.databinding.FragmentOnboardingPagesBinding

/**
 * Adaptador que gestiona y muestra la lista de pantallas en el flujo de OnBoarding.
 *
 * Recibe una lista de objetos [OnBoarding] y se encarga de inflar y vincular los datos
 * correspondientes a cada página. También controla la lógica del botón final.
 *
 * @property onBoardingList Lista estática con los datos de cada pantalla del onboarding.
 * @property onFinishClick Acción (lambda) que se ejecutará al pulsar el botón final del onboarding.
 */
class OnBoardingAdapter(
    private val onBoardingList: List<OnBoarding>,
    private val onFinishClick: () -> Unit
) : RecyclerView.Adapter<OnBoardingAdapter.OnBoardViewHolder>() {

    /**
     * Infla el layout de las pantallas individuales del onboarding y devuelve un [OnBoardViewHolder].
     *
     * @param parent El contenedor (ViewGroup) padre adonde se adjuntará la vista.
     * @param viewType El tipo identificador de la vista.
     * @return Un nuevo OnBoardViewHolder.
     */
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OnBoardViewHolder {
        val binding =
            FragmentOnboardingPagesBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return OnBoardViewHolder(binding)
    }

    /**
     * Vincula los datos de una pantalla de onboarding concreta con su ViewHolder.
     *
     * @param holder El ViewHolder a actualizar.
     * @param position La posición del elemento en la lista [onBoardingList].
     */
    override fun onBindViewHolder(
        holder: OnBoardViewHolder,
        position: Int
    ) {
        holder.bind(onBoardingList[position])
    }

    /**
     * Devuelve el total de páginas que tendrá el onboarding.
     *
     * @return Tamaño de la lista [onBoardingList].
     */
    override fun getItemCount(): Int {
        return onBoardingList.size
    }

    /**
     * ViewHolder que mantiene las referencias a los elementos de la interfaz de una
     * página individual del onboarding.
     * Nos permite usar una única página e ir actualizando sus elementos para simplificar el onboarding.
     *
     * @property binding Enlace con el layout (fragment_onboarding_pages.xml).
     */
    inner class OnBoardViewHolder(
        private val binding: FragmentOnboardingPagesBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        private val title = binding.onboardingTitle
        private val button = binding.letsGoButton

        /**
         * Asigna la imagen, título y descripción a la vista según los datos provistos.
         * Además, muestra el botón para finalizar el onboarding únicamente en la última página.
         *
         * @param onBoarding Objeto que encapsula la información de la página actual.
         */
        fun bind(onBoarding: OnBoarding) = with(binding) {
            //Se cargan los datos
            imageOnboarding.setImageResource(onBoarding.image)
            title.text = onBoarding.title
            description.text = onBoarding.description

            //Si es la primera página, se muestra el nombre de la app
            if (bindingAdapterPosition == 0) {
                appNameSubtitle.visibility = View.VISIBLE

            } else {
                //Si no, se oculta
                appNameSubtitle.visibility = View.GONE
            }

            //Si es la última página, se muestra el botón para finalizar el onboarding
            if (bindingAdapterPosition == onBoardingList.lastIndex) {
                button.visibility = View.VISIBLE
                button.text = root.context.getString(R.string.let_s_go)
                button.setOnClickListener {
                    onFinishClick()
                }
            } else {
                //Si no, se oculta
                button.visibility = View.GONE
            }
        }
    }
}