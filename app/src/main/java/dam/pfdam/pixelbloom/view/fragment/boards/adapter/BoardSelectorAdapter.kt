package dam.pfdam.pixelbloom.view.fragment.boards.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dam.pfdam.pixelbloom.utils.loadImage
import dam.pfdam.pixelbloom.R
import dam.pfdam.pixelbloom.data.model.Board
import dam.pfdam.pixelbloom.databinding.BoardItemSelectorBinding
import dam.pfdam.pixelbloom.utils.ImageUtils

/**
 * Adaptador para el selector de tableros.
 * Muestra una sola imagen de previsualización a la izquierda y el título a la derecha.
 * Se encarga de vincular los datos de la lista de [Board] con la vista [BoardItemSelectorBinding].
 *
 * @property boardsList Lista de tableros del usuario.
 * @property onClick Acción a realizar cuando se pulsa sobre un tablero.
 */
class BoardSelectorAdapter(
    private var boardsList: List<Board>,
    private val onClick: (Board) -> Unit
) : RecyclerView.Adapter<BoardSelectorAdapter.SelectorViewHolder>() {

    /**
     * Crea y configura el ViewHolder inflando el layout de cada item.
     *
     * @param parent El ViewGroup en el que se añadirá la nueva vista.
     * @param viewType El tipo de vista de la nueva vista.
     * @return Un nuevo BoardsViewHolder que contiene la vista del item.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectorViewHolder {
        val binding =
            BoardItemSelectorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SelectorViewHolder(binding)
    }

    /**
     * Vincula los datos de un board específico con su ViewHolder correspondiente.
     *
     * @param holder El ViewHolder que debe actualizarse.
     * @param position La posición del elemento dentro del conjunto de datos del adaptador.
     */
    override fun onBindViewHolder(holder: SelectorViewHolder, position: Int) {
        holder.bind(boardsList[position])
    }

    /**
     * Devuelve el número total de items en la lista de tableros.
     *
     * @return Tamaño de la lista [boardsList].
     */
    override fun getItemCount(): Int = boardsList.size

    /**
     * Utiliza [DiffUtil] para calcular los cambios entre la lista antigua y la nueva,
     * permitiendo actualizaciones parciales y eficientes.
     *
     * @param newList La nueva lista de boards para mostrar.
     */
    fun updateData(newList: List<Board>) {
        //Se crea una instancia de la clase DiffUtil
        val diffCallback = DataDiffCallBack(this.boardsList, newList)

        // Calcula las diferencias entre las listas y actualiza sólo los elementos que han cambiado
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        //Actualizamos la lista
        this.boardsList = newList
        //Manda las ordenes al RecyclerView indicando los elementos a modificar de la lista.
        diffResult.dispatchUpdatesTo(this)
    }

    /**
     * Clase auxiliar para calcular las diferencias entre dos listas de referencias.
     *
     * @property oldList Lista anterior de tableros.
     * @property newList Nueva lista de tableros.
     */
    inner class DataDiffCallBack(
        private val oldList: List<Board>,
        private val newList: List<Board>
    ) : DiffUtil.Callback() {
        //Obtenemos el tamaño de la lista antigua
        override fun getOldListSize(): Int = oldList.size

        //Y el de la lista nueva
        override fun getNewListSize(): Int = newList.size

        //Las comparamos
        override fun areItemsTheSame(oldPos: Int, newPos: Int) =
            boardsList[oldPos].id == newList[newPos].id

        //Se llama sólo si la lista cambia
        override fun areContentsTheSame(oldPos: Int, newPos: Int) =
            boardsList[oldPos] == newList[newPos]
    }

    /**
     * ViewHolder que gestiona la visualización de un solo item de boards.
     * Carga la imagen del usuario utilizando la clase [ImageUtils] que nos permite simplificar
     * la lógica de carga de imagenes con la librería de Picasso.
     * @property binding El binding del layout del item (board_item_selector.xml).
     */
    inner class SelectorViewHolder(private val binding: BoardItemSelectorBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * Asocia la información del tablero a las vistas de este item,
         * cargando su previsualización o una imagen por defecto.
         *
         * @param board Objeto de dominio con los datos del tablero.
         */
        fun bind(board: Board) {
            //Se pasa el titulo
            binding.textBoardTitleSelector.text = board.title

            //  Mostramos sólo la primera imagen para no sobrecargar el selector
            val previewUrl = board.imageRefs.firstOrNull()

            //Si hay url se muestra la primera imagen
            if (!previewUrl.isNullOrEmpty()) {
                binding.imgBoardPreviewSelector.loadImage(previewUrl)
            } else {
                //Si no se muestra una por defecto de la app
                binding.imgBoardPreviewSelector.setImageResource(R.drawable.default_reference_image)
            }
            //Se define el listener del click
            binding.root.setOnClickListener {
                onClick(board)
            }
        }
    }
}
