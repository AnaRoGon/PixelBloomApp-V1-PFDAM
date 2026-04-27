# Pixel Bloom
Se trata de una aplicación donde los usuarios pueden encontrar inspiración en Pixel Art, crear y organizar tableros personalizados, y participar en retos diarios que le ayuden a inspirarse en la creación de obras originales y mejorar día a día su técnica.

# Características principales

## Autenticación de Usuarios
Desde esta pantalla los usuarios pueden registrarse e iniciar sesión de forma segura. La aplicación mantiene la sesión activa para facilitar el acceso rápido.

<img width="270" height="600" alt="00" src="https://github.com/user-attachments/assets/2093a7f5-f1c6-4e24-ae8d-b5e55f1e2523" />

## Feed de Inspiración
Desde esta pantalla los usuarios pueden explorar un feed constante de nuevas referencias e ideas de Pixel Art. Descubrir estilos y temáticas diferentes para nutrir su creatividad. 

<img width="270" height="600" alt="01" src="https://github.com/user-attachments/assets/72c7ba86-f53c-4fe3-8378-46df339f64f9" />

## Detalles de una Referencia
Al seleccionar una referencia, podrás ver sus detalles completos y guardarla en tus propios tableros.

<img width="270" height="600" alt="05" src="https://github.com/user-attachments/assets/5e3b9d22-a3c2-4194-8990-63eec54ea923" />

## Tableros Personalizados y retos del usuario
Desde aquí el usuario puede crear, organizar y gestionar tus propios tableros. Se podrán tener organizadas las referencias favoritas del feed o el historial de retos completados. 

<img width="270" height="600" alt="02" src="https://github.com/user-attachments/assets/4f88f0db-5fe5-4bef-8559-8df8ed05c57b" />

## Detalles de un reto de usuario
Al seleccionar un reto podrá acceder al detalle del mismo y completarlo. Una vez completado el usuario podrá acceder siempre que quiera a visualizarlo o editarlo.

<img width="270" height="600" alt="06" src="https://github.com/user-attachments/assets/8969f0dd-430e-45ba-b237-933c89ca3a6a" />

Si el reto no está completado la pantalla mostrará una imagen por defecto e indicará que el reto aún no se ha completado.

<img width="270" height="600" alt="07" src="https://github.com/user-attachments/assets/471190d1-964a-44c5-8210-1f71c3867ee2" />

## Paletas de Colores Generativas
Para ayudar a los usuarios sus creaciones, la aplicación incluye una sección de retos diarios/semanales. Esta sección les permite acceder a una serie de propuestas inspiradoras para crear nuevos proyectos de Pixel Art.

<img width="270" height="600" alt="03" src="https://github.com/user-attachments/assets/0ac625b0-7c38-42e9-9ba3-e123a6c2564d" />

## Ajustes y Personalización
Desde el menú de ajustes se podrá editar el tema de la aplicación, idioma, acceder a la información del desarrollador de la app y cerrar la sesión.

<img width="270" height="600" alt="04" src="https://github.com/user-attachments/assets/c7e86980-278f-414b-9948-5ecd82163fa0" />

# Tecnologías utilizadas

Para el desarrollo de esta aplicación se han empleado las siguientes tecnologías:
* Uso de **Firebase Authentication** para la gestión segura de usuarios.
* Uso de **Firebase Firestore** como base de datos en la nube para guardar tableros, referencias y retos.
* Uso de **Firebase Storage** para el almacenamiento de imágenes.
* **Retrofit** para el consumo de la API externa utilizada en la generación de paletas de color.
* **Picasso** para la carga eficiente de imágenes desde URLs de internet.
* **RecyclerView** y **CardView** para la visualización fluida de listas (feed, tableros, retos).
* **Navigation Component** para una navegación robusta y estructurada entre los diferentes fragmentos.
* **Corrutinas** y **LiveData** para el manejo asíncrono y reactivo de los datos.
* **Photo Picker** para el acceso a la galería del dispositivo de forma segura. 
* **JUnit y MockK** para la ejecución de pruebas unitarias.
* **Espresso ** Para la ejecución de pruebas integrales de UI.

# Instrucciones de uso

Para probar la aplicación:
1. Copia el enlace del repositorio: `https://github.com/AnaRoGon/PixelBloomApp-V1-PFDAM.git`
2. En Android Studio, selecciona: `File -> New -> Project from Version Control` y pega el enlace.
3. Asegúrate de configurar el archivo `google-services.json` necesario para que Firebase funcione correctamente en tu entorno local.
4. Ejecuta el proyecto en tu emulador o dispositivo físico.
