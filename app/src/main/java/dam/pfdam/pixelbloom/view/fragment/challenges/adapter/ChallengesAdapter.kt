package dam.pfdam.pixelbloom.view.fragment.challenges.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dam.pfdam.pixelbloom.databinding.ChallengesItemBinding
import dam.pfdam.pixelbloom.data.model.Challenge
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.DiffUtil
import dam.pfdam.pixelbloom.R


/**
 * Adaptador para el RecyclerView que muestra la lista de challenges en el feed.
 *
 * Se encarga de vincular los datos de la lista de [Challenge] con la vista [ChallengesItemBinding].
 *
 * @param challengeList Lista inicial de challenges a mostrar.
 * @param onClick Acción a ejecutar cuando se pulsa sobre un elemento del feed.
 */
class ChallengesAdapter(
    private var challengeList: List<Challenge>,
    private val onClick: (Challenge) -> Unit
) : RecyclerView.Adapter<ChallengesAdapter.ChallengesViewHolder>() {


    /**
     * Crea y configura el ViewHolder inflando el layout de cada item.
     *
     * @param parent El ViewGroup en el que se añadirá la nueva vista.
     * @param viewType El tipo de vista de la nueva vista.
     * @return Un nuevo FeedViewHolder que contiene la vista del item.
     */
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChallengesViewHolder {
        val binding =
            ChallengesItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChallengesViewHolder(binding)
    }

    /**
     * Vincula los datos de un challenge específico con su ViewHolder correspondiente.
     *
     * @param holder El ViewHolder que debe actualizarse.
     * @param position La posición del elemento dentro del conjunto de datos del adaptador.
     */
    override fun onBindViewHolder(
        holder: ChallengesViewHolder,
        position: Int
    ) {
        holder.bin(challengeList[position], position)
    }

    /**
     * Retorna el número total de elementos en la lista de challenges.
     *
     * @return El tamaño de la lista actual.
     */
    override fun getItemCount(): Int {
        return challengeList.size
    }

    /**
     * Utiliza [DiffUtil] para calcular los cambios entre la lista antigua y la nueva,
     * permitiendo actualizaciones parciales y eficientes.
     *
     * @param newList La nueva lista de retos para mostrar.
     */
    fun updateData(newList: List<Challenge>) {
        // Se crea una instancia de nuestra clase DiffUtil
        val diffCallback = DataDiffCallback(this.challengeList, newList)

        // Calcula las diferencias entre las listas y actualiza sólo los elementos que han cambiado
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        // Se actualiza la lista interna del adaptador
        // Es importante hacerlo DESPUÉS de calcular las diferencias.
        this.challengeList = newList

        // Manda las ordenes al RecyclerView indicando los elementos a modificar de la lista.
        diffResult.dispatchUpdatesTo(this)
    }

    /**
     * Clase auxiliar para calcular las diferencias entre dos listas de challenges.
     *
     * @property oldList Lista anterior de challenges.
     * @property newList Nueva lista de challenges.
     */
    inner class DataDiffCallback(
        private val oldList: List<Challenge>,
        private val newList: List<Challenge>
    ) : DiffUtil.Callback() {

        // Devuelve el tamaño de la lista antigua
        override fun getOldListSize(): Int = oldList.size

        // Devuelve el tamaño de la lista nueva
        override fun getNewListSize(): Int = newList.size

        // DiffUtil compara el id de los elementos y revisa cuales han cambiado para actualizarlos
        override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean {
            return oldList[oldPos].id == newList[newPos].id
        }

        // Se llama solo si areItemsTheSame() devuelve true.
        // DiffUtil usa esto para saber si ha cambiado algun dato interno del objeto.
        override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean {
            // Si la posición ha cambiado, necesitamos actualizar la vista para refrescar el número (ej: #1, #2)
            if (oldPos != newPos) return false
            // Compara todos los campos que afectan a la vista.
            return oldList[oldPos] == newList[newPos] //Se utiliza el doble igual para comparar el objeto entero
        }
    }


    /**
     * ViewHolder que gestiona la visualización de un solo item de tipo Challenge.
     *
     * @property binding El binding del layout del item (challenges_item.xml).
     */
    inner class ChallengesViewHolder(private val binding: ChallengesItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        /**
         * Vincula los datos de un challenge con los componentes de la vista.
         * Carga la imagen utilizando Picasso y configura el listener del click.
         *
         * @param challenge El objeto [Challenge] con los datos a mostrar.
         */
        fun bin(challenge: Challenge, position: Int) {
            //Obtenemos la posición del onBindViewHolder para mostrarla con el título incrementandola en 1
            binding.challengeTitle.text = binding.root.context.getString(
                R.string.challenge_number_prefix,
                position + 1,
                challenge.title
            )

            //Almacenamos los colores de la paleta obtenidos
            val palette: List<String> = challenge.palette
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
                //Al utilizar zip hacemos que cada color quede enlazado a la vista correspondiente y mutamos el background para no afectar a otras pantallas
                colors.zip(colorViews).forEach { (color, view) ->
                    //Usamos mutate() para crear una instancia única de este fondo y no afectar al resto de la app
                    view.background.mutate().setTint(color.toColorInt())
                }
            }
            binding.root.setOnClickListener {
                onClick(challenge) //Se manda el item clickado al parámetro lambda
            }
        }
    }

}

