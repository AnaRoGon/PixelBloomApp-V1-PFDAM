package dam.pfdam.pixelbloom.data.network

import dam.pfdam.pixelbloom.utils.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Singleton object que gestiona la configuración e instanciación del cliente [Retrofit].
 * Centraliza la provisión rápida y única de la API para toda la aplicación.
 */
object RetrofitInstance {
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Instancia lazy de [ApiService] configurada para realizar llamadas a la API.
     */
    val api: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}