package dam.pfdam.pixelbloom.view.fragment.feed.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dam.pfdam.pixelbloom.utils.loadImage
import dam.pfdam.pixelbloom.databinding.FeedItemBinding
import dam.pfdam.pixelbloom.data.model.Reference
import dam.pfdam.pixelbloom.utils.ImageUtils

/**
 * Adaptador para el RecyclerView que muestra la lista de referencias en el feed.
 *
 * Se encarga de vincular los datos de la lista de [Reference] con la vista [FeedItemBinding].
 * Implementa [DiffUtil] para optimizar las actualizaciones de la lista.
 *
 * @param referenceList Lista inicial de referencias a mostrar.
 * @param onClick Acción a ejecutar cuando se pulsa sobre un elemento del feed.
 */
class FeedAdapter(
    private var referenceList: List<Reference>,
    private val onClick: (Reference) -> Unit
) : RecyclerView.Adapter<FeedAdapter.FeedViewHolder>() {


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
    ): FeedViewHolder {
        val binding = FeedItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FeedViewHolder(binding)
    }

    /**
     * Vincula los datos de una referencia específica con su ViewHolder correspondiente.
     *
     * @param holder El ViewHolder que debe actualizarse.
     * @param position La posición del elemento dentro del conjunto de datos del adaptador.
     */
    override fun onBindViewHolder(
        holder: FeedViewHolder,
        position: Int
    ) {
        holder.bin(referenceList[position])
    }

    /**
     * Retorna el número total de elementos en la lista de referencias.
     *
     * @return El tamaño de la lista actual.
     */
    override fun getItemCount(): Int {
        return referenceList.size
    }


    /**
     * Utiliza [DiffUtil] para calcular los cambios entre la lista antigua y la nueva,
     * permitiendo actualizaciones parciales y eficientes.
     *
     * @param newList La nueva lista de referencias para mostrar.
     */
    fun updateData(newList: List<Reference>) {
        // Se crea una instancia de nuestra clase DiffUtil
        val diffCallback = DataDiffCallBack(this.referenceList, newList)

        // Calcula las diferencias entre las listas y actualiza sólo los elementos que han cambiado
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        // Se actualiza la lista interna del adaptador
        this.referenceList = newList

        // Manda las ordenes al RecyclerView indicando los elementos a modificar de la lista.
        diffResult.dispatchUpdatesTo(this)
    }

    /**
     * Clase auxiliar para calcular las diferencias entre dos listas de referencias.
     *
     * @property oldList Lista anterior de referencias.
     * @property newList Nueva lista de referencias.
     */
    inner class DataDiffCallBack(
        private val oldList: List<Reference>,
        private val newList: List<Reference>
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
            // Compara todos los campos que afectan a la vista.
            return oldList[oldPos] == newList[newPos] //Se utiliza el doble igual para comparar el objeto entero
        }
    }


    /**
     * ViewHolder que gestiona la visualización de un solo item de referencia.
     * Carga la imagen del usuario utilizando la clase [ImageUtils] que nos permite simplificar
     * la lógica de carga de imagenes con la librería de Picasso.
     * @property binding El binding del layout del item (feed_item.xml).
     */
    inner class FeedViewHolder(private val binding: FeedItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        /**
         * Vincula los datos de una referencia con los componentes de la vista.
         * Carga la imagen utilizando Picasso y configura el listener del click.
         *
         * @param reference El objeto [Reference] con los datos a mostrar.
         */
        fun bin(reference: Reference) {

            // Si la referencia tiene ruta de imagen, la cargamos usando la utilidad para el ImageView
            binding.feedImage.loadImage(reference.imagePath)

            binding.root.setOnClickListener {
                onClick(reference) //Se manda el item clickado al parámetro lambda
            }
        }
    }
}