package dam.pfdam.pixelbloom.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.util.Log
import java.io.ByteArrayOutputStream

/**
 * Clase para simplificar la subida de imagenes al storage de firebase pasando por una compresión
 * para tener mejor control de calidad y tamaño en toda la app.
 */
object ImageUtils {

    /**
     * Comprime y redimensiona una imagen desde una URI local.
     * Escala la imagen para que ninguna dimensión supere los 1200px y la comprime al 70% de calidad.
     *
     * @param context Contexto de la aplicación para acceder al contentResolver.
     * @param imageUri URI de la imagen a procesar.
     * @return Array de bytes representando la imagen procesada en formato JPEG o null si falla.
     */
    fun compressImage(context: Context, imageUri: Uri): ByteArray? {
        var bitmap: Bitmap? = null
        val baos = ByteArrayOutputStream()

        try {
            //Utilizamos ImageDecoder para un procesado eficiente
            val source = ImageDecoder.createSource(context.contentResolver, imageUri)

            //Decodificamos haciendo un redimensionamiento para ahorrar memoria
            bitmap = ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                val maxDimension = 1200
                val width = info.size.width
                val height = info.size.height
                //Si es necesario redimensionar se hace
                if (width > maxDimension || height > maxDimension) {
                    val ratio = width.toFloat() / height.toFloat()
                    if (width > height) {
                        decoder.setTargetSize(maxDimension, (maxDimension / ratio).toInt())
                    } else {
                        decoder.setTargetSize((maxDimension * ratio).toInt(), maxDimension)
                    }
                }
                //Se reserva memoria para la imagen
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
            //Comprimimos la imagen al 70% en calidad JPG.
            //Si indicasemos un formato como PNG no se va a comprimir porque este formato es lossless (sin perdida de calidad).
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos)
            //Devolvemos el array de bytes de la imagen comprimida
            return baos.toByteArray()

        } catch (e: Exception) {
            //Se captura cualquier error que pueda ocurrir y se devuelve null
            Log.e("ImageUtils", "Error procesando la imagen: ${e.message}")
            return null
        } finally {
            //Liberamos la memoria de la imagen siempre
            bitmap?.recycle()
        }
    }
}
