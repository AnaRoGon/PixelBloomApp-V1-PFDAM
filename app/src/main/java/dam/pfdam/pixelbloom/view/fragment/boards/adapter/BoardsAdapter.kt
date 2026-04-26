package dam.pfdam.pixelbloom.view.fragment.boards.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dam.pfdam.pixelbloom.utils.loadImage
import dam.pfdam.pixelbloom.R
import dam.pfdam.pixelbloom.data.model.Board
import dam.pfdam.pixelbloom.databinding.BoardItemBinding
import dam.pfdam.pixelbloom.utils.ImageUtils



/**
 * Adaptador para el RecyclerView que muestra la lista de boards del usuario.
 *
 * Se encarga de vincular los datos de la lista de [Board] con la vista [BoardItemBinding].
 * Implementa [DiffUtil] para optimizar las actualizaciones de la lista.
 *
 * @param boardsList Lista inicial de boards a mostrar.
 * @param onClick Acción a ejecutar cuando se pulsa sobre un tablero (board).
 */
class BoardsAdapter(
    private var boardsList: List<Board>,
    private val onClick: (Board) -> Unit
) : RecyclerView.Adapter<BoardsAdapter.BoardsViewHolder>() {


    /**
     * Crea y configura el ViewHolder inflando el layout de cada item.
     *
     * @param parent El ViewGroup en el que se añadirá la nueva vista.
     * @param viewType El tipo de vista de la nueva vista.
     * @return Un nuevo BoardsViewHolder que contiene la vista del item.
     */
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BoardsViewHolder {
        val binding = BoardItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BoardsViewHolder(binding)
    }

    /**
     * Vincula los datos de un board específico con su ViewHolder correspondiente.
     *
     * @param holder El ViewHolder que debe actualizarse.
     * @param position La posición del elemento dentro del conjunto de datos del adaptador.
     */
    override fun onBindViewHolder(
        holder: BoardsViewHolder,
        position: Int
    ) {
        holder.bin(boardsList[position])
    }

    /**
     * Retorna el número total de elementos en la lista de boards.
     *
     * @return El tamaño de la lista actual.
     */
    override fun getItemCount(): Int {
        return boardsList.size
    }


    /**
     * Utiliza [DiffUtil] para calcular los cambios entre la lista antigua y la nueva,
     * permitiendo actualizaciones parciales y eficientes.
     *
     * @param newList La nueva lista de boards para mostrar.
     */
    fun updateData(newList: List<Board>) {
        // Se crea una instancia de nuestra clase DiffUtil
        val diffCallback = DataDiffCallback(this.boardsList, newList)

        // Calcula las diferencias entre las listas y actualiza sólo los elementos que han cambiado
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        // Se actualiza la lista interna del adaptador
        this.boardsList = newList

        // Manda las ordenes al RecyclerView indicando los elementos a modificar de la lista.
        diffResult.dispatchUpdatesTo(this)
    }


    /**
     * Clase auxiliar para calcular las diferencias entre dos listas de boards.
     *
     * @property oldList Lista anterior de boards.
     * @property newList Nueva lista de boards.
     */
    inner class DataDiffCallback(
        private val oldList: List<Board>,
        private val newList: List<Board>
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
            return oldList[oldItemPosition] == newList[newItemPosition] //Se utiliza el doble igual para comparar el objeto entero
        }
    }


    /**
     * ViewHolder que gestiona la visualización de un solo item de boards.
     * Carga la imagen del usuario utilizando la clase [ImageUtils] que nos permite simplificar
     * la lógica de carga de imagenes con la librería de Picasso.
     * @property binding El binding del layout del item (board_item.xml).
     */
    inner class BoardsViewHolder(private val binding: BoardItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        /**
         * Vincula los datos de un board con los componentes de la vista.
         * Carga la imagen utilizando Picasso y configura el listener del click.
         *
         * @param board El objeto [Board] con los datos a mostrar.
         */
        fun bin(board: Board) {
            // Pasamos la información de los boards al layout
            binding.textBoardTitle.text = board.title
            binding.numberOfBloomsText.text = board.imageRefs.size.toString() + " blooms"

            // Obtenemos un listado de los elementos del diseño del board
            // con los imageView para poder acceder a ellos más fácilmente
            val images = board.imageRefs
            val imageViews = listOf(
                binding.startImg,
                binding.startBottomImg,
                binding.endBottomImg
            )

            // Definimos un color por defecto distinto para cada posición
            // para que el diseño se vea coherente con el resto de la app si el
            // tablero no tiene al menos 3 referencias añadidas
            val defaultColors = listOf(
                R.color.banana_light,
                R.color.inactive_blue,
                R.color.light_purple
            )

            // Recorremos los ImageView del board para poblarlos con las referencias
            imageViews.forEachIndexed { index, imageView ->
                if (images.isNotEmpty() && index < images.size) {
                    val path = images[index]
                    // Cargamos la imagen desde el Storage
                    imageView.loadImage(path)
                } else {
                    // Establecemos un color de fondo distinto según el índice
                    val colorRes = defaultColors.getOrElse(index) { R.color.neutral_200 } //Condición de seguridad para que no de error y ponga un color por defecto si no hay indice en la lista
                    imageView.setImageResource(colorRes)
                }
            }

            binding.root.setOnClickListener {
                onClick(board) //Se manda el item clickado al parámetro lambda
            }
        }
    }
}