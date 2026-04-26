package dam.pfdam.pixelbloom.data.network

import dam.pfdam.pixelbloom.data.model.PaletteData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Interfaz que define los endpoints para la comunicación con la API externa de paletas de colores.
 */
interface ApiService {
    /**
     * Busca paletas de colores basadas en un texto o etiqueta.
     *
     * @param searchQuery El término de búsqueda (ej. "forest", "warm").
     * @return Un objeto [Response] que contiene una lista de objetos [PaletteData].
     */
    //Petición get para obtener la response de la API
    @GET("palette/search")
    //Al ser una función de suspend, la llamada se ejecuta de manera asíncrona. Se ejecuta en segundo plano sin bloquear la UI
    suspend fun getPaletteColor(
        @Query("q") searchQuery: String
    ): Response<List<PaletteData>>
}