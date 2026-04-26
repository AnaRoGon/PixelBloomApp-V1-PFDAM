package dam.pfdam.pixelbloom.view.fragment.boards.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dam.pfdam.pixelbloom.utils.loadImage
import dam.pfdam.pixelbloom.data.model.UserChallenge
import dam.pfdam.pixelbloom.databinding.UserChallengeItemBinding
import dam.pfdam.pixelbloom.utils.ImageUtils

/**
 * Adaptador para el RecyclerView que muestra la lista de retos del usuario en el carrusel horizontal.
 *
 * Se encarga de vincular los datos de la lista de [UserChallenge] con la vista [UserChallengeItemBinding].
 * Implementa [DiffUtil] para optimizar las actualizaciones de la lista.
 *
 * @param challengesList Lista inicial de retos a mostrar.
 * @param onClick Acción a ejecutar cuando se pulsa sobre un reto del carrusel.
 */
class UserChallengesAdapter(
    private var challengesList: List<UserChallenge>,
    private val onClick: (UserChallenge) -> Unit
) : RecyclerView.Adapter<UserChallengesAdapter.ChallengeViewHolder>() {

    /**
     * Crea y configura el ViewHolder inflando el layout de cada item.
     *
     * @param parent El ViewGroup en el que se añadirá la nueva vista.
     * @param viewType El tipo de vista de la nueva vista.
     * @return Un nuevo ChallengeViewHolder que contiene la vista del item.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChallengeViewHolder {
        val binding =
            UserChallengeItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChallengeViewHolder(binding)
    }

    /**
     * Vincula los datos de un reto específico con su ViewHolder correspondiente.
     *
     * @param holder El ViewHolder que debe actualizarse.
     * @param position La posición del elemento dentro del conjunto de datos del adaptador.
     */
    override fun onBindViewHolder(holder: ChallengeViewHolder, position: Int) {
        holder.bin(challengesList[position])
    }

    /**
     * Retorna el número total de elementos en la lista de retos del usuario.
     *
     * @return El tamaño de la lista actual.
     */
    override fun getItemCount(): Int {
        return challengesList.size
    }

    /**
     * Utiliza [DiffUtil] para calcular los cambios entre la lista antigua y la nueva,
     * permitiendo actualizaciones parciales y eficientes.
     *
     * @param newList La nueva lista de retos para mostrar.
     */
    fun updateData(newList: List<UserChallenge>) {
        // Se crea una instancia de nuestra clase DiffUtil
        val diffCallback = DataDiffCallback(this.challengesList, newList)
        // Calcula las diferencias entre las listas y actualiza sólo los elementos que han cambiado
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        // Se actualiza la lista interna del adaptador
        this.challengesList = newList
        // Manda las ordenes al RecyclerView indicando los elementos a modificar de la lista.
        diffResult.dispatchUpdatesTo(this)
    }

    /**
     * Clase auxiliar para calcular las diferencias entre dos listas de retos de usuario.
     *
     * @property oldList Lista anterior de retos.
     * @property newList Nueva lista de retos.
     */
    inner class DataDiffCallback(
        private val oldList: List<UserChallenge>,
        private val newList: List<UserChallenge>
    ) : DiffUtil.Callback() {
        // Devuelve el tamaño de la lista antigua
        override fun getOldListSize(): Int = oldList.size
        // Devuelve el tamaño de la lista nueva
        override fun getNewListSize(): Int = newList.size
        // DiffUtil compara el id de los elementos y revisa cuales han cambiado para actualizarlos
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }
        // Se llama solo si areItemsTheSame() devuelve true.
        // DiffUtil usa esto para saber si ha cambiado algun dato interno del objeto.
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            // Compara todos los campos que afectan a la vista.
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }

    /**
     * ViewHolder que gestiona la visualización de un solo item de reto del usuario.
     *
     * @property binding El binding del layout del item (user_challenge_item.xml).
     */
    inner class ChallengeViewHolder(private val binding: UserChallengeItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * Vincula los datos de un user challenge con los componentes de la vista.
         * Carga la imagen del usuario utilizando la clase [ImageUtils] que nos permite simplificar
         * la lógica de carga de imagenes con la librería de Picasso.
         *
         * @param challenge El objeto [UserChallenge] con los datos a mostrar.
         */
        fun bin(challenge: UserChallenge) {
            // Si el usuario ha subido una imagen se mostrará en el imageView
            if (challenge.userImagePath.isNotEmpty()) {
                binding.challengeDescription.visibility = View.GONE
                binding.challengeImage.loadImage(challenge.userImagePath)
            } else {
                // Si no hay imagen subida se mostrará el detalle del texto a modo decorativo.
                // Así es más sencillo localizar localizar cada challenge aceptado.
                binding.challengeDescription.visibility = View.VISIBLE
                //Hacemos que el background sea el tercer color de la paleta del reto
                //para dar un toque más llamativo a la UI.
                val color = challenge.palette[2].toColorInt()
                binding.challengeDescription.setBackgroundColor(color)
                binding.challengeDescription.text = challenge.description
            }

            binding.root.setOnClickListener {
                onClick(challenge)
            }
        }
    }
}
