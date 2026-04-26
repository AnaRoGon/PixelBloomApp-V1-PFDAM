package dam.pfdam.pixelbloom.utils

import android.widget.ImageView
import com.squareup.picasso.Picasso
import dam.pfdam.pixelbloom.R

/**
 * Se trata de una función de extensión para cargar imágenes en un ImageView.
 * que carga una imagen a partir de una URL o URI string.
 * Nos ayuda a centralizar el uso de Picasso en la app para no repetir código.
 * Si la URL es nula o vacía, carga una imagen por defecto.
 *
 * @param url La ruta de la imagen a cargar.
 * @param placeholder El ID del recurso de la imagen que se mostrará por defecto o mientras carga.
 * @param errorImg El ID del recurso de la imagen que se mostrará en caso de error.
 * @param isCenterCrop Opcional para indicar si se quiere aplicar centerCrop en lugar de centerInside.
 */
fun ImageView.loadImage(url: String?, placeholder: Int = R.drawable.default_challenge_img, errorImg: Int = R.drawable.error_img, isCenterCrop: Boolean = true) {
    val requestCreator = if (url.isNullOrEmpty()) {
        Picasso.get().load(placeholder).error(errorImg).fit()
    } else {
        Picasso.get().load(url).error(errorImg).fit()
    }
    
    if (isCenterCrop) {
        requestCreator.centerCrop().into(this)
    } else {
        requestCreator.centerInside().into(this)
    }
}
